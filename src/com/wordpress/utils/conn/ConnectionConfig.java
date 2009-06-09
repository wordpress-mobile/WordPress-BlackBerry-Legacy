package com.wordpress.utils.conn;


import java.util.Hashtable;

import com.wordpress.utils.log.Log;

/**
 * Connection Configurations repository: this class is needed to configure the 
 * connections using the carrier parameters in order to access the network.
 * Configurations are useful both for socket and Http connections. 
 * It should be suitable that every class that perform a call to the open method 
 * of the javax.microedition.io.Connector class use this class in oder to have 
 * working configurations loaded when the application runs. This class is mostly 
 * used into the Blackberry implementation as the device doesn't allow simple 
 * URL request to be sent on the network without APN or interface parameters. 
 * For more information about this topic see the Blackberry developers guide.
 */
public class ConnectionConfig {
	
	/**Maximum configurations number*/
    protected static final int MAX_CONFIG_NUMBER = 5;
    /**-1: No config has been set*/
    protected static final int CONFIG_NONE = -1;
    /**0: Wifi configurations index*/
    protected static final int WIFI_CONFIG = 0;
    /**1: TCP user's defined configurations index*/
    protected static final int TCP_CONFIG = 1;
    /**2: Apn table - APNGateway class defined - configurations index*/
    protected static final int APN_TABLE_CONFIG = 2;
    /**3: Service book configurations index*/
    protected static final int SERVICE_BOOK_CONFIG = 3;
    /**4: BES configuration index */
    protected static final int BES_CONFIG = 4;

    
    /**US Country selector*/
    private static final String COUNTRY_US = "US";
    /**IT country selector*/
    private static final String COUNTRY_IT = "IT";
    /**DE country selector*/
    private static final String COUNTRY_DE = "DE";
    /**BlackberryConfiguration array*/
    private static AbstractConfiguration[] configurations =  null;
 
    /**Hashtable used to store the APN list*/
    private static Hashtable apnTable = new Hashtable();
    
    static {
        //Statically fill the ApnTable hashtable
        initApnTable();
        //Statically fill all of the available BlackberryConfigurations
        init();
    }

    /**
     * Accessor Method to get the configuration availability run-time
     * @return true if the device bearer 
     */
    public static boolean isAvailable(int configuration) {
        switch (configuration) {
            case WIFI_CONFIG:
                return (ConnectionUtils.isWifiActive()&&ConnectionUtils.isWifiAvailable());
            case TCP_CONFIG:
            case APN_TABLE_CONFIG:
            case SERVICE_BOOK_CONFIG:
            case BES_CONFIG:
                return !ConnectionUtils.isWapGprsDataBearerOffline();
            default:
                break;
        }
        return false;
    }
    
    
    /**
     * Get the available configurations array
     * @return BlackberryConfiguration[] representing the array with the 
     * available configurations
     */
    protected static AbstractConfiguration[] getBlackberryConfigurations() {
        //It is better to perform this operation because some VM 
        //implementation could have a different behavior for static addressed 
        //classes in particular it could happen that the configuration 
        //permission would not be initialized when an application starts, but 
        //only when the application is installed the first time
        //The same call of static the block that Statically filled the ApnTable 
        //hashtable
        //initApnTable();
        //The same call of static the block that Statically filled all of the 
        //available BlackberryConfigurations
        //init();
        //Return the initialized BlackberryConfiguration array
        Log.debug("[ConnectionConfig.getBlackberryConfigurations]Returning Configurations");
        Log.debug(getConfigsDescription());
        Log.debug(getWorkingConfigurationDescription());
        return configurations;
    }

    /**
     * Check if the data connection provider is US based
     * @return true if an US carrier APN is found in the address book
     */
    protected static boolean isUSCarrier() {
        Log.debug("[ConnectionConfig.isUSCarrier] Checking if we're in US");
        String[] serviceBookApn = ConnectionUtils.getAllActiveServiceBookAPNs();

        boolean isUsCountry = false;
        if (serviceBookApn != null) {
            Log.trace("[ConnectionConfig.isUSCarrier]ServiceBook APN is not null");
            WapGateway gateway = findGatewayByApn(serviceBookApn);
            if (gateway != null) {
                Log.trace("[ConnectionConfig.isUSCarrier]Gateway is not null");
                Log.trace("[ConnectionConfig.isUSCarrier]APN: " + gateway.getApn());
                Log.trace("[ConnectionConfig.isUSCarrier]Username: " + gateway.getUsername());
                Log.trace("[ConnectionConfig.isUSCarrier]Password: " + gateway.getPassword());
                Log.trace("[ConnectionConfig.isUSCarrier]Country: " + gateway.getCountry());
                //if in us return false
                isUsCountry = COUNTRY_US.equals(gateway.getCountry());
                Log.trace("[ConnectionConfig.isUSCarrier]US Country check: " + isUsCountry);
                return isUsCountry;
            } else {
                Log.trace("[ConnectionConfig.isUSCarrier]Gateway is NULL");
            }
        } else {
            Log.trace("[ConnectionConfig.isUSCarrier]ServiceBook APN is NULL");
        }
        Log.debug("[ConnectionConfig.isUSCarrier]Final value returned by: " + isUsCountry + " for " + serviceBookApn);
        return isUsCountry;
    }

    /**
     * Look up the apntable to find the right wapgateway apnList, 
     * username and password to use with current network. 
     * @return String formatted APN and authentication related parameters to be 
     * added on the request url
     */
    private static String getAPNGatewayOptions() {
        StringBuffer options = new StringBuffer("");
        String[] serviceBookApn = ConnectionUtils.getAllActiveServiceBookAPNs();
        Log.debug("[ConnectionConfig]Trying to find gateway for APN: " + serviceBookApn);

        if (serviceBookApn != null) {
            WapGateway gateway = findGatewayByApn(serviceBookApn);
            if (gateway != null) {
                //We matched with a gateway in our list. Build connection options
                options.append(";apn=" + gateway.getApn());
                options.append(";WapGatewayAPN=" + gateway.getApn());
                if (gateway.getUsername() != null) {
                    options.append(";TunnelAuthUsername=" + gateway.getUsername());
                }
                if (gateway.getPassword() != null) {
                    options.append(";TunnelAuthPassword=" + gateway.getPassword());
                }
                if (gateway.getGatewayIP() != null) {
                    options.append(";WapGatewayIP=" + gateway.getGatewayIP());
                }
            }
        }
        return options.toString();
    }

    /**
     * Retrieves the APN for the given configuration
     * @param configId the configuration index
     * @return the String formatted APN
     */
    protected static String getAPNFromConfig(int configId) {
        if (configId == WIFI_CONFIG) {
            Log.debug("[ConnectionManager.isConfigurationAllowed]Returning wifi configuration description");
            return WiFiConfig.WIFI_CONFIG_DESCRIPTION;
        }

        if (configId == TCP_CONFIG) {
            Log.debug("[ConnectionManager.isConfigurationAllowed]Returning TCP configuration description");
            return TcpConfig.TCP_CONFIG_DESCRIPTION;
        }

        if (configId == SERVICE_BOOK_CONFIG) {
            Log.debug("[ConnectionManager.isConfigurationAllowed]Returning Service book configuration");
            return ConnectionUtils.getServiceBookWapTransportApn();
        }

        if (configId == BES_CONFIG) {
            Log.debug("[ConnectionManager.isConfigurationAllowed]Returning BES configuration description");
            return BESConfig.BES_CONFIG_DESCRIPTION;
        }

        // looking for apnList
        String sep = ";apn=";
        String config = configurations[configId].getUrlParameters();
        Log.debug("[ConnectionConfig]Configuration found: " + config);
        int start = config.indexOf(sep);
        if (start == -1) {
            Log.debug("[ConnectionConfig]APN not found: Returning NULL");
            return null;
        }
        start += sep.length();
        int stop = config.substring(start).indexOf(";");
        Log.debug("[ConnectionConfig]start= " + start + " stop= " + stop);


        if (stop == -1) {
            return config.substring(start);
        } else {
            return config.substring(start, start + stop);
        }
    }

    /**
     * Look into the apntable to find the correct gateway given the apnList.
     * A gateway is composed by apnList, username and password.
     * @param apnList the apnList to look for
     * @return the correct gateway object. Note that given an apnList, tha gateway may
     * use a different apnList, e.g. given wap.tim.it the returned gateway has 
     * ibox.tim.it as apnList, this is due to the fact that some apns does not allow
     * tcp communications
     */
    private static WapGateway findGatewayByApn(String[] apnList) {
        for (int i = 0; i < apnList.length; i++) {
            if (apnList[i] != null) {
                WapGateway ret = (WapGateway) apnTable.get(apnList[i].toLowerCase());
                if (ret != null) {
                    Log.debug("[ConnectionConfig]ApnTable returned " + ret.getApn() + " for apn " + apnList[i]);
                    return ret;
                } else {
                    Log.debug("[ConnectionConfig]ApnTable found no entry for apn " + apnList[i]);
                }
            }
        }
        Log.info("[ConnectionConfig] apnTable entry not found... returning null");
        return null;
    }
    
    /**
     * Initializes the ApnTable hashtable with the predefined values
     */
    private static void initApnTable() {
        // building APN table
        Log.debug("[ConnectionConfig] creating apn table...");

        //-------------------------
        //------ US OPERATORS -----
        //-------------------------
        
        //-------------------------
        //  ATT Orange (Cingular)
        //-------------------------
        apnTable.put("wap.cingular",
                new WapGateway("wap.cingular", "WAP@CINGULARGPRS.COM", "CINGULAR1", COUNTRY_US));
        //-------------------------
        //T-Mobile US1
        //-------------------------
        apnTable.put("internet2.voicestream.com",
                new WapGateway("internet2.voicestream.com", null, null, COUNTRY_US));
        //-------------------------
        //T-Mobile US2
        //-------------------------
        apnTable.put("wap.voicestream.com",
                new WapGateway("wap.voicestream.com", null, null, COUNTRY_US));
        //-------------------------
        //Sprint: 
        // NOTE: should be internet.com but there could be other parameters
        //-------------------------
        apnTable.put("internet.com",
                new WapGateway("internet.com", null, null, COUNTRY_US));

        //-------------------------
        //Verizon: should work with no apnList
        //-------------------------
        
        //-------------------------
        // ----  IT OPERATORS ---- //
        //-------------------------
        
        //-------------------------
        //Tim, both gprs and wap
        //-------------------------
        WapGateway tim = new WapGateway("ibox.tim.it", null, null, COUNTRY_IT);
        apnTable.put("ibox.tim.it", tim);
        apnTable.put("wap.tim.it", tim);
        
        //-------------------------
        // Wind
        //-------------------------
        WapGateway wind = new WapGateway("internet.wind", null, null, COUNTRY_IT);
        apnTable.put("internet.wind", wind);
        apnTable.put("internet.wind.biz", wind);
        apnTable.put("wap.wind.biz", wind);
        
        //-------------------------
        // Omnitel (Vodafone IT)
        //-------------------------
        WapGateway omni = new WapGateway("web.omnitel.it", null, null, COUNTRY_IT);
        apnTable.put("web.omnitel.it", omni);
        apnTable.put("wap.omnitel.it", omni);

        //-------------------------
        // ----  German OPERATORS ---- //
        //-------------------------
        
        //-------------------------
        // Vodafone DE, wap
        //-------------------------
        WapGateway vodafoneDe = new WapGateway("wap.vodafone.de", null, null,
                                               "139.7.29.1", COUNTRY_DE);
        apnTable.put("wap.vodafone.de", vodafoneDe);

        Log.debug("[ConnectionConfig] apntable created");
    }

    /**
     * Initializes the static BlackBerryConfigurations array
     */
    private static void init() {

        Log.info("[ConnectionConfig.init] init configs");

        ///////////////////////////////////////////////////////
        // Initialize the configurations arrays
        ///////////////////////////////////////////////////////
        configurations = new AbstractConfiguration[MAX_CONFIG_NUMBER];

        configurations[WIFI_CONFIG] = new WiFiConfig();
        configurations[TCP_CONFIG] = new TcpConfig();

        //Custom APNGateway table defined into the static block
        configurations[APN_TABLE_CONFIG] = new CustomAPNConfig();
        configurations[APN_TABLE_CONFIG].setUrlParameters(AbstractConfiguration.BASE_CONFIG_PARAMETERS + getAPNGatewayOptions());
       
        //Device's ServiceBook content related configurations 
        configurations[SERVICE_BOOK_CONFIG] = new ServiceBookConfig();
        configurations[SERVICE_BOOK_CONFIG].setUrlParameters(AbstractConfiguration.BASE_CONFIG_PARAMETERS + ConnectionUtils.getServiceBookOptions());
        //BES Settings
        configurations[BES_CONFIG] = new BESConfig();
        
        Log.info("[ConnectionConfig] Configs created");
    }
    
    /**
     * Refresh the configuration parameters. Useful when the servicebook changed
     */
    protected static void refreshServiceBookConfigurations() {
        configurations[APN_TABLE_CONFIG].setUrlParameters(AbstractConfiguration.BASE_CONFIG_PARAMETERS + getAPNGatewayOptions());
        configurations[SERVICE_BOOK_CONFIG].setUrlParameters(AbstractConfiguration.BASE_CONFIG_PARAMETERS +
                                                             ConnectionUtils.getServiceBookOptions());
    }
    
    /**
     * Get the description related to the given configurations index
     * @param configId the configurations index
     * @return String description for the given configurations index
     */
    protected static String getConfigurationDescription(int configId) {
        return configurations[configId].getDescription();
    }
    
    /**
     * Get the configurations description
     * @return a string describing the current configurations descriptions
     */
    public static String getConfigsDescription() {
        StringBuffer configs = new StringBuffer("Configurations:");

        for (int i = 0; i < configurations.length; i++) {
            configs.append("\n[" + i + "] " + configurations[i].getUrlParameters());
        }

        return configs.toString();
    }

    /**
     * Accessor method that return the current working configurations description
     * @return String related to the current working configurations description
     */
    public static String getWorkingConfigurationDescription() {
        if (ConnectionManager.workingConfigID == ConnectionConfig.CONFIG_NONE) {
            return AbstractConfiguration.CONFIG_NONE_DESCRIPTION;
        } else {
            return configurations[ConnectionManager.workingConfigID].getDescription();
        }
    }
    
    /**
     * Check if user must allow the APN usage. In US countries this check 
     * returns false 
     * @return false if the country retrieved by the service book is US, true 
     * otherwise
     */
    public static boolean isUserConfirmationNeeded() {
        Log.debug("[ConnectionManager.isUserConfirmationNeeded]User Confirmation needed value is: "
                  + !ConnectionConfig.isUSCarrier());
        return !isUSCarrier();
    }
    
    /**
     * Remove the saved working configurations ID. This method is used when 
     * we're unable to connect, to avoid trying the same configurations every 
     * time.
     */
    public static void removeSavedConfig() {
        Log.debug("[ConnectionManager.removeSavedConfig]Removing saved config (" +
                  ConnectionManager.workingConfigID + ") " + getWorkingConfigurationDescription());
        ConnectionManager.workingConfigID = ConnectionConfig.CONFIG_NONE;
    }

    /**
	 * Reset all of the configurations' permission to the initial state value
	 */
	public static void resetConfigurationsPermissions() {
	    //Reset WIFI permission checking the WIFI bearer presence and working 
	    //state
	    if (ConnectionUtils.isWifiAvailable()) {
	        configurations[ConnectionConfig.WIFI_CONFIG].setPermission(AbstractConfiguration.PERMISSION_GRANTED);
	    } else {
	        configurations[ConnectionConfig.WIFI_CONFIG].setPermission(AbstractConfiguration.PERMISSION_DENIED);
	    }
	
	    //Reset TCP Permission to granted
	    configurations[ConnectionConfig.TCP_CONFIG].setPermission(AbstractConfiguration.PERMISSION_GRANTED);
	
	    //Reset the generated APN table permission to undefined
	    configurations[ConnectionConfig.APN_TABLE_CONFIG].setPermission(AbstractConfiguration.PERMISSION_UNDEFINED);
	
	    //Reset the Service Book usage permission to undefined
	    configurations[ConnectionConfig.SERVICE_BOOK_CONFIG].setPermission(AbstractConfiguration.PERMISSION_UNDEFINED);
	
	    //Reset the BES permission to granted
	    configurations[ConnectionConfig.BES_CONFIG].setPermission(AbstractConfiguration.PERMISSION_GRANTED);
	}

}
