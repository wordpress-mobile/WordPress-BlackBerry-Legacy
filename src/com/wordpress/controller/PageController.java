package com.wordpress.controller;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.task.SendPageTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.Queue;
import com.wordpress.view.PageView;
import com.wordpress.view.PhotosView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.mm.PhotoPreview;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.NewMediaObjectConn;
import com.wordpress.xmlrpc.page.EditPageConn;
import com.wordpress.xmlrpc.page.NewPageConn;


public class PageController extends BlogObjectController {
	
	private PageView view = null;
	private PhotosView photoView = null;
	private PostSettingsView settingsView = null;
	private int draftPageFolder=-1; //identify draft post folder
	private boolean isDraft= false; // identify if post is loaded from draft folder
	
	private String[] pageStatusKey; // = {"draft, private, publish, localdraft"};
	private String[] pageStatusLabel;
	private String[] pageTemplateKey; 
	private String[] pageTemplateLabel; 
	private Page[] remotePages; //the page on the blog
	private Page page=null; //page object
    private boolean isModified = false; //the state of page. track changes on post..
	private SendPageTask sendTask;
	
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
	    	
			pageStatusLabel[pageStatusLabel.length-1]= LOCAL_DRAFT_LABEL;
			pageStatusKey[pageStatusLabel.length-1]= LOCAL_DRAFT_KEY;
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
		return remotePages.length; //selected no parent page (base Page page title)
	}
	
	//find the id of the selected page parent
	public void setParentPageID(int selectedFieldIndex) {
		int parentPageID= -1;
		if(remotePages.length > selectedFieldIndex)
			parentPageID = remotePages[selectedFieldIndex].getID();
		
		page.setWpPageParentID(parentPageID);
	}
		
	public String[] getPageTemplateLabels() {
		return pageTemplateLabel;
	}
	
	public String[] getPageTemplateKeys() {
		return pageTemplateKey;
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

	public void setPageAsChanged(boolean value) {
		isModified = value;
	}
	
	public boolean isPageChanged() {
		return isModified;
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
		
		if(page.getPageStatus().equals(LOCAL_DRAFT_KEY)) {
			displayMessage("Local Draft post cannot be submitted");
			return;
		}	
		 
		String[] draftPagePhotoList;

		try {
			draftPagePhotoList = PageDAO.getPagePhotoList(blog, draftPageFolder);
		} catch (Exception e) {
			displayError(e, "Cannot load photos from disk, publication failed!");
			return;
		}
		
		//SendPageDataTask sender = new SendPageDataTask (blog, page, draftPageFolder);
		Queue connectionsQueue = new Queue();
		
		//adding multimedia connection
		if(draftPagePhotoList.length > 0 ) {
			String key="";
			for (int i =0; i < draftPagePhotoList.length; i++ ) {
				key = draftPagePhotoList[i];
				NewMediaObjectConn connection = new NewMediaObjectConn (blog.getXmlRpcUrl(), 
			       		   blog.getUsername(), blog.getPassword(), blog.getId(), key);				
				connectionsQueue.push(connection);
			}
		}

		
		//adding post connection
		BlogConn connection;
		
		String remoteStatus = page.getPageStatus();
		boolean publish=false;
		if( remoteStatus.equalsIgnoreCase("private") || remoteStatus.equalsIgnoreCase("publish"))
			publish= true;
		
		if( page.getID()== -1 ) { //new page
	           connection = new NewPageConn (blog.getXmlRpcUrl(), blog.getUsername(), 
	        		   blog.getPassword(), Integer.parseInt(blog.getId()), page ,publish);
		
		} else { //edit post
			 connection = new EditPageConn (blog.getXmlRpcUrl(), blog.getUsername(), 
	        		   blog.getPassword(), Integer.parseInt(blog.getId()), page ,publish);
		
		}
		connectionsQueue.push(connection);
				
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));
		sendTask = new SendPageTask(blog, page, draftPageFolder, connectionsQueue);
		sendTask.setProgressListener(new SubmitPageTaskListener());
		//push into the Runner
		runner.enqueue(sendTask);
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			sendTask.stop();
		}
	}
	
	
	//listener on send page to blog
	private class SubmitPageTaskListener implements TaskProgressListener {

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
				FrontController.getIstance().backAndRefreshView(true);
			}
			else
				displayError(sendTask.getErrorMsg());
		}
		
		//listener for the adding blogs task
		public void taskUpdate(Object obj) {
		
		}	
	}
	
	
	public void saveDraftPage() {
		try {
		 draftPageFolder = PageDAO.storePage(blog, page, draftPageFolder);
		 setPageAsChanged(false); //set the post as not modified because we have saved it.
		 this.isDraft = true; //set as draft
		} catch (Exception e) {
			displayError(e,"Error while saving draft page!");
		}
	}
			
	public boolean dismissView() {
		
		if( isPageChanged() ) {
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
				setPageAsChanged(true);
			}
		} else {
			page.setDateCreatedGMT(new Date(authoredOn));
			setPageAsChanged(true);
		}
		
		if( page.getWpPassword() != null && !page.getWpPassword().equalsIgnoreCase(password) ){
			page.setWpPassword(password);
			setPageAsChanged(true);
		} else {
			if(page.getWpPassword()== null ){
				page.setWpPassword(password);
				setPageAsChanged(true);
			}
		}
	}
	
	public void showSettingsView(){			
		settingsView= new PostSettingsView(this, page.getDateCreatedGMT(), page.getWpPassword());		
		UiApplication.getUiApplication().pushScreen(settingsView);
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
	
	protected void storePhoto(byte[] data, String fileName) {
		try {
			EncodedImage img;
			img = EncodedImage.createEncodedImage(data, 0, -1);
			PageDAO.storePhoto(blog, draftPageFolder, data, fileName);
			photoView.addPhoto(fileName, img);
		} catch (Exception e) {
			displayError(e, "Cannot save photo to disk!");
		}
	}
	
	public void refreshView() {
		//resfresh the post view. not used.
	}
}