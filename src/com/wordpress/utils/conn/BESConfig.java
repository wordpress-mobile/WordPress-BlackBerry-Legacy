package com.wordpress.utils.conn;


public class BESConfig extends AbstractConfiguration {
	
    protected static final String CONFIG_DESCRIPTION = "BES Configuration";
	    
	public BESConfig() {
		super();
        setUrlParameters(";deviceside=false");
        setDescription(CONFIG_DESCRIPTION);
	}
}
