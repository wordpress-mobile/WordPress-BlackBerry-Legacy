package com.wordpress.controller;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.task.TasksRunner;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.ErrorView;
import com.wordpress.view.dialog.InfoView;
import com.wordpress.view.dialog.InquiryView;

public abstract class BaseController {

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
    
    protected static Queue codaTask = new Queue(); //create empty queue of task
    protected static TasksRunner runner = new TasksRunner (codaTask); //task runner obj
    	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
        runner.startWorker(); //start the task runner thread
    }
  	
    
  abstract 	public void showView();
  abstract 	public void refreshView();
	
  
  	//stop runner thread
	public synchronized void stopAllThread() {
		Log.debug("Runner Thread stopped");
		runner.quit();
	}

	// Utility routine to display errors
	public synchronized void displayError(final Exception e, String message) {
		
		if(e != null && e.getMessage()!= null ) {
			displayError(message + "\n" + e.getMessage());
		} else {
			displayError(message);			
		}
		
	}

	// Utility routine to display errors
	public synchronized void displayError(final String msg) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Log.error(msg);
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
		/*Log.debug(msg);
		InquiryView inqView= new InquiryView(msg);
		return inqView.doModal();*/
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