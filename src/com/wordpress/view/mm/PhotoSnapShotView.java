package com.wordpress.view.mm;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.model.Preferences;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.view.BaseView;


public class PhotoSnapShotView extends BaseView {
	
	ObjectChoiceField qualityField;
    private VerticalFieldManager _videoManager; //manager that hold the viewFiender
	private VideoControl vc;
	private String encoding;
	private Player p;
	private Field viewFinder;
	private boolean isViewfinderVisible = false;
	
	private Preferences prefs=Preferences.getIstance(); //main preferences object
	private final BlogObjectController controller;
	
	public PhotoSnapShotView(BlogObjectController _controller) {
		super(_resources.getString(WordPressResource.TITLE_SNAPSHOT_VIEW), VerticalFieldManager.USE_ALL_WIDTH | VerticalFieldManager.FIELD_HCENTER);
		this.controller = _controller;
		
		String[] choices = MultimediaUtils.getSupportedPhotoFormat();    
		qualityField = new ObjectChoiceField("Quality", choices);
		String photoEncoding = prefs.getPhotoEncoding(); //retrive preferences from the main setup
		//select the preference 
		for (int i = 0; i < choices.length; i++) {
        	if(choices[i].equalsIgnoreCase(photoEncoding)){
        		qualityField.setSelectedIndex(i);
    	 	}
		}
		
		add(qualityField);
				
		try {
			p = Manager.createPlayer("capture://video");
			p.realize();
			log("Player realized");
			p.prefetch();
			log("Player prefetched");

			//Get the Player VideoControl
			vc = (VideoControl) p.getControl("VideoControl");
			
			 //Initialize video display mode 
			viewFinder = (Field) vc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE,	"net.rim.device.api.ui.Field");
            
            //Create a manager for the video field
            _videoManager = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH | VerticalFieldManager.FIELD_HCENTER);
            _videoManager.add(viewFinder);
            add(_videoManager);
			
            vc.setVisible(true);
            isViewfinderVisible = true;
            _videoManager.setFocus();
            
            p.start();
            log("Player started");
            
		} catch (Exception me) {
			controller.displayError(me, "Error during Camera Initialization");
			log(me.getMessage());
		}
		
		addMenuItem(_snapshotMenuItem);
		addMenuItem(_fullScreenMenuItem);
		addMenuItem(_exitFullScreenMenuItem);		
	}
	
	
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
		if(fieldWithFocus == viewFinder) { 
			takeSnapShot();
			return true;
		}
		else 
		 return super.navigationClick(status,time);
	}
	

	private void getSelectedEncoding(){
		int selected = qualityField.getSelectedIndex();
		encoding =	MultimediaUtils.getPhotoEncoding(selected);
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
			if(vc!=null && isViewfinderVisible){	
				//String imageType = "encoding=jpeg&width=640&height=480&quality=superfine";
				final byte[] imageBytes = vc.getSnapshot(encoding);
				log("Encoding: "+encoding);
				log("Size: " + imageBytes.length);
				controller.addPhoto(imageBytes,System.currentTimeMillis()+".jpg");
				onClose();
				
			} else {
				controller.displayError("Viewfinder not visible!");
				log("Viewfinder not visible!");	
			}
			
			if(isViewfinderVisible){
				_videoManager.delete(viewFinder);				
				isViewfinderVisible=false;
			}
		} catch(Throwable e){
			controller.displayError(e.getMessage());
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
		controller.backCmd();
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