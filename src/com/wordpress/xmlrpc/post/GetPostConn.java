package com.wordpress.xmlrpc.post;

import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Post;
import com.wordpress.xmlrpc.BlogConn;

public class GetPostConn extends BlogConn  {
	
	private Post aPost=null;

	public GetPostConn(String hint,	String userHint, String passwordHint, TimeZone tz, Post mPost) {
		super(hint, userHint, passwordHint, tz);
		this.aPost=mPost;
	}

	/**
	 * Carica un post da remoto
	 * @param provider
	 */
	public void run() {
		try{
		
        Vector args = new Vector(3);
        args.addElement(aPost.getId());
        args.addElement(mUsername);
        args.addElement(mPassword);


        Object response = execute("metaWeblog.getPost", args);
		if(connResponse.isError()) {
			//se il server xml-rpc è andato in err
			notifyObservers(connResponse);
			return;		
		}

        try {
            Hashtable postData = (Hashtable) response;
            if( postData.get("postid") instanceof String) {
            	aPost.setId((String) postData.get("postid"));
            } else {
            	aPost.setId(String.valueOf(postData.get("postid")));
            }
            aPost.setTitle((String) postData.get("title"));
            aPost.setAuthor((String) postData.get("userid"));
            aPost.setBody((String) postData.get("description"));
            aPost.setExtendedBody((String) postData.get("mt_text_more"));
            aPost.setExcerpt((String) postData.get("mt_excerpt"));
            aPost.setAuthoredOn(((Date) postData.get("dateCreated")).getTime());
            aPost.setTags( (String) postData.get("mt_keywords"));
            aPost.setPassword((String) postData.get("wp_password"));
            aPost.setStatus((String) postData.get("post_status"));
            
            String breaks = (String) postData.get("mt_convert_breaks");
            if (breaks != null && !breaks.equals("__default__")) {
                aPost.setConvertLinebreaksEnabled(breaks.equals("1"));
            }
            
            Integer comments = (Integer) postData.get("mt_allow_comments");
            if (comments != null) {
                aPost.setCommentsEnabled(comments.intValue() != 0);
            }
            Integer trackback = (Integer) postData.get("mt_allow_pings");
            if (trackback != null) {
                aPost.setTrackbackEnabled(trackback.intValue() != 0);
            }
        } catch (ClassCastException cce) {
			setErrorMessage(cce, "Invalid server response");
			notifyObservers(connResponse);
        }

        response = execute("mt.getPostCategories", args);
                      
        try {
        	
            Vector categoryStructs = (Vector) response;
            int[] categories = new int[categoryStructs.size()];
            Hashtable categoryStruct = null;
            for (int i = 0; i < categoryStructs.size(); i++) {
                categoryStruct = (Hashtable) categoryStructs.elementAt(i);
                categories[i] = Integer.parseInt((String) categoryStruct.get("categoryId"));
            }
            aPost.setCategories(categories);
            
        } catch (ClassCastException cce) {
        	setErrorMessage(cce, "Error while reading post categories");
        }
        
        	connResponse.setResponseObject(aPost);
		} catch (Exception cce) {
			setErrorMessage(cce, "getPost error");
		}
		
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Get Post Conn Notify Error");
		}
	}
}