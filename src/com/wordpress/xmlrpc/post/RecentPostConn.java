package com.wordpress.xmlrpc.post;

import java.util.Vector;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.model.Blog;
import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class RecentPostConn extends BlogConn  {
	
	private Blog blog;
	private int numPosts = WordPressInfo.DEFAULT_ITEMS_NUMBER;
	
	public int getNumPosts() {
		return numPosts;
	}

	public RecentPostConn(String hint,String userHint, String passwordHint, Blog aBlog, int numPosts) {
		super(hint, userHint, passwordHint);
		this.blog=aBlog;
		this.numPosts = numPosts;
		if(aBlog.isHTTPBasicAuthRequired()) {
			this.setHttp401Password(aBlog.getHTTPAuthPassword());
			this.setHttp401Username(aBlog.getHTTPAuthUsername());
		}
	}

	/**
	 * get recent posts title
	 * @param provider
	 */
	public void run() {
		try{

			connResponse = new BlogConnResponse();
	        Vector recentPostTitle = getRecentPostTitle(blog.getId(), numPosts);
			if( recentPostTitle != null )
				connResponse.setResponseObject(recentPostTitle);

		} catch (Exception cce) {
			setErrorMessage(cce, "loadPosts error");	
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("Recent Post Notify Error");
		}
	}
}