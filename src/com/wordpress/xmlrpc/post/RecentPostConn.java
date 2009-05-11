package com.wordpress.xmlrpc.post;

import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Blog;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class RecentPostConn extends BlogConn  {
	
	private Blog blog;
	
	public RecentPostConn(String hint,String userHint, String passwordHint, TimeZone tz, Blog aBlog) {
		super(hint, userHint, passwordHint, tz);
		this.blog=aBlog;
	}

	/**
	 * reperisce da remoto gli i post più recenti
	 * @param provider
	 */
	public void run() {
		try{
			
			connResponse = new BlogConnResponse();
	        
			//getDefaultBlogData(blog);
	        Vector recentPostTitle = getRecentPostTitle(blog.getId(), blog.getMaxPostCount());
			connResponse.setResponseObject(recentPostTitle);
		/*
	        Vector args = new Vector(4);
	        args.addElement(blog.getBlogId());
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(new Integer(maxPost));

	        Object response = execute("mt.getRecentPostTitles", args);
			if(connResponse.isError()) {
				System.out.println("Errore nella connessione");
				//se il server xml-rpc è andato in err
				notifyObservers(connResponse);
				return;		
			}

            Vector responsePosts = (Vector) response;
            Hashtable postData = null;
            
             Simplify!!
            Post[] recentPosts = new Post[responsePosts.size()];
            for (int i = 0; i < recentPosts.length; i++) {
                postData = (Hashtable) responsePosts.elementAt(i);
                recentPosts[i] = new Post(blog,(String) postData.get("postid"),
                                          (String) postData.get("title"),
                                          (String) postData.get("userid"),
                                          (Date) postData.get("dateCreated"));
            }
			connResponse.setResponseObject(recentPosts);
			*/
            
          //  connResponse.setResponseObject(responsePosts);
		} catch (Exception cce) {
			setErrorMessage(cce, "loadPosts error");	
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Recent Post Notify Error");
		}
	}
}