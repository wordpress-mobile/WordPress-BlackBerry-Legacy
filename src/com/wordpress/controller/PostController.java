//#preprocess
package com.wordpress.controller;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.location.AddressInfo;
import javax.microedition.location.Landmark;
import javax.microedition.location.Location;
import javax.microedition.location.LocationException;
import javax.microedition.location.QualifiedCoordinates;

import org.json.me.JSONArray;
import org.json.me.JSONObject;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.system.ControlledAccessException;

import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
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
import com.wordpress.view.component.SelectorPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.DiscardChangeInquiryView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.HTTPGetConn;
import com.wordpress.xmlrpc.NewCategoryConn;
import com.wordpress.xmlrpc.post.EditPostConn;
import com.wordpress.xmlrpc.post.NewPostConn;


//#ifdef IS_OS50_OR_ABOVE
import net.rim.device.api.lbs.picker.AbstractLocationPicker;
import net.rim.device.api.lbs.picker.LocationPicker;
import net.rim.device.api.lbs.picker.LocationPicker.Picker;
import com.wordpress.location.WPLocationPicker;
//#endif


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
			displayError(e, _resources.getString(WordPress.ERROR_NOT_ENOUGH_SPACE));
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
		
	public void startGeoTagging() {
		//#ifdef IS_OS50_OR_ABOVE
		try{
			WPLocationPicker locationPicker = new WPLocationPicker();
			locationPicker.setListener(new WPLocationPickerListener());
			locationPicker.show();
		} catch (ControlledAccessException permExp) {
			displayError("The application does not have PERMISSION_LOCATION_DATA permission set to Allow");
			return;
		} catch (Exception gpExp) {
			displayError("Location data is currently unavailable");
			return;
		}
		//#else
		Gps gps = new Gps();
		gps.addObserver(new GPSLocationCallBack());
		gps.findMyPosition();
		//#endif
	}
	
	//#ifdef IS_OS50_OR_ABOVE 
	private class WPLocationPickerListener implements LocationPicker.Listener {
	
		public void locationPicked(Picker picker, Landmark location) {
			if(location != null)
			{
				String address = location.getDescription();
				QualifiedCoordinates coordinates = location.getQualifiedCoordinates();
				AddressInfo ai = location.getAddressInfo();
				if(coordinates != null ) {
					double latitude = coordinates.getLatitude();
					double longitude = coordinates.getLongitude();
					updateLocationCustomField(address, latitude, longitude);
				}  else if (ai != null) {
					StringBuffer locationsearch = new StringBuffer();
					if (picker.getLocationPickerName().equals("From Contacts...") == false && location.getName() != null && location.getName().length() > 0) {
						locationsearch.append(location.getName());
					}
					if (ai.getField(AddressInfo.STREET) != null && ai.getField(AddressInfo.STREET).length() > 0) {
						if (locationsearch.length() > 0) {
							locationsearch.append(", ");
						}
						locationsearch.append(ai.getField(AddressInfo.STREET));
						Log.trace("Street: " + ai.getField(AddressInfo.STREET));
					}
					if (ai.getField(AddressInfo.CITY) != null && ai.getField(AddressInfo.CITY).length() > 0) {
						if (locationsearch.length() > 0) {
							locationsearch.append(", ");
						}
						locationsearch.append(ai.getField(AddressInfo.CITY));
						Log.trace("City: " + ai.getField(AddressInfo.CITY));
					}
					if (ai.getField(AddressInfo.STATE) != null && ai.getField(AddressInfo.STATE).length() > 0) {
						if (locationsearch.length() > 0) {
							locationsearch.append(", ");
						}
						locationsearch.append(ai.getField(AddressInfo.STATE));
						Log.trace("State: " + ai.getField(AddressInfo.STATE));
					}
					if (ai.getField(AddressInfo.POSTAL_CODE) != null && ai.getField(AddressInfo.POSTAL_CODE).length() > 0) {
						if (locationsearch.length() > 0) {
							locationsearch.append(", ");
						}
						locationsearch.append(ai.getField(AddressInfo.POSTAL_CODE));
						Log.trace("Zip code: " + ai.getField(AddressInfo.POSTAL_CODE));
					}
					if (ai.getField(AddressInfo.COUNTRY) != null && ai.getField(AddressInfo.COUNTRY).length() > 0) {
						if (locationsearch.length() > 0) {
							locationsearch.append(", ");
						}
						locationsearch.append(ai.getField(AddressInfo.COUNTRY));
					}
					Log.trace("Search String: " + locationsearch.toString());
					
					URLEncodedPostData urlenc  = new URLEncodedPostData(URLEncodedPostData.DEFAULT_CHARSET, true);
					urlenc.append("address", locationsearch.toString());
					urlenc.append("sensor", "false");
					
					Log.trace("Google geocoding service invoked : " + WordPressInfo.GOOGLE_GEOCODING_API_URL+"json?"+ urlenc.toString());
					final HTTPGetConn connection = new HTTPGetConn(WordPressInfo.GOOGLE_GEOCODING_API_URL+"json?"+ urlenc.toString(), "", "");
					
			        connection.addObserver(new GoogleGecodingServiceCallBack(connection));  
			        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_INPROGRESS));	       
			        connection.startConnWork(); //starts connection
							
					int choice = connectionProgressView.doModal();
					if(choice==Dialog.CANCEL) {
						connection.stopConnWork(); //stop the connection if the user click on cancel button
					}
					
				} else {
					displayError("Location data does not contain any valid information");
					return;
				}
			} 
		}
	}
	
	private class GoogleGecodingServiceCallBack implements Observer {

		private final HTTPGetConn connection;
		
		public GoogleGecodingServiceCallBack(final HTTPGetConn connection){
			this.connection = connection;
		}

		public void update(Observable observable, final Object object) {

			dismissDialog(connectionProgressView);
			BlogConnResponse resp = (BlogConnResponse) object;

			if(resp.isStopped()){
				return;
			}

			if (resp.isError()) {
				final String respMessage = resp.getResponse();
				displayError(respMessage);
				return;
			}

			try {
				Hashtable[] rv = null;
				byte[] response = (byte[]) resp.getResponseObject();
				
				
				if(response != null ) {

					if(Log.getDefaultLogLevel() >= Log.TRACE)
						Log.trace("RESPONSE - " + new String(response));

					Vector searchplace = new Vector();

					JSONObject jsonRespObj = new JSONObject(new String(response));
					
					String jsonResponseCode = jsonRespObj.getString("status");
					if(!jsonResponseCode.equalsIgnoreCase("OK")) {
						displayError(_resources.getString(WordPressResource.ERROR_GEOCODING_ADDRESS_NOT_FOUND));
						return;
					}
					

					JSONArray jsonResults = jsonRespObj.getJSONArray("results");
					for (int x = 0; x < jsonResults.length(); ++x) {
						JSONObject jsonPlace = jsonResults.getJSONObject(x);
						String name = jsonPlace.getString("formatted_address");
						JSONObject geometry  = jsonPlace.getJSONObject("geometry");
						JSONObject location =  geometry.getJSONObject("location");
						double lat = location.getDouble("lat");
						double lng = location.getDouble("lng");
						Hashtable result = new Hashtable();
						result.put("formatted_address", name);
						result.put("lat", new Double(lat));
						result.put("lng", new Double(lng));
						searchplace.addElement(result);
					}
					rv = new Hashtable[searchplace.size()];
					
					searchplace.copyInto(rv);
				}

				if(rv == null || rv.length==0) {
					displayError(_resources.getString(WordPressResource.ERROR_GEOCODING_ADDRESS_NOT_FOUND));
				} else {
					final String[] names = new String[rv.length];
					for (int i = 0; i < rv.length; i++) {
						Hashtable result = rv[i];
						names[i] = (String)result.get("formatted_address");
					}
					int selection = -1;
					final SelectorPopupScreen selScr = new SelectorPopupScreen("Search Results", names);

					UiApplication.getUiApplication().invokeAndWait(new Runnable() {
						public void run() {
							selScr.pickItem();
						}
					});

					selection = selScr.getSelectedItem();
					Log.trace("Selected index: " + selection);
					if(selection != -1) {
						Hashtable selectedLocationData =  rv[selection];
						double latitude = ((Double)selectedLocationData.get("lat")).doubleValue();
						double longitude = ((Double)selectedLocationData.get("lng")).doubleValue();
						String addr = ((String)selectedLocationData.get("formatted_address"));
						updateLocationCustomField(addr, latitude, longitude);
					}
					
				}
			} catch (Exception e) {
				displayError("Error while geocoding: "+e.getMessage());
				return;
			}						
		}
	} 
	//#else
	private class GPSLocationCallBack implements Observer {

		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					if(object == null) {
					
					} else if(object instanceof Location) {

						//javax.microedition.location.Location
						Location location = (Location) object;
						double latitude = location.getQualifiedCoordinates().getLatitude();
						double longitude = location.getQualifiedCoordinates().getLongitude();
						updateLocationCustomField(null, latitude, longitude);
						
					} else if(object instanceof LocationException) {
						displayError((LocationException)object, "GPS Error");
					}
				}//end run 
			});
		}
	}
	//#endif


	/**
	 * Add or upgrade the location custom tags
	 * 
	 */
	protected void updateLocationCustomField(String address, double latitude, double longitude) {
		
		Log.debug(">>> updateLocationCustomField ");
		Post post = getPostObj();
		Vector customFields = post.getCustomFields();
		int size = customFields.size();
    	Log.debug("Found "+size +" custom fields");
    	boolean addressFound = false;
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
				
				if(key == null) continue;
				
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				if(key.equalsIgnoreCase("geo_accuracy")) {
					//not used field
					 customField.remove("key");
					 customField.remove("value");
					 Log.debug("Removed custom field : "+ key);
				}				
				if(key.equalsIgnoreCase("geo_address")){
					 Log.debug("Updated custom field : "+ key);
					 customField.put("value", (address == null ? "" : address));
					 addressFound = true;
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
		
		if(addressFound == false && address != null)
		{
			Hashtable customField1 = new Hashtable();
			customField1.put("key", "geo_address");
			customField1.put("value", address); 
			customFields.addElement(customField1);
			Log.debug("Added custom field geo_address");
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
		
		//add the show location link to the view
		view.showMapLink(true);
		
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
		if(status != null )
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
		if(blog.isHTTPBasicAuthRequired()) {
			connection.setHttp401Password(blog.getHTTPAuthPassword());
			connection.setHttp401Username(blog.getHTTPAuthUsername());
		}
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
					Log.error(e, "Cannot remove temporary files from disk!");
					displayErrorAndWait("Cannot remove temporary files from disk!");
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
			displayErrorAndWait(e, "Cannot remove temporary files from disk!");
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
	

	public void showComments() {
		Post post = getPostObj();
		if(post.getId() == null || post.getId().equals("")) {
			displayMessage(_resources.getString(WordPressResource.MESSAGE_LOCAL_DRAFT_NO_COMMENT));
			return;
		}
		else {
			FrontController.getIstance().showCommentsByPost(post.getBlog(), post.getId(), post.getTitle());
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
		boolean isPhotoResing = blog.isResizePhotos();
		Integer imageResizeWidth = blog.getImageResizeWidth();
		Integer imageResizeHeight = blog.getImageResizeHeight();

		if (post.isPhotoResizing() != null ) {
			isPhotoResing = post.isPhotoResizing().booleanValue();			
		}
		if (post.getImageResizeWidth() != null ) {
			imageResizeWidth = post.getImageResizeWidth();
		}
		if (post.getImageResizeHeight() != null ) {
			imageResizeHeight = post.getImageResizeHeight();
		}
		
		boolean isVideoResing = blog.isResizeVideos();
		Integer videoResizeWidth = blog.getVideoResizeWidth();
		Integer videoResizeHeight = blog.getVideoResizeHeight();
		
		if (post.isVideoResizing() != null ) {
			isVideoResing = post.isVideoResizing().booleanValue();			
		}
		if (post.getVideoResizeWidth() != null ) {
			videoResizeWidth = post.getVideoResizeWidth();
		}
		if (post.getVideoResizeHeight() != null ) {
			videoResizeHeight = post.getVideoResizeHeight();
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
				isVideoResing, videoResizeWidth, videoResizeHeight,
				isSignatureEnabled, signature);		
		} else {
			settingsView = new PostSettingsView(this, post.getAuthoredOn(), post.getPassword(),
					isPhotoResing, imageResizeWidth, imageResizeHeight,
					isVideoResing, videoResizeWidth, videoResizeHeight);
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
	
	protected String getTheSignaturePreview() {
		Post post = getPostObj();
		boolean needSig;
		String signature;
		
		if(post.isSignatureEnabled() != null) {
			Log.trace("post signature settings found!");
			needSig = post.isSignatureEnabled().booleanValue();
			signature = post.getSignature();
		} else {
			Log.trace("not found post signature settings, reading the signature settings from blog setting");
			//read the value from blog
			needSig = blog.isSignatureEnabled();
			signature = blog.getSignature();
			if(needSig &&  signature == null) {
				signature = _resources.getString(WordPressResource.DEFAULT_SIGNATURE);
				}
		}
		
		if(needSig && signature != null) {
			Log.trace("adding signature to the post preview");
			return 	 "<p>"+signature+"</p>"; 
		}

		return "";
	}
	
}
