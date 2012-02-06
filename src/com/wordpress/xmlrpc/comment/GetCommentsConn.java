package com.wordpress.xmlrpc.comment;

import java.util.Vector;

import org.kxmlrpc.XmlRpcException;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class GetCommentsConn extends BlogConn  {
	
	private String blogId;
	private String  postID;
	private String status="";
	private int offset=0;
	private int number=0; 
	
	public int getNumber() {
		return number;
	}

	public GetCommentsConn(String hint, String blogId, String userHint, String passwordHint,  String postID, String status, int offset, int number){
		super(hint, userHint, passwordHint);
		this.blogId = blogId;
		this.postID = postID;
		this.status = status;
		this.offset = offset;
		this.number = number;
	}
	
	public void run() {
		
		try{
			
			connResponse = new BlogConnResponse();

			//retrive the comments of the blog
	        Vector comments = getComments(blogId, postID, status, offset, number);
	        if(connResponse.isError()) {
	        	// WP < 2.7 doesn't have getComments...
	        	if ( connResponse.getResponseObject() instanceof XmlRpcException) {
	        		XmlRpcException exc = (XmlRpcException) connResponse.getResponseObject();
	        		if (exc.code == -32601 ) { //code returned when getComments not found on xmlrpc endpoint
	        			connResponse.setResponseObject(new XmlRpcException(exc.code, "You are using an old version of WordPress that cannot permitt comment management by xmlrpc"));
	        			//delete recorded error message and set the new one
	        			connResponse.setResponse(_resources.getString(WordPressResource.MESSAGE_CANNOT_MANAGE_COMMENTS));
	        		}
	        	} 
	        }
	        else
	        connResponse.setResponseObject(comments);
		
		} catch (Exception cce) {
			setErrorMessage(cce, "Error while loading comments");	
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("Error while loading comments: Notify Error");
		}
	}
}