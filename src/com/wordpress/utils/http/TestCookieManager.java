package com.wordpress.utils.http;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;

public class TestCookieManager {
    private Hashtable store;

    private static final String SET_COOKIE = "Set-Cookie";
    private static final String COOKIE_VALUE_DELIMITER = ";";
    private static final String PATH = "path";
    private static final String EXPIRES = "expires";
    private static final String DATE_FORMAT = "EEE, dd-MMM-yyyy hh:mm:ss z";
    private static final String SET_COOKIE_SEPARATOR="; ";
    private static final String COOKIE = "Cookie";
	
    private static final char NAME_VALUE_SEPARATOR = '=';
    private static final char DOT = '.';
    
    public TestCookieManager() {
    	store = new Hashtable();
    }
        
    public void storeCookie(String host, String headerName , String headerValue) {
    	
    	// let's determine the domain from where these cookies are being sent
    	String domain = getDomainFromHost(host);
    	    	
    	Hashtable domainStore; // this is where we will store cookies for this domain
    	
    	// now let's check the store to see if we have an entry for this domain
    	if (store.containsKey(domain)) {
    	    // we do, so lets retrieve it from the store
    	    domainStore = (Hashtable)store.get(domain);
    	} else {
    	    // we don't, so let's create it and put it in the store
    	    domainStore = new Hashtable();
    	    store.put(domain, domainStore);    
    	}
    	    	
    	if (headerName.equalsIgnoreCase(SET_COOKIE)) {
    		try {
    			Hashtable cookie = new Hashtable();
    			String[] st = StringUtils.split(headerValue, COOKIE_VALUE_DELIMITER);

    			for (int i = 0; i < st.length; i++) {
    				String token  = st[i];
    				if ( token.indexOf(NAME_VALUE_SEPARATOR) != -1 ) { //Make sure the '"' is available whitin the current token	
    					String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
    					String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
    					// the specification dictates that the first name/value pair
    					// in the string is the cookie name and value, so let's handle
    					// them as a special case: 
    					if ( i == 0 ) {
    						domainStore.put(name, cookie);
    					} 
    					cookie.put(name, value);
    				}
    			}
    		} catch (Exception e) {
    			Log.error(e, "Error while parsing the current cookies "+ headerValue );
    		}
    	}
    }
	
    public void setCookies(HttpConnection conn) throws IOException {
    	
    	String domain = getDomainFromHost(conn.getHost());
    	String path = conn.getURL();
    	Hashtable domainStore = (Hashtable)store.get(domain);
    	if (domainStore == null) return;
    	StringBuffer cookieStringBuffer = new StringBuffer();
    	
    	Enumeration cookieNames = domainStore.keys();
    	while(cookieNames.hasMoreElements()) {
    		String cookieName = (String)cookieNames.nextElement();
    		Hashtable cookie = (Hashtable)domainStore.get(cookieName);
    		if ( comparePaths( (String)cookie.get(PATH), path) ) {
    		cookieStringBuffer.append(cookieName);
    		cookieStringBuffer.append("=");
    		cookieStringBuffer.append((String)cookie.get(cookieName));
    		if (cookieNames.hasMoreElements()) cookieStringBuffer.append(SET_COOKIE_SEPARATOR);
    		}
    	}
    	
    	try {
    		if( cookieStringBuffer.length() > 0 ) {
    			conn.setRequestProperty(COOKIE, cookieStringBuffer.toString());
    			Log.trace("Cookie set on the connection: "+cookieStringBuffer.toString() );
    		}
    	} catch (java.lang.IllegalStateException ise) {
    		IOException ioe = new IOException("Illegal State! Cookies cannot be set on a URLConnection that is already connected. " 
    				+ "Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");
    		throw ioe;
    	}
    }

    private boolean comparePaths(String cookiePath, String targetPath) {
    	return true;
    	/* if (cookiePath == null) {
    		return true;
    	} else if (cookiePath.equals("/")) {
    		return true;
    	} else if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length())) {
    		return true;
    	} else {
    		return false;
    	 */
    }
    	
    private String getDomainFromHost(String host) {
    	if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
    		return host.substring(host.indexOf(DOT) + 1);
    	} else {
    		return host;
    	}
    }

}
