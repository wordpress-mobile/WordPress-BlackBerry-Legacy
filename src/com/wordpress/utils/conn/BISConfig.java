package com.wordpress.utils.conn;


public class BISConfig extends AbstractConfiguration {
	
    protected static final String CONFIG_DESCRIPTION = "BIS Configuration";
	    
	public BISConfig() {
		super();
        setUrlParameters(";deviceside=false;ConnectionType=mds-public");
        setDescription(CONFIG_DESCRIPTION);
	}
}
