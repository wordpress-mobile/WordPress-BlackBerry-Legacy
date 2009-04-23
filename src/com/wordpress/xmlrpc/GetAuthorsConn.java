package com.wordpress.xmlrpc;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Author;

public class GetAuthorsConn extends BlogConn  {
	
	
	private final int blogID;

	public GetAuthorsConn(String hint, int blogID, String userHint, String passwordHint, TimeZone tz) {
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
	}

	public void run() {
		  if (blogID < 0) {
			 setErrorMessage("Author does not have a BlogId");
			 notifyObservers(connResponse);
	         return;
	        }
	        
	        Vector args = new Vector(3);
	        args.addElement(String.valueOf(blogID));
	        args.addElement(mUsername);
	        args.addElement(mPassword);

	        Object response = execute("wp.getAuthors", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}

			try{

				Vector temp = (Vector) response;
				Author[] myauth= new Author[temp.size()];
				
				Hashtable tempData = null;
				for (int i=0; i<temp.size(); i++){
					tempData = (Hashtable) temp.elementAt(i);

					
					System.out.println("userid: "+(String) tempData.get("user_id"));
					System.out.println("user_login: "+(String) tempData.get("user_login"));
					System.out.println("display_name: "+(String) tempData.get("display_name"));
					System.out.println("user_email: "+(String) tempData.get("user_email"));
					
					//System.out.println("meta_value: "+ tempData.get("meta_value"));
					
					
					int userID= Integer.parseInt((String) tempData.get("user_id"));
					String userLogin=(String) tempData.get("user_login");
					String displayName= (String) tempData.get("display_name");
					String userEmail= (String) tempData.get("user_email");
					byte[] meta= (byte[]) tempData.get("meta_value"); //TODO what is metavalue?
					
					Author auth= new Author(blogID,userID, userLogin, displayName, userEmail,meta);
					
					myauth[i]=auth;
				}
							
				connResponse.setResponseObject(myauth);
		} catch (Exception cce) {
			setErrorMessage(cce, "Authors error");
		}
		
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("notify error"); //TODO handle errors here...
		}
	}
}