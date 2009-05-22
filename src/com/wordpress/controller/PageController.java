package com.wordpress.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.model.PostState;
import com.wordpress.model.Preferences;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PageView;
import com.wordpress.view.PhotosView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.mm.MultimediaPopupScreen;
import com.wordpress.view.mm.PhotoPreview;
import com.wordpress.view.mm.PhotoSnapShotView;
import com.wordpress.xmlrpc.BlogConnResponse;


public class PageController extends BlogObjectController {
	
	private PageView view = null;
	private PhotosView photoView = null;
	private PostSettingsView settingsView = null;
	ConnectionInProgressView connectionProgressView=null;
	private int draftPageFolder=-1; //identify draft post folder
	private boolean isDraft= false; // identify if post is loaded from draft folder
	
	private String[] pageStatusKey; // = {"draft, private, publish, localdraft"};
	private String[] pageStatusLabel; 
	
	private String[] pageTemplateKey; 
	private String[] pageTemplateLabel; 
	
	
	private Page[] remotePages; //the page on the blog
	
	public static final int PHOTO=1;
	public static final int BROWSER=4;
	
	private Preferences prefs = Preferences.getIstance();
	private Page page=null; //page object
	private final Blog blog;
	private PostState postState = new PostState(); //the state of post. track changes on post..
	
	//used when new page/edit page
	public PageController(Blog blog, Page page) {
		super();	
		this.blog = blog;
		this.page= page;	
		//assign new space on draft folder, used for photo IO
		try {
			draftPageFolder = PageDAO.storePage(blog, page, draftPageFolder);
		} catch (Exception e) {
			displayError(e, "Cannot create space on disk for your page!");
		}
		remotePages = PageDAO.buildPagesArray(blog.getPages());
		
	}
	
	
	//used when loading draft page from disk
	public PageController(Blog blog, Page page, int _draftPostFolder) {
		super();
		this.blog = blog;	
		this.page=page;
		this.draftPageFolder=_draftPostFolder;
		this.isDraft = true;
		remotePages = PageDAO.buildPagesArray(blog.getPages());
	}
	
	public void showView() {
		//unfolds hashtable of status
		Hashtable postStatusHash = blog.getPageStatusList();
		pageStatusLabel= new String [0];
		pageStatusKey = new String [0];
		
		if(postStatusHash != null) {
			pageStatusLabel= new String [postStatusHash.size()+1]; 
			pageStatusKey = new String [postStatusHash.size()+1];
	    	
	    	Enumeration elements = postStatusHash.keys();
	    	int i = 0;
	
	    	for (; elements.hasMoreElements(); ) {
				String key = (String) elements.nextElement();
				String value = (String) postStatusHash.get(key);
				pageStatusLabel[i] = value; //label
				pageStatusKey[i] = key;
				i++;
			}
	    	
			pageStatusLabel[pageStatusLabel.length-1]= _resources.getString(WordPress.LABEL_LOCAL_DRAFT);
			pageStatusKey[pageStatusLabel.length-1]= "localdraft";
			// end 
		}
		
		
		//unfold hashtable of page template
		Hashtable pageTemplates = blog.getPageTemplates();
		pageTemplateKey = new String[0];
		pageTemplateLabel = new String[0];
		
		if(pageTemplates != null) {
			
			pageTemplateKey = new String [pageTemplates.size()];
			pageTemplateLabel = new String [pageTemplates.size()];
			
		 	Enumeration elements = pageTemplates.keys();
	    	int i = 0;	
	    	for (; elements.hasMoreElements(); ) {
				String key = (String) elements.nextElement();
				String value = (String) pageTemplates.get(key);
				pageTemplateKey[i] = value; //note:  we have inverted the value and key
				pageTemplateLabel[i] = key; //label
				i++;
			}
		}
		//---

		
		String[] draftPostPhotoList = new String[0];
		try {
			draftPostPhotoList = PageDAO.getPagePhotoList(blog, draftPageFolder);
		} catch (Exception e) {
			displayError(e, "Cannot load photos of this page!");
		}
		this.view= new PageView(this, page);
		view.setNumberOfPhotosLabel(draftPostPhotoList.length);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public String[] getParentPagesTitle() {
		String[] titles = new String[remotePages.length+1];
		for (int i = 0; i < remotePages.length; i++) {
			titles[i] = remotePages[i].getTitle();
		}
		titles[remotePages.length]= _resources.getString(WordPress.LABEL_PARENT_PAGE_DEFAULT);
		return titles;
	}
	
	public int getParentPageFieldIndex() {
		for (int i = 0; i < remotePages.length; i++) {
			if ( remotePages[i].getID() == page.getWpPageParentID() )
				return i;
		}
		return remotePages.length; //selected no parent page
	}
	
	//create an array of page templates title
	public String[] getPageTemplatesTitle() {
		return pageTemplateLabel;
	}
	
	public int getPageTemplateFieldIndex() {

		for (int i = 0; i < pageTemplateKey.length; i++) {
			if ( pageTemplateKey[i].equals(page.getWpPageTemplate()))
				return i;
		}
		//can't find index of the selected page template, try with "default"/"home" template
		for (int i = 0; i < pageTemplateKey.length; i++) {
			if ( pageTemplateKey[i].equalsIgnoreCase("default") || pageTemplateKey[i].equalsIgnoreCase("home"))
				return i;
		}
		
		return 0; 
	}


	public void setPageAsChanged() {
		postState.setModified(true);
	}
	
	public boolean isPageChanged() {
		return postState.isModified();
	}
	
	public String[] getStatusLabels() {
		return pageStatusLabel;
	}
	
	public String[] getStatusKeys() {
		return pageStatusKey;
	}
	
		
	public int getPageStatusFieldIndex() {
		String status = page.getPageStatus();
		if(status != null )
		for (int i = 0; i < pageStatusKey.length; i++) {
			String key = pageStatusKey[i];
				
			if( key.equals(status) ) {
				return i;
			}
		}
		return pageStatusLabel.length-1;
	}
	
		

	public void sendPageToBlog() {
		
	
	}
	
	public void saveDraftPage() {
		try {
		 draftPageFolder = PageDAO.storePage(blog, page, draftPageFolder);
		 postState.setModified(false); //set the post as not modified because we have saved it.
		 this.isDraft = true; //set as draft
		} catch (Exception e) {
			displayError(e,"Error while saving draft page!");
		}
	}
			
	public boolean dismissView() {
		
		if( postState.isModified() ) {
	    	int result=this.askQuestion("Changes Made, are sure to close this screen?");   
	    	if(Dialog.YES==result) {
	    		try {
	    			if( !isDraft ){ //not previous draft saved post
	    				PageDAO.removePage(blog, draftPageFolder);
	    			}
				} catch (Exception e) {
					displayError(e, "Cannot remove temporary files from disk!");
				}
	    		FrontController.getIstance().backAndRefreshView(true);
	    		return true;
	    	} else {
	    		return false;
	    	}
		}
		
		try {
			if( !isDraft ){ //not previous draft saved post
				PageDAO.removePage(blog, draftPageFolder);
			}
		} catch (Exception e) {
			displayError(e, "Cannot remove temporary files from disk!");
		}
		
		FrontController.getIstance().backAndRefreshView(true);		
		return true;
	}
	
	
	
	public void setSettingsValues(long authoredOn, String password){
		
		if(page.getDateCreatedGMT() != null ) {
			if ( page.getDateCreatedGMT().getTime() != authoredOn ) {
				page.setDateCreatedGMT(new Date(authoredOn));
				postState.setModified(true);
			}
		} else {
			page.setDateCreatedGMT(new Date(authoredOn));
			postState.setModified(true);
		}
		
		if( page.getWpPassword() != null && !page.getWpPassword().equalsIgnoreCase(password) ){
			page.setWpPassword(password);
			postState.setModified(true);
		} else {
			if(page.getWpPassword()== null ){
				page.setWpPassword(password);
				postState.setModified(true);
			}
		}
	}
	
	public void showSettingsView(){			
		settingsView= new PostSettingsView(this, page.getDateCreated(), page.getWpPassword());		
		UiApplication.getUiApplication().pushScreen(settingsView);
	}
	 
	public void showPreview(){
		
		
	}

	/*
	 * set photos number on main post vire
	 */
	public void setPhotosNumber(int count){
		view.setNumberOfPhotosLabel(count);
	}
	
	public void showPhotosView(){
		
		String[] draftPostPhotoList;
		try {
			draftPostPhotoList = PageDAO.getPagePhotoList(blog, draftPageFolder);
			photoView= new PhotosView(this);
			for (int i = 0; i < draftPostPhotoList.length; i++) {
				String currPhotoPath = draftPostPhotoList[i];
				byte[] data=PageDAO.loadPagePhoto(blog, draftPageFolder, currPhotoPath);
				EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
				photoView.addPhoto(currPhotoPath, img);
			}			
			UiApplication.getUiApplication().pushScreen(photoView);
		} catch (Exception e) {
			displayError(e, "Cannot load photos from disk!");
			return;
		}
	}
	
	/*
	 * show selected photo
	 */
	public void showEnlargedPhoto(String key){
		System.out.println("showed photos: "+key);
		byte[] data;
		try {
			data = PageDAO.loadPagePhoto(blog, draftPageFolder, key);
			EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
			UiApplication.getUiApplication().pushScreen(new PhotoPreview(this, key ,img)); //modal screen...
		} catch (Exception e) {
			displayError(e, "Cannot load photos from disk!");
			return;
		}
		
	}
	
	
	/*
	 * delete selected photo
	 */
	public boolean deletePhoto(String key){
		System.out.println("deleting photo: "+key);
		
		try {
			PageDAO.removePagePhoto(blog, draftPageFolder, key);
			photoView.deletePhotoBitmapField(key); //delete the thumbnail
		} catch (Exception e) {
			displayError(e, "Cannot remove photo from disk!");
		}		
		return true;
		
	}
	
	//* called by photoview */
	public void showAddPhotoPopUp() {
		int response= BROWSER;
		
    	MultimediaPopupScreen multimediaPopupScreen = new MultimediaPopupScreen();
    	UiApplication.getUiApplication().pushModalScreen(multimediaPopupScreen); //modal screen...
		response = multimediaPopupScreen.getResponse();
			
		switch (response) {
		case BROWSER:
           	 String imageExtensions[] = {"jpg", "jpeg","bmp", "png", "gif"};
             FileSelectorPopupScreen fps = new FileSelectorPopupScreen(null, imageExtensions);
             fps.pickFile();
             String theFile = fps.getFile();
             if (theFile == null){
                 Dialog.alert("Screen was dismissed. No file was selected.");
             } else {
            	 String[] fileNameSplitted = StringUtils.split(theFile, "/");
            	 String ext= fileNameSplitted[fileNameSplitted.length-1];
				try {
					byte[] readFile = JSR75FileSystem.readFile(theFile);
					addPhoto(readFile,ext);	
				} catch (IOException e) {
					displayError(e, "Cannot load photo from disk!");
				}
             }					
			break;
			
		case PHOTO:
			PhotoSnapShotView snapView = new PhotoSnapShotView(this);
			UiApplication.getUiApplication().pushScreen(snapView); //modal screen...
			break;
			
		default:
			break;
		}
				
	}
	
	public void addPhoto(byte[] data, String fileName){
		if(fileName == null) 
			fileName= String.valueOf(System.currentTimeMillis()+".jpg");
		
		EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
				
		//check if blog has "photo resize option" selected
		if (blog.isResizePhotos()){
			EncodedImage rescaled= MultimediaUtils.bestFit2(img, 640, 480);
			img=rescaled;
		} 

		try {
			PageDAO.storePagePhoto(blog, draftPageFolder, data, fileName);
		} catch (Exception e) {
			displayError(e, "Cannot save photo to disk!");
		}
		
		photoView.addPhoto(fileName, img);
	}
	
	public void refreshView() {
		//resfresh the post view. not used.
	}
	




	//callback for preview
	private class SendGetTamplateCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}			
				}
			});
		}
	}
	
}

	/*
	//callback for send post to the blog
	private class getPostStatusListCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
						
				}
			});
		}
	}
	*/

