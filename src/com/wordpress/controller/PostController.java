package com.wordpress.controller;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.location.Location;
import javax.microedition.location.LocationException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.DraftDAO;
import com.wordpress.location.Gps;
import com.wordpress.model.Blog;
import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.task.SendToBlogTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.ExcerptView;
import com.wordpress.view.NewCategoryView;
import com.wordpress.view.PostCategoriesView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.PostView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.NewCategoryConn;
import com.wordpress.xmlrpc.post.EditPostConn;
import com.wordpress.xmlrpc.post.NewPostConn;


public class PostController extends BlogObjectController {
	
	private PostView view = null;
	private PostCategoriesView catView = null;
	private String[] postStatusKey; // = {"draft, pending, private, publish, localdraft"};
	private String[] postStatusLabel; 
	
	//used when new post / or when post is loaded from server 
	public PostController(Post post) {
		super(post.getBlog(), post);	
		//assign new space on draft folder, used for photo IO
		try {
			draftFolder = DraftDAO.storePost(post, draftFolder);
		} catch (Exception e) {
			displayError(e, _resources.getString(WordPress.ERROR_NOT_ENOUGHT_SPACE));
		}
		checkMediaObjectLinks();
	}

	//used when loading from draft folder on the device memory
	public PostController(Post post, int _draftPostFolder) {
		super(post.getBlog(), post);	
		this.draftFolder=_draftPostFolder;
		this.isDraft = true;
		checkMediaObjectLinks();
	}

	protected Post getPostObj() {
		return (Post)blogEntry;
	}
		
	public void startGeoTagging(boolean sentAfterPosition) {
		Gps gps = new Gps();
		gps.addObserver(new GPSLocationCallBack(sentAfterPosition));
		gps.findMyPosition();
	}
	
	private class GPSLocationCallBack implements Observer {
		private boolean sentAfterPosition = false;
		
		GPSLocationCallBack (boolean sendAfterPosition){
			this.sentAfterPosition = sendAfterPosition;
		}
		
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					if(object == null) {
					
					} else if(object instanceof Location) {
						
						//javax.microedition.location.Location
						Location location = (Location) object;
						double latitude = location.getQualifiedCoordinates().getLatitude();
						double longitude = location.getQualifiedCoordinates().getLongitude();
						updateLocationCustomField(latitude, longitude);
						
						if(sentAfterPosition)
							sendPostToBlog();
						else {
							saveDraftPost();
						}

					} else if(object instanceof LocationException) {
						displayError((LocationException)object, "GPS Error");
					}
				}//end run 
			});
		}
	}

	
	/**
	 * Add or upgrade the location custom tags
	 * 
	 */
	protected void updateLocationCustomField(double latitude, double longitude) {
		
		Log.debug(">>> updateLocationCustomField ");
		Post post = getPostObj();
		Vector customFields = post.getCustomFields();
		int size = customFields.size();
    	Log.debug("Found "+size +" custom fields");
    	boolean latitudeFound = false;
    	boolean longitudeFound = false;
    	boolean locationPublicFound = false;
    	
		for (int i = 0; i <size; i++) {
			Log.debug("Elaborating custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)customFields.elementAt(i);
				
				String ID = (String)customField.get("id");
				String key = (String)customField.get("key");
				String value = (String)customField.get("value");
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				//find the lat/lon field
				if(key.equalsIgnoreCase("geo_address") || key.equalsIgnoreCase("geo_accuracy")) {
				
					 customField.remove("key");
					 customField.remove("value");
					 Log.debug("Removed custom field : "+ key);
				}
				
				if(key.equalsIgnoreCase("geo_longitude")){
					 Log.debug("Updated custom field : "+ key);
					 customField.put("value", String.valueOf(longitude));
					 longitudeFound = true;
				}
				if( key.equalsIgnoreCase("geo_latitude")){
					Log.debug("Updated custom field : "+ key);
					customField.put("value", String.valueOf(latitude));
					latitudeFound = true;
				}
				//geo_public
				if( key.equalsIgnoreCase("geo_public")){
					Log.debug("Updated custom field : "+ key);
					if(post.isLocationPublic())
						customField.put("value", String.valueOf(1));
					else
						customField.put("value", String.valueOf(0));
					locationPublicFound = true;
				}
				
			} catch(Exception ex) {
				Log.error("Error while Elaborating custom field # "+ i);
			}
		}
		
		if(longitudeFound == false)
		{
			Hashtable customField1 = new Hashtable();
			customField1.put("key", "geo_longitude");
			customField1.put("value", String.valueOf(longitude)); 
			customFields.addElement(customField1);
			Log.debug("Added custom field longitude");
		}
		
		if(latitudeFound == false)
		{
			Hashtable customField2 = new Hashtable();
			customField2.put("key", "geo_latitude"); 
			customField2.put("value", String.valueOf(latitude)); 
			customFields.addElement(customField2);
			Log.debug("Added custom field latitude");
		}
		
		//add geo_public field
		if(locationPublicFound == false)
		{
			Hashtable customField3 = new Hashtable();
			customField3.put("key", "geo_public"); 
			if(post.isLocationPublic())
				customField3.put("value", String.valueOf(1));
			else
				customField3.put("value", String.valueOf(0));
			customFields.addElement(customField3);
			
			Log.debug("Added custom field geo_public");
		}
		
		Log.debug("<<< updateLocationCustomField ");
	}
	
	public void showView() {
		//unfolds hashtable of status
		Post post = getPostObj();
		Hashtable postStatusHash = post.getBlog().getPostStatusList();
		postStatusLabel= new String [0];
		postStatusKey = new String [0];
		
		if(postStatusHash != null) {
			postStatusLabel= new String [postStatusHash.size()+1]; 
			postStatusKey = new String [postStatusHash.size()+1];
	    	
	    	Enumeration elements = postStatusHash.keys();
	    	int i = 0;
	
	    	for (; elements.hasMoreElements(); ) {
				String key = (String) elements.nextElement();
				String value = (String) postStatusHash.get(key);
				postStatusLabel[i] = value; //label
				postStatusKey[i] = key;
				i++;
			}
			postStatusLabel[postStatusLabel.length-1]= LOCAL_DRAFT_LABEL;
			postStatusKey[postStatusLabel.length-1]= LOCAL_DRAFT_KEY;
			// end 
		}

		
		String[] draftPostPhotoList =  getPhotoList();

		this.view= new PostView(this, post);
		view.setNumberOfPhotosLabel(draftPostPhotoList.length);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
		
	public String[] getStatusLabels() {
		return postStatusLabel;
	}
	
	public String[] getStatusKeys() {
		return postStatusKey;
	}
		
	public int getPostStatusFieldIndex() {
		Post post = getPostObj();
		String status = post.getStatus();
		if(post.getStatus() != null )
		for (int i = 0; i < postStatusKey.length; i++) {
			String key = postStatusKey[i];
				
			if( key.equals(status) ) {
				return i;
			}
		}
		return postStatusLabel.length-1;
	}
		
	
	public String getPostCategoriesLabel() {
		Post post = getPostObj();
		//start with categories
		int[] selectedCategories = post.getCategories();
		Category[] blogCategories = post.getBlog().getCategories();
		
		Vector categoriesLabelVector;		
		if(selectedCategories != null && selectedCategories.length >0 ) {
			categoriesLabelVector  = new Vector(selectedCategories.length);
		
			for (int i = 0; i < blogCategories.length; i++) {
				Category category = blogCategories[i];
				
				if(selectedCategories != null) {
					for (int j = 0; j < selectedCategories.length; j++) {
						if(selectedCategories[j] == Integer.parseInt(category.getId()) ){
							categoriesLabelVector.addElement( category.getLabel());
							break;
						}
					}
				}
			}
		
		} else {
			//no category found. set a uncategorized string
			categoriesLabelVector  = new Vector();
			categoriesLabelVector.addElement(_resources.getString(WordPressResource.LABEL_NO_CATEGORY) );
		}

		//fill the cat string buffer
		StringBuffer categoriesLabel = new StringBuffer();
		for (int i = 0; i < categoriesLabelVector.size(); i++) {
			String catLabel = (String) categoriesLabelVector.elementAt(i);
			if(i == 0) {
				categoriesLabel.append(catLabel);
			} else {
				categoriesLabel.append(", "+catLabel);
			}
		}
		//end with cat
		
		return categoriesLabel.toString();
	}
	
	public void newCategory(String label, int parentCatID){	
		Post post = getPostObj();
		NewCategoryConn connection = new NewCategoryConn (post.getBlog().getXmlRpcUrl(), 
				Integer.parseInt(post.getBlog().getId()), post.getBlog().getUsername(),
				post.getBlog().getPassword(), label, parentCatID);
		
		connection.addObserver(new SendNewCatCallBack(label,parentCatID)); 
        
		connectionProgressView= new ConnectionInProgressView(
        		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
       
        connection.startConnWork(); //starts connection
        int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	public void setPostCategories(Category[] selectedCategories){
		Post post = getPostObj();
		//TODO: simply this methods.
		//first: find if there is any change in selected categories
		int[] postPrevCategories = post.getCategories();
		
		if (postPrevCategories == null) {
			if( selectedCategories.length > 0) {
				int[] selectedIDs = new int[selectedCategories.length];
				
				for (int i = 0; i < selectedCategories.length; i++) {
					Category category = selectedCategories[i];
					String catID = category.getId();
					selectedIDs[i]=Integer.parseInt(catID);
				}
				post.setCategories(selectedIDs);
				setObjectAsChanged(true);
				
			} else if (selectedCategories.length == 0) {
				return;
			} 
		} else
		if (postPrevCategories.length == 0 && selectedCategories.length == 0) {
			return;
		} else {

			int[] selectedIDs = new int[selectedCategories.length];
			for (int i = 0; i < selectedCategories.length; i++) {
				Category category = selectedCategories[i];
				String catID = category.getId();
				selectedIDs[i]=Integer.parseInt(catID);
			}
			
			if(selectedCategories.length != postPrevCategories.length) {
				post.setCategories(selectedIDs);
				setObjectAsChanged(true);
			} else {
				//find differences			
				for (int i = 0; i < selectedIDs.length; i++) {
					int indexSelectedCat = selectedIDs[i];
					boolean presence = false;
					for (int j = 0; j < postPrevCategories.length; j++) {
						if ( postPrevCategories[j] == indexSelectedCat ){
							presence = true;
							break;
						}
					}
					if(!presence)  {
						post.setCategories(selectedIDs);
						setObjectAsChanged(true);
						break; //exit second if	
					}
				}
			}
		}
		view.updateCategoriesField(); 	//refresh the label field that contains cats..
	}

	
	
	public void sendPostToBlog() {
		Post post = getPostObj();
		if(post.getStatus() == null || post.getStatus().equals(LOCAL_DRAFT_KEY)) {
		//	displayMessage(_resources.getString(WordPressResource.MESSAGE_LOCAL_DRAFT_NOT_SUBMIT));
		//	return;
			post.setStatus("publish");
		}
		
		//adding post connection
		BlogConn connection;
		
		String remoteStatus = post.getStatus();
		boolean publish=false;
		//if( remoteStatus.equalsIgnoreCase("private") || remoteStatus.equalsIgnoreCase("publish"))
		if(remoteStatus.equalsIgnoreCase("publish"))
			publish= true;
		
		if(post.getId() == null || post.getId().equalsIgnoreCase("-1")) { //new post
	           connection = new NewPostConn (post.getBlog().getXmlRpcUrl(), 
	        		post.getBlog().getUsername(),post.getBlog().getPassword(), post, publish);
		
		} else { //edit post
			 connection = new EditPostConn (post.getBlog().getXmlRpcUrl(), 
					 post.getBlog().getUsername(),post.getBlog().getPassword(), post, publish);
		}
				
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));
		
		sendTask = new SendToBlogTask(post, draftFolder, connection);
		sendTask.setProgressListener(new SubmitPostTaskListener());
		//push into the Runner
		WordPressCore.getInstance().getTasksRunner().enqueue(sendTask);
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
			sendTask.stop();
		}
	}
	
	//listener on send post to blog
	private class SubmitPostTaskListener implements TaskProgressListener {

		public void taskComplete(Object obj) {			

			//task  stopped previous
			if (sendTask.isStopped()) 
				return;  
			
			if(connectionProgressView != null)
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						connectionProgressView.close();
					}
				});
			
			if (!sendTask.isError()){			
				FrontController.getIstance().backAndRefreshView(true);
			}
			else {
				displayError(sendTask.getErrorMsg());				
			}
		}
		
		//listener for the adding blogs task
		public void taskUpdate(Object obj) {
		
		}	
	}
	
	//user save post as localdraft
	public void saveDraftPost() {
		try {
    	Post post = getPostObj();
		 draftFolder = DraftDAO.storePost(post, draftFolder);
		 setObjectAsChanged(false); //set the post as not modified because we have saved it.
		 //the changes over the clean state for the UI Fields will be done into view-> save-draft menu item
		 this.isDraft = true; //set as draft
		 backCmd();
		} catch (Exception e) {
			displayError(e,"Error while saving draft post!");
		}
	}
			
	public boolean dismissView() {
		Post post = getPostObj();
		if( isObjectChanged() ) {

			String quest=_resources.getString(WordPressResource.MESSAGE_INQUIRY_DIALOG_BOX);
	    	DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
	    	int choice=infoView.doModal();  
	    	
	    	if(Dialog.DISCARD == choice) {

	    		try {
	    			if( !isDraft ){ //not remove post if it is a draft post
	    				DraftDAO.removePost(post.getBlog(), draftFolder);
	    			}
				} catch (Exception e) {
					displayError(e, "Cannot remove temporary files from disk!");
				}
	    		FrontController.getIstance().backAndRefreshView(false);
	    		return true;
	    		
	    	} else if(Dialog.SAVE == choice) {
	    		saveDraftPost();
	    		return true;
	    	} else {
	    		Log.trace("user has selected Cancel");
	    		return false;
	    	}
		}
		
		try {
			if( !isDraft ){ //not previous draft saved post
				DraftDAO.removePost(post.getBlog(), draftFolder);
			}
		} catch (Exception e) {
			displayError(e, "Cannot remove temporary files from disk!");
		}
		
		FrontController.getIstance().backAndRefreshView(false);		
		return true;
	}
	
	
	public void setAuthDate(long authoredOn) {
		Post post = getPostObj();
		if(post.getAuthoredOn() != null ) {
			if ( post.getAuthoredOn().getTime() != authoredOn ) {
				post.setAuthoredOn(authoredOn);
				setObjectAsChanged(true);
			}
		} else {
			post.setAuthoredOn(authoredOn);
			setObjectAsChanged(true);
		}
	}
	
	public void setPassword(String password) {
		Post post = getPostObj();
		if( post.getPassword() != null && !post.getPassword().equalsIgnoreCase(password) ){
			post.setPassword(password);
			setObjectAsChanged(true);
		} else {
			if(post.getPassword() == null ){
				post.setPassword(password);
				setObjectAsChanged(true);
			}
		}
	}
	
	public void setPhotoResizing(boolean isPhotoRes, Integer imageResizeWidth, Integer imageResizeHeight) {

		Log.trace("Entering setPhotoResizing. imageResizeWidth is " + imageResizeWidth.toString());
		Post post = getPostObj();
		if( post.getIsPhotoResizing() != null && !post.getIsPhotoResizing().booleanValue()== isPhotoRes ){
			post.setIsPhotoResizing(new Boolean(isPhotoRes));
			setObjectAsChanged(true);
		} else {
			if(post.getIsPhotoResizing() == null ){
				post.setIsPhotoResizing(new Boolean(isPhotoRes));
				setObjectAsChanged(true);
			}
		}

		if(post.getImageResizeWidth() != null && !post.getImageResizeWidth().equals(imageResizeWidth)) {
			post.setImageResizeWidth(imageResizeWidth);
			setObjectAsChanged(true);
		} else {
			if(post.getImageResizeWidth() == null) {
				post.setImageResizeWidth(imageResizeWidth);
				setObjectAsChanged(true);
			}
		}
		
		if(post.getImageResizeHeight() != null && !post.getImageResizeHeight().equals(imageResizeHeight)) {
			post.setImageResizeHeight(imageResizeHeight);
			setObjectAsChanged(true);
		} else {
			if(post.getImageResizeHeight() == null) {
				post.setImageResizeHeight(imageResizeHeight);
				setObjectAsChanged(true);
			}
		}	
	}

	 	
	public void showComments() {
		Post post = getPostObj();
		if(post.getId() == null || post.getId().equals("")) {
			displayMessage(_resources.getString(WordPressResource.MESSAGE_LOCAL_DRAFT_NO_COMMENT));
			return;
		}
		else {
			FrontController.getIstance().showCommentsByPost(post.getBlog(), Integer.parseInt(post.getId()), post.getTitle());
		}
	}
	
	public void showExcerptView(String title){		
		Post post = getPostObj();
		ExcerptView excerptView= new ExcerptView(this, post, title);
		UiApplication.getUiApplication().pushScreen(excerptView);
	}
	
	
	public void showCategoriesView(){
		Post post = getPostObj();
		catView= new PostCategoriesView(this, post.getBlog().getCategories(), post.getCategories());
		UiApplication.getUiApplication().pushScreen(catView);
	}
	
	
	public void showNewCategoriesView(){
		Post post = getPostObj();
		NewCategoryView newCatView= new NewCategoryView(this, post.getBlog().getCategories());		
		UiApplication.getUiApplication().pushScreen(newCatView);
	}
	
	public  void showSettingsView(){
		Post post = getPostObj();
		boolean isPhotoResing = blog.isResizePhotos(); //first set the value as the predefined blog value
		Integer imageResizeWidth = blog.getImageResizeWidth();
		Integer imageResizeHeight = blog.getImageResizeHeight();

		if (post.getIsPhotoResizing() != null ) {
			isPhotoResing = post.getIsPhotoResizing().booleanValue();			
		}
		if (post.getImageResizeWidth() != null ) {
			imageResizeWidth = post.getImageResizeWidth();
		}
		if (post.getImageResizeHeight() != null ) {
			imageResizeHeight = post.getImageResizeHeight();
		}
		
		//only for a new post show signature fields...
		if(post.getId() == null ) {
			//adding signature fields
		boolean isSignatureEnabled = blog.isSignatureEnabled();
		String  signature = blog.getSignature();
		if(signature == null) 
			signature = _resources.getString(WordPressResource.DEFAULT_SIGNATURE);
		
		if (post.isSignatureEnabled() != null ) {
			isSignatureEnabled = post.isSignatureEnabled().booleanValue();			
		}
		if (post.getSignature() != null ) {
			signature = post.getSignature();
		}
		
		settingsView = new PostSettingsView(this, post.getAuthoredOn(), post.getPassword(),
				isPhotoResing, imageResizeWidth, imageResizeHeight,
				isSignatureEnabled, signature);		
		} else {
			settingsView = new PostSettingsView(this, post.getAuthoredOn(), post.getPassword(),
					isPhotoResing, imageResizeWidth, imageResizeHeight);
		}
		
		UiApplication.getUiApplication().pushScreen(settingsView);
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
	
	//callback for send post to the blog
	private class SendNewCatCallBack implements Observer{
		private String label;
		private int parentCat=-1;
		
		SendNewCatCallBack(String label, int catId){
			this.label= label;
			this.parentCat=catId;
		}
		
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					Post post = getPostObj();
					
					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}

						//aggiorna le categorie del blog ed aggiorna la view...
						String intValue = String.valueOf(resp.getResponseObject());
						Blog blog = post.getBlog();
						Category[] categories = blog.getCategories();
						Category[] newCategories = new Category[categories.length+1];
						for (int i = 0; i < categories.length; i++) {
							newCategories[i]= categories[i];
						}
						Category newCat= new Category(String.valueOf(intValue), label);
						newCat.setParentCategory(parentCat);
						newCategories[categories.length] = newCat;
						
						blog.setCategories(newCategories);
						
						try {
							BlogDAO.updateBlog(blog);
						} catch (Exception e) {
							displayError(e, "Cannot update blog information on disk!");
						}              
						catView.refreshView(post.getBlog().getCategories(), post.getCategories());
						backCmd(); //return to catView
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}			
				}
			});
		}
	}


	public void setSignature(boolean isSignatureEnabled, String signature) {
		Post post = getPostObj();
		post.setSignatureEnabled(new Boolean(isSignatureEnabled));
		post.setSignature(signature);
	}
}
