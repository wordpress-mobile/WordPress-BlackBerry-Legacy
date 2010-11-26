package com.wordpress.xmlrpc.post;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.io.DraftDAO;
import com.wordpress.model.Post;
import com.wordpress.xmlrpc.BlogConn;

public class NewPostConn extends BlogConn  {
	
	private Post post=null;
	private boolean isPublished=false;
	
	public NewPostConn(String hint,	String userHint, String passwordHint, Post mPost, boolean isPublished) {
		super(hint, userHint, passwordHint);
		this.post=mPost;
		this.isPublished=isPublished;
   		if(post.getBlog().isHTTPBasicAuthRequired()) {
			this.setHttp401Password(post.getBlog().getHTTPAuthPassword());
			this.setHttp401Username(post.getBlog().getHTTPAuthUsername());
		}
	}

	public void setPost(Post post) {
		this.post = post;
   		if(post.getBlog().isHTTPBasicAuthRequired()) {
			this.setHttp401Password(post.getBlog().getHTTPAuthPassword());
			this.setHttp401Username(post.getBlog().getHTTPAuthUsername());
		}
	}

	/**
	 * 
	 * @param provider
	 */
	public void run() {
		try{
		
		 if (post.getId() != null) {
			 setErrorMessage("Post already has a postid");
			 notifyObservers(connResponse);
	         return;
	        }
		 
		 	Hashtable content = DraftDAO.post2Hashtable(post);
	        /*
	         * 'trackback' and 'enable comments' option
	         *  should not be considered at this moment.
	         *  We haven't the GUI to set this value so we are using the ù
	         *  blog main setting. 
	         *  
	         *  look at EditPostConn.java
	         */
		 	content.remove("mt_allow_comments");
		    content.remove("mt_allow_pings");

	        Vector args = new Vector(5);
	        args.addElement(post.getBlog().getId());
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(content);
	        args.addElement(isPublished ? TRUE : FALSE);

	        Object response = execute("metaWeblog.newPost", args);
			if(connResponse.isError()) {
				//se il server xml-rpc è andato in err
				notifyObservers(connResponse);
				return;		
			}
	        post.setId((String) response);

	        
	        int[] categories = post.getCategories();
	        
	        setPostCategories(categories, post.getId());

	        if (isPublished) {
	            args = new Vector(3);
	            args.addElement(post.getId());
	            args.addElement(mUsername);
	            args.addElement(mPassword);
	            response = execute("mt.publishPost", args);
	    		if(connResponse.isError()) {
	    			//se il server xml-rpc è andato in err
	    			notifyObservers(connResponse);
	    			return;		
	    		}
	        }

			connResponse.setResponseObject(response);
		} catch (Exception cce) {
			setErrorMessage(cce, "loadBlogs error");
		}
		
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("New Post Conn Notify Error");
		}
	}
}