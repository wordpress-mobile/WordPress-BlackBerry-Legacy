package com.wordpress.controller;

import com.wordpress.view.MainView;

import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;


/*
 * a soft of generic front controller.
 */

public class FrontController {
	private static FrontController singletonObject;
	
	
	public static FrontController getIstance() {
		if (singletonObject == null) {
			singletonObject = new FrontController();
		}
		return singletonObject;
	}
    
    //singleton
    private FrontController() {

    }
    
	
	/**
	 * show setupUp view
	 */
	public void showAboutView(){
		AboutController abtCtrl=new AboutController();
		abtCtrl.showView();
	}
		
	/**
	 * show setupUp view
	 */
	public void showAddBlogsView(){
		AddBlogsController ctrl=new AddBlogsController();
		ctrl.showView();
	}
	
	
	/**
	 * show setupUp view
	 */
	public void showSetupView(){
		PreferenceController ctrl=new PreferenceController();
		ctrl.showView();		
	}
	
	
	/**
	 * pop out screens form the stack until MainView found.
	 * Then Refresh the main view.
	 */
	public void backToMainView(){
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				
				Screen scr;
				while ((scr=UiApplication.getUiApplication().getActiveScreen()) != null){
					if (scr instanceof MainView) {		
						((MainView)scr).refreshBlogList();
						break;
					} else {
						UiApplication.getUiApplication().popScreen(scr);
					}
				}
			}
		});
	}
}