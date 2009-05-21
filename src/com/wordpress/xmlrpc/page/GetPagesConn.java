package com.wordpress.xmlrpc.page;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class GetPagesConn extends BlogConn  {

	private final int blogID;

	public GetPagesConn(String hint, String userHint, String passwordHint, TimeZone tz,  int blogID) {
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
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

		Object response = execute("wp.getPages", args);
		if(connResponse.isError()) {
			notifyObservers(connResponse);
			return;		
		}
		try{
			Vector returnedPages = (Vector) response;
			connResponse.setResponseObject(returnedPages);
		} catch (Exception cce) {
			setErrorMessage(cce, "GetPages error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("GetPages error: Notify error"); 
		}
	}
}