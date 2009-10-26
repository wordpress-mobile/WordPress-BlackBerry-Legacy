package com.wordpress.model;

import java.io.IOException;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;

import com.wordpress.io.JSR75FileSystem;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;

public class PhotoEntry extends MediaEntry {
	
	
	public PhotoEntry() {
		super();
	}

	/**
	 * get the thumb - 128x128 pixel
	 * @return
	 */
	public Bitmap getThumb() {
		byte[] readFile;
		Bitmap bitmapRescale;
		try {
			readFile = JSR75FileSystem.readFile(this.getFilePath());
			EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
			//find the photo size
			int scale = ImageUtils.findBestImgScale(img, 128, 128);
			if(scale > 1)
				img.setScale(scale); //set the scale
			
			bitmapRescale= img.getBitmap();
		} catch (IOException e) {
			Log.error("Error during img preview");
			bitmapRescale = Bitmap.getBitmapResource(predefinedThumb);
		}
		
		return bitmapRescale;
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
		 * <img src="http://localhost/wp_mopress/wp-content/uploads/2009/03/back.jpg" alt="Utilizzato anche come testo alternativo all’immagine" title="back" width="820" height="992" class="size-full wp-image-30" />
		 * </a>
		 * <p class="wp-caption-text">Utilizzato anche come testo alternativo all’immagine</p>
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
			tmpBuff.append("</div>");
		} else {
			tmpBuff.append("</p>");
		}
		return tmpBuff.toString();
	}
	
}
