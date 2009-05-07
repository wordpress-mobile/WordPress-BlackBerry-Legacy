package com.wordpress.xmlrpc;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Blog;

public class BlogUpdateConn extends BlogConn  {
	
	private Blog blog;
	
	public BlogUpdateConn(TimeZone tz, Blog blog) {
		super(blog.getBlogXmlRpcUrl(), blog.getUsername(), blog.getPassword(), tz);
		this.blog=blog;
	}

	/**
	 * refresh blog
	 * @param provider
	 */
	public void run() {
		try {
			connResponse = new BlogConnResponse();
	        
			getDefaultBlogData(blog);
			
			System.out.println("reading recent post title list for the blog : "	+ blog.getBlogName());
			Vector recentPostTitle = getRecentPostTitle(blog.getBlogId(), blog.getMaxPostCount());
			blog.setRecentPostTitles(recentPostTitle);
			
			System.out.println("End reading recent post title list for the blog : " + blog.getBlogName());	
	        
			connResponse.setResponseObject(blog);
			
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "update Blog error");
			//notifyObservers(connResponse);
		}
		catch (Exception e) {
			setErrorMessage(e, "Invalid server response");
			//notifyObservers(connResponse);
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Blog Update Notify Error");
		}
	}

	
}