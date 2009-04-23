package com.wordpress.xmlrpc.post;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.xmlrpc.BlogConn;

public class EditPostConn extends BlogConn  {
	
	private Post post=null;
	private boolean isPublished=false;
	
	public EditPostConn(String hint, String userHint, String passwordHint, TimeZone tz, Post mPost, boolean isPublished) {
		super(hint, userHint, passwordHint, tz);
		this.post=mPost;
		this.isPublished=isPublished;
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

	        Hashtable content = new Hashtable(10);
	        if (post.getTitle() != null) {
	            content.put("title", post.getTitle());
	        }
	        if (post.getBody() != null) {
	            content.put("description", post.getBody());
	        }
	        if (post.getExtendedBody() != null) {
	            content.put("mt_text_more", post.getExtendedBody());
	        }
	        if (post.getExcerpt() != null) {
	            content.put("mt_excerpt", post.getExcerpt());
	        }
	        if (post.getAuthoredOn() != null) {
	            content.put("dateCreated", post.getAuthoredOn());
	        }
	        content.put("mt_convert_breaks", post.isConvertLinebreaksEnabled() ? "1" : "0");
	        content.put("mt_allow_comments", new Integer(post.isCommentsEnabled() ? 1 : 0));
	        content.put("mt_allow_pings", new Integer(post.isTrackbackEnabled() ? 1 : 0));

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

	        Category category = post.getPrimaryCategory();
	        if (category != null) {
	            Vector categories = new Vector(1);
	            Hashtable primary = new Hashtable(4);
	            primary.put("categoryId", category.getId());
	            primary.put("isPrimary", TRUE);
	            categories.addElement(primary);
	            
	            args = new Vector(4);
	            args.addElement(post.getId());
	            args.addElement(mUsername);
	            args.addElement(mPassword);
	            args.addElement(categories);
	                 
	            response = execute("mt.setPostCategories", args);
	    		if(connResponse.isError()) {
	    			//se il server xml-rpc è andato in err
	    			notifyObservers(connResponse);
	    			return;		
	    		}
	        }

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
			System.out.println("notify error"); //TODO handle error here
		}
	}
}