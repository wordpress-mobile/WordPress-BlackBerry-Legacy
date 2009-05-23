package com.wordpress.xmlrpc;

import java.util.Vector;

public class GetTemplateConn extends BlogConn  {

	private final int blogId;

	public GetTemplateConn(String url, int blogId, String user, String pass){
		super(url, user, pass);
		this.blogId = blogId;
	}

	public void run() {
		try {
			if (blogId < 0) {
				setErrorMessage("Comment doesn't have BlogID");
				notifyObservers(connResponse);
				return;
			}
			Vector args = new Vector();
			args.addElement("");
			args.addElement(String.valueOf(blogId));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement("main");
			Object response = execute("metaWeblog.getTemplate", args);

			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;	
			}
			connResponse.setResponseObject(response);
		}
		catch (Exception cce) {
			setErrorMessage(cce, "Get Template error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Get Template error: Notify error"); 		
		}
	}
}

