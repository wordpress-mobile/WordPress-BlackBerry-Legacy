package com.wordpress.xmlrpc;

import java.util.Vector;

import org.kxmlrpc.XmlRpcException;

import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;

public class BlogUpdateConn extends BlogConn  {
	
	private Blog blog;
	
	private String wholeErrorMessage = ""; 
	private boolean isError = false;
	
	public BlogUpdateConn(Blog blog) {
		super(blog.getXmlRpcUrl(), blog.getUsername(), blog.getPassword());
		this.blog=blog;
	}

	private void checkConnectionResponse(String errorTitle) throws Exception {
		if(connResponse.isError()) {
			if ( connResponse.getResponseObject() instanceof XmlRpcException) {
	/*			connResponse.setError(false);
				connResponse.setStopped(false);
				connResponse.setResponse("");
				connResponse.setResponseObject(null);*/
			} else {
				Exception currentError = (Exception) connResponse.getResponseObject();
				isError = true;
				if(currentError != null) {
					String errorMessage = currentError.getMessage();
					if(errorMessage != null && !errorMessage.trim().equals(""))
					wholeErrorMessage += errorTitle + " - " + errorMessage + "\n";
				}
				//throw (Exception) connResponse.getResponseObject();
			}
		} 
		
		connResponse.setError(false);
		connResponse.setStopped(false);
		connResponse.setResponse("");
		connResponse.setResponseObject(null);
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
			checkConnectionResponse("Load Categories");
			
			getPageStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Load Page Status");
			
			getPageTemplates(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Load Page Templates");
			
			getPostStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Load Post Status");
			
			getTagList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Load Tags");

			getCommentStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Load Comment Status");
						
			Vector recentPostTitle = getRecentPostTitle(blog.getId(), blog.getMaxPostCount());
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setRecentPostTitles(recentPostTitle);
			checkConnectionResponse("Load Recent Post");
			
			
			Vector blogPages = getPages(blog.getId());
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setPages(blogPages);
			checkConnectionResponse("Load Page");
		
			Vector comments = getComments(Integer.parseInt(blog.getId()), -1, "", 0, 100);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				CommentsDAO.storeComments(blog, comments);
			checkConnectionResponse("Load Comment");
			
			if(!isError) {
				connResponse.setResponseObject(blog);
			} else {
				connResponse.setError(true);
				connResponse.setStopped(false);
				connResponse.setResponse("Refreshing Blog Error: \n"+ wholeErrorMessage);
				connResponse.setResponseObject(new Exception(wholeErrorMessage));				
				throw new Exception(wholeErrorMessage);
			}
			
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "Loading Blog Error");
		}
		catch (Exception e) {
			setErrorMessage(e, "Loading Blog Error");
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