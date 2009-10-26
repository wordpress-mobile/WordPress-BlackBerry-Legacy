package com.wordpress.xmlrpc.page;

import java.util.Vector;

import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class GetPagesConn extends BlogConn  {

	private final int blogID;
	private final int maxPages;

	public GetPagesConn(String hint, String userHint, String passwordHint, int blogID, int maxPages) {
		super(hint, userHint, passwordHint);
		this.blogID = blogID;
		this.maxPages = maxPages;
	}

	public void run() {

		if (this.blogID < 0) {
			setErrorMessage("Page does not have a BlogId");
			notifyObservers(connResponse);
			return;
		}
		
		try{
			connResponse = new BlogConnResponse();
	        Vector recentPostTitle = getPages(blogID, this.maxPages);
	        
			connResponse.setResponseObject(recentPostTitle);
		} catch (Exception cce) {
			setErrorMessage(cce, "loadPages error");	
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("Recent Pages Notify Error");
		}
		
	}
}