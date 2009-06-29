package com.wordpress.utils.conn;


public class WiFiConfig extends AbstractConfiguration {
	
	/**WIFI configuration description*/
    protected static final String WIFI_CONFIG_DESCRIPTION = "Wifi Network";
    
    /**WIFI custom URL configuration parameters*/
    private static String WIFI_CONFIG_PARAMETERS = ";interface=wifi";
  
	public WiFiConfig() {
		super();
		setUrlParameters(WIFI_CONFIG_PARAMETERS);
		setDescription(WIFI_CONFIG_DESCRIPTION);
	}
}
