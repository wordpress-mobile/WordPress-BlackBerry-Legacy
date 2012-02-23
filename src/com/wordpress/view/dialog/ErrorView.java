package com.wordpress.view.dialog;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.DialogFieldManager;

public class ErrorView extends Dialog {

	/**
	 * Use this dialog boxes to provide feedback about an Error. It include only an OK button. 
	 * 
	 * @param message
	 */
	public ErrorView(String message) {
		super(Dialog.D_OK, message, Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Dialog.GLOBAL_STATUS);
	}


	public ErrorView(final String message, final Exception e) {
		super(Dialog.D_OK, message, Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Dialog.GLOBAL_STATUS);
		net.rim.device.api.ui.Manager delegate = getDelegate();
		if( delegate instanceof DialogFieldManager){
			DialogFieldManager dfm = (DialogFieldManager)delegate;
			net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
			if( manager != null && e != null ){
				ButtonField buttonField = new ButtonField("Copy error trace to clipboard");
				buttonField.setChangeListener(new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						// Retrieve the Clipboard object.
						Clipboard  cp = Clipboard.getClipboard();
						String errorMessageToCopy = null;
						if( e != null ) {
							if (e.getMessage()!= null)
								errorMessageToCopy= message + " - " + e.getMessage();
						}
						String message = "["+ e.getClass().getName() + "] " + errorMessageToCopy;

						// Copy to clipboard.
						cp.put( message );
						close();
					}
				});
				manager.insert(buttonField, manager.getFieldCount());
			}
		}
	}
}
