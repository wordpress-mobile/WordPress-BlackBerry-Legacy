package com.wordpress.utils.conn;

import net.rim.device.api.i18n.ResourceBundle;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;

public class ConnectionListener {

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    
    }

    /**
     * Check if the connection configuration is allowed
     * @param config is the configuration to be checked
     * @return true in the user allow the conn configuration  
     * performed on the configuration permission
     */
    public boolean isConnectionConfigurationAllowed(final int conType, final String config) {
    	Preferences pref = 	Preferences.getIstance();
    	//Log.debug("[BasicConnectionListener] check if connection is Allowed: " + config);
    	
    	switch (conType) {
    	case ConnectionConfig.WIFI_CONFIG: 
    		return (ConnectionUtils.isWifiActive()&& ConnectionUtils.isWifiAvailable()
    				&& pref.isWiFiConnectionPermitted() );
    	case ConnectionConfig.TCP_CONFIG:
    		return pref.isTcpConnectionPermitted();
    	case ConnectionConfig.APN_TABLE_CONFIG:
    		return pref.isWapConnectionPermitted();
    	case ConnectionConfig.SERVICE_BOOK_CONFIG:
    		return pref.isServiceBookConnectionPermitted();
    	case ConnectionConfig.BES_CONFIG:
    		return pref.isBESConnectionPermitted();
    		
		default:
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