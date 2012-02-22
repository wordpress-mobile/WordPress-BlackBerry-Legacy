package com.wordpress.utils;

import java.util.Vector;

import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.util.StringUtilities;


public class MultimediaUtils {

	public static boolean isPhotoCaptureSupported(){
		if( System.getProperty("video.snapshot.encodings")!= null 
				&& System.getProperty("video.snapshot.encodings").trim().length()>0 ){
			return true;
		} else 
			return false;
	}
	
	public static boolean isVideoRecordingSupported(){		
		int moduleHandle = CodeModuleManager.getModuleHandle("net_rim_bb_videorecorder"); 
		if(moduleHandle == 0) 
			return false;
		else 
			return true;
	/*
	if(System.getProperty("supports.video.capture") != null
			&& System.getProperty("supports.video.capture").trim().equalsIgnoreCase("true")
			&& System.getProperty("video.encodings")!=null){
		return true;
	} else {
		return false;
		}*/
	}
	
	public static boolean isAudioRecordingSuported(){
		if( System.getProperty("supports.audio.capture")!= null &&
				System.getProperty("supports.audio.capture").trim().equalsIgnoreCase("true")){
		return true;
	} else {
		return false;
		}
	}
	
	public static String[] getSupportedAudioFormat(){
		String formatiSuportati=System.getProperty("audio.encodings");
		formatiSuportati="default "+formatiSuportati;
		String[] lines=StringUtils.split(formatiSuportati, " ");
		return lines;
	}

	public static ImageEncodingProperties[] getSupportedPhotoFormat() throws Exception {
		ImageEncodingProperties[] _encodings;

		// Retrieve the list of valid encodings
		String encodingString = System.getProperty("video.snapshot.encodings");

		// Extract the properties as an array of word
		String[] properties = StringUtilities.stringToKeywords(encodingString);

		// The list of encodings
		Vector encodingList = new Vector();

		// Strings representing the three properties of an encoding as
		// returned by System.getProperty().
		String encoding = "encoding";
		String width = "width";
		String height = "height";            

		ImageEncodingProperties temp = null;

		for(int i = 0; i < properties.length ; ++i)
		{
			if( properties[i].equals(encoding))
			{
				if(temp != null && temp.isComplete())
				{
					// Add a new encoding to the list if it
					// has been properly set.
					encodingList.addElement( temp );
				}
				temp = new ImageEncodingProperties();

				// Set the new encoding's format
				++i;
				temp.setFormat(properties[i]);
			}
			else if( properties[i].equals(width))
			{
				// Set the new encoding's width
				++i;
				temp.setWidth(properties[i]);
			}
			else if( properties[i].equals(height))
			{
				// Set the new encoding's height
				++i;
				temp.setHeight(properties[i]);
			}                
		}

		// If there is a leftover complete encoding, add it
		if(temp != null && temp.isComplete())
		{
			encodingList.addElement( temp );
		}

		// Convert the Vector to an array for later use
		_encodings = new ImageEncodingProperties[encodingList.size()];

		encodingList.copyInto((Object[])_encodings);

		return _encodings;
	}
	
	/**
	 *  get the correct encoding string, based on user preference, for take a photo with built-in camera.
	 * @param choice
	 * @return 
	 */
	public static String getPhotoEncoding(int choice){
		String encoding = null;
		switch (choice){
			case 0:
				encoding = "encoding=jpeg&width=1600&height=1200&quality=superfine";
				break;
			case 1:
				encoding = "encoding=jpeg&width=1600&height=1200&quality=fine";
				break;
			case 2:
				encoding = "encoding=jpeg&width=1600&height=1200&quality=normal";
				break;
			case 3:
				encoding = "encoding=jpeg&width=1024&height=768&quality=superfine";
				break;
			case 4:
				encoding = "encoding=jpeg&width=1024&height=768&quality=fine";
				break;
			case 5:
				encoding = "encoding=jpeg&width=1024&height=768&quality=normal";
				break;
			case 6:
				encoding = "encoding=jpeg&width=640&height=480&quality=superfine";
				break;
			case 7:
				encoding = "encoding=jpeg&width=640&height=480&quality=fine";
				break;
			case 8:
				encoding = "encoding=jpeg&width=640&height=480&quality=normal";
				break;
			default:
				encoding = "encoding=jpeg&width=640&height=480&quality=superfine";	
			
		}
		return encoding;
	}

	public static String[] getSupportedWordPressImageFormat() {
		String[] imageFormats = { "jpg", "jpeg","bmp", "png", "gif"};
		return imageFormats;
	}
	
	public static String[] getSupportedWordPressVideoFormat(){
		String[] lines= {"avi", "mov", "mp4", "m4v", "mpg", "3gp", "3g2"};
		return lines; 
		/*
		String formatiSuportati=System.getProperty("video.encodings");
		formatiSuportati="default "+formatiSuportati;
		Log.debug("Supported Video File Formats: "+formatiSuportati);
		String[] lines=StringUtils.split(formatiSuportati, " ");
		return lines;*/
	}	
	
	public static String[] getSupportedWordPressAudioFormat(){
		String audioExtensions[] = { "mp3", "m4a","wav", "ogg"};
		return audioExtensions;
	}
	
	
	public static String getFileMIMEType(String ext){
		String mime = null;
		mime = getImageMIMEType(ext); 
		if(!mime.equalsIgnoreCase("")) return mime;
		mime = getAudioMIMEType(ext);
		if(!mime.equalsIgnoreCase("")) return mime;
		mime = getVideoMIMEType(ext);
		if(!mime.equalsIgnoreCase("")) return mime;
		
		return "";
	}
	
	
	
	public static String getImageMIMEType(String ext){
		if(ext.toLowerCase().equals("jpg"))
			return "image/jpeg";
		
		if(ext.toLowerCase().equals("jpeg"))
			return "image/jpeg";

		if(ext.toLowerCase().equals("bmp"))
			return "image/bmp";
		
		if(ext.toLowerCase().equals("png"))
			return "image/png";
		
		if(ext.toLowerCase().equals("gif"))
			return "image/gif";

		return "";		
	}
	
	
	public static String getVideoMIMEType(String ext){
		if(ext.toLowerCase().equals("avi"))
			return "video/x-msvideo";
		
		if(ext.toLowerCase().equals("mov"))
			return "video/quicktime";

		if(ext.toLowerCase().equals("mp4"))
			return "video/mp4";
		
		if(ext.toLowerCase().equals("m4v"))
			return "video/mp4";
		
		if(ext.toLowerCase().equals("mpg"))
			return "video/mpeg";

		if(ext.toLowerCase().equals("3gp"))
			return "video/3gpp";
		
		if(ext.toLowerCase().equals("3g2"))
			return "video/3gpp2";
		
		return "";		
	}

	public static String getAudioMIMEType(String ext){
		if(ext.toLowerCase().equals("mp3"))
			return "audio/mpeg";
		
		if(ext.toLowerCase().equals("m4a"))
			return "audio/mp4";
		
		if(ext.toLowerCase().equals("wav"))
			return "audio/wav";
		
		if(ext.toLowerCase().equals("ogg"))
			return "application/ogg";
		
		return "";		
	}
	
}

