package com.wordpress.xmlrpc.page;

import java.util.Vector;

import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class GetPagesConn extends BlogConn  {

	private final int blogID;

	public GetPagesConn(String hint, String userHint, String passwordHint, int blogID) {
		super(hint, userHint, passwordHint);
		this.blogID = blogID;
	}

	public void run() {

		if (this.blogID < 0) {
			setErrorMessage("Page does not have a BlogId");
			notifyObservers(connResponse);
			return;
		}
		
		try{
			connResponse = new BlogConnResponse();
	        Vector recentPostTitle = getPages(String.valueOf(blogID));
	        
			connResponse.setResponseObject(recentPostTitle);
		} catch (Exception cce) {
			setErrorMessage(cce, "loadPages error");	
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("Recent Pages Notify Error");
		}
/*
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
		}*/
	}
}