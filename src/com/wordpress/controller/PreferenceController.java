package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.TextField;

import com.wordpress.utils.Preferences;
import com.wordpress.utils.StringUtils;
import com.wordpress.view.PreferencesView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class PreferenceController extends BaseController {
	
	private PreferencesView view = null;
    private Preferences mPrefs=Preferences.getIstance();
    //variables that old the user temporary choices
    private String audioEnc=null;
    private String videoEnc=null;
    private String photoEnc=null;
    private int maxPostCount=-1;
    
    
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
			mPrefs.save();
		} catch (RecordStoreException e) {
			displayError(e, "Error while saving setup informations");
		} catch (IOException e) {
			displayError(e, "Error while saving setup informations");
		}
	}
	
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