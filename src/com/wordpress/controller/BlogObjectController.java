package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.FileUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PostPreviewView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.GetTemplateConn;

public abstract class BlogObjectController extends BaseController {
	
	protected static final String LOCAL_DRAFT_KEY = "localdraft";
	protected static final String LOCAL_DRAFT_LABEL = _resources.getString(WordPress.LABEL_LOCAL_DRAFT);
	protected ConnectionInProgressView connectionProgressView=null;
	
	public abstract void setSettingsValues(long authoredOn, String password);
		
	public abstract void showAddPhotoPopUp();
	public abstract void showEnlargedPhoto(String key);
	public abstract void addPhoto(byte[] data, String fileName);
	public abstract boolean deletePhoto(String key);
	public abstract void setPhotosNumber(int count);
	
	
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
		
		UiApplication.getUiApplication().pushScreen(new PostPreviewView(html));	
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
					
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}

						
						try {
							html = (String)resp.getResponseObject();
						} catch (Exception e) {
							startLocalPreview(title,content,tags, cats, photoNumber);
							return;
						}
						
						UiApplication.getUiApplication().pushScreen(new PostPreviewView(html));	
						
					} else {
						startLocalPreview(title,content,tags, cats, photoNumber);
					}
					
					
				}
			});
		}
	} 
	
}
