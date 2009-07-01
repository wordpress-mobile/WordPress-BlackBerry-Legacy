package com.wordpress.utils.conn;


/**
 * Abstract class for blackberry configurations. 
 * 
 */
public abstract class AbstractConfiguration {

    protected static final int PERMISSION_DENIED = 1;
    protected static final int PERMISSION_GRANTED = 0;
    protected static final int PERMISSION_UNDEFINED = -1;

    protected static String BASE_CONFIG_PARAMETERS = ";deviceside=true";
    protected static final String CONFIG_NONE_DESCRIPTION = "No working config found";

 
    private int permission = PERMISSION_UNDEFINED; //config predefined permission
    private String urlParameters = ""; //config url parameters
    private String description = ""; //config description

   
    public int getPermission() {
        return this.permission;
    }

    public void setPermission(int permission) {
        this.permission = permission;
    }

    public String getUrlParameters() {
        return this.urlParameters;
    }

    public void setUrlParameters(String urlParameters) {
        this.urlParameters = urlParameters;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}