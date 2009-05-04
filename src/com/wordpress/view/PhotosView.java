package com.wordpress.view;


import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.view.mm.PhotoBitmapField;

public class PhotosView extends BaseView {
	
    private PostController controller; //controller associato alla view
    private VerticalFieldManager manager=null;
		
    public PhotosView(PostController _controller, Hashtable initialContent) {
    	super();
    	this.controller=_controller;
        //add a screen title
        LabelField screenTitle = new LabelField(_resources.getString(WordPressResource.TITLE_PHOTOSVIEW),
                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(screenTitle);
        buildUI(initialContent);
        addMenuItem(_addPhotoItem);
    }
    
    private void buildUI(Hashtable initialContent){
    	manager= new VerticalFieldManager( Field.FOCUSABLE| Field.FIELD_HCENTER);
    	
    	if (initialContent == null ) return;    	
    	Enumeration elements = initialContent.keys();
		for ( ; elements.hasMoreElements() ; ) {
			String key = (String) elements.nextElement();
			EncodedImage photo = (EncodedImage) initialContent.get(key);
			addPhoto(key, photo);
		}
		add(manager);
		
    }
    
    private MenuItem _addPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_PHOTOS_ADD, 110, 10) {
        public void run() {
        	controller.showAddPhotoPopUp();
        }
    };
 	
    private MenuItem _showPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_PHOTOS_VIEW, 120, 10) {
        public void run() {        	
        	Field fieldWithFocus = getLeafFieldWithFocus();
    		if(fieldWithFocus instanceof PhotoBitmapField) {
    			String key= ((PhotoBitmapField)fieldWithFocus).getPath();
    			controller.showEnlargedPhoto(key);
    		}
        }
    };
      
    
    private MenuItem _deletePhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_PHOTOS_DELETE, 130, 10) {
        public void run() {        	
        	Field fieldWithFocus = getLeafFieldWithFocus();
    		if(fieldWithFocus instanceof PhotoBitmapField) {
    			String key= ((PhotoBitmapField)fieldWithFocus).getPath();
    			controller.deletePhoto(key);
    		}
        }
    };
    
    
    //called from controller: delete the thumbnail
    public void deletePhotoBitmapField(String key){
    	int fieldCount = manager.getFieldCount();
    	for (int i = 0; i < fieldCount; i++) {
    		Field field = manager.getField(i);
    		if(field instanceof PhotoBitmapField) {
    			String currKey= ((PhotoBitmapField)field).getPath();
    			if(currKey == key ) {
    				manager.delete(field);
    				break; 
    			}
    		}
		}
    	
    	if (manager.getFieldCount() == 0) {
    		removeMenuItem(_deletePhotoItem);
    		removeMenuItem(_showPhotoItem);
    	}
    }
    
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
		
	public void addPhoto(String key, EncodedImage photo){
    	if (manager.getFieldCount() == 0) {
    		addMenuItem(_deletePhotoItem);
    		addMenuItem(_showPhotoItem);
    	}
    	EncodedImage rescaled= MultimediaUtils.bestFit2(photo, 128, 128);
    	Bitmap bitmapRescale= rescaled.getBitmap();
		PhotoBitmapField photoBitmapField = new PhotoBitmapField(bitmapRescale, BitmapField.FOCUSABLE | BitmapField.FIELD_HCENTER, key);
		photoBitmapField.setSpace(5, 5);
		manager.add(photoBitmapField);
	}
}