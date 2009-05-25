package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.bb.WordPress;
import com.wordpress.view.PostPreviewView;

public abstract class BlogObjectController extends BaseController {
	
	protected static final String LOCAL_DRAFT_KEY = "localdraft";
	protected static final String LOCAL_DRAFT_LABEL = _resources.getString(WordPress.LABEL_LOCAL_DRAFT);

	
	public abstract void setSettingsValues(long authoredOn, String password);
		
	public abstract void showAddPhotoPopUp();
	public abstract void showEnlargedPhoto(String key);
	public abstract void addPhoto(byte[] data, String fileName);
	public abstract boolean deletePhoto(String key);
	public abstract void setPhotosNumber(int count);
	
	public void showPreview(){
		UiApplication.getUiApplication().pushScreen(new PostPreviewView(""));
	}
	
}
