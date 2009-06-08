package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.DraftDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.model.Post;
import com.wordpress.task.SendToBlogTask;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PhotosView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.PreviewView;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.mm.MultimediaPopupScreen;
import com.wordpress.view.mm.PhotoPreview;
import com.wordpress.view.mm.PhotoSnapShotView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.GetTemplateConn;

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
	public abstract void setSettingsValues(long authoredOn, String password, boolean isPhotoResizing);	
	public abstract void setPhotosNumber(int count);
	
	
	
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
	
	
	
	protected String[] getPhotoList() {
		String[] draftPostPhotoList = new String [0];
		try {
			if( post != null )
				draftPostPhotoList = DraftDAO.getPostPhotoList(post.getBlog(), draftFolder);
			else
				draftPostPhotoList = PageDAO.getPagePhotoList(blog, draftFolder);
		} catch (Exception e) {
			displayError(e, "Cannot load photos from disk!");
		}
		return draftPostPhotoList;
	}

	
	public void storePhoto(byte[] data, String fileName) {
		try {
			EncodedImage img;
			img = EncodedImage.createEncodedImage(data, 0, -1);
			if(post != null)
				DraftDAO.storePhoto(post.getBlog(), draftFolder, data, fileName);
			else
				PageDAO.storePhoto(blog, draftFolder, data, fileName);
			photoView.addPhoto(fileName, img);
		} catch (Exception e) {
			displayError(e, "Cannot save photo to disk!");
		}
	}
	
		
	/*
	 * delete selected photo
	 */
	public boolean deletePhoto(String key){
		System.out.println("deleting photo: "+key);
		
		try {
			if(post != null)
				DraftDAO.removePostPhoto(post.getBlog(), draftFolder, key);
			else
				PageDAO.removePagePhoto(blog, draftFolder, key);
				
			photoView.deletePhotoBitmapField(key); //delete the thumbnail
		} catch (Exception e) {
			displayError(e, "Cannot remove photo from disk!");
		}		
		return true;
	}
	
	
	/*
	 * show selected photo
	 */
	public void showEnlargedPhoto(String key){
		System.out.println("showed photos: "+key);
		byte[] data;
		try {
			if(post != null)
				data = DraftDAO.loadPostPhoto(blog, draftFolder, key);
			else
				data = PageDAO.loadPagePhoto(blog, draftFolder, key);
			EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
			UiApplication.getUiApplication().pushScreen(new PhotoPreview(this, key ,img)); //modal screen...
		} catch (Exception e) {
			displayError(e, "Cannot load photos from disk!");
			return;
		}
	}
	
	
	//* show up multimedia type selection */
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
                // Dialog.alert("Screen was dismissed. No file was selected.");
             } else {
            	 String[] fileNameSplitted = StringUtils.split(theFile, "/");
            	 String ext= fileNameSplitted[fileNameSplitted.length-1];
				try {
					byte[] readFile = JSR75FileSystem.readFile(theFile);
					storePhoto(readFile,ext);	
				} catch (IOException e) {
					displayError(e, "Cannot load photo from disk!");
				}
             }					
			break;
			
		case PHOTO:
			PhotoSnapShotView snapView = new PhotoSnapShotView(this);
			UiApplication.getUiApplication().pushScreen(snapView); //modal screen...
			break;
			
		default:
			break;
		}		
	}
	
	
	public void startRemotePreview(String objectLink, String title, String content, String tags){
		String connMessage = null;
		connMessage = _resources.getString(WordPressResource.CONN_LOADING_PREVIEW_TEMPLATE);
		
		final GetTemplateConn connection = new GetTemplateConn(objectLink);
		
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
	 * Build the html fragment for the body
	 * @param body  original body text field content
	 * @return
	 */
	public static synchronized String buildBodyHtmlFragment(String originalContent) {
		String[] split = StringUtils.split(originalContent, Characters.ENTER);
		StringBuffer newContentBuff = new StringBuffer();
		for (int i = 0; i < split.length; i++) {
			newContentBuff.append("<p>");
			newContentBuff.append(split[i]);
			newContentBuff.append("</p>");
		}
		return newContentBuff.toString();
	}
		
}
