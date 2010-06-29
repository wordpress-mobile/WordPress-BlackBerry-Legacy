//#preprocess
package com.wordpress.bb;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.notification.NotificationsConstants;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.controller.MainController;
import com.wordpress.io.AppDAO;
import com.wordpress.io.BaseDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Preferences;
import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Appender;
import com.wordpress.utils.log.BlackberryEventLogAppender;
import com.wordpress.utils.log.ConsoleAppender;
import com.wordpress.utils.log.FileAppender;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.ErrorView;

public class WordPress extends UiApplication implements WordPressResource {

    //create a variable to store the ResourceBundle for localization support
    private static ResourceBundle _resources;
    private static Appender logAppender  = null;
    private Timer timer = new Timer();
    private SplashScreen loadingScreen = null;
    private MainController mainScreen = null;
    private boolean isSDCardNotFound = false;
    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
	
    public static void main(String[] args) {
    	try {
    		//Open the RuntimeStore.
    		RuntimeStore store = RuntimeStore.getRuntimeStore();
    		//Obtain the reference to WordPress for BlackBerry.
    		final Object obj = store.get(WordPressInfo.APPLICATION_ID);
    		//If obj is null, there is no current reference
    		//to WordPress for BlackBerry
    		if (obj == null) {
    			System.out.println("RecordStore is void, launching new app");
    			WordPress app = new WordPress(args);
    			app.enterEventDispatcher();
    		} else {
    			System.out.println("RecordStore is NOT void, foreground the app");
    			((WordPress)obj).requestForeground();
    			System.exit(0);
    		}
    	} catch (Exception e) {
    		WordPress app = new WordPress(args);
    		app.enterEventDispatcher();
    		System.err.println("error reading appstore: " + e.getMessage());
    	} finally {

    	}
    }

	 /**
     * Method to execute in autostart mode.
     */
    private void doAutoStart() {
		
    	invokeLater(new Runnable()
        {
            public void run()
            {
                ApplicationManager myApp = ApplicationManager.getApplicationManager();
                boolean keepGoing = true;
                while (keepGoing)
                {
                    if (myApp.inStartup())
                    {
                        try { Thread.sleep(1000); }
                        catch (Exception ex) { }
                    }
                    else
                    {
                    	// The BlackBerry has finished its startup process
                    	                    	
                    	// Configure the rollover icons                    	
                        HomeScreen.updateIcon(WordPressInfo.getIcon(), 0);
                        //HomeScreen.setRolloverIcon(WordPressInfo.getRolloverIcon(), 0);
                    	initLog();
                    	
	                    //adds the global menuitems for sharing to WP 
	                    SharingHelperOldDevices.getInstance().addGlobalMenuItems(_resources);
                    	
                    	//adds the CHAPI sharing to WP 
	                    SharingHelper.getInstance().unregisterCHAPI();
                		SharingHelper.getInstance().verifyRegistration();
                    	
                    	Log.trace("==== Registering WordPress Comments Notification ====");
                    	//Define a dummy object that provides the source for the event.
                    	Object eventSource = new Object() {
                    		public String toString() {
                    			return "WordPress";
                    		}
                    	};                    	
                    	
                        NotificationsManager.registerSource(
                                WordPressInfo.COMMENTS_UID,
                                eventSource,
                                NotificationsConstants.CASUAL);
                                            	
                    	Log.trace("==== Checking WordPress AutoStart ====");
                    	//check if autostart app is enabled, if so start the "full app"
                    	String baseDirPath = null;
						try {	
							Log.trace("Reading the prefs to find out if  autostartup is selected");
							baseDirPath = AppDAO.getBaseDirPath();
	                    	if(baseDirPath != null) {
	                    		Preferences appPrefs = Preferences.getIstance();
	                    		AppDAO.readApplicationPreferecens(appPrefs); //load pref on startup
	                    		
	                    		if(appPrefs.isAutoStartup()) {
	                    			Log.trace("AutoStart is active");
	                    			loadApp();
	                    		} else {
	                    			Log.trace("AutoStart is NOT active");
	                    			System.exit(0);
	                    		}
	                    	} else {
                    			Log.trace("==== Not foud prefs files. No WordPress AutoStart ====");
	                    		System.exit(0);
	                    	}
						} catch (Exception errCheckStartup) {
							Log.trace("==== Error reading AutoStart value ====");
							System.exit(0);
						} 
                    	
                    	keepGoing = false;
                    }//end else
                }//end while
            }
        });
    }
	
    //init the log system
	private void initLog() {
		Appender eventAppender = new BlackberryEventLogAppender("WordPress for BlackBerry");
		eventAppender.setLogLevel(Log.ERROR);
		eventAppender.open();
		Log.addAppender(eventAppender);
		
		//#ifdef LOG_CONSOLE
		Log.addAppender(new ConsoleAppender());
		//#endif
		
		Log.initLog(Log.TRACE);		
		Log.trace("==== WordPress for BlackBerry Startup ====");
		
		//#ifdef IS_OS47_OR_ABOVE
		Log.trace("==== IS_OS47_OR_ABOVE Edition ====");
		//#endif
	}
    
	public WordPress(String[] args){
		WordPressInfo.initialize(args);
		//When device is in startup check the startup variable
		ApplicationManager myApp = ApplicationManager.getApplicationManager();
		if (myApp.inStartup()) {
			doAutoStart();
		} else {
			initLog();
			Log.trace("==== User Start Mode ====");
        	loadApp();
		}
	}
	
	private void loadApp() {
		
		loadingScreen = new SplashScreen();
		pushScreen(loadingScreen);
     	 
		 //The following code specifies that the screen backlight, once activated,
		 //has a timeout period of 200 seconds.
		 if ((Display.getProperties() & Display.DISPLAY_PROPERTY_REQUIRES_BACKLIGHT) != 0) {
			Backlight.enable(true, 200);
		}

	    // Create an instance of the ConnectionManager and register the GlobalEventListener
		ConnectionManager  _manager = ConnectionManager.getInstance();
	    this.addGlobalEventListener( _manager ); // Needed for ServiceBook parsing method 
		
		Preferences appPrefs = Preferences.getIstance();
		//check application permission as first step
		WordPressApplicationPermissions.getIstance().checkPermissions();
		
		try {
			String baseDirPath = AppDAO.getBaseDirPath(); //read from rms the base dir path

			//if there is no app path stored into RMS put the default path
			if(baseDirPath == null) {
				//first startup
				appPrefs.isFirstStartupOrUpgrade = true; //set as first startup.
				AppDAO.setBaseDirPath(BaseDAO.DEVICE_STORE_PATH); 
			} else {
				//set as no first  startup.
				appPrefs.isFirstStartupOrUpgrade = false; 

				//checking if storage is set to SDcard, then verify the presence of sd card into phone
				if(baseDirPath.equals(AppDAO.SD_STORE_PATH)) {
					if(JSR75FileSystem.supportMicroSD() && JSR75FileSystem.hasMicroSD()) {
						//ok
					} else {
						//microSD not present. set the storage to memory device
						isSDCardNotFound = true;
						AppDAO.setBaseDirPath(BaseDAO.DEVICE_STORE_PATH); 
						baseDirPath = null;
					}
				}
			}

			AppDAO.setUpFolderStructure(); //check for the folder existence, create it if not exist
						
			if ( baseDirPath != null ) {
				//not first startup 	
				AppDAO.readApplicationPreferecens(appPrefs); //load pref on startup
			} else { 
				//check if this is a new inst or an upgrade. 
				//if prefs file exist, this is an upgrade.
				String appPrefsFilePath = AppDAO.getAppPrefsFilePath();
				if (JSR75FileSystem.isFileExist(appPrefsFilePath)) {
					//upgrading
					Log.trace("App upgrading");
					AppDAO.readApplicationPreferecens(appPrefs); //load pref on upgrading
				} else {
					//new inst
					Log.trace("App first installation");
				}
			}
			
			//modified since 1.3, now we are using the real device PIN
			//check the existence of UUID var.
			//if UUID does not exists, generate it and put it in the prefs
			/*Hashtable opt = appPrefs.getOpt();
			if(opt.get("device_uuid") == null)
				opt.put("device_uuid", ""+(Tools.generateDeviceUUID())); 
			AppDAO.storeApplicationPreferecens(appPrefs); //store app pref, trick for store pref when added new parameters
			*/
			
			//add the file log appender
			FileAppender fileAppender = new FileAppender(baseDirPath, BaseDAO.LOG_FILE_PREFIX);
			fileAppender.setLogLevel(Log.ERROR); //if we set level to TRACE the file log size grows too fast
			fileAppender.open();
			Log.addAppender(fileAppender);
			WordPressCore wpCore = WordPressCore.getInstance();
			wpCore.setFileAppender(fileAppender); // add the file appender to the queue
						
			SharingHelper.getInstance().verifyRegistration(); //probably we can cut of this line
			
			//Store this application istance into recordstore
			SharingHelper.storeAppIstance(UiApplication.getUiApplication());
			
     		timer.schedule(new CountDown(), 1500); //1,5 sec splash
			
			// Initialize the notification handler only if notification interval is != 0
			if (appPrefs.getUpdateTimeIndex() != 0)
				NotificationHandler.getInstance().setCommentsNotification(true, appPrefs.getUpdateTimeIndex());
		} catch (Exception e) {
			timer.cancel();
			final String excMsg;
			
			if(e != null && e.getMessage()!= null ) {
				excMsg = "\n" + e.getMessage();
			} else {
				excMsg = "";
			}
			invokeLater(new Runnable() {
				public void run() {
					ErrorView errView = new ErrorView("Startup Error:"+excMsg);
					errView.doModal();
					if (loadingScreen != null) popScreen(loadingScreen);
					mainScreen = MainController.getIstance();
					mainScreen.showView();
				}
			});
		}
	}
	
   private class CountDown extends TimerTask {
	   public void run() {
   	   invokeLater(new Runnable() {
			public void run() {
				
				timer.cancel();
				//previous the user has set the storage to the SD, but SD card is not available right now.
				if(isSDCardNotFound) {
					String errorMsgSD =_resources.getString(WordPressResource.ERROR_SDCARD_NOT_FOUND); 
					ErrorView errView = new ErrorView(errorMsgSD);
					errView.doModal();
				}
				
			    popScreen(loadingScreen);
				mainScreen = MainController.getIstance();
			    mainScreen.showView();
			}
		});
      }
   }
   
}