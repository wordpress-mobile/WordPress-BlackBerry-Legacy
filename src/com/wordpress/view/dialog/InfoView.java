package com.wordpress.view.dialog;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.Dialog;

public class InfoView extends Dialog {

	/**
	 * Use this dialog boxes to provide feedback about a user action. Include only an OK button. 
	 * 
	 * @param message
	 */
	public InfoView(String message) {
		super(Dialog.D_OK, message, Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
	}	

}
