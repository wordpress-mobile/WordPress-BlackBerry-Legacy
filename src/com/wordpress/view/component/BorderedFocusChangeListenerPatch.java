package com.wordpress.view.component;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Manager;


/* (non-Javadoc)
 * This class is a patch for focus changes when using borderedFieldManager Class
 * 
 * Each field can have only one focus listener object. If you want to provide a new focus listener for a field, 
 * you must first invoke this method with null remove the old listener.
 * 
 * @see net.rim.device.api.ui.Field#setFocusListener
 */
public class BorderedFocusChangeListenerPatch implements FocusChangeListener {

	public void focusChanged(Field arg0, int arg1) {
		if(arg1 == FocusChangeListener.FOCUS_LOST) {
			Manager manager = arg0.getManager();
			if(manager != null) {
				manager.invalidate();
			}
		}
	}
}