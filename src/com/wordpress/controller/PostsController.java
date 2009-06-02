package com.wordpress.controller;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.model.Preferences;
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
            String postId = (String) recentPost.get("postid");
			
		    for (int j = 0; j < viewedPostsTitle.size(); j++) {
		    	Hashtable viewedPost = (Hashtable) viewedPostsTitle.elementAt(j);
	            String viewedPostId = (String) viewedPost.get("postid");
		    	
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
	
	/** starts the  post loading */
	public void editPost(int selected){
		if(!checkUserRights()){
			displayMessage("You cannot Manage Post!");
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
	        		currentBlog.getPassword(), currentBlog, (String) postData.get("postid"));
	        
	        connection.addObserver(new loadPostCallBack());  
	        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_LOADING_POST));
	       
	        connection.startConnWork(); //starts connection
					
			int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				System.out.println("Chiusura della conn dialog tramite cancel");
				connection.stopConnWork(); //stop the connection if the user click on cancel button
			}
			
		}	     	
	}	

	public void deletePost(int postID){
		if(postID == -1) return;
		int result=this.askQuestion("Delete selected post?");   
		
    	if(Dialog.YES==result) {
		
    		Hashtable postData = (Hashtable) currentBlog.getRecentPostTitles().elementAt(postID);

			String postid=(String) postData.get("postid");
				 
			DeletePostConn connection = new DeletePostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
					 currentBlog.getPassword(), postid);
		     
			 connection.addObserver(new deletePostCallBack());
		     
		     connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_DELETE_POST));
	  
		    connection.startConnWork(); //starts connection
		    int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				System.out.println("Chiusura della conn dialog tramite cancel");
				connection.stopConnWork(); //stop the connection if the user click on cancel button
			}
    	}
	}
	
			
	public void showDraftPosts(){
		if(!checkUserRights()){
			displayMessage("You cannot Manage Post!");
			return;
		}
		if(currentBlog != null) {
			FrontController.getIstance().showDraftPostsView(currentBlog);
		}
	}
		

	public void newPost() {
		if(!checkUserRights()){
			displayMessage("You cannot Manage Post!");
			return;
		}
		
		if (currentBlog != null) {
			FrontController.getIstance().newPost(currentBlog); // show the new post view
		}
	}

	//called from the front controller
	public void refreshView() {
		//no action..

	}
	
	public void refreshPostsList() {
		System.out.println(">>>refreshPosts");
		Preferences prefs = Preferences.getIstance();
        final RecentPostConn connection = new RecentPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(), currentBlog);
        
        connection.addObserver(new refreshRecentPostCallBack()); 
        String connMsg=_resources.getString(WordPressResource.CONN_REFRESH_POSTLIST);
        connectionProgressView= new ConnectionInProgressView(connMsg);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	
	class deletePostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					System.out.println(">>>deletePostResponse");

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
					
					System.out.println(">>>loadRecentPostsResponse");

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
						FrontController.getIstance().showPost(post);	
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}
				
				}
			});
		}
	}
}