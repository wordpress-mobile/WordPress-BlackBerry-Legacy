package com.wordpress.xmlrpc;

import java.util.Vector;

import org.kxmlrpc.XmlRpcException;

import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;

public class BlogUpdateConn extends BlogConn  {
	
	private Blog blog;
	
	public BlogUpdateConn(Blog blog) {
		super(blog.getXmlRpcUrl(), blog.getUsername(), blog.getPassword());
		this.blog=blog;
	}

	
	
	private void checkConnectionResponse() throws Exception {
		if(connResponse.isError()) {
			if ( connResponse.getResponseObject() instanceof XmlRpcException) {
				connResponse.setError(false);
				connResponse.setStopped(false);
				connResponse.setResponse("");
				connResponse.setResponseObject(null);
			} else {
				throw (Exception) connResponse.getResponseObject();
			}
		} else {
			connResponse.setError(false);
			connResponse.setStopped(false);
			connResponse.setResponse("");
			connResponse.setResponseObject(null);
		}
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
			getBlogCategories(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse();
			
			getPageStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse();
			
			getPageTemplates(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse();
			
			getPostStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse();
			
			getTagList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse();

			getCommentStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse();
						
			Vector recentPostTitle = getRecentPostTitle(blog.getId(), blog.getMaxPostCount());
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setRecentPostTitles(recentPostTitle);
			checkConnectionResponse();
		
			Vector comments = getComments(Integer.parseInt(blog.getId()), -1, "", 0, 100);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				CommentsDAO.storeComments(blog, comments);
			checkConnectionResponse();
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