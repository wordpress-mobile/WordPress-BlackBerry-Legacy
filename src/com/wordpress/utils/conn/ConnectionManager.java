package com.wordpress.utils.conn;


import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;

import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;

/**
 * Controls all of the connections requested by the API implementations. 
 * It is strongly recommended to use this class instead of the direct call to 
 * the Connector.open(String url) method. This class is based on the singleton 
 * pattern: it has private constructor and just one instance to be referenced 
 * calling the method ConnectionManager.getInstance() by other classes. The 
 * connection logic switch between all of the BlackberryConfiguration object 
 * returned by the Connection config class. The usage of the returned 
 * configuration is the following: 
 * <br>
 * </br>
 * 
 * <br>1. WIFI Network - just on the wifi capable devices, skipped if the wifi 
 * bearer is not present;
 * </br>
 * <br>
 * </br>
 * <br>2. Custom TCP configuration - Manually configured by the user
 * </br>
 * <br>
 * </br>
 * <br>3. APN gateway configuration - Carrier's Country based Configuration
 * </br>
 * <br>
 * </br>
 * <br>4. Blackberry ServiceBook configuration WAP Transport entry - Blackberry 
 * ServiceBook based Configuration
 * </br>
 * <br>
 * </br>
 * <br>NOTE 1: When a wifi network is no more available the system tries to 
 * configure a GPRS data connection in the same session.
 * </br>
 * <br>
 * </br>
 * <br>NOTE 2: When the Manually configured TCP configuration is cancelled or become
 * invalid (i.e.: an invalid APN is substituted to a valid one), the TCP 
 * configuration won't ever be used during the same session but the system will 
 * switch other provided configurations.
 * </br>
 * <br>
 * </br>
 * <br>Note 3:When a configuration that is not WIFI or TCP based is validated, that 
 * configuration will be used for the entire session.
 * </br>
 */

public class ConnectionManager {

	/**String representation of SMS connection*/
    private static final String SMS_URL = "sms://:";

    /**WORKING configurations ID*/
    protected static int workingConfigID = ConnectionConfig.CONFIG_NONE;

    /**Array with possible blackberry configuration*/
    private static AbstractConfiguration[] configurations = null;
    
    /**The unique instance of this class*/
    private static ConnectionManager instance = null;
    
    /**ConnectionListener*/
    private BasicConnectionListener connectionListener = new BasicConnectionListener();

    private String connectionParameters = null;
    
    private Preferences userPreferences = Preferences.getIstance();
    
    /**
     * Singleton realization: Private constructor
     * Use getInstance() method to access the public methods
     */
    private ConnectionManager() {
        configurations = ConnectionConfig.getBlackberryConfigurations();
    }

    /**
     * Singleton implementation:
     * @return the current instance of this class or a new instance if it the 
     * current instance is null
     */
    public static ConnectionManager getInstance() {
        if (instance == null) {
            Log.debug("[ConnectionManager.getInstance]Creating new connection manager");
            instance = new ConnectionManager();
            instance.setConnectionListener(new BasicConnectionListener());
        }
        return instance;
    }

    /**
     * Open up a connection to the given url with the default 
     * CLDC 1.0 API default values for the stream (READ_WRITE) and the value 
     * TRUE for the availability of InterruptedIOException to be thrown
     * @param url The URL for the connection
     * @return the connection url with the given parameters 
     * @throws java.io.IOException
     */
    public synchronized Connection open(String url) throws IOException {
        return open(url, Connector.READ_WRITE, true);
    }

    /**
     * Open up a connection to the given url with the given access accessMode and 
     * @param url The URL for the connection
     * @param accessMode the access accessMode that can be READ, WRITE, READ_WRITE
     * @param enableTimeoutException A flag to indicate that the caller wants to 
     * enable the throwing of InterruptedIOException when a connection timeout
     * occurs
     * @return Connection related to the given parameters
     * @throws java.io.IOException
     */
    public synchronized Connection open(String url, int accessMode, boolean enableTimeoutException)
    throws IOException {

        //A message connection is required. It needs no parameters
        if (url.indexOf(SMS_URL)>0) {
            return Connector.open(url);
        }
        
        if (this.connectionParameters!=null) {
            Log.debug("[ConnectionManager.open]Fixed Apn parameters detected for this connection: " + url + this.connectionParameters);
            return Connector.open(url + this.connectionParameters);
        }
        
        //check user wap apn settings
        if (userPreferences.isUserConnectionOptionsEnabled()) {
        	WapGateway gtw = new WapGateway(userPreferences.getApn(), userPreferences.getUsername(),
        			userPreferences.getPassword(), userPreferences.getGateway(), null);
        	gtw.setGatewayPort(userPreferences.getGatewayPort());
        	gtw.setSourceIP(userPreferences.getSourceIP());
        	gtw.setSourcePort(userPreferences.getSourcePort());
        	
        	String gtwUrl = ConnectionUtils.buildWapConnectionString(gtw);

            Log.debug("[ConnectionManager.open]User Apn parameters detected for this connection: " + url + 
            		 AbstractConfiguration.BASE_CONFIG_PARAMETERS + gtwUrl);
            return Connector.open(url + AbstractConfiguration.BASE_CONFIG_PARAMETERS + gtwUrl);
        } 
        
        //If the GPRS coverage was lost the ServiceBook could have changed
        //A refresh is needed
        Log.debug("[connectionManager.open]Refreshing the configuration");
        ConnectionConfig.refreshServiceBookConfigurations();

        //Just displays the network coverage
        //Log.trace("[connectionManager.open]Current network informations:\n"
        //          + BlackberryUtils.getNetworkCoverageReport());

        //Checks the wifi status and removes it if the bearer is offline and 
        //it is the current working configuration
        Log.trace("[ConnectionManager.open]Checking wifi status");
        //Denies the wifi usage permission if the bearer is not available or 
        //turned off
        if (!ConnectionUtils.isWifiActive()||!ConnectionUtils.isWifiAvailable()) {
            Log.debug("[ConnectionManager.checkWifiStatus]Wifi not available");
            //switch to the TCPConfiguration if the wifi network is not 
            //available and the last working configuration was set to WIFI
            if (workingConfigID==ConnectionConfig.WIFI_CONFIG) {
                workingConfigID++;//=ConnectionConfig.CONFIG_NONE; or TCP??
            }
        }        
        
        //if the GPRS bearer is not available and wifi is available, then it 
        //become the working id 
        if (workingConfigID > 0 && ConnectionUtils.isWapGprsDataBearerOffline()) {
            Log.debug("[connectionManager.open]GPRS bearer is offline: switching to wifi if available");
            if (ConnectionUtils.isWifiActive() && ConnectionUtils.isWifiAvailable()) {
                Log.debug("[connectionManager.open]WIFI bearer is online: changing working configuration");
                workingConfigID=ConnectionConfig.WIFI_CONFIG;
            } else {
                //Doesn't try the connection because in this case there is no 
                //bearer available.
                Log.debug("[connectionManager.open]WIFI bearer is offline: "
                          + "no suitable bearer were found. Throwing IOException");
                throw new IOException();
            }
        }
        
        if (workingConfigID < 0) {
            Log.debug("[ConnectionManager.open]Setting up the connection...");
            return setup(url, accessMode, enableTimeoutException);
        } else {
            Log.trace("[ConnectionManager.open]Working Configuration already assigned - ID="
                      + workingConfigID);
            Log.trace("[ConnectionManager.open]Using configuration: ID="
                      + workingConfigID + " - Description "
                      + configurations[workingConfigID].getDescription());
            Connection ret = null;
            try {
                Log.debug("[ConnectionManager.open]Opening connection with: "
                          + configurations[workingConfigID].getDescription());
                String fullUrl = url + configurations[workingConfigID].getUrlParameters();
                Log.trace("Opening url: " + fullUrl);
                ret = Connector.open(fullUrl, accessMode, enableTimeoutException);
            } catch (Exception ioe) {
                Log.debug("[ConnectionManager.open]Error occured while accessing " +
                          "the network with the last working configuration");
                // If the current configuration no longer works, we try all the
                // known connections to see if we find one that works.
                // Connections that have already been granted/denied do not
                // result in an extra prompt to the user, we rather keep his
                // previous answer.
                workingConfigID=ConnectionConfig.CONFIG_NONE;
                // Close the connection if it got opened
                if (ret != null) {
                    try {
                        ret.close();
                    } catch (Exception e) {
                        Log.debug("[ConnectionManager.setup]Failed Closing connection: " 
                                  + "setting up another configuration");
                        Log.debug("[ConnectionManager.setup]Exception: " + e);
                    }
                }
                // If all the possible connections are not working, the setup
                // will throw an exception resulting in an exception in the
                // "open".
                ret = setup(url, accessMode, enableTimeoutException);
            }
            return ret;
        }
    }
    
    
    /**
     * Add optional parameters (such as APN configurations or wifi interface) to 
     * the given url
     * @param url is the request url without configurations parameters
     * @return String representing the url with the optional parameter added
     */
    private Connection setup(String url, int accessMode, boolean enableTimeoutException)
    throws IOException {

        Connection ret = null;
        String requestUrl = null;
        for (int i = 0; i < ConnectionConfig.MAX_CONFIG_NUMBER; i++) {
            try {
                Log.debug("[ConnectionManager.setup]Looping configurations: "+ (i+1) + "/" + ConnectionConfig.MAX_CONFIG_NUMBER);
                //If the open operation fails a subclass of IOException is thrown by the system
                if (isConfigurationAllowed(i)) {
                    Log.debug("[ConnectionManager.setup]Configuration Allowed: " + (i+1));
                    workingConfigID = i % configurations.length;
                    String options = configurations[i].getUrlParameters();
                    Log.debug("[ConnectionManager.setup]Using parameters: " + options);
                    requestUrl = url + options;
                } else {
                    Log.debug("[ConnectionManager.setup] config " + (i+1) + " cannot be used.");
                    continue;
                }

                Log.debug("[ConnectionManager.setup]Connecting to: " + requestUrl);
                ret = Connector.open(requestUrl, accessMode, enableTimeoutException);
                //If the open call is succesfull it could be useful to notify 
                //the current working configuration changes to the listener
                connectionListener.connectionConfigurationChanged();
                Log.debug("[ConnectionManager.setup]Listener notified");
                return ret;
            } catch (Exception ioe) {
                Log.debug("[ConnectionManager.setup]Connection not opened at attempt " + (i + 1));
                Log.debug("[ConnectionManager.setup] " + ioe);

                // Close the connection in case it got opened
                if (ret != null) {
                    try {
                        ret.close();
                    } catch (Exception e) {
                        Log.debug("[ConnectionManager.setup]Failed Closing connection: trying next configuration if exists");
                        Log.debug("[ConnectionManager.setup]Exception: " + e);
                    }
                }
                ret = null;
            }
        }

        if (ret != null) {
            return ret;
        } else { 
            workingConfigID = ConnectionConfig.CONFIG_NONE;
            //Doesn't return a null connection but throws an IOException
            throw new IOException("[ConnectionManager.setup]Cannot find a suitable configuration");
        }
    }
    
    /**
     * Return the Availability of a configurations
     * @param configNumber the Configuration ID
     * @return boolean true if the configurations can be used, false otherwise
     */
    private boolean isConfigurationAllowed(int configNumber) {
        Log.debug("[ConnectionManager.isConfigurationAllowed]Config number: " + configNumber);
        if (!ConnectionConfig.isAvailable(configNumber)) {
            Log.debug("[ConnectionManager.isConfigurationAllowed]Connection not available");
            return false;
        }
        
        //Calculates the apn String
        Log.debug("[ConnectionManager.isConfigurationAllowed]Calculating APN");
        String apn = ConnectionConfig.getAPNFromConfig(configNumber);
        Log.debug("[ConnectionManager.isConfigurationAllowed]APN Found: " + apn);

        //Permission is denied
        if (configurations[configNumber].getPermission()== AbstractConfiguration.PERMISSION_DENIED){
            Log.debug("[ConnectionManager.isConfigurationAllowed]Permission DENIED for: " + apn);
            return false;
        }

        //Permission is granted
        if (configurations[configNumber].getPermission()== AbstractConfiguration.PERMISSION_GRANTED){
            Log.debug("[ConnectionManager.isConfigurationAllowed]Permission GRANTED for: " + apn);
            return true;
        } 

        //null check is performed because the apn is returned as null when the
        //mobile network is not available
        if (apn==null) {
            if (configNumber>ConnectionConfig.TCP_CONFIG) {
                Log.debug("[ConnectionManager.isConfigurationAllowed]Retrieved null APN: availability DENIED");
                return false;
            }
        }
        
        //Connection listener logic implemented for undefined permission when network is covered
        if (configurations[configNumber].getPermission()==AbstractConfiguration.PERMISSION_UNDEFINED){
            Log.debug("[ConnectionManager.isConfigurationAllowed]Permission not defined for: " + apn);
            boolean isConfigurationAllowed = connectionListener.isConnectionConfigurationAllowed(apn);
            if (isConfigurationAllowed) {
                Log.debug("[ConnectionManager.isConfigurationAllowed]Permission set to GRANTED");
                configurations[configNumber].setPermission(AbstractConfiguration.PERMISSION_GRANTED);
            } else {
                Log.debug("[ConnectionManager.isConfigurationAllowed]Permission set to DENIED");
                configurations[configNumber].setPermission(AbstractConfiguration.PERMISSION_DENIED);
            }
            return isConfigurationAllowed;
        }
        Log.debug("[ConnectionManager.isConfigurationAllowed]No suitable condition "
                  + "to allow the configuration (" + (configNumber+1) + ") was found");
        return false;
    }
    
    /**
     * Accessor method to set the connection listener 
     * @param cl the connection listener to be set
     */
    public void setConnectionListener(BasicConnectionListener cl) {
        this.connectionListener = cl;
    }

    /**
     * Accessor method to get the current connection listener 
     * @return ConnectionListener related to this ConnectionManager instance
     */
    public BasicConnectionListener getConnectionListener() {
        return connectionListener;
    }

    /**
     * Set the Connection parameters for this connection given the apn string
     * @param apn the fixed apn to be used.
     */
    public void setFixedApn(String apn) {
        if (apn!=null) {
            this.connectionParameters = ";deviceside=true;apn=" + apn + ";WapGatewayAPN=" + apn;
        } else {
            this.connectionParameters = null;
        }
    }

    /**
     * Disable the connection parameters for the fixed APN usage. Use this
     * method after a setConnectionPaarameters call in order to use the device 
     * apn instead of the fixed one
     */
    public void unsetFixedApn() {
        this.connectionParameters=null;
    }
}


