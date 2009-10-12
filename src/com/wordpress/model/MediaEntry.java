package com.wordpress.model;

import java.util.Hashtable;

import com.wordpress.utils.StringUtils;

public class MediaEntry {
	public static final int IMAGE_FILE = 0;
	public static final int VIDEO_FILE = 1;
	
	private int type = IMAGE_FILE; //default type is image
	private String filePath = null; //path of the file into disk
	private String fileName = null; //name of the file into the server
	private String title = null;
	private String caption = null;
	private String description = null;
	private String fileURL = null;
	private boolean verticalAligment = false; //false = bottom, true = top
	private int width, height;
	private String MIMEType = ""; //do not store this value

	
	public Hashtable getMediaObjectAsHashtable() {
		Hashtable hash = new Hashtable();
		
		hash.put("type", String.valueOf(type));
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
		
		if(verticalAligment)
			hash.put("verticalAligment", "1");
		else
			hash.put("verticalAligment", "0");
		
		if(width > 0)
			hash.put("width", String.valueOf(width));
		
		if(height > 0)
			hash.put("height", String.valueOf(height));
		
		return hash;
	}
	
	public MediaEntry(Hashtable hash) {
		super();
		
		String typeString = (String)hash.get("type");
		this.type = Integer.parseInt(typeString);
				
		String filePath = (String)hash.get("filePath");
		if(filePath != null)
			this.filePath = filePath;
		
		String fileName = (String)hash.get("fileName");
		if(fileName != null)
			this.fileName = fileName;
		
		String title = (String)hash.get("title");
		if(title != null)
			this.title = title;
		
		String caption = (String)hash.get("caption");
		if(caption != null)
			this.caption = caption;
		
		String description = (String)hash.get("description");
		if(description != null)
			this.description = description;

		String fileURL = (String)hash.get("fileURL");
		if(fileURL != null)
			this.fileURL = fileURL;

		
		String verticalAligment = (String)hash.get("verticalAligment");
		if(verticalAligment!= null && verticalAligment.equalsIgnoreCase("1"))
			this.verticalAligment = true;
		else
			this.verticalAligment = false;
		
		String width = (String)hash.get("width");
		if(width!= null)
			this.width = Integer.parseInt(width);
		
		String height = (String)hash.get("height");
		if(height!= null)
			this.height = Integer.parseInt(height);
	
	}
	
	public MediaEntry(String filePath) {
		super();
		this.filePath = filePath;
		//retrive the file name part
   	 	String[] fileNameSplitted = StringUtils.split(filePath, "/");
   	 	String fileName= fileNameSplitted[fileNameSplitted.length-1];
   	 	this.fileName = fileName;
	}
	

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
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
		return verticalAligment;
	}

	public void setVerticalAlignmentOnTop(boolean verticalAligment) {
		this.verticalAligment = verticalAligment;
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
}
