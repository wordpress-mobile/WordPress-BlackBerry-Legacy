package com.wordpress.task;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.EncodedImage;

import com.wordpress.io.AppDAO;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.DraftDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.Page;
import com.wordpress.model.Post;
import com.wordpress.utils.CalendarUtils;
import com.wordpress.utils.ImageUtils;
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

	private Queue executionQueue = new Queue(); // queue of BlogConn
	private final Blog blog;
	private Page page;
	private Post post;
	private int draftFolder; 
	
	public SendToBlogTask(Blog blog, Page page, int draftPageFolder, BlogConn connection) {
		this.blog = blog;
		this.page = page;
		this.draftFolder = draftPageFolder;
		//adding multimedia connection
		Vector mediaObjects = page.getMediaObjects();
		for (int i =0; i < mediaObjects.size(); i++ ) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			NewMediaObjectConn mediaConnection = new NewMediaObjectConn (blog.getXmlRpcUrl(), 
					blog.getUsername(),blog.getPassword(), blog.getId(), tmp);				
			executionQueue.push(mediaConnection);
		}
		//adding the post conn
		executionQueue.push(connection);
	}

	public SendToBlogTask(Post post, int draftPostFolder, BlogConn connection) {
		this.post = post;
		this.blog = post.getBlog();
		this.draftFolder = draftPostFolder;
		//adding multimedia connection
		Vector mediaObjects = post.getMediaObjects();
		Log.trace("Found "+mediaObjects.size()+ " media objs attached to content");
		for (int i =0; i < mediaObjects.size(); i++ ) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			NewMediaObjectConn mediaConnection = new NewMediaObjectConn (post.getBlog().getXmlRpcUrl(), 
					post.getBlog().getUsername(), post.getBlog().getPassword(), post.getBlog().getId(), tmp);				
			executionQueue.push(mediaConnection);
		}
		//adding the post conn
		executionQueue.push(connection);
	}

	public void execute() {
		next();
	}
		
	private void next() {
		
		if (stopping == true)
			return;
		
		if (!executionQueue.isEmpty()  && isError == false) {
			BlogConn blogConn = (BlogConn) executionQueue.pop();
			
			if(blogConn instanceof NewMediaObjectConn) {
				try{
					sendMedia(blogConn);
				} catch (Exception e) {
					final String respMessage=e.getMessage();
					errorMsg.append(respMessage+"\n");
					isError=true;
					next(); //called next to exit
					return;
				}
			} else {
				
				String type=""; //type of action. passed to callback
				
				//sending object to blog
				if( post != null ) {
					
					//adding multimedia info to post. 
					String extendedBody = post.getExtendedBody();
					if(extendedBody != null && !extendedBody.trim().equals("")) {
						extendedBody = buildFullHtml(extendedBody);
						post.setExtendedBody(extendedBody);
					} else {
						String body = post.getBody();
						body = buildFullHtml(body);
						post.setBody(body);
					}
					if(blogConn instanceof NewPostConn) {
						((NewPostConn) blogConn).setPost(post);
						type = "NewPostConn";
					} else {
						((EditPostConn) blogConn).setPost(post);
						type = "EditPostConn";
					}
				} else {
					
					//adding multimedia info to page. 
					String moreText = page.getMtTextMore();
					if(moreText != null && !moreText.trim().equals("")) {
						moreText= buildFullHtml(moreText);
						page.setMtTextMore(moreText);
					} else {
						String body = page.getDescription();
						body = buildFullHtml(body);
						page.setDescription(body); 
					}
					
					if(blogConn instanceof NewPageConn) {
						((NewPageConn) blogConn).setPage(page);
						type = "NewPageConn";
					} else {
						((EditPageConn) blogConn).setPage(page);
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

	private void sendMedia(BlogConn blogConn) throws IOException, RecordStoreException {
		MediaEntry mediaEntry = ((NewMediaObjectConn) blogConn).getMediaObj();
		String filePath = mediaEntry.getFilePath();
		Log.trace("sending file: "+filePath);
		byte[] photosBytes;
		int h, w;
		String MIMEtype = "";
		
		photosBytes = JSR75FileSystem.readFile(filePath);
		
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
			Hashtable content = ImageUtils.resizePhoto(photosBytes, filePath, this);
			//resizing img is a long task. if user has stoped the operation..
			if (stopping == true)
				return;
			
			//save the res img in a temp file
			photosBytes = (byte[]) content.get("bits");
			String imageTempFilePath = AppDAO.getImageTempFilePath();
			if(JSR75FileSystem.isFileExist(imageTempFilePath)) {
				JSR75FileSystem.removeFile(imageTempFilePath);
			}
			JSR75FileSystem.createFile(imageTempFilePath);
			DataOutputStream dataOutputStream = JSR75FileSystem.getDataOutputStream(imageTempFilePath);
			dataOutputStream.write(photosBytes);
			
			h = Integer.parseInt( (String) content.get("height") );
			w = Integer.parseInt( (String) content.get("width") );
			mediaEntry.setFilePath(imageTempFilePath);
			MIMEtype = (String) content.get("type");
		} else {
			EncodedImage imgTmp = EncodedImage.createEncodedImage(photosBytes, 0, -1);
			h = imgTmp.getHeight();
			w = imgTmp.getWidth();
			MIMEtype = imgTmp.getMIMEType();
		}
		
		mediaEntry.setMIMEType(MIMEtype);
		mediaEntry.setWidth(w);
		mediaEntry.setHeight(h);
		
		//set the new file path 
		//((NewMediaObjectConn) blogConn).setMediaObj(mediaEntry);
		
		blogConn.addObserver(new SendMediaCallBack(mediaEntry));
		blogConn.setConnPriority(Thread.MIN_PRIORITY); 
		blogConn.startConnWork();						
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
						if(resp.getResponseObject() != null) { //safety check
							
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
								if(responseValue != null)
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
		
		private MediaEntry mediaObj;

		SendMediaCallBack(MediaEntry mediaObj){
			this.mediaObj = mediaObj;
		}
		
		public void update(Observable observable, final Object object) {
			BlogConnResponse resp= (BlogConnResponse) object;

			//if(resp.isStopped()){ 
			if(stopping){ //stopping on task not on connection

			} else if(!resp.isError()) {				
				Hashtable content =(Hashtable)resp.getResponseObject();
				String file = (String)content.get("file");
				String url = (String)content.get("url");
				mediaObj.setFileURL(url);
				mediaObj.setFileName(file);
			} else {
				final String respMessage=resp.getResponse();
				errorMsg.append(respMessage+"\n");
				isError=true;
			}
			
			next(); // call to next
		}
	}
	

	/**
	 * Build the Full post/page html. 
	 * @param 
	 * @return
	 */
	private synchronized String buildFullHtml(String html) {
		StringBuffer topMediaFragment = new StringBuffer();
		StringBuffer bottomMediaFragment = new StringBuffer();
		Vector mediaObjects;
		if(post != null)
			mediaObjects = post.getMediaObjects();
		else 
			mediaObjects = page.getMediaObjects();
		
		for (int i = 0; i < mediaObjects.size(); i++) {

			MediaEntry remoteFileInfo = (MediaEntry)mediaObjects.elementAt(i);
			StringBuffer tmpBuff = null;
			if(remoteFileInfo.isVerticalAlignmentOnTop())
				tmpBuff  = 	topMediaFragment;
			else
				tmpBuff  = 	bottomMediaFragment;
			
			String title = remoteFileInfo.getTitle() != null ? remoteFileInfo.getTitle() : remoteFileInfo.getFileName();
			String caption = remoteFileInfo.getCaption() != null ? remoteFileInfo.getCaption() : "";
			
			if(!caption.equals("")) {
				//<div id="attachment_30" class="wp-caption alignnone" style="width: 830px">
				tmpBuff.append("<div class=\"wp-caption alignnone\" style=\"width: "+remoteFileInfo.getWidth()+10+"px\"");
			} else {
				tmpBuff.append("<p>");
			}
			
			tmpBuff.append("<a href=\""+remoteFileInfo.getFileURL()+"\">" +
							"<img class=\"alignnone size-full\"" +
							" src=\""+remoteFileInfo.getFileURL()+"\" alt=\""+caption+"\"" +
							" title=\""+title+"\"" +
							" width=\""+remoteFileInfo.getWidth()+"\" height=\""+remoteFileInfo.getHeight()+"\" />" +
							"</a>");
		
			if(!caption.equals("")) {
				//<p class="wp-caption-text">qusta e la didascalia</p>
				//</div>
				tmpBuff.append("<p class=\"wp-caption-text\">");
				tmpBuff.append(caption);
				tmpBuff.append("</div>");
			} else {
				tmpBuff.append("</p>");
			}
		}
		return topMediaFragment.toString() + html + bottomMediaFragment.toString();
	}
	
	/**
	 * Build the html fragment for the photos
	 * @param remoteFileInfo  contain files info on the WP server (used after sending MM files to the blog)
	 * @return
	 
	private synchronized String buildHtmlPhotoFragment() {
		StringBuffer photoFragment = new StringBuffer();
		Vector mediaObjects;
		if(post != null)
			mediaObjects = post.getMediaObjects();
		else 
			mediaObjects = page.getMediaObjects();
		
		for (int i = 0; i < mediaObjects.size(); i++) {

			MediaEntry remoteFileInfo = (MediaEntry)mediaObjects.elementAt(i);

			photoFragment.append("<p><a href=\""+remoteFileInfo.getFileURL()+"\">" +
							"<img class=\"alignnone size-full wp-image-364\"" +
							" src=\""+remoteFileInfo.getFileURL()+"\" alt=\"\" " +
							"width=\""+remoteFileInfo.getWidth()+"\" height=\""+remoteFileInfo.getHeight()+"\" />" +
									"</a></p>");
		}
		
		return photoFragment.toString();
	} */
}
