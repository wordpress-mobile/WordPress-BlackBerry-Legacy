//#preprocess
package com.wordpress.quickphoto.OS5;

import java.util.Vector;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.GUIControl;
import javax.microedition.media.control.VideoControl;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.MainController;
import com.wordpress.quickphoto.CameraScreenListener;
import com.wordpress.utils.ImageEncodingPropertiesOS5;
import com.wordpress.utils.log.Log;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.TransitionContext;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.UiEngineInstance;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.util.StringUtilities;

/**
 * A UI screen to display the camera display and buttons
 */
final class CameraScreenOS5 extends MainScreen implements CameraScreenListener {
    /** The camera's video controller */
    private VideoControl _videoControl;

    /** The field containing the feed from the camera */
    private Field _videoField;

    /** An array of valid snapshot encodings */
    private ImageEncodingPropertiesOS5[] _encodings; 

    private int _indexOfEncoding = 0;
    
    private String mediaFilePath = null;
    
    private CameraScreenListener listener = null;

	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
    
    /**
     * Constructor. Initializes the camera and creates the UI.
     */
    public CameraScreenOS5() {
        // Set the title of the screen
        setTitle(_resources.getString(WordPressResource.MENUITEM_QUICKPHOTO));

        // Initialize the camera object and video field
        initializeCamera();

        // Initialize the list of possible encodings
        initializeEncodingList();

        // If the field was constructed successfully, create the UI
        if (_videoField != null) {
            createUI();
            addMenuItem(_encodingMenuItem);
        }
        // If not, display an error message to the user
        else {
            add(new RichTextField("Error connecting to camera."));
        }
    }

    /**
     * Initialize the list of encodings
     */
    private void initializeEncodingList() {
        try {
            // Retrieve the list of valid encodings
            final String encodingString =
                    System.getProperty("video.snapshot.encodings");

            // Extract the properties as an array of word
            final String[] properties =
                    StringUtilities.stringToKeywords(encodingString);

            // The list of encodings
            final Vector encodingList = new Vector();

            // Strings representing the four properties of an encoding as
            // returned by System.getProperty().
            final String encoding = "encoding";
            final String width = "width";
            final String height = "height";
            final String quality = "quality";

            ImageEncodingPropertiesOS5 temp = null;

            for (int i = 0; i < properties.length; ++i) {
                if (properties[i].equals(encoding)) {
                    if (temp != null && temp.isComplete()) {
                        // Add a new encoding to the list if it has been
                        // properly set.
                        encodingList.addElement(temp);
                    }
                    temp = new ImageEncodingPropertiesOS5();

                    // Set the new encoding's format
                    ++i;
                    temp.setFormat(properties[i]);
                } else if (properties[i].equals(width)) {
                    // Set the new encoding's width
                    ++i;
                    temp.setWidth(properties[i]);
                } else if (properties[i].equals(height)) {
                    // Set the new encoding's height
                    ++i;
                    temp.setHeight(properties[i]);
                } else if (properties[i].equals(quality)) {
                    // Set the new encoding's quality
                    ++i;
                    temp.setQuality(properties[i]);
                }
            }

            // If there is a leftover complete encoding, add it.
            if (temp != null && temp.isComplete()) {
                encodingList.addElement(temp);
            }

            // Convert the Vector to an array for later use
            _encodings = new ImageEncodingPropertiesOS5[encodingList.size()];
            encodingList.copyInto(_encodings);
        } catch (final Exception e) {
            // Something is wrong, indicate that there are no encoding options
            _encodings = null;
            Log.error(e, "Unable to initialize camera encodings");
        }
    }
    
    
    /**
     * Displays the various encoding choices available
     */
    private final MenuItem _encodingMenuItem = new MenuItem(
            "Encoding Settings", 10, 100) {
        public void run() {
            final EncodingPropertiesScreenOS5 s =
                    new EncodingPropertiesScreenOS5(_encodings, CameraScreenOS5.this,
                            _indexOfEncoding);
            UiApplication.getUiApplication().pushModalScreen(s);
        }
    };

    /**
     * Takes a picture with the selected encoding settings
     */
    public void takePicture() {
        try {
            // A null encoding indicates that the camera should
            // use the default snapshot encoding.
            String encoding = null;

            if (_encodings != null) {
                // Use the user-selected encoding
                encoding = _encodings[_indexOfEncoding].getFullEncoding();
            }

            // Retrieve the raw image from the VideoControl and
            // create a screen to display the image to the user.
            createImageScreen(_videoControl.getSnapshot(encoding));
        } catch (final Exception e) {
        	MainController.getIstance().displayError(e, "Unable to take a picture");
        }
    }

    /**
     * Prevent the save dialog from being displayed
     * 
     * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
     */
    protected boolean onSavePrompt() {
        return true;
    }

    /**
     * Initializes the Player, VideoControl and VideoField
     */
    private void initializeCamera() {
        try {
            // Create a player for the Blackberry's camera
            final Player player = Manager.createPlayer("capture://video");

            // Set the player to the REALIZED state (see Player javadoc)
            player.realize();

            // Grab the video control and set it to the current display
            _videoControl = (VideoControl) player.getControl("VideoControl");

            if (_videoControl != null) {
                // Create the video field as a GUI primitive (as opposed to a
                // direct video, which can only be used on platforms with
                // LCDUI support.)
                _videoField =
                        (Field) _videoControl.initDisplayMode(
                                GUIControl.USE_GUI_PRIMITIVE,
                                "net.rim.device.api.ui.Field");
                _videoControl.setDisplayFullScreen(true);
                _videoControl.setVisible(true);
            }

            // Set the player to the STARTED state (see Player javadoc)
            player.start();
        } catch (final Exception e) {
        	MainController.getIstance().displayError(e, "Unable to initialize the Camera");
        }
    }


    /**
     * Adds the VideoField to the screen
     */
    private void createUI() {
        // Add the video field to the screen
        add(_videoField);
    }

    /**
     * Create a screen used to display a snapshot
     * 
     * @param raw
     *            A byte array representing an image
     */
    private void createImageScreen(final byte[] raw) {
        // Initialize the screen
        final ImageScreenOS5 imageScreen = new ImageScreenOS5(raw);
        imageScreen.setListener(this);

        // Push screen to display it to the user
		UiEngineInstance engine = Ui.getUiEngineInstance();
		TransitionContext transitionContextIn;
		transitionContextIn = new TransitionContext(TransitionContext.TRANSITION_SLIDE);
		transitionContextIn.setIntAttribute(TransitionContext.ATTR_DURATION, 500);
		transitionContextIn.setIntAttribute(TransitionContext.ATTR_DIRECTION, TransitionContext.DIRECTION_LEFT);   
		engine.setTransition(null, imageScreen, UiEngineInstance.TRIGGER_PUSH, transitionContextIn);
        
        // Push this screen to display it to the user
        UiApplication.getUiApplication().pushScreen(imageScreen);
    }

    /**
     * Sets the index of the encoding in the 'encodingList' Vector
     * 
     * @param index
     *            The index of the encoding in the 'encodingList' Vector
     */
    public void setIndexOfEncoding(final int index) {
        _indexOfEncoding = index;
    }

    /**
     * @see net.rim.device.api.ui.Screen#invokeAction(int)
     */
    protected boolean invokeAction(final int action) {
        final boolean handled = super.invokeAction(action);

        if (!handled) {
            switch (action) {
            case ACTION_INVOKE: // Trackball click
            {
                takePicture();
                return true;
            }
            }
        }
        return handled;
    }

	public void mediaItemTaken(String filePath) {
		this.mediaFilePath = filePath;
	}

	protected void onExposed(){
    	Log.trace("CameraScreen - onExposed");
    	super.onExposed();	
    	if ( this.mediaFilePath != null ) {
    		if ( listener != null ) {
    			listener.mediaItemTaken(mediaFilePath);
    		}
    		close();
    	}
    }
	
    public void setListener(CameraScreenListener listener) {
		this.listener = listener;
	}
}