package com.wordpress.utils.conn;


/**
 * Abstract class for blackberry configurations. 
 * 
 */
public abstract class AbstractConfiguration {

    protected static String BASE_CONFIG_PARAMETERS = ";deviceside=true";
    protected static final String CONFIG_NONE_DESCRIPTION = "No working config found";

    private String urlParameters = ""; //config url parameters
    private String description = ""; //config description

    
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