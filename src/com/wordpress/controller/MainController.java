package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.WordPressDAO;
import com.wordpress.model.Blog;
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
		
	public void deleteBlog(int selectedBlog){
		try {
			String blogName= WordPressDAO.getBlogsName()[selectedBlog];
			System.out.println("selezionato per la cancellazione: " + blogName);           
	        WordPressDAO.removeBlog(blogName);
        } catch (IOException e) {
			displayError(e, "Error while deleting the blog");
		}
	}
	
	
	public void showBlog(int selectedBlog){
		Blog currentBlog=null;
   	 try {
   		 currentBlog=WordPressDAO.getBlog(selectedBlog);
		} catch (Exception e) {
			displayError(e, "Show Blog Error");
		}
		if(currentBlog != null) {
			FrontController.getIstance().showBlog(currentBlog);
		}
	}
			
	public void refreshView() {
		view.refreshBlogList();
	}	
}