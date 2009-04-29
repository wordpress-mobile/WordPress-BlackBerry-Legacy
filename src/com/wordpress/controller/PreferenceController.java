package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ObjectChoiceField;

import com.wordpress.utils.Preferences;
import com.wordpress.utils.SimpleTimeZone;
import com.wordpress.view.PreferencesView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class PreferenceController extends BaseController {
	
	private PreferencesView view = null;
    private Preferences mPrefs=Preferences.getIstance();
    //variables that old the user temporary choices
    private String audioEnc=null;
    private String videoEnc=null;
    private String photoEnc=null;
    private int timezoneIndex=-1;
	private int deviceSideConnection=-1;
	private boolean stateChanged=false; //hold UI state change
	
	public PreferenceController() {
		super();
		this.view= new PreferencesView(this);		
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	 
	public void savePrefAndBack(){
		savePref();
		backCmd();
	}
	
	private boolean isStateChanged(){
		if(audioEnc != null){
			stateChanged=true;
		}
		if(videoEnc != null){
			stateChanged=true;
		}
		if(photoEnc != null){
			stateChanged=true;
		}
		if(timezoneIndex > 0){
			stateChanged=true;
		}
		if(deviceSideConnection > -1){ 
			stateChanged=true;
		}
		return stateChanged;
	}
	
	
	private void savePref(){
		if(!isStateChanged()) return; //if the state of ui isn't changed return immediatly
		
		try {
			if(audioEnc != null){
				mPrefs.setAudioEncoding(audioEnc);
			}
			if(videoEnc != null){
				mPrefs.setVideoEncoding(videoEnc);
			}
			if(photoEnc != null){
				mPrefs.setPhotoEncoding(photoEnc);
			}
			if(timezoneIndex > 0){
				mPrefs.setTimeZone(new SimpleTimeZone(timezoneIndex));
			}
			if(deviceSideConnection > -1){ 
				mPrefs.setDeviceSideConnection( deviceSideConnection == 1 ? true : false);
			}
			mPrefs.save();
		} catch (RecordStoreException e) {
			displayError(e, "Error while saving setup informations");
		} catch (IOException e) {
			displayError(e, "Error while saving setup informations");
		}
	}
	
	
	private FieldChangeListener timezoneListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	Field original = field.getOriginal();
        	int selected= ((ObjectChoiceField)original).getSelectedIndex();
        	if(selected != -1) {
        		timezoneIndex=selected; //set the user choice
        	}
        }
    };
	
    
	private FieldChangeListener deviceSideConnectionListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	CheckboxField original = (CheckboxField)field.getOriginal();
        	if(original.getChecked()){
        		deviceSideConnection=1;
        	} else {
        		deviceSideConnection=0;
        	}
        }
    };
    
    
	private FieldChangeListener audioListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	Field original = field.getOriginal();
        	int selected= ((ObjectChoiceField)original).getSelectedIndex();
        	if(selected != -1) {
        		String choice=(String)((ObjectChoiceField)original).getChoice(selected);
        		audioEnc=choice; //set the user choice
        	}
        }
    };
    
	private FieldChangeListener videoListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	Field original = field.getOriginal();
        	int selected= ((ObjectChoiceField)original).getSelectedIndex();
        	if(selected != -1) {
        		String choice=(String)((ObjectChoiceField)original).getChoice(selected);
        		videoEnc=choice; //set the user choice
        	}
       
        }
    };
    
	private FieldChangeListener photoListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	Field original = field.getOriginal();
        	int selected= ((ObjectChoiceField)original).getSelectedIndex();
        	if(selected != -1) {
        		String choice=(String)((ObjectChoiceField)original).getChoice(selected);
        		photoEnc=choice; //set the user choice
        	}       
        }
    };


	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	savePrefAndBack();
	    }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	dismissView();
	    }
	};

	public FieldChangeListener getOkButtonListener() {
		return listenerOkButton;
	}
	   
	public FieldChangeListener getBackButtonListener() {
		return listenerBackButton;
	}
	
	public FieldChangeListener getDeviceSideConnListener() {
		return deviceSideConnectionListener;
	}
	
	public FieldChangeListener getAudioListener() {
		return audioListener;
	}
	
	public FieldChangeListener getVideoListener() {
		return videoListener;
	}
	
	public FieldChangeListener getPhotoListener() {
		return photoListener;
	}
	
	public FieldChangeListener getTimeZoneListener() {
		return timezoneListener;
	}
	
	
	
	public boolean dismissView() {
		if(!isStateChanged()) {
			backCmd();
			return true;
		}
		
    	System.out.println("Sto per mostrare la finestra di dialogo");
		String quest="Changes Made!";
    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
    	int choice=infoView.doModal();    	 
    	if(Dialog.DISCARD == choice) {
    		System.out.println("la scelta dell'utente è discard");
    		backCmd();
    		return true;
    	}else if(Dialog.SAVE == choice) {
    		System.out.println("la scelta dell'utente è save");
    		savePref();
    		backCmd();
    		return true;
    	} else {
    		System.out.println("la scelta dell'utente è cancel");
    		return false;
    	}
	}

	public void refreshView() {
		
	}
	
}