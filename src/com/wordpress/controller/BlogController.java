package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
import com.wordpress.view.BlogView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class BlogController extends BaseController {
	
	private BlogView view = null;
	private Blog currentBlog;
	
	ConnectionInProgressView connectionProgressView=null;
	
 
	public BlogController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
	}
				
	public String getBlogName() {
		return currentBlog.getName();
	}
	
	public void showView(){
		this.view= new BlogView(this);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public void showComments() {
		if (currentBlog != null) {
			FrontController.getIstance().showCommentsView(currentBlog);
		}
	}
	
	public void showPosts() {
		if (currentBlog != null) {
			FrontController.getIstance().showPostsView(currentBlog);
		}
	}
	
	/** refresh all blog information */
	public void refreshBlog(){
		if(currentBlog != null) {
			FrontController.getIstance().refreshBlog(currentBlog);
		}
	 }
	
		
	public void showBlogOptions() {
		if (currentBlog != null) {
			FrontController.getIstance().showBlogOptions(currentBlog);
		}
	}

	//called from the front controller
	public void refreshView() {
	}	
}