package com.wordpress.xmlrpc.page;

import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class GetPageListConn extends BlogConn  {

	private final String blogID;	

	public GetPageListConn(String hint, String blogId, String userHint, String passwordHint) {
		super(hint, userHint, passwordHint);
		this.blogID = blogId;
	}

	public void run() {

		if (this.blogID == null) {
			setErrorMessage("Page does not have a BlogId");
			notifyObservers(connResponse);
			return;
		}

		Vector args = new Vector(3);
		args.addElement(this.blogID);
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