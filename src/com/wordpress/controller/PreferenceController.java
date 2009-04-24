package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.TextField;

import com.wordpress.utils.Preferences;
import com.wordpress.utils.SimpleTimeZone;
import com.wordpress.utils.StringUtils;
import com.wordpress.view.PreferencesView;


public class PreferenceController extends BaseController {
	
	private PreferencesView view = null;
    private Preferences mPrefs=Preferences.getIstance();
    //variables that old the user temporary choices
    private String audioEnc=null;
    private String videoEnc=null;
    private String photoEnc=null;
    private int maxPostCount=-1;
    private int timezoneIndex=-1;
	private int deviceSideConnection=-1;
	
	public PreferenceController() {
		super();
		this.view= new PreferencesView(this);		
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	
	public void savePref(){
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
			if(maxPostCount > 0){
				mPrefs.setRecentPostCount(maxPostCount);
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

	private FieldChangeListener recentPostListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	Field original = field.getOriginal();
        	String maxPost= ((TextField)original).getText();
        	int maxPostSanitized = StringUtils.ensureInt(maxPost,-1); //if value of the Field is empty return -1 
			if(maxPostSanitized > 0) {
        		maxPostCount=maxPostSanitized; //set the user choice
        	}       
        }
    };

    
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
	
	public FieldChangeListener getRecentPostListener() {
		return recentPostListener;
	}
	
	public FieldChangeListener getTimeZoneListener() {
		return timezoneListener;
	}
	
	
	// Utility routine to discard the question about change made in Screen component and save
	public boolean discardChangeInquiry() {
		savePref();
		backCmd();
		return true;
		/*
		
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
    		return true;
    	} else {
    		System.out.println("la scelta dell'utente è cancel");
    		return false;
    	}
    	*/
	}
	
	

		
	
}