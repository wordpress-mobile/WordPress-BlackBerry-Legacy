package com.wordpress.xmlrpc;

import java.util.Vector;

import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;

public class BlogSignUpConn extends BlogConn  {
	
	String blogName;
	String email;
	
	public BlogSignUpConn(String URL, String userHint, String passwordHint, String blogName, String email ) {
		super(URL, userHint, passwordHint);
		this.blogName = blogName;
		this.email = email;
	}

	/**
	 * 
	 * @param provider
	 */
	public void run() {
		try{
	        Vector args = new Vector(4);
	        args.addElement(blogName);
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(email);

	        Object response = execute("wpcom.registerAccount", args);
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
			Log.error("BlogSignUp Conn Notify Error");
		}
	}
}