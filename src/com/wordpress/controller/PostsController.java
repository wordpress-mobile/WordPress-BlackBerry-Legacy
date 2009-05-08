package com.wordpress.controller;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.WordPressDAO;
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
		return currentBlog.getBlogName();
	}
	
	public void showView(){
		
		this.view= new PostsView(this,currentBlog.getRecentPostTitles());

		UiApplication.getUiApplication().pushScreen(view);
	}
	
	/**
	 * return the title and the dateCreated or recent posts
	 * @return
	 
	private String[] getRecentPost() {
		Vector recentPostTitles = currentBlog.getRecentPostTitles();
		if(recentPostTitles == null) return new String[0];
		
		String[] postCaricati= new String [recentPostTitles.size()*2]; //double size
		
         for (int i = 0; i < recentPostTitles.size(); i++) {
        	 Hashtable postData = (Hashtable) recentPostTitles.elementAt(i);
             String title = (String) postData.get("title");
             if (title == null || title.length() == 0) {
                 title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
             }
             postCaricati[i]=title;
             
         }
		return postCaricati;
	}
	*/	
		
	/** starts the  post loading */
	public void editPost(int selected){
		if(selected != -1){
			
			Hashtable postData = (Hashtable) currentBlog.getRecentPostTitles().elementAt(selected);

			Post  post = new Post(currentBlog,(String) postData.get("postid"),
                                      (String) postData.get("title"),
                                      (String) postData.get("userid"),
                                      (Date) postData.get("dateCreated"));
			
			Preferences prefs = Preferences.getIstance();
	        
			final GetPostConn connection = new GetPostConn (currentBlog.getBlogXmlRpcUrl(),currentBlog.getUsername(),
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
				 
			DeletePostConn connection = new DeletePostConn (currentBlog.getBlogXmlRpcUrl(),currentBlog.getUsername(),
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
        final RecentPostConn connection = new RecentPostConn (currentBlog.getBlogXmlRpcUrl(),currentBlog.getUsername(),
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
				        
						
						view.refresh(currentBlog.getRecentPostTitles());
						
						try{
							WordPressDAO.updateBlog(currentBlog);							
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
						
						
						view.refresh(currentBlog.getRecentPostTitles());
						
						try{
							WordPressDAO.updateBlog(currentBlog);							
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