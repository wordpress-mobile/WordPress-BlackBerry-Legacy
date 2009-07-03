package com.wordpress.utils;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.wordpress.utils.log.Log;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;


public class MultimediaUtils {

		public static Hashtable resizePhotoAndOutputJpeg(byte[] data, String fileName) throws IOException {
				
		EncodedImage originalImage = EncodedImage.createEncodedImage(data, 0, -1);
		Hashtable content = new Hashtable(2);

		//no resize
		if(originalImage.getWidth() <= 640 && originalImage.getWidth() <= 480) {
			content.put("name", fileName);
			content.put("height", String.valueOf(originalImage.getHeight()));
			content.put("width", String.valueOf(originalImage.getWidth()));
			content.put("bits", data);
			return content;
		}
		
		int type = originalImage.getImageType();
		
		//starting resize
		EncodedImage bestFit2 = bestFit2(originalImage, 640, 480);
		originalImage = null;
		Bitmap resizedBitmap = bestFit2.getBitmap();
	
		if (fileName.endsWith("png") || fileName.endsWith("PNG")){
			
		} else {
			fileName+=".png";
		}
		
		//PNGEncoder encoderPNG = new PNGEncoder(resizedBitmap,true);
		//byte[] imageBytes = encoderPNG.encode(true);
		byte[] imageBytes;
		try {
			imageBytes = toPNG(resizedBitmap);
		} catch (Exception e) {
			Log.error(e, "Error during PNG encoding");
			imageBytes = data;
		}
		
		//EncodedImage fullImage = EncodedImage.createEncodedImage(imageBytes, 0, imageBytes.length);
		content.put("name", fileName);
		content.put("height", String.valueOf(resizedBitmap.getHeight()));
		content.put("width", String.valueOf(resizedBitmap.getWidth()));
		content.put("bits", imageBytes );
		return content;
	}

		
		/** 
		 * Returns a PNG stored in a byte array from the supplied Image.
		 *
		 * @param image   an Image object
		 * @return        a byte array containing PNG data
		 * @throws IOException 
		 *
		 */
		public static byte[] toPNG(Bitmap image) throws IOException {
			
			int imageSize = image.getWidth() * image.getHeight();
			int[] rgbs = new int[imageSize];
			byte[] a, r, g, b;
			int colorToDecode;
			
			image.getARGB(rgbs, 0, image.getWidth() , 0, 0, image.getWidth(), image.getHeight());
			
			a = new byte[imageSize];
			r = new byte[imageSize];
			g = new byte[imageSize];
			b = new byte[imageSize];
			
			for (int i = 0; i < imageSize; i++) {
				colorToDecode = rgbs[i];
				
				a[i] = (byte) ((colorToDecode & 0xFF000000) >>> 24);
				r[i] = (byte) ((colorToDecode & 0x00FF0000) >>> 16);
				g[i] = (byte) ((colorToDecode & 0x0000FF00) >>> 8);
				b[i] = (byte) ((colorToDecode & 0x000000FF));
			}
			
			return MinimalPNGEncoder.toPNG(image.getWidth(), image.getHeight(), a, r, g, b);
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
