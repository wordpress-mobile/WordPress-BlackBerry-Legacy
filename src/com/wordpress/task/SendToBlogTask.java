package com.wordpress.task;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.EncodedImage;

import com.wordpress.io.BlogDAO;
import com.wordpress.io.DraftDAO;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.model.Post;
import com.wordpress.utils.CalendarUtils;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.NewMediaObjectConn;
import com.wordpress.xmlrpc.page.EditPageConn;
import com.wordpress.xmlrpc.page.NewPageConn;
import com.wordpress.xmlrpc.post.EditPostConn;
import com.wordpress.xmlrpc.post.NewPostConn;

public class SendToBlogTask extends TaskImpl {

	private Queue executionQueue = null; // queue of BlogConn
	private boolean stopping = false;

	private final Blog blog;
	private Page page;
	private Post post;
	private int draftFolder; 
	private Vector remoteFilesInfo = new Vector(); //contain files info on the WP server (used after sending MM files to the blog)
	
	public SendToBlogTask(Blog blog, Page page, int draftPageFolder, Queue executionQueue) {
		this.blog = blog;
		this.page = page;
		this.draftFolder = draftPageFolder;
		this.executionQueue = executionQueue;
	}

	public SendToBlogTask(Post post, int draftPostFolder, Queue executionQueue) {
		this.executionQueue = executionQueue;
		this.post = post;
		this.blog = post.getBlog();
		this.draftFolder = draftPostFolder;
	}

	public void execute() {
		next();
	}
	
	
	private byte[] getPhotosBytes(String key) throws IOException, RecordStoreException {
		byte[] data;
		if(post != null)
			data = DraftDAO.loadPostPhoto(blog, draftFolder, key);
		else
			data = PageDAO.loadPagePhoto(blog, draftFolder, key);
		return data;
	}
	
	
	private void next() {
		
		if (stopping == true)
			return;
		
		if (!executionQueue.isEmpty()  && isError == false) {
			BlogConn blogConn = (BlogConn) executionQueue.pop();
			
			if(blogConn instanceof NewMediaObjectConn) {
				String fileName = ((NewMediaObjectConn) blogConn).getFileName();
				byte[] photosBytes;
				int h, w;
								
				try {
					photosBytes = getPhotosBytes(fileName);
					
					boolean isRes = false;
					Boolean currentObjectPhotoResSetting = null;
					if( post != null )
						currentObjectPhotoResSetting = post.getIsPhotoResizing();
					else
						currentObjectPhotoResSetting = page.getIsPhotoResizing();
						
					if( currentObjectPhotoResSetting == null ){
						isRes = blog.isResizePhotos(); //get the option from the blog settings
					} else {
						isRes = currentObjectPhotoResSetting.booleanValue();
					}
					
					if(isRes){
						Hashtable content = MultimediaUtils.resizePhotoAndOutputJpeg(photosBytes, fileName);
						fileName = (String) content.get("name");
						photosBytes = (byte[]) content.get("bits");
						h = Integer.parseInt( (String) content.get("height") );
						w = Integer.parseInt( (String) content.get("width") );
					} else {
						EncodedImage imgTmp = EncodedImage.createEncodedImage(photosBytes, 0, -1);
						h = imgTmp.getHeight();
						w = imgTmp.getWidth();
					}
					
					((NewMediaObjectConn) blogConn).setFileContent(photosBytes);
					((NewMediaObjectConn) blogConn).setFileName(fileName);
					
					blogConn.addObserver(new SendMediaCallBack(new FileInfo(fileName, h, w)));
					blogConn.startConnWork();					

				} catch (Exception e) {
					final String respMessage=e.getMessage();
					errorMsg.append(respMessage+"\n");
					isError=true;
					next(); //called next to exit
					return;
				}
			} else {
				
				String htmlPhotosFragment = buildHtmlPhotoFragment(remoteFilesInfo);
				String type=""; //type of action. passed to callback
			
				//sending object to blog
				if( post != null ) {
				
					//adding multimedia info to post
					String body = post.getBody();
					body+= htmlPhotosFragment;
					post.setBody(body);
					
					if(blogConn instanceof NewPostConn) {
						((NewPostConn) blogConn).setPost(post);
						type = "NewPostConn";
					} else {
						((EditPostConn) blogConn).setPost(post);
						type = "EditPostConn";
					}
				} else {
					
					//adding multimedia info to post
					String body = page.getDescription() ;
					body+= htmlPhotosFragment;
					page.setDescription(body);
					
					if(blogConn instanceof NewPageConn) {
						((NewPageConn) blogConn).setPageDescription(body);
						type = "NewPageConn";
					} else {
						((EditPageConn) blogConn).setPageDescription(body);
						type = "EditPageConn";
					}
					
				}
				
				blogConn.addObserver(new SendCallBack(type));
				blogConn.startConnWork();
			}
			
		} else {
			//end
			if (progressListener != null)
				progressListener.taskComplete(null);		
		}
	}
	
	
	//callback for send post to the blog
	private class SendCallBack implements Observer{
		String type = "";
	
		public SendCallBack(String type) {
			super();
			this.type = type;
		}


		public void update(Observable observable, final Object object) {
			BlogConnResponse resp = (BlogConnResponse) object;

			// if(resp.isStopped()){
			if (stopping) { // stopping on task not on connection
				// stopping = true;
			} else if (!resp.isError()) {

				// response from post
				try {
					if (post != null) {
						// delete post from draft after sending
						DraftDAO.removePost(blog, draftFolder);
						if(type.equalsIgnoreCase("NewPostConn")) {
							//add post on disk
							String postID=String.valueOf(resp.getResponseObject());
							Log.info("new post was added to blog with id: "+postID);
							post.setId(postID); //update the post ID
							if(post.getAuthoredOn() == null) {
								long gmtTime = CalendarUtils.adjustTimeFromDefaultTimezone(System.currentTimeMillis());
								post.setAuthoredOn(gmtTime);
							}
							Vector recentPostTitles = blog.getRecentPostTitles();
							recentPostTitles.insertElementAt(DraftDAO.post2Hashtable(post), 0);
							blog.setRecentPostTitles(recentPostTitles);
						} else {
							//update previous post on disk
							String responseValue=String.valueOf(resp.getResponseObject());
							Vector recentPostTitles = blog.getRecentPostTitles();
							//if response from update was true
							if(responseValue != null && responseValue.equalsIgnoreCase("true"))
					        for (int i = 0; i < recentPostTitles.size(); i++) {
					        	Hashtable postData = (Hashtable) recentPostTitles.elementAt(i);
					        	String tmpPostID =(String) postData.get("postid");
					        	if(tmpPostID.equalsIgnoreCase(post.getId())){
					        		recentPostTitles.setElementAt(DraftDAO.post2Hashtable(post),i);
					        		break;
					        	}
							}
							blog.setRecentPostTitles(recentPostTitles);
						}
						BlogDAO.updateBlog(blog);							
					} else {
						//remove draft page on disk
						PageDAO.removePage(blog, draftFolder);
					}
				} catch (IOException e) {
					final String respMessage = e.getMessage();
					errorMsg.append(respMessage + "\n");
				} catch (RecordStoreException e) {
					final String respMessage = e.getMessage();
					errorMsg.append(respMessage + "\n");
				} catch (Exception e) {
					final String respMessage = e.getMessage();
					errorMsg.append(respMessage + "\n");
				}	
			} else {
				final String respMessage = resp.getResponse();
				errorMsg.append(respMessage + "\n");
				isError = true;
			}

			next(); // call to next
		}
	}
	
	
	
	//callback for send post to the blog
	private class SendMediaCallBack implements Observer{
		
		private FileInfo info;

		SendMediaCallBack(FileInfo info){
			this.info = info;
			
		}
		
		public void update(Observable observable, final Object object) {
			BlogConnResponse resp= (BlogConnResponse) object;

			//if(resp.isStopped()){ 
			if(stopping){ //stopping on task not on connection

			} else if(!resp.isError()) {				
				Hashtable content =(Hashtable)resp.getResponseObject();
				info.setName((String)content.get("file"));
				info.setUrl((String)content.get("url"));

				remoteFilesInfo.addElement(info);
			} else {
				final String respMessage=resp.getResponse();
				errorMsg.append(respMessage+"\n");
				isError=true;
			}
			
			next(); // call to next
		}
	}
	
	
	private class FileInfo {
		int height,width;

		String name = "";
		String url = "";

		public FileInfo(String name, int h, int w) {
			super();
			this.height = h;
			this.width = w;
			this.name = name;
		}
		
		public int getHeight() {
			return height;
		}
		
		public int getWidth() {
			return width;
		}
		
		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

	}
	
	

	
	/**
	 * Build the html fragment for the photos
	 * @param remoteFileInfo  contain files info on the WP server (used after sending MM files to the blog)
	 * @return
	 */
	private static synchronized String buildHtmlPhotoFragment(Vector remoteFilesInfo) {
		
		StringBuffer photoFragment = new StringBuffer();

		for (int i = 0; i < remoteFilesInfo.size(); i++) {

			FileInfo remoteFileInfo = (FileInfo)remoteFilesInfo.elementAt(i);

			photoFragment.append("<p><a href=\""+remoteFileInfo.getUrl()+"\">" +
							"<img class=\"alignnone size-full wp-image-364\"" +
							" src=\""+remoteFileInfo.getUrl()+"\" alt=\"\" " +
							"width=\""+remoteFileInfo.getWidth()+"\" height=\""+remoteFileInfo.getHeight()+"\" />" +
									"</a></p>");
		}
		
		return photoFragment.toString();
		
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

}
