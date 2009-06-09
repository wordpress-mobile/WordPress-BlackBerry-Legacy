package com.wordpress.utils.conn;

public class TcpConfig extends AbstractConfiguration {
	
	   /**TCP user's defined configurations description*/
    protected static final String TCP_CONFIG_DESCRIPTION = "User defined TCP Configuration";

	/**
	 * TCP Settings Class
	 */
	public TcpConfig() {
		super();
		
       setUrlParameters(BASE_CONFIG_PARAMETERS);
       setDescription(TCP_CONFIG_DESCRIPTION);
        //Grants automatically connections for specific TCP setting
        //manually set by the user in the Options -> Advanced Options -> TCP
        //device's configuration screen
       setPermission(PERMISSION_GRANTED);
	}
}