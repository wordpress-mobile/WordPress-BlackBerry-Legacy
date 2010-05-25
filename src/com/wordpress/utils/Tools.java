package com.wordpress.utils;

import java.util.Vector;

import javax.microedition.content.ContentHandler;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.blackberry.api.browser.PostData;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.synchronization.UIDGenerator;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.SelectorPopupScreen;

public class Tools {
		
	public static synchronized void openFileWithExternalApp(String fileURL){
		
		ResourceBundle resourceBundle = WordPressCore.getInstance().getResourceBundle();
		
		// Create the Invocation with the file URL
		Invocation invoc = new Invocation(fileURL);
		invoc.setResponseRequired(false); // We don't require a response
		invoc.setAction(ContentHandler.ACTION_OPEN);
		
		if(!fileURL.startsWith("file://")){
			Tools.getNativeBrowserSession(fileURL);
			return;
		} 
		
		// Get access to the Registry and pass it the Invocation
		Registry registry = Registry.getRegistry("com.wordpress.utils.Tools");
		try {
			ContentHandler[] candidates = new ContentHandler[0];
			candidates = registry.findHandler(invoc);
		
			if(candidates.length == 0) {
				Tools.getNativeBrowserSession(fileURL);
				return;
			}

			String[] appNames = new String[candidates.length];
			for (int i = 0; i < candidates.length; i++) {
				appNames[i] = candidates[i].getAppName();
			}
			String title = resourceBundle.getString(WordPressResource.MENUITEM_OPEN_IN);
			SelectorPopupScreen selScr = new SelectorPopupScreen(title, appNames);
			selScr.pickBlog();
			int selection = selScr.getSelectedBlog();
			if(selection != -1) {
				invoc.setID(candidates[selection].getID());
				registry.invoke(invoc);                
			}
			
	
		} catch (Exception e) {
			Log.error(e, "Error while finding a chapi endpoint");
			Tools.getNativeBrowserSession(fileURL);
			return;
		}
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
	 * Finds installed Browsers and asks user which one should be used to open the page
	 */
	public static synchronized void openURL(String url){
		
		ResourceBundle resourceBundle = WordPressCore.getInstance().getResourceBundle();
		
		// Create the Invocation with the file URL
		Invocation invoc = new Invocation(url);
		invoc.setResponseRequired(false); // We don't require a response
		invoc.setType("text/html");
		invoc.setAction(ContentHandler.ACTION_OPEN);
		
		if(!url.startsWith("http")){
			Tools.getNativeBrowserSession(url);
			return;
		} 
		
		// Get access to the Registry and pass it the Invocation
		Registry registry = Registry.getRegistry("com.wordpress.utils.Tools");		
		try {
			ContentHandler[] candidates = new ContentHandler[0];
			candidates = registry.findHandler(invoc);
			
			//check the #s of apps that can handle html file
			if(candidates.length == 0) {
				Tools.getNativeBrowserSession(url);
				return;
			}
					
			//remove the net.rim.bb.mediacontenthandler from content handler list
			Vector newCandidates = new Vector();
			for (int i = 0; i < candidates.length; i++) {
				if(candidates[i].getID().equalsIgnoreCase("net.rim.bb.mediacontenthandler")) {
					
				} else { 
					newCandidates.addElement(candidates[i]);
				}
			}
			
			candidates = new ContentHandler[newCandidates.size()];
			newCandidates.copyInto(candidates);

			if(candidates.length == 0) {
				Tools.getNativeBrowserSession(url);
				return;
			}

			//add the native browser at the end of the list		
			String[] appNames = new String[candidates.length+1];
			for (int i = 0; i < appNames.length-1; i++) {
				appNames[i] = candidates[i].getAppName();
			}
			appNames[appNames.length-1] = resourceBundle.getString(WordPressResource.NATIVE_BROWSER);
				
			String title = resourceBundle.getString(WordPressResource.MENUITEM_OPEN_IN);
			SelectorPopupScreen selScr = new SelectorPopupScreen(title, appNames);
			selScr.pickBlog();
			int selection = selScr.getSelectedBlog();
			if(selection != -1) {
				if(selection < appNames.length-1) {
					invoc.setID(candidates[selection].getID());
					registry.invoke(invoc);
				} else {
					//native browser
					Tools.getNativeBrowserSession(url);
				}
			}
	
		} catch (Exception e) {
			Log.error(e, "Error while finding a chapi endpoint");
			Tools.getNativeBrowserSession(url);
			return;
		}
	}	
	
	
	public static synchronized BrowserSession getNativeBrowserSession(String URL) {
		// Get the default sessionBrowserSession
    	BrowserSession browserSession = Browser.getDefaultSession();
    	// now launch the URL
    	browserSession.displayPage(URL);
    	// The following line is a work around to the issue found in
    	// version 4.2.0
    	browserSession.showBrowser();
		return browserSession;
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
	 
	public static synchronized BrowserSession getNativeBrowserSession(String URL, String referrer, HttpHeaders headers, PostData postData) {
		// Get the default sessionBrowserSession
    	BrowserSession browserSession = Browser.getDefaultSession();
    	// now launch the URL
    	browserSession.displayPage(URL, referrer, headers, postData);
    	// The following line is a work around to the issue found in
    	// version 4.2.0
    	browserSession.showBrowser();
		return browserSession;
	}
*/
	
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

}