package com.wordpress.controller;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.FileUtils;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.task.SendToBlogTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.Queue;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.PageView;
import com.wordpress.view.PhotosView;
import com.wordpress.view.PreviewView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.NewMediaObjectConn;
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
		super();	
		this.blog = blog;
		this.page= page;	
		//assign new space on draft folder, used for photo IO
		try {
			draftFolder = PageDAO.storePage(blog, page, draftFolder);
		} catch (Exception e) {
			displayError(e, _resources.getString(WordPress.ERROR_NOT_ENOUGHT_SPACE));
		}
		remotePages = PageDAO.buildPagesArray(blog.getPages());
		
	}
	
	
	//used when loading draft page from disk
	public PageController(Blog blog, Page page, int _draftPostFolder) {
		super();
		this.blog = blog;	
		this.page=page;
		this.draftFolder=_draftPostFolder;
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
			draftPostPhotoList = PageDAO.getPagePhotoList(blog, draftFolder);
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
			displayMessage(_resources.getString(WordPressResource.MESSAGE_LOCAL_DRAFT_NOT_SUBMIT));
			return;
		}	
		 
		String[] draftPagePhotoList;

		try {
			draftPagePhotoList = PageDAO.getPagePhotoList(blog, draftFolder);
		} catch (Exception e) {
			displayError(e, _resources.getString(WordPressResource.ERROR_LOADING_PHOTO));
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
		//if( remoteStatus.equalsIgnoreCase("private") || remoteStatus.equalsIgnoreCase("publish"))
		if(remoteStatus.equalsIgnoreCase("publish"))
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
		sendTask = new SendToBlogTask(blog, page, draftFolder, connectionsQueue);
		sendTask.setProgressListener(new SubmitPageTaskListener());
		//push into the Runner
		runner.enqueue(sendTask);
		
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
		 draftFolder = PageDAO.storePage(blog, page, draftFolder);
		 setPageAsChanged(false); //set the post as not modified because we have saved it.
		 this.isDraft = true; //set as draft
		} catch (Exception e) {
			displayError(e,"Error while saving draft page!");
		}
	}
			
	public boolean dismissView() {
		
		if( isPageChanged() ) {
			
			String quest=_resources.getString(WordPressResource.MESSAGE_INQUIRY_DIALOG_BOX);
	    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
	    	int choice=infoView.doModal();  
	    	
	    	if(Dialog.DISCARD == choice) {

	    		try {
	    			if( !isDraft ){ //not previous draft saved post
	    				PageDAO.removePage(blog, draftFolder);
	    			}
				} catch (Exception e) {
					displayError(e, "Cannot remove temporary files from disk!");
				}
	    		FrontController.getIstance().backAndRefreshView(false);
	    		return true;
	    		
	    	} else if(Dialog.SAVE == choice) {
	    		saveDraftPage();
	    		FrontController.getIstance().backAndRefreshView(false);
	    		return true;
	    	} else {
	    		Log.trace("la scelta dell'utente è cancel");
	    		return false;
	    	}
		}
		
		try {
			if( !isDraft ){ //not previous draft saved post
				PageDAO.removePage(blog, draftFolder);
			}
		} catch (Exception e) {
			displayError(e, "Cannot remove temporary files from disk!");
		}
		
		FrontController.getIstance().backAndRefreshView(false);		
		return true;
	}
	
	
	public void setAuthDate(long authoredOn) {
		if(page.getDateCreatedGMT() != null ) {
			if ( page.getDateCreatedGMT().getTime() != authoredOn ) {
				page.setDateCreatedGMT(new Date(authoredOn));
				setPageAsChanged(true);
			}
		} else {
			page.setDateCreatedGMT(new Date(authoredOn));
			setPageAsChanged(true);
		}
	}


	public void setPassword(String password) {
		
		if( page.getWpPassword() != null && !page.getWpPassword().equalsIgnoreCase(password) ){
			page.setWpPassword(password);
			setPageAsChanged(true);
		} else {
			if(page.getWpPassword() == null ){
				page.setWpPassword(password);
				setPageAsChanged(true);
			}
		}
	}
	
	public void setPhotoResizing(boolean isPhotoRes) {
		
		if( page.getIsPhotoResizing() != null && !page.getIsPhotoResizing().booleanValue()== isPhotoRes ){
			page.setIsPhotoResizing(new Boolean(isPhotoRes));
			setPageAsChanged(true);
		} else {
			if(page.getIsPhotoResizing() == null ){
				page.setIsPhotoResizing(new Boolean(isPhotoRes));
				setPageAsChanged(true);
			}
		}
		
	}
	
	/*
	 * set photos number on main post view
	 */
	public void setPhotosNumber(int count){
		view.setNumberOfPhotosLabel(count);
	}
	
	public void showPhotosView(){
		
		String[] draftPostPhotoList;
		try {
			draftPostPhotoList = PageDAO.getPagePhotoList(blog, draftFolder);
			photoView= new PhotosView(this);
			for (int i = 0; i < draftPostPhotoList.length; i++) {
				String currPhotoPath = draftPostPhotoList[i];
				byte[] data=PageDAO.loadPagePhoto(blog, draftFolder, currPhotoPath);
				EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
				photoView.addPhoto(currPhotoPath, img);
			}			
			UiApplication.getUiApplication().pushScreen(photoView);
		} catch (Exception e) {
			displayError(e, "Cannot load photos from disk!");
			return;
		}
	}
			
	
	public void startLocalPreview(String title, String content, String tags){
		//photo are reader from the model
		
		String[] draftPostPhotoList = getPhotoList();
		StringBuffer photoHtmlFragment = new StringBuffer();
		
		for (int i = 0; i < draftPostPhotoList.length; i++) {
			try {
				String photoRealPath = PageDAO.getPhotoRealPath(blog, draftFolder, draftPostPhotoList[i]);
				photoHtmlFragment.append("<p>"+
						"<img class=\"alignnone size-full wp-image-364\"" +
						" src=\""+photoRealPath+"\" alt=\"\" " +
				"</p>");
			} catch (IOException e) {
			} catch (RecordStoreException e) {
			}
		}
		photoHtmlFragment.append("<p>&nbsp;</p>");
		
		String html = FileUtils.readTxtFile("defaultPostTemplate.html");
		if(title == null || title.length() == 0) title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
		html = StringUtils.replaceAll(html, "!$title$!", title);
		html = StringUtils.replaceAll(html, "<p>!$text$!</p>", buildBodyHtmlFragment(content)+ photoHtmlFragment.toString());
		html = StringUtils.replaceAll(html, "!$mt_keywords$!", "");//The pages have no tags
		html = StringUtils.replaceAll(html, "!$categories$!", ""); //The pages have no categories
		
		UiApplication.getUiApplication().pushScreen(new PreviewView(html));	
	}
	
	public void refreshView() {
		//resfresh the post view. not used.
	}
}