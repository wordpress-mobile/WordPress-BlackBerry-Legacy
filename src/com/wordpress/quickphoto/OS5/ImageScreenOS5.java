package com.wordpress.quickphoto.OS5;

import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.MainController;
import com.wordpress.quickphoto.CameraScreenListener;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

/**
 * The main screen to display an image taken from the camera demo.
 */
public final class ImageScreenOS5 extends MainScreen {
  
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;

	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
	
	/** The down-scaling ratio applied to the snapshot Bitmap */
    private static final int IMAGE_SCALING = 7;

    /** The base file name used to store pictures */
    private static String FILE_NAME = System.getProperty("fileconn.dir.photos")
            + "IMAGE";

    /** The extension of the pictures to be saved */
    private static String EXTENSION = ".jpg";

    /** A counter for the number of snapshots taken */
    private static int _counter;
    
    private CameraScreenListener listener = null;

    /**
     * Constructor
     * 
     * @param raw
     *            A byte array representing an image
     */
    public ImageScreenOS5(final byte[] raw) {

        setTitle("IMAGE");

        // Convert the byte array to a Bitmap image
        final Bitmap image =
                Bitmap.createBitmapFromBytes(raw, 0, -1, IMAGE_SCALING);

        // Create two field managers to center the screen's contents
        final HorizontalFieldManager hfm1 =
                new HorizontalFieldManager(Field.FIELD_HCENTER);
        final HorizontalFieldManager hfm2 =
                new HorizontalFieldManager(Field.FIELD_HCENTER);

        // Create the field that contains the image
        final BitmapField imageField = new BitmapField(image);
        hfm1.add(imageField);

        // Create the SAVE button which returns the user to the main camera
        // screen and saves the picture as a file.
        final ButtonField photoButton = new ButtonField( _resources.getString( WordPressResource.MENUITEM_ACCEPT ) );
        photoButton.setChangeListener(new SaveListener(raw));
        hfm2.add(photoButton);

        // Create the CANCEL button which returns the user to the main camera
        // screen without saving the picture.
        final ButtonField cancelButton = new ButtonField( _resources.getString( WordPressResource.MENUITEM_RETAKE ));
        cancelButton.setChangeListener(new CancelListener());
        hfm2.add(cancelButton);

        // Add the field managers to the screen
        add(hfm1);
        add(hfm2);
    }

    /**
     * Handles trackball click events
     * 
     * @see net.rim.device.api.ui.Screen#invokeAction(int)
     */
    protected boolean invokeAction(final int action) {
        final boolean handled = super.invokeAction(action);

        if (!handled) {
            switch (action) {
            case ACTION_INVOKE: // Trackball click.
            {
                return true;
            }
            }
        }
        return handled;
    }

    /**
     * A listener used for the "Save" button
     */
    private class SaveListener implements FieldChangeListener {
        /** A byte array representing an image */
        private final byte[] _raw;

        /**
         * Constructor.
         * 
         * @param raw
         *            A byte array representing an image
         */
        SaveListener(final byte[] raw) {
            _raw = raw;
        }

        /**
         * Saves the image as a file in the BlackBerry filesystem
         */
        public void fieldChanged(final Field field, final int context) {
            try {
                // Create the connection to a file that may or
                // may not exist.
                FileConnection file =
                        (FileConnection) Connector.open(FILE_NAME + _counter + EXTENSION);

                // If the file exists, increment the counter until we find
                // one that hasn't been created yet.
                while (file.exists()) {
                    file.close();
                    ++_counter;
                    file = (FileConnection) Connector.open(FILE_NAME + _counter + EXTENSION);
                }

                // We know the file doesn't exist yet, so create it
                file.create();

                // Write the image to the file
                final OutputStream out = file.openOutputStream();
                out.write(_raw);

                // Close the connections
                out.close();
                file.close();
            } catch (final Exception e) {
            	MainController.getIstance().displayError(e, "Unable to save the picture");
            }

            // Increment the image counter
            ++_counter;
        	
            if ( listener != null )
				listener.mediaItemTaken( FILE_NAME + _counter + EXTENSION);
            
            UiApplication.getUiApplication().invokeLater(new Runnable()
			{
				public void run()
				{
					close();
				}
			}, 250, false);

        }
    } 
    
    /**
     * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
     */
    protected boolean onSavePrompt()
    {
        // Prevent the save dialog from being displayed
        return true;
    }
    
    /**
     * A listener used for the "Cancel" button
     */
    private class CancelListener implements FieldChangeListener {
        /**
         * Return to the main camera screen
         */
        public void fieldChanged(final Field field, final int context) {
        	if ( listener != null )
    			listener.mediaItemTaken( null );
        	
        	UiApplication.getUiApplication().invokeLater(new Runnable()
			{
				public void run()
				{
					close();
				}
			}, 250, false);
        }
    }
    
	public void setListener(CameraScreenListener listener) {
		this.listener = listener;
	}
}