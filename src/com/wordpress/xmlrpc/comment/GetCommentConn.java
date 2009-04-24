package com.wordpress.xmlrpc.comment;

import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Comment;
import com.wordpress.xmlrpc.BlogConn;

public class GetCommentConn extends BlogConn  {
	
	
	private final int commentID;
	private final int blogID;

	public GetCommentConn(String hint, int blogID, String userHint, String passwordHint,  TimeZone tz, int commentID){
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
		this.commentID = commentID;
	}
	
	public void run() {
		try {
			
			Vector args = new Vector(4);
	        args.addElement(String.valueOf(this.blogID));
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(String.valueOf(this.commentID));
		
	        Object response = execute("wp.getComment", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
			
		
            Hashtable returnCommentData = (Hashtable) response;
            
            int commentID=Integer.parseInt((String)returnCommentData.get("comment_id"));
	        int commentParent=Integer.parseInt((String) returnCommentData.get("parent"));
            String status=(String) returnCommentData.get("status");
            String authorID= (String) returnCommentData.get("author_Id");
            String authorUrl=((String) returnCommentData.get("author_url"));
            String authorEmail=((String) returnCommentData.get("author_email"));
            String content= (String) returnCommentData.get("content");
            
            Comment comment = new Comment(commentParent, content, authorID, authorUrl, authorEmail, status);
            comment.setID(commentID);
            comment.setLink((String) returnCommentData.get("link"));
            comment.setPostID(Integer.parseInt((String)returnCommentData.get("post_id")));
            comment.setPostTitle((String) returnCommentData.get("title"));
            comment.setAuthor_Ip((String) returnCommentData.get("author_ip"));
            comment.setDate_created_gmt((Date) returnCommentData.get("dateCreated"));
            
            connResponse.setResponseObject(comment);
			}
			catch (Exception e) {
				setErrorMessage(e, "GetComment error: Invalid server response");
	        }
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("GetComment error: Notify error"); 
			}
		}
	}