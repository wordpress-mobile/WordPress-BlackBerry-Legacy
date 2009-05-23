package com.wordpress.xmlrpc.page;

import java.util.Vector;

import com.wordpress.io.PageDAO;
import com.wordpress.model.Page;
import com.wordpress.xmlrpc.BlogConn;

public class NewPageConn extends BlogConn  {

	private boolean isPublished=false;
	private final int blogId;
	private Page page;

	public NewPageConn(String hint, String userHint, String passwordHint, int blogId, Page page, boolean isPublished) {
		super(hint, userHint, passwordHint);
		this.blogId = blogId;
		this.page = page;
		this.isPublished=isPublished;
	}

	public void setPageDescription(String newDescription){
		page.setDescription(newDescription);
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

			Vector args = new Vector(6);
			args.addElement(String.valueOf(blogId));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(PageDAO.page2Hashtable(page));
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