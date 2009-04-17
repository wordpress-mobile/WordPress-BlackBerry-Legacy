package com.wordpress.xmlrpc;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Post;

public class DeletePostConn extends BlogConn  {
	
	private Post aPost=null;

	public DeletePostConn(String hint,	String userHint, String passwordHint, TimeZone tz, Post mPost) {
		super(hint, userHint, passwordHint, tz);
		this.aPost=mPost;
	}

	/**
	 * cancella un post da remoto
	 * @param provider
	 */
	public void run() {
		try{
		
        Vector args = new Vector(3);
        args.addElement(""); //appkkey
        args.addElement(aPost.getId());
        args.addElement(mUsername);
        args.addElement(mPassword);


        Object response = execute("blogger.deletePost", args);
		if(connResponse.isError()) {
			//se il server xml-rpc è andato in err
			notifyObservers(connResponse);
			return;		
		}

			connResponse.setResponseObject(response);
			notifyObservers(connResponse);
		} catch (Exception cce) {
			setErrorMessage(cce, "delete error");
			notifyObservers(connResponse);
		}
	}
}