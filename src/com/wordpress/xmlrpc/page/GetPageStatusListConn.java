package com.wordpress.xmlrpc.page;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Page;
import com.wordpress.xmlrpc.BlogConn;

public class GetPageStatusListConn extends BlogConn  {
	
	private Page page;
		
	public GetPageStatusListConn(String hint, int blogId, String userHint, String passwordHint, TimeZone tz, Page page) {
		super(hint, userHint, passwordHint, tz);
		this.page=page;
	}

	public void run() {
		try{
			
		 if (page.getBlogId() == -1) {
			 setErrorMessage("Blog already has a BlogId");
			 notifyObservers(connResponse);
	         return;
	        }
   	        Vector args = new Vector(3);
	        args.addElement(String.valueOf(page.getBlogId()));
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        
	        Object response = execute("wp.getPageStatusList", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
			
            Hashtable PageData = (Hashtable) response;

            page.setPageStatus(String.valueOf(PageData.get("page_status")));

			//TODO: modificare exception x debug
            
		} catch (Exception e) {
			setErrorMessage(e, "loadBlogs error");
			notifyObservers(connResponse);
		}
	}
}