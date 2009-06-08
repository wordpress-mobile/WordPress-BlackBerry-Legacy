//#preprocess
package com.wordpress.bb;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.controller.MainController;
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
		//#ifdef DEBUG
		 Log.addAppender(new ConsoleAppender());
		//#endif
		 Log.initLog(Log.TRACE);
		
		 //start app
		new SplashScreen(this,mainScreen);
	}
}