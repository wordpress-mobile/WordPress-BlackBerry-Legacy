package com.wordpress.view;


import java.io.IOException;
import java.util.Vector;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
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
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.MediaEntry;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.BorderedFocusChangeListenerPatch;
import com.wordpress.view.mm.MediaViewMediator;

public class PhotosView extends StandardBaseView {
	
    private BlogObjectController controller; //controller associato alla view
	private int counterPhotos = 0;
	private Vector uiLink = new Vector();
	private BorderedFieldManager noPhotoBorderedManager = null;
	private static final String predefinedThumb = "video_thumb.png";
	
	
    public PhotosView(BlogObjectController _controller) {
    	super(_resources.getString(WordPressResource.TITLE_MEDIA_VIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
    	
    	//init for the 0 photo item
    	noPhotoBorderedManager= new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
    	LabelField noPhoto = getLabel(_resources.getString(WordPressResource.LABEL_NO_PHOTO));
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
    	
    	if(count == 0) {
    		add(noPhotoBorderedManager);
    	} else {
    		addMenuItem(_showPhotoItem);
    		addMenuItem(_showPhotoPropertiesItem);
    		addMenuItem(_deletePhotoItem);
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
    		if(fieldWithFocus instanceof BitmapField) {
    			MediaEntry mediaEntry = getMediaEntry((BitmapField) fieldWithFocus);
    			if (mediaEntry == null) {
    				Log.error("Connot find post/page media object in the screen");
    				return;
    			}
    			if(mediaEntry.getType() == MediaEntry.IMAGE_FILE) {
    				controller.showEnlargedPhoto(mediaEntry.getFilePath());
    			} else {
    				BrowserSession videoClip = Browser.getDefaultSession();
    				videoClip.displayPage(mediaEntry.getFilePath());
    			}
    		}
        }
    };
      
    private MenuItem _showPhotoPropertiesItem = new MenuItem( _resources, WordPressResource.MENUITEM_PROPERTIES, 130, 10) {
        public void run() {        	
        	Field fieldWithFocus = getLeafFieldWithFocus();
    		if(fieldWithFocus instanceof BitmapField) {
    			MediaViewMediator mediaViewMediator = getMediator((BitmapField) fieldWithFocus);
    			if(mediaViewMediator != null) {
    				controller.showMediaObjectProperties(mediaViewMediator);
    			}
    		}
        }
    };
    
    private MediaViewMediator getMediator(BitmapField source) {
    	for (int i = 0; i < uiLink.size(); i++) {
    		MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(i);
    		BitmapField bitmapField = tmpLink.getBitmapField();
    		if (source.equals(bitmapField)) {
    			return tmpLink;
    		}
		}
    	return null;
    }

    private MediaEntry getMediaEntry(BitmapField source) {
    	MediaViewMediator mediaViewMediator = getMediator(source);
    	if (mediaViewMediator != null) {
    		return mediaViewMediator.getMediaEntry();
    	}
    	return null;
    }
    
    private MenuItem _deletePhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_REMOVE, 130, 10) {
        public void run() {        	
        	Field fieldWithFocus = getLeafFieldWithFocus();
    		if(fieldWithFocus instanceof BitmapField) {
    			MediaEntry mediaEntry = getMediaEntry((BitmapField) fieldWithFocus);
    			if(mediaEntry != null)
    				controller.deleteLinkToMediaObject(mediaEntry.getFilePath());
    		}
        }
    };
    
    //called from controller: delete the thumbnail
    public void deletePhotoBitmapField(String key){
    	for (int i = 0; i < uiLink.size(); i++) {
    		MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(i);
    		if (tmpLink.getMediaEntry().getFilePath().equalsIgnoreCase(key)) {
    			delete(tmpLink.getManager());
    			uiLink.removeElementAt(i);
    			break;
    		}
    	}
    	
    	counterPhotos--;
		updateUI(counterPhotos);
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
		
	public void addMedia(MediaEntry mediaEntry){
		Bitmap bitmapRescale;

		//if media file is an image create the thumb
		if(mediaEntry.getType() == MediaEntry.IMAGE_FILE) {
			byte[] readFile;
			try {
				readFile = JSR75FileSystem.readFile(mediaEntry.getFilePath());
				EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
				//find the photo size
				int scale = ImageUtils.findBestImgScale(img, 128, 128);
				if(scale > 1)
					img.setScale(scale); //set the scale
				
				bitmapRescale= img.getBitmap();
			} catch (IOException e) {
				Log.error("Error during img preview");
				bitmapRescale = Bitmap.getBitmapResource(predefinedThumb);
			}
		} else {
			bitmapRescale = Bitmap.getBitmapResource(predefinedThumb);
		}
		
    	BitmapField photoBitmapField = new BitmapField(bitmapRescale, 
				BitmapField.FOCUSABLE | BitmapField.FIELD_HCENTER | Manager.FIELD_VCENTER);
		photoBitmapField.setSpace(5, 5);
        
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
        
        innerManager.add(photoBitmapField);
        innerManager.add(new LabelField("  ", LabelField.NON_FOCUSABLE));
        innerManager.add(fromDataManager);
        borderedManager.add(innerManager);
        add(borderedManager);
 
        //	add the fields to the mediator
        MediaViewMediator uiLinker = new MediaViewMediator(mediaEntry, borderedManager, photoBitmapField, filenameField, titleField);
        uiLink.addElement(uiLinker);
        
		counterPhotos++;
		updateUI(counterPhotos);
	}
}