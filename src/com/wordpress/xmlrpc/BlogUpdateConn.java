package com.wordpress.xmlrpc;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.io.CommentsDAO;
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
			//the following calls uses the same connection 
			//These calls can modify the state of the connection to isError=true;
			//we ignore its errors now
			getBlogCategories(blog);
			connResponse.setError(false);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			connResponse.setStopped(false);
			connResponse.setResponse("");
			
			getPageStatusList(blog);
			connResponse.setError(false);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			connResponse.setStopped(false);
			connResponse.setResponse("");
			
			
			getPageTemplates(blog);
			connResponse.setError(false);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			connResponse.setStopped(false);
			connResponse.setResponse("");
			
			getPostStatusList(blog);
			connResponse.setError(false);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			connResponse.setStopped(false);
			connResponse.setResponse("");
			
			getTagList(blog);
			connResponse.setError(false);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			connResponse.setStopped(false);
			connResponse.setResponse("");

			getCommentStatusList(blog);
			connResponse.setError(false);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			connResponse.setStopped(false);
			connResponse.setResponse("");
						
			System.out.println("reading recent post title list for the blog : "	+ blog.getName());
			Vector recentPostTitle = getRecentPostTitle(blog.getId(), blog.getMaxPostCount());
			blog.setRecentPostTitles(recentPostTitle);
			System.out.println("End reading recent post title list for the blog : " + blog.getName());
			connResponse.setError(false);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			connResponse.setStopped(false);
			connResponse.setResponse("");
			
			System.out.println("reading comments for the blog : "	+ blog.getName());
			Vector comments = getComments(Integer.parseInt(blog.getId()), -1, "", 0, 100);
			connResponse.setError(false);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			connResponse.setStopped(false);
			connResponse.setResponse("");
			try {
				CommentsDAO.storeComments(blog, comments);
			} catch (Exception e) {
			
			} 
			System.out.println("End reading comments for the blog : " + blog.getName());
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
	
	//return the blogs associated with this connection
	public Blog getBlog() {
		return blog;
	}
}