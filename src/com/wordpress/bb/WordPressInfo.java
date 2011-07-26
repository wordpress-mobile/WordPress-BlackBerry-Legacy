//#preprocess
package com.wordpress.bb;

import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.log.Log;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

//#ifndef VER_6.0.0 | BlackBerrySDK7.0.0
/*  avoid Eclipse complaints
//#endif
import net.rim.device.api.system.capability.DeviceCapability;
//#ifndef VER_6.0.0 | BlackBerrySDK7.0.0
*/
//#endif


/**
 * Class to provide information about the application
 * and its environment.  
 */
public final class WordPressInfo {

	/** System event GUID */
    public final static long APPLICATION_ID = 0x97ebc046dec5817fL;
    public final static long COMMENTS_UID = 0x8de9c6b3a49fd864L;
    
	public final static String STATS_AUTH_ENDPOINT_URL = "https://public-api.wordpress.com/get-user-blogs/1.0/";
	public final static String STATS_ENDPOINT_URL = "http://stats.wordpress.com/api/1.0/";
	public final static String STATS_CHART_URL = "http://chart.apis.google.com/chart";
	public final static String BB_APP_STATS_ENDPOINT_URL = "http://api.wordpress.org/bbapp/update-check/1.0/";
	public final static String BB_APP_SIGNUP_URL = "http://wordpress.com/signup/?ref=wp-blackberry";
	
	public final static String GOOGLE_GEOCODING_API_URL = "http://maps.google.com/maps/api/geocode/";
	public static final String GOOGLE_STATIC_MAP_URL = "http://maps.google.com/maps/api/staticmap";
	
	public final static String WPCOM_LOGIN_URL = "https://wordpress.com/wp-login.php";
	public final static String WPCOM_GLOBAL_DASHBOARD = "http://dashboard.wordpress.com/wp-admin/";
	public final static String WPCOM_READER_URL = "https://wordpress.com/reader/mobile/";
	
	public static final int DEFAULT_DOWNLOADED_COMMENTS = 100;
	
	/*
	 * you can determine whether a device with a touch screen supports forceless clicks 
	 * by invoking DeviceCapability.isTouchClickSupported(). 
	 * If the device supports forceless clicks, the method returns false. 
	 * If the device has a SurePress touch screen, the method returns true.
	 */
	public static boolean isForcelessTouchClickSupported; //If the device supports forceless clicks (BB 6.0 or higher)

	private static Bitmap icon = Bitmap.getBitmapResource("application-icon.png");
    private static Bitmap newCommentsIcon = Bitmap.getBitmapResource("application-icon-new.png");

    private static Bitmap icon68px = Bitmap.getBitmapResource("application-icon-68.png");
    private static Bitmap newCommentsIcon68px = Bitmap.getBitmapResource("application-icon-new68.png");
    
    private static Bitmap icon92px = Bitmap.getBitmapResource("application-icon-92.png");
    private static Bitmap newCommentsIcon92px = Bitmap.getBitmapResource("application-icon-new92.png");
   
    //keep some configuration data outside the classic app storage model
    private static PersistentObject store;
    private static PersistableAppInfo persistableInfo;
        
    static {
    	if(tryInitializeStore()) {
    		synchronized(store) {
    			if (!(store.getContents() instanceof PersistableAppInfo)) {
    				store.setContents(new PersistableAppInfo(2));
    				store.commit();
    			}
    		}
    		persistableInfo = (PersistableAppInfo)store.getContents();
    	}
    	else {
    		persistableInfo = new PersistableAppInfo(2);
    	}
    }

    private static boolean tryInitializeStore() {
    	if(store != null) { return true; }
    	try {
    		//"com.wordpress.bb.PersistableAppInfo"
    		store = PersistentStore.getPersistentObject(0x90b8f1bf73b6cbb9L);
    		return true;
    	} catch (ControlledAccessException e) {
    		return false;
    	}
    }

    /**
     * Initializes the application information from the descriptor and the
     * command-line arguments.  This method must be called on startup.
     * @param args Arguments
     */
    public static synchronized void initialize() {
    	Log.trace("WordPress Info inizialized");
    	isForcelessTouchClickSupported = false;

    	//#ifndef VER_6.0.0 | BlackBerrySDK7.0.0
    	/*  avoid Eclipse complaints
    	//#endif
    	 
    	 //this code is executed only when the tag VER_6.0.0 is defined
 		// If the device supports forceless clicks, the method returns false
    	isForcelessTouchClickSupported = !(DeviceCapability.isTouchClickSupported());
    	
    	//#ifndef VER_6.0.0 | BlackBerrySDK7.0.0
    	*/  
    	//#endif
    }
   
    public static Bitmap getIcon() {
    	int prefSize = HomeScreen.getPreferredIconHeight();
    	if( prefSize == 68 )
    		return icon68px;
    	else if( prefSize == 92 )
    		return icon92px;
    	else return icon;
    }
    
    public static Bitmap getNewCommentsIcon() {
    	int prefSize = HomeScreen.getPreferredIconHeight();
    	if( prefSize == 68 )
    		return newCommentsIcon68px;
    	else if( prefSize == 92 )
    		return newCommentsIcon92px;
    	else return newCommentsIcon;    
    }
    
    public static String getLastVersion() {
        Object value = persistableInfo.getElement(PersistableAppInfo.FIELD_LAST_APP_VERSION);
        if(value instanceof String) {
            return (String)value;
        }
        else {
            return "";
        }
    }
    
    public static void updateLastVersion() {
        persistableInfo.setElement(PersistableAppInfo.FIELD_LAST_APP_VERSION, PropertyUtils.getIstance().getAppVersion());
        commitPersistableInfo();
    }
    
    private static void commitPersistableInfo() {
        if(tryInitializeStore()) {
            synchronized(store) {
                store.setContents(persistableInfo);
                store.commit();
            }
        }
    }
}