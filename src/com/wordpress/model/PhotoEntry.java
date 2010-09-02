package com.wordpress.model;

import java.util.Hashtable;

import net.rim.device.api.system.Bitmap;

public class PhotoEntry extends MediaEntry {
	
	/* constants used in photo rotation */
	public final static int NO_ROTATION = 0;
	public final static int ROTATION_90 = 90;
	public final static int ROTATION_180 = 180;
	public final static int ROTATION_270 = 270;
	
	public int rotationAngle = NO_ROTATION;
	
	public PhotoEntry() {
		super();
	}
	
/*	public int getRotationAngle() {
		return rotationAngle;
	}

	public void setRotationAngle(int rotationAngle) {
		this.rotationAngle = rotationAngle;
	}

	public Hashtable serialize() {
		Hashtable hashtable = super.serialize();
		hashtable.put("rotationAngle", new Integer(rotationAngle));
		return hashtable;
	}
	
	protected void _deserialize(Hashtable hash) {
		Integer _rotationAngle = (Integer)hash.get("rotationAngle");
		if(_rotationAngle != null)
			rotationAngle = _rotationAngle.intValue();
	}
	*/
	
	/**
	 * get the thumb - 128x128 pixel
	 * @return
	 */
	public Bitmap getThumb() {
		return getThumb(128, 128);
	}
	
	public Bitmap getThumb(int width, int height) {
		return  Bitmap.getBitmapResource(predefinedThumb);
	}
	
	public String getMediaObjectAsSmallHtml() {
		
		StringBuffer tmpBuff = new StringBuffer();
			tmpBuff.append("<p>"+
					"<img class=\"alignnone size-full\"" +
					" src=\""+this.getFilePath()+"\" alt=\"\" " +
					"</p>");
		return tmpBuff.toString();
	}
	
	/**
	 * Return the full html rappresentation of the media obj
	 * @return
	 */
	public String getMediaObjectAsHtml() {
		/*
		 * Return the html rappresentation of the image
		 * 
		 * <div id="attachment_30" class="wp-caption alignnone" style="width: 830px">
		 * <a href="http://localhost/wp_mopress/wp-content/uploads/2009/03/back.jpg">
		 * <img src="http://localhost/wp_mopress/wp-content/uploads/2009/03/back.jpg" alt="Utilizzato anche come testo alternativo" title="back" width="820" height="992" class="size-full wp-image-30" />
		 * </a>
		 * <p class="wp-caption-text">Utilizzato anche come testo alternativo</p>
		 * </div>
		 * 
		 * @return
		 */
		
		StringBuffer tmpBuff = new StringBuffer();
		
		String title = this.getTitle() != null ? this.getTitle() : this.getFileName();
		String caption = this.getCaption() != null ? this.getCaption() : "";
		
		if(!caption.equals("")) {
			//<div id="attachment_30" class="wp-caption alignnone" style="width: 830px">
			int divWidth = this.getWidth()+10; //adding 10px padding
			tmpBuff.append("<div class=\"wp-caption alignnone\" style=\"width: "+divWidth+"px\"");
		} else {
			tmpBuff.append("<p>");
		}
		
		tmpBuff.append("<a href=\""+this.getFileURL()+"\">" +
				"<img class=\"alignnone size-full\"" +
				" src=\""+this.getFileURL()+"\" alt=\""+caption+"\"" +
				" title=\""+title+"\"" +
				" width=\""+this.getWidth()+"\" height=\""+this.getHeight()+"\" />" +
		"</a>");
		
		if(!caption.equals("")) {
			tmpBuff.append("<p class=\"wp-caption-text\">");
			tmpBuff.append(caption);
			tmpBuff.append("</p>");
			tmpBuff.append("</div>");
		} else {
			tmpBuff.append("</p>");
		}
		return tmpBuff.toString();
	}
}
