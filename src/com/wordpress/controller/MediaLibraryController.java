package com.wordpress.controller;

import java.io.IOException;
import java.util.Date;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.MediaLibraryDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.MediaLibrary;
import com.wordpress.task.SendToBlogTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.log.Log;
import com.wordpress.view.MediaLibraryView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;

public class MediaLibraryController extends BlogObjectController {

	private MediaLibraryView view;
	private int internalIdx;

	public MediaLibraryController(Blog currentBlog, MediaLibrary entry, int internalIdx) {
		super(currentBlog, entry);
		this.internalIdx = internalIdx;
		checkMediaObjectLinks();
	}
	
	private MediaLibrary getMediaLibraryObj() {
		return (MediaLibrary)blogEntry;
	}

	public void setAuthDate(long authoredOn) {
		
	}

	public void setPassword(String password) {
		
	}

	public void setPhotoResizing(boolean isPhotoRes, Integer imageResizeWidth,
			Integer imageResizeHeight) {
			
	}

	public void setPhotosNumber(int count) {
			
	}
	
	
	public void saveLibrary() throws IOException, RecordStoreException {
		MediaLibraryDAO.updateMediaLibrary(blog, internalIdx, getMediaLibraryObj());
	}
	
	public boolean dismissView() {
		
		if( isObjectChanged() ) {
			
			String quest=_resources.getString(WordPressResource.MESSAGE_INQUIRY_DIALOG_BOX);
	    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
	    	int choice=infoView.doModal();  
	    	
	    	if(Dialog.DISCARD == choice) {
	    		FrontController.getIstance().backAndRefreshView(false);
	    		return true;
	    	} else if(Dialog.SAVE == choice) {
	    		try {
					MediaLibraryDAO.updateMediaLibrary(blog, internalIdx, getMediaLibraryObj());
					FrontController.getIstance().backAndRefreshView(false);
				} catch (IOException e) {
					displayError(e, "Error while saving media library");
				} catch (RecordStoreException e) {
					displayError(e, "Error while saving media library");
				}

	    		return true;
	    	} else {
	    		Log.trace("user has selected cancel");
	    		return false;
	    	}
		} else {
			FrontController.getIstance().backAndRefreshView(false);		
		}
		return true;
	}
		
	public void showSettingsView() {
		MediaLibrary entry = getMediaLibraryObj();
		boolean isPhotoResing = blog.isResizePhotos(); //first set the value as the predefined blog value
		Integer imageResizeWidth = blog.getImageResizeWidth();
		Integer imageResizeHeight = blog.getImageResizeHeight();

		if (entry.isPhotoResizing() != null ) {
			isPhotoResing = entry.isPhotoResizing().booleanValue();			
		}
		if (entry.getImageResizeWidth() != null ) {
			imageResizeWidth = entry.getImageResizeWidth();
		}
		if( entry.getImageResizeHeight() != null ) {
			imageResizeHeight = entry.getImageResizeHeight();
		}
		settingsView= new PostSettingsView(this, new Date(), "", isPhotoResing, imageResizeWidth, imageResizeHeight);
		settingsView.removeDateAndPasswdField();
		UiApplication.getUiApplication().pushScreen(settingsView);
	}

	public void refreshView() {
	}

	public void showView() {
		this.view= new MediaLibraryView(this, getMediaLibraryObj());
		//view.setNumberOfPhotosLabel(draftPostPhotoList.length);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public void sendLibraryToBlog() {
		
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));
		sendTask = new SendToBlogTask(blog, getMediaLibraryObj());
		sendTask.setProgressListener(new SubmitlibraryTaskListener());
		//push into the Runner
		WordPressCore.getInstance().getTasksRunner().enqueue(sendTask);
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
			sendTask.stop();
		}
	}

//listener on send page to blog
private class SubmitlibraryTaskListener implements TaskProgressListener {

	public void taskComplete(Object obj) {
		if (sendTask.isStopped()) 
			return;  //task  stopped previous
		
		if(connectionProgressView != null)
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					connectionProgressView.close();
				}
			});
		
		if (!sendTask.isError()){
			if(internalIdx != -1) {
				//delete the draft from disk
				try {
					MediaLibraryDAO.deleteMediaLibrary(blog, internalIdx);
				} catch (IOException e) {
			    	displayError(e, "Error while deleting from media library");
				} catch (RecordStoreException e) {
					displayError(e, "Error while deleting from media library");
				}
			}
			FrontController.getIstance().backAndRefreshView(false);
		}
		else
			displayError(sendTask.getErrorMsg());
	}
	
	//listener for the adding blogs task
	public void taskUpdate(Object obj) {
	
	}	
}
}