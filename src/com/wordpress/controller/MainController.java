package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.view.MainView;


public class MainController extends BaseController {
	
	private MainView mainView = null;
	 private BlogController blogController = null;
	
	public MainController() {
		super();
		this.mainView=new MainView(this);
		blogController= BlogController.getIstance();
	}
	
	public void showView(){
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
}