package com.wordpress.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.AccountsDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.log.Log;
import com.wordpress.view.AccountDetailView;
import com.wordpress.view.AccountsView;
import com.wordpress.view.BaseView;

public class AccountsController extends BaseController {

	private BaseView accountsView= null;
	
	//open the accounts list view
	public AccountsController() {
		super();
		accountsView = new AccountsView(this);
	}
	
	//open the account details view
	public AccountsController(String accountName) {
		super();
		Hashtable applicationAccounts = MainController.getIstance().getApplicationAccounts();
		Hashtable accountHashtable = (Hashtable)applicationAccounts.get(accountName);
		accountsView= new AccountDetailView(this, accountHashtable);
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(accountsView);
	}

	public void refreshView() {
				
	}	
	
	public static synchronized int getAccountsNumber() {
		return MainController.getIstance().getApplicationAccounts().size();
	}
	
	public static synchronized boolean isAccountExist(String username){
		Hashtable applicationAccounts = MainController.getIstance().getApplicationAccounts();
		Hashtable account = (Hashtable)applicationAccounts.get(username);
		if(account == null)
			return false; 
		else
			return true;
	}

	public static synchronized String getAccountPassword(String username) throws  NullPointerException{
		Hashtable applicationAccounts = MainController.getIstance().getApplicationAccounts();
		Hashtable account = (Hashtable)applicationAccounts.get(username);
		return (String) account.get(AccountsDAO.PASSWORD_KEY);
	}
	
	public static synchronized String[] getAccountsName() throws  NullPointerException{
		 final Hashtable accounts = MainController.getIstance().getApplicationAccounts();
		 Enumeration k = accounts.keys();
			String[] accountsList = new String[accounts.size()];
			int i = 0;
			while (k.hasMoreElements()) {
				String key = (String) k.nextElement();
				accountsList[i] = key;
				i++;
			}
			return accountsList;
	}
	
	public static synchronized void storeWPCOMAccount(Blog[] serverBlogs) {

		if (serverBlogs.length == 0) return;
	
		try {
			Hashtable loadAccounts = MainController.getIstance().getApplicationAccounts();
				
			Blog tmpBlog = serverBlogs[0];
			String username = tmpBlog.getUsername();
			String passwd = tmpBlog.getPassword();
			Hashtable accountInfo = new Hashtable();
			accountInfo.put(AccountsDAO.USERNAME_KEY, username);
			accountInfo.put(AccountsDAO.PASSWORD_KEY, passwd);
			accountInfo.put(AccountsDAO.BLOGNUMBER_KEY, ""+serverBlogs.length);
			
			Object object = loadAccounts.get(username);
			if(object == null) {
				//new account detected
				loadAccounts.put(username, accountInfo);
				AccountsDAO.storeAccounts(loadAccounts);
			} else {
				//account already available inside the app
				loadAccounts.put(username, accountInfo);
				AccountsDAO.storeAccounts(loadAccounts);
			}
		} catch (IOException e) {
			Log.error(e, "Error while storing account info");
		} catch (RecordStoreException e) {
			Log.error(e, "Error while storing account info");
		}
	}
	
}
