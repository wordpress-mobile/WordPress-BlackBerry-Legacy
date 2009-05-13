package com.wordpress.controller;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.utils.Preferences;
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
	
	
	private int countRecentPost() {
		Vector recentPostTitles = currentBlog.getRecentPostTitles();
		Vector viewedPost = currentBlog.getViewedPost();
		int count = 0;

		for (int i = 0; i < recentPostTitles.size(); i++) {
			boolean presence = false; 
			Hashtable postData = (Hashtable) recentPostTitles.elementAt(i);
             String postid = (String) postData.get("postid");
			
		    for (int j = 0; j < viewedPost.size(); j++) {
		    	String elementAt = (String) viewedPost.elementAt(j);
		    	if (elementAt.equalsIgnoreCase(postid)) {
		    		presence = true;
		    		break;
		    	}
			}
		    
		    if (!presence) 
		    	count++;
		}
		return count;
	}
	
	public void showView(){
		this.view= new PostsView(this,currentBlog.getRecentPostTitles(), countRecentPost());
		UiApplication.getUiApplication().pushScreen(view);
	}
			
	/** starts the  post loading */
	public void editPost(int selected){
		if(selected != -1){
			
			Hashtable postData = (Hashtable) currentBlog.getRecentPostTitles().elementAt(selected);
			
			String postid= (String) postData.get("postid");
			currentBlog.addViewedPost(postid); //add the current post to the viewed post
			view.refresh(currentBlog.getRecentPostTitles() , countRecentPost()); //refresh the ui 
			
			Post  post = new Post(currentBlog,(String) postData.get("postid"),
                                      (String) postData.get("title"),
                                      (String) postData.get("userid"),
                                      (Date) postData.get("dateCreated"));
			
			Preferences prefs = Preferences.getIstance();
	        
			final GetPostConn connection = new GetPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
	        		currentBlog.getPassword(),  prefs.getTimeZone(), post);
	        
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
					 currentBlog.getPassword(),  null, postid);
		     
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
		if(currentBlog != null) {
			FrontController.getIstance().showDraftPostsView(currentBlog);
		}
	}
		

	public void newPost() {
		if (currentBlog != null) {
			FrontController.getIstance().newPost(currentBlog); // show the new post view
		}
	}

	//usually called from the front controller
	public void refreshView() {
		System.out.println(">>>refreshPosts");
		Preferences prefs = Preferences.getIstance();
        final RecentPostConn connection = new RecentPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(),  prefs.getTimeZone(), currentBlog);
        
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
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
						
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
						
						view.refresh(currentBlog.getRecentPostTitles(), countRecentPost());
						
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
							
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
						
						Vector recentPostTitle= (Vector) resp.getResponseObject();
						currentBlog.setRecentPostTitles(recentPostTitle);
						

						view.refresh(currentBlog.getRecentPostTitles() , countRecentPost());
						
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
							
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
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