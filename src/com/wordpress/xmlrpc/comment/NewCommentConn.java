package com.wordpress.xmlrpc.comment;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.model.Comment;
import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;

public class NewCommentConn extends BlogConn  {

	private Comment comment;
	private final String blogId;

	public NewCommentConn(String url, String user, String password, String blogId, Comment comment){
		super(url, user, password);
		this.blogId = blogId;
		this.comment=comment;
	}

	public void run() {
		try {
			
			if (comment.getPostID() == -1) {
				setErrorMessage("Comment doesn't have PostID");
				notifyObservers(connResponse);
				return;
			}

			Hashtable vcomment = new Hashtable(10);
			if (comment.getParent() != 0) {
				vcomment.put("comment_parent", String.valueOf(comment.getParent()));
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
			args.addElement(blogId);
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(String.valueOf(comment.getPostID()));
			args.addElement(vcomment);
			Object response = execute("wp.newComment", args);

			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;	
			}
			Integer newCommentID = (Integer)response;
			
			//retrive entire comment from server
            args = new Vector(4);
            args.addElement(blogId);
            args.addElement(mUsername);
            args.addElement(mPassword);
            args.addElement(newCommentID);
            response = execute("wp.getComment", args);
    		if(connResponse.isError()) {
    			//se il server xml-rpc è andato in err
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
			Log.error("NewComment error: Notify error"); 		
		}
	}
}