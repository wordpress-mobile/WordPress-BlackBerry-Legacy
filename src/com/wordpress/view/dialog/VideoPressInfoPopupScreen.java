package com.wordpress.view.dialog;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.Tools;
import com.wordpress.view.GUIFactory;

public class VideoPressInfoPopupScreen extends PopupScreen {

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

	public VideoPressInfoPopupScreen() {
		super(new DialogFieldManager());
		String[] messages = _resources.getStringArray(WordPressResource.MESSAGE_VIDEOPRESS_UPGRADE);

		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setIcon(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.INFORMATION)));
		dfm.setMessage(new RichTextField(messages[0], Field.NON_FOCUSABLE));
		
		LabelField createURLLabelField = GUIFactory.createURLLabelField(messages[1],
					"http://videopress.com", LabelField.FOCUSABLE);
		 FieldChangeListener listener = new FieldChangeListener() {
			 public void fieldChanged(Field field, int context) {
				 Tools.openURL("http://videopress.com");
				 VideoPressInfoPopupScreen.this.close();
			 }
		 };
		 createURLLabelField.setChangeListener(null);
		 createURLLabelField.setChangeListener(listener);
		 
		dfm.addCustomField(createURLLabelField);
	}

	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			this.close();
			return true;
		} 
		return super.keyChar(c, status, time);
	}
}