package com.wordpress.model;

import java.util.Hashtable;

import net.rim.device.api.system.Bitmap;

import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;

public abstract class MediaEntry {
	
	/*constants for horizontal alignment
	public final static int ALIGNMENT_NONE = 0;
	public final static int ALIGNMENT_LEFT = 1;
	public final static int ALIGNMENT_CENTER = 2;
	public final static int ALIGNMENT_RIGHT = 3;
	*/
	protected String filePath = null; //path of the file into disk
	protected String fileName = null; //name of the file into the server
	protected String title = null;
	protected String caption = null;
	protected String description = null;
	protected String fileURL = null;
	protected boolean verticalAlignment = false; //false = bottom, true = top
	protected int width, height;
	protected String MIMEType = ""; //do not store this value
	protected String videoPressShortCode = null; 
	//protected int horizontalAlignment = 0; //applicable on photo only
	
	protected String predefinedThumb = "mime_unknown.png";

	public MediaEntry() {}
	
	/**
	 * Return the full html rappresentation of the media obj
	 * @return
	 */
	public abstract String getMediaObjectAsHtml();
	
	/**
	 * Return the Small html rappresentation of the media obj, usefull on preview
	 * @return
	 */
	public abstract String getMediaObjectAsSmallHtml() ;
	
	
	public abstract Bitmap getThumb();
	
	public abstract Bitmap getThumb(int width , int height);
	
	
	public Hashtable serialize() {
		Hashtable hash = new Hashtable();
		String type= this.getClass().getName();
		hash.put("type", type);
		hash.put("filePath", filePath);
		
		if(fileName != null)
			hash.put("fileName", fileName);
		if(title != null)
			hash.put("title", title);
		if(caption != null)
			hash.put("caption", caption);
		if(description != null)
			hash.put("description", description);
		if(fileURL != null)
			hash.put("fileURL", fileURL);
		
		if(videoPressShortCode != null)
			hash.put("videoPressShortCode", videoPressShortCode);
		
		if(verticalAlignment)
			hash.put("verticalAligment", "1");
		else
			hash.put("verticalAligment", "0");
		
		if(width > 0)
			hash.put("width", String.valueOf(width));
		
		if(height > 0)
			hash.put("height", String.valueOf(height));
		
	//	hash.put("horizontalAligment", String.valueOf(horizontalAlignment));
		
		return hash;
	}
	
	//all subclasses should implements this method to deserialize their own fields.
	protected void _deserialize(Hashtable hash) {
		
	}
	
	public static MediaEntry deserialize(Hashtable hash) {
		
		MediaEntry tmpMedia = null;
		// Get a class reference for the concrete factory
		Class factoryClass = null;
		String type = (String)hash.get("type");
		try {
			factoryClass = Class.forName(type);
		} catch (ClassNotFoundException e) {
			Log.trace(e, "Unable to instantiate media object");
			return null;
		}
		
		try {
			Object instance = factoryClass.newInstance();
			tmpMedia = (MediaEntry)instance;
		} catch (InstantiationException e) {
			Log.trace(e, "Unable to instantiate " + factoryClass.getName());
			return null;
		} catch (IllegalAccessException e) {
			Log.trace(e, "Unable to instantiate " + factoryClass.getName());
			return null;
		}
		
		String filePath = (String)hash.get("filePath");
		tmpMedia.setFilePath(filePath);

		String fileName = (String)hash.get("fileName");
		if(fileName != null)
			tmpMedia.fileName = fileName;
		
		String videoPressShortCode = (String)hash.get("videoPressShortCode");
		if(videoPressShortCode != null)
			tmpMedia.videoPressShortCode = videoPressShortCode;
		
		String title = (String)hash.get("title");
		if(title != null)
			tmpMedia.title = title;
		
		String caption = (String)hash.get("caption");
		if(caption != null)
			tmpMedia.caption = caption;
		
		String description = (String)hash.get("description");
		if(description != null)
			tmpMedia.description = description;

		String fileURL = (String)hash.get("fileURL");
		if(fileURL != null)
			tmpMedia.fileURL = fileURL;

		String verticalAligment = (String)hash.get("verticalAligment");
		if(verticalAligment!= null && verticalAligment.equalsIgnoreCase("1"))
			tmpMedia.verticalAlignment = true;
		else
			tmpMedia.verticalAlignment = false;
		
		String width = (String)hash.get("width");
		if(width!= null)
			tmpMedia.width = Integer.parseInt(width);
		
		String height = (String)hash.get("height");
		if(height!= null)
			tmpMedia.height = Integer.parseInt(height);
		
	/*	String horizontalAligmentStr = (String)hash.get("horizontalAligment");
		if(horizontalAligmentStr!= null)
			tmpMedia.horizontalAlignment = Integer.parseInt(horizontalAligmentStr);
		else
			tmpMedia.horizontalAlignment = ALIGNMENT_NONE;
	*/
		
		//call the subclasses deserialize submethod
		tmpMedia._deserialize(hash);
		
		return tmpMedia;
	}
	


	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;

		//set the file name part only if it is null
		if(fileName == null) {
	   	 	String[] fileNameSplitted = StringUtils.split(filePath, "/");
	   	 	String fileName= fileNameSplitted[fileNameSplitted.length-1];
	        //decode the escaped filename - ASCII format as defined by RFC 2396
	   	 	try {
				fileName = StringUtils.decode(fileName);
			} catch (Exception e) {
				Log.trace(e, "error while decoding file name");
			}
	   	 	this.fileName = fileName;
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCaption() {
		return caption;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFileURL() {
		return fileURL;
	}

	public void setFileURL(String fileURL) {
		this.fileURL = fileURL;
	}

	public boolean isVerticalAlignmentOnTop() {
		return verticalAlignment;
	}

	public void setVerticalAlignmentOnTop(boolean verticalAligment) {
		this.verticalAlignment = verticalAligment;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getMIMEType() {
		return MIMEType;
	}

	public void setMIMEType(String type) {
		MIMEType = type;
	}

	public String getVideoPressShortCode() {
		return videoPressShortCode;
	}
/*
	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}
*/
	public void setVideoPressShortCode(String videoPressShortCode) {
		this.videoPressShortCode = videoPressShortCode;
	}	
}