package com.wordpress.utils.http;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import com.wordpress.utils.http.Cookie.CookieException;
import com.wordpress.utils.log.Log;

public class SimpleCookieManager {
    private Hashtable store;

    private static final String SET_COOKIE_SEPARATOR="; ";
    private static final String COOKIE = "Cookie";
    private static final char DOT = '.';
    
    public SimpleCookieManager() {
    	store = new Hashtable();
    }
        
    public void storeCookie(HttpConnection conn, String cookieString) {
    	
    	// let's determine the domain from where these cookies are being sent
    	String domain = getDomainFromHost(conn.getHost());

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
		try {
			Cookie tmp = new Cookie(cookieString);
			domainStore.put(tmp.getName(), tmp);
		} catch (Exception e) {
			Log.error(e, "Error while parsing the current cookie string: "+ cookieString );
		}
    	
    }
	
    public void addCookiesToRequest(HttpConnection conn) throws IOException, CookieException {
    	
    	String domain = getDomainFromHost(conn.getHost());
    	String path = conn.getURL();
    	Hashtable domainStore = (Hashtable)store.get(domain);
    	if (domainStore == null) return;
    	StringBuffer cookieStringBuffer = new StringBuffer();
    	
    	Enumeration cookieNames = domainStore.keys();
    	while(cookieNames.hasMoreElements()) {
    		String cookieName = (String)cookieNames.nextElement();
    		Cookie cookie = (Cookie)domainStore.get(cookieName);
    		if ( cookie.matches(path) ) {
    			cookieStringBuffer.append(cookie.getCookieString());
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

    private String getDomainFromHost(String host) {
    	if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
    		return host.substring(host.indexOf(DOT) + 1);
    	} else {
    		return host;
    	}
    }

}
