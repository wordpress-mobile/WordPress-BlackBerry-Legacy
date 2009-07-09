package com.wordpress.xmlrpc;

import java.util.Hashtable;
import java.util.Vector;

import org.kxmlrpc.XmlRpcClient;

import com.wordpress.model.Blog;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;

public class BlogAuthConn extends BlogConn  {

	private String xPingbackString = null;
	
	public BlogAuthConn(String hint,String userHint, String passwordHint) {
		super(hint, userHint, passwordHint);
	}

	
	private void resetConnectionResponse() {
		connResponse.setError(false);
		connResponse.setStopped(false);
		connResponse.setResponse("");
		connResponse.setResponseObject(null);
	}
	
	
	private void setXPingBack(){
		if( responseHeaders.get("X-Pingback") != null ) {
			xPingbackString = (String)responseHeaders.get("X-Pingback");
		} 
	}
	
	private Object guessUrl(){
		Vector args;
		resetConnectionResponse();
		args = new Vector(2);
		args.addElement(this.mUsername);
		args.addElement(this.mPassword);
		
		// blogger_getUsersBlogs
		Object response = execute("wp.getUsersBlogs", args);
		if(connResponse.isStopped()) return null; //if the user has stopped the connection
		setXPingBack(); //set the pingback url if any
		if(connResponse.isError()) {
			resetConnectionResponse();		
			//try with old blogger xml-rpc call
			args.insertElementAt("",0); //blogger api need key
			response = execute("blogger.getUsersBlogs", args);
			if(connResponse.isStopped()) return null; //if the user has stopped the connection
			setXPingBack();
			if(connResponse.isError()) {
				response = null; //if still error there no reset response
			}
		}
		return response;
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
		
		/*
		 * 1. try the user inserted url as xmlrpc endpoint
		 * 2. try the X-ping back header as xmlrpc endpoint
		 * 2b. try syntax guess
		 */
		Object response = guessUrl();
		if(connResponse.isError()) {
			
			if(xPingbackString != null ){
				//2
				urlConnessione = xPingbackString;
			} else {
				//2b
				urlConnessione = Tools.checkURL(urlConnessione);
			}			
			mConnection = new XmlRpcClient(urlConnessione);
			response = guessUrl();
			
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
			
				String url = null;
				if ( blogData.get("xmlrpc") != null ) {
					url = (String)blogData.get("xmlrpc");
				} else {
					Log.trace("blog xmlrpc response url was null");
					Log.trace("blog xmlrpc url was set to connection url: "+urlConnessione);
					url = urlConnessione; 
				}
				
				if(url == null || url.equalsIgnoreCase(""))
					continue; //skip this blog
				
				Blog currentBlog= new Blog((String)blogData.get("blogid") , (String)blogData.get("blogName"),
						(String)blogData.get("url"), url, this.mUsername, this.mPassword);
				
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