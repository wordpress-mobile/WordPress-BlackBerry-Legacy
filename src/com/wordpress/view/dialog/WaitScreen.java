
package com.wordpress.view.dialog;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

public class WaitScreen extends  PopupScreen {

	/**
	 * Use this dialog boxes to provide 
	 * 
	 * @param message
	 */
	public WaitScreen(String message) {
		super(new DialogFieldManager());
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setIcon(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.HOURGLASS)));
		dfm.setMessage(new RichTextField(message));
	}
	
	
	// Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		return true;
	}

	protected boolean keyChar(char c, int status, int time) {
		return true;
	}
	
}
