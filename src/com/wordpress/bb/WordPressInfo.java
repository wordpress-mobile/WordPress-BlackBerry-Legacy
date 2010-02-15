package com.wordpress.bb;

import net.rim.device.api.system.Bitmap;

/**
 * Class to provide information about the application
 * and its environment.  
 */
public final class WordPressInfo {

	/** System event log GUID */
    public final static long GUID = 0x97ebc046dec5817fL;
    public final static long COMMENTS_UID = 0x8de9c6b3a49fd864L;
    
    private static Bitmap icon = Bitmap.getBitmapResource("application-icon.png");
    private static Bitmap newCommentsIcon = Bitmap.getBitmapResource("application-icon-new.png");
    
    /**
     * Initializes the application information from the descriptor and the
     * command-line arguments.  This method must be called on startup.
     * @param args Arguments
     */
    public static synchronized void initialize(String args[]) {

    }
   
    public static Bitmap getIcon() {
    	return icon;
    }
    
    public static Bitmap getNewCommentsIcon() {
    	return newCommentsIcon;
    }
    
}