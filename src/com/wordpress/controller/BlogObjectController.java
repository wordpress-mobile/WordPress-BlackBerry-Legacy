package com.wordpress.controller;

public abstract class BlogObjectController extends BaseController {

	
	public abstract void setSettingsValues(long authoredOn, String password);
		
	public abstract void showAddPhotoPopUp();
	public abstract void showEnlargedPhoto(String key);
	public abstract void addPhoto(byte[] data, String fileName);
	public abstract boolean deletePhoto(String key);
	public abstract void setPhotosNumber(int count);
	
}
