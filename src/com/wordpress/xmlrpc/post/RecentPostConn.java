package com.wordpress.xmlrpc.post;

import java.util.Vector;

import com.wordpress.model.Blog;
import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class RecentPostConn extends BlogConn  {
	
	private Blog blog;
	
	public RecentPostConn(String hint,String userHint, String passwordHint, Blog aBlog) {
		super(hint, userHint, passwordHint);
		this.blog=aBlog;
	}

	/**
	 * get recent posts title
	 * @param provider
	 */
	public void run() {
		try{

			connResponse = new BlogConnResponse();
	        Vector recentPostTitle = getRecentPostTitle(blog.getId(), blog.getMaxPostCount());
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