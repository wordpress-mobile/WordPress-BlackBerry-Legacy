package com.wordpress.utils.conn;

public class CustomAPNConfig extends AbstractConfiguration {
	
	   /** Apn table - APNGateway class defined - configurations description*/
    protected static final String APN_TABLE_CONFIG_DESCRIPTION = "Client APN table defined configuration";
 
	/**
	 * Custom APNGateway table defined into the static block
	 */
	public CustomAPNConfig() {
		super();
        setDescription(APN_TABLE_CONFIG_DESCRIPTION);
	}
}
