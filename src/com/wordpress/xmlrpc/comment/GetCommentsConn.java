package com.wordpress.xmlrpc.comment;

import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Comment;
import com.wordpress.xmlrpc.BlogConn;

public class GetCommentsConn extends BlogConn  {
	
	private int blogId;
	private int  postID=-1;
	private String status="";
	private int offset=0;
	private int number=0; 
	
	public GetCommentsConn(String hint, int blogId, String userHint, String passwordHint,  TimeZone tz,
			int postID, String status, int offset, int number){
		super(hint, userHint, passwordHint, tz);
		this.blogId=blogId;
		this.postID=postID;
		this.status=status;
		this.offset=offset;
		this.number=number;
	}
	
	public void run() {
		try {
			
			Hashtable StructData = new Hashtable(5);
			if (postID > 0) {
	            StructData.put("post_id", String.valueOf(postID));
	        } else {
	        	
	        }
			
            StructData.put("comment_status", status);
            
			if (offset!=0 ) {
	            StructData.put("offset", String.valueOf(offset));
	        }
			if (number != 0) {
	            StructData.put("number", String.valueOf(number));
	        }
			
			Vector args = new Vector(5);
	        args.addElement(String.valueOf(blogId));
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(StructData);
		
	        Object response = execute("wp.getComments", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
			
			connResponse.setResponseObject((Vector)response);
			}
		
			catch (Exception e) {
				setErrorMessage(e, "GetComments error: Invalid server response");
	        }
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("GetComments error: Notify error"); 
			}
			
		}
	}