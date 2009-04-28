package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
import com.wordpress.view.MainBlogView;


public class MainBlogController extends BaseController {
	
	private MainBlogView view = null;
	private BlogIOController blogIOController = null;
	private Blog currentBlog;
	 
	public MainBlogController(Blog currentBlog) {
		super();
		view= new MainBlogView(this);
		this.currentBlog=currentBlog;
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(this.view);
	}
		
		
	public void showDraftPosts(){
	/*	Blog currentBlog=null;
   	 try {
   		 currentBlog=blogIOController.getBlog(selectedBlog);
		} catch (Exception e) {
			displayError(e, "Error while getting Blog Data");
		}*/
		if(currentBlog != null) {
			FrontController.getIstance().showDraftPostsView(currentBlog);
		}
	}
	
	
	public void showRecentPosts(){
		/*Blog currentBlog=null;
   	 try {
   		 currentBlog=blogIOController.getBlog(selectedBlog);
		} catch (Exception e) {
			displayError(e, "Recent Blog Error");
		}*/
		if(currentBlog != null) {
			FrontController.getIstance().showRecentPostsView(currentBlog);
		}
	}
	
	public void refreshBlog(){
  	/* Blog currentBlog=null;
  	 try {
  		 currentBlog=blogIOController.getBlog(selectedBlog);
		} catch (Exception e) {
			displayError(e, "Refresh Blog Error");
		}*/
		if(currentBlog != null) {
			FrontController.getIstance().refreshBlog(currentBlog);
		}
	}
	
	public void newPost(){
	  	 /*Blog currentBlog=null;
	  	 try {
	  		 currentBlog=blogIOController.getBlog(selectedBlog);
			} catch (Exception e) {
				displayError(e, "Load Blog info Error");
			}*/
			if(currentBlog != null) {
				FrontController.getIstance().newPost(currentBlog); //show the new post view
			}
		}
}