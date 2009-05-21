package com.wordpress.xmlrpc.page;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Page;
import com.wordpress.xmlrpc.BlogConn;

public class NewPageConn extends BlogConn  {

	private Page page;
	private boolean isPublished=false;
	private final int blogId;

	public NewPageConn(String hint, int blogId, String userHint, String passwordHint, TimeZone tz, Page page, boolean isPublished) {
		super(hint, userHint, passwordHint, tz);
		this.blogId = blogId;
		this.page=page;
		this.isPublished=isPublished;
	}

	public void run() {
		try{

			if (blogId < 0 ) {
				setErrorMessage("Blog already has a BlogId");
				notifyObservers(connResponse);
				return;
			}

			if (page.getID() > 0 ) {
				setErrorMessage("Page already has an Id");
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
				content.put("wp_page_parent_id", new Integer(page.getWpPageParentID()));
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
			args.addElement(String.valueOf(blogId));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(content);
			args.addElement(isPublished ? TRUE : FALSE);

			Object response = execute("wp.newPage", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}

			connResponse.setResponseObject(response);

		} 
		catch (Exception cce) {
			setErrorMessage(cce, "NewPage error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("NewPage error: Notify error"); 
		}

	}
}