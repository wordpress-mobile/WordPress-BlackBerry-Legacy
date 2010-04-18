//#preprocess
package com.wordpress.view;


import java.util.Vector;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchEvent;
//#endif

import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.model.MediaEntry;
import com.wordpress.utils.log.Log;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.container.BorderedFocusChangeListenerPatch;
import com.wordpress.view.dialog.InfoView;
import com.wordpress.view.mm.MediaViewMediator;

public class PhotosView extends StandardBaseView {
	
    protected BlogObjectController controller; //controller associato alla view
	private int counterPhotos = 0;
	protected Vector uiLink = new Vector();
	private BorderedFieldManager noPhotoBorderedManager = null;
	protected boolean refreshThumbOnExpose = false; //used when adding a photo, or photo is changed on the FS.
	
    public PhotosView(BlogObjectController _controller) {
    	super(_resources.getString(WordPressResource.TITLE_MEDIA_VIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
    	
    	//init for the 0 photo item
    	noPhotoBorderedManager= new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
    	LabelField noPhoto = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_NO_MEDIA), Color.BLACK);
    	noPhotoBorderedManager.add(noPhoto);
    	
        updateUI(counterPhotos);
        addMenuItem(_addAudioItem);
        addMenuItem(_addPhotoItem);
        addMenuItem(_addVideoItem);
    }
    	
    protected void updateUI(int count) {
    	this.setTitleText(count + " "+_resources.getString(WordPressResource.TITLE_MEDIA_VIEW) );
    	if(count == 0) {
    		add(noPhotoBorderedManager);
    	} else {
    		if(noPhotoBorderedManager.getManager() != null) {
    			delete(noPhotoBorderedManager); 
    		}
    	}
    }
    
    private MenuItem _addPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_PHOTO_ADD, 110, 10) {
        public void run() {
        	controller.showAddMediaPopUp(BlogObjectController.PHOTO);
        }
    };
 	
    private MenuItem _addVideoItem = new MenuItem( _resources, WordPressResource.MENUITEM_VIDEO_ADD, 110, 10) {
        public void run() {
        	controller.showAddMediaPopUp(BlogObjectController.VIDEO);
        }
    };
    
    private MenuItem _addAudioItem = new MenuItem( _resources, WordPressResource.MENUITEM_AUDIO_ADD, 110, 10) {
        public void run() {
        	controller.showAddMediaPopUp(BlogObjectController.AUDIO);
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
        	BrowserSession videoClip = Browser.getDefaultSession();
    		videoClip.displayPage(mediaEntry.getFilePath());
    			
        	/*if(mediaEntry instanceof PhotoEntry) {
        		controller.showEnlargedPhoto(mediaEntry.getFilePath());
        	} else {
        		BrowserSession videoClip = Browser.getDefaultSession();
        		videoClip.displayPage(mediaEntry.getFilePath());
        	}*/
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
    
    protected MenuItem _allOnTopPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_ALLTOP, 100000, 10) {
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
    
     protected MenuItem _allOnBottomPhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_ALLBOTTOM, 100000, 10) {
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
    public void deleteMedia(String key){
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
    
	public boolean onMenu(int instance) {
		boolean result;		
		// Prevent the context menu from being shown if focus
		// is on the bitmap
		if (getLeafFieldWithFocus() instanceof BitmapField 
				&& instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
	}

    //Override the makeMenu method so we can add a custom menu item
    protected void makeMenu(Menu menu, int instance)
    {
    	    	
    	if (getLeafFieldWithFocus() instanceof BitmapField ) {
    		menu.add(_showPhotoItem);
    		menu.add(_showPhotoPropertiesItem);
    		menu.add(_deletePhotoItem);
    	}        

    	addExclusiveMenuItem(menu, instance);
    
        //Create the default menu.
        super.makeMenu(menu, instance);
    }
	
    protected void addExclusiveMenuItem(Menu menu, int instance) {
    	if(uiLink.size() > 0) {
    		menu.add(_allOnTopPhotoItem);
    		menu.add(_allOnBottomPhotoItem);
    	}
    }
    
	
    protected void onExposed() {
    	Log.trace("MediaView - onExposed");
    	controller.removeMediaFileJournalListener(); //remove the fs listener (used only when recording live video)
    	//update the thumbnails
    	if(refreshThumbOnExpose) {
    		Log.trace("MediaView - onExposed - refresh thumbs");
    		for (int i = 0; i < uiLink.size(); i++) {
    			MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(i);
    			MediaEntry mediaEntry = tmpLink.getMediaEntry();
    			Bitmap bitmapThumb = mediaEntry.getThumb();
    			//if thumb is a photo then...
    			if( tmpLink.getField() instanceof BitmapField) {
    				BitmapField bitmapField = (BitmapField) tmpLink.getField();
    				bitmapField.setBitmap(bitmapThumb);
    			} else {
    				//video prev is a bitmap right now!!
    			}
    		}
    		refreshThumbOnExpose = false;
    	}
    	super.onExposed();
    }
    
    protected void onDisplay() {
    	Log.trace("MediaView - OnDisplay");
		controller.removeMediaFileJournalListener(); //remove the fs listener (used only when recording live video)
		refreshThumbOnExpose = false;
    	super.onDisplay();
    }
    
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
                	Field fieldWithFocus = this;
                	MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
                	if(mediaViewMediator != null) {
                		controller.showMediaObjectProperties(mediaViewMediator);
                	}
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
                	Field fieldWithFocus = this;
                	MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
                	if(mediaViewMediator != null) {
                		controller.showMediaObjectProperties(mediaViewMediator);
                	}
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
                	Field fieldWithFocus = this;
                	MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
                	if(mediaViewMediator != null) {
                		controller.showMediaObjectProperties(mediaViewMediator);
                	}
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
		refreshThumbOnExpose = true;
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
        		field.setFocusListener(null);
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