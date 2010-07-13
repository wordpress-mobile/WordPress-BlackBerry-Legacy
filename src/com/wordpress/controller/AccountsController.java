package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.view.AccountsView;

public class AccountsController extends BaseController {

	private AccountsView accountsView=null;
	
	public AccountsController() {
		super();
		accountsView = new AccountsView(this);
	
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(accountsView);
	}

	public void refreshView() {
				
	}	
}
