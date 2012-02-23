//#preprocess
package com.wordpress.quickphoto;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.MainController;
import com.wordpress.utils.log.Log;

import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.extension.container.ZoomScreen;

/**
 * A screen to display an image taken with the camera
 */
public final class ImageScreen extends ZoomScreen
{  
    private static final String FILE_NAME = System.getProperty("fileconn.dir.photos") + "IMAGE";
    private String extension = ".jpg";
    
    private static int _counter; // A counter for the number of snapshots taken         
    
    private CameraScreenListener listener = null;
	private final byte[] raw;
	
	private final String acceptStr;
	private final String retakeStr;
	private final Font myFont;
	private int acceptStringWidth;
	private int retakeStringWidth;
	private int hPadding = 10;
	private int wPadding = 10;
	private int hRequired;
	
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
	
	
   /**
    * Creates a new ImageScreen object
    * @param raw A byte array representing an image
    * @param image Image to display 
    */
    public ImageScreen( final byte[] raw , EncodedImage image )
    {
        super(image);
        this.raw = raw;

        int type = image.getImageType();
        switch (type) {

		case EncodedImage.IMAGE_TYPE_PNG:
			extension = ".png";
			break;

		case EncodedImage.IMAGE_TYPE_JPEG:
			extension = ".jpg";
			break;

		case EncodedImage.IMAGE_TYPE_GIF:
			extension = ".gif";
			break;

		case EncodedImage.IMAGE_TYPE_BMP:
			extension = ".bmp";
			break;
			
		default:
			break;
        }
        
        MenuItem retake = new MenuItem( _resources, WordPressResource.MENUITEM_RETAKE, 0x230010, 1000);
        retake.setCommand(retakeCommand);
        MenuItem accept = new MenuItem( _resources, WordPressResource.MENUITEM_ACCEPT, 0x230010, 1000);
        accept.setCommand(useCommand);
        
        addMenuItem(accept);
        addMenuItem(retake);

        //#ifdef BlackBerrySDK7.0.0
        // Initialize the zoom screen to be zoomed all the way out
        setViewableArea(0, 0, 0);
        //#endif    
        
        
		myFont = Font.getDefault().derive(Font.PLAIN);
		acceptStr = _resources.getString( WordPressResource.MENUITEM_ACCEPT );
		retakeStr = _resources.getString( WordPressResource.MENUITEM_RETAKE );
		int myFontHeight = myFont.getHeight();
		acceptStringWidth = myFont.getAdvance(acceptStr);
		retakeStringWidth = myFont.getAdvance(retakeStr);
		hRequired = hPadding + myFontHeight + hPadding;
    }
    
    
    public void paint(Graphics g) {
    	super.paint(g);
    	int oldColour = g.getColor();
    	int oldAlpha = g.getGlobalAlpha();
    	try {
    		int width  = Display.getWidth();
    		int bgWidth = wPadding + acceptStringWidth + wPadding;
    		g.setColor(Color.BLACK);
    		g.setGlobalAlpha(125);
    		g.fillRect(0, 0, bgWidth, hRequired );   		
    		g.setGlobalAlpha(oldAlpha);
    		g.setColor(Color.GRAY);
    		g.drawLine(bgWidth+1, 0, bgWidth+1, hRequired);
    		g.drawLine(0, hRequired, bgWidth+1, hRequired);
    		g.setColor(Color.WHITE);
    		g.drawText( acceptStr, wPadding, hPadding , DrawStyle.TOP | DrawStyle.ELLIPSIS, acceptStringWidth);
    		
    		bgWidth = wPadding + retakeStringWidth + wPadding;
    		g.setColor(Color.BLACK);
    		g.setGlobalAlpha(125);
    		g.fillRect(width - bgWidth, 0, bgWidth, hRequired );
    		g.setGlobalAlpha(oldAlpha);
    		g.setColor(Color.GRAY);
    		g.drawLine(width - (bgWidth + 1) , 0, width - (bgWidth + 1), hRequired);
    		g.drawLine(width, hRequired, width - ( bgWidth+1) , hRequired);
    		g.setColor(Color.WHITE);
    		g.drawText( retakeStr, width - ( retakeStringWidth + wPadding) , hPadding , DrawStyle.TOP | DrawStyle.ELLIPSIS, retakeStringWidth);
    		
    	} finally {
    		g.setColor( oldColour );
    		g.setGlobalAlpha(oldAlpha);
    	}
    }
    
    private boolean isActionButtonClicked( int x, int y ) {
		int width  = Display.getWidth();
		if (  ( x <  wPadding + acceptStringWidth + wPadding )  && y < hRequired ) {
			useCommand.execute(new Boolean(false));
			UiApplication.getUiApplication().invokeLater(new Runnable()
			{
				public void run()
				{
					close();
				}
			}, 250, false);
			return true;
		}
		if (  ( x >  width - ( wPadding + retakeStringWidth + wPadding ) )  && y < hRequired ) {
			retakeCommand.execute(new Boolean(false));
			UiApplication.getUiApplication().invokeLater(new Runnable()
			{
				public void run()
				{
					close();
				}
			}, 250, false);
			return true;
		}
		return false;
    }

    protected boolean touchEvent(TouchEvent message) {
    	int eventCode = message.getEvent();
    	
		if(WordPressInfo.isForcelessTouchClickSupported) {
			if (eventCode == TouchEvent.GESTURE) {
				TouchGesture gesture = message.getGesture();
				int gestureCode = gesture.getEvent();
				if (gestureCode == TouchGesture.TAP) {
					int x = message.getX(1);
		    		int y = message.getY(1);
		    		if ( isActionButtonClicked(x, y) ) 
		    			return true;
				}
			} 
		} else {
	    	if(eventCode == TouchEvent.CLICK) {
	    		Log.trace("TouchEvent.CLICK");
	    		int x = message.getX(1);
	    		int y = message.getY(1);
	    		if ( isActionButtonClicked(x, y) ) 
	    			return true;
	    	}
		}

    	return super.touchEvent(message);
    }
    
    protected boolean keyChar(char c, int status, int time) {
    	if(c == 'p' || c == Characters.ESCAPE)
        {
    	Log.trace("back clicked");
        }
    	return super.keyChar(c, status, time);
    }
    
    private Command retakeCommand =  new Command(new CommandHandler() 
    {
    	/**
    	 * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    	 */
    	public void execute(ReadOnlyCommandMetadata metadata, Object context) 
    	{
    		if ( listener != null )
    			listener.mediaItemTaken( null );
    		
			//do not close this screen
			if( context != null && context instanceof Boolean ) {
				if ( ((Boolean) context).booleanValue() == false ) 
					return;
			}
    		close();
    	}
    }
    );
    
    private Command useCommand =  new Command(new CommandHandler() 
    {
    	/**
    	 * @see net.rim.device.api.command.CommandHandler#execute(ReadOnlyCommandMetadata, Object)
    	 */
    	public void execute(ReadOnlyCommandMetadata metadata, Object context) 
    	{
    		try
    		{       
    			// Create connection to a file that may or
    			// may not exist.
    			FileConnection file = (FileConnection)Connector.open(FILE_NAME + _counter + extension);

    			// If the file exists, increment the counter and try
    			// again until we have a filename for a file that hasn't
    			// been created yet.
    			while(file.exists())
    			{
    				file.close();
    				++_counter;
    				file = (FileConnection)Connector.open(FILE_NAME + _counter + extension);
    			}

    			// We know the file doesn't exist yet, so create it
    			file.create();

    			// Write the image to the file
    			OutputStream out = file.openOutputStream();
    			out.write(raw);

    			// Close the connections
    			out.close();
    			file.close();                                         

    			// Inform the user where the file has been saved
    			//  Dialog.inform("Saved to " + FILE_NAME + _counter + extension);                       

    			if ( listener != null )
    				listener.mediaItemTaken( FILE_NAME + _counter + extension);

    			//do not close this screen
    			if( context != null && context instanceof Boolean ) {
    				if ( ((Boolean) context).booleanValue() == false ) 
    					return;
    			}
    	   		close();
    	   		
    		}
    		catch(IOException ioe)
    		{
    			MainController.getIstance().displayError(ioe, "Unable to save the picture");
    		}
    	}
    }

    );
    
    /**
     * @see ZoomScreen#zoomedOutNearToFit()
     
    public void zoomedOutNearToFit()
    {            
        useCommand.execute(null);
    }
    */
    
    /**
     * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
     */
    protected boolean onSavePrompt()
    {
        // Prevent the save dialog from being displayed
        return true;
    }
    
	public void setListener(CameraScreenListener listener) {
		this.listener = listener;
	}
}