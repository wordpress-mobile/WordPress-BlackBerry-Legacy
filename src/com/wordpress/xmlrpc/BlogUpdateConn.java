package com.wordpress.xmlrpc;

import java.util.TimeZone;

import com.wordpress.model.Blog;

public class BlogUpdateConn extends BlogConn  {
	
	private Blog blog;
	
	public BlogUpdateConn(TimeZone tz, Blog blog) {
		super(blog.getBlogXmlRpcUrl(), blog.getUsername(), blog.getPassword(), tz);
		this.blog=blog;
	}

	/**
	 * Aggiorna un determinato blog
	 * @param provider
	 */
	public void run() {
		try {
			connResponse = new BlogConnResponse();
	        readBlogCategories(blog);
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
			System.out.println("notify error"); //TODO handle error here
		}
	}

	
}