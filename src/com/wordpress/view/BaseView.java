package com.wordpress.view;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressResource;

/**
 * Base class for all Application Screen
 * @author dercoli
 *
 */
public abstract class BaseView extends MainScreen{
	
    public BaseView() {
		super();
	
	}
	public BaseView(long style) {
		super(style);
	}

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
    
    //return the resource bundle associated with this view
    public ResourceBundle getAssociatedResourceBundle(){
    	return _resources;
    }
}