package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.view.AboutView;

public class AboutController extends BaseController {

	private AboutView aboutView=null;
	
	public AboutController() {
		super();
		aboutView=new AboutView(this);
	
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(aboutView);
	}
	
	
}
