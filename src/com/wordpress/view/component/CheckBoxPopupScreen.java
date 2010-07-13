package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.view.GUIFactory;

/**
 * A PopupScreen that uses a checkboxlistfield for item selection 
 */

public class CheckBoxPopupScreen extends PopupScreen {

	private CheckBoxListField checkBoxController = null;
    private ListField chkField;
    private boolean isSelectionSkipped = false;
    
	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

	public CheckBoxPopupScreen(String title, String[] items) {
		super(new DialogFieldManager());
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setIcon(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.QUESTION)));
		dfm.setMessage(new RichTextField(title, Field.NON_FOCUSABLE ));
		//the blogs list
		checkBoxController = new CheckBoxListField(items, new boolean[items.length]);
		this.chkField = checkBoxController.get_checkList();
		dfm.addCustomField(chkField);
		
		dfm.addCustomField(new LabelField("", Field.NON_FOCUSABLE)); //space after list
	      
        BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
        BaseButtonField buttonBACK= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
        buttonBACK.setChangeListener(listenerBackButton);
        buttonOK.setChangeListener(listenerOkButton);        
        HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
        buttonsManager.add(buttonOK);
		buttonsManager.add(buttonBACK);
		dfm.addCustomField(buttonsManager);
		dfm.addCustomField(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons
	}

	public void pickBlogs() {
		UiApplication.getUiApplication().pushModalScreen(this);
	}

	public boolean[] getSelectedBlogs() {
		if (isSelectionSkipped) {
			return new boolean[chkField.getSize()];
		} else {
			boolean[] selected = checkBoxController.getSelected();
			return selected;
		}
	}

	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	close();
	   }
	};

	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	isSelectionSkipped = true;
	    	close();
	   }
	};
	
	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			isSelectionSkipped = true;
			this.close();
			return true;
		}
		return super.keyChar(c, status, time);
	}
}