package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.PreferenceController;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.SimpleTimeZone;

public class PreferencesView extends BaseView {
	
    private PreferenceController preferencesController= null;
    private Preferences mPrefs=Preferences.getIstance();
	private ObjectChoiceField audioGroup;
	private ObjectChoiceField photoGroup;
	private ObjectChoiceField videoGroup;
	private ObjectChoiceField timezoneGroup;
	private CheckboxField clientSideConn;
	private HorizontalFieldManager buttonsManager;

	
	 public PreferencesView(PreferenceController _preferencesController) {
	    	super();
	    	this.preferencesController=_preferencesController;
	    	//add a screen title
	        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_APPLICATION),
	                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
	        setTitle(title);
	    	
            addMultimediaOption();
            add(new SeparatorField());
            
            int selected = SimpleTimeZone.getIndexForOffset(mPrefs.getTimeZone().getRawOffset());
            timezoneGroup = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_TIMEZONE),SimpleTimeZone.TIME_ZONE_IDS,selected);
            timezoneGroup.setChangeListener(preferencesController.getTimeZoneListener());
			add( timezoneGroup );
			
			add(new SeparatorField());
			
			clientSideConn=new CheckboxField(_resources.getString(WordPressResource.LABEL_DEVICESIDECONN), mPrefs.isDeviceSideConnection());
			clientSideConn.setChangeListener(preferencesController.getDeviceSideConnListener());
			add(clientSideConn);
             			
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK));
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK));
    		buttonBACK.setChangeListener(preferencesController.getBackButtonListener());
            buttonOK.setChangeListener(preferencesController.getOkButtonListener());
            buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		add(buttonsManager); 
            addMenuItem(_saveItem);
	 }
	      
	 private void addMultimediaOption() {
			//audio config 
			if( MultimediaUtils.supportAudioRecording()){
				String[] lines=MultimediaUtils.getSupportedAudioFormat();
				int selectedIndex=0;
		        for (int i = 0; i < lines.length; i++) {
		        	if(lines[i].equalsIgnoreCase(mPrefs.getAudioEncoding())){
		        		selectedIndex=i;
		    	 	}
				}
		       
		    	audioGroup = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_AUDIOENCODING),lines,selectedIndex);
		    	audioGroup.setChangeListener(preferencesController.getAudioListener());
				add( audioGroup );
			} else {
				LabelField lbl = new LabelField(_resources.getString(WordPressResource.LABEL_AUDIORECORDING_NOTSUPPORTED));
				Font fnt = this.getFont().derive(Font.ITALIC);
				lbl.setFont(fnt);
				add(lbl);
			}
			
			//photo config
			if( MultimediaUtils.supportPhotoCapture()){
				String[] lines=MultimediaUtils.getSupportedPhotoFormat();
				int selectedIndex=0;
		        for (int i = 0; i < lines.length; i++) {
		        	if(lines[i].equalsIgnoreCase(mPrefs.getAudioEncoding())){
		        		selectedIndex=i;
		    	 	}
				}
		       
		    	photoGroup = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_PHOTOENCODING),lines,selectedIndex);
		    	photoGroup.setChangeListener(preferencesController.getPhotoListener());
				add( photoGroup );
			} else {
				LabelField lbl = new LabelField(_resources.getString(WordPressResource.LABEL_PHOTO_NOTSUPPORTED));
				Font fnt = this.getFont().derive(Font.ITALIC);
				lbl.setFont(fnt);
				add(lbl);
			}
			
			//video config
			if(MultimediaUtils.supportVideoRecording()){
				String[] lines=MultimediaUtils.getSupportedVideoFormat();
				int selectedIndex=0;
		        for (int i = 0; i < lines.length; i++) {
		        	if(lines[i].equalsIgnoreCase(mPrefs.getAudioEncoding())){
		        		selectedIndex=i; 
		    	 	}
				}
		        
		    	videoGroup = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_VIDEOENCODING),lines,selectedIndex);
		    	videoGroup.setChangeListener(preferencesController.getVideoListener());
				add( videoGroup );
			} else {
				LabelField lbl = new LabelField(_resources.getString(WordPressResource.LABEL_VIDEORECORDING_NOTSUPPORTED));
				Font fnt = this.getFont().derive(Font.ITALIC);
				lbl.setFont(fnt);
				add(lbl);
			}
		}
	 
	 
	    //create a menu item for users click to save
	    private MenuItem _saveItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVE, 1000, 10) {
	        public void run() {
	        	preferencesController.savePrefAndBack();
	        }
	    };

	 
	 // Handle trackball clicks.
		protected boolean navigationClick(int status, int time) {
			Field fieldWithFocus = UiApplication.getUiApplication().getActiveScreen().getFieldWithFocus();
			if(fieldWithFocus == buttonsManager) { //focus on the bottom buttons, do not open menu on whell click
				return true;
			}
			else 
			 return super.navigationClick(status,time);
		}
	 
	    //override onClose() to display a dialog box when the application is closed    
		public boolean onClose()   {
	    	return preferencesController.dismissView();
	    }
}