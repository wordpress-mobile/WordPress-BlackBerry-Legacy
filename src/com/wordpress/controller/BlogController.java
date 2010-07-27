package com.wordpress.controller;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.view.BlogView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class BlogController extends BaseController {
	
	private BlogView view = null;
	private BlogInfo currentBlogInfo;
	private Blog currentBlog = null;
	ConnectionInProgressView connectionProgressView=null;
	
 
	public BlogController(BlogInfo currentBlog) {
		super();
		this.currentBlogInfo=currentBlog;
	}
				
	public String getBlogName() {
		return currentBlogInfo.getName();
	}

	public EncodedImage getBlogIcon() {
		EncodedImage _theImage = null;
		if(currentBlog.isWPCOMBlog()) {
			_theImage = EncodedImage.getEncodedImageResource("wp_blue-s.png");
		} else {
			_theImage = EncodedImage.getEncodedImageResource("wp_grey-s.png");
		}
		return _theImage;
	}

	public void showView(){
		try {
			this.currentBlog = BlogDAO.getBlog(currentBlogInfo);
		} catch (Exception e) {
			displayError(e, "Loading Blog Error");
		}
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
	
	public void showPages() {
		if (currentBlog != null) {
			PagesController ctrl=new PagesController(currentBlog);
			ctrl.showView();
		}
	}
	
	
	public void showMediaLibrary() {
		if (currentBlog != null) {
			MediaLibrariesController ctrl = new MediaLibrariesController(currentBlog);
			ctrl.showView();
		}
	}
	
	public void showStats() {
		if (currentBlog != null) {
			StatsController ctrl = new StatsController(currentBlog);
			ctrl.showView();
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

	public BlogInfo getCurrentBlogInfo() {
		return currentBlogInfo;
	}	
}