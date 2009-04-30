package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
import com.wordpress.view.MainView;


public class MainController extends BaseController {
	
	private MainView view = null;
	private BlogIOController blogIOController = null;
	
	public MainController() {
		super();
	}
	
	public void showView(){
		this.view=new MainView(this); //main view init here!.
		blogIOController= BlogIOController.getIstance();
		UiApplication.getUiApplication().pushScreen(this.view);
	}
		
	public void deleteBlog(int selectedBlog){
		String blogName= blogIOController.getBlogNames()[selectedBlog];
			System.out.println("selezionato per la cancellazione: " + blogName);           
        try {
			blogIOController.removeBlog(blogName);
		} catch (RecordStoreException e) {
			displayError(e, "Error while deleting the blog");
		} catch (IOException e) {
			displayError(e, "Error while deleting the blog");
		}
	}
	
	public void showBlog(int selectedBlog){
		Blog currentBlog=null;
   	 try {
   		 currentBlog=blogIOController.getBlog(selectedBlog);
		} catch (Exception e) {
			displayError(e, "Show Blog Error");
		}
		if(currentBlog != null) {
			FrontController.getIstance().showBlog(currentBlog);
		}
	}
	
	
	public void showBlogOptions(int selectedBlog) {
		Blog currentBlog=null;
	  	 try {
	  		 currentBlog=blogIOController.getBlog(selectedBlog);
			} catch (Exception e) {
				displayError(e, "Loading Blog Error");
			}
		if (currentBlog != null) {
			FrontController.getIstance().showBlogOptions(currentBlog);
		}
	}
	
	public void refreshView() {
		view.refreshBlogList();
	}	
}