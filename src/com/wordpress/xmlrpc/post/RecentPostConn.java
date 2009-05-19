package com.wordpress.xmlrpc.post;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Blog;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class RecentPostConn extends BlogConn  {
	
	private Blog blog;
	
	public RecentPostConn(String hint,String userHint, String passwordHint, TimeZone tz, Blog aBlog) {
		super(hint, userHint, passwordHint, tz);
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
			System.out.println("Recent Post Notify Error");
		}
	}
}