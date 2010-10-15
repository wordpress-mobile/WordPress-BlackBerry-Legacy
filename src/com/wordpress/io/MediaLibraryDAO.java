package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Blog;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.MediaLibrary;
import com.wordpress.utils.log.Log;

public class MediaLibraryDAO implements BaseDAO {
	
	public static synchronized MediaLibrary[] loadAllMediaLibrary(Blog blog) throws IOException, RecordStoreException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+MEDIALIBRARY_FILE;
    	MediaLibrary[] mediaLib = null;
    	
    	if (!JSR75FileSystem.isFileExist(commentsFilePath)){
    		return new MediaLibrary[0];
    	}   	
    	
    	FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
		DataInputStream in = fc.openDataInputStream();
    	Serializer ser= new Serializer(in);
    	Vector comments = (Vector) ser.deserialize();
    	in.close();
    	fc.close();
    	
    	Vector controlledMediaEntries = new Vector();
    	String error = null;
    	for (int i = 0; i < comments.size(); i++) {
    		Hashtable elementAt = (Hashtable) comments.elementAt(i);
    		MediaLibrary tmpEntry = hashtable2MediaLibraryEntry(elementAt);
    		if(tmpEntry != null)
    		 controlledMediaEntries.addElement(tmpEntry);
    		else 
    			error = new String ("Error while loading media library from device memory");
		}

    	mediaLib = new MediaLibrary[controlledMediaEntries.size()];
    	for (int i = 0; i < controlledMediaEntries.size(); i++) {
    		MediaLibrary elementAt = (MediaLibrary) controlledMediaEntries.elementAt(i);
    		mediaLib[i] = elementAt;
		}
    	
    	return mediaLib;
	}
	
	
	public static synchronized boolean deleteMediaLibrary(Blog blog, int itemIdx) throws IOException, RecordStoreException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+MEDIALIBRARY_FILE;
    	
    	if (!JSR75FileSystem.isFileExist(commentsFilePath)){
    		return false;
    	}   	
    	
		FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
		
		DataInputStream in = fc.openDataInputStream();
    	Serializer ser= new Serializer(in);
    	Vector comments = (Vector) ser.deserialize();
    	in.close();
    	comments.removeElementAt(itemIdx);
    	
    	//store objs
//    	JSR75FileSystem.createFile(commentsFilePath); //create the file
//		FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
		DataOutputStream out = fc.openDataOutputStream();
    	ser= new Serializer(out);
    	ser.serialize(comments);
    	out.close();
    	fc.close();	  	
   
    	return true;
	}
	
	public static synchronized boolean updateMediaLibrary(Blog blog, int itemIdx, MediaLibrary updatedItem) throws IOException, RecordStoreException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+MEDIALIBRARY_FILE;
    	Vector comments = null;
    	Serializer ser = null;
    	
    	if (JSR75FileSystem.isFileExist(commentsFilePath)){
    		FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
    		DataInputStream in = fc.openDataInputStream();
    		ser= new Serializer(in);
    		comments = (Vector) ser.deserialize();
    		in.close();
    		fc.close();
    	} else {
    		comments = new Vector();
    	}
    	
    	Hashtable tmpData = mediaItem2Hashtable(updatedItem);
    	if(itemIdx != -1) {
    		comments.setElementAt(tmpData, itemIdx);
    	} else {
    		comments.addElement(tmpData);
    	}
    	//store objs
    	JSR75FileSystem.createFile(commentsFilePath); //create the file
		FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
		DataOutputStream out = fc.openDataOutputStream();		
    	ser= new Serializer(out);
    	ser.serialize(comments);
    	out.close();
    	fc.close();
    	return true;
	}
	
	
	//store comments and updates comment waiting notification info
	public static synchronized void storeMediaLibraries(Blog blog, MediaLibrary[] mediaEntries) throws IOException, RecordStoreException {
		Log.trace(">>> storeMediaLibray ");
		Vector serializedData = new Vector(mediaEntries.length);
		for (int i = 0; i < mediaEntries.length; i++) {
			Hashtable tmpData = mediaItem2Hashtable(mediaEntries[i]);
			serializedData.setElementAt(tmpData, i);
		}
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
		String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+MEDIALIBRARY_FILE;
    	
    	JSR75FileSystem.createFile(commentsFilePath); //create the file
		FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
		DataOutputStream out = fc.openDataOutputStream();
    	Serializer ser= new Serializer(out);
    	ser.serialize(serializedData);
    	out.close();
    	fc.close();
		Log.trace("<<< storeMediaLibray");
	}
	
	private static synchronized Hashtable mediaItem2Hashtable(MediaLibrary item) {
        Hashtable content = new Hashtable();
        if (item.getTitle() != null) {
            content.put("title", item.getTitle());
        }
        content.put("cutandpaste", new Boolean(item.isCutAndPaste()));
        
		//convert media object before save them
		Vector mediaObjects = item.getMediaObjects();
		Vector hashedMediaIbjects = new Vector(mediaObjects.size());
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			hashedMediaIbjects.addElement(tmp.serialize());
			}
		content.put("mediaObjects", hashedMediaIbjects);
		
		
		if(item.isPhotoResizing() != null) {
			content.put("IsPhotoResizing", item.isPhotoResizing());
		}
		if(item.getImageResizeWidth() != null) {
			content.put("imageResizeWidth", item.getImageResizeWidth());
		}
		if(item.getImageResizeHeight() != null) {
			content.put("imageResizeHeight", item.getImageResizeHeight());
		}
		
		if(item.isVideoResizing() !=null)
			content.put("IsVideoResizing", item.isVideoResizing());
		
		if(item.getImageResizeWidth() != null)
			content.put("videoResizeWidth", item.getVideoResizeWidth());
		
		if(item.getImageResizeHeight() != null)
			content.put("videoResizeHeight", item.getVideoResizeHeight());
		
		return content;
	}
	
	private static synchronized MediaLibrary hashtable2MediaLibraryEntry(Hashtable storedData) {
		try {
			MediaLibrary entry = new MediaLibrary();
			entry.setTitle((String) storedData.get("title"));
			Boolean isCutAndPaste = (Boolean)storedData.get("cutandpaste");
			entry.setCutAndPaste(isCutAndPaste.booleanValue());
			
			if(storedData.get("mediaObjects") != null) {
				Vector hashedMediaIbjects = (Vector) storedData.get("mediaObjects");
				Vector mediaObjects = new Vector(hashedMediaIbjects.size());
				for (int i = 0; i < hashedMediaIbjects.size(); i++) {
					Hashtable tmp = (Hashtable) hashedMediaIbjects.elementAt(i);
					MediaEntry tmpMedia = MediaEntry.deserialize(tmp);
					if(tmpMedia != null )
						mediaObjects.addElement(tmpMedia);
					}
			entry.setMediaObjects(mediaObjects);
			}
			
			//set the prop for photo res
			if(storedData.get("IsPhotoResizing") != null) {
				entry.setPhotoResizing((Boolean) storedData.get("IsPhotoResizing"));
			}
			
			if(storedData.get("imageResizeWidth") != null) {
				entry.setImageResizeWidth((Integer) storedData.get("imageResizeWidth"));
			}
			
			if(storedData.get("imageResizeHeight") != null) {
				entry.setImageResizeHeight((Integer) storedData.get("imageResizeHeight"));
			}
			
			//set the prop for videopres resizing options
			if(storedData.get("IsVideoResizing") != null) {
				entry.setVideoResizing((Boolean) storedData.get("IsVideoResizing"));
			}
			
			// Set the image resize dimension properties
			if(storedData.get("videoResizeWidth") != null) {
				entry.setVideoResizeWidth((Integer) storedData.get("videoResizeWidth"));
			}
			
			if(storedData.get("videoResizeHeight") != null) {
				entry.setVideoResizeHeight((Integer) storedData.get("videoResizeHeight"));
			}
			
			return entry;
		} catch (Exception e) {
			Log.trace(e, "Error while reading media library entry from device storage");
			return null;
		}
	}
}