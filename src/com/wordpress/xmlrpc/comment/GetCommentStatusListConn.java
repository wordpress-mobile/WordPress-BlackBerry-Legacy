package com.wordpress.xmlrpc.comment;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class GetCommentStatusListConn extends BlogConn  {
	
	private int blogId=-1;
	
	public GetCommentStatusListConn(String hint, int blogId, String userHint, String passwordHint,  TimeZone tz){
		super(hint, userHint, passwordHint, tz);
		this.blogId=blogId;
	}
	
	public void run() {
		try {
			
			Vector args = new Vector(4);
	        args.addElement(String.valueOf(blogId));
	        args.addElement(mUsername);
	        args.addElement(mPassword);
		
	        Object response = execute("wp.getCommentStatusList", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}		
            Hashtable commentData = (Hashtable) response;
            //TODO handle response
            connResponse.setResponseObject(commentData);
 			}
			catch (Exception e) {
				setErrorMessage(e, "Invalid server response");
				notifyObservers(connResponse);
	        }
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("notify error"); //TODO handle errors here...
			}
			
		}
	}