package com.wordpress.utils.conn;


/**
 * Abstract class for blackberry configurations. It provides information over
 * the configuration to be used in order to have a working connection over 
 * blackberry based systems. 
 * 
 */
public abstract class AbstractConfiguration {

    /** 1: Permission denied value*/ 
    protected static final int PERMISSION_DENIED = 1;
    /** 0: Permission granted value*/
    protected static final int PERMISSION_GRANTED = 0;
    /** -1: Permission undefined value*/
    protected static final int PERMISSION_UNDEFINED = -1;
    /** Empty String: Value to initialize the configurations parameters*/
    protected static final String NO_PARAMETERS = "";
    /** Empty String: Value to initialize the configurations Descriptions*/
    protected static final String NO_DESCRIPTION = "";
	
    /** BASIC custom APN configuration parameters */
    protected static String BASE_CONFIG_PARAMETERS = ";deviceside=true";
    
    /**Description when no config has been set*/
    protected static final String CONFIG_NONE_DESCRIPTION = "No working config found";
 
    /**The permission to use the configuration owned by this class*/
    private int permission = PERMISSION_UNDEFINED;
    /**The String to be added to the URL when a connection is requested*/
    private String urlParameters = NO_PARAMETERS;
    /**The String formatted description for this configuration*/
    private String description = NO_DESCRIPTION;

    /**
     * Accessor method to get the permission field of this class
     * @return int permission associated with this configuration
     */
    public int getPermission() {
        return this.permission;
    }

    /**
     * Accessor method to set the permission field of this configuration
     * @param permission is the permission to be set for this configuration
     */
    public void setPermission(int permission) {
        this.permission = permission;
    }

    /**
     * Accessor method to get the urlParameters field of this configuration
     * @return String configuration to be added to the request URL in order to 
     * activate this configuration when the connector is opened
     */
    public String getUrlParameters() {
        return this.urlParameters;
    }

    /**
     * Accessor method to set the urlParameters for this configuration
     * @param urlParameters is the parameter string to be set for connections 
     * requests
     */
    public void setUrlParameters(String urlParameters) {
        this.urlParameters = urlParameters;
    }

    /**
     * Accessor method to get this configuration description
     * @return String description of this configuration
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Accessor method to set this configuration's description
     * @param description new description for this configuration
     */
    public void setDescription(String description) {
        this.description = description;
    }
}