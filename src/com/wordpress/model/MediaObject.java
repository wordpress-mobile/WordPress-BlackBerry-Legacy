package com.wordpress.model;

import com.wordpress.utils.StringUtils;

/**
 * This abstract class encapsulates all property of media
 * component.
 */
public class MediaObject {

	// the mediaData
	protected byte[] mediaData = null;
	// the contentType of the media
	protected String contentType = null;

	public byte[] getMediaData() {
		return this.mediaData;
	}

	public void setMediaData(byte[] mediaData) {
		this.mediaData = mediaData;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	//descrizione della img
	private String description="";

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * This method tries to guess the extension of the likely media data file
	 * that will be gauged from the content type of this data
	 */
	public String guessFileExtension() {
		if (contentType == null || contentType.length() == 0)
			return "";
		contentType = contentType.toLowerCase();
		if (contentType.equals("audio/x-wav"))
			return "wav";
		if (contentType.equals("audio/wav"))
			return "wav";
		if (contentType.equals("audio/amr")
				|| contentType.equals("audio/amr-nb")
				|| contentType.equals("audio/amr-wb"))
			return "amr";
		
		if (contentType.equals("jpeg") || contentType.equals("jpg")
				|| contentType.equals("image/jpeg")
				|| contentType.equals("image/jpg"))
			return "jpg";
		if (contentType.equals("gif") || contentType.equals("image/gif"))
			return "gif";
		if (contentType.equals("png") || contentType.equals("image/png"))
			return "png";
		
		if (contentType.equals("video/mpeg"))
			return "mpg";
		if (contentType.equals("video/3gpp") || contentType.equals("video/3gp"))
			return "3gp";
		
		//String ext=contentType.substring(contentType.lastIndexOf('/')+1);
		String[] contentTypeTokenized=StringUtils.split2Strings(contentType, '/');
		if(contentTypeTokenized[1]!=null) {
			return contentTypeTokenized[1];
		} else 
		return "unknow"; 
		
	}
}