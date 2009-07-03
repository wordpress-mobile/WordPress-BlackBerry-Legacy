package com.wordpress.utils;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;

import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.HTTPPostConn;

/*
 * 
 */

public class DataCollector {
	
	public static void collectData(int numberOfBlog) {
		Preferences appPrefs= Preferences.getIstance();

		String appVersion = "";
		if(appPrefs.isFirstStartup) {
			try {
				appVersion = getAppVersion();
			} catch (Exception e) {
				Log.error(e, "Could not retrive App version");
			}
			
			String language = "";
			try {
				language = getLanguage();
			} catch (Exception e) {
				Log.error(e, "Could not retrive Devices Language");			
			}
			
			int mobileNetworkNumber = -1;
			int mobileCountryCode = -1;
			try {
				mobileNetworkNumber = getMobileNetworkNumber();
				mobileCountryCode = getMobileCountryCode();
			} catch (Exception e) {
				Log.error(e, "Could not retrive Carrier Info");
			}
			
			String deviceOS = "";
			try {
				deviceOS = getDeviceOS();
				if("".equals(deviceOS)) {
					//could be simulator. do one more check
					if(DeviceInfo.isSimulator()) {
						deviceOS= "Simulator";
					} else {
						deviceOS= "Unknown";
					}
				}
			} catch (Exception e) {
				Log.error(e, "Could not retrive Os version");
			}
			
			
			String deviceSoftwareVersion = "";
			try {
				deviceSoftwareVersion = getDeviceSoftwareVersion();
			} catch (Exception e) {
				Log.error(e, "Could not retrive Device Version");
			}
			
			//crate the link
			URLEncodedPostData urlEncoder = new URLEncodedPostData("UTF-8", false);
			urlEncoder.append("app_version", appVersion);
			urlEncoder.append("device_language", language);
			
			if ( mobileCountryCode != -1 )
				urlEncoder.append("mobile_country_code", ""+mobileCountryCode);
			
			if(mobileNetworkNumber != -1)
				urlEncoder.append("mobile_network_number", ""+mobileNetworkNumber);
			
			urlEncoder.append("device_os", deviceOS);
			urlEncoder.append("device_version", deviceSoftwareVersion);
			urlEncoder.append("num_blogs", ""+numberOfBlog);
			
			final HTTPPostConn connection = new HTTPPostConn( "http://api.wordpress.org/bbapp/update-check/1.0/"  , urlEncoder.getBytes());
	        connection.startConnWork(); //starts connection
		}
	
	}
	
	
	public static String getLanguage() {
		String language = Locale.getDefault().getLanguage();
		Log.debug("Device Language: "+language);
		return language;
	}
	
	public static int getMobileNetworkNumber() {
		int mnc = RadioInfo.getMNC(RadioInfo.getCurrentNetworkIndex());
		Log.debug("Mobile NetworkNumber:"+mnc);
		return mnc;
	}
	
	public static int getMobileCountryCode() {
		int mcc = RadioInfo.getMCC( RadioInfo.getCurrentNetworkIndex() );
		Log.debug("mobile Country Code: "+ mcc);
		return mcc;
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
	
	//A string representing the platform version. 
	//An empty string is returned when program is being run on a simulator.
	public static String getDeviceOS(){
		String deviceOS = DeviceInfo.getPlatformVersion();
		Log.debug("deviceOS: "+deviceOS);
		return deviceOS;
	}
	
	//ex. 4.6.1
	public static String getDeviceSoftwareVersion() {
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
