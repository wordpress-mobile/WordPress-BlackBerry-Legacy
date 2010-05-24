package com.wordpress.model;

import com.wordpress.utils.StringUtils;

import net.rim.device.api.system.Bitmap;

public class VideoEntry extends MediaEntry {


	public VideoEntry() {
		super();
		predefinedThumb = "video_thumb.png";
	}
	
	
	public Bitmap getThumb() {
		return  Bitmap.getBitmapResource(predefinedThumb);
	}
	
	public Bitmap getThumb(int width, int height) {
		return  getThumb();
	}
	
	/**
	 * Return the full html rappresentation of the media obj
	 * @return
	 */
	public String getMediaObjectAsHtml() {

		StringBuffer tmpBuff = new StringBuffer();
		String title = ""; 
		//if the user have set the title use it, otherwise use the real file name as title
		if(this.getTitle() != null) {
			title = this.getTitle();
		}
		else {
			title = this.getFileName();
			String[] split = StringUtils.split(title, "/");
			title = split[split.length-1];
		}
		//[wpvideo code]
		if(videoPressShortCode != null && !videoPressShortCode.trim().equals("")) {
			tmpBuff.append(videoPressShortCode);
		} else { 
			tmpBuff.append("<p>");
			tmpBuff.append("<a href=\""+this.getFileURL()+"\" title=\""+title+"\">"+
					title+
			"</a>");
			tmpBuff.append("</p>");
		}
		
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
