package com.wordpress.utils;

public class MultimediaUtils {
	
	public static boolean supportPhotoCapture(){
		if( System.getProperty("supports.video.capture")!=null
				&& System.getProperty("supports.video.capture").trim().equalsIgnoreCase("true") 
				&& System.getProperty("video.snapshot.encodings")!=null){
			return true;
		} else 
			return false;
	}
	
	public static boolean supportVideoRecording(){
	if(System.getProperty("supports.video.capture") != null
			&& System.getProperty("supports.video.capture").trim().equalsIgnoreCase("true")
			&& System.getProperty("video.encodings")!=null){
		return true;
	} else {
		return false;
		}
	}
	
	public static boolean supportAudioRecording(){
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

	public static String[] getSupportedPhotoFormat(){
		String formatiSuportati=System.getProperty("video.snapshot.encodings");
		formatiSuportati="default "+formatiSuportati;
		String[] lines=StringUtils.split(formatiSuportati, " ");
		return lines;
	}
	
	public static String[] getSupportedVideoFormat(){
		String formatiSuportati=System.getProperty("video.encodings");
		formatiSuportati="default "+formatiSuportati;
		String[] lines=StringUtils.split(formatiSuportati, " ");
		return lines;
	}	
	

}
