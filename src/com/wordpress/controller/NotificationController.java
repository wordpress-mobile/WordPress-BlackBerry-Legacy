package com.wordpress.controller;

import java.util.Hashtable;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.BlogDAO;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.log.Log;
import com.wordpress.view.NotificationView;


public class NotificationController extends BaseController {
	
	private NotificationView view = null;
	private BlogInfo[] blogsList = new BlogInfo[0];
	
	public NotificationController() {
		super();
	   	try {
	   			Hashtable blogsInfo = BlogDAO.getBlogsInfo();
	   			blogsList =  (BlogInfo[]) blogsInfo.get("list");
				if(blogsInfo.get("error") != null )
					displayError((String)blogsInfo.get("error"));
				
				int numberOfBlog = blogsList.length;  //get the number of blog
				String[] blogName = new String[numberOfBlog];
				boolean[] blogSelected = new boolean[numberOfBlog];
				
				for (int i = 0; i < blogsList.length; i++) {
					BlogInfo blogInfo = blogsList[i];
					blogName[i] = blogInfo.getName();
					blogSelected[i] = blogInfo.isCommentNotifies();
				}

				this.view= new NotificationView(this, blogName, blogSelected);
				
			} catch (Exception e) {
				Log.error(e, "Error while reading stored blog");
			}
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}
	
/*
	
	private boolean isModified() {
		boolean isModified=false;
		
		String pass= view.getBlogPass();
		String user= view.getBlogUser();
		int maxPostIndex=view.getMaxRecentPostIndex();
		int valueMaxPostCount=AddBlogsController.recentsPostValues[maxPostIndex];
		boolean isResPhotos = view.isResizePhoto();
		 
		if(!blog.getUsername().equals(user) || !blog.getPassword().equals(pass)
			|| blog.getMaxPostCount() != valueMaxPostCount || isResPhotos != blog.isResizePhotos() ) {
			isModified=true;
		}
		return isModified;
	}
	*/

	public void refreshView() {
		
	}
}