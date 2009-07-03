package com.wordpress.view.mm;

import java.io.IOException;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EventInjector;

import com.wordpress.controller.BlogObjectController;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;

/**
 * Listener to determine when files have been added to the file system.
 */
public final class PhotoFileJournalListener implements FileSystemJournalListener
{
    private BlogObjectController _screen;
    private long _lastUSN; // = 0;
    
    /**
     * Constructor.
     * 
     * @param screen The screen to update when events occur.
     */
    public PhotoFileJournalListener(BlogObjectController screen) {
        _screen = screen;
    }

    
    /**
     * Notified when FileSystem event occurs.
     */
    public void fileJournalChanged() {
        long nextUSN = FileSystemJournal.getNextUSN();
        String msg = null;
        for (long lookUSN = nextUSN - 1; lookUSN >= _lastUSN && msg == null; --lookUSN) {
            FileSystemJournalEntry entry = FileSystemJournal.getEntry(lookUSN);
            if (entry == null) { // we didn't find an entry.
                break;
            }

            //check if this entry was added or deleted
            String path = entry.getPath();
            if (path != null) {
            	
            	Log.debug("FS changed: "+path);
            	//check if is an image, and exclude WP inst folder 
            	if ( (path.endsWith("png") | path.endsWith("jpg") | path.endsWith("bmp") | path.endsWith("gif")) 
            			&& path.indexOf("store/home/user/wordpress") == -1)
                switch (entry.getEvent()) {
                
                    case FileSystemJournalEntry.FILE_ADDED:
                    	
                    		
                    		Log.debug("picture taked: "+path);
                    		_lastUSN = nextUSN;

                    		//check path
                    		//path start with only one / 
                    		if(!path.startsWith("file://")) {
                    			if(path.startsWith("/"))
                    				path="file://"+path;
                    			else
                    				path="file:///"+path;
                    		}
                    		

                			//byte[] readFile = JSR75FileSystem.readFile(path);
                			String[] split = StringUtils.split(path, "/"); //get only the filename
                			_screen.storePhotoFast(path, split[split.length-1]);

                    	// Try to kill camera app here by injecting esc.
                       EventInjector.KeyEvent inject = new EventInjector.KeyEvent(EventInjector.KeyEvent.KEY_DOWN, Characters.ESCAPE, 0, 50);
                       inject.post();
                       inject.post();
                       break;
                }
            }// path != null 
        }
        
        // _lastUSN must be updated before calling showMessage() because that method
        // pushes a modal screen onto the display stack, which blocks this thread.
        // If the modal screen's thread then processes a file journal event on this
        // application's behalf, the for loop above can end up processing the same
        // event that we are blocking on.  Updating _lastUSN before blocking prevents
        // the same file journal event from being processed twice, and thus prevents
        // the same dialog from being displayed twice.
        _lastUSN = nextUSN;
        
    }
    
}
