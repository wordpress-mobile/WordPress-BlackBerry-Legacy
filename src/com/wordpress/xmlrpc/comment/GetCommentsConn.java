package com.wordpress.xmlrpc.comment;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

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
		
		try{
			
			connResponse = new BlogConnResponse();

			//retrive the comments of the blog
	        Vector comments = getComments(blogId, postID, status, offset, number);
			connResponse.setResponseObject(comments);
		
		} catch (Exception cce) {
			setErrorMessage(cce, "loadPosts error");	
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Recent Post Notify Error");
		}
	}
	
	}