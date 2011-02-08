package com.wordpress.controller;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PostsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.post.DeletePostConn;
import com.wordpress.xmlrpc.post.GetPostConn;
import com.wordpress.xmlrpc.post.RecentPostConn;


public class PostsController extends BaseController{
	
	private PostsView view = null;
	private Blog currentBlog;
	ConnectionInProgressView connectionProgressView=null;
	
 
	public PostsController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
	}
				
	public String getBlogName() {
		return currentBlog.getName();
	}
	
	//count number of new posts and set the new number in the blog structure.
	private int countNewPosts() {
		Vector recentPostsTitle = currentBlog.getRecentPostTitles();
		Vector viewedPostsTitle = currentBlog.getViewedPost();
		
		if(recentPostsTitle == null) //if recentpost is null you are not allow to view the post of this blog
			return 0;
		
		int count = 0;

		for (int i = 0; i < recentPostsTitle.size(); i++) {
			boolean presence = false; 
			Hashtable recentPost = (Hashtable) recentPostsTitle.elementAt(i);
            String postId = String.valueOf( recentPost.get("postid") );
			
		    for (int j = 0; j < viewedPostsTitle.size(); j++) {
		    	Hashtable viewedPost = (Hashtable) viewedPostsTitle.elementAt(j);
	            String viewedPostId = String.valueOf( viewedPost.get("postid") );
		    	
		    	if (postId.equalsIgnoreCase(viewedPostId)) {
		    		presence = true;
		    		break;
		    	}
			}
		    
		    if (!presence) 
		    	count++;
		}
		currentBlog.setViewedPost(recentPostsTitle); //update viewed post in blog obj
		return count;
	}
	
	public void showView(){
		this.view= new PostsView(this,currentBlog.getRecentPostTitles(), countNewPosts());
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	//check if the user can do the action
	private boolean checkUserRights(){
		if (currentBlog.getPostStatusList() == null) 
			return false;
		return true;
	}
	
	public void showComments(int selected) {
		if(!checkUserRights()){ 
			displayMessage(_resources.getString(WordPressResource.ERROR_YOU_CANNOT_MANAGEPOST));
			return;
		}
		if(selected != -1){
			
			Hashtable postData = (Hashtable) currentBlog.getRecentPostTitles().elementAt(selected);
			String postID = (String) postData.get("postid");
			
			if(postID == null || postID.equals("")) {
				displayMessage(_resources.getString(WordPressResource.MESSAGE_LOCAL_DRAFT_NO_COMMENT));
				return;
			}
			else {
				FrontController.getIstance().showCommentsByPost(currentBlog, postID, (String) postData.get("title"));
			}
		}
	}
	
	/** starts the  post loading */
	public void editPost(int selected){
		if(!checkUserRights()){
			displayMessage(_resources.getString(WordPressResource.ERROR_YOU_CANNOT_MANAGEPOST));
			return;
		}
				
		if(selected != -1){
			
			Hashtable postData = (Hashtable) currentBlog.getRecentPostTitles().elementAt(selected);
						
			/*Post  post = new Post(currentBlog,(String) postData.get("postid"),
                                      (String) postData.get("title"),
                                      (String) postData.get("userid"),
                                      (Date) postData.get("dateCreated"));
			*/
	        
			final GetPostConn connection = new GetPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
	        		currentBlog.getPassword(), currentBlog, String.valueOf(postData.get("postid")) );
	        connection.addObserver(new loadPostCallBack());  
	        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_LOADING_POST));
	       
	        connection.startConnWork(); //starts connection
					
			int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				Log.trace("Chiusura della conn dialog tramite cancel");
				connection.stopConnWork(); //stop the connection if the user click on cancel button
			}
			
		}	     	
	}	

	public void deletePost(int postID){
		if(postID == -1) return;
		
		int result=this.askQuestion(_resources.getString(WordPressResource.MESSAGE_DELETE_POST));   
		
    	if(Dialog.YES==result) {
		
    		Hashtable postData = (Hashtable) currentBlog.getRecentPostTitles().elementAt(postID);

			String postid = String.valueOf(postData.get("postid"));
				 
			DeletePostConn connection = new DeletePostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
					 currentBlog.getPassword(), postid);
	   		if(currentBlog.isHTTPBasicAuthRequired()) {
				connection.setHttp401Password(currentBlog.getHTTPAuthPassword());
				connection.setHttp401Username(currentBlog.getHTTPAuthUsername());
			}
			 connection.addObserver(new deletePostCallBack());
		     
		     connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_DELETE_POST));
	  
		    connection.startConnWork(); //starts connection
		    int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				Log.trace("Chiusura della conn dialog tramite cancel");
				connection.stopConnWork(); //stop the connection if the user click on cancel button
			}
    	}
	}
	
			
	public void showDraftPosts(){
		if(!checkUserRights()){
			displayMessage(_resources.getString(WordPressResource.ERROR_YOU_CANNOT_MANAGEPOST));
			return;
		}
		if(currentBlog != null) {
			FrontController.getIstance().showDraftPostsView(currentBlog);
		}
	}
		

	public void newPost() {
		if(!checkUserRights()){
			displayMessage(_resources.getString(WordPressResource.ERROR_YOU_CANNOT_MANAGEPOST));
			return;
		}
		
		if (currentBlog != null) {
			FrontController.getIstance().newPost(currentBlog); // show the new post view
		}
	}

	//called from the front controller when necessary
	public void refreshView() {
	   	 try {
	   		 currentBlog=BlogDAO.getBlog(currentBlog);
			} catch (Exception e) {
				displayError(e, "Refreshing Blog Error");
			}
		view.refresh(currentBlog.getRecentPostTitles(), countNewPosts());
	}
	
	public void refreshPostsList() {
		System.out.println(">>>refreshPosts");
        final RecentPostConn connection = new RecentPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(), currentBlog);
        connection.addObserver(new refreshRecentPostCallBack()); 
        String connMsg=_resources.getString(WordPressResource.CONN_REFRESH_POSTLIST);
        connectionProgressView= new ConnectionInProgressView(connMsg);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	
	class deletePostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					Log.debug(">>>deletePostResponse");

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					if(resp.isStopped()){
						return;
					}
					if(!resp.isError()) {
						
				        String postID=(String) resp.getResponseObject();
				        Vector recentPostTitles = currentBlog.getRecentPostTitles();
				        
				        for (int i = 0; i < recentPostTitles.size(); i++) {
				        	Hashtable postData = (Hashtable) recentPostTitles.elementAt(i);
				        	String tmpPostID =(String) postData.get("postid");
				        	if(tmpPostID.equalsIgnoreCase(postID)){
				        		recentPostTitles.removeElementAt(i);
				        		break;
				        	}
						}			        
						
						view.refresh(currentBlog.getRecentPostTitles(), countNewPosts());
						
						try{
							BlogDAO.updateBlog(currentBlog);							
						} catch (final Exception e) {
						 	displayError(e,"Error while refreshing blogs");	
						}

						
					} else {  
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}
					
					
					}//and run 
			});
		}
	}
		
	
	class refreshRecentPostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					Log.trace(">>>loadRecentPostsResponse");

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
							
					if(resp.isStopped()){
						return;
					}
					if(!resp.isError()) {
						
						Vector recentPostTitle= (Vector) resp.getResponseObject();
						currentBlog.setRecentPostTitles(recentPostTitle);

						view.refresh(currentBlog.getRecentPostTitles() , countNewPosts());
						
						try{
							BlogDAO.updateBlog(currentBlog);							
						} catch (final Exception e) {
						 	displayError(e,"Error while refreshing blogs");	
						}
						
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}

					
					}//and run 
			});
		}
	}

	
	//callback for post loading
	class loadPostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					System.out.println(">>>loadPostResponse");

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
							
					if(resp.isStopped()){
						return;
					}
					if(!resp.isError()) {
						Post post=(Post)resp.getResponseObject();
						FrontController.getIstance().showPost(post, false);	
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}
				
				}
			});
		}
	}
}