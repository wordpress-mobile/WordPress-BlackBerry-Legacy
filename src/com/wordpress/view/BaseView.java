package com.wordpress.view;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;

/**
 * Base class for all Application Screen
 * @author dercoli
 *
 */
public abstract class BaseView extends MainScreen{
	
    public BaseView(String title) {
		super();
		LabelField titleField = getTitleField(title);
		this.setTitle(titleField);
	}
    
	public BaseView(String title, long style) {
		super(style);
		LabelField titleField = getTitleField(title);
		this.setTitle(titleField);
	}

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
    
    
    //return the controller associated with thi view
    public abstract BaseController getController();
    
    //create the title filed
	protected LabelField getTitleField(String title) {
		LabelField titleField = new LabelField(title, LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
		Font fnt = this.getFont().derive(Font.BOLD);
		titleField.setFont(fnt);
		return titleField;
	}
    
}