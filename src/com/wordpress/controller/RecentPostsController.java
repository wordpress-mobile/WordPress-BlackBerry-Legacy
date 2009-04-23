package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.RecentPostsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.post.DeletePostConn;
import com.wordpress.xmlrpc.post.RecentPostConn;


public class RecentPostsController extends BaseController implements Observer{
	
	private RecentPostsView view = null;
	ConnectionInProgressView connectionProgressView=null;
	
	public static int PAUSE=0;
	public static int LOADING_POSTS_LIST=1;
	public static int LOADING_POST=2;
	public static int DELETING_POST=3;
	public static int REFRESH_POSTS_LIST=4;

	
	private int state= 0;
	
	private Blog currentBlog=null;
	private Post[] mPosts;
		
	
	public String getCurrentBlogName() {
		return currentBlog.getBlogName();
	}
	
	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public RecentPostsController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
	}
	
	public void showView(){
		if(mPosts == null) return; //TODO handle error here
		System.out.println("numero di post trovati: "+ mPosts.length);
		
		String[] postCaricati= new String[mPosts.length];
		String title;
        for (int i = 0; i < mPosts.length; i++) {
            title = ((Post) mPosts[i]).getTitle();
            if (title == null || title.length() == 0) {
                title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
            }
            postCaricati[i]=title;
        }
		this.view= new RecentPostsView(this,postCaricati);
		UiApplication.getUiApplication().pushScreen(view);
	}

	private void refreshView(){
		if(mPosts == null) return; //TODO handle error here
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

		view.refresh(postCaricati);
	}
	
	
	public void deletePost(int postID){
		 this.setState(DELETING_POST);
		 Post post = (Post) mPosts[postID];
		 DeletePostConn connection = new DeletePostConn (currentBlog.getBlogXmlRpcUrl(),currentBlog.getUsername(),
				 currentBlog.getPassword(),  null, post);
	     connection.addObserver(this);
	     
	     connectionProgressView= new ConnectionInProgressView(
	    		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
   
	    connection.startConnWork(); //starts connection
	    int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
		
	}
	
	
	/** delete post loading callback */
	private void deletePostResponse(Object object) {
		System.out.println(">>>deletePostResponse");

		dismissDialog(connectionProgressView);
		BlogConnResponse resp= (BlogConnResponse) object;
		if(!resp.isError()) {
			if(resp.isStopped()){
				return;
			}

			//reload post from blog
			this.setState(REFRESH_POSTS_LIST);
	        load(); 
			
		} else {  
			final String respMessage=resp.getResponse();
		 	displayError(respMessage);	
		}
		return;
	}
	
	public void update(Observable observable, final Object object) {
	
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				if(state == LOADING_POSTS_LIST || state == REFRESH_POSTS_LIST){
					loadResponse(object);		
				 } else if (state == DELETING_POST){
					 deletePostResponse(object);
				 } 
				 System.out.println("impostato lo stato come pause");
				 state=PAUSE;
			}
		});
	}
	
	/** starts the recent post loading: called by front controller */
	public void loadPosts(){
		this.setState(LOADING_POSTS_LIST);
        load();
	}
	
	/** starts the recent post list refreshing */
	public void refreshPosts(){
		this.setState(REFRESH_POSTS_LIST);
        load();
	}
	
	/** starts the  post loading */
	public void editPost(int selected){
		this.setState(LOADING_POST);
        FrontController.getIstance().showPost();		
	}	


	private void load() {
		Preferences prefs = Preferences.getIstance();
        final RecentPostConn connection = new RecentPostConn (currentBlog.getBlogXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(),  prefs.getTimeZone(), currentBlog, prefs.getRecentPostCount());
        
        connection.addObserver(this); 
         connectionProgressView= new ConnectionInProgressView(
        		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	
	/** recent post loading callback */
	private void loadResponse(Object object) {
		System.out.println(">>>loadPostsResponse");

		dismissDialog(connectionProgressView);
		BlogConnResponse resp= (BlogConnResponse) object;
				
		if(!resp.isError()) {
			if(resp.isStopped()){
				return;
			}
			mPosts= (Post[]) resp.getResponseObject();
			if(this.state == LOADING_POSTS_LIST) { 
				showView();
			} else if( this.state == REFRESH_POSTS_LIST){
				refreshView();
			}
		} else {
			final String respMessage=resp.getResponse();
		 	displayError(respMessage);	
		}
	}
}