package com.wordpress.utils.conn;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.InquiryView;

/**
 * the simplest connection handler ever.
 * 
 */
public class BasicConnectionListener {

	

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    
    }
	

	// a simple class used to show dialog sync over the main event Thread
	//a copy from BaseController...
	private class AskThread implements Runnable {
		int userResponse;
		final String config; 
		
		public AskThread(String msg){
			config = msg;
		}
		public void run() {
			InquiryView inqView= new InquiryView(config);
			userResponse = inqView.doModal();
		}
		public int getResponse (){
			return userResponse;
		}
	}
	
    /**
     * Check if the connection configuration is allowed
     * @param config is the configuration to be checked
     * @return true in the user allow the conn configuration  
     * performed on the configuration permission
     */
    public boolean isConnectionConfigurationAllowed(final String config) {
    	Log.debug("[BasicConnectionListener]Ask to user if the current config is allowed");

    	AskThread ask = new AskThread(_resources.getString(WordPressResource.MESSAGE_CHOOSE_CONNECTION) + " "+ config);
    	UiApplication.getUiApplication().invokeAndWait(ask); //get lock on main thread is required because we are on bg Thread
    	if (ask.getResponse() == Dialog.YES) {
    	//if (choose == Dialog.YES) {
    	  	Log.debug("[BasicConnectionListener]user response for current config: Allowed");
    		return true;
    	} else {
    		Log.debug("[BasicConnectionListener]user response for current config: NOT Allowed");
    		return false;
    	} 
    }

    /**
     * Notify that a connection was succesfully opened
     */
    public void connectionOpened() {
        Log.debug("[BasicConnectionListener]Connection Opened");
    }

    /**
     * Notify that a data request was succesfully written on the connection 
     * stream
     */
    public void requestWritten() {
        Log.debug("[BasicConnectionListener]Request written");
    }

    /**
     * Notify that a response was received after the request was sent
     */
    public void responseReceived() {
        Log.debug("[BasicConnectionListener]response received");
    }

    /**
     * Notify that a previously opened connection has been closed
     */
    public void connectionClosed() {
        Log.debug("[BasicConnectionListener]Connection closed");
    }

    public void connectionConfigurationChanged() {
        Log.debug("Connection Configuration changed");
    }
}