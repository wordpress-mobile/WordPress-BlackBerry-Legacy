//#preprocess
package com.wordpress.view;

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchEvent;
//#endif

import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.FlowFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MediaLibraryController;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.MediaLibrary;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.HorizontalPaddedFieldManager;

public class MediaLibraryView extends StandardBaseView {
	
	private MediaLibraryController controller;
	private MediaLibrary entry;
	
	//content of tabs summary
	private BasicEditField title;
	private LabelField lblPhotoNumber;
	private FlowFieldManager thumbManager;
	private CheckboxField cutAndPaste;
	
    public MediaLibraryView(MediaLibraryController _controller, MediaLibrary  entry) {
    	super(_resources.getString(WordPressResource.TITLE_POSTVIEW) , MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
		this.entry = entry;
                
        //row title
    	BorderedFieldManager outerManagerRowTitle = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
        HorizontalFieldManager rowTitle = new HorizontalPaddedFieldManager();
		LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_POST_TITLE)+":");
		title = new BasicEditField("", entry.getTitle(), 100, Field.EDITABLE);
        rowTitle.add(lblTitle);
        rowTitle.add(title);
        outerManagerRowTitle.add(rowTitle);
    	BasicEditField lblTitleDesc = getDescriptionTextField(_resources.getString(WordPressResource.MEDIALIBRARY_SCREEEN_TITLE_DESC));
    	outerManagerRowTitle.add(lblTitleDesc);
        add(outerManagerRowTitle);
        
        //row cut&paste
    	BorderedFieldManager outerManagerCutAndPaste = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
    	
    	cutAndPaste = new CheckboxField(_resources.getString(WordPressResource.LABEL_CUT_AND_PASTE), entry.isCutAndPaste());
    	outerManagerCutAndPaste.add(cutAndPaste);
    	BasicEditField lblCutAndPasteDesc = getDescriptionTextField(_resources.getString(WordPressResource.MEDIALIBRARY_SCREEEN_CUTANDPASTE_DESC));
    	outerManagerCutAndPaste.add(lblCutAndPasteDesc);
        add(outerManagerCutAndPaste);        
        
        
        //row photo #s and thumbs
    	BorderedFieldManager outerManagerRowPhoto = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
         		| Manager.NO_VERTICAL_SCROLL);    	 
    	lblPhotoNumber = GUIFactory.getLabel("", LabelField.FOCUSABLE);
    	lblPhotoNumber.setText(entry.getMediaObjects().size() + " "+_resources.getString(WordPressResource.TITLE_MEDIA_VIEW));    	
        outerManagerRowPhoto.add(lblPhotoNumber);
        
        thumbManager = new FlowFieldManager(Manager.USE_ALL_WIDTH | Manager.NO_HORIZONTAL_SCROLL
         		| Manager.VERTICAL_SCROLL);    
        outerManagerRowPhoto.add(thumbManager);
        
		add(outerManagerRowPhoto);
		addMenuItem(_saveDraftItem);
		addMenuItem(_submitItem);
		addMenuItem(_photosItem);
		addMenuItem(_settingsItem);
		
		updateThumbs();
    }
   
    //save a local copy of post
    private MenuItem _saveDraftItem = new MenuItem( _resources, WordPressResource.MENUITEM_SAVEDRAFT, 100220, 10) {
        public void run() {
    		try {
    			updateModel();
    			controller.saveLibrary();
	    		controller.backCmd();
    		} catch (Exception e) {
    			controller.displayError(e, "Error while saving media library files");
    		}
        }
    };
    
    //send post to blog
    private MenuItem _submitItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SUBMIT, 100230, 10) {
        public void run() {
    		try {
    			updateModel();
   				controller.sendLibraryToBlog();
    		} catch (Exception e) {
    			controller.displayError(e, "Error while sending media files");
    		}
        }
    };
    
   
    private MenuItem _photosItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA, 110, 10) {
        public void run() {
        	controller.showPhotosView();
        }
    };
    
    private MenuItem _settingsItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETTINGS, 110, 10) {
        public void run() {
        	controller.showSettingsView();
        }
    };

    	
    
    /**
     * Change UI Fields "cleanliness" state to false.
     * A field's cleanliness state tracks when changes happen to a field.
     */
    private void cleanFieldState(){
    	title.setDirty(false);
    }
    
       	
	/*
	 * Update Page data model and Track changes.
	 * 
	 * Photos changes are tracked into controller 
	 */
	private void updateModel() throws Exception{	
		
		if(title.isDirty()){
			entry.setTitle(title.getText());
			controller.setObjectAsChanged(true);
			Log.trace("title dirty");
		}
		
		if(cutAndPaste.isDirty()) {
			entry.setCutAndPaste(cutAndPaste.getChecked());
			controller.setObjectAsChanged(true);
		}
	}
	
	
    protected void onExposed() {
    	Log.trace("ON EXPOSED");
    	updateThumbs();
    }


	private void updateThumbs() {
		thumbManager.deleteAll();
    					
    	Vector mediaObjects = entry.getMediaObjects();
		
        //set the media obj numbers label
    	lblPhotoNumber.setText(mediaObjects.size() + " "+_resources.getString(WordPressResource.TITLE_MEDIA_VIEW));    	

    	for (int i =0; i < mediaObjects.size(); i++ ) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			Bitmap bitmapThumb = tmp.getThumb();
			BitmapField bitmapField = new BitmapField(bitmapThumb, 
					BitmapField.FOCUSABLE | BitmapField.FIELD_HCENTER | Manager.FIELD_VCENTER){
				
	            /**
	             * Overrides default implementation.  Performs default action if the 
	             * 4ways trackpad was clicked; otherwise, the default action occurs.
	             * 
	             * @see net.rim.device.api.ui.Screen#navigationClick(int,int)
	             */
	        	protected boolean navigationClick(int status, int time) {
	        		Log.trace(">>> navigationClick");
	        		
	        		if ((status & KeypadListener.STATUS_TRACKWHEEL) == KeypadListener.STATUS_TRACKWHEEL) {
	        			Log.trace("Input came from the trackwheel");
	        			// Input came from the trackwheel
	        			return super.navigationClick(status, time);
	        			
	        		} else if ((status & KeypadListener.STATUS_FOUR_WAY) == KeypadListener.STATUS_FOUR_WAY) {
	        			Log.trace("Input came from a four way navigation input device");
	        			controller.showPhotosView();
	        			return true;
	        		}
	        		return super.navigationClick(status, time);
	        	}
	        	        	
	            //Allow the space bar to toggle the status of the selected row.
	            protected boolean keyChar(char key, int status, int time)
	            {
	                //If the spacebar was pressed...
	                if (key == Characters.SPACE || key == Characters.ENTER)
	                {
	                	controller.showPhotosView();
	                	return true;
	                }
	                return false;
	            }
				
				
	        	//#ifdef IS_OS47_OR_ABOVE
	        	protected boolean touchEvent(TouchEvent message) {
	        		Log.trace(">>> touchEvent");
	                boolean isOutOfBounds = false;
	                int x = message.getX(1);
	                int y = message.getY(1);
	                // Check to ensure point is within this field
	                if(x < 0 || y < 0 || x > this.getExtent().width || y > this.getExtent().height) {
	                    isOutOfBounds = true;
	                }
	                if (isOutOfBounds) return false;
	        		    		
	        		//DOWN, UP, CLICK, UNCLICK, MOVE, and CANCEL. An additional event, GESTURE
	        		int eventCode = message.getEvent();
	        		if(eventCode == TouchEvent.CLICK) {
	        			Log.trace("TouchEvent.CLICK");
	        			controller.showPhotosView();
	        			return true;
	        		}else if(eventCode == TouchEvent.DOWN) {
	        			Log.trace("TouchEvent.CLICK");
	        		} else if(eventCode == TouchEvent.UP) {
	        			Log.trace("TouchEvent.UP");
	        		} else if(eventCode == TouchEvent.UNCLICK) {
	        			Log.trace("TouchEvent.UNCLICK");
	        			return true; //consume the event: avoid context menu!!
	        		} else if(eventCode == TouchEvent.CANCEL) {
	        			Log.trace("TouchEvent.CANCEL");
	        		}
	        		
	        		return false; 
	        		//return super.touchEvent(message);
	        	}
	        	//#endif
				
			};

			bitmapField.setSpace(5, 5);
			thumbManager.add(bitmapField);
    	}//end for 
		
        super.onExposed();
        lblPhotoNumber.setFocus();
	}
    
	
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		try {
			updateModel();
		} catch (Exception e) {
			controller.displayError(e, _resources.getString(WordPressResource.ERROR_WHILE_SAVING_PAGE));
		}
		return controller.dismissView();	
    }
	
	public BaseController getController() {
		return controller;
	}
}