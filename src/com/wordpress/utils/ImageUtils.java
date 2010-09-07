package com.wordpress.utils;

import java.io.IOException;
import java.util.Hashtable;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;

import com.wordpress.task.SendToBlogTask;
import com.wordpress.utils.log.Log;

/**
 * @author schalk
 * @author daniloercoli
 */
public class ImageUtils {

	public static int DEFAULT_RESIZE_WIDTH = 640;
	public static int DEFAULT_RESIZE_HEIGHT = 480;


	public static int[] keepAspectRatio(int width, int height) {
		int[] returnValues = {DEFAULT_RESIZE_WIDTH, DEFAULT_RESIZE_HEIGHT};

		if(width == 0 || width == DEFAULT_RESIZE_WIDTH ) {
			return returnValues;
		}

		int newHeight = (int)(width * 0.75);
		if(height != newHeight)
			height = newHeight;

		returnValues[0] = width;
		returnValues[1] = height;
		return returnValues;
	}

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

	/*
	 * Rotates a Picture by a given angle.
	 * The output picture is always stored as JPG. 
	 * 
	 * The output picture is always a JPG to prevent out of memory exception when using a PNG image.
	 * This is because the PNG encoder by RIM is not fully compatible with browsers, so we are using 
	 * an external PNG encoder that is not fully optimized.
	 * 
	 */
	public static Hashtable rotatePhoto(byte[] data, int angle, String fileName) throws Exception  {
		EncodedImage originalImage = EncodedImage.createEncodedImage(data, 0, -1);
		Hashtable content = new Hashtable(5);
		//init the hash table with no rotated img data
		content.put("name", fileName);
		content.put("height", String.valueOf(originalImage.getHeight()));
		content.put("width", String.valueOf(originalImage.getWidth()));
		content.put("bits", data);
		content.put("type", originalImage.getMIMEType());

		EncodedImage img = EncodedImage.createEncodedImage(data, 0, -1);

		Bitmap rotatedBitmap = null;
		
		rotatedBitmap = ImageUtils.rotate(img.getBitmap(), angle);

		byte[] imageBytes = JPEGEncodedImage.encode(rotatedBitmap, 75).getData();
		//check file name ext eventually add jpg ext
		if (fileName.endsWith("jpg") || fileName.endsWith("JPG")){				
		} else {
			fileName+=".jpg";
		}

		if(rotatedBitmap != null) {
			content.put("name", fileName);
			content.put("height", String.valueOf(rotatedBitmap.getHeight()));
			content.put("width", String.valueOf(rotatedBitmap.getWidth()));

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

	/*
	public static Bitmap rotate(Bitmap bitmap, int angle) throws Exception {
		ImageManipulator imageTool_ = new ImageManipulator(bitmap);
		imageTool_.setBitmap(bitmap);
		imageTool_.transformByAngle(-1 * angle, false, false);
		return imageTool_.transformAndPaintBitmap();
	}
	*/
		
	public static Bitmap rotate(Bitmap bitmap, int angle) throws Exception
	{
		if(angle == 0)
		{
			return null; 
		}
		else if(angle != 180 && angle != 90 && angle != 270)
		{
			throw new Exception("Invalid angle");
		}
	 
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
	 
		int[] rowData = new int[width];
		int[] rotatedData = new int[width * height];
	 
		int rotatedIndex = 0;
	 
		for(int i = 0; i < height; i++)
		{
			bitmap.getARGB(rowData, 0, width, 0, i, width, 1);
	 
			for(int j = 0; j < width; j++)
			{
				rotatedIndex = 
					angle == 90 ? (height - i - 1) + j * height : 
					(angle == 270 ? i + height * (width - j - 1) : 
						width * height - (i * width + j) - 1
					);
	 
				rotatedData[rotatedIndex] = rowData[j];
			}
		}

		javax.microedition.lcdui.Image tmpJ2MEImage = null;
		if(angle == 90 || angle == 270)
		{
			tmpJ2MEImage = javax.microedition.lcdui.Image.createRGBImage(rotatedData, height, width, true);
		}
		else
		{
			tmpJ2MEImage = javax.microedition.lcdui.Image.createRGBImage(rotatedData, width, height, true);
		}
		
		rowData = null;
		rotatedData = null;
		
		int rotWidth = tmpJ2MEImage.getWidth();
		int rotHeight = tmpJ2MEImage.getHeight();
		rotatedData = new int[rotWidth * rotHeight];
		tmpJ2MEImage.getRGB(rotatedData, 0, rotWidth, 0, 0, rotWidth, rotHeight);
		tmpJ2MEImage = null;
		
		Bitmap rotatedBitmap = new Bitmap(rotWidth, rotHeight);
		rotatedBitmap.setARGB(rotatedData, 0, rotWidth, 0, 0, rotWidth, rotHeight);
		rotatedData = null;
		return rotatedBitmap;		
	}

	public static int findBestImgScale(int originalWidth, int originalHeight, int maxWidth, int maxHeight) {

		if(originalWidth < maxWidth && originalHeight < maxHeight) return 1; //image is smaller than the desidered size...no resize!


		double numeratorW = originalWidth;
		double denominatorW = maxWidth;
		int scaleW = (int)Tools.round((numeratorW / denominatorW));

		double numeratorH = originalHeight;
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
	public static Hashtable resizePhoto(byte[] data, String fileName, SendToBlogTask task, int width, int height) throws IOException {

		EncodedImage originalImage = EncodedImage.createEncodedImage(data, 0, -1);
		Hashtable content = new Hashtable(2);

		//init the hash table with no resized img data
		content.put("name", fileName);
		content.put("height", String.valueOf(originalImage.getHeight()));
		content.put("width", String.valueOf(originalImage.getWidth()));
		content.put("bits", data);
		content.put("type", originalImage.getMIMEType());
		//no resize is necessary
		if(originalImage.getWidth() <= width && originalImage.getWidth() <= height) {
			Log.trace("no resize required"+fileName);
			return content;
		}

		int type = originalImage.getImageType();

		//starting resize
		EncodedImage bestFit2 = resizeEncodedImage(originalImage, width, height);
		originalImage = null;
		Bitmap resizedBitmap = bestFit2.getBitmap();
		bestFit2 = null;

		byte[] imageBytes;
		switch (type) {

		case EncodedImage.IMAGE_TYPE_PNG:
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
				imageBytes = JPEGEncodedImage.encode(resizedBitmap, 75).getData();
				if (task != null && task.isStopped()) return null; //resizing img is a long task. if user has stoped the operation..
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
	 * Returns a PNG stored in a byte array from the supplied Bitmap.
	 *
	 * @param image   an Bitmap object
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
