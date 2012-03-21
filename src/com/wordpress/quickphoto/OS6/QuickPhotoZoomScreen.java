//#preprocess
package com.wordpress.quickphoto.OS6;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.extension.container.ZoomScreen;

//ZoomScreen that calls close() on itself when the zoom level is nearToFit and the user hit back.
public class QuickPhotoZoomScreen extends ZoomScreen {
	public QuickPhotoZoomScreen(EncodedImage encodedImage){
		super(encodedImage);
		//#ifdef BlackBerrySDK7.0.0
		// Initialize the zoom screen to be zoomed all the way out
		setViewableArea(0, 0, 0);
		//#endif    
	}
	/**
	 * @see ZoomScreen#zoomedOutNearToFit()
	 */ 
	public void zoomedOutNearToFit()
	{            
		close();
	}
}