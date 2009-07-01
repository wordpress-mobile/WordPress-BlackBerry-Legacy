package com.wordpress.utils.conn;

public class TcpConfig extends AbstractConfiguration {
	
    protected static final String CONFIG_DESCRIPTION = "User defined TCP Configuration";

	public TcpConfig() {
		super();	
       setUrlParameters(BASE_CONFIG_PARAMETERS);
       setDescription(CONFIG_DESCRIPTION);
	}
}