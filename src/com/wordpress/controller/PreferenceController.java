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
			savePref();
			backCmd();
		} catch (Exception e) {
			displayError(e, "Error while saving preferences");
			return false;
		}
		return true;
	}
	

	
	private void savePref() throws Exception {
		AppDAO.storeApplicationPreferecens(Preferences.getIstance());
	}
    
	public void refreshView() {
		
	}
	
}