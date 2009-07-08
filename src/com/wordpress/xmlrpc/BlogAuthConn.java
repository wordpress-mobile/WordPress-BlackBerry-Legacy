package com.wordpress.xmlrpc;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.model.Blog;
import com.wordpress.utils.log.Log;

public class BlogAuthConn extends BlogConn  {

	public BlogAuthConn(String hint,String userHint, String passwordHint) {
		super(hint, userHint, passwordHint);
	}

	/**
	 * Load blogs 
	 * 
	 */
	public void run() {
		Vector args;

		args = new Vector(2);
		args.addElement(this.mUsername);
		args.addElement(this.mPassword);
		
		// blogger_getUsersBlogs
		Object response = execute("wp.getUsersBlogs", args);
		if(connResponse.isError()) {
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			//reset the response obj 
			connResponse.setError(false);
			connResponse.setStopped(false);
			connResponse.setResponse("");
			connResponse.setResponseObject(null);
			//try with old add blog conn
			args.insertElementAt("",0); //blogger api need key
			response = execute("blogger.getUsersBlogs", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
		}
	
		try {
			Vector blogs = (Vector) response;
			Blog[] myBlogs= new Blog[blogs.size()];
			
			Hashtable blogData = null;
			for (int i = 0; i < blogs.size(); i++) {
				blogData = (Hashtable) blogs.elementAt(i);
			
				Log.trace("blogId: "+(String) blogData.get("blogid"));
				Log.trace("blogName: "+(String) blogData.get("blogName"));
				Log.trace("blogURL: " +(String) blogData.get("url"));
				Log.trace("blogXMLRPC: " +(String) blogData.get("xmlrpc"));
			
				Blog currentBlog= new Blog((String)blogData.get("blogid") , (String)blogData.get("blogName"),
						(String)blogData.get("url"), (String)blogData.get("xmlrpc"), this.mUsername, this.mPassword);
				
				myBlogs[i]=currentBlog;		
			}		
			
			connResponse.setResponseObject(myBlogs);
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "loadBlogs error");
		} catch (Exception e) {
			setErrorMessage(e, "Invalid server response");
		}
		
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Blog Auth Notify Error"); 
		}
	}
}