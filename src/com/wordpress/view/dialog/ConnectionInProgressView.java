package com.wordpress.view.dialog;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.DialogFieldManager;

import com.wordpress.view.component.AnimatedGIFField;

public class ConnectionInProgressView extends Dialog {

	/**
	 * Use this dialog boxes to provide feedback about an connection in progress. It include only an Cancel button. 
	 * 
	 * @param message
	 */
	public ConnectionInProgressView(String message) {

		super(message,  new String [] { "Cancel" },   new int [] { Dialog.CANCEL }, 
				Dialog.CANCEL,   Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS));
		
		GIFEncodedImage _theImage= (GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-gif.bin");
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setIcon(new AnimatedGIFField(_theImage));
	}
}
