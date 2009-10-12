package com.wordpress.utils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.wordpress.task.SendToBlogTask;
import com.wordpress.utils.log.Log;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;

/**
 * @author schalk
 * @author daniloercoli
 */
public class ImageUtils {
	
	/**
	 * Sets the image width to the optimal width for the BlackBerry device screen
	 * 
	 * @param _img The image to scale
	 * @return The optimised image
	 */
	public static EncodedImage setOptimalWidth(EncodedImage _img) {
		int _preferedWidth = -1;
		EncodedImage _resImg = null;
		if(_img.getWidth() > Display.getWidth()) {
			_preferedWidth = Display.getWidth();
		}
		if(_preferedWidth != -1) {
			_resImg = ImageUtils.resizeEncodedImage(_img, _preferedWidth, _img.getHeight());
			_img = _resImg;
		}
		return _img;
	}
	
	public static EncodedImage resizeEncodedImage(EncodedImage image, int maxWidth, int maxHeight)
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
		 
	  
	  public static Image createResizedImg(Image image) {
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

	public static int findBestImgScale(EncodedImage image, int maxWidth, int maxHeight) {
			// getting image properties
			int w = image.getWidth();
			int h = image.getHeight();
			
			if(w < maxWidth && h < maxHeight) return 1; //image is smaller than the desidered size...no resize!
			
				
			double numeratorW = w;
			double denominatorW = maxWidth;
			int scaleW = (int)Tools.round((numeratorW / denominatorW));
	
			double numeratorH = h;
			double denominatorH = maxHeight;
			int scaleH = (int) Tools.round(numeratorH / denominatorH);
			
			if(scaleH > scaleW) {
				return scaleH;
			} else
			{
				return scaleW;
			}
	  }

	//TODO: remove the task as parameter, use listener instead
		public static Hashtable resizePhoto(byte[] data, String fileName, SendToBlogTask task) throws IOException {
		
		EncodedImage originalImage = EncodedImage.createEncodedImage(data, 0, -1);
		Hashtable content = new Hashtable(2);
	
		//init the hash table with no resized img data
		content.put("name", fileName);
		content.put("height", String.valueOf(originalImage.getHeight()));
		content.put("width", String.valueOf(originalImage.getWidth()));
		content.put("bits", data);
		content.put("type", originalImage.getMIMEType());
		//no resize is necessary
		if(originalImage.getWidth() <= 640 && originalImage.getWidth() <= 480) {
			Log.trace("no resize required"+fileName);
			return content;
		}
		
		int type = originalImage.getImageType();
				
		//starting resize
		EncodedImage bestFit2 = resizeEncodedImage(originalImage, 640, 480);
		originalImage = null;
		Bitmap resizedBitmap = bestFit2.getBitmap();
		bestFit2 = null;
		
		byte[] imageBytes;
		switch (type) {
	
		case EncodedImage.IMAGE_TYPE_PNG:
			//PNGEncoder encoderPNG = new PNGEncoder(resizedBitmap,true);
			//byte[] imageBytes = encoderPNG.encode(true);
			try {
				imageBytes = ImageUtils.toPNG(resizedBitmap);
				if (task != null && task.isStopped()) return null; //resizing img is a long task. if user has stoped the operation..
			} catch (Exception e) {
				Log.error(e, "Error during PNG encoding, restore prev img");
				imageBytes = data;
			}
			if (fileName.endsWith("png") || fileName.endsWith("PNG")){
				
			} else {
				fileName+=".png";
			}
			
			break;
			
		case EncodedImage.IMAGE_TYPE_JPEG:
		default:
	
			try {
					Log.trace("starting resizing to jpg format ");
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					OutputStream nuoviBytes= new DataOutputStream(out);
					JpegEncoder jpgenc= new JpegEncoder(resizedBitmap, 75 , nuoviBytes, task);
					if (task != null && task.isStopped()) return null; //resizing img is a long task. if user has stoped the operation..
					imageBytes = out.toByteArray();
				} catch (Exception e) {
					Log.error(e, "Error during JPEG encoding, restore prev img");
					imageBytes = data;
				}
	
			//check file name ext eventually add jpg ext
			if (fileName.endsWith("jpg") || fileName.endsWith("JPG")){				
			} else {
				fileName+=".jpg";
			}
		break;
	
		}//end switch
			
	
		Log.trace("checking new img size");
		if(imageBytes.length >= data.length) {
			Log.trace("new img bites size > = orig img bites size");
			//using original img			
		} else {
			Log.trace("new img bites size < orig img bites size");
			content.put("name", fileName);
			content.put("height", String.valueOf(resizedBitmap.getHeight()));
			content.put("width", String.valueOf(resizedBitmap.getWidth()));
			content.put("bits", imageBytes );	
			//set the new mime type
			if (fileName.endsWith("jpg") || fileName.endsWith("JPG")){
				content.put("type", "image/jpeg");
			} else {
				content.put("type", "image/png");
			}
		}
		
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
		private static byte[] toPNG(Bitmap image) throws IOException {
			
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

	  
}
