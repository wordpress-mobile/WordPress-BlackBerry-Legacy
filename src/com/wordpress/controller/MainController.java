package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.view.MainView;


public class MainController extends BaseController {
	
	private MainView view = null;
	
	public MainController() {
		super();
	}
	
	public void showView(){
		this.view=new MainView(this); //main view init here!.	
		UiApplication.getUiApplication().pushScreen(this.view);
	}
	
	
	public BlogInfo[] getBlogsList() {
		
		BlogInfo[] blogs = new BlogInfo[0];
		
		BlogInfo[]  blogsInLoading = AddBlogsMediator.getIstance().getBlogs();

		try {
			blogs = BlogDAO.getBlogsInfo();
		} catch (Exception e) {
			displayError(e, "Error while loading your blog");
		}

		for (int i = 0; i < blogs.length; i++) {
			BlogInfo currentBlogInfo = blogs[i];
			boolean presence = false;
			int state=-1;
			for (int j = 0; j < blogsInLoading.length; j++) {
				if(currentBlogInfo.equals(blogsInLoading[j])){
					presence = true;
					state = blogsInLoading[j].getState();
					break;
				}
			}
									
			if (!presence) { // already present blog
				currentBlogInfo.setState(BlogInfo.STATE_LOADED);
			} else {
				currentBlogInfo.setState(state);
			}
			 blogs[i] = currentBlogInfo;
		}

		return blogs;
	}


	
	public void deleteBlog(BlogInfo selectedBlog) {
		if (selectedBlog.getState() != BlogInfo.STATE_LOADED) {
			// blog in caricamento
			displayMessage("Loading blog. Try later..");
		} else {
			try {
				BlogDAO.removeBlog(selectedBlog);
			} catch (IOException e) {
				displayError(e, "Error while deleting the blog");
			} catch (RecordStoreException e) {
				displayError(e, "Error while deleting the blog");
			}
		}
	}

	public void addBlogs() {
		FrontController.getIstance().showAddBlogsView();
	}
		
	public void showBlog(BlogInfo selectedBlog){
		
		if( selectedBlog.getState() != BlogInfo.STATE_LOADED ) {
			//blog in caricamento
			displayMessage("Loading blog. Try later..");
		} else {
			
			Blog currentBlog=null;
	   	 try {
	   		 currentBlog=BlogDAO.getBlog(selectedBlog);
	   		 FrontController.getIstance().showBlog(currentBlog);
			} catch (Exception e) {
				displayError(e, "Show Blog Error");
			}
		}
	}
			
	public void refreshView() {
		view.refreshBlogList();
	}	
	
	// Utility routine to ask question about exit application
	public synchronized boolean exitApp() {
		
		boolean inLoadingState = AddBlogsMediator.getIstance().isInLoadingState();
		if( inLoadingState ) {
			displayMessage("There are blogs in loading... Wait until blogs are loaded");
			return false;
		}
		
    	int result=this.askQuestion("Are sure to exit?");   
    	if(Dialog.YES==result) {
    		System.exit(0);
    		return true;
    	} else {
    		return false;
    	}
	}	
	
}