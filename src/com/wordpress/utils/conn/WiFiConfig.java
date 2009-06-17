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
		// Grants automatically wifi connections only if Wifi Bearer is present
		// on the device
	/*	if (ConnectionUtils.isWifiAvailable()) {
			setPermission(PERMISSION_GRANTED);
		} else {
			setPermission(PERMISSION_DENIED);
		} */
	}
}
