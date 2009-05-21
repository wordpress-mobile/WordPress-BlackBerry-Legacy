package com.wordpress.xmlrpc.page;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class GetPageListConn extends BlogConn  {

	private final int blogID;	

	public GetPageListConn(String hint, int blogId, String userHint, String passwordHint, TimeZone tz) {
		super(hint, userHint, passwordHint, tz);
		this.blogID=blogId;
	}

	public void run() {

		if (this.blogID < 0) {
			setErrorMessage("Page does not have a BlogId");
			notifyObservers(connResponse);
			return;
		}

		Vector args = new Vector(3);
		args.addElement(String.valueOf(this.blogID));
		args.addElement(mUsername);
		args.addElement(mPassword);

		Object response = execute("wp.getPageList", args);
		if(connResponse.isError()) {
			notifyObservers(connResponse);
			return;		
		}

		try{

			Vector temp = (Vector) response;
			
			connResponse.setResponseObject(temp);
			notifyObservers(connResponse);

		} catch (Exception cce) {
			setErrorMessage(cce, "GetPageList error: Invalid server response");
		}
		try {
			notifyObservers(connResponse); 
		} catch (Exception e) {
			System.out.println("GetPageList error: Notify error");
		}
	}
}