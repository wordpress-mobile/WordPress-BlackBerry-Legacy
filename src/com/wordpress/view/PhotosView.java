package com.wordpress.view;


import java.util.Vector;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.PhotoEntry;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.BorderedFocusChangeListenerPatch;
import com.wordpress.view.dialog.InfoView;
import com.wordpress.view.mm.MediaViewMediator;

public class PhotosView extends StandardBaseView {
	
    private BlogObjectController controller; //controller associato alla view
	private int counterPhotos = 0;
	private Vector uiLink = new Vector();
	private BorderedFieldManager noPhotoBorderedManager = null;

	
	
    public PhotosView(BlogObjectController _controller) {
    	super(_resources.getString(WordPressResource.TITLE_MEDIA_VIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
    	
    	//init for the 0 photo item
    	noPhotoBorderedManager= new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
    	LabelField noPhoto = getLabel(_resources.getString(WordPressResource.LABEL_NO_MEDIA));
    	noPhotoBorderedManager.add(noPhoto);
    	
        updateUI(counterPhotos);
        addMenuItem(_addPhotoItem);
        addMenuItem(_addVideoItem);
    }
    	
    private void updateUI(int count) {
    	this.setTitleText(count + " "+_resources.getString(WordPressResource.TITLE_MEDIA_VIEW) );
    	removeMenuItem(_deletePhotoItem);
    	removeMenuItem(_showPhotoItem);
    	removeMenuItem(_showPhotoPropertiesItem);
    	removeMenuItem(_allOnTopPhotoItem);
    	removeMenuItem(_allOnBottomPhotoItem);
    	
    	if(count == 0) {
    		add(noPhotoBorderedManager);
    	} else {
    		addMenuItem(_showPhotoItem);
    		addMenuItem(_showPhotoPropertiesItem);
    		addMenuItem(_deletePhotoItem);
    		addMenuItem(_allOnTopPhotoItem);
    		addMenuItem(_allOnBottomPhotoItem);
    		if(noPhotoBorderedManager.getManager() != null) {
    			delete(noPhotoBorderedManager); 
    		}
    	}
    }
    
    private MenuItem _addPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_PHOTOS_ADD, 110, 10) {
        public void run() {
        	controller.showAddMediaPopUp(BlogObjectController.PHOTO);
        }
    };
 	
    private MenuItem _addVideoItem = new MenuItem( _resources, WordPressResource.MENUITEM_VIDEO_ADD, 110, 10) {
        public void run() {
        	controller.showAddMediaPopUp(BlogObjectController.VIDEO);
        }
    };
    
    private MenuItem _showPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_OPEN, 120, 10) {
        public void run() {        	
        	Field fieldWithFocus = getLeafFieldWithFocus();
        	MediaEntry mediaEntry = getMediaEntry(fieldWithFocus);
        	if (mediaEntry == null) {
        		Log.error("Connot find post/page media object in the screen");
        		return;
        	}
        	if(mediaEntry instanceof PhotoEntry) {
        		controller.showEnlargedPhoto(mediaEntry.getFilePath());
        	} else {
        		BrowserSession videoClip = Browser.getDefaultSession();
        		videoClip.displayPage(mediaEntry.getFilePath());
        	}
        }
    };
      
    private MenuItem _showPhotoPropertiesItem = new MenuItem( _resources, WordPressResource.MENUITEM_PROPERTIES, 130, 10) {
        public void run() {        	
        	Field fieldWithFocus = getLeafFieldWithFocus();
        	MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
        	if(mediaViewMediator != null) {
        		controller.showMediaObjectProperties(mediaViewMediator);
        	}
        }
    };
    
    private MediaViewMediator getMediator(Field source) {
    	for (int i = 0; i < uiLink.size(); i++) {
    		MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(i);
    		Field bitmapField = tmpLink.getField();
    		if (source.equals(bitmapField)) {
    			return tmpLink;
    		}
		}
    	return null;
    }

    private MediaEntry getMediaEntry(Field source) {
    	MediaViewMediator mediaViewMediator = getMediator(source);
    	if (mediaViewMediator != null) {
    		return mediaViewMediator.getMediaEntry();
    	}
    	return null;
    }
    
    private MenuItem _deletePhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_REMOVE, 130, 10) {
        public void run() {        	
        	Field fieldWithFocus = getLeafFieldWithFocus();
    			MediaEntry mediaEntry = getMediaEntry(fieldWithFocus);
    			if(mediaEntry != null) {
    				controller.deleteLinkToMediaObject(mediaEntry.getFilePath());
    			}
    		}
    };
    
    private MenuItem _allOnTopPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_ALLTOP, 100000, 10) {
        public void run() {
        	for (int i = 0; i < uiLink.size(); i++) {
        		MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(i);
        		MediaEntry mediaEntry = tmpLink.getMediaEntry();
        		mediaEntry.setVerticalAlignmentOnTop(true);
    		}
        	controller.setObjectAsChanged(true);
			InfoView inqView= new InfoView(_resources.getString(WordPressResource.MESSAGE_MEDIA_ON_TOP));
			inqView.show();
        }
    };
    
     private MenuItem _allOnBottomPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_ALLBOTTOM, 100000, 10) {
        public void run() {
        	for (int i = 0; i < uiLink.size(); i++) {
        		MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(i);
        		MediaEntry mediaEntry = tmpLink.getMediaEntry();
        		mediaEntry.setVerticalAlignmentOnTop(false);
    		}
    		controller.setObjectAsChanged(true);
    		InfoView inqView= new InfoView(_resources.getString(WordPressResource.MESSAGE_MEDIA_ON_BOTTOM));
			inqView.show();
        }
    };
    
    //called from controller: delete the thumbnail
    public void deletePhotoBitmapField(String key){
    	for (int i = 0; i < uiLink.size(); i++) {
    		MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(i);
    		if (tmpLink.getMediaEntry().getFilePath().equalsIgnoreCase(key)) {
    			delete(tmpLink.getManager());
    			counterPhotos--;
    			uiLink.removeElementAt(i);
    			updateUI(counterPhotos);
    			break;
    		}
    	}
    }
    
    protected void onDisplay() {
		super.onDisplay();
		controller.removeMediaFileJournalListener();
    }
    
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose() {
		controller.removeMediaFileJournalListener();
		controller.setPhotosNumber(counterPhotos);
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
	
	private Field buildThumbField(MediaEntry mediaEntry) {

		Bitmap bitmapRescale = mediaEntry.getThumb();
		
		BitmapField photoBitmapField = new BitmapField(bitmapRescale, 
				BitmapField.FOCUSABLE | BitmapField.FIELD_HCENTER | Manager.FIELD_VCENTER);
		photoBitmapField.setSpace(5, 5);
		return photoBitmapField;
/*
		if (mediaEntry instanceof VideoEntry){
			Player capturePlayer = null;
			
			try {
				capturePlayer = javax.microedition.media.Manager.createPlayer(mediaEntry.getFilePath());
				capturePlayer.realize();
				capturePlayer.prefetch();
				Log.trace("getting video controll");
				VideoControl videoControl = (VideoControl)capturePlayer.getControl("javax.microedition.media.control.VideoControl");
				Field videoField = (Field)videoControl.initDisplayMode( VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field" );			 
				videoControl.setDisplaySize(64 , 64);
				videoControl.setVisible(true);

				// get the volume control
				VolumeControl volume = (VolumeControl)capturePlayer.getControl("javax.microedition.media.control.VolumeControl");
				// initialize it to 0
				if(volume != null)
					volume.setLevel(0);

				FramePositioningControl frameControl = (FramePositioningControl)capturePlayer.getControl("javax.microedition.media.control.FramePositioningControl");
				if( frameControl != null )
					frameControl.seek(100);
										
				capturePlayer.start();
				return videoField;
			}
			catch (MediaException pe) {
				Log.error("Error pe");
				
			} catch (IOException ioe) {
				Log.error("Error ioe");				
			}
		}
		
		return photoBitmapField;*/
	}
	
	public void addMedia(MediaEntry mediaEntry){

		Field thumbField = this.buildThumbField(mediaEntry);
		//outer Manager
        BorderedFieldManager borderedManager = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
        
        HorizontalFieldManager innerManager = new HorizontalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
        VerticalFieldManager fromDataManager = new VerticalFieldManager(VerticalFieldManager.NO_VERTICAL_SCROLL | VerticalFieldManager.NO_HORIZONTAL_SCROLL 
        		| Manager.FIELD_VCENTER)
        {//add the focus change listener patch
        	public void add( Field field ) {
        		super.add( field );
        		field.setFocusListener(new BorderedFocusChangeListenerPatch()); 
        	}
        };
	  		  	
        LabelField fileNameLbl = getLabel(_resources.getString(WordPressResource.LABEL_FILE_NAME));        
        fromDataManager.add( fileNameLbl );
        String fileName =  mediaEntry.getFileName() != null ? mediaEntry.getFileName() : "";
        LabelField filenameField = new LabelField(fileName, LabelField.NON_FOCUSABLE | LabelField.ELLIPSIS);        
		fromDataManager.add( filenameField );
        
	//	fromDataManager.add(new LabelField("", Field.NON_FOCUSABLE)); //space between title and filename

		LabelField titleLbl = getLabel(_resources.getString(WordPressResource.LABEL_TITLE));        
        fromDataManager.add( titleLbl );
        String title = mediaEntry.getTitle() != null ? mediaEntry.getTitle() : "";       
        LabelField titleField = new LabelField(title, LabelField.NON_FOCUSABLE | LabelField.ELLIPSIS);
        if(title.equals("")) {
        	//define the italic font
        	Font fnt = this.getFont().derive(Font.ITALIC);
        	titleField.setText("None");
        	titleField.setFont(fnt);
        }

        
		fromDataManager.add( titleField );
        
        innerManager.add(thumbField);
        innerManager.add(new LabelField("  ", LabelField.NON_FOCUSABLE));
        innerManager.add(fromDataManager);
        borderedManager.add(innerManager);
        add(borderedManager);
 
        //	add the fields to the mediator
        MediaViewMediator uiLinker = new MediaViewMediator(mediaEntry, borderedManager, thumbField, filenameField, titleField);
        uiLink.addElement(uiLinker);
        
		counterPhotos++;
		updateUI(counterPhotos);
	}
}