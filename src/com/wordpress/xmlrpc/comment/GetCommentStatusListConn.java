package com.wordpress.xmlrpc.comment;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class GetCommentStatusListConn extends BlogConn  {
	
	private int blogId=-1;
	
	public GetCommentStatusListConn(String hint, int blogId, String userHint, String passwordHint){
		super(hint, userHint, passwordHint);
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
            connResponse.setResponseObject(commentData);
 			}
			catch (Exception e) {
				setErrorMessage(e, "GetCommentStatusList error: Invalid server response");
	        }
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("GetCommentStatusList error: Notify error"); 
			}
			
		}
	}