package com.wordpress.xmlrpc.comment;

import java.util.Vector;

import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;

public class DeleteCommentConn extends BlogConn  {
	
	private final int commentID;
	private final String blogID;

	public DeleteCommentConn(String hint, String userHint, String passwordHint, String blogID, int commentID){
		super(hint, userHint, passwordHint);
		this.blogID = blogID;
		this.commentID = commentID;
	}
	
	
	public void run() {
		try {
			if (Integer.parseInt(blogID) < 0 ) {
				 setErrorMessage("Error Missing Blog Identification");
				 notifyObservers(connResponse);
		         return;
			}
			if (this.commentID < 0 ) {
				 setErrorMessage("Error CommentId");
				 notifyObservers(connResponse);
		         return;
			}
				Vector args = new Vector(4);
		        args.addElement(this.blogID);
				args.addElement(mUsername);
		        args.addElement(mPassword);
		        args.addElement(String.valueOf(this.commentID));

		        Object response = execute("wp.deleteComment", args);
				if(connResponse.isError()) {
					notifyObservers(connResponse);
					return;		
				}

				connResponse.setResponseObject(response);

			} catch (Exception e) {
				setErrorMessage(e, "DeleteComment error: Invalid server response");
			}
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				Log.error("DeleteComment error: Notify error"); 
			}
		}
}
