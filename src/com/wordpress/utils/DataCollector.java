package com.wordpress.utils;

import java.util.Hashtable;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.DeviceInfo;
import net.rim.device.api.system.RadioInfo;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.AppDAO;
import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.ErrorView;
import com.wordpress.view.dialog.InfoView;
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
	
	ConnectionInProgressView connectionProgressView=null;
	
	private static int UPDATE_REMOTE_CHECK_NUMBER_DELAY_DAYS = 5; //after xx days check for upgrade
	
	private byte[] getAllInfo(int numberOfBlog){
		Log.trace(">>> Getting device infos");
		Preferences appPrefs= Preferences.getIstance();

		String appVersion = "";

		try {
			appVersion = PropertyUtils.getIstance().getAppVersion();
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
			deviceOS = getPlatformVersion();
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
		urlEncoder.append("device_uuid", appPrefs.getDeviceUUID());
		
		if ( mobileCountryCode != -1 )
			urlEncoder.append("mobile_country_code", ""+mobileCountryCode);
		
		if(mobileNetworkNumber != -1)
			urlEncoder.append("mobile_network_number", ""+mobileNetworkNumber);
		
		urlEncoder.append("device_os", deviceOS);
		urlEncoder.append("device_version", deviceSoftwareVersion);
		urlEncoder.append("num_blogs", ""+numberOfBlog);
		
		Log.trace("device infos -> "+ urlEncoder.toString());
		Log.trace("<<< Getting device infos");
		
		return urlEncoder.getBytes();
		
	}
	
	
	private boolean isCollectedDataExpired() {
		Log.trace("Find out if stats was expired");
	
		Preferences appPrefs = Preferences.getIstance();
		if(appPrefs.isFirstStartupOrUpgrade) return true;
		
		//check upgrade only. no gathering stats
		Hashtable opt = appPrefs.getOpt();
		String updateTime = (String)opt.get("update_check_time");
		long currentTime = System.currentTimeMillis();
		long diffDays = Integer.MAX_VALUE;
		
		if(updateTime != null) {
			long lastUpdate = Long.parseLong(updateTime);
			// Get difference in milliseconds
		    long diffMillis = currentTime - lastUpdate;
		    // Get difference in days
		    diffDays = diffMillis/(24*60*60*1000);
		}
			
		//start check upgrade
		if(diffDays > UPDATE_REMOTE_CHECK_NUMBER_DELAY_DAYS ) {
			return true;
		} else 
			return false;
		
	}
	
	public void pingStatsEndpoint(int numberOfBlog) {
		Log.trace("Pinging Stat Endpoint...");
		byte[] data = getAllInfo(numberOfBlog);	
		final HTTPPostConn connection = new HTTPPostConn( WordPressInfo.BB_APP_STATS_ENDPOINT_URL  , data);
		connection.startConnWork(); //starts connection
	}
	
	/**
	 * Called at startup and at fixed interval to gather stats and/or check updating
	 *  	
	 * @param numberOfBlog
	 */
	public void collectData(int numberOfBlog) {
		Log.trace(">>> Collect data");
		Preferences appPrefs= Preferences.getIstance();		
		if(appPrefs.isFirstStartupOrUpgrade) {
			pingStatsEndpoint(numberOfBlog);
			appPrefs.isFirstStartupOrUpgrade = false; //consume the first startup event
		} else {
			//checking new app version and send stats
			if(this.isCollectedDataExpired()) {
				checkForUpdateSilent(numberOfBlog);
			}	else {
				Log.trace("< 5 days");
			}
		}
		Log.trace("<<< Collect data");
	}
	
	
	/**
	 * checking if a new app version is available
	 * @param data
	 * @throws Exception
	 */
	public void checkForUpdate(int numberOfBlog) throws Exception {
		Log.trace("checking for a new app version");
		Preferences appPrefs= Preferences.getIstance();
		byte[] data = getAllInfo(numberOfBlog);	
		final HTTPPostConn connection = new HTTPPostConn( WordPressInfo.BB_APP_STATS_ENDPOINT_URL , data);
		connection.addObserver(new CheckUpdateCallBack());
		connectionProgressView= new ConnectionInProgressView(
				_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
		connection.startConnWork(); //starts connection
		
		//store the date of check
		Hashtable opt = appPrefs.getOpt();
		long currentTime = System.currentTimeMillis();
		opt.put("update_check_time", String.valueOf(currentTime)); //update last update chek time
		AppDAO.storeApplicationPreferecens(appPrefs);		
		
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	private void checkForUpdateSilent(int numberOfBlog){
		try {
				Preferences appPrefs= Preferences.getIstance();
				byte[] data = getAllInfo(numberOfBlog);
				final HTTPPostConn connection = new HTTPPostConn( WordPressInfo.BB_APP_STATS_ENDPOINT_URL , data);
				connection.addObserver(new CheckAutomaticUpdateCallBack());
				connection.startConnWork(); //starts connection
				//store the date of check
				Hashtable opt = appPrefs.getOpt();
				long currentTime = System.currentTimeMillis();
				opt.put("update_check_time", String.valueOf(currentTime)); //update last update chek time
				AppDAO.storeApplicationPreferecens(appPrefs);
		} catch (Exception e) {
			Log.error(e, "Error while checking upgrade");
		}
	}
	
	
	/**
	 * 
	 * @param html The response from the API server
	 * @param showPopup true if popup should be showed when app is already updated
	 */
	private void parseServerResponse(String html, boolean showPopup) {
		
		//break the string 
		String[] split = StringUtils.split(html, "\n");
		String remoteAppVersion = split[0];
		String remoteAppUrl = split[1];
		String currentAppVersion = PropertyUtils.getIstance().getAppVersion();
		Log.info("remote app version is: "+remoteAppVersion);
		Log.info("remote app url is: "+remoteAppUrl);
		Log.info("local app version is: "+currentAppVersion);
				
		if(currentAppVersion.indexOf(".alpha") > -1)
			currentAppVersion = StringUtils.replaceLast(currentAppVersion, ".alpha", "");
		if(currentAppVersion.indexOf(".beta") > -1)
			currentAppVersion = StringUtils.replaceLast(currentAppVersion, ".beta", "");
		if(currentAppVersion.indexOf(".rc1") > -1)
			currentAppVersion = StringUtils.replaceLast(currentAppVersion, ".rc1", "");
		if(currentAppVersion.indexOf(".rc2") > -1)
			currentAppVersion = StringUtils.replaceLast(currentAppVersion, ".rc2", "");
		if(currentAppVersion.indexOf(".rc3") > -1)
			currentAppVersion = StringUtils.replaceLast(currentAppVersion, ".rc3", "");
	
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
		
		
		if (isNewVersionAvailable) {
			Log.info("new version is available");
			InquiryView inqView= new InquiryView(_resources.getString(WordPressResource.MESSAGE_NEW_VERSION_AVAILABLE));
			//InquiryView inqView= new InquiryView(_resources.getString(WordPressResource.MESSAGE_APP_UPGRADE));
			inqView.setDialogClosedListener(new MyDialogClosedListener(remoteAppUrl));
			inqView.show();
		} else if(showPopup) {
			Log.info("version is updated");
			InfoView inqView= new InfoView(_resources.getString(WordPressResource.MESSAGE_NO_NEW_VERSION_AVAILABLE));
			inqView.show();
		}
	}
	
	
	
	/**
	 * callback for upgrade check 
	 * @author dercoli
	 *
	 */
	private class CheckUpdateCallBack implements Observer {
		
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Log.trace("checking for a new app version callback method");
					//dismiss the dialog
		
					connectionProgressView.close();
					
					BlogConnResponse resp= (BlogConnResponse) object;
					
					if(resp.isStopped()){
						return;
					}
					if(!resp.isError()) {
						try {
							String html = (String)resp.getResponseObject();
							parseServerResponse(html, true);
						} catch (Exception e) {
							Log.error(e, "updater error");
							return;
						}
					} else {
						Log.error(resp.getResponseObject(), "updater error");
						ErrorView errView = new ErrorView(resp.getResponse());
						errView.doModal();
					}
				}
			});
		}
	} 
	
	
	
	/**
	 * callback for background checking of new version 
	 * @author dercoli
	 *
	 */
	private class CheckAutomaticUpdateCallBack implements Observer {
		
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
							parseServerResponse(html, false);
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
	
	
	//A string representing the platform version. 
	//An empty string is returned when program is being run on a simulator.
	private String getPlatformVersion(){
		String platformVersion = DeviceInfo.getPlatformVersion() == null ? " n.a." : DeviceInfo.getPlatformVersion(); 
		Log.debug("Platform Version: " +platformVersion);
		return platformVersion;
	}
	
	//ex. 4.6.1
	private String getDeviceSoftwareVersion() {
		String deviceSoftwareVersion =  (DeviceInfo.getSoftwareVersion() == null ? " n.a." : DeviceInfo.getSoftwareVersion());
		Log.debug("Software Version: " + deviceSoftwareVersion);
		return deviceSoftwareVersion;
	}
}