package com.wordpress.utils.conn;

public class ServiceBookConfig extends AbstractConfiguration {
	
	/**Service book configurations description*/
    protected static final String SERVICE_BOOK_CONFIG_DESCRIPTION = "Service book Configuration";

    /**
     *  Device's ServiceBook content related configurations 
     */
	public ServiceBookConfig() {
		super();
        setDescription(SERVICE_BOOK_CONFIG_DESCRIPTION);
	}
}