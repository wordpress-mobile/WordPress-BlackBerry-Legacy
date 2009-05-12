package com.wordpress.xmlrpc;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

public class NewCategoryConn extends BlogConn  {

	private final String catName;
	private final int blogId;
	private final int parentCatID;

	public NewCategoryConn(String url, int blogId, String user, String pass,  TimeZone tz, String catName, int parentCatID){
		super(url, user, pass, tz);
		this.blogId = blogId;
		this.catName=catName;
		this.parentCatID = parentCatID;
	}

	public void run() {
		try {
			if (blogId < 0) {
				setErrorMessage("Comment doesn't have BlogID");
				notifyObservers(connResponse);
				return;
			}
			
			Hashtable values = new Hashtable(5);
			if (parentCatID >  0) {
				values.put("parent_id", String.valueOf(parentCatID));
			}
			if (catName != null && !catName.trim().equals("")) {
				values.put("name", catName);
			}        
			values.put("slug", "");
			values.put("description", "");
						
			Vector args = new Vector(5);
			args.addElement(String.valueOf(blogId));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(values);
			Object response = execute("wp.newCategory", args);

			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;	
			}
			connResponse.setResponseObject(response);
		}
		catch (Exception cce) {
			setErrorMessage(cce, "New Category error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("New Category error: Notify error"); 		
		}
	}
}

