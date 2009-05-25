package com.wordpress.xmlrpc.post;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.io.DraftDAO;
import com.wordpress.model.Post;
import com.wordpress.model.Preferences;
import com.wordpress.utils.Queue;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.NewMediaObjectConn;

public class SendPostDataTask {

	private final Queue executionQueue = new Queue(); // queue of BlogConn
	private WorkerThread worker = null;

	private final int draftPostFolder;
	private final Post post;
	Preferences prefs = Preferences.getIstance();
	private Hashtable remoteFileInfo = new Hashtable(); //contain files info on the WP server (used after sending MM files to the blog)
	
	private boolean stopping = false;
	private boolean started = false;
	private boolean taskCompleted = false;
	private boolean isError = false; // true if there was errors in the connections
	private StringBuffer errorMessage=new StringBuffer();
	
	private Dialog connectionProgressView;

	public SendPostDataTask(Post post, int draftPostFolder) {
		this.post = post;
		this.draftPostFolder = draftPostFolder;
		}

	 public void setDialog(Dialog dlg){
		 this.connectionProgressView = dlg;
	 }

	 public boolean isError() {
		 return isError;
	 }
	 	 
	 public String getErrorMessage() {
		 return errorMessage.toString();
	 }
	
	public void startWorker() {
		started = true;
		worker = new WorkerThread();
		worker.run();
	}
	
	public boolean isTaskCompleted() {
		return taskCompleted;
	}	
  
	public void quit() {
	    stopping = true;
	}

	public void addConn(BlogConn blogConn) {
		executionQueue.push(blogConn);
	}
	
	private byte[] getPhotosBytes(String key) throws IOException, RecordStoreException {
		byte[] data;
		data = DraftDAO.loadPostPhoto(post, draftPostFolder, key);
		EncodedImage img = EncodedImage.createEncodedImage(data, 0, -1);
		return img.getData();
	}
	
	private class WorkerThread implements Runnable, Observer {

		public void run() {
			next();
		}

		private void next() {
			
			if (!executionQueue.isEmpty() && stopping == false && isError == false) {
				BlogConn blogConn = (BlogConn) executionQueue.pop();
				if(blogConn instanceof NewMediaObjectConn) {
					String fileName = ((NewMediaObjectConn) blogConn).getFileName();
					byte[] photosBytes;
					try {
						photosBytes = getPhotosBytes(fileName);
						((NewMediaObjectConn) blogConn).setFileContent(photosBytes);					
					} catch (Exception e) {
						final String respMessage=e.getMessage();
						errorMessage.append(respMessage+"\n");
						isError=true;
						next(); //called next to exit
						return;
					}
				} else {
					
					//adding multimedia info to post
					String body = post.getBody();
					if(remoteFileInfo.size() > 0 ) {
						Enumeration keys = remoteFileInfo.keys();
						body+="<br /> <br />";
						for (; keys.hasMoreElements(); ) {
							String key = (String) keys.nextElement();
							String url = (String) remoteFileInfo.get(key);
							body+="<a href=\""+url+"\">"
							+"<img title=\""+key+"\" alt=\""+key+"\" src=\""+url+"\" /> </a> ";
						}
						post.setBody(body);
						
					}
					
					if(blogConn instanceof NewPostConn) {
						((NewPostConn) blogConn).setPost(post);
					} else {
						((EditPostConn) blogConn).setPost(post);
					}
					
				}
				
				blogConn.addObserver(this);
				blogConn.startConnWork();
			} else {
				taskCompleted = true; 
				
				if(connectionProgressView != null)
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						connectionProgressView.close();
					}
				});
			
			}
		}

		public void update(Observable observable, final Object object) {

			BlogConnResponse resp= (BlogConnResponse) object;
			if(!resp.isError()) {
				if(resp.isStopped()){
					stopping = true;
				} else {
					//response from MM
					if (resp.getResponseObject() instanceof Hashtable ) {
						
						Hashtable content =(Hashtable)resp.getResponseObject();
						System.out.println("url del file remoto: "+content.get("url") );
						System.out.println("nome file remoto: "+content.get("file") );	
						final String url=(String)content.get("url");
						remoteFileInfo.put(content.get("file"), url);
					} else {
					//response from post
						
						//delete post from draft after sending
						try {
							DraftDAO.removePost(post.getBlog(),	draftPostFolder);
						} catch (IOException e) {
							final String respMessage=e.getMessage();
							errorMessage.append(respMessage+"\n");
						} catch (RecordStoreException e) {
							final String respMessage=e.getMessage();
							errorMessage.append(respMessage+"\n");
						}
					}
				}
			} else {
				final String respMessage=resp.getResponse();
				errorMessage.append(respMessage+"\n");
				isError=true;
			}
		
			next(); // call to next
		
		}
	}
}