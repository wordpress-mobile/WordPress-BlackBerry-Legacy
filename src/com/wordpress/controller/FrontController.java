package com.wordpress.controller;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Post;
import com.wordpress.utils.log.Log;
import com.wordpress.view.BaseView;
import com.wordpress.view.MainView;


/*
 * a soft of generic front controller.
 */

public class FrontController {
	private static FrontController singletonObject;
	
	public static FrontController getIstance() {
		if (singletonObject == null) {
			singletonObject = new FrontController();
		}
		return singletonObject;
	}
    
    //singleton
    private FrontController() {

    }
    
	
	/**
	 * show About view
	 */
	public void showAboutView(){
		AboutController abtCtrl=new AboutController();
		abtCtrl.showView();
	}
		
	/**
	 * show setupUp view
	 */
	public void showSetupView(){
		PreferenceController ctrl=new PreferenceController();
		ctrl.showView();		
	}
	
	/**
	 * show Comment Notification view
	 */
	public void showNotificationView(BlogInfo[] blogs){
		NotificationController ctrl = new NotificationController(blogs);
		ctrl.showView();		
	}
	
	/**
	 * show blog  view
	 */
	public void showBlog(BlogInfo currentBlog){
		BlogController ctrl=new BlogController(currentBlog);
		ctrl.showView();
	}
	
	
	/**
	 * show blog options view
	 */
	public void showBlogOptions(Blog currentBlog){
		BlogOptionsController ctrl=new BlogOptionsController(currentBlog);
		ctrl.showView();
	}
	
	
	/**
	 * show posts view
	 */
	public void showPostsView(Blog currentBlog){
		PostsController ctrl=new PostsController(currentBlog);
		ctrl.showView();
	}
	
	
	
	/**
	 * show draft post view
	 */
	public void showDraftPostsView(Blog currentBlog){
		DraftPostsController ctrl=new DraftPostsController(currentBlog);
		ctrl.showView();
	}
	
	/**
	 * refresh all the blog local informations
	 */
	public void refreshBlog(Blog currentBlog){
		RefreshBlogController ctrl=new RefreshBlogController(currentBlog);
		ctrl.refreshBlog();
	}
		
	/**
	 * show->edit post
	 */
	public void showPost(Post post){
		//read custom field and set the post.isLocation = true  if location fields are present
		Vector customFields = post.getCustomFields();
    	int size = customFields.size();
    	Log.debug("Found "+size +" custom fields");
    	
		for (int i = 0; i <size; i++) {
			Log.debug("Elaborating custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)customFields.elementAt(i);
				
				String ID = (String)customField.get("id");
				String key = (String)customField.get("key");
				String value = (String)customField.get("value");
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				//find the lat/lon field
				if(key.equalsIgnoreCase("geo_longitude")) {
					post.setLocation(true); //set the post as geo-tagged
					Log.debug("Location Custom Field  found!");
				} 
			} catch(Exception ex) {
				Log.error("Error while Elaborating custom field # "+ i);
			}
		}
		
		PostController ctrl=new PostController(post);
		ctrl.showView();
	}
	
	/**
	 * show draft post 
	 */
	public void showDraftPost(Post post, int draftId){
		PostController ctrl=new PostController(post,draftId);
		ctrl.showView();
	}
	
	/**
	 * show new post view
	 */
	public void newPost(Blog currentBlog) {
		Post post = new Post(currentBlog);
		//check if the blog has the location enabled by default
		boolean location = post.getBlog().isLocation();
		post.setLocation(location);
		PostController ctrl=new PostController(post);
		ctrl.showView();
	}
	
	
	/**
	 * show comments view
	 */
	public void showCommentsView(Blog currentBlog){
		CommentsController ctrl=new RecentCommentsController(currentBlog);
		ctrl.showView();
	}
	
	/*
	 * show comments by post view
	 */
	public void showCommentsByPost(Blog currentBlog, int postID, String postTitle){
		FilteredCommentsController ctrl=new FilteredCommentsController(currentBlog);
		ctrl.setPostID(postID);
		ctrl.setPostTitle(postTitle);
		ctrl.showView();
	}
	

	/**
	 * pop 1 screen out of the stack and refresh the view.
	 * 
	 */
	public void backAndRefreshView(final boolean wasRemoteUpdate){
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				
				Screen scr=UiApplication.getUiApplication().getActiveScreen();
				UiApplication.getUiApplication().popScreen(scr);
				scr=UiApplication.getUiApplication().getActiveScreen();
				
				if (scr instanceof BaseView){	
					BaseController controller = ((BaseView)scr).getController();
					if(wasRemoteUpdate) {
						controller.refreshView();
					} 
					
				}
			} //end run
		});
	}
	
	/**
	 * pop out screens form the stack until MainView found.
	 * Then Refresh the main view.
	*/ 
	public void backToMainView(){
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				
				Screen scr;
				while ((scr=UiApplication.getUiApplication().getActiveScreen()) != null){
					if (scr instanceof MainView) {		
						BaseController controller = ((BaseView)scr).getController();
						controller.refreshView();
						break;
					} else {
						UiApplication.getUiApplication().popScreen(scr);
					}
				}
			}
		});
	}
}