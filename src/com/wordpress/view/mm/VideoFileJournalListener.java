package com.wordpress.view.mm;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;

import com.wordpress.controller.BlogObjectController;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.log.Log;

/**
 * Listener to determine when files have been added to the file system.
 */
public final class VideoFileJournalListener implements FileSystemJournalListener
{
    private BlogObjectController _screen;
    private long _lastUSN; // = 0;
    
    /**
     * Constructor.
     * 
     * @param screen The screen to update when events occur.
     */
    public VideoFileJournalListener(BlogObjectController screen) {
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
            	
            	Log.debug("FS listener : "+path);
            	String[] supportedVideoFormat = MultimediaUtils.getSupportedVideoFormat();
            	boolean isSupportedFileFormat = false;
            	for (int i = 0; i < supportedVideoFormat.length; i++) {
					if(path.toLowerCase().endsWith(supportedVideoFormat[i])) {
						isSupportedFileFormat = true;
						break;
					}
				}
            	
            	path = sanitizeFilePath(path);
            	msg= "file added";
            	_lastUSN = nextUSN;
            	
            	if (!isSupportedFileFormat) break;
            	
            	switch (entry.getEvent()) {

            	case FileSystemJournalEntry.FILE_ADDED:
            		Log.debug("video added: "+path);
            		_screen.addLinkToMediaObject(path, BlogObjectController.VIDEO);
            		break;
            		
            	case FileSystemJournalEntry.FILE_RENAMED:
            		Log.debug("video renamed");
            		String oldPath = entry.getOldPath();
            		oldPath = sanitizeFilePath(oldPath);
            		Log.debug("video old file name: "+oldPath);
            		Log.debug("video new file name: "+path);
            		_screen.deleteLinkToMediaObject(oldPath);
            		_screen.addLinkToMediaObject(path, BlogObjectController.VIDEO);
            		break;
            		
            	case FileSystemJournalEntry.FILE_DELETED:
            		Log.debug("video deleted: "+path);
            		_screen.deleteLinkToMediaObject(path);
            		break;
            		
            	}//end switch

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
    
    private String sanitizeFilePath(String path) {
    	//check path
		//path start with only one / 
		if(!path.startsWith("file://")) {
			if(path.startsWith("/"))
				path="file://"+path;
			else
				path="file:///"+path;
		}
		 return path;
    }
    
}

