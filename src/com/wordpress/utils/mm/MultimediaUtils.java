package com.wordpress.utils.mm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.EncodedImage;

import com.wordpress.utils.StringUtils;

public class MultimediaUtils {
	

	public static Hashtable resizePhotoAndOutputJpeg(byte[] data, String fileName) throws IOException {
		
		Hashtable content = new Hashtable(2);
		Image createImage = Image.createImage(new ByteArrayInputStream(data));	
		//no resize
		if(createImage.getWidth() <= 640 && createImage.getWidth() <= 480) {
			content.put("name", fileName);
			content.put("bits", data);
			return content;
		}
				
		Image resized = createResizedImg(createImage);
			
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		OutputStream nuoviBytes= new DataOutputStream(out);
		JpegEncoder jpgenc= new JpegEncoder(resized,100, nuoviBytes);

		//check file name ext eventually add jpg ext
		if (fileName.endsWith("jpg") || fileName.endsWith("JPG")){
			
		} else {
			fileName+=".jpg";
		}
		
		content.put("name", fileName);
		content.put("bits",  out.toByteArray());
		return content;
		
		/*EncodedImage rescaled= MultimediaUtils.bestFit2(img, 256, 256);
		Bitmap original= img.getBitmap();
    	
    	PNGEncoder encoder = new PNGEncoder(original, true);
    	byte[] imageBytes = encoder.encode(true);
	    	*/
	    	
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

	  
	  
/*
	public static Bitmap bestFit(Bitmap image, int maxWidth, int maxHeight)
	{

		// getting image properties
		int w = image.getWidth();
		int h = image.getHeight();

		//  get the ratio
		int ratiow = 100 * maxWidth / w;
		int ratioh = 100 * maxHeight / h;

		// this is to find the best ratio to resize the image without deformations
		int ratio = Math.min(ratiow, ratioh);

		// computing final desired dimensions
		int desiredWidth = w * ratio / 100;
		int desiredHeight = h * ratio / 100;

		//resizing: 
		return resizeBitmap(image, desiredWidth, desiredHeight); 
	}

	private static int[] rescaleArray(int[] ini, int x, int y, int x2, int y2)
	{
		int out[] = new int[x2*y2];
		for (int yy = 0; yy < y2; yy++)
		{
			int dy = yy * y / y2;
			for (int xx = 0; xx < x2; xx++)
			{
				int dx = xx * x / x2;
				out[(x2 * yy) + xx] = ini[(x * dy) + dx];
			}
		}
		return out;
	}


	public static Bitmap resizeBitmap(Bitmap image, int width, int height)
	{	
		// Note from DCC:
		// an int being 4 bytes is large enough for Alpha/Red/Green/Blue in an 8-bit plane...
		// my brain was fried for a little while here because I am used to larger plane sizes for each
		// of the color channels....
		//

		//Need an array (for RGB, with the size of original image)
		//
		int rgb[] = new int[image.getWidth()*image.getHeight()];

		//Get the RGB array of image into "rgb"
		//
		image.getARGB(rgb, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());

		//Call to our function and obtain RGB2
		//
		int rgb2[] = rescaleArray(rgb, image.getWidth(), image.getHeight(), width, height);

		//Create an image with that RGB array
		//
		Bitmap temp2 = new Bitmap(width, height);

		temp2.setARGB(rgb2, 0, width, 0, 0, width, height);
		
		return temp2;
	}  
	 */ 
	
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
