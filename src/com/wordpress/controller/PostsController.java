package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
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
	private Post[] mPosts;

 
	public PostsController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
	}
				
	public String getBlogName() {
		return currentBlog.getBlogName();
	}
	
	public void showView(){
		loadPosts(); //we do not show ui immediatly, loading recent post first.
	}
	
	private void buildUI(){
		final String[] postCaricati = getPostsTitle();
        if(postCaricati == null) return;
        
		this.view= new PostsView(this,postCaricati);
		UiApplication.getUiApplication().pushScreen(view);
	}

	

	private String[] getPostsTitle() {
		if(mPosts == null) return null;
		System.out.println("numero di post trovati: "+ mPosts.length);
		
		final String[] postCaricati= new String[mPosts.length];
		String title;
        for (int i = 0; i < mPosts.length; i++) {
            title = ((Post) mPosts[i]).getTitle();
            if (title == null || title.length() == 0) {
                title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
            }
            postCaricati[i]=title;
        }
		return postCaricati;
	}
		
	
	/** starts the recent post loading: called by front controller */
	public void loadPosts(){
		System.out.println(">>>loadRecentPosts");
		Preferences prefs = Preferences.getIstance();
        final RecentPostConn connection = new RecentPostConn (currentBlog.getBlogXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(),  prefs.getTimeZone(), currentBlog, currentBlog.getMaxPostCount());
        
        connection.addObserver(new loadRecentPostsCallBack()); 
        String connMsg=_resources.getString(WordPressResource.CONN_LOADING_POSTLIST);
        connectionProgressView= new ConnectionInProgressView(connMsg);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}		
	}
	
	/** starts the recent post list refreshing */
	public void refreshPosts(){
		System.out.println(">>>refreshPosts");
		Preferences prefs = Preferences.getIstance();
        final RecentPostConn connection = new RecentPostConn (currentBlog.getBlogXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(),  prefs.getTimeZone(), currentBlog, currentBlog.getMaxPostCount());
        
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
	
	/** starts the  post loading */
	public void editPost(int selected){
		if(selected != -1){
			
			Post post = (Post) mPosts[selected];
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
		
			 Post post = (Post) mPosts[postID];
			 DeletePostConn connection = new DeletePostConn (currentBlog.getBlogXmlRpcUrl(),currentBlog.getUsername(),
					 currentBlog.getPassword(),  null, post);
		     
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

	//called from the front controller
	public void refreshView() {
		refreshPosts();
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
				        
						refreshPosts();
				        
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
						mPosts= (Post[]) resp.getResponseObject();					
						final String[] postCaricati = getPostsTitle();
				        if(postCaricati == null) return; 
						view.refresh(postCaricati);
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}

					
					}//and run 
			});
		}
	}
	
	class loadRecentPostsCallBack implements Observer{
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
						mPosts= (Post[]) resp.getResponseObject();
						buildUI();
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