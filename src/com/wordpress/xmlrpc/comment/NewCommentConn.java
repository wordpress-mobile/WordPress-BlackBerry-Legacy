package com.wordpress.xmlrpc.comment;

import java.util.TimeZone;
import java.util.Vector;

import java.util.Hashtable;

import com.wordpress.model.Comment;
import com.wordpress.xmlrpc.BlogConn;

public class NewCommentConn extends BlogConn  {

	private Comment comment;

	public NewCommentConn(String hint, int blogId, String userHint, String passwordHint,  TimeZone tz, Comment ncomment){
		super(hint, userHint, passwordHint, tz);
		this.comment=ncomment;
	}

	public void run() {
		try {
			if (comment.getBlogId() == -1) {
				setErrorMessage("Comment doesn't have BlogID");
				notifyObservers(connResponse);
				return;
			}
			if (comment.getPostID() == -1) {
				setErrorMessage("Comment doesn't have PostID");
				notifyObservers(connResponse);
				return;
			}

			Hashtable vcomment = new Hashtable(10);
			if (comment.getCommentParent() != 0) {
				vcomment.put("comment_parent", String.valueOf(comment.getCommentParent()));
			}
			if (comment.getContent() != null) {
				vcomment.put("content", comment.getContent());
			}
			if (comment.getAuthor() != null) {
				vcomment.put("author", comment.getAuthor());
			}
			if (comment.getAuthorUrl() != null) {
				vcomment.put("author_url", comment.getAuthorUrl());
			}
			if (comment.getAuthorEmail() != null) {
				vcomment.put("author_email", comment.getAuthorEmail());
			}        
			Vector args = new Vector(5);
			args.addElement(String.valueOf(comment.getBlogId()));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(String.valueOf(comment.getPostID()));
			args.addElement(vcomment);
			Object response = execute("wp.newComment", args);

			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;	
			}
			connResponse.setResponseObject(response);
		}
		catch (Exception cce) {
			setErrorMessage(cce, "NewComment error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("NewComment error: Notify error"); 		
		}
	}
}