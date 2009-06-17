package com.wordpress.utils.conn;

import com.wordpress.utils.log.Log;

/**
 * BES Settings Class
 *  
 * @author dercoli
 *
 */
public class BESConfig extends AbstractConfiguration {
	
	 /**Straight connection - works only with BES */
    protected static final String BES_CONFIG_DESCRIPTION = "BES Configuration";
    
    /**BES APN configuration parameters*/
    private static String BES_CONFIG_PARAMETERS = "";
	    
	public BESConfig() {
		super();
        Log.debug("Setting BES Config " + BES_CONFIG_PARAMETERS + " " + BES_CONFIG_DESCRIPTION);
        setUrlParameters(BES_CONFIG_PARAMETERS);
        setDescription(BES_CONFIG_DESCRIPTION);
        //Grants automatically connections for BES configuration
       // setPermission(PERMISSION_GRANTED);
	}
}
