//#preprocess
package com.wordpress.quickphoto;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import com.wordpress.controller.MainController;
import com.wordpress.utils.log.Log;

import net.rim.device.api.command.Command;
import net.rim.device.api.command.CommandHandler;
import net.rim.device.api.command.ReadOnlyCommandMetadata;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.TouchEvent;
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
	
	private int[] X_PTS;
	private int[] Y_PTS;
	private int[] upperDrawColors;
	private static final int HEIGHT = 54;
	
	
	public void setListener(CameraScreenListener listener) {
		this.listener = listener;
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

        X_PTS = new int[]{0, 0, getPreferredWidth(), getPreferredWidth()};
        Y_PTS = new int[]{0, HEIGHT, HEIGHT, 0};
        upperDrawColors = new int[]{0x444444, Color.BLACK, Color.BLACK, 0x444444};
        
		
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
        
        MenuItem retake = new MenuItem( "Retake", 0x230010, 1000);
        retake.setCommand(retakeCommand);
        MenuItem accept = new MenuItem( "Accept", 0x230010, 1000);
        accept.setCommand(useCommand);
        
        addMenuItem(accept);
        addMenuItem(retake);

        //#ifdef BlackBerrySDK7.0.0
        // Initialize the zoom screen to be zoomed all the way out
        setViewableArea(0, 0, 0);
       //#endif        
    }
    
    
    public void paint(Graphics g) {
    	super.paint(g);
    	int oldColour = g.getColor();
    	int height = Display.getHeight();
    	int width  = Display.getWidth();
    	
    	try { 
    		g.setColor(Color.BLACK);
    		g.fillRect(0, height - HEIGHT, width, HEIGHT );
    		g.setColor(Color.RED);
    		g.drawLine(0, height - HEIGHT , width, height - HEIGHT );
    		g.drawText( "Accept", 10, height - HEIGHT /2 , DrawStyle.TOP | DrawStyle.ELLIPSIS, width);
    		
    	} finally {
    		g.setColor( oldColour );
    	}
    }
    
    
  	protected boolean touchEvent(TouchEvent message) {
  		int eventCode = message.getEvent();
  		Log.trace(">>> touchEvent - "+ eventCode);
  		// Get the screen coordinates of the touch event
        int x = message.getX(1);
        int y = message.getY(1);
        Log.trace(">>> touchEvent - "+ x + " " + y);
  		if(eventCode == TouchEvent.CLICK) {
  			Log.trace("TouchEvent.CLICK");

  		} else if ( eventCode == TouchEvent.UNCLICK) {

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

    	   		close();
    		}
    		catch(IOException ioe)
    		{
    			MainController.getIstance().displayError("ERROR " + ioe.getClass() + ":  " + ioe.getMessage());
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
}
