package com.wordpress.xmlrpc.post;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.io.DraftDAO;
import com.wordpress.model.Post;
import com.wordpress.xmlrpc.BlogConn;

public class EditPostConn extends BlogConn  {
	
	private Post post=null;
	private boolean isPublished=false;
	
	public EditPostConn(String hint, String userHint, String passwordHint, Post mPost, boolean isPublished) {
		super(hint, userHint, passwordHint);
		this.post=mPost;
		this.isPublished=isPublished;
		if(mPost.getBlog().isHTTPBasicAuthRequired()) {
			this.setHttp401Password(mPost.getBlog().getHTTPAuthPassword());
			this.setHttp401Username(mPost.getBlog().getHTTPAuthUsername());
		}
	}

	public void setPost(Post post) {
		this.post = post;
	}
	
	/**
	 * scrive in remoto un post di tipo draft 
	 * @param provider
	 */
	public void run() {
		try{
		 if (post.getId() == null) {
			 setErrorMessage("Post does not have a postid");
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
	         *  look at NewPostConn.java
	         */
		 	content.remove("mt_allow_comments");
		    content.remove("mt_allow_pings");
		 	
			Vector args = new Vector(5);
			args.addElement(post.getId());
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(content);
			args.addElement(isPublished ? TRUE : FALSE);

	        Object response = execute("metaWeblog.editPost", args);
			if(connResponse.isError()) {
				//se il server xml-rpc è andato in err
				notifyObservers(connResponse);
				return;		
			}

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
			setErrorMessage(cce, "Edit post error");
		}
		
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Edit Post Notify Error"); 
		}
	}

}