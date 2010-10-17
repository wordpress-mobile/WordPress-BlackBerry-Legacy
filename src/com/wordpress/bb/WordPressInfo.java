//#preprocess
package com.wordpress.bb;

import com.wordpress.utils.log.Log;

import net.rim.device.api.system.Bitmap;

/**
 * Class to provide information about the application
 * and its environment.  
 */
public final class WordPressInfo {

	/** System event GUID */
    public final static long APPLICATION_ID = 0x97ebc046dec5817fL;
    public final static long COMMENTS_UID = 0x8de9c6b3a49fd864L;
    
	public final static String STATS_AUTH_ENDPOINT_URL = "https://public-api.wordpress.com/getuserblogs.php";
	public final static String STATS_ENDPOINT_URL = "http://stats.wordpress.com/csv.php";
	public final static String STATS_CHART_URL = "http://chart.apis.google.com/chart";
	public final static String BB_APP_STATS_ENDPOINT_URL = "http://api.wordpress.org/bbapp/update-check/1.0/";
	public final static String BB_APP_SIGNUP_URL = "http://wordpress.com/signup/?ref=wp-blackberry";
	
	public final static String GOOGLE_GEOCODING_API_URL = "http://maps.google.com/maps/api/geocode/";
	public static final String GOOGLE_STATIC_MAP_URL = "http://maps.google.com/maps/api/staticmap";
	
	public static final int DEFAULT_DOWNLOADED_COMMENTS = 100;
	
	public static boolean isTorch; //used in a very ugly trick!!
	
    private static Bitmap icon = Bitmap.getBitmapResource("application-icon.png");
    private static Bitmap newCommentsIcon = Bitmap.getBitmapResource("application-icon-new.png");
    
    /**
     * Initializes the application information from the descriptor and the
     * command-line arguments.  This method must be called on startup.
     * @param args Arguments
     */
    public static synchronized void initialize(String args[]) {
    	Log.trace("WordPress Info inizialized");
    	//#ifdef VER_6.0.0
    	isTorch = true;
    	//#else
    	isTorch = false;
    	//#endif
    }
   
    public static Bitmap getIcon() {
    	return icon;
    }
    
    public static Bitmap getNewCommentsIcon() {
    	return newCommentsIcon;
    }    
}