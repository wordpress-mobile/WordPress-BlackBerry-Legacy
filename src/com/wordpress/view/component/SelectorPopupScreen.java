package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

import com.wordpress.bb.WordPressResource;

/**
 * A PopupScreen for selection.
 */

public class SelectorPopupScreen extends PopupScreen {

	ObjectListField _olf; 
	int selectedItem = -1;

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

	public SelectorPopupScreen(String title, String[] items) {
		super(new DialogFieldManager());
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setIcon(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.QUESTION)));
		dfm.setMessage(new RichTextField(title, Field.NON_FOCUSABLE ));
		
		_olf = new ObjectListField();
		dfm.addCustomField(_olf);
		
		_olf.set(items);
	}

	public void pickItem() {
		UiApplication.getUiApplication().pushModalScreen(this);
	}


	public int getSelectedItem() {
		return selectedItem;
	}
	

	// Handles a user picking an entry in the ObjectListField.
	private void doSelection() {
		selectedItem = _olf.getSelectedIndex();
		this.close();
	}

	// Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		doSelection();
		return true;
	}

	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			selectedItem = -1;
			this.close();
			return true;
		} else if (c == Characters.ENTER) {
			doSelection();
			return true;
		}

		return super.keyChar(c, status, time);
	}
}