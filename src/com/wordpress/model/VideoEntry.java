package com.wordpress.model;

import net.rim.device.api.system.Bitmap;

public class VideoEntry extends MediaEntry {

	public VideoEntry() {
		super();
	}

	
	/**
	 * get the thumb - 128x128 pixel
	 * @return
	 */
	public Bitmap getThumb() {
		return  Bitmap.getBitmapResource(predefinedThumb);
	}
	
	/**
	 * Return the full html rappresentation of the media obj
	 * @return
	 */
	public String getMediaObjectAsHtml() {
	StringBuffer tmpBuff = new StringBuffer();
		String title = this.getTitle() != null ? this.getTitle() : this.getFileName();
	
		tmpBuff.append("<p>");
		tmpBuff.append("<a href=\""+this.getFileURL()+"\" title=\""+title+"\">"+
						this.getFileName()+
						"</a>");
		tmpBuff.append("</p>");
		return tmpBuff.toString();
	}
	
	
	/**
	 * Return the Small html rappresentation of the media obj, usefull on preview
	 * @return
	 */
	public String getMediaObjectAsSmallHtml() {
		StringBuffer tmpBuff = new StringBuffer();
			tmpBuff.append("<p>");
			tmpBuff.append("<a href=\""+this.getFileURL()+"\" title=\""+title+"\">"+
							this.getFileName()+
							"</a>");
			tmpBuff.append("</p>");		
		return tmpBuff.toString();
	}
}
