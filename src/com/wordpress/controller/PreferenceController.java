package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.TextField;

import com.wordpress.utils.Preferences;
import com.wordpress.utils.StringUtils;
import com.wordpress.view.PreferencesView;


public class PreferenceController extends BaseController {
	
	private PreferencesView view = null;
    private Preferences mPrefs=Preferences.getIstance();

	public PreferenceController() {
		super();
		this.view= new PreferencesView(this);		
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public void savePref(){
		try {
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
        		mPrefs.setAudioEncoding(choice);
        	}
        }
    };
    
	private FieldChangeListener videoListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	Field original = field.getOriginal();
        	int selected= ((ObjectChoiceField)original).getSelectedIndex();
        	if(selected != -1) {
        		String choice=(String)((ObjectChoiceField)original).getChoice(selected);
        		mPrefs.setVideoEncoding(choice);
        	}

       
        }
    };
    
	private FieldChangeListener photoListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	Field original = field.getOriginal();
        	int selected= ((ObjectChoiceField)original).getSelectedIndex();
        	if(selected != -1) {
        		String choice=(String)((ObjectChoiceField)original).getChoice(selected);
        		mPrefs.setPhotoEncoding(choice);
        	}       
        }
    };

	private FieldChangeListener recentPostListener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	Field original = field.getOriginal();
        	String maxRecentPost= ((TextField)original).getText();
        	StringUtils.ensureInt(maxRecentPost,5);
        	int maxPost = StringUtils.ensureInt(maxRecentPost,5);
			if(maxPost > 0) {
        		mPrefs.setRecentPostCount(maxPost);
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

}