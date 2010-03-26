package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.MediaLibraryDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.MediaLibrary;
import com.wordpress.view.MediaLibrariesView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class MediaLibrariesController extends BaseController {
	
	private MediaLibrariesView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private Blog currentBlog=null;
	private MediaLibrary[] mediaLibrary;
	
	public MediaLibrariesController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
		getMediaLibraries();
	}

	public MediaLibrary[] getMediaLibraries() {
		try {
			//load the media Library obj 
			mediaLibrary = MediaLibraryDAO.loadAllMediaLibrary(currentBlog);
		} catch (IOException e) {
			displayError(e, "Error while reading drafts phones memory");
		} catch (RecordStoreException e) {
			displayError(e, "Error while reading drafts phones memory");
		}
		
		return mediaLibrary;
	}

	public void showView() {
	    try {    	
		    this.view= new MediaLibrariesView(this);
			UiApplication.getUiApplication().pushScreen(view);
		} catch (Exception e) {
	    	displayError(e, "Error while reading drafts phones memory");
		}
	}

	public String getCurrentBlogName() {
		return currentBlog.getName();
	}
	
	
	
	public void deleteMediaLibrary(int selected){
		MediaLibrary[] newMediaLibrary = new MediaLibrary[mediaLibrary.length-1];
		int j = 0;
		for (int i = 0; i < mediaLibrary.length; i++) {
			if(i != selected) {
				newMediaLibrary[j] = mediaLibrary[i];
				j++;				
			}
		}
		mediaLibrary = newMediaLibrary;
		try {
			MediaLibraryDAO.deleteMediaLibrary(currentBlog, selected);
		} catch (IOException e) {
	    	displayError(e, "Error while deleting from media library");
		} catch (RecordStoreException e) {
			displayError(e, "Error while deleting from media library");
		}
	}
	
	public void newMediaLibrary() {
		if (currentBlog != null) {
			MediaLibrary entry = new MediaLibrary();
			MediaLibraryController ctrl=new MediaLibraryController(currentBlog, entry, -1);
			ctrl.showView();
		}
	}
	
	public void editMediaLibrary(int selected) {
		try {
			if (selected != -1) {
				MediaLibrary entry = mediaLibrary[selected];
				MediaLibraryController ctrl=new MediaLibraryController(currentBlog, entry, selected);
				ctrl.showView();
			}
		} catch (Exception e) {
			displayError(e, "Error while loading media entry");
		}
	}	
	
	public void refreshView() {
	}
}