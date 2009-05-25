package com.wordpress.xmlrpc.post;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.io.DraftDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.xmlrpc.BlogConn;

public class GetPostConn extends BlogConn  {
	

	private final Blog blog;
	private final String postID;

	public GetPostConn(String hint,	String userHint, String passwordHint, Blog blog, String postID) {
		super(hint, userHint, passwordHint);
		this.blog = blog;
		this.postID = postID;
	}

	/**
	 * Carica un post da remoto
	 * @param provider
	 */
	public void run() {
		try{
		
        Vector args = new Vector(3);
        args.addElement(postID);
        args.addElement(mUsername);
        args.addElement(mPassword);
        Post aPost;

        Object response = execute("metaWeblog.getPost", args);
		if(connResponse.isError()) {
			//se il server xml-rpc è andato in err
			notifyObservers(connResponse);
			return;		
		}

        try {
            Hashtable postData = (Hashtable) response;
            aPost= DraftDAO.hashtable2Post(postData, blog);
           
        } catch (ClassCastException cce) {
			setErrorMessage(cce, "Invalid server response");
			notifyObservers(connResponse);
			return;
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