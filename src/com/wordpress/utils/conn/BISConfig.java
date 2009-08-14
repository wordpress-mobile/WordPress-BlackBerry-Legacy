package com.wordpress.utils.conn;

import com.wordpress.utils.log.Log;

public class BISConfig extends AbstractConfiguration {
	
    protected static final String CONFIG_DESCRIPTION = "BIS Configuration";
	    
	public BISConfig() {
		super();
        Log.debug("Setting BIS Config " + " " + CONFIG_DESCRIPTION);
        setUrlParameters(";deviceside=false;ConnectionType=mds-public");
        setDescription(CONFIG_DESCRIPTION);
	}
}
