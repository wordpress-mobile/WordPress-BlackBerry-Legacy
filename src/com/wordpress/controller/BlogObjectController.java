package com.wordpress.controller;

import java.io.IOException;
import java.util.Vector;

import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Blog;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.Page;
import com.wordpress.model.Post;
import com.wordpress.task.SendToBlogTask;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.CustomFieldsView;
import com.wordpress.view.MediaEntryPropView;
import com.wordpress.view.PhotosView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.PreviewView;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.mm.MediaObjFileJournalListener;
import com.wordpress.view.mm.MediaViewMediator;
import com.wordpress.view.mm.MultimediaPopupScreen;
import com.wordpress.view.mm.PhotoPreview;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.PreviewHTTPConn;

/**
 * This is the base class for Post and Page Obj
 * @author dercoli
 *
 */
public abstract class BlogObjectController extends BaseController {
	
	protected Blog blog;
	protected Page page=null; //page object
	protected Post post=null; //post object (mutually excluded with page obj) 
	protected boolean isModified = false; //the state of post/page. track changes on post/page..
	protected int draftFolder=-1; //identify draft post folder
	protected boolean isDraft= false; // identify if post/page is loaded from draft folder

	public static final int NONE=-1;
	public static final int PHOTO=1;
	public static final int BROWSER=4;
	
	//related view
	protected SendToBlogTask sendTask;
	protected PostSettingsView settingsView = null;
	protected PhotosView photoView = null;
	protected ConnectionInProgressView connectionProgressView=null;

		
	protected static final String LOCAL_DRAFT_KEY = "localdraft";
	protected static final String LOCAL_DRAFT_LABEL = _resources.getString(WordPress.LABEL_LOCAL_DRAFT);
	public abstract void setPhotosNumber(int count);
	public abstract void setAuthDate(long authoredOn);
	public abstract void setPassword(String password);
	public abstract void setPhotoResizing(boolean isPhotoRes);

	//journal listener
	MediaObjFileJournalListener photoFSListener = null;
	
	public void showSettingsView(){
		boolean isPhotoResing = blog.isResizePhotos(); //first set the value as the predefined blog value
		if(post != null) {
			if (post.getIsPhotoResizing() != null ) {
				isPhotoResing = post.getIsPhotoResizing().booleanValue();			
			}
			settingsView= new PostSettingsView(this, post.getAuthoredOn(), post.getPassword(), isPhotoResing);		
		} else {
			if (page.getIsPhotoResizing() != null ) {
				isPhotoResing = page.getIsPhotoResizing().booleanValue();			
			}
			settingsView= new PostSettingsView(this, page.getDateCreatedGMT(), page.getWpPassword(), isPhotoResing);		
		}
		UiApplication.getUiApplication().pushScreen(settingsView);
	}
	
	
	public void showCustomFieldsView(String title){
		CustomFieldsView view;
		
		if( post != null )
			view = new CustomFieldsView(this, post.getCustomFields(), title);
		else
			view = new CustomFieldsView(this, page.getCustomFields(), title);
		
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	
	public void setObjectAsChanged(boolean value) {
		isModified = value;
	}
	
	public boolean isObjectChanged() {
		return isModified;
	}
	
	
	protected String[] getPhotoList() {
		String[] draftPostPhotoList = new String [0];
		
		Vector mediaObjects;
		if(post != null) {
			mediaObjects = post.getMediaObjects();				
		} else {
			mediaObjects = page.getMediaObjects();
		}
		draftPostPhotoList = new String[mediaObjects.size()];
		
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			draftPostPhotoList[i] = tmp.getFilePath();
		}
		
		return draftPostPhotoList;
	}


	protected synchronized void checkMediaObjectLinks() {
		try {
			Log.trace(">>> checkMediaLink ");
			
			Vector mediaObjects;
			if(post != null) {
				mediaObjects = post.getMediaObjects();				
			} else {
				mediaObjects = page.getMediaObjects();
			}
			
			Vector notFoundMediaObjects = new Vector();
			//checking the existence of photo already linked with the page/post obj
			for (int i = 0; i < mediaObjects.size(); i++) {
				MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
				if(!JSR75FileSystem.isFileExist(tmp.getFilePath())) {
					notFoundMediaObjects.addElement(tmp);
					Log.trace("media file not found on disk : "+tmp.getFilePath());
				}
			}
			for (int i = 0; i < notFoundMediaObjects.size(); i++) {
				MediaEntry tmp = (MediaEntry) notFoundMediaObjects.elementAt(i);
				mediaObjects.removeElement(tmp);
				Log.trace("media file removed from post : "+tmp.getFilePath());
			}
						
			
			if(post != null) {
				post.setMediaObjects(mediaObjects);
			} else {
				page.setMediaObjects(mediaObjects);
			}
			Log.trace("<<< checkMediaLink ");
		} catch (Exception e) {
			Log.error(e, "checkMediaLink error");
		}
	}
	
	
	public synchronized void addLinkToMediaObject(String completePath) {
		try {
			Log.trace("linking media obj photo: "+completePath);
			isModified = true; //set the post/page as modified
			
       	 	MediaEntry mediaObj = new MediaEntry(completePath);
       	 	Vector mediaObjects;
			if(post != null) {
				mediaObjects = post.getMediaObjects();				
			} else {
				mediaObjects = page.getMediaObjects();
			}
			//checking the existence of photo already linked with the page/post obj
			for (int i = 0; i < mediaObjects.size(); i++) {
				MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
				if(tmp.getFilePath().equalsIgnoreCase(mediaObj.getFilePath()))
					return;	
			}
			
			mediaObjects.addElement(mediaObj);
			
			if(post != null) {
				post.setMediaObjects(mediaObjects);
			} else {
				page.setMediaObjects(mediaObjects);
			}
						
			byte[] readFile = JSR75FileSystem.readFile(completePath);
			EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
			photoView.addPhoto(mediaObj, img);
			removePhotoJournalListener(); //remove the fs listener.
			
		} catch (Exception e) {
			deleteLinkToMediaObject(completePath);
			displayError(e, "Cannot save photo to disk!");
		}
	}
	
	
		
	/*
	 * delete selected photo
	 */
	public synchronized boolean deleteLinkToMediaObject(String key) {
		Log.trace("deleting link to photo: "+key);
		isModified = true; //set the post/page as modified
		Vector mediaObjects;
		if(post != null) {
			mediaObjects = post.getMediaObjects();				
		} else {
			mediaObjects = page.getMediaObjects();
		}
		//checking the existence of photo linked with the page/post obj
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			if(tmp.getFilePath().equalsIgnoreCase(key)) {
				mediaObjects.removeElementAt(i);
				break;
			}
		}
		
		if(post != null) {
			post.setMediaObjects(mediaObjects);
		} else {
			page.setMediaObjects(mediaObjects);
		}

		photoView.deletePhotoBitmapField(key); //delete the thumb
		return true;
	}
		
	public void showPhotosView(){
		
		photoView= new PhotosView(this);

		Vector mediaObjects;
		if(post != null) {
			mediaObjects = post.getMediaObjects();				
		} else {
			mediaObjects = page.getMediaObjects();
		}
						
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			byte[] readFile;
			try {
				readFile = JSR75FileSystem.readFile(tmp.getFilePath());
				EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
				photoView.addPhoto(tmp, img);
			} catch (IOException e) {
				displayError(e, "Cannot read media: "+tmp.getFilePath());
			}
		}			
		UiApplication.getUiApplication().pushScreen(photoView);
	}
	
	/*
	 * show selected photo
	 */
	public void showEnlargedPhoto(String key){
		Log.trace("showed photos: "+key);
		byte[] data;
		try {
			data = JSR75FileSystem.readFile(key);			
			EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
			UiApplication.getUiApplication().pushScreen(new PhotoPreview(this, key ,img)); //modal screen...
		} catch (Exception e) {
			displayError(e, "Cannot load photos from disk!");
			return;
		}
	}
	
	/*
	 * show selected photo properties
	 */
	public void showMediaObjectProperties(MediaViewMediator mediaViewMediator){
		Log.trace("showed properties for media: "+mediaViewMediator.getMediaEntry().getFilePath());
		UiApplication.getUiApplication().pushScreen(new MediaEntryPropView(this, mediaViewMediator,"pippo"));
	}
	
	/** show up multimedia type selection */
	public void showAddPhotoPopUp() {
		int response= BROWSER;
		
    	MultimediaPopupScreen multimediaPopupScreen = new MultimediaPopupScreen();
    	UiApplication.getUiApplication().pushModalScreen(multimediaPopupScreen); //modal screen...
		response = multimediaPopupScreen.getResponse();
			
		switch (response) {
		case BROWSER:
           	 String imageExtensions[] = { "jpg", "jpeg","bmp", "png", "gif"};
             FileSelectorPopupScreen fps = new FileSelectorPopupScreen(null, imageExtensions);
             fps.pickFile();
             String theFile = fps.getFile();
             if (theFile == null){
            	 
             } else {
    		     if(!theFile.startsWith("file:///")) {
    		    	 theFile = "file:///"+ theFile;
    		       } 
				addLinkToMediaObject(theFile);	
             }					
			break;
			
		case PHOTO:
			try {
				 	addPhotoJournalListener();
					Invoke.invokeApplication(Invoke.APP_TYPE_CAMERA, new CameraArguments());
				} catch (Exception e) {
					removePhotoJournalListener();
					displayError(e, "Cannot invoke camera!");
				}
			break;
			
		default:
			break;
		}		
	}
	
	private void addPhotoJournalListener() {
		//create a new listener only if it is null
		if(photoFSListener == null ) {
			photoFSListener = new MediaObjFileJournalListener(this);
		}
		UiApplication.getUiApplication().addFileSystemJournalListener(photoFSListener);
	}
	
	//called when photoview is closed
	public void removePhotoJournalListener() {
		if(photoFSListener != null) {
			UiApplication.getUiApplication().removeFileSystemJournalListener(photoFSListener);
			photoFSListener = null;
		} 
	}
	
	public void startRemotePreview(String objectLink, String title, String content, String tags){
		String connMessage = null;
		connMessage = _resources.getString(WordPressResource.CONN_LOADING_PREVIEW_TEMPLATE);
		
		final PreviewHTTPConn connection = new PreviewHTTPConn(objectLink);
		
        connection.addObserver(new loadTemplateCallBack(title, content, tags));  
        connectionProgressView= new ConnectionInProgressView(connMessage);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}		
	}
	
	public abstract void startLocalPreview(String title, String content, String tags);
	
		
	//callback for post loading
	private class loadTemplateCallBack implements Observer {
		
		private final String title;
		private final String content;
		private final String tags;

		public loadTemplateCallBack(String title, String content, String tags) {
			this.title = title;
			this.content = content;
			this.tags = tags;
		}

		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					
					String html = null;
					
					if(resp.isStopped()){
						return;
					}
					
					if(!resp.isError()) {
						
						try {
							html = (String)resp.getResponseObject();
						} catch (Exception e) {
							startLocalPreview(title,content,tags);
							return;
						}						
						UiApplication.getUiApplication().pushScreen(new PreviewView(html));							
					} else {
						startLocalPreview(title,content,tags);
					}
					
				}
			});
		}
	} 
	
	
	/**
	 * Build the html fragment for the body preview
	 * @param body  original body text field content
	 * @return
	 */
	public static synchronized String buildBodyHtmlFragment(String originalContent) {
		String[] split = StringUtils.split(originalContent, "\n\n");
		StringBuffer newContentBuff = new StringBuffer();
		for (int i = 0; i < split.length; i++) {
			newContentBuff.append("<p>");
			newContentBuff.append(split[i]);
			newContentBuff.append("</p>");
		}
		
		split = StringUtils.split(newContentBuff.toString(), "\n");
		newContentBuff = new StringBuffer();
		for (int i = 0; i < split.length; i++) {
			newContentBuff.append("<br/>");
			newContentBuff.append(split[i]);
		}
		return newContentBuff.toString();
		//return originalContent;
	}
	
	/**
	 * Build the field body content from the html fragment of the body
	 * @param body  original body text field content
	 * @return
	 */
	public static synchronized String buildBodyFieldContentFromHtml(String originalContent) {
		//String[] split = StringUtils.split(originalContent, Characters.ENTER);
		String replaceAll = StringUtils.replaceAll(originalContent, "<p>", "");
		replaceAll = StringUtils.replaceAll(replaceAll, "</p>", Characters.ENTER+"");
		return replaceAll;		
	}
}
