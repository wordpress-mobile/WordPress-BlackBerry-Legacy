package com.wordpress.view.dialog;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.Dialog;

public class InquiryView extends Dialog {

	/**
	 * Use this inquiry dialog boxes when users must confirm an action before continuing. 
	 * A question mark (?) indicator appears in an inquiry dialog box.
	 * 
	 * @param message
	 */
	public InquiryView(String message) {
		super(Dialog.D_YES_NO, message, Dialog.NO, Bitmap.getPredefinedBitmap(Bitmap.QUESTION), Dialog.GLOBAL_STATUS);
	}	
	
}
