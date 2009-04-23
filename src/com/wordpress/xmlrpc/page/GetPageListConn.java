package com.wordpress.xmlrpc.page;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Page;
import com.wordpress.xmlrpc.BlogConn;

public class GetPageListConn extends BlogConn  {
	
	private final int blogID;	
	
	public GetPageListConn(String hint, int blogId, String userHint, String passwordHint, TimeZone tz) {
		super(hint, userHint, passwordHint, tz);
		this.blogID=blogId;
	}

	public void run() {
		 
		 if (this.blogID < 0) {
			 setErrorMessage("Page does not have a BlogId");
			 notifyObservers(connResponse);
	         return;
	        }
	        
	        Vector args = new Vector(3);
	        args.addElement(String.valueOf(this.blogID));
	        args.addElement(mUsername);
	        args.addElement(mPassword);

	        Object response = execute("wp.getPageList", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}

			try{

				Vector temp = (Vector) response;
				Page[] mypage= new Page[temp.size()];
				
				Hashtable tempData = null;
				for (int i=0; i<temp.size(); i++){
					tempData = (Hashtable) temp.elementAt(i);

					System.out.println("pageId: "+ Integer.parseInt((String) tempData.get("pageId")));
					System.out.println("title: "+ (String) tempData.get("Title"));
					System.out.println("page_parent_id: "+ Integer.parseInt((String) tempData.get("page_parent_id")));
					System.out.println("date: "+ (String)tempData.get("dateCreated"));
					System.out.println("date_created_gmt: " + (String)tempData.get("date_created_gmt"));
					
				}
							
			connResponse.setResponseObject(mypage);
			notifyObservers(connResponse);
			
		} catch (Exception cce) {
			setErrorMessage(cce, "PageList error");
		}try {
			   notifyObservers(connResponse);
		  } catch (Exception e) {
		   System.out.println("notify error");
		  }
	}

}