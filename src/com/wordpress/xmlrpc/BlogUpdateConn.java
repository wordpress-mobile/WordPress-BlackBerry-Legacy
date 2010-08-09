package com.wordpress.xmlrpc;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import org.kxmlrpc.XmlRpcException;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.log.Log;

public class BlogUpdateConn extends BlogConn  {
	
	private Blog blog;
	
	private String wholeErrorMessage = ""; 
	private boolean isError = false;
	
	public BlogUpdateConn(Blog blog) {
		super(blog.getXmlRpcUrl(), blog.getUsername(), blog.getPassword());
		this.blog=blog;
		if(blog.isHTTPBasicAuthRequired()) {
			this.setHttp401Password(blog.getHTTPAuthPassword());
			this.setHttp401Username(blog.getHTTPAuthUsername());
		}
	}

	private void checkConnectionResponse(String errorTitle) throws Exception {
		if(connResponse.isError()) {
			if ( connResponse.getResponseObject() instanceof XmlRpcException) {
				//do nothing. here we capturing all permission denied for blog...
				//or xmlrpc method missing (old wp)"
				XmlRpcException responseObject = (XmlRpcException) connResponse.getResponseObject();
				if(responseObject.code == 403) { //bad login 
					connResponse.setResponseObject(new Exception(_resources.getString(WordPressResource.MESSAGE_BAD_USERNAME_PASSWORD)));
					connResponse.setResponse("");
					throw new Exception(_resources.getString(WordPressResource.MESSAGE_BAD_USERNAME_PASSWORD));
				}
			} /*else if ( connResponse.getResponseObject() instanceof IOException) {
				//if IO exception occurred we should exit immediately 
				throw (Exception) connResponse.getResponseObject();
			}*/ else {
				Exception currentError = (Exception) connResponse.getResponseObject();
				isError = true;
				if(currentError != null) {
					String errorMessage = currentError.getMessage();
					if(errorMessage != null && !errorMessage.trim().equals(""))
					wholeErrorMessage += errorTitle + " - " + errorMessage + "\n";
				}
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
			checkConnectionResponse("Error while loading categories");
			
			getPageStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Page Status");
			
			getPageTemplates(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Page Templates");
			
			getPostStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Post Status");
			
			getTagList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Tags");

			getCommentStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Comment Status");
						
			Vector recentPostTitle = getRecentPostTitle(blog.getId(), blog.getMaxPostCount());
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setRecentPostTitles(recentPostTitle);
			checkConnectionResponse("Error while loading Recent Post");
			
			
			Vector blogPages = getPages(Integer.parseInt(blog.getId()), blog.getMaxPostCount());
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setPages(blogPages);
			checkConnectionResponse("Error while loading Pages");
		
			Vector comments = getComments(Integer.parseInt(blog.getId()), -1, "", 0, 100);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false ) {
				try{
					CommentsDAO.storeComments(blog, comments);
				} catch (IOException e) {
					Log.error(e, "Error while storing comments");
				} catch (RecordStoreException e) {
					Log.error(e, "Error while storing comments");
				} catch (Exception e) {
					Log.error(e, "Error while storing comments");
				} 
			}
			checkConnectionResponse("Error while loading comments");

			//if there was an errors
			if(!isError) {
				connResponse.setResponseObject(blog);
			} else {
				throw new Exception(wholeErrorMessage);
			}
			
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "Error while loading blog:");
		}
		catch (Exception e) {
			setErrorMessage(e, "Error while loading blog:");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.trace("Blog Update Notify Error");
		}
	}
	
	//read the blog options - this is a prototype 
	protected synchronized void getOptions(Blog blog) {
		try {
			Log.debug("reading page status list for the blog : " + blog.getName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getOptions", args);
			if (connResponse.isError()) {
				return;
			}
			
			Hashtable optionsStruct = (Hashtable) response;
			
			Enumeration k = optionsStruct.keys();
			while (k.hasMoreElements()) {
				String key = (String) k.nextElement();
				Log.trace("==== " + key + " ==== ");
				Hashtable currentOption = (Hashtable) optionsStruct.get(key);
				Enumeration innerkeys = currentOption.keys();
				while (innerkeys.hasMoreElements()) {
					String innerkey = (String) innerkeys.nextElement();
					Log.trace("innerkey " + innerkey + "; ");
					Log.trace("innervalue " + String.valueOf( currentOption.get(innerkey) )+ "; "); 
				}
			}			
		} catch (ClassCastException cce) {
			Log.error(cce, "Error while reading blog options");
		}
	}
	
	
	//return the blogs associated with this connection
	public Blog getBlog() {
		return blog;
	}
}