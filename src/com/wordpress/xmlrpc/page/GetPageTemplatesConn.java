package com.wordpress.xmlrpc.page;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Page;
import com.wordpress.xmlrpc.BlogConn;

public class GetPageTemplatesConn extends BlogConn  {
	
	private Page page;
		
	public GetPageTemplatesConn(String hint, int blogId, String userHint, String passwordHint, TimeZone tz, Page page) {
		super(hint, userHint, passwordHint, tz);
		this.page=page;
	}

	public void run() {
		 if (page.getBlogId() == -1) {
			 setErrorMessage("Blog already has a BlogId");
			 notifyObservers(connResponse);
	         return;
	        }
   	        Vector args = new Vector(3);
	        args.addElement(String.valueOf(page.getBlogId()));
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        
	        Object response = execute("wp.getPageTemplates", args);
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

					System.out.println("name: "+ ((String)tempData.get("name")));
					System.out.println("description: "+(String) tempData.get("description"));
				}				
			connResponse.setResponseObject(mypage);
			notifyObservers(connResponse);
			
			//TODO: modificare exception x debug

			
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "loadBlogs error");
		}
			try {
				   notifyObservers(connResponse);
				  } catch (Exception e) {
				   System.out.println("notify error");
				  }	
			  }
}