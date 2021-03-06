package com.wordpress.controller;


import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Post;
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
	 * show blog options view
	 */
	public void showBlogOptions(Blog currentBlog){
		BlogOptionsController ctrl=new BlogOptionsController(currentBlog);
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
	 * show->edit post
	 */
	public void showPost(Post post, boolean isPostChanged){
		
		if(LocationHelper.isLocationCustomFieldsAvailable(post))
			post.setLocation(true); //set the post as geo-tagged

		if(LocationHelper.isLocationPublicCustomField(post.getCustomFields()))
			post.setLocationPublic(true); 
		
		PostController ctrl=new PostController(post);
		ctrl.showView();
		
		if(isPostChanged)
			ctrl.setObjectAsChanged(isPostChanged);
	}

	/**
	 * show draft post 
	 */
	public void showDraftPost(Post post, int draftId){
		PostController ctrl=new PostController(post,draftId);
		
		if(LocationHelper.isLocationPublicCustomField(post.getCustomFields()))
			post.setLocationPublic(true); 
		
		if(LocationHelper.isLocationCustomFieldsAvailable(post))
			post.setLocation(true); //set the post as geo-tagged
		
		ctrl.showView();
	}
	
	/**
	 * show new post view
	 */
	public void newPost(Blog currentBlog) {
		Post post = new Post(currentBlog);
		PostController ctrl=new PostController(post);
		ctrl.showView();
	}
	
	/*
	 * show comments by post view
	 */
	public void showCommentsByPost(Blog currentBlog, String postID, String postTitle){
		RecentCommentsController ctrl=new RecentCommentsController(currentBlog);
		ctrl.setPostID(postID);
		ctrl.setPostTitle(postTitle);
		ctrl.showView();
	}

	/**
	 * pop 1 screen out of the stack and refresh the view.
	 * 
	 */
	public void backAndRefreshView(final boolean wasRemoteUpdate){
		final UiApplication uiApplication = UiApplication.getUiApplication();
		uiApplication.invokeLater(new Runnable() {
			public void run() {
				
				if ( uiApplication.getScreenCount() == 1 ) return;
				
				Screen scr = uiApplication.getActiveScreen();
				uiApplication.popScreen(scr);
				scr = uiApplication.getActiveScreen();
				
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