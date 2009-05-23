package com.wordpress.xmlrpc.comment;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class GetCommentCountConn extends BlogConn  {
	
	
	private final int blogID;
	private final int postID;

	public GetCommentCountConn(String hint, int blogId, String userHint, String passwordHint, int postID){
		super(hint, userHint, passwordHint);
		this.blogID = blogId;
		this.postID = postID;
	}
	
	public void run() {
		try {
			if (this.blogID < 0) {
				 setErrorMessage("Error BlogId");
				 notifyObservers(connResponse);
		         return;
			}
			if (this.postID < 0) {
				 setErrorMessage("Error PostID");
				 notifyObservers(connResponse);
		         return;
			}
			
			Vector args = new Vector(5);
			
	        args.addElement(String.valueOf(this.blogID));
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(String.valueOf(this.postID));
		
	        Object response = execute("wp.getCommentCount", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
			
		
            Hashtable commentData = (Hashtable) response;
            connResponse.setResponseObject(commentData);

            }
			catch (Exception e) {
				setErrorMessage(e, "GetCommentCount error: Invalid server response");
	        }
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("GetCommentCount error: Notify error"); 
			}
			
		}
	}