package com.wordpress.utils.conn;


import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;


public class ConnectionManager {

	//singleton
	private static ConnectionManager instance = null;

	protected static final int MAX_CONFIG_NUMBER = 4;
	protected static final int CONFIG_NONE = -1;
	protected static final int WIFI_CONFIG = 0;
	protected static final int TCP_CONFIG = 1;
	protected static final int SERVICE_BOOK_CONFIG = 2;
	protected static final int BES_CONFIG = 3;
	private static AbstractConfiguration[] configurations = null;
	
	private Preferences userPreferences = Preferences.getIstance();
	protected static int currConfigID = CONFIG_NONE;
    
    
    private ConnectionManager() {
        configurations = initConfig(); 
    }

    //	singleton
    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public synchronized Connection open(String url) throws IOException {
        return open(url, Connector.READ_WRITE, true);
    }


    public synchronized Connection open(String url, int accessMode, boolean enableTimeoutException)
    throws IOException {

        //check user defined APN settings
        if (userPreferences.isUserConnectionOptionsEnabled()) {
        	String gtwUrl = "";
        	if(userPreferences.isUserConnectionWap()) { //if the user APN is WAP
	        	Gateway gtw = new Gateway(userPreferences.getApn(), userPreferences.getUsername(),
	        			userPreferences.getPassword(), userPreferences.getGateway(), null);
	        	
	        	gtw.setGatewayPort(userPreferences.getGatewayPort());
	        	gtw.setSourceIP(userPreferences.getSourceIP());
	        	gtw.setSourcePort(userPreferences.getSourcePort());
	        	
	        	gtwUrl = ConnectionUtils.buildWapConnectionString(gtw);
	
        	} else { //user APN is not wap
        		
        		gtwUrl =";apn="+userPreferences.getApn();
        		if (userPreferences.getUsername() != null) {
        			gtwUrl+=";tunnelauthusername="+userPreferences.getUsername();
        		}
        		if (userPreferences.getPassword() != null) {
        			gtwUrl+=";tunnelauthpassword=" + userPreferences.getPassword();
        		}
        		
        	}
        	Log.debug("User Wap Apn parameters detected for this connection: " + url + 
        			AbstractConfiguration.BASE_CONFIG_PARAMETERS + gtwUrl);
            return Connector.open(url + AbstractConfiguration.BASE_CONFIG_PARAMETERS + gtwUrl);
        } 
        
        //user hasn't personal conn settings. Load them from devices setting
        
        Log.debug("Reload configuration from device");
        refreshServiceBookConfigurations();

        if (!ConnectionUtils.isWifiActive() || !ConnectionUtils.isWifiAvailable()) {
            Log.debug("WI-FI not available");
            if (currConfigID == WIFI_CONFIG) {
                currConfigID = TCP_CONFIG;
            }
        }        
        
        if (currConfigID  > 0 && ConnectionUtils.isDataBearerOffline()) {
            if (ConnectionUtils.isWifiActive() && ConnectionUtils.isWifiAvailable()) {
                currConfigID=WIFI_CONFIG;
            } else {
                Log.debug("no suitable bearer were found");
                throw new IOException();
            }
        }
                
        if (currConfigID < 0) {
        
            return setupConnection(url, accessMode, enableTimeoutException);
        
        } else {
        	
            Connection ret = null;
            try {
                String fullUrl = url + configurations[currConfigID].getUrlParameters();
                Log.trace("Opening url: " + fullUrl);
                ret = Connector.open(fullUrl, accessMode, enableTimeoutException);
            } catch (Exception ioe) {
                currConfigID=CONFIG_NONE;
                closeConnection(ret);
                ret = setupConnection(url, accessMode, enableTimeoutException);
            }
            return ret;
        }
    }
    
    
    private Connection setupConnection(String url, int accessMode, boolean enableTimeoutException) throws IOException {

        Connection ret = null;
        String requestUrl = null;
        
        for (int i = 0; i < MAX_CONFIG_NUMBER; i++) {
            try {


                if (isConfigurationAllowed(i)) {
                    Log.debug("Configuration Allowed: " + (i+1));
                    currConfigID = i % configurations.length;
                    String options = configurations[i].getUrlParameters();
                    Log.debug("Using parameters: " + options);
                    requestUrl = url + options;
                } else {
                    Log.debug("Config " + (i+1) + " cannot be used.");
                    continue;
                }

                Log.debug("Connecting to: " + requestUrl);
                ret = Connector.open(requestUrl, accessMode, enableTimeoutException);
                
                return ret;
            } catch (Exception ioe) {
                Log.debug("setupConnection error " + ioe);
                closeConnection(ret);
                ret = null;
            }
        }

        if (ret != null) {
            return ret;
        } else { 
            currConfigID = CONFIG_NONE;
            throw new IOException("No route to blog");
        }
    }
    
    
    private void closeConnection(Connection conn) {
    	 // Close the connection in case it got opened
        if (conn != null) {
            try {
            	conn.close();
            } catch (Exception e) {
            	
            }
        }
    	
    }

    private boolean isConfigurationAllowed(int configNumber) {
        if (!isAvailable(configNumber)) {
            Log.debug("Connection not available");
            return false;
        }

        //Permission is denied
        if (configurations[configNumber].getPermission()== AbstractConfiguration.PERMISSION_DENIED){
            Log.debug("Connection denied");
            return false;
        }

        //Permission is granted
        if (configurations[configNumber].getPermission()== AbstractConfiguration.PERMISSION_GRANTED){
            Log.debug("Connection granted");
            return true;
        } 
        
        if (configurations[configNumber].getPermission() == AbstractConfiguration.PERMISSION_UNDEFINED){
            boolean isConfigurationAllowed = isConnectionConfigurationAllowed(configNumber,configurations[configNumber].getDescription());
            return isConfigurationAllowed;
        }
        
        return false;
    }
    
   
    /**
     * Check if the connection configuration is allowed
     * @param config is the configuration to be checked
     * @return true in the user allow the conn configuration  
     * performed on the configuration permission
     */
    public boolean isConnectionConfigurationAllowed(final int conType, final String config) {
    	Preferences pref = 	Preferences.getIstance();
    	
    	switch (conType) {
    	case WIFI_CONFIG: 
    		return (ConnectionUtils.isWifiActive()&& ConnectionUtils.isWifiAvailable()
    				&& pref.isWiFiConnectionPermitted() );
    	case TCP_CONFIG:
    		return pref.isTcpConnectionPermitted();
    	case SERVICE_BOOK_CONFIG:
    		return pref.isServiceBookConnectionPermitted();
    	case BES_CONFIG:
    		return pref.isBESConnectionPermitted();
    		
		default:
			return false;
		}
    }   
    
    public static boolean isAvailable(int configuration) {
        switch (configuration) {
            case WIFI_CONFIG:
                return (ConnectionUtils.isWifiActive()&&ConnectionUtils.isWifiAvailable());
            case TCP_CONFIG:
            case SERVICE_BOOK_CONFIG:
            case BES_CONFIG:
                return !ConnectionUtils.isDataBearerOffline();
            default:
                break;
        }
        return false;
    }
        
    
    static AbstractConfiguration[] initConfig() {
        configurations = new AbstractConfiguration[MAX_CONFIG_NUMBER];
        configurations[WIFI_CONFIG] = new WiFiConfig();
        configurations[TCP_CONFIG] = new TcpConfig();
        configurations[SERVICE_BOOK_CONFIG] = new ServiceBookConfig();
        configurations[SERVICE_BOOK_CONFIG].setUrlParameters(AbstractConfiguration.BASE_CONFIG_PARAMETERS + ConnectionUtils.getServiceBookOptions());
        configurations[BES_CONFIG] = new BESConfig();
        
        return configurations;
    }
    
    /**
     * Refresh the configuration parameters. Useful when the servicebook changed
     */
    protected static void refreshServiceBookConfigurations() {
        configurations[SERVICE_BOOK_CONFIG].setUrlParameters(AbstractConfiguration.BASE_CONFIG_PARAMETERS + 
                                                             ConnectionUtils.getServiceBookOptions());
    }
    
}


