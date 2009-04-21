package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.text.NumericTextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.PreferenceController;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.SimpleTimeZone;

public class PreferencesView extends BaseView {
	
    private PreferenceController preferencesController= null;
    private Preferences mPrefs=Preferences.getIstance();
	private BasicEditField maxRecentPost;
	private BasicEditField maxBodySize;
	private ObjectChoiceField audioGroup;
	private ObjectChoiceField photoGroup;
	private ObjectChoiceField videoGroup;
	private ObjectChoiceField timezoneGroup;
	
	 public PreferencesView(PreferenceController _preferencesController) {
	    	super();
	    	this.preferencesController=_preferencesController;
	    	//add a screen title
	        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_APPLICATION),
	                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
	        setTitle(title);
	    	
	        maxBodySize = new BasicEditField(_resources.getString(WordPressResource.LABEL_MAXBODYSIZE), "", 60, Field.EDITABLE);
	        maxBodySize.setFilter(new NumericTextFilter());
	        	        
            maxRecentPost = new BasicEditField(_resources.getString(WordPressResource.LABEL_MAXRECENTPOST), String.valueOf(mPrefs.getRecentPostCount()), 100, Field.EDITABLE);
            maxRecentPost.setFilter(new NumericTextFilter());
            
            add(maxRecentPost);
            add(maxBodySize);
            add(new SeparatorField());
            addMultimediaOption();
            add(new SeparatorField());
            
            int selected = SimpleTimeZone.getIndexForOffset(mPrefs.getTimeZone().getRawOffset());
            timezoneGroup = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_TIMEZONE),SimpleTimeZone.TIME_ZONE_IDS,selected);
            timezoneGroup.setChangeListener(preferencesController.getTimeZoneListener());
			add( timezoneGroup );
            
            maxRecentPost.setChangeListener(preferencesController.getRecentPostListener()); 
            //maxBodySize.setChangeListener(preferencesController.getButtonListener());
            //FIXME: we needs max body size setup??
            
          //  addMenuItem(_saveItem); autosave like iphone??
            addMenuItem(_backItem);
            
	 }
	  
	    //create a menu item for users click to save
	    private MenuItem _saveItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVE, 1000, 10) {
	        public void run() {
	        	preferencesController.savePref();
	        }
	    };
	    
	
	    protected MenuItem _backItem = new MenuItem( _resources, WordPressResource.MENUITEM_BACK, 110, 10) {
	            public void run() {
	            	preferencesController.backCmd();
	            }
	    };  
	    
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
			}
			
		}
	 
	    //override onClose() to display a dialog box when the application is closed    
		public boolean onClose()   {
	    	return preferencesController.discardChangeInquiry();
	    }
}