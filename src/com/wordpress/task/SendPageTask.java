package com.wordpress.task;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.EncodedImage;

import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.Queue;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.NewMediaObjectConn;
import com.wordpress.xmlrpc.page.EditPageConn;
import com.wordpress.xmlrpc.page.NewPageConn;

public class SendPageTask extends TaskImpl implements Observer {

	private Queue executionQueue = null; // queue of BlogConn
	private boolean stopping = false;

	private int draftPageFolder; 
	private Hashtable remoteFileInfo = new Hashtable(); //contain files info on the WP server (used after sending MM files to the blog)
	private final Blog blog;
	private Page page;
	
	public SendPageTask(Blog blog, Page page, int draftPageFolder, Queue executionQueue) {
		this.blog = blog;
		this.page = page;
		this.draftPageFolder = draftPageFolder;
		this.executionQueue = executionQueue;
	}

	public void execute() {
		next();
	}
	
	private byte[] getPhotosBytes(String key) throws IOException, RecordStoreException {
		byte[] data;
		data = PageDAO.loadPagePhoto(blog, draftPageFolder, key);
		EncodedImage img = EncodedImage.createEncodedImage(data, 0, -1);
		return img.getData();
	}
	
	private void next() {
		
		if (stopping == true)
			return;
		
		if (!executionQueue.isEmpty()  && isError == false) {
			BlogConn blogConn = (BlogConn) executionQueue.pop();
			
			if(blogConn instanceof NewMediaObjectConn) {
				String fileName = ((NewMediaObjectConn) blogConn).getFileName();
				byte[] photosBytes;
				try {
					photosBytes = getPhotosBytes(fileName);
					//check if resizing photo
					boolean isRes = false;
					if( page.getIsPhotoResizing() == null ){
						isRes = blog.isResizePhotos(); //get the option from the blog settings
					} else {
						isRes = page.getIsPhotoResizing().booleanValue();
					}
					
					if(isRes){
						Hashtable content = MultimediaUtils.resizePhotoAndOutputJpeg(photosBytes, fileName);
						fileName = (String) content.get("name");
						photosBytes = (byte[]) content.get("bits");
					}
					
					((NewMediaObjectConn) blogConn).setFileContent(photosBytes);
					((NewMediaObjectConn) blogConn).setFileName(fileName);
				} catch (Exception e) {
					final String respMessage=e.getMessage();
					errorMsg.append(respMessage+"\n");
					isError=true;
					next(); //called next to exit
					return;
				}
			} else {
				
				//adding multimedia info to post
				String body = page.getDescription();
				if(remoteFileInfo.size() > 0 ) {
					Enumeration keys = remoteFileInfo.keys();
					body+="<br /> <br />";
					for (; keys.hasMoreElements(); ) {
						String key = (String) keys.nextElement();
						String url = (String) remoteFileInfo.get(key);
						body+="<a href=\""+url+"\">"
						+"<img title=\""+key+"\" alt=\""+key+"\" src=\""+url+"\" /> </a> ";
					}
					page.setDescription(body);
					
				}
				
				if(blogConn instanceof NewPageConn) {
					((NewPageConn) blogConn).setPageDescription(body);
				} else {
					((EditPageConn) blogConn).setPageDescription(body);
				}
			}
			
			blogConn.addObserver(this);
			blogConn.startConnWork();
			
		} else {
			if (progressListener != null)
				progressListener.taskComplete(null);		
			//notifica al listener
		}
	}
	
	public void stop() {
		if(this.stopping == true) return; //already stopped
		
		this.stopping = true;
		if (progressListener != null)
			progressListener.taskComplete(null);
	}
	
	public boolean isStopped() {
		return stopping;
	}

	public void update(Observable observable, final Object object) {
		
		BlogConnResponse resp= (BlogConnResponse) object;
		
		//if(resp.isStopped()){ 
		if(stopping){ //stopping on task not on connection
			//stopping = true;			
		} else if(!resp.isError()) {
			//response from MM
			if (resp.getResponseObject() instanceof Hashtable ) {
				
				Hashtable content =(Hashtable)resp.getResponseObject();
				System.out.println("url del file remoto: "+content.get("url") );
				System.out.println("nome file remoto: "+content.get("file") );	
				final String url=(String)content.get("url");
				remoteFileInfo.put(content.get("file"), url);
			} else {
				//response from page
				try {
					//delete page from draft after sending
					PageDAO.removePage(blog, draftPageFolder);
				} catch (IOException e) {
					final String respMessage=e.getMessage();
					errorMsg.append(respMessage+"\n");
					isError=true;
				} catch (RecordStoreException e) {
					final String respMessage=e.getMessage();
					errorMsg.append(respMessage+"\n");
					isError=true;
				}
				
			}

		} else {
			final String respMessage=resp.getResponse();
			errorMsg.append(respMessage+"\n");
			isError=true;
		}
		next(); // call to next
	}
}
