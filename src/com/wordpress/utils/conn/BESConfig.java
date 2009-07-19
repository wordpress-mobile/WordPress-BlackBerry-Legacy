package com.wordpress.utils.conn;

import com.wordpress.utils.log.Log;

public class BESConfig extends AbstractConfiguration {
	
    protected static final String CONFIG_DESCRIPTION = "BES Configuration";
	    
	public BESConfig() {
		super();
        Log.debug("Setting BES Config " + " " + CONFIG_DESCRIPTION);
        setUrlParameters(";deviceside=false");
        setDescription(CONFIG_DESCRIPTION);
	}
}
