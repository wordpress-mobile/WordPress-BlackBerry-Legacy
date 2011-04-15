package com.wordpress.model;

import java.util.Vector;

/**
 * This is the base class for main blog objs - pages, posts, galleries
 * @author Danilo Ercoli
 *
 */
public class BlogEntry {
	
    private Vector mediaObjects = new Vector();
	private Vector customFields = new Vector();

/*	protected Boolean isPhotoResizing = null; // 0 = false ; 1 = true //null = get option from blog settings
	protected Integer imageResizeWidth = null;
	protected Integer imageResizeHeight = null;
	
	protected Boolean isVideoResizing = null; // 0 = false ; 1 = true //null = get option from blog settings
	protected Integer videoResizeWidth = null;
	protected Integer videoResizeHeight = null;
	*/
	public void setCustomFields(Vector custom_field) {
		this.customFields = custom_field;
	}

	public Vector getCustomFields() {
		return customFields;
	}
    
	public Vector getMediaObjects() {
		return mediaObjects;
	}

	public void setMediaObjects(Vector mediaObjects) {
		this.mediaObjects = mediaObjects;
	}
	/*
	public Boolean isPhotoResizing() {
		return isPhotoResizing;
	}

	public void setPhotoResizing(Boolean isPhotoResizing) {
		this.isPhotoResizing = isPhotoResizing;
	}
	
	public Integer getImageResizeWidth() {
		return imageResizeWidth;
	}
	
	public void setImageResizeWidth(Integer imageResizeWidth) {
		this.imageResizeWidth = imageResizeWidth;
	}
	
	public Integer getImageResizeHeight() {
		return imageResizeHeight;
	}
	
	public void setImageResizeHeight(Integer imageResizeHeight) {
		this.imageResizeHeight = imageResizeHeight;
	}

	public Boolean isVideoResizing() {
		return isVideoResizing;
	}

	public void setVideoResizing(Boolean isVideoResizing) {
		this.isVideoResizing = isVideoResizing;
	}

	public Integer getVideoResizeWidth() {
		return videoResizeWidth;
	}

	public void setVideoResizeWidth(Integer videoResizeWidth) {
		this.videoResizeWidth = videoResizeWidth;
	}

	public Integer getVideoResizeHeight() {
		return videoResizeHeight;
	}

	public void setVideoResizeHeight(Integer videoResizeHeight) {
		this.videoResizeHeight = videoResizeHeight;
	}
	*/
}
