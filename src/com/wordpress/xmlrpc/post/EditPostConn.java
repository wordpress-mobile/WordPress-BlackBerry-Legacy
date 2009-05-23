package com.wordpress.xmlrpc.post;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.model.Post;
import com.wordpress.xmlrpc.BlogConn;

public class EditPostConn extends BlogConn  {
	
	private Post post=null;
	private boolean isPublished=false;
	
	public EditPostConn(String hint, String userHint, String passwordHint, Post mPost, boolean isPublished) {
		super(hint, userHint, passwordHint);
		this.post=mPost;
		this.isPublished=isPublished;
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

		 	Hashtable content = buildCallData(post);
	        
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

	protected static Hashtable buildCallData(Post post) {
        Hashtable content = new Hashtable();
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
        if (post.getTags() != null) {
        	content.put("mt_keywords", post.getTags());
        }
        if (post.getStatus() != null) {
        	content.put("post_status", post.getStatus());
        }
        if (post.getPassword() != null) {
        	content.put("wp_password", post.getPassword());
        }	        
        
        content.put("mt_convert_breaks", post.isConvertLinebreaksEnabled() ? "1" : "0");
        content.put("mt_allow_comments", new Integer(post.isCommentsEnabled() ? 1 : 0));
        content.put("mt_allow_pings", new Integer(post.isTrackbackEnabled() ? 1 : 0));
		return content;
	}
}