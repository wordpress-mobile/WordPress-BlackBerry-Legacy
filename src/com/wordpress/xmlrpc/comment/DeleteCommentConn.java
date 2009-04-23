package com.wordpress.xmlrpc.comment;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class DeleteCommentConn extends BlogConn  {
	
	private final int commentID;
	private final int blogID;

	public DeleteCommentConn(String hint, int blogID, String userHint, String passwordHint,  TimeZone tz, int commentID){
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
		this.commentID = commentID;
	}
	
	
	public void run() {
		try {
			if (this.blogID < 0 ) {
				 setErrorMessage("Error BlogId");
				 notifyObservers(connResponse);
		         return;
			}
			if (this.commentID < 0 ) {
				 setErrorMessage("Error CommentId");
				 notifyObservers(connResponse);
		         return;
			}
				Vector args = new Vector(5);
		        args.addElement(String.valueOf(this.blogID));
				args.addElement(mUsername);
		        args.addElement(mPassword);
		        args.addElement(String.valueOf(this.commentID));

		        Object response = execute("wp.deleteComment", args);
				if(connResponse.isError()) {
					//se il server xml-rpc è andato in err
					notifyObservers(connResponse);
					return;		
				}

				connResponse.setResponseObject(response);

			} catch (Exception e) {
				setErrorMessage(e, "delete error");
				notifyObservers(connResponse);
			}
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("notify error"); //TODO handle errors here...
			}
		}
}
