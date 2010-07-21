package com.wordpress.controller;


import net.rim.device.api.ui.UiApplication;

import com.wordpress.model.Blog;
import com.wordpress.view.BlogOptionsView;


public class BlogOptionsController extends BaseController {
	
	private BlogOptionsView view = null;
	private final Blog blog;

	
	public BlogOptionsController(Blog blog) {
		super();
		this.blog = blog;
		this.view= new BlogOptionsView(this,blog);
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public Blog getBlog() {
		return blog;
	}
	
	public String getBlogName() {
		return blog.getName();
	}

	public void refreshView() {
		
	}
}