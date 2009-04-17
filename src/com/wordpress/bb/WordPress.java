package com.wordpress.bb;

import com.wordpress.controller.MainController;
import com.wordpress.view.MainView;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.UiApplication;

public class WordPress extends UiApplication implements WordPressResource {

    //create a variable to store the ResourceBundle for localization support
    private static ResourceBundle _resources;
	
    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
	
	public static void main(String[] args) {
            WordPress app = new WordPress();
            app.enterEventDispatcher();
    }
		
    public WordPress(){
        	MainController mainScreen = new MainController();
            new SplashScreen(this,mainScreen);
    }
}