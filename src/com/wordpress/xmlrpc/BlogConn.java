package com.wordpress.xmlrpc;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;

import org.kxmlrpc.XmlRpcClient;
import org.kxmlrpc.XmlRpcException;

import com.wordpress.model.Blog;
import com.wordpress.model.Category;
import com.wordpress.model.Tag;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;

public abstract class BlogConn extends Observable implements Runnable {
	
	protected final Boolean TRUE = new Boolean(true);
    protected final Boolean FALSE = new Boolean(false);
	protected String urlConnessione;
    protected String mUsername;
    protected String mPassword;
    protected XmlRpcClient mConnection;	
	protected BlogConnResponse connResponse = new BlogConnResponse();
	protected boolean isWorking = false;
	protected Thread t = null;
	
	public BlogConn(String url, String user, String password) {
	    mUsername = user;
	    mPassword = password;
	    urlConnessione=url;
	}
	
		
	public void startConnWork(){
		Log.debug("Inizio richiesta XML-RPC");
		isWorking=true;
		t = new Thread(this);
		t.start();
	}
	
	/**
	 * blocca il funzionamento della connessione immediatamente.
	 */
	public void stopConnWork() {
		if (!isWorking)
			return;
		
		Log.debug("User requested stop the XML-RPC connection");
		isWorking = false;
		t = null;
		mConnection = null;

		connResponse = new BlogConnResponse();
		connResponse.setError(false);
		connResponse.setStopped(true);
		connResponse.setResponse("connection stopped by user");
		notifyObservers(connResponse);
	}
	
	public abstract  void run();

	protected Object execute(String aCommand, Vector aArgs){
		isWorking=true;
		
		Object response = null;
		if(mConnection == null)
			mConnection = new XmlRpcClient(urlConnessione);
		
		try {
			response = mConnection.execute(aCommand, aArgs);
		} catch (ConnectionNotFoundException cnfe) {
			setErrorMessage(cnfe, "The server was not found");
		} catch (IOException ioe) {
			setErrorMessage(ioe, "A server communications error occured");
		} catch (XmlRpcException xre) {
			setErrorMessage(xre, "Blog Message (code " +xre.code+")");
		} catch (Exception t) {
			setErrorMessage(t, "An error occured");
		} 
   	 
  	   System.out.println("termine richiesta XML-RPC");
		isWorking=false;
		return response;
	}
	
	
	protected void setPostCategories(int[] categories, String postID) throws Exception {
		System.out.println(">>>Set Post categories ");
		Vector args;
		if (categories != null) {
		    Vector catVector = new Vector(categories.length);
		    for (int i = 0; i < categories.length; i++) {
		    	Hashtable primary = new Hashtable(4);
		        primary.put("categoryId", String.valueOf(categories[i]));
		        catVector.addElement(primary);
			}
		  		    
		    args = new Vector(4);
		    args.addElement(postID);
		    args.addElement(mUsername);
		    args.addElement(mPassword);
		    args.addElement(catVector);
		         
		    Object  response = execute("mt.setPostCategories", args);
			if(connResponse.isError()) {
				throw new Exception("Error while setting post categories");
			}
		}

	}
	
	//retrive comments 
	protected synchronized Vector getComments(int blogId, int postId, String status, int offset, int number){
		try{
			
			Hashtable StructData = new Hashtable(5);
			
			if (postId > 0) {
				StructData.put("post_id", String.valueOf(postId));
			} else {
				
			}
			
			StructData.put("comment_status", status);
			
			if (offset!=0 ) {
				StructData.put("offset", String.valueOf(offset));
			}
			if (number != 0) {
				StructData.put("number", String.valueOf(number));
			}
			
			Vector args = new Vector(5);
			args.addElement(String.valueOf(blogId));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(StructData);
			
			Object response = execute("wp.getComments", args);
			if(connResponse.isError()) {
				//notifyObservers(connResponse);
				return null;		
			}
			
			return ((Vector)response);
		}
		catch (ClassCastException e) {
			throw new ClassCastException("GetComments error: Invalid server response "+ e.getMessage());
		}
		
	}
		
	//retrive "recent post title list"
	protected synchronized Vector getRecentPostTitle(String blogID, int maxPost) throws Exception {
		try {

			Vector args = new Vector(4);
	        args.addElement(blogID);
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(new Integer(maxPost));

	        Object response = execute("mt.getRecentPostTitles", args);
			if(connResponse.isError()) {
				return null;		
			}

            Vector responsePosts = (Vector) response;
            return responsePosts;

		} catch (ClassCastException cce) {
			throw new Exception("Error while reading recent post title list");
		}
	}
	
	protected synchronized void getBlogCategories(Blog blog) throws Exception {
  	   System.out.println("Reperisco le categorie del blog : "+ blog.getName());
		Vector args;
		Object response;
		Vector categoryStructs;
		Hashtable categoryStruct;
		Category[] categories;

		args = new Vector(3);
		args.addElement(blog.getId());
		args.addElement(mUsername);
		args.addElement(mPassword);

		response = execute("wp.getCategories", args);
		if(connResponse.isError()) {
			 //blog.setCategories(null);
			 return;
		}
		
		args = null;

		try {
		    categoryStructs = (Vector) response;
		    categoryStruct = null;
		    if (categoryStructs.size() > 0) {
		        categories = new Category[categoryStructs.size()];
		        for (int i = 0; i < categories.length; i++) {
		            categoryStruct = (Hashtable) categoryStructs.elementAt(i);
		            categories[i] = new Category
		            ((String) categoryStruct.get("categoryId"),
		            (String) categoryStruct.get("categoryName"),
		            (String) categoryStruct.get("description"),
		            Integer.parseInt((String) categoryStruct.get("parentId")),
		            (String) categoryStruct.get("htmlUrl"),
		            (String) categoryStruct.get("rssUrl")
		            );
		        }
		        blog.setCategories(categories);
		    } else {
		        blog.setCategories(null);
		    }
	  	   System.out.println("Terminato reperimento delle categorie del blog : "+ blog.getName());
		} catch (ClassCastException cce) {
		    throw new Exception("Error while reading Categories");
		}
	}
	
//retrive the blog "post status list"
	protected synchronized void getPostStatusList(Blog blog) throws Exception {
		try {
			System.out.println("reading post status list for the blog : "
					+ blog.getName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getPostStatusList", args);
			if (connResponse.isError()) {
				// blog.setPostStatusList(null);
				 return;
				//throw new Exception("Cannot read post status list");
			}

			Hashtable statusList = (Hashtable) response;

			Enumeration elements = statusList.keys();
			for (; elements.hasMoreElements();) {
				String key = (String) elements.nextElement();
				System.out.println("key: " + key);
				System.out.println("value: " + statusList.get(key));
			}

			blog.setPostStatusList(statusList);
			
			System.out.println("End reading post status list for the blog : "
					+ blog.getName());
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading post status list");
		}
	}
	
	//retrive all pages from blog
	protected synchronized Vector getPages(String blogID) throws Exception{
		try{
			Vector args = new Vector(3);
			args.addElement(blogID);
			args.addElement(mUsername);
			args.addElement(mPassword);
			
			Object response = execute("wp.getPages", args);
			if(connResponse.isError()) {
				return null;		
			}
			
			Vector pagesVector = (Vector) response;
			return pagesVector;
			
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading pages data from blog");
		}
	}
	
	//retrive the blog "page status list"
	protected synchronized void getPageStatusList(Blog blog) throws Exception {
		try {
			System.out.println("reading page status list for the blog : "
					+ blog.getName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getPageStatusList", args);
			if (connResponse.isError()) {
				//blog.setPageStatusList(null);
				return;
			}

			Hashtable statusList = (Hashtable) response;

			Enumeration elements = statusList.keys();
			for (; elements.hasMoreElements();) {
				String key = (String) elements.nextElement();
				System.out.println("key: " + key);
				System.out.println("value: " + statusList.get(key));
			}

			blog.setPageStatusList(statusList);
			
			System.out.println("End reading page status list for the blog : : "
					+ blog.getName());
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading post status list");
		}
	}
	
	//retrive the blog "page status list"
	protected synchronized void getPageTemplates(Blog blog) throws Exception {
		try {
			System.out.println("reading pages templates for the blog : "
					+ blog.getName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getPageTemplates", args);
			if (connResponse.isError()) {
				//blog.setPageStatusList(null);
				return;
			}

			Hashtable statusList = (Hashtable) response;

/*			Enumeration elements = statusList.keys();
			for (; elements.hasMoreElements();) {
				String key = (String) elements.nextElement();
				System.out.println("key: " + key);
				System.out.println("value: " + statusList.get(key));
			}
*/
			blog.setPageTemplates(statusList);
			
			System.out.println("End reading page templates for the blog : : "
					+ blog.getName());
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading post status list");
		}
	}
	
	
	protected synchronized void getCommentStatusList(Blog blog) {
		try {

			System.out.println("reading comment status list for the blog : "
					+ blog.getName());

			Vector args = new Vector(4);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getCommentStatusList", args);
			if (connResponse.isError()) {
				//blog.setCommentStatusList(null);
				return;
			}
			
			Hashtable commentData = (Hashtable) response;
			blog.setCommentStatusList(commentData);
			
			System.out.println("End reading comment status list for the blog : "
					+ blog.getName());
		} catch (Exception e) {
			setErrorMessage(e,
					"GetCommentStatusList error: Invalid server response");
		}
	}
	
	
	//retrive the blog "tag list"
	protected synchronized void getTagList(Blog blog) throws Exception {
		try {
			System.out.println("reading tag list for the blog : "
					+ blog.getName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getTags", args);
			if(connResponse.isError()) {
				// blog.setTags(null);
				 return;
				//throw new Exception("cannot read tag list");
			}

			
			Vector tags = (Vector) response;

			Tag[] mytags= new Tag[tags.size()];
			int blogId =Integer.parseInt(blog.getId());
			
			Hashtable tagData = null;
			for (int i=0; i<tags.size(); i++){
				tagData = (Hashtable) tags.elementAt(i);
				int tagId=Integer.parseInt((String)tagData.get("tag_id"));
				String tagName=(String) tagData.get("name");
				int count=Integer.parseInt((String) tagData.get("count"));
				String slug=(String) tagData.get("slug");
				String htmlUrl=(String) tagData.get("html_url");
				String rssUrl= (String) tagData.get("rss_url");
				Tag myTag= new Tag(tagId,tagName,count,slug, htmlUrl, rssUrl);
				mytags[i]=myTag;
			}
			blog.setTags(mytags);
			
			System.out.println("End reading tag list for the blog : "
					+ blog.getName());
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading post status list");
		}
	}
	
	
	//retrive the blog "tag list"
	protected synchronized void getOptions(Blog blog) throws Exception {
		try {
			System.out.println("reading option list for the blog : "
					+ blog.getName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getOptions", args);
			if(connResponse.isError()) {
				 blog.setTags(null);
				 return;
				//throw new Exception("cannot read tag list");
			}

			
			Hashtable tagData = (Hashtable) response;

			Enumeration elements = tagData.keys();
			for (; elements.hasMoreElements();) {
				String key = (String) elements.nextElement();
				Log.debug("key: " + key);
				Log.debug("value: " + tagData.get(key));
			}
			
				
//			blog.setTags(mytags);
			
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading post status list");
		}
	}
	
	
	protected void setErrorMessage(Exception e, String err){
		if (!isWorking)
			return;
		connResponse=new BlogConnResponse();
		connResponse.setError(true);
		connResponse.setResponseObject(e); //set the exception as response option
		
		if(e != null && e.getMessage()!= null ) {
			connResponse.setResponse(err+" : "+e.getMessage());
			Log.error(err+" : "+e.getMessage());
		} else {
			connResponse.setResponse(err);
			Log.error(err);			
		}
	}
	
	protected void setErrorMessage(String err){
		if (!isWorking)
			return;
		connResponse=new BlogConnResponse();
		connResponse.setError(true);
		connResponse.setResponse(err);
    
		Log.error(err);
	}
}
