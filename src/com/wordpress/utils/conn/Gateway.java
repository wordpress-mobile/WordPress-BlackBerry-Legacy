package com.wordpress.utils.conn;


/**
 * Wrap around the concept of gateway: provides the informations related to 
 *  configurations like APN, Username, Password and country....
 */
public class Gateway {

    private String apn;
    private String username;
    private String password;
    private String gatewayIP;
    private String country;    
    private String gatewayPort;
    private String sourceIP;
    private String sourcePort;

    
    protected Gateway() {
        
    }
    
    /**
     * The constructor must receive all of the parameters
     * @param apn is the String representation of the Access Point Name for this 
     * wap gateway
     * @param username required to access the given gateway (mandatory for some 
     * carrier)
     * @param password required to access the given gateway (mandatory for some 
     * carrier)
     * @param country is the country identificator for this gateway
     */
    public Gateway(String apn, String username, String password, String country) {
        this.apn        = apn;
        this.username   = username;
        this.password   = password;
        this.gatewayIP  = null;
        this.country    = country;
    }

    /**
     * The constructor must receive all of the parameters
     * @param apn is the String representation of the Access Point Name for this 
     * wap gateway
     * @param username required to access the given gateway (mandatory for some 
     * carrier)
     * @param password required to access the given gateway (mandatory for some 
     * carrier)
     * @param gatewayIP the IP of the gateway
     * @param country is the country identificator for this gateway
     */
    public Gateway(String apn, String username, String password,
                      String gatewayIP, String country) {
        this.apn        = apn;
        this.username   = username;
        this.password   = password;
        this.gatewayIP  = gatewayIP;
        this.country    = country;
    }


    /**
     * Accessor  method
     * @return String representation of the Access Point Name (APN)
     */
    public String getApn() {
        return apn;
    }

    /**
     * Accessor  method
     * @return String representation of the username related to this Access 
     * Point Name (APN)
     */
    public String getUsername() {
        return username;
    }

    /**
     * Accessor  method
     * @return String representation of the password related to this Access 
     * Point Name (APN)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Accessor  method
     * @return String representation of the country related to this Access 
     * Point Name (APN)
     */
    public String getCountry() {
        return country;
    }

    public String getGatewayIP() {
        return gatewayIP;
    }

	public String getGatewayPort() {
		return gatewayPort;
	}

	public String getSourceIP() {
		return sourceIP;
	}

	public String getSourcePort() {
		return sourcePort;
	}

	public void setGatewayPort(String gatewayPort) {
		this.gatewayPort = gatewayPort;
	}

	public void setSourceIP(String sourceIP) {
		this.sourceIP = sourceIP;
	}

	public void setSourcePort(String sourcePort) {
		this.sourcePort = sourcePort;
	}
}
