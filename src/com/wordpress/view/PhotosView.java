package com.wordpress.view;


import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.FlowFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.view.mm.PhotoBitmapField;

public class PhotosView extends BaseView {
	
    private BlogObjectController controller; //controller associato alla view
    private FlowFieldManager manager=null;
	private LabelField lblPhotoNumber;
	private int counterPhotos = 0;
	
    public PhotosView(BlogObjectController _controller) {
    	super(_resources.getString(WordPressResource.TITLE_PHOTOSVIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;

  	  //A HorizontalFieldManager to hold the photos number label
        HorizontalFieldManager photoNumberManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH | HorizontalFieldManager.FIELD_HCENTER);

        lblPhotoNumber = getLabel("");
        setNumberOfPhotosLabel(counterPhotos);
        photoNumberManager.add(lblPhotoNumber);
        
    	manager= new FlowFieldManager( Field.FOCUSABLE| VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.VERTICAL_SCROLLBAR);
    	
        addMenuItem(_addPhotoItem);
        add(photoNumberManager);
        add(new SeparatorField());
        add(manager);
    }
    
    //set the photos number label text
    private void setNumberOfPhotosLabel(int count) {
    	lblPhotoNumber.setText(count + " "+_resources.getString(WordPressResource.TITLE_PHOTOSVIEW));
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
    	
    	counterPhotos--;
		setNumberOfPhotosLabel(counterPhotos);
    }
    
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		controller.setPhotosNumber(counterPhotos);
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
		counterPhotos++;
		setNumberOfPhotosLabel(counterPhotos);
	}
}