package com.wordpress.xmlrpc;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Blog;

public class BlogUpdateConn extends BlogConn  {
	
	private Blog blog;
	
	public BlogUpdateConn(TimeZone tz, Blog blog) {
		super(blog.getXmlRpcUrl(), blog.getUsername(), blog.getPassword(), tz);
		this.blog=blog;
	}

	/**
	 * refresh blog
	 * @param provider
	 */
	public void run() {
		try {
			connResponse = new BlogConnResponse();
			//the following calls uses the same connection of the main function GetBlog..
			//These calls can modify the state of the connection to isError=true;
			//we ignore its errors now
			getBlogCategories(blog);
			getPageStatusList(blog);
			getPostStatusList(blog);
			getTagList(blog);
			connResponse.setError(false);
			connResponse.setResponse("");
			
			System.out.println("reading recent post title list for the blog : "	+ blog.getName());
			Vector recentPostTitle = getRecentPostTitle(blog.getId(), blog.getMaxPostCount());
			blog.setRecentPostTitles(recentPostTitle);
			
			System.out.println("End reading recent post title list for the blog : " + blog.getName());	
	        
			connResponse.setResponseObject(blog);
			
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "update Blog error");
		}
		catch (Exception e) {
			setErrorMessage(e, "Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Blog Update Notify Error");
		}
		
	}
}