package com.wordpress.view.mm;

import net.rim.device.api.system.Bitmap;

/**
 * Added the property "path" to the standard BitmapField. Used to show a thumb of the photo.
 * @author dercoli
 *
 */
public class PhotoBitmapField  extends net.rim.device.api.ui.component.BitmapField {

	private String path=null; //the path of the image
	
	public String getPath() {
		return path;
	}

	public PhotoBitmapField(String path) {
		super();
		this.path=path;
		
	}

	public PhotoBitmapField(Bitmap arg0, long arg1, String path) {
		super(arg0, arg1);
		this.path=path;
	}

	public PhotoBitmapField(Bitmap arg0, String path) {
		super(arg0);
		this.path=path;
	}
	
}
