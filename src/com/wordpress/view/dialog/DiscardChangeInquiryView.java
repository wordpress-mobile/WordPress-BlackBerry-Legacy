package com.wordpress.view.dialog;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.Dialog;

public class DiscardChangeInquiryView extends Dialog {

	/**
	 * Use this inquiry dialog boxes (YES/CANCEL/DISCARD) when users must confirm an action before continuing. 
	 * A question mark (?) indicator appears in an inquiry dialog box.
	 * 
	 * @param message
	 */
	public DiscardChangeInquiryView(String message) {
		super(Dialog.D_SAVE, message, Dialog.CANCEL, Bitmap.getPredefinedBitmap(Bitmap.QUESTION), Dialog.GLOBAL_STATUS);
	}	
	
}
