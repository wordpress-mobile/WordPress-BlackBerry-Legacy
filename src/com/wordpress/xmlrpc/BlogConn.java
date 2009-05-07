package com.wordpress.xmlrpc;


import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import javax.microedition.io.ConnectionNotFoundException;

import org.kxmlrpc.XmlRpcClient;
import org.kxmlrpc.XmlRpcException;

import com.wordpress.model.Blog;
import com.wordpress.model.Category;
import com.wordpress.model.Tag;
import com.wordpress.utils.Preferences;
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
	
	public BlogConn(String url, String user, String password, TimeZone timezone) {

	    mUsername = user;
	    mPassword = password;
	    if(Preferences.getIstance().isDeviceSideConnection())
	    	urlConnessione=url+";deviceside=true";
	    else urlConnessione=url;
	   				
  	   System.out.println("connection created");
	}
	
		
	public void startConnWork(){
  	   System.out.println("Inizio richiesta XML-RPC");
		isWorking=true;
		t = new Thread(this);
		t.start();
	}
	
	/**
	 * blocca il funzionamento della connessione immediatamente.
	 */
	public void stopConnWork(){
		if(!isWorking) return;
  	   System.out.println("Richiesta chiusura della connessione XML-RPC");
		isWorking=false;
		t=null;
		mConnection=null;
		setStopMessage("chiusura della connessione XML-RPC");
		notifyObservers(connResponse);
	}
	
	public abstract  void run();

	protected Object execute(String aCommand, Vector aArgs){
		Object response = null;
		if(mConnection == null)
			mConnection = new XmlRpcClient(urlConnessione);
		
		try {
			response = mConnection.execute(aCommand, aArgs);
		} catch (ConnectionNotFoundException cnfe) {
			setErrorMessage(cnfe, "The server was not found");
		} catch (IOException ioe) {
			setErrorMessage(ioe, "A server communications error occured:");
		} catch (XmlRpcException xre) {
			setErrorMessage(xre, "An error occured talking to the blog:");
		} catch (Exception t) {
			setErrorMessage(t, "An error occured :");
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
	
	/**
	 * Get most used data of current blog
	 * @param currentBlog
	 * @throws Exception
	 */
	protected synchronized void getDefaultBlogData(Blog currentBlog) throws Exception {
		//the following calls uses the same connection of the main function GetBlog..
		//These calls can modify the state of the connection to isError=true;
		//we ignore its errors now
		getBlogCategories(currentBlog);
		getPageStatusList(currentBlog);
		getPostStatusList(currentBlog);
		getTagList(currentBlog);
		connResponse.setError(false);
		connResponse.setResponse("");
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
				//se il server xml-rpc è andato in err
				notifyObservers(connResponse);
				return null;		
			}

            Vector responsePosts = (Vector) response;
            return responsePosts;

		} catch (ClassCastException cce) {
			throw new Exception("Error while reading recent post title list");
		}
	}
	
	private synchronized void getBlogCategories(Blog blog) throws Exception {
  	   System.out.println("Reperisco le categorie del blog : "+ blog.getBlogName());
		Vector args;
		Object response;
		Vector categoryStructs;
		Hashtable categoryStruct;
		Category[] categories;

		args = new Vector(3);
		args.addElement(blog.getBlogId());
		args.addElement(mUsername);
		args.addElement(mPassword);

		response = execute("wp.getCategories", args);
		if(connResponse.isError()) {
			 blog.setCategories(null);
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
	  	   System.out.println("Terminato reperimento delle categorie del blog : "+ blog.getBlogName());
		} catch (ClassCastException cce) {
		    throw new Exception("Error while reading Categories");
		}
	}
	
//retrive the blog "post status list"
	private synchronized void getPostStatusList(Blog blog) throws Exception {
		try {
			System.out.println("reading post status list for the blog : "
					+ blog.getBlogName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getBlogId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getPostStatusList", args);
			if (connResponse.isError()) {
				 blog.setPostStatusList(null);
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
			
			System.out.println("End reading post status list for the blog : : "
					+ blog.getBlogName());
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading post status list");
		}
	}
	
	//retrive the blog "page status list"
	private synchronized void getPageStatusList(Blog blog) throws Exception {
		try {
			System.out.println("reading page status list for the blog : "
					+ blog.getBlogName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getBlogId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getPageStatusList", args);
			if (connResponse.isError()) {
				//throw new Exception("Cannot read page status list");
				blog.setPageStatusList(null);
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
					+ blog.getBlogName());
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading post status list");
		}
	}
	
	
	//retrive the blog "tag list"
	private synchronized void getTagList(Blog blog) throws Exception {
		try {
			System.out.println("reading tag list for the blog : "
					+ blog.getBlogName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getBlogId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getTags", args);
			if(connResponse.isError()) {
				 blog.setTags(null);
				 return;
				//throw new Exception("cannot read tag list");
			}

			
			Vector tags = (Vector) response;

			Tag[] mytags= new Tag[tags.size()];
			int blogId =Integer.parseInt(blog.getBlogId());
			
			Hashtable tagData = null;
			for (int i=0; i<tags.size(); i++){
				tagData = (Hashtable) tags.elementAt(i);

				System.out.println("tag_id: "+ (Integer.parseInt((String)tagData.get("tag_id"))));
				System.out.println("name: "+(String) tagData.get("name"));
				System.out.println("count: "+(Integer.parseInt((String) tagData.get("count"))));
				System.out.println("slug: "+(String) tagData.get("slug"));
				System.out.println("html_url: "+(String) tagData.get("html_url"));
				System.out.println("rss_url: "+(String) tagData.get("rss_url"));

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
					+ blog.getBlogName());
		} catch (ClassCastException cce) {
			throw new Exception("Error while reading post status list");
		}
	}
	
	
	
	protected void setStopMessage(String stopMsg){
		connResponse=new BlogConnResponse();
		connResponse.setError(false);
		connResponse.setStopped(true);
		connResponse.setResponse(stopMsg);
    	
		System.out.println(stopMsg);
	}
	
	protected void setErrorMessage(Exception e, String err){
		connResponse=new BlogConnResponse();
		connResponse.setError(true);
		
		if(e != null) {
			connResponse.setResponse(err+" - "+e.getMessage());
			System.out.println(err+" - "+e.getMessage());
		} else {
			connResponse.setResponse(err);
			System.out.println(err);			
		}
	}
	
	protected void setErrorMessage(String err){
		connResponse=new BlogConnResponse();
		connResponse.setError(true);
		connResponse.setResponse(err);
    
		System.out.println(err);
	}
}
