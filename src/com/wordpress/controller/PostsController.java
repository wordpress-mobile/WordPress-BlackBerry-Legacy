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
import com.wordpress.xmlrpc.ParameterizedBlogConn;
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
						
			/*Post  post = new Post(currentBlog,(String) postData.get("postid"),
                                      (String) postData.get("title"),
                                      (String) postData.get("userid"),
                                      (Date) postData.get("dateCreated"));
			*/
	        
			final GetPostConn connection = new GetPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
	        		currentBlog.getPassword(), currentBlog, String.valueOf(postData.get("postid")) );
	        connection.addObserver(new LoadPostCallBack());  
	        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_LOADING_POST));
	       
	        connection.startConnWork(); //starts connection
					
			int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				Log.trace("Chiusura della conn dialog tramite cancel");
				connection.stopConnWork(); //stop the connection if the user click on cancel button
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
		view.refresh(currentBlog.getRecentPostTitles());
	}
	
	public void refreshPostsList() {
		System.out.println(">>>refreshPosts");
        final RecentPostConn connection = new RecentPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(), currentBlog);
        connection.addObserver(new RefreshRecentPostCallBack()); 
        String connMsg=_resources.getString(WordPressResource.CONN_REFRESH_POSTLIST);
        connectionProgressView= new ConnectionInProgressView(connMsg);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	
	class DeletePostCallBack implements Observer {

		private final String postID;
		public DeletePostCallBack(String postID) {
			super();
			this.postID = postID;
		}

		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

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

							view.refresh(currentBlog.getRecentPostTitles());

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


				}//End run 
			});
		}
	}
		
	
	class RefreshRecentPostCallBack implements Observer{
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

						view.refresh(currentBlog.getRecentPostTitles());

						try{
							BlogDAO.updateBlog(currentBlog);							
						} catch (final Exception e) {
							displayError(e,"Error while refreshing blogs");	
						}


					} else {
						final String respMessage=resp.getResponse();
						displayError(respMessage);	
					}


				}//End run 
			});
		}
	}

	
	//callback for post loading
	class LoadPostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					Log.debug(">>>loadPostResponse");

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