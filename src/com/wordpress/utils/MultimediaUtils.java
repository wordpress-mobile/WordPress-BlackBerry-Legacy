package com.wordpress.utils;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;


public class MultimediaUtils {

	
	public static Hashtable resizePhotoAndOutputJpeg(byte[] data, String fileName) throws IOException {
				
		EncodedImage originalImage = EncodedImage.createEncodedImage(data, 0, -1);
		Hashtable content = new Hashtable(2);

		//no resize
		if(originalImage.getWidth() <= 640 && originalImage.getWidth() <= 480) {
			content.put("name", fileName);
			content.put("bits", data);
			return content;
		}
		
		int type = originalImage.getImageType();
		
		//starting resize
		EncodedImage bestFit2 = bestFit2(originalImage, 640, 480);
		originalImage = null;
		Bitmap resizedBitmap = bestFit2.getBitmap();
		
		EncodedImage resizedEncodedImg = null;
		
		switch (type) {
		case EncodedImage.IMAGE_TYPE_JPEG:
			resizedEncodedImg= JPEGEncodedImage.encode(resizedBitmap, 100);
			break;

		default:
			resizedEncodedImg= JPEGEncodedImage.encode(resizedBitmap, 75);
			//check file name ext eventually add jpg ext
			if (fileName.endsWith("jpg") || fileName.endsWith("JPG")){
				
			} else {
				fileName+=".jpg";
			}
			break;
		}
		
		
		content.put("name", fileName);
		content.put("bits",  resizedEncodedImg.getData());
		return content;

	    	
	}


	private static Image createResizedImg(Image image) {
	    int sourceWidth = image.getWidth();
	    int sourceHeight = image.getHeight();
	    
	    int thumbWidth = 640;
	    int thumbHeight = -1;
	    
	    if (thumbHeight == -1)
	        thumbHeight = thumbWidth * sourceHeight / sourceWidth;
	    
	    Image thumb = Image.createImage(thumbWidth, thumbHeight);
	    Graphics g = thumb.getGraphics();
	    
	    for (int y = 0; y < thumbHeight; y++) {
	        for (int x = 0; x < thumbWidth; x++) {
	            g.setClip(x, y, 1, 1);
	            int dx = x * sourceWidth / thumbWidth;
	            int dy = y * sourceHeight / thumbHeight;
	            g.drawImage(image, x - dx, y - dy, Graphics.LEFT | Graphics.TOP);
	        }
	    }
	    
	    Image immutableThumb = Image.createImage(thumb);
	    return immutableThumb;
	}
	
	
	
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
	 
	public static EncodedImage bestFit2(EncodedImage image, int maxWidth, int maxHeight)
	{

		// getting image properties
		int w = image.getWidth();
		int h = image.getHeight();
		
		if(w < maxWidth && h < maxHeight) return image; //image is smaller than the desidered size...no resize!
		
		
		int numeratorW = Fixed32.toFP(w);
		int denominatorW = Fixed32.toFP(maxWidth);
		int scaleW = Fixed32.div(numeratorW, denominatorW);

		int numeratorH = Fixed32.toFP(h);
		int denominatorH = Fixed32.toFP(maxHeight);
		int scaleH = Fixed32.div(numeratorH, denominatorH);
		
		if(scaleH > scaleW) {
			return image.scaleImage32(scaleH, scaleH);
		} else
		{
			return image.scaleImage32(scaleW, scaleW);
		}
				
	}
	
	public static boolean isPhotoCaptureSupported(){
		if( System.getProperty("video.snapshot.encodings")!= null 
				&& System.getProperty("video.snapshot.encodings").trim().length()>0 ){
			return true;
		} else 
			return false;
	}
	
	public static boolean isVideoRecordingSupported(){
	if(System.getProperty("supports.video.capture") != null
			&& System.getProperty("supports.video.capture").trim().equalsIgnoreCase("true")
			&& System.getProperty("video.encodings")!=null){
		return true;
	} else {
		return false;
		}
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

	public static String[] getSupportedPhotoFormat(){
		/*String formatiSuportati=System.getProperty("video.snapshot.encodings");
		formatiSuportati="default "+formatiSuportati;
		String[] lines=StringUtils.split(formatiSuportati, " ");
		return lines;
		*/
		String[] choices = {"SuperFine 1600x1200", "Fine 1600x1200", "Normal 1600x1200", "SuperFine 1024x768", 
				"Fine 1024x768", "Normal 1024x768", "SuperFine 640x480", "Fine 640x480", "Normal 640x480"};
		return choices;
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

	
	
	public static String[] getSupportedVideoFormat(){
		String formatiSuportati=System.getProperty("video.encodings");
		formatiSuportati="default "+formatiSuportati;
		String[] lines=StringUtils.split(formatiSuportati, " ");
		return lines;
	}	
	

}
