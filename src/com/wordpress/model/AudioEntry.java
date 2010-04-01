package com.wordpress.model;

import com.wordpress.utils.StringUtils;

import net.rim.device.api.system.Bitmap;

public class AudioEntry extends MediaEntry {

	public static final String audioPredefinedThumb = "audio.png";
	
	public AudioEntry() {
		super();
	}
	
	public Bitmap getThumb() {
		return  Bitmap.getBitmapResource(audioPredefinedThumb);
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


		tmpBuff.append("<p>");
		tmpBuff.append("<a href=\""+this.getFileURL()+"\" title=\""+title+"\">"+
				title+
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
