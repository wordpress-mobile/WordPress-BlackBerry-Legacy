package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.view.SignUpBlogView;
import com.wordpress.view.StandardBaseView;


public class SignUpBlogController extends BaseController {
	
	private StandardBaseView view = null;

	public SignUpBlogController() {
		super();
		this.view= new SignUpBlogView(this);
	}
		
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}

	public void refreshView() {
		
	}
}