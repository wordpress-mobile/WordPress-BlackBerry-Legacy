package com.wordpress.xmlrpc;

import java.util.Vector;

import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;

public class BlogGetActivationStatusConn extends BlogConn  {
	String blogURL;
	
	public BlogGetActivationStatusConn(String xmlrpcURL, String blogURL) {
		super(xmlrpcURL, "", "");
		this.blogURL = blogURL;
	}

	/**
	 * 
	 * @param provider
	 */
	public void run() {
		try{
	        Vector args = new Vector(1);
	        args.addElement(blogURL);

	        Object response = execute("wpcom.getActivationStatus", args);
			if(connResponse.isError()) {
				//se il server xml-rpc è andato in err
				notifyObservers(connResponse);
				return;		
			}
			connResponse.setResponseObject(response);
		} catch (Exception cce) {
			setErrorMessage(cce, "SignUp Error");
		}
		
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("getActivationStatus Conn Notify Error");
		}
	}
}