package com.wordpress.xmlrpc.page;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Page;
import com.wordpress.xmlrpc.BlogConn;

public class EditPageConn extends BlogConn  {
	
	private Page page;
	private boolean isPublished=false;
	
	public EditPageConn(String hint, int blogId, String userHint, String passwordHint, TimeZone tz, Page page, boolean isPublished) {
		super(hint, userHint, passwordHint, tz);
		this.page=page;
		this.isPublished=isPublished;
	}

	public void run() {
		try{
		 if (page.getID() == -1) {
			 setErrorMessage("Page does not have an Id");
			 notifyObservers(connResponse);
	         return;
	        }

		 Hashtable content = new Hashtable(15);
	        if (page.getTitle() != null) {
	            content.put("title", page.getTitle());
	        }
	        if (page.getWpSlug() != null) {
	            content.put("wp_slug", page.getWpSlug());
	        }
	        if (page.getWpPassword() != null) {
	            content.put("wp_password", page.getWpPassword());
	        }
	        if (page.getWpPageParentID() != -1) {
	            content.put("wp_parent_id", new Integer(page.getWpPageParentID()));
	        }
	        if(page.getWpPageOrder()!= -1) {
	        	content.put("wp_page_order", new Integer(page.getWpPageOrder()));
	        }
	        if(page.getWpAuthorID()!= -1) {
	        	content.put("wp_author_id", new Integer(page.getWpAuthorID()));
	        }
	        if(page.getDescription() != null){
	        	content.put("description", page.getDescription());
	        } 	        
	        if (page.getMt_excerpt() != null) {
	            content.put("mt_excerpt", page.getMt_excerpt());
	        }
	        if (page.getMt_text_more() != null) {
	            content.put("mt_text_more", page.getMt_text_more());
	        }
	        if (page.getDateCreated() != null) {
	            content.put("dateCreated", page.getDateCreated());
	        }
	        content.put("mt_allow_comments", page.isCommentsEnabled() ? "1" : "0");
	        content.put("mt_allow_pings", new Integer(page.isPingsEnabled() ? 1 : 0));
	        if(page.getCustomField() != null){
	        	content.put("custom_fields", page.getCustomField());
	        }

	        
	        Vector args = new Vector(6);
	        args.addElement(String.valueOf(page.getBlogId()));
	        args.addElement(String.valueOf(page.getID()));
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(content);
	        args.addElement(isPublished ? TRUE : FALSE);

	        Object response = execute("wp.editPage", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
	        
			connResponse.setResponseObject(response);
			notifyObservers(connResponse);
			
		} catch (Exception e) {
			setErrorMessage(e, "Comment error");
		}
		
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("notify error"); //TODO handle errors here...
		}
	}
}