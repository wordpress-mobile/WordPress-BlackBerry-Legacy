package com.wordpress.view.dialog;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.Dialog;

public class ConnectionInProgressView extends Dialog {

	/**
	 * Use this dialog boxes to provide feedback about an connection in progress. It include only an Cancel button. 
	 * 
	 * @param message
	 */
	public ConnectionInProgressView(String message) {

		super(message,  new String [] { "Cancel" },   new int [] { Dialog.CANCEL }, 
				Dialog.CANCEL,   Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS));
	}
	
	
}
