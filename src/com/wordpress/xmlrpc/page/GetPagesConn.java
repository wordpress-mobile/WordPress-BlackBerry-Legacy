package com.wordpress.xmlrpc.page;

import java.util.Vector;

import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class GetPagesConn extends BlogConn  {

	private final String blogID;
	private final int maxPagesNumber;

	public int getMaxPagesNumber() {
		return maxPagesNumber;
	}

	public GetPagesConn(String hint, String userHint, String passwordHint, String blogID, int maxPages) {
		super(hint, userHint, passwordHint);
		this.blogID = blogID;
		this.maxPagesNumber = maxPages;
	}

	public void run() {

		if (this.blogID == null) {
			setErrorMessage("Page does not have a BlogId");
			notifyObservers(connResponse);
			return;
		}
		
		try{
			connResponse = new BlogConnResponse();
	        Vector recentPostTitle = getPages(blogID, this.maxPagesNumber);
	        if( recentPostTitle != null )
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