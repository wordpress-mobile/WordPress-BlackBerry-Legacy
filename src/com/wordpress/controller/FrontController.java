package com.wordpress.controller;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
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
	 * show blog  view
	 */
	public void showBlog(Blog currentBlog){
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
	 * show edit post view
	 */
	public void showPost(Post post){
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
	public void newPost(Blog currentBlog){
		Post post =new Post(currentBlog);
		PostController ctrl=new PostController(post);
		ctrl.showView();
	}
	
	
	/**
	 * show comments view
	 */
	public void showCommentsView(Blog currentBlog){
		CommentsController ctrl=new CommentsController(currentBlog);
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