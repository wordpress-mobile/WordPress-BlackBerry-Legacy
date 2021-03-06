package com.wordpress.view.mm;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.PostController;
import com.wordpress.utils.MultimediaUtils;

public class MultimediaPopupScreen extends PopupScreen {
    
    private int response=-1;
	private BitmapField bitmapFieldCamera;
	private BitmapField bitmapBrowser;
	private ButtonField buttonCamera;
	private ButtonField buttonBrowser;
     
	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

    public MultimediaPopupScreen(int type)
    {
        super(new VerticalFieldManager(Field.FIELD_HCENTER),Field.FOCUSABLE);
        
        if(type == PostController.PHOTO || type == PostController.BROWSER_PHOTO) {
        	buildPhotoUI();
        } else {
        	buildVideoUI();
        }
    }
    
    private void buildVideoUI()
    {

        HorizontalFieldManager hManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
        if(MultimediaUtils.isVideoRecordingSupported()) {
	    	Bitmap _bitmapCamera = Bitmap.getBitmapResource("video_rec.png");
	    	bitmapFieldCamera = new BitmapField(_bitmapCamera, Field.NON_FOCUSABLE | Field.FIELD_HCENTER | Field.FIELD_VCENTER | Field.USE_ALL_HEIGHT);
	    	buttonCamera= new ButtonField(_resources.getString(WordPressResource.QUESTION_ADD_VIDEO_FROM) + " " + _resources.getString(WordPressResource.LABEL_VIDEO_REC));
	    	buttonCamera.setChangeListener(listenerButtonVideo);
	    	hManager.add(bitmapFieldCamera);
	    	hManager.add(buttonCamera);
        }
    	
        HorizontalFieldManager hManager2 = new HorizontalFieldManager(Field.FIELD_HCENTER);
    	Bitmap _bitmapBrowser = Bitmap.getBitmapResource("folder_yellow_open_32.png");
    	bitmapBrowser = new BitmapField(_bitmapBrowser, Field.NON_FOCUSABLE | Field.FIELD_HCENTER | Field.FIELD_VCENTER | Field.USE_ALL_HEIGHT);
    	buttonBrowser= new ButtonField(_resources.getString(WordPressResource.QUESTION_ADD_VIDEO_FROM) + " " +_resources.getString(WordPressResource.LABEL_VIDEO_ADD_FROM_LIB));
    	buttonBrowser.setChangeListener(listenerButtonVideo);
    	hManager2.add(bitmapBrowser);
    	hManager2.add(buttonBrowser);
    	
        add(hManager);
        add(hManager2);
    }
    
    private void buildPhotoUI()
    {
              
        HorizontalFieldManager hManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
        if(MultimediaUtils.isPhotoCaptureSupported()) {
	    	Bitmap _bitmapCamera = Bitmap.getBitmapResource("camera.png");
	    	bitmapFieldCamera = new BitmapField(_bitmapCamera, Field.NON_FOCUSABLE | Field.FIELD_HCENTER | Field.FIELD_VCENTER | Field.USE_ALL_HEIGHT);
	    	buttonCamera= new ButtonField(_resources.getString(WordPressResource.QUESTION_ADD_PHOTO_FROM) + " " + _resources.getString(WordPressResource.LABEL_PHOTO_TAKE_FROM_CAMERA));
	    	buttonCamera.setChangeListener(listenerButtonPhoto);
	    	hManager.add(bitmapFieldCamera);
	    	hManager.add(buttonCamera);
        }
    	
        HorizontalFieldManager hManager2 = new HorizontalFieldManager(Field.FIELD_HCENTER);
    	Bitmap _bitmapBrowser = Bitmap.getBitmapResource("folder_yellow_open_32.png");
    	bitmapBrowser = new BitmapField(_bitmapBrowser, Field.NON_FOCUSABLE | Field.FIELD_HCENTER | Field.FIELD_VCENTER | Field.USE_ALL_HEIGHT);
    	buttonBrowser= new ButtonField(_resources.getString(WordPressResource.QUESTION_ADD_PHOTO_FROM) + " " + _resources.getString(WordPressResource.LABEL_PHOTO_ADD_FROM_LIB));
    	buttonBrowser.setChangeListener(listenerButtonPhoto);
    	hManager2.add(bitmapBrowser);
    	hManager2.add(buttonBrowser);

        add(hManager);
        add(hManager2);
    }
    
	private FieldChangeListener listenerButtonPhoto = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(field == buttonBrowser){
	    		response = PostController.BROWSER_PHOTO;
	    	} else if(field == buttonCamera) {
	    		response = PostController.PHOTO;
	    	}
	    	close();
	   }
	};
	
	private FieldChangeListener listenerButtonVideo = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(field == buttonBrowser){
	    		response = PostController.BROWSER_VIDEO;
	    	} else if(field == buttonCamera) {
	    		response = PostController.VIDEO;
	    	}
	    	close();
	   }
	};

	
    public int getResponse() {
        return response;
    }
        

	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			response = PostController.NONE;
			this.close();
			return true;
		} else
		return super.keyChar(c, status, time);
	}   
	
}
