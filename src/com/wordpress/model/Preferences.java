package com.wordpress.model;

import com.wordpress.utils.conn.ConnectionUtils;

public class Preferences {

	private static Preferences singletonObject;
	
	private String photoEncoding=""; //jpg, png, ecc
    private String audioEncoding="";
    private String videoEncoding="";
    private boolean isUserConnectionOptionsEnabled = false;
    private String username;
    private String password;
    private String gateway;
    private String apn;
    private String gatewayPort;
    private String sourceIP;
    private String sourcePort;
    
	public static Preferences getIstance() {
		if (singletonObject == null) {
			singletonObject = new Preferences();
		}
		return singletonObject;
	}
		
	// singleton
	private Preferences() {
		gatewayPort = ConnectionUtils.WAP_DEFAULT_GWAYPORT;
		sourceIP = ConnectionUtils.WAP_DEFAULT_SOURCEIP;
		sourcePort = ConnectionUtils.WAP_DEFAULT_SOURCEPORT;
	}
    
	public String getGateway() {
		return gateway;
	}

	public String getApn() {
		return apn;
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

	public void setGateway(String gateway) {
		this.gateway = gateway;
	}

	public void setApn(String apn) {
		this.apn = apn;
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
  
    public String getPhotoEncoding() {
		return photoEncoding;
	}

	public void setPhotoEncoding(String photoEncoding) {
		this.photoEncoding = photoEncoding;
	}

	public String getAudioEncoding() {
		return audioEncoding;
	}

	public void setAudioEncoding(String audioEncoding) {
		this.audioEncoding = audioEncoding;
	}

	public String getVideoEncoding() {
		return videoEncoding;
	}

	public void setVideoEncoding(String videoEncoding) {
		this.videoEncoding = videoEncoding;
	}

	public boolean isUserConnectionOptionsEnabled() {
		return isUserConnectionOptionsEnabled;
	}

	public void setUserConnectionOptionsEnabled(boolean isUserWapOptions) {
		this.isUserConnectionOptionsEnabled = isUserWapOptions;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}