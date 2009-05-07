package com.wordpress.view.mm;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.Preferences;
import com.wordpress.view.BaseView;


public class PhotoSnapShotView extends BaseView {
	
	private MainScreen preview;
	private EditField logField;
	ObjectChoiceField qualityField;
	private VideoControl vc;
	private String encoding;
	private Player p;
	private Field viewFinder;
	private BitmapField bitmapField;
	private boolean isViewfinderVisible;
	
	private Preferences prefs=Preferences.getIstance(); //main preferences object
	private final PostController controller;
	
	public PhotoSnapShotView(PostController _controller) {
		super("");
		this.controller = _controller;
		logField = new EditField("Log:","");
		bitmapField = new BitmapField();
		
		String[] choices = MultimediaUtils.getSupportedPhotoFormat();    
		qualityField = new ObjectChoiceField("Quality", choices);
		String photoEncoding = prefs.getPhotoEncoding(); //retrive preferecnes from the main setup
		//select the preference 
		for (int i = 0; i < choices.length; i++) {
        	if(choices[i].equalsIgnoreCase(photoEncoding)){
        		qualityField.setSelectedIndex(i);
    	 	}
		}
      
		add(qualityField);
		add(logField);		
		addMenuItem(_snapshotMenuItem);
		addMenuItem(_fullScreenMenuItem);
		addMenuItem(_exitFullScreenMenuItem);
		preview = new MainScreen();
		preview.add(bitmapField);	
		
		try{						
			p = Manager.createPlayer("capture://video");						
			p.realize();
			log("Player realized");
			p.prefetch();
			log("Player prefetched");
			p.start();
			log("Player started");				
			vc = (VideoControl) p.getControl("VideoControl");
			viewFinder = (Field)vc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field");
			log("Initialized.");
		} catch (Exception me){
			log(me.getMessage());
		}
		isViewfinderVisible=false;
		
		
		setupEncoding();
		logField.setText("");
		log("Active Encoding: "+encoding);
		if(vc!=null){					
			
			if(getFieldCount()<3){								
				delete(logField);
				add(viewFinder);								
				add(logField);
				viewFinder.setFocus();
			}
			vc.setVisible(true);			
			isViewfinderVisible=true;
			log("Initialized ViewFinder");								
		}else {
			log("VideoControl not initialized");
		}
	
	}

	private void setupEncoding(){
		int selected = qualityField.getSelectedIndex();
		encoding =	MultimediaUtils.getPhotoEncoding(selected);
	}
	

	private MenuItem _snapshotMenuItem = new MenuItem("Snap", 1, 1) {
		public void run() {			
			try {
				logField.setText("");
				log("Taking snapshot");
				setupEncoding();
				if(vc!=null && isViewfinderVisible){	
					//String imageType = "encoding=jpeg&width=640&height=480&quality=superfine";
					final byte[] imageBytes = vc.getSnapshot(encoding);
					log("Encoding: "+encoding);
					log("Size: " + imageBytes.length);
					controller.addPhoto(imageBytes,null);
					controller.backCmd();
				} else 
					log("Viewfinder not visible!");	
				
				if(isViewfinderVisible){
					delete(viewFinder);				
					isViewfinderVisible=false;
				}
			} catch(Throwable e){
				log(e + ":" + e.getMessage());
			}
		}
	};
	
	private MenuItem _fullScreenMenuItem = new MenuItem("FullScreen", 1, 1) {
		public void run() {			
			if(vc!=null){
				try {					
					vc.setDisplayFullScreen(true);
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
				logField.setText(logField.getText() + "\n" + msg);
			}
		});
	}

	public BaseController getController() {
		// TODO Auto-generated method stub
		return null;
	}
	
}

