package com.wordpress.utils;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class MultimediaUtils {
	
	
	
  public static Image resizeImageAndCopyPrevious(final int newWidth, final int newHeight,
	      final Image resized) {
	    // TODO : if new is smaller can optimize with
	    // createImage(Image image, int x, int y, int width, int height, int
	    // transform)
	    final Image result = Image.createImage(newWidth, newHeight);
	    final Graphics g = result.getGraphics();
	    g.drawImage(resized, (newWidth - resized.getWidth()) / 2,
	        (newHeight - resized.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);
	    return result;
	  }
	
	/**
	 * Creates an immutable image which is decoded from the data stored in the specified byte array at
	 * the specified offset and length. The data must be in a self-identifying image file format supported 
	 * by the implementation, such as PNG. 

	 * @param image
	 * @return
	 */
	  public static Image createImage(byte[] imageData) {
		    return Image.createImage(imageData,0,imageData.length-1 );
		}
	
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
