//#preprocess
package com.wordpress.view;


import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.content.ContentHandler;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;

import net.rim.blackberry.api.browser.Browser;
import net.rim.blackberry.api.browser.BrowserSession;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchEvent;
import com.wordpress.view.touch.BottomBarItem;
import net.rim.device.api.ui.Touchscreen;
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
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.PhotoEntry;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.PillButtonField;
import com.wordpress.view.component.SelectorPopupScreen;
import com.wordpress.view.component.ClickableLabelField;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.container.BorderedFocusChangeListenerPatch;
import com.wordpress.view.dialog.InfoView;
import com.wordpress.view.mm.MediaViewMediator;

public class MediaView extends StandardBaseView {
	
    protected BlogObjectController controller; //controller associato alla view
	private int counterPhotos = 0;
	protected Vector uiLink = new Vector();
	private VerticalFieldManager noMediaContainer = null;
	private Timer timer = new Timer(); //timer used to photoPreview.
	
	//private MediaEntry lastAddedMediaObj = null;
	
    public MediaView(BlogObjectController _controller) {
    	super(_resources.getString(WordPressResource.TITLE_MEDIA_VIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
    	this.controller=_controller;
    	
    	//setting up the box used when there are no media items
    	noMediaContainer= new VerticalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
    	    	
		PillButtonField buttonAddAudio = new PillButtonField(_resources.getString(WordPressResource.MENUITEM_AUDIO_ADD));
		buttonAddAudio.setDrawPosition(PillButtonField.DRAWPOSITION_SINGLE);
		buttonAddAudio.setChangeListener(new FieldChangeListener() {
			 public void fieldChanged(Field field, int context) {
				 controller.showAddMediaPopUp(BlogObjectController.AUDIO);
			 }
		 });
		buttonAddAudio.setMargin( 5, 15, 5, 15 );
		
		PillButtonField buttonAddVideo = new PillButtonField(_resources.getString(WordPressResource.MENUITEM_VIDEO_ADD));
		buttonAddVideo.setDrawPosition(PillButtonField.DRAWPOSITION_SINGLE);
		buttonAddVideo.setChangeListener(new FieldChangeListener() {
			 public void fieldChanged(Field field, int context) {
				 controller.showAddMediaPopUp(BlogObjectController.VIDEO); 
			 }
		 });
		buttonAddVideo.setMargin( 5, 15, 5, 15  );
		
		PillButtonField buttonAddPhoto = new PillButtonField(_resources.getString(WordPressResource.MENUITEM_PHOTO_ADD));
		buttonAddPhoto.setDrawPosition(PillButtonField.DRAWPOSITION_SINGLE);
		buttonAddPhoto.setChangeListener(new FieldChangeListener() {
			 public void fieldChanged(Field field, int context) {
				 controller.showAddMediaPopUp(BlogObjectController.PHOTO);
			 }
		 });
		buttonAddPhoto.setMargin( 5, 15, 5, 15  );
		
		//trick to avoid extra shifting on old devices
		HorizontalFieldManager buttonsManagerOne = new HorizontalFieldManager(Field.FIELD_HCENTER);
		buttonsManagerOne.add(buttonAddPhoto);
		HorizontalFieldManager buttonsManagerTwo = new HorizontalFieldManager(Field.FIELD_HCENTER);
		buttonsManagerTwo.add(buttonAddVideo);
		HorizontalFieldManager buttonsManagerThree = new HorizontalFieldManager(Field.FIELD_HCENTER);
		buttonsManagerThree.add(buttonAddAudio);
		noMediaContainer.add(buttonsManagerOne);
		noMediaContainer.add(buttonsManagerTwo);
		noMediaContainer.add(buttonsManagerThree);
    	
		//end setting up the no media box
		
    	//#ifdef IS_OS47_OR_ABOVE
    	initUpBottomBar();
    	//#endif

    	updateUI(counterPhotos);
        addMenuItem(_addAudioItem);
        addMenuItem(_addPhotoItem);
        addMenuItem(_addVideoItem);
        
    }
    	
	//#ifdef IS_OS47_OR_ABOVE
	private void initUpBottomBar() {
		if (Touchscreen.isSupported() == false) return;
		
		BottomBarItem items[] = new BottomBarItem[4];
		items[0] = new BottomBarItem("bottombar_image.png", "bottombar_image.png", _resources.getString(WordPressResource.MENUITEM_PHOTO_ADD));
		items[1] = new BottomBarItem("bottombar_video.png", "bottombar_video.png", _resources.getString(WordPressResource.MENUITEM_VIDEO_ADD));
		items[2] = new BottomBarItem("bottombar_audio.png", "bottombar_audio.png", _resources.getString(WordPressResource.MENUITEM_AUDIO_ADD));
		items[3] = new BottomBarItem("bottombar_delete.png", "bottombar_disabled.png", _resources.getString(WordPressResource.MENUITEM_MEDIA_REMOVE));
		initializeBottomBar(items);
	}
	
	protected void bottomBarActionPerformed(int mnuItem) {
		switch (mnuItem) {
		case 0:
			controller.showAddMediaPopUp(BlogObjectController.PHOTO);
			break;
		case 1:
			controller.showAddMediaPopUp(BlogObjectController.VIDEO);
			break;
		case 2:
			controller.showAddMediaPopUp(BlogObjectController.AUDIO);
			break;
		case 3:
			removeMediaItem();
			break;
		default:
			break;
		}
	}
	//#endif
    
    
    protected void updateUI(int count) {
    	this.setTitleText(count + " "+_resources.getString(WordPressResource.TITLE_MEDIA_VIEW) );
    	if(count == 0) {
    		add(noMediaContainer);
    		noMediaContainer.setFocus();
    	} else {
    		if(noMediaContainer.getManager() != null) {
    			delete(noMediaContainer); 
    		}
    	}
    	
	    //#ifdef IS_OS47_OR_ABOVE
		if(count == 0) {
			setBottomBarButtonState(3, false); //disable the delete btn
		} else
			setBottomBarButtonState(3, true);
		//#endif
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
    
    private MenuItem _showOpenInNativeBrowserItem = new MenuItem( _resources, WordPressResource.MENUITEM_OPEN, 120, 10) {
    	public void run() {
    		openMediaItemUsingDefaultBrowser();
    	}
    };
    
    private MenuItem _showPhotoPropertiesItem = new MenuItem( _resources, WordPressResource.MENUITEM_PROPERTIES, 130, 10) {
        public void run() {        	
        	showMediaItemProperties();
        }
    };
    
    private MenuItem _deletePhotoItem = new MenuItem( _resources, WordPressResource.MENUITEM_MEDIA_REMOVE, 130, 10) {
    	public void run() {        	
    		removeMediaItem();
    	}
    };
    
    private MenuItem _rotatePhotoItem = new MenuItem("Rotate", 130, 10) {
    	public void run() { 
    		Field fieldWithFocus = getLeafFieldWithFocus();
    		MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
    		if (mediaViewMediator != null) {
    			MediaEntry mediaEntry = mediaViewMediator.getMediaEntry();
    			if(mediaEntry != null) {
    				try {
    					
							Log.trace( " resize2");
							byte[] readFile = JSR75FileSystem.readFile(mediaEntry.getFilePath());
							EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
							
							int scale = ImageUtils.findBestImgScale(img.getWidth(), img.getHeight(), 128, 128);
							if(scale > 1)
								img.setScale(scale); //set the scale
							
							
							byte[] rotatedImg = ImageUtils.rotateImage(img.getBitmap(), 90);
				    		
							Bitmap test = Bitmap.createBitmapFromBytes(rotatedImg, 0, -1, 1);
							((BitmapField)mediaViewMediator.getFields()[0]).setBitmap(test);
				    		invalidate();
						  	
					} catch (Exception e) {
						Log.trace(e, "error during rotating");
					}
    			}
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
    
    protected void removeMediaItem() {
		Field fieldWithFocus = getLeafFieldWithFocus();
		MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
		if (mediaViewMediator != null) {
			MediaEntry mediaEntry = mediaViewMediator.getMediaEntry();
			if(mediaEntry != null) {
				controller.deleteLinkToMediaObject(mediaEntry.getFilePath());
			}
		}
    }
       
    /**
     * This is used when chapi could not find an app to open the media file
     */
    protected void openMediaItemUsingDefaultBrowser() {
		Field fieldWithFocus = getLeafFieldWithFocus();
		if(fieldWithFocus == null) return;
		MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
		MediaEntry mediaEntry = null;
		if (mediaViewMediator != null) {
			mediaEntry = mediaViewMediator.getMediaEntry();
		}
		if (mediaEntry == null) {
			Log.error("Connot find media object in the screen");
			return;
		}
		BrowserSession videoClip = Browser.getDefaultSession();
		videoClip.displayPage(mediaEntry.getFilePath());
    }
    
    protected void openMediaItemUsingCHAPI() {
    	try {
    		Invocation invoc =  buildCHAPIInvocation();
    		if (invoc == null) {
    			return;
    		}
    		// Get access to the Registry and pass it the Invocation
    		Registry registry = Registry.getRegistry(getClass().getName());
    		ContentHandler[] candidates = registry.findHandler(invoc);

    		if(candidates.length == 0) { //there is no ext-app that could open this file
    			Log.trace("there is no ext-app that could open this file, using browser");
    			openMediaItemUsingDefaultBrowser();
    		} else if(candidates.length == 1) { //there is 1 only ext-app that could open this file
    			Log.trace("there is 1 only ext-app that could open this file");
    			invoc.setID(candidates[0].getID());
    			registry.invoke(invoc);
    		} else {
    			String[] appNames = new String[candidates.length];
    			for (int i = 0; i < candidates.length; i++) {
    				appNames[i] = candidates[i].getAppName();
    			}
    			String title = _resources.getString(WordPressResource.MENUITEM_OPEN_IN);
    			SelectorPopupScreen selScr = new SelectorPopupScreen(title, appNames);
    			selScr.pickItem();
    			int selection = selScr.getSelectedItem();
    			if(selection != -1) {
    				invoc.setID(candidates[selection].getID());
    				registry.invoke(invoc);                
    			}
    		}
    	} 
    	catch (Exception ioe)
    	{
    		Log.error(ioe, "Error while finding a chapi endpoint");
    		openMediaItemUsingDefaultBrowser();
    	}
    }
       
    protected void showMediaItemProperties() {
    	Field fieldWithFocus = getLeafFieldWithFocus();
    	if(fieldWithFocus == null) return;
    	MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
    	if(mediaViewMediator != null) {
    		controller.showMediaObjectProperties(mediaViewMediator);
    	}
    }
        
    private MediaViewMediator getMediator(Field source) {
    	for (int i = 0; i < uiLink.size(); i++) {
    		MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(i);
    		Field[] fieldsManaged = tmpLink.getFields();
    		for (int j = 0; j < fieldsManaged.length; j++) {
    			Field tmpField = fieldsManaged[j];
    			if (source.equals(tmpField)) {
    				return tmpLink;
    			}
			}
		}
    	return null;
    }
      
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
		if (instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
	}
	
	protected Invocation buildCHAPIInvocation() {
		try {
			Field fieldWithFocus = getLeafFieldWithFocus();
			if (fieldWithFocus == null)
				return null;
			MediaViewMediator mediaViewMediator = getMediator(fieldWithFocus);
			MediaEntry mediaEntry = null;
			if (mediaViewMediator != null) {
				mediaEntry = mediaViewMediator.getMediaEntry();
			}
			if (mediaEntry == null)
				return null;

			String[] split = StringUtils.split(mediaEntry.getFilePath(), ".");
			String ext = split[split.length - 1];
			String MIMEType = "";
			// Create the Invocation with the file URL
			Invocation invoc = new Invocation(mediaEntry.getFilePath());
			invoc.setResponseRequired(false); // We don't require a response
			// We want to invoke a handler that has registered with ACTION_OPEN
			invoc.setAction(ContentHandler.ACTION_OPEN);			
			MIMEType = MultimediaUtils.getFileMIMEType(ext);
			
			invoc.setType(MIMEType);
			return invoc;
		} catch (Exception ioe) {
			Log.error(ioe, "Error while creating the chapi invocation object");
		}

		return null;
	}
	
	//Override the makeMenu method so we can add a custom menu item
	protected void makeMenu(Menu menu, int instance)
	{

		if (counterPhotos > 0) {
			menu.add(_showPhotoPropertiesItem);
			menu.add(_deletePhotoItem);
			//menu.add(_rotatePhotoItem);
		
			try {
				Invocation invoc =  buildCHAPIInvocation();
				if (invoc != null) {
					// Get access to the Registry and pass it the Invocation
					Registry registry = Registry.getRegistry(getClass().getName());
					ContentHandler[] candidates = registry.findHandler(invoc);
					for (int i = 0; i < candidates.length; i++) {
						MenuItem tmpMnu = createCHAPIMenuItem(invoc, candidates[i]);
						menu.add(tmpMnu);
					}	
					
					if(candidates.length == 0) //add a generic open menu that open the file in the native browser
						menu.add(_showOpenInNativeBrowserItem);
					
				} else {
					//no invocation found.
					menu.add(_showOpenInNativeBrowserItem); //add a generic open menu that open the file in the native browser
				}
			} 
			catch (Exception ioe)
			{
				Log.error(ioe, "Error while creating the chapi menu item");
				menu.add(_showOpenInNativeBrowserItem); //add a generic open menu that open the file in the native browser
			}
		}
		
		addExclusiveMenuItem(menu, instance);
		//Create the default menu.
		super.makeMenu(menu, instance);
	}

	protected MenuItem createCHAPIMenuItem(final Invocation invoc, final ContentHandler handler) {
		String label = _resources.getString(WordPressResource.MENUITEM_OPEN_IN) +" "+ handler.getAppName();
		MenuItem mnuItem= new MenuItem(label, 120, 10) {
			public void run() {
				try
				{   
					// Get access to the Registry and pass it the Invocation
					Registry registry = Registry.getRegistry(getClass().getName());
					invoc.setID(handler.getID());
					registry.invoke(invoc);                
				}
				catch (IOException ioe)
				{
					controller.displayError(ioe, "Error opening the file!");
				}
			}	
		};
		return mnuItem;
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

    	/*update the thumbnails
    	if(lastAddedMediaObj != null) {
    		MediaViewMediator tmpLink = (MediaViewMediator)uiLink.elementAt(uiLink.size()-1);
    		MediaEntry mediaEntry = tmpLink.getMediaEntry();
    		Bitmap bitmapThumb = mediaEntry.getThumb();
    		Field[] fieldsManaged = tmpLink.getFields();
    		if( fieldsManaged[0] instanceof BitmapField) {
    			BitmapField bitmapField = (BitmapField) fieldsManaged[0];
    			bitmapField.setBitmap(bitmapThumb);
    		}
    		lastAddedMediaObj = null;
    	}
    	*/

    	super.onExposed();
    }
    
    protected void onDisplay() {
    	Log.trace("MediaView - OnDisplay");
		controller.removeMediaFileJournalListener(); //remove the fs listener (used only when recording live video)
    	super.onDisplay();
    }
    
	public boolean onClose() {
		controller.removeMediaFileJournalListener();
		controller.setPhotosNumber(counterPhotos);
		controller.backCmd();
		timer.cancel();
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
        			openMediaItemUsingCHAPI();
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
                	openMediaItemUsingCHAPI();
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
        			openMediaItemUsingCHAPI();
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
		
		if(mediaEntry instanceof PhotoEntry) {
			BuildThumbImageTask taskImpl = new BuildThumbImageTask((PhotoEntry)mediaEntry, photoBitmapField);
			timer.schedule(taskImpl, 2000);
		}
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
	
	/* This is used to ensure that last added medio obj preview will be ok 
	public void setLastAddedMediaObj(MediaEntry mediaEntry) {
		lastAddedMediaObj = mediaEntry;
	}*/
	
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
        		field.setFocusListener(null);
        		field.setFocusListener(new BorderedFocusChangeListenerPatch()); 
        	}
        };
	  		  	
        LabelField fileNameLbl = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_FILE_NAME), Color.BLACK);
        fromDataManager.add( fileNameLbl );
        String fileName =  mediaEntry.getFileName() != null ? mediaEntry.getFileName() : "";
        ClickableLabelField filenameField = new ClickableLabelField(fileName, LabelField.FOCUSABLE | LabelField.ELLIPSIS);
        FieldChangeListener listener = new FieldChangeListener() {
        	public void fieldChanged(Field field, int context) {
        		//user clicks on the field (not a change that comes from prop view)
        		if(context == 0){
        			showMediaItemProperties();
        		}
        	}
        };
 		filenameField.setChangeListener(listener);
		fromDataManager.add( filenameField );
        
	//	fromDataManager.add(new LabelField("", Field.NON_FOCUSABLE)); //space between title and filename

		LabelField titleLbl = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_TITLE), Color.BLACK);        
		titleLbl.setMargin(5, 0, 0, 0);
		fromDataManager.add( titleLbl );
        String title = mediaEntry.getTitle() != null ? mediaEntry.getTitle() : "";       
        ClickableLabelField titleField = new ClickableLabelField(title, LabelField.FOCUSABLE | LabelField.ELLIPSIS);
        if(title.equals("")) {
        	//define the italic font
        	Font fnt = this.getFont().derive(Font.ITALIC);
        	titleField.setText("None");
        	titleField.setFont(fnt);
        }
        titleField.setChangeListener(listener);
        
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

	private class BuildThumbImageTask extends TimerTask {
		
		private int width = 96;
		private int height = 96;
		private final PhotoEntry entry;
		private final BitmapField field;

		public BuildThumbImageTask(PhotoEntry entry, BitmapField field) {
			super();
			this.entry = entry;
			this.field = field;
		}

		public void run() {
			byte[] readFile;
			try {
				Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
				readFile = JSR75FileSystem.readFile(entry.getFilePath());
				EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
				//find the photo size
				int scale = ImageUtils.findBestImgScale(img.getWidth(), img.getHeight(), width, height);
				if(scale > 1)
					img.setScale(scale); //set the scale
				
				final Bitmap bitmapRescale = img.getBitmap();
				UiApplication.getUiApplication().invokeLater(new Runnable() {
		    		public void run() {
		    			if(bitmapRescale != null)
		    				field.setBitmap(bitmapRescale);
		    		}
		    	});
			} catch (Throwable t) {
				cancel();
				Log.error(t, "Serious Error in buildThumbnails Task: " + t.getMessage());
			}
			
		}
	}	
}