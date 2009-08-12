package com.wordpress.utils.conn;


import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.wordpress.model.Preferences;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;


public class ConnectionManager {

	//singleton
	private static ConnectionManager instance = null;

	protected static final int MAX_CONFIG_NUMBER = 5;
	protected static final int CONFIG_NONE = -1;
	protected static final int WIFI_CONFIG = 0;
	protected static final int TCP_CONFIG = 1;
	protected static final int SERVICE_BOOK_CONFIG = 2;
	protected static final int BES_CONFIG = 3;
	protected static final int BIS_CONFIG = 4;
	private static AbstractConfiguration[] connections = null;
	
	private Preferences userPreferences = Preferences.getIstance();
	protected static int currConfigID = CONFIG_NONE;
    
    
    private ConnectionManager() {
        connections = initConnectionType(); 
    }

    //	singleton
    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public synchronized Connection open(String url) throws IOException {
    	//TODO inserire il controllo della connessione BIS
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

        	Connection ret = Connector.open(url + AbstractConfiguration.BASE_CONFIG_PARAMETERS + gtwUrl);
        	setCommonRequestProperty(ret);
            return ret;
        } 
        
        //user hasn't personal conn settings. Load them from devices setting
        
        Log.debug("Reload configuration from device");
        connections[SERVICE_BOOK_CONFIG].setUrlParameters(AbstractConfiguration.BASE_CONFIG_PARAMETERS + 
                ConnectionUtils.getServiceBookOptionsNew());

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

        	//check if the current config is already allowed
        	if (!isConnectionAllowed(currConfigID)) {
        		currConfigID=CONFIG_NONE;
        		return setupConnection(url, accessMode, enableTimeoutException);
        	}
        	
            Connection ret = null;
            try {
                String fullUrl = url + connections[currConfigID].getUrlParameters();
                Log.trace("Opening url: " + fullUrl);
                ret = Connector.open(fullUrl, accessMode, enableTimeoutException);
            	setCommonRequestProperty(ret);
                return ret;
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

                if (isConnectionAllowed(i)) {
                    Log.debug("Configuration Allowed: " + (i+1));
                    currConfigID = i % connections.length;
                    String options = connections[i].getUrlParameters();
                    Log.debug("Using parameters: " + options);
                    requestUrl = url + options;
                } else {
                    Log.debug("Config " + (i+1) + " cannot be used.");
                    continue;
                }

                Log.debug("Connecting to: " + requestUrl);
                ret = Connector.open(requestUrl, accessMode, enableTimeoutException);
            	setCommonRequestProperty(ret);
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
    
    /**
     * Setting the common http connection properties
     * @param conn
     * @throws IOException
     */
    private void setCommonRequestProperty(Connection conn) throws IOException {
    	try {
    		if(conn instanceof HttpConnection) {
    			HttpConnection connCasted = (HttpConnection) conn;
    			connCasted.setRequestProperty("User-Agent","wp-blackberry/"+ Tools.getAppVersion());
    			Log.trace("common http request properties setted");
    		}
    	} catch (IOException e) {
    		Log.error("Cannot set http request common properties!");
    		throw e;
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

    
    private boolean isConnectionAllowed(int configNumber) {
        if (!isAvailable(configNumber)) {
            Log.debug("Connection not available");
            return false;
        }

    	switch (configNumber) {
    	case WIFI_CONFIG: 
    		return (ConnectionUtils.isWifiActive()&& ConnectionUtils.isWifiAvailable()
    				&& userPreferences.isWiFiConnectionPermitted() );
    	case TCP_CONFIG:
    		return userPreferences.isTcpConnectionPermitted();
    	case SERVICE_BOOK_CONFIG:
    		return userPreferences.isServiceBookConnectionPermitted();
    	case BES_CONFIG:
    		return userPreferences.isBESConnectionPermitted();
    	case BIS_CONFIG:
    		return userPreferences.isBlackBerryInternetServicePermitted();
		default:
			return false;
		}
    }
    
    public static boolean isAvailable(int configuration) {
        switch (configuration) {
            case WIFI_CONFIG:
                return (ConnectionUtils.isWifiActive() && ConnectionUtils.isWifiAvailable());
            case TCP_CONFIG:
            case BES_CONFIG:
            case BIS_CONFIG:
                return !ConnectionUtils.isDataBearerOffline();
            case SERVICE_BOOK_CONFIG:
            	return (!ConnectionUtils.isDataBearerOffline() 
            			&&  !connections[SERVICE_BOOK_CONFIG].getUrlParameters().trim().equals(AbstractConfiguration.BASE_CONFIG_PARAMETERS));
            default:
                break;
        }
        return false;
    }
        
    
    static AbstractConfiguration[] initConnectionType() {
        connections = new AbstractConfiguration[MAX_CONFIG_NUMBER];
        connections[WIFI_CONFIG] = new WiFiConfig();
        connections[TCP_CONFIG] = new TcpConfig();
        connections[SERVICE_BOOK_CONFIG] = new ServiceBookConfig();
        connections[SERVICE_BOOK_CONFIG].setUrlParameters(AbstractConfiguration.BASE_CONFIG_PARAMETERS + ConnectionUtils.getServiceBookOptionsNew());
        connections[BES_CONFIG] = new BESConfig();
        connections[BIS_CONFIG] = new BISConfig();
        
        return connections;
    }
}


