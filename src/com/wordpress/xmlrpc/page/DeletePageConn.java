package com.wordpress.xmlrpc.page;

import java.util.Vector;

import com.wordpress.xmlrpc.BlogConn;

public class DeletePageConn extends BlogConn {

	private final int pageID;
	private final int blogID;

	public DeletePageConn(String hint, String userHint, String passwordHint, int blogID, int pageID) {
		super(hint, userHint, passwordHint);
		this.pageID = pageID;
		this.blogID = blogID;
	}

	public void run() {
		try {
			if (this.blogID < 0) {
				setErrorMessage("Page doesn't have a BlogId");
				notifyObservers(connResponse);
				return;
			}
			if (this.pageID < 0) {
				setErrorMessage("Page doesn't have an Id");
				notifyObservers(connResponse);
				return;
			}

			Vector args = new Vector(5);
			args.addElement(String.valueOf(this.blogID));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(String.valueOf(this.pageID));

			Object response = execute("wp.deletePage", args);
			if (connResponse.isError()) {
				notifyObservers(connResponse);
				return;
			}

			connResponse.setResponseObject(response);

		} catch (Exception e) {
			setErrorMessage(e, "Delete Error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("DeletePage: Notify error");
		}
	}
}
