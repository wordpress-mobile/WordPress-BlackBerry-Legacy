package com.wordpress.model;



public class Preferences {

	private static Preferences singletonObject;
	
    private int timeZoneIndex=-1;
    private int localeIndex=0;
    private boolean deviceSideConnection=true; //identify if the device require client side http connection

	private String photoEncoding=""; //jpg, png, ecc
    private String audioEncoding="";
    private String videoEncoding="";
        
	public static Preferences getIstance() {
		if (singletonObject == null) {
			singletonObject = new Preferences();
		}
		return singletonObject;
	}
    
    //singleton
    private Preferences() {

    }
/*
    public SimpleTimeZone getTimeZone() {
    	if(timeZoneIndex != -1)
    		return new SimpleTimeZone(timeZoneIndex);
    	else
    		return new SimpleTimeZone();
    }
    
    public int getTimeZoneIndex() {
    	return timeZoneIndex;
    }

    public void setTimeZoneIndex(int index) {
    	timeZoneIndex = index;
    }
    */
   
    public int getLocaleIndex() {
		return localeIndex;
	}

	public void setLocaleIndex(int localeIndex) {
		this.localeIndex = localeIndex;
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
	
	public boolean isDeviceSideConnection() {
		return deviceSideConnection;
	}

	public void setDeviceSideConnection(boolean value) {
		this.deviceSideConnection = value;
	}
}