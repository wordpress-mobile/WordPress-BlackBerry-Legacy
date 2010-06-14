package com.wordpress.model;

/**
 * This class rappresents one media library
 * @author dercoli
 *
 */
public class MediaLibrary extends BlogEntry {

	private String title = null;
 
    private boolean isCutAndPaste = false; //cut&paste the response from the server when sending data
    
	public void setTitle(String title) {
		this.title = title;
	}
	public String getTitle() {
		return title;
	}
    public boolean isCutAndPaste() {
		return isCutAndPaste;
	}
	public void setCutAndPaste(boolean isCutAndPaste) {
		this.isCutAndPaste = isCutAndPaste;
	}
}
