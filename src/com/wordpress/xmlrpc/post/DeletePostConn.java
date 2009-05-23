package com.wordpress.xmlrpc.post;

import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class DeletePostConn extends BlogConn  {
	
	private String postID;

	public DeletePostConn(String hint,	String userHint, String passwordHint, String postID) {
		super(hint, userHint, passwordHint);
		this.postID=postID;
	}

	/**
	 * delete remote post
	 * 
	 */
	public void run() {
		try {

			Vector args = new Vector(3);
			args.addElement(""); // appkkey
			args.addElement(postID);
			args.addElement(mUsername);
			args.addElement(mPassword);

			execute("blogger.deletePost", args);

			if (connResponse.isError()) {
				// if there are errors in xml-rpc call
				notifyObservers(connResponse);
				return;
			}
			connResponse.setResponseObject(postID);

		} catch (Exception cce) {
			setErrorMessage(cce, "delete error");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Delete Post Notify Error");
		}
	}
}