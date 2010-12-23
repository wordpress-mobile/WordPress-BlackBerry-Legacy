package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.AppDAO;
import com.wordpress.model.Preferences;
import com.wordpress.view.PreferencesView;


public class PreferenceController extends BaseController {
	
	private PreferencesView view = null;
	
	
	public PreferenceController() {
		super();
		this.view= new PreferencesView(this);		
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	 
	public boolean savePrefAndBack(){
		try {
			AppDAO.storeApplicationPreferecens(Preferences.getIstance());
			FrontController.getIstance().backToMainView();
		} catch (Exception e) {
			displayError(e, "Error while saving preferences");
			return false;
		}
		return true;
	}
	
	public void refreshView() {
	}
	
}