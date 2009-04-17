package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.Dialog;

public class ErrorView extends Dialog {

	/**
	 * Use this dialog boxes to provide feedback about an Error. It include only an OK button. 
	 * 
	 * @param message
	 */
	public ErrorView(String message) {
		super(Dialog.D_OK, message, Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Dialog.GLOBAL_STATUS);
	}	

}
