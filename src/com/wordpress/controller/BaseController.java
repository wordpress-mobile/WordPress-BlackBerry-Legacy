//#preprocess
package com.wordpress.controller;

import java.util.Hashtable;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.webtrends.mobile.analytics.IllegalWebtrendsParameterValueException;
import com.webtrends.mobile.analytics.rim.WebtrendsDataCollector;
import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.task.TaskImpl;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.ErrorView;
import com.wordpress.view.dialog.InfoView;
import com.wordpress.view.dialog.InquiryView;

//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
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
  
  /**
   * Tracks instances that a user views a screen in an application. 
   * 
   * @param eventPath - The hierarchical location of where an object or screen is located in an application (for example, "mainscreen/optionscreen/samplemedia").
   * @param eventDesc - The name of the screen where the event occurs.
   * @param eventType - The event type.
   * @param customData - A hashtable containing custom metadata. Custom metadata is not supported for this method, so specify null.
   * @param contentGroup - A category name for the screen (for example, "sports" or "music").
   */
  public synchronized void bumpScreenViewStats(final String eventPath, final String eventDesc, 
		  final String eventType, final Hashtable customData, final String contentGroup) {

	  WordPressCore.getInstance().getTasksRunner().enqueue( new TaskImpl() {
		  public void execute() {
			  try
			  {
				  WebtrendsDataCollector.getInstance().onScreenView(eventPath, eventDesc, eventType, customData, contentGroup);
			  }
			  catch (IllegalWebtrendsParameterValueException err)
			  {
				  WebtrendsDataCollector.getLog().e(err.getMessage());
			  }

		  }
	  }
	  );
  }
  
  public synchronized void bumpErrorStats(final String error){
	  WordPressCore.getInstance().getTasksRunner().enqueue( new TaskImpl() {
		  public void execute() {
			  //Creates a Hashtable object containing the exception that is thrown by the application
			  Hashtable customParams = new Hashtable();
			  customParams.put("WT.er", error);    
			  try
			  {
				  WebtrendsDataCollector.getInstance().onApplicationError("", customParams);
			  }
			  catch (IllegalWebtrendsParameterValueException err)
			  {
				  WebtrendsDataCollector.getLog().e(err.getMessage());
			  }
		  }
	  }
	  );
  }
  
	// Utility routine to display errors
	public synchronized void displayError(final Exception e, String message) {
		if(e != null && e.getMessage()!= null ) {
			Log.error(e, message);
			_displayError(e, message + "\n" + e.getMessage());
		} else {
			_displayError(null, message);			
		}
	}
	
	// Utility routine to display errors
	public synchronized void displayError(final String msg) {
		Log.error(msg);
		_displayError(null, msg);
	}
	
	private void _displayError(final Exception e, final String msg) {
		bumpErrorStats(msg);
	  	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		Screen scr = UiApplication.getUiApplication().getActiveScreen();
		if(scr != null) {
	    	VirtualKeyboard virtKbd = scr.getVirtualKeyboard();
	    	if(virtKbd != null)
	    		virtKbd.setVisibility(VirtualKeyboard.HIDE);
		}
    	//#endif
		
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				ErrorView errView;
				if( e != null )
					errView = new ErrorView(msg, e);
				else
					errView = new ErrorView(msg);
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
		bumpErrorStats(msg);
		
		//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		Screen scr = UiApplication.getUiApplication().getActiveScreen();
		if(scr != null) {
			VirtualKeyboard virtKbd = scr.getVirtualKeyboard();
			if(virtKbd != null)
				virtKbd.setVisibility(VirtualKeyboard.HIDE);
		}
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
				try {
					UiApplication.getUiApplication().popScreen(dlg);
				} catch (Exception e) {
					Log.error(e, "Error while removing the dialog from the screen stack");
				}
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