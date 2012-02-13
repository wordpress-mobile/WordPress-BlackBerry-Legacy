package com.wordpress.controller;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.task.StopConnTask;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PostsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.ParameterizedBlogConn;
import com.wordpress.xmlrpc.post.GetPostConn;
import com.wordpress.xmlrpc.post.RecentPostConn;


public class PostsController extends BaseController{
	
	private PostsView view = null;
	private Blog currentBlog;
	ConnectionInProgressView connectionProgressView=null;
	
	protected RecentPostConn loadMoreConnection = null;
	private boolean isLoadingMore = false;
	private boolean hasMorePosts = true;
	 
	public PostsController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
	}
				
	public Blog getBlog() {
		return currentBlog;
	}
	
	public String getBlogName() {
		return currentBlog.getName();
	}
		
	public void showView(){
		this.view= new PostsView(this,currentBlog.getRecentPostTitles());
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
			final GetPostConn connection = new GetPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
	        		currentBlog.getPassword(), currentBlog, String.valueOf(postData.get("postid")) );
	        connection.addObserver(new LoadPostCallBack());  
	        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_LOADING_POST));
	       
	        connection.startConnWork(); //starts connection
					
			int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				WordPressCore.getInstance().getTasksRunner().enqueue(new StopConnTask(connection));
			}
		}	     	
	}	

	public void deletePost(int postListIndex){
		if(postListIndex == -1) return;
		
		int result=this.askQuestion(_resources.getString(WordPressResource.MESSAGE_DELETE_POST));   
		
    	if(Dialog.YES==result) {
		
    		Hashtable postData = (Hashtable) currentBlog.getRecentPostTitles().elementAt(postListIndex);
			String postID = String.valueOf(postData.get("postid"));
			
			Vector args = new Vector();
			args.addElement(""); // appkkey
			args.addElement(postID);
			args.addElement(currentBlog.getUsername());
			args.addElement(currentBlog.getPassword());
				 
			ParameterizedBlogConn connection = new ParameterizedBlogConn (currentBlog.getXmlRpcUrl(), "blogger.deletePost", args);
	   		if(currentBlog.isHTTPBasicAuthRequired()) {
				connection.setHttp401Password(currentBlog.getHTTPAuthPassword());
				connection.setHttp401Username(currentBlog.getHTTPAuthUsername());
			}
			 connection.addObserver(new DeletePostCallBack(postID));
		     
		     connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_DELETE_POST));
	  
		    connection.startConnWork(); //starts connection
		    int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				WordPressCore.getInstance().getTasksRunner().enqueue(new StopConnTask(connection));
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
		view.refresh(currentBlog.getRecentPostTitles());
	}
	
	public void refreshPostsList() {
		//stop the load more and reset the variable here
		if( isLoadingMore ) view.showLoadMoreStatus( false );
		isLoadingMore = false;
		hasMorePosts = true;
		if( loadMoreConnection != null ) loadMoreConnection.stopConnWork();
				
        final RecentPostConn connection = new RecentPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(), currentBlog, currentBlog.getMaxPostCount());
        connection.addObserver(new RefreshRecentPostCallBack()); 
        String connMsg=_resources.getString(WordPressResource.CONN_REFRESH_POSTLIST);
        connectionProgressView= new ConnectionInProgressView(connMsg);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			WordPressCore.getInstance().getTasksRunner().enqueue(new StopConnTask(connection));
		}
	}
	
	public void loadMorePosts() {
		Log.debug("Ctrl load more posts");
		if ( hasMorePosts == false || isLoadingMore ) {
			return;
		}
		view.showLoadMoreStatus(true);
		isLoadingMore = true;
        int numerberOfPosts = 0;
        numerberOfPosts = Math.max(currentBlog.getRecentPostTitles().size(), WordPressInfo.DEFAULT_ITEMS_NUMBER);
        if (hasMorePosts) {
            numerberOfPosts += WordPressInfo.DEFAULT_ITEMS_NUMBER;
        } else {
            //removing this block you will enable the refresh of posts when reached the end of the list and no more posts are available
            isLoadingMore = false;
            return;
        }
		
		loadMoreConnection = new RecentPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(), currentBlog, numerberOfPosts);
		loadMoreConnection.addObserver(new LoadMorePostsCallBack());       
		loadMoreConnection.startConnWork(); //starts connection
	}
	
	private class LoadMorePostsCallBack implements Observer{
		public void update(Observable observable, final Object object) {

			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run(){
					view.showLoadMoreStatus(false);
				}
            });
			
			int numberOfRequestedPosts = loadMoreConnection.getNumPosts();
			loadMoreConnection = null;
			isLoadingMore = false;
			BlogConnResponse resp = (BlogConnResponse) object;
			if(resp.isStopped()){
				
				return;
			}
			if(!resp.isError()) {

				final Vector recentPostTitle= (Vector) resp.getResponseObject();
				// If we asked for more and we got what we had, there are no more posts to load
	            if ( numberOfRequestedPosts > WordPressInfo.DEFAULT_ITEMS_NUMBER && (recentPostTitle.size() <= currentBlog.getRecentPostTitles().size()))
	            {
	                hasMorePosts = false;
	            }
	            else if (recentPostTitle.size() == WordPressInfo.DEFAULT_ITEMS_NUMBER)
	            {
	                //we should reset the flag otherwise when you refresh this blog you can't get more than CHUNK_SIZE posts
	            	 hasMorePosts = true;
	            }
	            currentBlog.setRecentPostTitles(recentPostTitle);

				try{
					BlogDAO.updateBlog(currentBlog);							
				} catch (final Exception e) {
				}
	            
	            UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run(){
						view.addPostsToScreen(recentPostTitle);
					}
	            });
			} else {
				hasMorePosts = false; //error response, do not keep downloading more posts
			}
		}
	}
	
	class DeletePostCallBack implements Observer {

		private final String postID;
		public DeletePostCallBack(String postID) {
			super();
			this.postID = postID;
		}

		public void update(Observable observable, final Object object) {
			Log.debug(">>>DeletePostResponse");

			dismissDialog(connectionProgressView);
			BlogConnResponse resp= (BlogConnResponse) object;
			if(resp.isStopped()){
				return;
			}
			if(!resp.isError()) {

				Boolean deleteResp = (Boolean) resp.getResponseObject();
				if(deleteResp.booleanValue()) {
					//delete ok

					Vector recentPostTitles = currentBlog.getRecentPostTitles();

					for (int i = 0; i < recentPostTitles.size(); i++) {
						Hashtable postData = (Hashtable) recentPostTitles.elementAt(i);
						String tmpPostID =(String) postData.get("postid");
						if(tmpPostID.equalsIgnoreCase(postID)){
							recentPostTitles.removeElementAt(i);
							break;
						}
					}			        

					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run(){
							view.refresh(currentBlog.getRecentPostTitles());
						}
					});

					try{
						BlogDAO.updateBlog(currentBlog);							
					} catch (final Exception e) {
						displayError(e,"Error while refreshing blogs");	
					}
				} else {
					displayError("Error while deleting Post");
				}
			} else {  
				final String respMessage=resp.getResponse();
				displayError(respMessage);	
			}
		}
	}

	
	class RefreshRecentPostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			Log.trace(">>>loadRecentPostsResponse");
			dismissDialog(connectionProgressView);
			BlogConnResponse resp= (BlogConnResponse) object;

			if(resp.isStopped()){
				return;
			}
			if(!resp.isError()) {
				Vector recentPostTitle= (Vector) resp.getResponseObject();
				currentBlog.setRecentPostTitles(recentPostTitle);

				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run(){
						view.refresh(currentBlog.getRecentPostTitles());
					}
				});
				try{
					BlogDAO.updateBlog(currentBlog);							
				} catch (final Exception e) {
					displayError(e,"Error while refreshing blogs");	
				}
			} else {
				final String respMessage=resp.getResponse();
				displayError(respMessage);	
			}
		}
	}
	
	//callback for post loading
	class LoadPostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			Log.debug(">>>loadPostResponse");
			dismissDialog(connectionProgressView);
			BlogConnResponse resp= (BlogConnResponse) object;
			if(resp.isStopped()){
				return;
			}
			if(!resp.isError()) {
				final Post post=(Post)resp.getResponseObject();

				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run(){
						FrontController.getIstance().showPost(post, false);
					}
				});
			} else {
				final String respMessage=resp.getResponse();
				displayError(respMessage);	
			}
		}
	}
}