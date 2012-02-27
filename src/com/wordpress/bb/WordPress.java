//#preprocess
package com.wordpress.bb;

import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;

import com.webtrends.mobile.analytics.IllegalWebtrendsParameterValueException;
import com.webtrends.mobile.analytics.rim.WebtrendsConfigurator;
import com.webtrends.mobile.analytics.rim.WebtrendsDataCollector;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.applicationcontrol.ApplicationPermissions;
import net.rim.device.api.applicationcontrol.ApplicationPermissionsManager;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.notification.NotificationsConstants;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Backlight;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.system.SystemListener;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.controller.MainController;
import com.wordpress.io.AppDAO;
import com.wordpress.io.BaseDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Preferences;
import com.wordpress.task.TaskImpl;
import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Appender;
import com.wordpress.utils.log.BlackberryEventLogAppender;
import com.wordpress.utils.log.ConsoleAppender;
import com.wordpress.utils.log.FileAppender;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.ErrorView;

public class WordPress extends UiApplication implements WordPressResource, SystemListener  {

    //create a variable to store the ResourceBundle for localization support
    private static ResourceBundle _resources;
    private static Appender logAppender  = null;
    private Timer timer = new Timer();
    private SplashScreen loadingScreen = null;
    private MainController mainScreen = null;
    private boolean isSDCardNotFound = false;
    //Flag that is used to check whether the application is starting for the first time, 
    //or if it's a move from the background to foreground.
    private boolean AppStartHappened = false;
    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
	
    public static void main(String[] args) {
    	WordPress app = new WordPress();
    	// Perform additional work as part of main method if necessary.
    	app.initLog();
    	
    	if( ApplicationManager.getApplicationManager().inStartup() ) { 
    		// Add a system listener to detect when system is ready and available.
    		app.addSystemListener( app ); 
    	} else { 
    		// System is already ready and available so perform your start up work now.
    		// Note that this work must be completed using invokeLater because the 
    		// application has not yet entered the event dispatcher.
    		app.doStartupWorkLater(); 
    		//SharingHelperOldDevices.storeAppIstance(app);
    	}
    	// Enter the event dispatcher.
    	app.enterEventDispatcher(); 
    }
    
    /**
     * Implementation of the powerUp method for the SystemListener interface
     *
     */
    public void powerUp()
    {
    	// The BlackBerry has finished its startup process
    	Log.trace("==== powerUp : The BlackBerry has finished its startup process ====");
    	removeSystemListener( this );

    	// Configure the rollover icons                    	
    	HomeScreen.updateIcon(WordPressInfo.getIcon(), 0);

    	//adds the CHAPI sharing to WP 
    	SharingHelper.getInstance().unregisterCHAPI();
    	//SharingHelper.getInstance().registerCHAPI();
    	//SharingHelperOldDevices.getInstance().addGlobalMenuItems(_resources);

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
    	
    	System.exit(0);
    }
    
    /**
    * Perform the start up work on a new Runnable using the 
    * invokeLater construct to ensure that it is executed
    * after the event thread has been created.
    *
    */
    private void doStartupWorkLater() 
    { 
       this.invokeLater( new Runnable() { 
          public void run() { 
        	  loadApp(); 
          }
       } );
    }

    
	 /**
     *  autostart method
     
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
                		//SharingHelper.getInstance().verifyRegistration();
                    	
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
							Log.trace("Reading the prefs and check if  autostartup is selected");
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
	*/
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
	}
    
	public WordPress(){
	}
	
	  /**
	 *  If the permissions are insufficient, the user will be prompted
     * to increase the level of permissions.
     */
    public void checkPermissions()
    {
        // NOTE: This app leverages the following permissions: 
        // --
        // --
        // --
        // --
    	//
        
        // Capture the current state of permissions and check against the requirements.
        ApplicationPermissions original = ApplicationPermissionsManager.getInstance().getApplicationPermissions();
        
        if( original.getPermission( ApplicationPermissions.PERMISSION_MEDIA ) == ApplicationPermissions.VALUE_ALLOW &&
        	//original.getPermission( ApplicationPermissions.PERMISSION_IDLE_TIMER ) == ApplicationPermissions.VALUE_ALLOW &&
        	original.getPermission( ApplicationPermissions.PERMISSION_FILE_API ) == ApplicationPermissions.VALUE_ALLOW  &&
        	original.getPermission( ApplicationPermissions.PERMISSION_WIFI ) == ApplicationPermissions.VALUE_ALLOW  )
        {

        	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0

        	//check additional permissions for BB OS4.7 or higher
        	if( 
        		original.getPermission( ApplicationPermissions.PERMISSION_INTERNET ) == ApplicationPermissions.VALUE_ALLOW &&
        		original.getPermission( ApplicationPermissions.PERMISSION_LOCATION_DATA ) == ApplicationPermissions.VALUE_ALLOW &&
        		original.getPermission( ApplicationPermissions.PERMISSION_ORGANIZER_DATA  ) == ApplicationPermissions.VALUE_ALLOW &&
        		original.getPermission( ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION  ) == ApplicationPermissions.VALUE_ALLOW &&
        		original.getPermission( ApplicationPermissions.PERMISSION_RECORDING ) == ApplicationPermissions.VALUE_ALLOW 
        	) {
        		return;
        	}

        	//#else

        	//check additional permissions for BB OS4.6 or lower
        	if( 
        		original.getPermission( ApplicationPermissions.PERMISSION_EVENT_INJECTOR ) == ApplicationPermissions.VALUE_ALLOW &&
        		original.getPermission( ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS ) == ApplicationPermissions.VALUE_ALLOW &&
        		original.getPermission( ApplicationPermissions.PERMISSION_LOCATION_API ) == ApplicationPermissions.VALUE_ALLOW &&
        		original.getPermission( ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION ) == ApplicationPermissions.VALUE_ALLOW 
        	) {
        		return;
        	}

        	//#endif
        }

        // Create a permission request for each of the permissions your application
        // needs. Note that you do not want to list all of the possible permission
        // values since that provides little value for the application or the user.  
        // Please only request the permissions needed for your application.
        ApplicationPermissions permRequest = new ApplicationPermissions();
        //permRequest.addPermission( ApplicationPermissions.PERMISSION_IDLE_TIMER );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_FILE_API );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_WIFI );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_MEDIA );
        
    	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
        permRequest.addPermission( ApplicationPermissions.PERMISSION_LOCATION_DATA );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_ORGANIZER_DATA  );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_CROSS_APPLICATION_COMMUNICATION  );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_INTERNET );
        //#else
        permRequest.addPermission( ApplicationPermissions.PERMISSION_EVENT_INJECTOR );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_EXTERNAL_CONNECTIONS );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_LOCATION_API );
        permRequest.addPermission( ApplicationPermissions.PERMISSION_INTER_PROCESS_COMMUNICATION );
    	//#endif
        
        boolean acceptance = ApplicationPermissionsManager.getInstance().invokePermissionsRequest( permRequest );
        
        if( acceptance ) 
        {
            // User has accepted all of the permissions.
            return;
        } 
        else 
        {
            // The user has only accepted some or none of the permissions requested. In this
            // app, we will not perform any additional actions based on this information. 
            // However, there are several screen where this information will be used throught
        	//the call to methods
            // For example, if the user denied networking capabilities then the application 
            // could disable that functionality if it was not core to the operation of the 
            // application.
        }
    }
	    
    private void loadApp() {
		Log.trace("==== loadApp ====");
    	WordPressInfo.initialize();
		
		//Check the permission only at the first app startup.
		if(!WordPressInfo.getLastVersion().equals(PropertyUtils.getIstance().getAppVersion())) {              
			checkPermissions();
			WordPressInfo.updateLastVersion();
		} else {
			//check the minimum permission settings
		}
			    	
    	loadingScreen = new SplashScreen();
    	pushScreen(loadingScreen);

    	//The following code specifies that the screen backlight, once activated,
    	//has a timeout period of 200 seconds.
    	if ((Display.getProperties() & Display.DISPLAY_PROPERTY_REQUIRES_BACKLIGHT) != 0) {
    		Backlight.enable(true, 200);
    	}

    	// Create an instance of the ConnectionManager and register the GlobalEventListener
    	ConnectionManager  _manager = ConnectionManager.getInstance();
    	addGlobalEventListener( _manager ); // Needed for ServiceBook parsing method 

    	Preferences appPrefs = Preferences.getIstance();

    	try {
    		String baseDirPath = AppDAO.getBaseDirPath(); //read the base dir path
    		//first startup
    		if(baseDirPath == null) {
    			appPrefs.isFirstStartupOrUpgrade = true; //set as first startup.
    			if(JSR75FileSystem.supportMicroSD() && JSR75FileSystem.hasMicroSD()) {
    				AppDAO.setBaseDirPath(AppDAO.SD_STORE_PATH);
    			} else {
    				AppDAO.setBaseDirPath(BaseDAO.DEVICE_STORE_PATH); 
    			}
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
    		AppDAO.readApplicationPreferecens(appPrefs); //load pref on startup

    		//add the file log appender
    		FileAppender fileAppender = new FileAppender(baseDirPath, BaseDAO.LOG_FILE_PREFIX);
    		fileAppender.setLogLevel(Log.ERROR); //if we set level to TRACE the file log size grows too fast
    		fileAppender.open();
    		Log.addAppender(fileAppender);
    		WordPressCore wpCore = WordPressCore.getInstance();
    		wpCore.setFileAppender(fileAppender); // add the file appender to the queue

    		timer.schedule(new CountDown(), 800); //splash

    		// Initialize the notification handler only if notification interval is != 0
    		if (appPrefs.getUpdateTimeIndex() != 0)
    			NotificationHandler.getInstance().setCommentsNotification(true, appPrefs.getUpdateTimeIndex());
    	} catch (Exception e) {
    		//Creates a Hashtable object containing the exception that is thrown by the application
    		Hashtable customParams = new Hashtable();
    		customParams.put("WT.er", e.getMessage());    
    		try
    		{
    			WebtrendsDataCollector.getInstance().onApplicationError("", customParams);
    		}
    		catch (IllegalWebtrendsParameterValueException err)
    		{
    			WebtrendsDataCollector.getLog().e(err.getMessage());
    		}
    		
    		timer.cancel();
    		final String excMsg;

    		if(e != null && e.getMessage()!= null ) {
    			excMsg = "\n" + e.getMessage();
    		} else {
    			excMsg = "\n" + "Please configure application permissions and reboot the device by removing and reinserting the battery.";
    		}

    		timer.cancel();

    		ErrorView errView = new ErrorView("Startup Error:"+excMsg);
    		errView.doModal();
    		try {
    			if (loadingScreen != null && loadingScreen.isDisplayed() ) 
    				popScreen(loadingScreen);
    		} catch (Exception e2) {
    			Log.error(e2, "Splash Screen is not on the stack!");
    		}

    		mainScreen = MainController.getIstance();
    		mainScreen.showView();
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
				
			    try {
			    	if (loadingScreen != null && loadingScreen.isDisplayed() ) 
			    		popScreen(loadingScreen);
				} catch (Exception e) {
					Log.error(e, "Splash Screen is not on the stack!");
				}
				mainScreen = MainController.getIstance();
			    mainScreen.showView();
			}
		});
      }
   }

   public void batteryGood() {
   }
   public void batteryLow() {
   }
   public void batteryStatusChange(int status) {
   }
   public void powerOff() {
   }
  
   public void activate(){
	   super.activate();
	   if(AppStartHappened)
	   {
		   WordPressCore.getInstance().getTasksRunner().enqueue( new TaskImpl() {
			   public void execute() {
				   try {
					   WebtrendsDataCollector.getInstance().onApplicationForeground("", null);
				   } catch (IllegalWebtrendsParameterValueException e) {
					   e.printStackTrace();
				   }
			   }
		   }
		   );
	   } else {
			try {
				WebtrendsConfigurator.LoadConfigFile(new AnalyticsConfig());
				WebtrendsDataCollector wtDC = WebtrendsDataCollector.getInstance();
				wtDC.Initialize();
				wtDC.onApplicationStart( "", null);
			} catch (IllegalWebtrendsParameterValueException e) {
				e.printStackTrace();
			}
		   AppStartHappened=true;
	   }

   }

   public void deactivate(){
	   super.deactivate();
	   WordPressCore.getInstance().getTasksRunner().enqueue( new TaskImpl() {
		   public void execute() {
			   try {
				   WebtrendsDataCollector.getInstance().onApplicationBackground("", null);
			   } catch (IllegalWebtrendsParameterValueException e) {
				   e.printStackTrace();
			   }
		   }
	   }
	   );
   }
}