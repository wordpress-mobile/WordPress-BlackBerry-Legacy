package com.wordpress.xmlrpc.comment;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;

public class GetCommentCountConn extends BlogConn  {
	
	private int postID = -1;

	public GetCommentCountConn(String url, String user, String password, int postID){
		super(url, user, password);
		this.postID = postID;
	}
	
	public void run() {
		try {

/*			if (this.postID < 0) {
				 setErrorMessage("Error PostID");
				 notifyObservers(connResponse);
		         return;
			}
	*/		
			Vector args = new Vector(5);
			
	        args.addElement("1");
	        args.addElement(mUsername);
	        args.addElement(mPassword);
	        args.addElement(String.valueOf(this.postID));
		
	        Object response = execute("wp.getCommentCount", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
					
            Hashtable commentData = (Hashtable) response;
            connResponse.setResponseObject(commentData);
/*{
            approved:(new String("14")), awaiting_moderation:(new String("1")), spam:(new String("4")), total_comments:(new Number(19))
		}
		*/	
            }
			catch (Exception e) {
				setErrorMessage(e, "GetCommentCount error: Invalid server response");
	        }
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				Log.error("GetCommentCount error: Notify error"); 
			}
			
		}
	}