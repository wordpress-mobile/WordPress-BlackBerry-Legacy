package com.wordpress.utils;

import java.util.Vector;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.browser.PostData;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.synchronization.UIDGenerator;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.utils.log.Log;

public class Tools {
	
	
	
	public static synchronized void openWordPressSignUpURL(String refScreen){
		HttpHeaders headers = new HttpHeaders();
    	headers.addProperty("User-Agent", "wp-blackberry/"+ Tools.getAppVersion());
    	Tools.getBrowserSession(WordPressInfo.BB_APP_SIGNUP_URL,"/wp-blackberry/"+refScreen, headers, null);
	}
	
	
	
	/**
	 * Groups numbers by inserting 'separator' after every group of 'size' digits,
	 * starting from the right.
	 */
	public static synchronized String groupDigits(String value, int size, char separator) {

		StringBuffer r = new StringBuffer(value.length() + 10);
		int ndx = 0;
		int len = value.length() - 1;
		int mod = len % size;
		while (ndx < len) {
			r.append(value.charAt(ndx));
			if (mod == 0) {
				r.append(separator);
				mod = size;
			}
			mod--;
			ndx++;
		}
		r.append(value.charAt(ndx));
		return r.toString();
	}
	
	
	
	/**
	 * Invoke the default browser on the BlackBerry smartphone
	 * 
	 * @param URL 
	 * @return
	 * 
	 *  In addition, an app can invoke the Internet Browser, the Wireless Application Protocol (WAP) Browser, 
	 *  the BlackBerry Browser, the Wi-Fi Browser, or the BlackBerry Unite! Browser
	 *  see DB-00701
	 */
	public static synchronized BrowserSession getBrowserSession(String URL) {
		// Get the default sessionBrowserSession
    	BrowserSession browserSession = Browser.getDefaultSession();
    	// now launch the URL
    	browserSession.displayPage(URL);
    	// The following line is a work around to the issue found in
    	// version 4.2.0
    	browserSession.showBrowser();
		return browserSession;
	}
	
	public static synchronized BrowserSession getBrowserSession(String URL, String referrer, HttpHeaders headers, PostData postData) {
		// Get the default sessionBrowserSession
    	BrowserSession browserSession = Browser.getDefaultSession();
    	// now launch the URL
    	browserSession.displayPage(URL, referrer, headers, postData);
    	// The following line is a work around to the issue found in
    	// version 4.2.0
    	browserSession.showBrowser();
		return browserSession;
	}

	
	public static synchronized String getAppVersion() {
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
	
	/**
	 * check the path of the file xmlrpc.php into the url string
	 */
	public static String checkURL(String url){
		Log.trace(">>> checkURL");
		Log.trace("in URL: "+url);
		if(url == null || url.trim().length() == 0 ) {
			return null;
		}
			
		if (url.endsWith("xmlrpc.php")){
			
		} else {
			if (!url.endsWith("/")){
				url+="/";
			}
			url+="xmlrpc.php";
		}
		Log.trace("out URL: "+url);	
		return url;
	}
	
  public static double round(final double num) {
	    final double floor = Math.floor(num);
	    if (num - floor >= 0.5) {
	      return Math.ceil(num);
	    } else {
	      return floor;
	    }
	  }
	
	public static long generateDeviceUUID() {
		//return (new Random()).nextLong();
		return UIDGenerator.makeLUID(UIDGenerator.getUniqueScopingValue(), UIDGenerator.getUID());
	}
	
	 public static int decodeInt(Object input){	 
		 if (input instanceof String){
			 return Integer.parseInt((String)input);
		 } else if (input instanceof Integer){
			 return ((Integer)input).intValue();			 
		 } else if (input instanceof Boolean) {
			 if (((Boolean)input).booleanValue())
				 return 1;
			 else 
				 return 0;
		 }
		 return -1;		 
	 }
	 
	 public static String decodeString(Object input){
		 if(input == null ) return null;
		 if (input instanceof String)
			 return (String) input;
		  else 
			 return String.valueOf(input);			 			 
	 }
	
	  /**
	   * Convert a vector to a string array.
	   * 
	   * @param v
	   *          vector to convert
	   * @return the string array
	   */
	  public static String[] toStringArray(final Vector v) {
	    final String[] res = new String[v.size()];
	    v.copyInto(res);
	    return res;
	  }
	  
	  public static synchronized String getMemoryStats() {
		 Runtime rt = java.lang.Runtime.getRuntime();
     	 long totalMem=rt.totalMemory();
     	 long freeMem=rt.freeMemory();
     	 
     	 float a = (float)(totalMem);
     	 float b = 1048576f;
     	 String totalMemMB=Float.toString(a/b);
     	 
     	 a = (float)(freeMem);
     	 String totalFreeMemMB=Float.toString(a/b);
     	 
          return "Total MB heap: " + totalMemMB + "\nTotal MB free: " + totalFreeMemMB;
	  }
}