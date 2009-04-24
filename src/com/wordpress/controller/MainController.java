package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
import com.wordpress.view.MainView;


public class MainController extends BaseController {
	
	private MainView mainView = null;
	 private BlogController blogController = null;
	
	public MainController() {
		super();
	}
	
	public void showView(){
		this.mainView=new MainView(this); //main view init here!.
		blogController= BlogController.getIstance();
		UiApplication.getUiApplication().pushScreen(this.mainView);
	}
		
	public void deleteBlog(int selectedBlog){
		String blogName= blogController.getBlogNames()[selectedBlog];
			System.out.println("selezionato per la cancellazione: " + blogName);           
        try {
			blogController.removeBlog(blogName);
		} catch (RecordStoreException e) {
			displayError(e, "Error while deleting the blog");
		} catch (IOException e) {
			displayError(e, "Error while deleting the blog");
		}
	}
	
	
	public void showDraftPosts(int selectedBlog){
		Blog currentBlog=null;
   	 try {
   		 currentBlog=blogController.getBlog(selectedBlog);
		} catch (Exception e) {
			displayError(e, "Recent Blog Error");
		}
		if(currentBlog != null) {
			FrontController.getIstance().showDraftPostsView(currentBlog);
		}
	}
	
	
	public void showRecentPosts(int selectedBlog){
		Blog currentBlog=null;
   	 try {
   		 currentBlog=blogController.getBlog(selectedBlog);
		} catch (Exception e) {
			displayError(e, "Recent Blog Error");
		}
		if(currentBlog != null) {
			FrontController.getIstance().showRecentPostsView(currentBlog);
		}
	}
	
	public void refreshBlog(int selectedBlog){
  	 Blog currentBlog=null;
  	 try {
  		 currentBlog=blogController.getBlog(selectedBlog);
		} catch (Exception e) {
			displayError(e, "Refresh Blog Error");
		}
		if(currentBlog != null) {
			FrontController.getIstance().refreshBlog(currentBlog);
		}
	}
}