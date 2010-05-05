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
 * A PopupScreen with a file browser allowing for file selection.
 */

public class BlogSelectorPopupScreen extends PopupScreen {

	ObjectListField _olf; 
	int selectedBlog = -1;

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

	public BlogSelectorPopupScreen(String[] blogNames) {
		super(new DialogFieldManager());
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setIcon(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.QUESTION)));
		dfm.setMessage(new RichTextField(_resources.getString(WordPressResource.TITLE_BLOG_SELECTOR_POPUP), Field.NON_FOCUSABLE ));
		
		_olf = new ObjectListField();
		dfm.addCustomField(_olf);
		
		// Update the field with blog names
		_olf.set(blogNames);
	}

	public void pickBlog() {
		UiApplication.getUiApplication().pushModalScreen(this);
	}


	public int getSelectedBlog() {
		return selectedBlog;
	}
	

	// Handles a user picking an entry in the ObjectListField.
	private void doSelection() {
		// Determine the current path.
		selectedBlog = _olf.getSelectedIndex();
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
			selectedBlog = -1;
			this.close();
			return true;
		} else if (c == Characters.ENTER) {
			doSelection();
			return true;
		}

		return super.keyChar(c, status, time);
	}
}