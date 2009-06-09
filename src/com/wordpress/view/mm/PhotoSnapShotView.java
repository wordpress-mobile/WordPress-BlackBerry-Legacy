package com.wordpress.view.mm;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressApplicationPermissions;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.model.Preferences;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.view.BaseView;


public class PhotoSnapShotView extends BaseView {
	
	ObjectChoiceField qualityField;
	private VideoControl vc;
	private String encoding;
	private Player p;
	private Field viewFinder;
	private boolean isViewfinderVisible = false;
	private ButtonField buttonOK;
	
	private Preferences prefs=Preferences.getIstance(); //main preferences object
	private final BlogObjectController controller;
	
	public PhotoSnapShotView(BlogObjectController _controller) {
		super(_resources.getString(WordPressResource.TITLE_SNAPSHOT_VIEW));
		this.controller = _controller;
		
		String[] choices = MultimediaUtils.getSupportedPhotoFormat();    
		qualityField = new ObjectChoiceField("Quality", choices);
		String photoEncoding = prefs.getPhotoEncoding(); //retrive preferences from the main setup
		//select the preference 
		boolean flagP = false;
		if(photoEncoding != null)
		for (int i = 0; i < choices.length; i++) {
        	if(choices[i].equalsIgnoreCase(photoEncoding)){
        		qualityField.setSelectedIndex(i);
        		flagP = true;
    	 	}
		}
		if(!flagP)
			qualityField.setSelectedIndex(5);
		
		HorizontalFieldManager qualityFieldManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
		qualityFieldManager.add(qualityField);
		add(qualityFieldManager);
		
		buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK); 
		buttonOK.setChangeListener(listenerOkButton); 
		HorizontalFieldManager buttonsFieldManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
		buttonsFieldManager.add(buttonOK);
		add(buttonsFieldManager);
				
		try {
			p = Manager.createPlayer("capture://video");
			p.realize();
			log("Player realized");
			p.prefetch();
			log("Player prefetched");
			p.start();
			log("Player started");

			//Get the Player VideoControl
			vc = (VideoControl) p.getControl("VideoControl");
			
			 //Initialize video display mode 
			viewFinder = (Field) vc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE,	"net.rim.device.api.ui.Field");
            
            //Create a manager for the video field
            add(viewFinder);
			
            vc.setVisible(true);
            isViewfinderVisible = true;
                     
		} catch (Exception me) {
			controller.displayError(me, "Error during Camera Initialization");
			log(me.getMessage());
		}
		
		buttonOK.setFocus();
		addMenuItem(_snapshotMenuItem);
		addMenuItem(_fullScreenMenuItem);
		addMenuItem(_exitFullScreenMenuItem);		
	}
	
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	takeSnapShot();
	   }
	};
	
    //override keyControl to handle photo snapshot
    protected boolean keyControl(char c, int status, int time) 
    {
       //Handle volume up and down key presses to adjust Player volume
       //if volume up is pressed call volumeUp() 
        if (c == Characters.ENTER)
        {
        	takeSnapShot();
            return true;
        }
        //else call the default keyControl handler of the super class
        else 
        {
            return super.keyControl(c, status, time);
        }
    }
    
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		Field fieldWithFocus = this.getFieldWithFocus();
		if(fieldWithFocus == buttonOK) { 
			takeSnapShot();
			return true;
		}
		else 
		 return super.navigationClick(status,time);
	}
	

	private void getSelectedEncoding(){
		int selected = qualityField.getSelectedIndex();
		System.out.println("selected img quality index: "+selected);
	
		encoding =	MultimediaUtils.getPhotoEncoding(selected);
		System.out.println("selected img quality: "+ encoding);
	}
	

	private MenuItem _snapshotMenuItem = new MenuItem("Snap", 1, 1) {
		public void run() {			
			takeSnapShot();
		}
	
	};
	
	private void takeSnapShot() {
		try {
			log("Taking snapshot");
			getSelectedEncoding();
			
			//check the app permissions
			if(! WordPressApplicationPermissions.getIstance().isScreenCapturePermitted() ) {
				controller.displayMessage("Error while taking a photo");
				return;
			}
			
			if( vc != null && isViewfinderVisible){	
				log("Encoding: "+encoding);

				final byte[] imageBytes = vc.getSnapshot(encoding);

				log("Size: " + imageBytes.length);

				if(isViewfinderVisible){
					delete(viewFinder);				
					isViewfinderVisible=false;
				}
				
				onClose();
				
				
				controller.storePhoto(imageBytes, System.currentTimeMillis()+".jpg");
				
			} else {
				controller.displayError("Viewfinder not visible!");
				log("Viewfinder not visible!");	
			}
			
		} catch(Exception e){
			controller.displayError(e, "Error while take photo, have you changed application permission settings?");
			log(e + ":" + e.getMessage());
		}
	}
	
	private MenuItem _fullScreenMenuItem = new MenuItem("FullScreen", 1, 1) {
		public void run() {			
			if(vc!=null){
				try {					
					vc.setDisplayFullScreen(true); //The only camera preview sizes are full screen mode and the default non full screen size.
				} catch (MediaException e) {
					log(e.getMessage());
				}
			} else log("VideoControl is not initialized");
		}
	};
	
	private MenuItem _exitFullScreenMenuItem = new MenuItem("Exit FullScreen", 1, 1) {
		public void run() {			
			if(vc!=null){
				try {					
					vc.setDisplayFullScreen(false);
				} catch (MediaException e) {
					log(e.getMessage());
				}
			} else log("VideoControl is not initialized");
		}
	};
	
	private void log(final String msg) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				System.out.println(msg);
			}
		});
	}

	public BaseController getController() {
		return controller;
	}
	
    // override onClose() to cleanup Player resources when the screen is closed
	public boolean onClose() {
		// Cleanup Player resources
		// try block
		try {
			// Stop Player
			if( p!= null)
				p.stop();
 		} catch (Exception e) {
			e.printStackTrace();
		}

		if (p != null) {
			// Close Player
			p.close();
			p = null;
		} 
		//controller.backCmd();
		UiApplication.getUiApplication().popScreen(this); //remove screen immediatly
		return true;
	}

	//Manager to lay out the Player _videoField on the VideoScreen
	private final class VideoManager extends net.rim.device.api.ui.Manager 
	{
	    public VideoManager() 
	    {
	        super(VerticalFieldManager.USE_ALL_WIDTH | VerticalFieldManager.FIELD_HCENTER);
	    }

	    //lay out the _videoField on the screen based on it's preferred width and height
	    protected void sublayout(int width, int height) 
	    {
	        if (getFieldCount() > 0) 
	        {
	            Field videoField = getField(0);
	            layoutChild(videoField, videoField.getPreferredWidth(), videoField.getPreferredHeight());
	        }
	        setExtent(width, height);
	    }
	    
	}
	
}