package com.wordpress.utils;

import java.util.Hashtable;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.AppDAO;
import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.dialog.InquiryView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.HTTPPostConn;

/*
 * 
 */

public class DataCollector {
	
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
	
	public void collectData(int numberOfBlog) {
		Preferences appPrefs= Preferences.getIstance();
		
		if(appPrefs.isFirstStartupOrUpgrade) {

			String appVersion = "";

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
			//http://localhost/geo4you/info.php
			//http://api.wordpress.org/bbapp/update-check/1.0/

				urlEncoder.append("app_version", appVersion);
				urlEncoder.append("device_language", language);
				urlEncoder.append("device_uuid", (String)appPrefs.getOpt().get("device_uuid"));
				
				if ( mobileCountryCode != -1 )
					urlEncoder.append("mobile_country_code", ""+mobileCountryCode);
				
				if(mobileNetworkNumber != -1)
					urlEncoder.append("mobile_network_number", ""+mobileNetworkNumber);
				
				urlEncoder.append("device_os", deviceOS);
				urlEncoder.append("device_version", deviceSoftwareVersion);
				urlEncoder.append("num_blogs", ""+numberOfBlog);				
				//checking new app version and send stats
				final HTTPPostConn connection = new HTTPPostConn( "http://api.wordpress.org/bbapp/update-check/1.0/"  , urlEncoder.getBytes());
				connection.startConnWork(); //starts connection
			} 
	}
	
	
	public void checkUpdate(){
		Preferences appPrefs= Preferences.getIstance();
		//if(appPrefs.isFirstStartupOrUpgrade) return; 
		
		try {
			
		//check upgrade only. no gathering stats
		Hashtable opt = appPrefs.getOpt();
		String updateTime = (String)opt.get("update_check_time");
		long currentTime = System.currentTimeMillis();
		long diffDays = 100;
		
		if(updateTime != null) {
			long lastUpdate = Long.parseLong(updateTime);
			// Get difference in milliseconds
		    long diffMillis = currentTime - lastUpdate;
		    // Get difference in days
		    diffDays = diffMillis/(24*60*60*1000);
		}
			
		//start check upgrade
		if(diffDays > 7 ) {
			final HTTPPostConn connection = new HTTPPostConn( "http://api.wordpress.org/bbapp/update-check/1.0/"  , null);
			connection.addObserver(new CheckUpdateCallBack());
			connection.startConnWork(); //starts connection
		
			//store the date of check
			opt.put("update_check_time", String.valueOf(currentTime)); //update last update chek time
			AppDAO.storeApplicationPreferecens(appPrefs);
		}	
		
		} catch (Exception e) {
			Log.error(e, "Error while checking upgrade");
		}
		
	}
	
	
	private class CheckUpdateCallBack implements Observer {
		
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					BlogConnResponse resp= (BlogConnResponse) object;
					
					String html = null;
					
					if(resp.isStopped()){
						return;
					}
					
					if(!resp.isError()) {
						try {
							html = (String)resp.getResponseObject();
							//break the string 
							String[] split = StringUtils.split(html, "\n");
							String remoteAppVersion = split[0];
							String remoteAppUrl = split[1];
							String currentAppVersion = getAppVersion();
							Log.info("remote app version is: "+remoteAppVersion);
							Log.info("remote app url is: "+remoteAppUrl);
							Log.info("local app version is: "+currentAppVersion);
							
							String[] remoteAppVersionArray = StringUtils.split(remoteAppVersion, ".");
							String[] currentAppVersionArray = StringUtils.split(currentAppVersion, ".");
							boolean isNewVersionAvailable = false;
							
							for (int i = 0; i < remoteAppVersionArray.length; i++) {
								String newAppVersionNum = 	remoteAppVersionArray[i];
								
								if(currentAppVersionArray.length-1 >= i ) {
									String localVersionNum = currentAppVersionArray[i];
									
									int nerVersNum = Integer.parseInt(newAppVersionNum);
									int currVersNum = Integer.parseInt(localVersionNum);
									
									if ( nerVersNum >  currVersNum) {
										isNewVersionAvailable = true;
										break;
									} else 	if (Integer.parseInt(newAppVersionNum) < 
									Integer.parseInt(localVersionNum) ) {
										//maybe?
										isNewVersionAvailable = false;
										break;
									}
									
								} else {//	server Version is newer
									isNewVersionAvailable = true;
									break;
								}
							}
							
							
							if (isNewVersionAvailable ) {
								Log.info("new version is available");
			    				InquiryView inqView= new InquiryView("WordPress for Blackberry new version is now available. Do you want update now?");
								//InquiryView inqView= new InquiryView(_resources.getString(WordPressResource.MESSAGE_APP_UPGRADE));
			    				inqView.setDialogClosedListener(new MyDialogClosedListener(remoteAppUrl));
			    				inqView.show();
								
							}							
						} catch (Exception e) {
							Log.error(e, "updater error");
							return;
						}
					} else {
					}
				}
			});
		}
	} 
	

	private class MyDialogClosedListener implements DialogClosedListener {
		
		private String updateUrl = "";
		
		public MyDialogClosedListener(String updateUrl) {
			super();
			this.updateUrl = updateUrl;
		}

		
		public void dialogClosed(Dialog dialog, int choice) {
			if (choice == Dialog.YES) {
				
		        //Get the current application descriptor.
				BrowserSession visit = Browser.getDefaultSession();
				visit.displayPage(updateUrl);		        
		        Log.info("Application is exiting...");
		        System.exit(0);
			}
		}
	}
    
	
	private String getLanguage() {
		String language = Locale.getDefault().getLanguage();
		Log.debug("Device Language: "+language);
		return language;
	}
	
	private  int getMobileNetworkNumber() {
		int mnc = RadioInfo.getMNC(RadioInfo.getCurrentNetworkIndex());
		Log.debug("Mobile NetworkNumber:"+mnc);
		return mnc;
	}
	
	private int getMobileCountryCode() {
		int mcc = RadioInfo.getMCC( RadioInfo.getCurrentNetworkIndex() );
		Log.debug("mobile Country Code: "+ mcc);
		return mcc;
	}
	
	private String getAppVersion() {
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
	private String getDeviceOS(){
		String deviceOS = DeviceInfo.getPlatformVersion();
		Log.debug("deviceOS: "+deviceOS);
		return deviceOS;
	}
	
	//ex. 4.6.1
	private String getDeviceSoftwareVersion() {
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