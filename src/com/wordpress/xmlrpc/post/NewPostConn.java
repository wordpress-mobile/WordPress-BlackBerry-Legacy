package com.wordpress.xmlrpc.post;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Post;
import com.wordpress.xmlrpc.BlogConn;

public class NewPostConn extends BlogConn  {
	
	private Post post=null;
	private boolean isPublished=false;
	
	public NewPostConn(String hint,	String userHint, String passwordHint, TimeZone tz, Post mPost, boolean isPublished) {
		super(hint, userHint, passwordHint, tz);
		this.post=mPost;
		this.isPublished=isPublished;
	}

	public void setPost(Post post) {
		this.post = post;
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
		 
		 	Hashtable content = EditPostConn.buildCallData(post);

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
			System.out.println("New Post Conne Notify Error");
		}
	}
}