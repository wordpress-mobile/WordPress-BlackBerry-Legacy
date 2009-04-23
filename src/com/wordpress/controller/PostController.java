package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PostView;
import com.wordpress.view.RecentPostsView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class PostController extends BaseController implements Observer{
	
	private PostView view = null;
	ConnectionInProgressView connectionProgressView=null;
	
	
	
	public PostController() {
		super();	
	}
	
	public void showView() {
		this.view= new PostView(this);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public void update(Observable observable, Object object) {
		// TODO Auto-generated method stub
		
	}
	
}