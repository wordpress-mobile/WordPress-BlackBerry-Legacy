//#preprocess
package com.wordpress.controller;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.ErrorView;
import com.wordpress.view.dialog.InfoView;
import com.wordpress.view.dialog.InquiryView;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.VirtualKeyboard;
//#endif

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
		if(e != null && e.getMessage()!= null ) {
			Log.error(e, message);
			_displayError(message + "\n" + e.getMessage());
		} else {
			_displayError(message);			
		}
	}
	
	// Utility routine to display errors
	public synchronized void displayError(final String msg) {
		Log.error(msg);
		_displayError(msg);
	}
	
	private void _displayError(final String msg) {
	  	//#ifdef IS_OS47_OR_ABOVE
		Screen scr = UiApplication.getUiApplication().getActiveScreen();
    	VirtualKeyboard virtKbd = scr.getVirtualKeyboard();
    	if(virtKbd != null)
    		virtKbd.setVisibility(VirtualKeyboard.HIDE);
    	//#endif
		
		
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				ErrorView errView = new ErrorView(msg);
				errView.doModal();
			}
		});
	}
	

	// Utility routine to display errors
	public synchronized void displayErrorAndWait(final String msg) {
		Log.error(msg);
		_displayErrorAndWait(msg);
	}

	public synchronized void displayErrorAndWait(final Exception e, String message) {
		if(e != null && e.getMessage()!= null ) {
			Log.error(e, message);
			_displayErrorAndWait(message + "\n" + e.getMessage());
		} else {
			_displayErrorAndWait(message);			
		}
	}
	
	private void _displayErrorAndWait(final String msg) {
	  	//#ifdef IS_OS47_OR_ABOVE
		Screen scr = UiApplication.getUiApplication().getActiveScreen();
    	VirtualKeyboard virtKbd = scr.getVirtualKeyboard();
    	if(virtKbd != null)
    		virtKbd.setVisibility(VirtualKeyboard.HIDE);
    	//#endif
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				ErrorView errView = new ErrorView(msg);
				errView.doModal();
			}
		});
	}
	
	
	// Utility routine to display msg
	public synchronized void displayMessage(final String msg) {
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				Log.debug(msg);
				InfoView infoView= new InfoView(msg);
				infoView.doModal();
			}
		});
	}
	
	// Utility routine to ask question to the user
	public synchronized int askQuestion(String msg) {
		
		AskThread ask = new AskThread(msg);
    	UiApplication.getUiApplication().invokeAndWait(ask);
    	if (ask.getResponse() == Dialog.YES) {
    	  	Log.debug("user response YES");
    	} else {
    		Log.debug("user response NO");
    	} 
    	
		return ask.getResponse();
	}
	
	
	public void dismissDialog(final Dialog dlg) {
		if(dlg != null && dlg.isDisplayed()) 
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				UiApplication.getUiApplication().popScreen(dlg);
				//dlg.close();
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
	
	// a simple class used to show dialog sync over the main event Thread
	private class AskThread implements Runnable {
		int userResponse;
		final String config; 
		
		public AskThread(String msg){
			config = msg;
		}
		public void run() {
			InquiryView inqView= new InquiryView(config);
			userResponse = inqView.doModal();
		}
		public int getResponse (){
			return userResponse;
		}
	}
	
}