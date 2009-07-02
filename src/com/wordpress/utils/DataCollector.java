package com.wordpress.utils;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;

import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.HTTPGetConn;

/*
 * 
 */

public class DataCollector {
	
	public static void collectData() {
		Preferences appPrefs= Preferences.getIstance();

		if(appPrefs.isFirstStartup) {
			getAppVersion();
			getLanguage();
			getCarrier();
			getDeviceOS();
			getDeviceVersion();
			
			//crate the link
			URLEncodedPostData urlEncoder = new URLEncodedPostData("UTF-8", false);
			//urlEncoder.append(name, value);
			final HTTPGetConn connection = new HTTPGetConn("");
	     //   connection.startConnWork(); //starts connection
		}
	
	}
	
	
	public static String getLanguage() {
		String language = Locale.getDefault().getLanguage();
		Log.debug("Device Language: "+language);
		return language;
	}
	
	public static String getCarrier() {
		int mcc = RadioInfo.getMCC( RadioInfo.getCurrentNetworkIndex() );
		int mnc = RadioInfo.getMNC(RadioInfo.getCurrentNetworkIndex());
		Log.debug("mobile Country Code: "+ mcc+" Mobile NetworkNumber:"+mnc);
		return "MCC:"+mcc+" MNC:"+mnc; 

	}
	
	public static String getAppVersion() {
    	String version = PropertyUtils.getAppVersion(); //read from the alx files
        if(version == null || version.trim().equals("")) { //read value from jad file
        	//MIDlet-Version
        	version = PropertyUtils.getIstance().get("MIDlet-Version");
        	if(version == null)
        		version = "";
        }
    	Log.debug("App version: "+version);
        return version;
	}
	
	public static String getDeviceOS(){
		String deviceOS = DeviceInfo.getPlatformVersion();
		Log.debug("deviceOS: "+deviceOS);
		return deviceOS;
	}
	
	public static String getDeviceVersion() {
		  // Get the platform version
    	int[] handles = CodeModuleManager.getModuleHandles();
    	int size = handles.length;
    	//Check for a particular RIM module (Here, the ribbon app)
    	for (int i = size-1; i>=0;--i) {
	    	if (CodeModuleManager.getModuleName(handles[i]).equals("net_rim_bb_ribbon_app")) {
	    		Log.debug("DeviceVersion: "+CodeModuleManager.getModuleVersion(handles[i]));
	    		return CodeModuleManager.getModuleVersion(handles[i]);
	    	}
    	}
	
    	return "";
	}
}
