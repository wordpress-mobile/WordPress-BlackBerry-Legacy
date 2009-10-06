package com.wordpress.controller;

import java.util.Vector;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.view.CommentsView;

public class FilteredCommentsController extends CommentsController{
	
	protected String postTitle = null;
	
	public FilteredCommentsController(Blog currentBlog) {
		super(currentBlog);
	}
	
	public void setPostID(int postID) {
		this.postID = postID;
	}
	
	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}
	
	public void showView() {
		view= new CommentsView(this, storedComments, gravatarController, currentBlog.getName());
		if (postTitle != null) {
			String viewTitle = _resources.getString(WordPressResource.TITLE_COMMENTS);
			viewTitle+= " > "+postTitle;
			view.setTitleText(viewTitle);
		}
		UiApplication.getUiApplication().pushScreen(view);
		refreshComments(false);
	}
	
	protected void storeComment(Vector comments) {
	}	
}


