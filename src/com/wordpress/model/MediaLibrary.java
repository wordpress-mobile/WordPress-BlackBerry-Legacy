package com.wordpress.model;

/**
 * This class rappresents one media library
 * @author dercoli
 *
 */
public class MediaLibrary extends BlogEntry {

	private String title = null;
    private Boolean isPhotoResizing = null; // 0 = false ; 1 = true //null = get option from blog settings
    private Integer imageResizeWidth = null;
    private Integer imageResizeHeight = null;
    
    public Boolean isPhotoResizing() {
		return isPhotoResizing;
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

	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
}
