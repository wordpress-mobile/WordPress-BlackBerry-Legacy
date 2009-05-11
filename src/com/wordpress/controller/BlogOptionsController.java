package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.view.BlogOptionsView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class BlogOptionsController extends BaseController {
	
	private BlogOptionsView view = null;
	
	private Hashtable guiValues= new Hashtable();
	private final Blog blog;
	
	public BlogOptionsController(Blog blog) {
		super();
		this.blog = blog;
		//read the int value for maxPost showed
		int recentPostSelected= blog.getMaxPostCount();
		//find the index of the value in the predefined array
		int indexRecPost=0;
	 	for (int i = 0; i < AddBlogsController.recentsPostValues.length; i++) {
	 		if (AddBlogsController.recentsPostValues[i] == recentPostSelected ) {
	 			indexRecPost=i;
	 			break;
	 		}
	    }
		guiValues.put("user", blog.getUsername());
		guiValues.put("pass", blog.getPassword());
		guiValues.put("recentpost", AddBlogsController.recentsPostValuesLabel);
		guiValues.put("recentpostselected", new Integer(indexRecPost));
		guiValues.put("isresphotos", new Boolean(blog.isResizePhotos()));
		this.view= new BlogOptionsView(this,guiValues);
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}


	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	saveAndBack();
	   }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	        backCmd();
	   }
	};

	public FieldChangeListener getOkButtonListener() {
		return listenerOkButton;
	}
	   
	public FieldChangeListener getBackButtonListener() {
		return listenerBackButton;
	}
	
	public String getBlogName() {
		return blog.getName();
	}
	
	private boolean isModified() {
		boolean isModified=false;
		
		String pass= view.getBlogPass();
		String user= view.getBlogUser();
		int maxPostIndex=view.getMaxRecentPostIndex();
		int valueMaxPostCount=AddBlogsController.recentsPostValues[maxPostIndex];
		boolean isResPhotos = view.isResizePhoto();
		 
		if(!blog.getUsername().equals(user) || !blog.getPassword().equals(pass)
			|| blog.getMaxPostCount() != valueMaxPostCount || isResPhotos != blog.isResizePhotos() ) {
			isModified=true;
		}
		return isModified;
	}
	
	//called when user click the OK button
	private void  saveAndBack(){
		boolean isModified=this.isModified();
		
		if(!isModified) {
			backCmd();
			return;
		}
		
		String pass= view.getBlogPass();
		String user= view.getBlogUser();
		int maxPostIndex=view.getMaxRecentPostIndex();
		int valueMaxPostCount=AddBlogsController.recentsPostValues[maxPostIndex];
		boolean isResPhotos = view.isResizePhoto();
		
		blog.setPassword(pass);
		blog.setUsername(user);
		blog.setResizePhotos(isResPhotos);
		blog.setMaxPostCount(valueMaxPostCount);
		
		try {
			BlogDAO.updateBlog(blog);
		} catch (Exception e) {
			displayError(e, "Error while saving blog options");
		}
		
		backCmd();
	}
	
	public boolean dismissView() {
		boolean isModified=this.isModified();
			
		if(!isModified) {
			backCmd();
			return true;
		}
		
		String quest="Changes Made!";
    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
    	int choice=infoView.doModal();    	 
    	if(Dialog.DISCARD == choice) {
    		System.out.println("la scelta dell'utente è discard");
    		backCmd();
    		return true;
    	}else if(Dialog.SAVE == choice) {
    		System.out.println("la scelta dell'utente è save");
    		saveAndBack();
    		return true;
    	} else {
    		System.out.println("la scelta dell'utente è cancel");
    		return false;
    	}
	}

	public void refreshView() {
		
	}
}