package com.wordpress.controller;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.model.Comment;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.CommentsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.comment.GetCommentsConn;


public class CommentsController extends BaseController{
	
	private CommentsView view = null;
	private Blog currentBlog;
	ConnectionInProgressView connectionProgressView=null;
	 
	public CommentsController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
	}
				
	public String getBlogName() {
		return currentBlog.getName();
	}
		
	public void showView() {
		
		if( 9 == 9) {
			
			
			Preferences prefs = Preferences.getIstance();
	        
			final GetCommentsConn connection = new GetCommentsConn(currentBlog.getXmlRpcUrl(), 
					Integer.parseInt(currentBlog.getId()), currentBlog.getUsername(), 
					currentBlog.getPassword(), prefs.getTimeZone(), -1, "approve", 0, 10);
			
	        connection.addObserver(new loadCommentsCallBack(this));  
	        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_LOADING_POST));
	       
	        connection.startConnWork(); //starts connection
					
			int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				System.out.println("Chiusura della conn dialog tramite cancel");
				connection.stopConnWork(); //stop the connection if the user click on cancel button
			}
			
		} else {
			
		}
		
		//this.view= new CommentsView(this,currentBlog.getRecentPostTitles(), countRecentPost());
		//UiApplication.getUiApplication().pushScreen(view);
	}

	public void refreshView() {
		
	}
	
	//callback for post loading
	private class loadCommentsCallBack implements Observer {
		
		private CommentsController ctrl;
		
		public loadCommentsCallBack(CommentsController ctrl) {
			this.ctrl= ctrl;
		}
		
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
							
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
						
						Vector respVector = (Vector) resp.getResponseObject();

						Comment[] myCommentsList =new Comment[respVector.size()]; //my comment object list
						
						for (int i = 0; i < respVector.size(); i++) {
							 Hashtable returnCommentData = (Hashtable)respVector.elementAt(i);
							 
							//String userId= (String) returnCommentData.get("user_id");
							
							 int commentID=Integer.parseInt((String)returnCommentData.get("comment_id"));
			   	            int commentParent=Integer.parseInt((String) returnCommentData.get("parent"));
				            String status=(String) returnCommentData.get("status");
				            String authorID= (String) returnCommentData.get("author_Id");
				            String authorUrl=((String) returnCommentData.get("author_url"));
				            String authorEmail=((String) returnCommentData.get("author_email"));
				            String content= (String) returnCommentData.get("content");
				            
				            Comment comment= 
				            	new Comment(commentParent, content, authorID,authorUrl,authorEmail,status);
				            comment.setID(commentID);
				            comment.setLink((String) returnCommentData.get("link"));
				            comment.setPostID(Integer.parseInt((String)returnCommentData.get("post_id")));
				            comment.setPostTitle((String) returnCommentData.get("title"));
				            comment.setAuthor_Ip((String) returnCommentData.get("author_ip"));
				            comment.setDate_created_gmt((Date) returnCommentData.get("dateCreated"));
				            
				            myCommentsList[i]=comment; //add comment to my return list
						}
						
						view= new CommentsView( ctrl, myCommentsList);
						UiApplication.getUiApplication().pushScreen(view);
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}
				
				}
			});
		}
	}

}

