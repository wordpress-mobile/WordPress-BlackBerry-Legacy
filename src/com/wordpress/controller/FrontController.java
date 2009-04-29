package com.wordpress.controller;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.view.BlogView;
import com.wordpress.view.DraftPostsView;
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
	 * show setupUp view
	 */
	public void showAboutView(){
		AboutController abtCtrl=new AboutController();
		abtCtrl.showView();
	}
		
	/**
	 * show add blogs view
	 */
	public void showAddBlogsView(){
		AddBlogsController ctrl=new AddBlogsController();
		ctrl.showView();
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
	 * show recent post view
	 */
	public void showRecentPostsView(Blog currentBlog){
		RecentPostsController ctrl=new RecentPostsController(currentBlog);
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
	 * refresh a blog
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
	 * 
	 * 
	 */
	public void fromWritingPostToPrevView(final boolean isRemoteUpdate){
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				
				Screen scr=UiApplication.getUiApplication().getActiveScreen();
				UiApplication.getUiApplication().popScreen(scr);
				scr=UiApplication.getUiApplication().getActiveScreen();
				
				if (scr instanceof BlogView) {		
					if(isRemoteUpdate) { 
						BlogController controller = ((BlogView)scr).getController();
						controller.refreshPosts();
					}			
				} else if (scr instanceof DraftPostsView) { 
					DraftPostsController controller = ((DraftPostsView)scr).getController();
					controller.refreshUI();
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
						((MainView)scr).refreshBlogList();
						break;
					} else {
						UiApplication.getUiApplication().popScreen(scr);
					}
				}
			}
		});
	}
}