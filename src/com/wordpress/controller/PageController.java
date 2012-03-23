package com.wordpress.controller;

import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.task.SendToBlogTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.log.Log;
import com.wordpress.view.PageView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.page.EditPageConn;
import com.wordpress.xmlrpc.page.NewPageConn;


public class PageController extends BlogObjectController {
	
	private PageView view = null;	
	private String[] pageStatusKey; // = {"draft, private, publish, localdraft"};
	private String[] pageStatusLabel;
	private String[] pageTemplateKey; 
	private String[] pageTemplateLabel; 
	private Page[] remotePages; //the page on the blog
	
	//used when new page/edit page
	public PageController(Blog blog, Page page) {
		super(blog, page);	
		//assign new space on draft folder, used for photo IO
		try {
			draftFolder = PageDAO.storePage(blog, page, draftFolder);
		} catch (Exception e) {
			displayError(e, _resources.getString(WordPress.ERROR_NOT_ENOUGH_SPACE));
		}
		remotePages = PageDAO.buildPagesArray(blog.getPages());
		checkMediaObjectLinks();
	}
		
	//used when loading draft page from disk
	public PageController(Blog blog, Page page, int _draftPostFolder) {
		super(blog, page);
		this.draftFolder=_draftPostFolder;
		this.isDraft = true;
		remotePages = PageDAO.buildPagesArray(blog.getPages());
		checkMediaObjectLinks();
	}
	
	protected Page getPageObj() {
		return (Page)blogEntry;
	}
	
	public void showView() {
		//unfolds hashtable of status
		Hashtable postStatusHash = blog.getPageStatusList();
		pageStatusLabel= new String [0];
		pageStatusKey = new String [0];

		if(postStatusHash != null) {
			pageStatusLabel= new String [postStatusHash.size()]; 
			pageStatusKey = new String [postStatusHash.size()];
	    	
	    	Enumeration elements = postStatusHash.keys();
	    	int i = 0;
	
	    	for (; elements.hasMoreElements(); ) {
				String key = (String) elements.nextElement();
				String value = (String) postStatusHash.get(key);
				pageStatusLabel[i] = value; //label
				pageStatusKey[i] = key;
				i++;
			}
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

		
		String[] draftPostPhotoList = getPhotoList();
		this.view= new PageView(this, getPageObj());
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
			if ( remotePages[i].getID().equalsIgnoreCase(getPageObj().getWpPageParentID()) )
				return i;
		}
		return remotePages.length; //selected no parent page (base Page page title)
	}
	
	//find the id of the selected page parent
	public void setParentPageID(int selectedFieldIndex) {
		String parentPageID = null;
		if(remotePages.length > selectedFieldIndex)
			parentPageID = remotePages[selectedFieldIndex].getID();
		getPageObj().setWpPageParentID(parentPageID);
	}
		
	public String[] getPageTemplateLabels() {
		return pageTemplateLabel;
	}
	
	public String[] getPageTemplateKeys() {
		return pageTemplateKey;
	}
	
	public int getPageTemplateFieldIndex() {

		for (int i = 0; i < pageTemplateKey.length; i++) {
			if ( pageTemplateKey[i].equals(getPageObj().getWpPageTemplate()))
				return i;
		}
		//can't find index of the selected page template, try with "default"/"home" template
		for (int i = 0; i < pageTemplateKey.length; i++) {
			if ( pageTemplateKey[i].equalsIgnoreCase("default") || pageTemplateKey[i].equalsIgnoreCase("home"))
				return i;
		}
		
		return 0; 
	}
	
	public String[] getStatusLabels() {
		return pageStatusLabel;
	}
	
	public String[] getStatusKeys() {
		return pageStatusKey;
	}
		
	public int getPageStatusFieldIndex() {
		String status = getPageObj().getPageStatus();
		if(status != null ){
			//trick to remove the localdraft status. ver 1.6
			if ( LOCAL_DRAFT_KEY.equals(status) ) {
				status = "publish";
				getPageObj().setPageStatus("publish");
			}
			for (int i = 0; i < pageStatusKey.length; i++) {
				String key = pageStatusKey[i];
				if( key.equals(status) ) {
					return i;
				}
			}
		}
		return pageStatusLabel.length-1;
	}

	public void sendPageToBlog() {
		
		if(getPageObj().getPageStatus() == null || getPageObj().getPageStatus().equals(LOCAL_DRAFT_KEY)) {
			//displayMessage(_resources.getString(WordPressResource.MESSAGE_LOCAL_DRAFT_NOT_SUBMIT));
			//return;
			getPageObj().setPageStatus("publish");
		}	
		
		String remoteStatus = getPageObj().getPageStatus();
		boolean publish=false;
		//if( remoteStatus.equalsIgnoreCase("private") || remoteStatus.equalsIgnoreCase("publish"))
		if(remoteStatus.equalsIgnoreCase("publish"))
			publish= true;
		
		BlogConn connection;
		if( getPageObj().getID()== null ) { //new page
	           connection = new NewPageConn (blog.getXmlRpcUrl(), blog.getUsername(), 
	        		   blog.getPassword(), blog.getId(), getPageObj() ,publish);
		} else { //edit post
			 connection = new EditPageConn (blog.getXmlRpcUrl(), blog.getUsername(), 
	        		   blog.getPassword(), blog.getId(), getPageObj() ,publish);
		}
		if(blog.isHTTPBasicAuthRequired()) {
			connection.setHttp401Password(blog.getHTTPAuthPassword());
			connection.setHttp401Username(blog.getHTTPAuthUsername());
		}
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));
		sendTask = new SendToBlogTask(blog, getPageObj(), draftFolder, connection);
		sendTask.setProgressListener(new SubmitPageTaskListener());
		//push into the Runner
		WordPressCore.getInstance().getTasksRunner().enqueue(sendTask);
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
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
		 draftFolder = PageDAO.storePage(blog, getPageObj(), draftFolder);
		 setObjectAsChanged(false); //set the post as not modified because we have saved it.
		 //the changes over the clean state for the UI Fields will be done into view-> save-draft menu item
		 this.isDraft = true; //set as draft
		 backCmd();
		} catch (Exception e) {
			displayError(e,"Error while saving draft page!");
		}
	}
			
	public boolean dismissView() {
		
		if( isObjectChanged() ) {
			
			String quest=_resources.getString(WordPressResource.MESSAGE_INQUIRY_DIALOG_BOX);
	    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
	    	int choice=infoView.doModal();  
	    	
	    	if(Dialog.DISCARD == choice) {

	    		try {
	    			if( !isDraft ){ //not previous draft saved post
	    				PageDAO.removePage(blog, draftFolder);
	    			}
				} catch (Exception e) {
					Log.error(e, "Cannot remove temporary files from disk!");
					displayErrorAndWait("Cannot remove temporary files from disk!");
				}
	    		FrontController.getIstance().backAndRefreshView(false);
	    		return true;
	    		
	    	} else if(Dialog.SAVE == choice) {
	    		saveDraftPage();
	    		FrontController.getIstance().backAndRefreshView(false);
	    		return true;
	    	} else {
	    		Log.trace("user has selected cancel");
	    		return false;
	    	}
		}
		
		try {
			if( !isDraft ){ //not previous draft saved post
				PageDAO.removePage(blog, draftFolder);
			}
		} catch (Exception e) {
			displayErrorAndWait(e, "Cannot remove temporary files from disk!");
		}
		
		FrontController.getIstance().backAndRefreshView(false);		
		return true;
	}
	
	
	public void setAuthDate(long authoredOn) {
		Page page = getPageObj();
		if(page.getDateCreatedGMT() != null ) {
			if ( page.getDateCreatedGMT().getTime() != authoredOn ) {
				page.setDateCreatedGMT(new Date(authoredOn));
				setObjectAsChanged(true);
			}
		} else {
			page.setDateCreatedGMT(new Date(authoredOn));
			setObjectAsChanged(true);
		}
		view.updateSendMenuItemAndButtonLabel();
	}


	public void setPassword(String password) { //password is not available for pages. it is in the model, but not on the UI
		/*Page page = getPageObj();
		if( page.getWpPassword() != null && !page.getWpPassword().equalsIgnoreCase(password) ){
			page.setWpPassword(password);
			setObjectAsChanged(true);
		} else {
			if(page.getWpPassword() == null ){
				page.setWpPassword(password);
				setObjectAsChanged(true);
			}
		}*/
	}
	
	public boolean isPingbacksAndTrackbacksAllowed(){
		return getPageObj().isPingsEnabled();
	}
	public void setPingbacksAndTrackbacksAllowed(boolean value) {
		getPageObj().setPingsEnabled(value);
		setObjectAsChanged(true);
	}
	public boolean isCommentsAllowed() {
		return getPageObj().isCommentsEnabled();
	}
	public void setCommentsAllowed(boolean value){
		getPageObj().setCommentsEnabled(value);
		setObjectAsChanged(true);
	}
	
	/*
	 * set photos number on main post view
	 */
	public void setPhotosNumber(int count){
		view.setNumberOfPhotosLabel(count);
	}
	
	public void refreshView() {
		//resfresh the post view. not used.
	}
	
	public void showSettingsView(){
		Page page = getPageObj();
		settingsView = new PostSettingsView(this, page.getDateCreatedGMT(), null);		
		UiApplication.getUiApplication().pushScreen(settingsView);
	}
}