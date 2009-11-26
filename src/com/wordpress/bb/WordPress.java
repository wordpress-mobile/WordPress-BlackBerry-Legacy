//#preprocess
package com.wordpress.bb;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.controller.MainController;
import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Appender;
import com.wordpress.utils.log.BlackberryEventLogAppender;
import com.wordpress.utils.log.ConsoleAppender;
import com.wordpress.utils.log.Log;

public class WordPress extends UiApplication implements WordPressResource {

    //create a variable to store the ResourceBundle for localization support
    private static ResourceBundle _resources;
    private static Appender logAppender  = null;
    
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
		
		//init the log system
		Appender eventAppender = new BlackberryEventLogAppender("WordPress for BlackBerry");
		eventAppender.setLogLevel(Log.ERROR);
		eventAppender.open();
		Log.addAppender(eventAppender);
		
		//#ifdef LOG_CONSOLE
		Log.addAppender(new ConsoleAppender());
		//#endif

		Log.initLog(Log.TRACE);
     	 
		 //The following code specifies that the screen backlight, once activated,
		 //has a timeout period of 200 seconds.
		 if ((Display.getProperties() & Display.DISPLAY_PROPERTY_REQUIRES_BACKLIGHT) != 0) {
			Backlight.enable(true, 200);
		}
		 
	    // Create an instance of the ConnectionManager and register the GlobalEventListener
		ConnectionManager  _manager = ConnectionManager.getInstance();
	    this.addGlobalEventListener( _manager ); // Needed for ServiceBook parsing method 
		 
		 //start app
		new SplashScreen(this,mainScreen);
	}
}