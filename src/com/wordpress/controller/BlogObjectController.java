package com.wordpress.controller;

import java.io.IOException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.FileUtils;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Blog;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PreviewView;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.mm.MultimediaPopupScreen;
import com.wordpress.view.mm.PhotoSnapShotView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.GetTemplateConn;

public abstract class BlogObjectController extends BaseController {
	
	protected Blog blog;
	protected static final String LOCAL_DRAFT_KEY = "localdraft";
	protected static final String LOCAL_DRAFT_LABEL = _resources.getString(WordPress.LABEL_LOCAL_DRAFT);
	protected ConnectionInProgressView connectionProgressView=null;
	public abstract void setSettingsValues(long authoredOn, String password, boolean isPhotoResizing);
	
	public static final int NONE=-1;
	public static final int PHOTO=1;
	public static final int BROWSER=4;
	
	public abstract void showEnlargedPhoto(String key);
	public abstract boolean deletePhoto(String photoName); //delete 
	public abstract void setPhotosNumber(int count);
	public abstract void storePhoto(byte[] data, String fileName);
	
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
	
	
	public void startRemotePreview(String objectLink, String title, String content, String tags, String cats, String photoNumber){
		String connMessage = null;
		connMessage = _resources.getString(WordPressResource.CONN_LOADING_PREVIEW_TEMPLATE);
		
		final GetTemplateConn connection = new GetTemplateConn(objectLink);
		
        connection.addObserver(new loadTemplateCallBack(title, content, tags, cats, photoNumber));  
        connectionProgressView= new ConnectionInProgressView(connMessage);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}		
	}
	
	public void startLocalPreview(String title, String content, String tags, String cats, String photoNumber){
		String html = FileUtils.readTxtFile("defaultPostTemplate.html");
		
		html = StringUtils.replaceAll(html, "!$title$!", title);
		html = StringUtils.replaceAll(html, "!$text$!", content+"<br/>"+photoNumber);
		html = StringUtils.replaceAll(html, "!$mt_keywords$!", tags);
		html = StringUtils.replaceAll(html, "!$categories$!", cats);
		
		UiApplication.getUiApplication().pushScreen(new PreviewView(html));	
	}
		
		
	//callback for post loading
	private class loadTemplateCallBack implements Observer {
		
		private final String title;
		private final String content;
		private final String tags;
		private final String cats;
		private final String photoNumber;

		public loadTemplateCallBack(String title, String content, String tags, String cats, String photoNumber) {
			this.title = title;
			this.content = content;
			this.tags = tags;
			this.cats = cats;
			this.photoNumber = photoNumber;
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
							startLocalPreview(title,content,tags, cats, photoNumber);
							return;
						}
						
						UiApplication.getUiApplication().pushScreen(new PreviewView(html));	
						
					} else {
						startLocalPreview(title,content,tags, cats, photoNumber);
					}
					
					
				}
			});
		}
	} 
	
}
