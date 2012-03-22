package com.wordpress.quickphoto.OS5;

import java.io.DataOutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.media.control.MetaDataControl;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.MainController;
import com.wordpress.quickphoto.CameraScreenListener;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.GUIFactory;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.container.JustifiedEvenlySpacedHorizontalFieldManager;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.decor.Border;
import net.rim.device.api.ui.decor.BorderFactory;

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

    /** The base file name used to store pictures */
    private static String FILE_NAME = System.getProperty("fileconn.dir.photos") + "IMAGE";

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

        setTitle(_resources.getString(WordPressResource.TITLE_PREVIEW));
        VerticalFieldManager _manager = (VerticalFieldManager)getMainManager();

        Background bg = BackgroundFactory.createSolidBackground(0xefebef);
        _manager.setBackground(bg);

        // Convert the byte array to a Bitmap image
        final Bitmap image = createImageThumb(raw); //Bitmap.createBitmapFromBytes(raw, 0, -1, IMAGE_SCALING);

        // Create two field managers to center the screen's contents
        final HorizontalFieldManager hfm1 = new HorizontalFieldManager(Field.FIELD_HCENTER);
        hfm1.setMargin(5,0,0,0);
		JustifiedEvenlySpacedHorizontalFieldManager bottomToolbar = new JustifiedEvenlySpacedHorizontalFieldManager();	
		bottomToolbar.setMargin(5,0,5,0);

        // Create the field that contains the image
        final BitmapField imageField = new BitmapField(image, BitmapField.FOCUSABLE );
        XYEdges edges = new XYEdges(1, 1, 1, 1);
        XYEdges colors = new XYEdges(Color.BLACK, Color.BLACK, Color.BLACK, Color.BLACK);
        Border border = BorderFactory.createSimpleBorder(edges, colors, Border.STYLE_SOLID);
        imageField.setBorder(border);
        hfm1.add(imageField);

        // Create the SAVE button which returns the user to the main camera
        // screen and saves the picture as a file.
//        final ButtonField photoButton = new ButtonField( _resources.getString( WordPressResource.MENUITEM_ACCEPT ) );
        BaseButtonField photoButton = GUIFactory.createButton(_resources.getString(WordPressResource.MENUITEM_ACCEPT), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
        photoButton.setChangeListener(new SaveListener(raw));
        bottomToolbar.add(photoButton);

        // Create the CANCEL button which returns the user to the main camera
        // screen without saving the picture.
       // final ButtonField cancelButton = new ButtonField( _resources.getString( WordPressResource.MENUITEM_RETAKE ));
        BaseButtonField cancelButton = GUIFactory.createButton(_resources.getString(WordPressResource.MENUITEM_RETAKE), ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
        cancelButton.setChangeListener(new CancelListener());
        bottomToolbar.add(cancelButton);

        // Add the field managers to the screen
        add(hfm1);
        add(bottomToolbar);
        
        photoButton.setFocus();
    }

    private Bitmap createImageThumb(byte[] raw) {
    	EncodedImage img = EncodedImage.createEncodedImage(raw, 0, -1);
		
		int angle = 0;
		/* Fixes Orientation on devices with gyroschope ref #222 */
		try {
			MetaDataControl metaData = img.getMetaData();
			String orientation = metaData.getKeyValue("orientation");
			if (orientation.equals("8")) {
				angle = 270;
			}
			else if (orientation.equals("3")) {
				angle = 180;
			}
			else if (orientation.equals("6")) {
				angle = 90;
			}
		} catch (Exception e) {
			//Image doesn't have any exif data treat it as a normal bitmap
			Log.error(e, "Image doesn't have any exif data treat it as a normal bitmap");
		}
		
		//find the photo size
		int scale = ImageUtils.findBestImgScale(img.getWidth(), img.getHeight(), Display.getWidth(), Display.getHeight() - 50 );
		if(scale > 1)
			img.setScale(scale); //set the scale
		Bitmap bitmapRescale = img.getBitmap();
		
		if( angle != 0 ) {
			try {
				bitmapRescale = ImageUtils.rotate(bitmapRescale, angle);
			} catch (Exception e) {
				Log.error(e, "Image can't be rotated in QP");
			}
		} 
		
		return bitmapRescale;
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
                
            	DataOutputStream dataOutputStream = file.openDataOutputStream();
        		dataOutputStream.write(_raw);
        		dataOutputStream.flush();
        		dataOutputStream.close();
        		file.close();
               
            } catch (final Exception e) {
            	MainController.getIstance().displayError(e, "Unable to save the picture");
            }
        	
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