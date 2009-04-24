package com.wordpress.xmlrpc.page;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class GetPageStatusListConn extends BlogConn  {


	private final int blogID;

	public GetPageStatusListConn(String hint, int blogID, String userHint, String passwordHint, TimeZone tz) {
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
	}

	public void run() {
		try{

			if (blogID < 0) {
				setErrorMessage("Blog already has a BlogId");
				notifyObservers(connResponse);
				return;
			}
			Vector args = new Vector(3);
			args.addElement(String.valueOf(blogID));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getPageStatusList", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}

			Hashtable statusList = (Hashtable) response;

			Enumeration elements = statusList.keys();
			for ( ; elements.hasMoreElements() ;) {
				String value = (String) elements.nextElement();
				System.out.println("key: " +value);
			}

			elements = statusList.elements();
			for ( ; elements.hasMoreElements() ;) {
				String value = (String) elements.nextElement();
				System.out.println("value: "+value);
			}

			connResponse.setResponseObject(statusList);
		} catch (Exception cce) {
			setErrorMessage(cce, "GetPageStatulList error: Invalid server response");
		}

		try {
			notifyObservers(connResponse); 
		} catch (Exception e) {
			System.out.println("GetPageStatulList error: Notify error");
		}	
	}
}