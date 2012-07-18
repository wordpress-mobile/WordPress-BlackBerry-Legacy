//#preprocess
package com.wordpress.bb;

import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.log.Log;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0 
import net.rim.device.api.ui.Touchscreen;
//#endif

//#ifdef BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.system.capability.DeviceCapability;
import net.rim.device.api.ui.toolbar.ToolbarManager;
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
	
	public final static String readerURL_v3 = "https://en.wordpress.com/reader/mobile/v2/?chrome=no"; 
	public final static String authorizedHybridHost = "en.wordpress.com"; 
	public final static String readerTopicsURL = "http://en.wordpress.com/reader/mobile/v2/?template=topics";
	public final static String readerDetailURL = "https://en.wordpress.com/wp-admin/admin-ajax.php?action=wpcom_load_mobile&template=details&v=2";
	
	public static final int DEFAULT_DOWNLOADED_COMMENTS = 20;
	public static final int DEFAULT_ITEMS_NUMBER = 20;
	
	public static final String SUPPORT_EMAIL_ADDRESS = "blackberry-issue-report@automattic.com";
	public static final String ISSUE_REPORT_EMAIL_ADDRESS = "blackberry-issue-report@automattic.com";
	
	/*
	 * you can determine whether a device with a touch screen supports forceless clicks 
	 * by invoking DeviceCapability.isTouchClickSupported(). 
	 * If the device supports forceless clicks, the method returns false. 
	 * If the device has a SurePress touch screen, the method returns true.
	 */
	public static boolean isForcelessTouchClickSupported; //If the device supports forceless clicks (BB 6.0 or higher)
	public static boolean isToolbarSupported;
	public static boolean isTouchscreenSupported;
	
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

    	 //#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0 
    	isTouchscreenSupported = Touchscreen.isSupported();
    	//#elseif
    	isTouchscreenSupported = false;
    	//#endif
    	
    	
    	//#ifdef BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
    	
    	if( isTouchscreenSupported )
    		isForcelessTouchClickSupported = !(DeviceCapability.isTouchClickSupported());  	// If the device supports forceless clicks, the method returns false
    	else
    		isForcelessTouchClickSupported = false;
    	
    	isToolbarSupported = ToolbarManager.isToolbarSupported();
    	
    	//#elseif
    	
    	isForcelessTouchClickSupported = false;// on Touchscreen devices (Storm only?) running OS5 or lower Toolbar is always supported and forceless click is not supported 
    	//toolbar is always supported on older devices. Documentation doesn't say nothing about ToolBar and Touch capabilities
    	//so we need to assume that things are not related to each other.
    	isToolbarSupported = true; 
    	
    	//#endif
    }
   
    /**
     * Returns the BannerIcon that best fits the required dimensions by the active theme icon size.
     * Since there is no way to get the desidered dimension of the BannerIcon we used
     * the function HomeScreen.getPreferredIconHeight() as base for our calculation.
     * 
     * = BlackBerry Bold 9900
     *   Application icon size 92 x 92 pixels
     *   Banner indicator size 33 x 29 pixels
     *   Title bar indicator size 25 x 25 pixels
     * 
     * = BlackBerry Bold 9700
     *   Application icon size 68 x 68 pixels
     *   Banner indicator size 25 x 22 pixels
     *   Title bar indicator size 19 x 19 pixels
     * 
     * = BlackBerry Curve 9300
     *   Application icon size 44 x 44 pixels
     *   Banner indicator size 19 x 19 pixels
     *   Title bar indicator size 17 x 15 pixels
     *   
     * OS7  
     * http://docs.blackberry.com/en/developers/deliverables/28627/Dimensions_for_indicators_6_1_1491553_11.jsp
     * http://docs.blackberry.com/en/developers/deliverables/28627/Dimensions_for_application_icons_1491552_11.jsp
     * 
     * OS6
     * http://docs.blackberry.com/en/developers/deliverables/17965/Dimensions_for_indicators_1155197_11.jsp
     * http://docs.blackberry.com/en/developers/deliverables/17965/Default_themes_and_dimensions_for_icons_1153521_11.jsp
     * 
     * OS5
     * http://docs.blackberry.com/en/developers/deliverables/24222/Dimensions_for_screens_images_and_icons_476251_11.jsp
     * 
     * @return
     */
    
    public static EncodedImage getBannerIndicator() {
    	int prefSize = HomeScreen.getPreferredIconHeight();
    	if( prefSize >= 68 )
    		return EncodedImage.getEncodedImageResource("wordpress-logo-21-blue.png");
    	else if( prefSize >= 92 )
    		return EncodedImage.getEncodedImageResource("wordpress-logo-32-blue.png");
    	else 
    		return EncodedImage.getEncodedImageResource("wordpress-logo-21-blue.png");
    }
    
    public static Bitmap getIcon() {
    	int prefSize = HomeScreen.getPreferredIconHeight();
    	if( prefSize >= 92 ) {
    		return Bitmap.getBitmapResource("application-icon-92.png");
    	} else if( prefSize >= 80 ) {
    		return Bitmap.getBitmapResource("application-icon-80.png");
    	} else if( prefSize >= 72 ) {
    		return Bitmap.getBitmapResource("application-icon-72.png");
	    } else if( prefSize >= 68 ) {
			return Bitmap.getBitmapResource("application-icon-68.png");
	    } else if( prefSize >= 50 ) {
    		return Bitmap.getBitmapResource("application-icon-50.png");
	    } else if( prefSize >= 46 ) {
			return Bitmap.getBitmapResource("application-icon-46.png");
		} else
    		return Bitmap.getBitmapResource("application-icon-46.png");
    }
    
    public static Bitmap getNewCommentsIcon() {
    	int prefSize = HomeScreen.getPreferredIconHeight();
    	if( prefSize >= 92 ) {
    		return Bitmap.getBitmapResource("application-icon-new92.png");
    	} else if( prefSize >= 80 ) {
    		return Bitmap.getBitmapResource("application-icon-new80.png");
    	} else if( prefSize >= 72 ) {
    		return Bitmap.getBitmapResource("application-icon-new72.png");
	    } else if( prefSize >= 68 ) {
			return Bitmap.getBitmapResource("application-icon-new68.png");
	    } else if( prefSize >= 50 ) {
    		return Bitmap.getBitmapResource("application-icon-new50.png");
	    } else if( prefSize >= 46 ) {
			return Bitmap.getBitmapResource("application-icon-new46.png");
		} else
    		return Bitmap.getBitmapResource("application-icon-new46.png");   
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