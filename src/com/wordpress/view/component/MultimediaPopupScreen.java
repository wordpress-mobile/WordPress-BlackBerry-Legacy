package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.MultimediaController;

public class MultimediaPopupScreen extends PopupScreen implements FocusChangeListener{
    
    private int response=-1;
	private BitmapField bitmapFieldCamera;
	private BitmapField bitmapFieldSound;
	private BitmapField bitmapVideo;
	private BitmapField bitmapBrowser;
     
	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

    
    public MultimediaPopupScreen()
    {
        super(new VerticalFieldManager(),Field.FOCUSABLE);
              
        LabelField question = new LabelField(_resources.getString(WordPressResource.LABEL_MULTIMEDIATYPE));
        add(question);
        add(new SeparatorField());
        HorizontalFieldManager hManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
    	Bitmap _bitmapCamera = Bitmap.getBitmapResource("camera.png");
    	Bitmap _bitmapSound = Bitmap.getBitmapResource("sound.png");
    	Bitmap _bitmapVideo = Bitmap.getBitmapResource("video.png");
    	Bitmap _bitmapBrowser = Bitmap.getBitmapResource("browser.png");

    	bitmapFieldCamera = new BitmapField(_bitmapCamera, Field.FOCUSABLE | Field.FIELD_HCENTER);
    	bitmapFieldSound = new BitmapField(_bitmapSound, Field.FOCUSABLE | Field.FIELD_HCENTER);
    	bitmapVideo = new BitmapField(_bitmapVideo, Field.FOCUSABLE | Field.FIELD_HCENTER);
    	bitmapBrowser = new BitmapField(_bitmapBrowser, Field.FOCUSABLE | Field.FIELD_HCENTER);
    	
    	bitmapFieldCamera.setFocusListener(this);
    	bitmapFieldSound.setFocusListener(this);
    	bitmapVideo.setFocusListener(this);
    	bitmapBrowser.setFocusListener(this);
		    	
    	hManager.add(bitmapFieldCamera);
    	hManager.add(bitmapFieldSound);
    	hManager.add(bitmapVideo);
    	hManager.add(bitmapBrowser);
        add(hManager);
    }
    
    
    public int getResponse() {
        return response;
    }
    
    public void focusChanged(Field field, int eventType) {
		if (eventType == FOCUS_GAINED) {

			Field fieldWithFocus = field.getOriginal();
			if (fieldWithFocus != null) {
				if (fieldWithFocus == bitmapFieldCamera) {
					response = MultimediaController.PHOTO;
				} else if (fieldWithFocus == bitmapFieldSound) {
					response = MultimediaController.AUDIO;
				} else if (fieldWithFocus == bitmapVideo) {
					response = MultimediaController.VIDEO; 
				} else if (fieldWithFocus == bitmapBrowser) {
					response = MultimediaController.BROWSER;
				} else {
					response = -1;
				}
			} else {
				response = -1;
			}
		}
	}
	
    
    
    private void doSelection(){
    	this.close();
    }
    
 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		doSelection();
		return true;
	}

	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			response= -1;
			this.close();
			return true;
		} else if (c == Characters.ENTER) {
			doSelection();
			return true;
		}

		return super.keyChar(c, status, time);
	}   
}
