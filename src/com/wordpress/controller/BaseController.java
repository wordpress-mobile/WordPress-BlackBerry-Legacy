package com.wordpress.controller;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.view.dialog.ErrorView;
import com.wordpress.view.dialog.InfoView;
import com.wordpress.view.dialog.InquiryView;

public abstract class BaseController {

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
  	
  abstract 	public void showView();
  abstract 	public void refreshView();
	
	// Utility routine to display errors
	public synchronized void displayError(final Exception e, String message) {
		displayError(message + "\n" + e.getMessage());
	}

	// Utility routine to display errors
	public synchronized void displayError(final String msg) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				
				System.out.println(msg);
				ErrorView errView = new ErrorView(msg);
				errView.doModal();
				
			}
		});
	}
	
	// Utility routine to display msg
	public synchronized void displayMessage(final String msg) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				
				System.out.println(msg);
				InfoView infoView= new InfoView(msg);
				infoView.doModal();
				
			}
		});
	}
	
	// Utility routine to ask question to the user
	public synchronized int askQuestion(String msg) {
		System.out.println(msg);
		InquiryView inqView= new InquiryView(msg);
		return inqView.doModal();
	}
	
	
	public void dismissDialog(final Dialog dlg) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				dlg.close();
			}
		});
	}
	

	public void backCmd(){ 	
	 	UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Screen scr=UiApplication.getUiApplication().getActiveScreen();
			 	UiApplication.getUiApplication().popScreen(scr);			
			}
		});
	}
	
	// Utility routine to ask question about exit application
	public synchronized boolean exitApp() {
    	int result=this.askQuestion("Are sure to exit?");   
    	if(Dialog.YES==result) {
    		System.exit(0);
    		return true;
    	} else {
    		return false;
    	}
	}	
}