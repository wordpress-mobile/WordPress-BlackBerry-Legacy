package com.wordpress.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.DraftDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Blog;
import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.model.Preferences;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.NewCategoryView;
import com.wordpress.view.PhotosView;
import com.wordpress.view.PostCategoriesView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.PostView;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.mm.MultimediaPopupScreen;
import com.wordpress.view.mm.PhotoPreview;
import com.wordpress.view.mm.PhotoSnapShotView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.NewCategoryConn;
import com.wordpress.xmlrpc.NewMediaObjectConn;
import com.wordpress.xmlrpc.post.EditPostConn;
import com.wordpress.xmlrpc.post.NewPostConn;
import com.wordpress.xmlrpc.post.SendPostDataTask;


public class PostController extends BlogObjectController {
	
	private PostView view = null;
	private PhotosView photoView = null;
	private PostCategoriesView catView = null;
	private PostSettingsView settingsView = null;
	private Post post=null;
	private int draftPostFolder=-1; //identify draft post folder
	private boolean isDraft= false; // identify if post is loaded from draft folder
    private boolean isModified = false; //the state of post. track changes on post..
	
	private String[] postStatusKey; // = {"draft, pending, private, publish, localdraft"};
	private String[] postStatusLabel; 

	
	Preferences prefs = Preferences.getIstance();
	
	//used when new post/recent post
	// 0 = new post
	// 1 = edit recent post
	public PostController(Post post) {
		super();	
		this.post=post;
		//assign new space on draft folder, used for photo IO
		try {
			draftPostFolder = DraftDAO.storePost(post, draftPostFolder);
		} catch (Exception e) {
			displayError(e, "Cannot create space on disk for your post!");
		}
	}
	
	//used when loading draft post from disk
	public PostController(Post post,int _draftPostFolder) {
		super();	
		this.post=post;
		this.draftPostFolder=_draftPostFolder;
		this.isDraft = true;
	}
	
	public void showView() {
		//unfolds hashtable of status
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

		
		String[] draftPostPhotoList = new String[0];
		try {
			draftPostPhotoList = DraftDAO.getPostPhotoList(post.getBlog(), draftPostFolder);
		} catch (Exception e) {
			displayError(e, "Cannot load photos of this post!");
		}
		this.view= new PostView(this, post);
		view.setNumberOfPhotosLabel(draftPostPhotoList.length);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	
	public void setPostAsChanged(boolean value) {
		isModified = value;
	}
	
	public boolean isPostChanged() {
		return isModified;
	}
	
	public String[] getStatusLabels() {
		return postStatusLabel;
	}
	
	public String[] getStatusKeys() {
		return postStatusKey;
	}
		
	public int getPostStatusFieldIndex() {
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
		Category[] availableCategories = post.getBlog().getCategories();
		
		Vector categoryLabels = new Vector();
		int[] postCategories = post.getCategories();
		
		if (postCategories != null && availableCategories != null) {
            for (int i = 0; i < postCategories.length; i++) {
            	int idCatPost = postCategories[i];
            	
            	for (int j = 0; j < availableCategories.length; j++) {
            		Category category = availableCategories[j];
            		String idString = category.getId();
            		int idCat = Integer.parseInt(idString);
            		if( idCatPost == idCat ) categoryLabels.addElement(category.getLabel());
				
            	}
            }
		} 
		
		if(categoryLabels.size() == 0 ){
			String emptyCatLabel = _resources.getString(WordPressResource.LABEL_OPTIONAL);
			return emptyCatLabel;
		} else {
			//trim...
			String firstCat= (String)categoryLabels.elementAt(0);
			firstCat+= " ...";
			return firstCat;
		}
	}
	
	public void newCategory(String label, int parentCatID){	
		NewCategoryConn connection = new NewCategoryConn (post.getBlog().getXmlRpcUrl(), 
				Integer.parseInt(post.getBlog().getId()), post.getBlog().getUsername(),
				post.getBlog().getPassword(), label, parentCatID);
		
		connection.addObserver(new SendNewCatCallBack(label,parentCatID)); 
        
		connectionProgressView= new ConnectionInProgressView(
        		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
       
        connection.startConnWork(); //starts connection
        int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	public void setPostCategories(Vector newCatID){
			
		if ( newCatID == null || newCatID.size() == 0 ){
			post.setCategories(null);
		} else {
			int[] selectedID = new int[newCatID.size()];
			
			for (int i = 0; i < newCatID.size(); i++) {
				String elementAt = (String)newCatID.elementAt(i);
				selectedID[i]=Integer.parseInt(elementAt);
			}
			post.setCategories(selectedID);
		}
		
		view.updateCategoriesField(); 	//refresh the label field that contains cats..
		setPostAsChanged(true);
	}

	public void sendPostToBlog() {
		
		if (!isPostChanged()) { //post without change
			return;
		}
		if(post.getStatus().equals(LOCAL_DRAFT_KEY)) {
			displayMessage("Local Draft post cannot be submitted");
			return;
		}	
		 
		String[] draftPostPhotoList;

		try {
			draftPostPhotoList = DraftDAO.getPostPhotoList(post.getBlog(), draftPostFolder);
		} catch (Exception e) {
			displayError(e, "Cannot load photos from disk, publication failed!");
			return;
		}
		
		SendPostDataTask sender = new SendPostDataTask (post, draftPostFolder);
		
		//adding multimedia connection
		if(draftPostPhotoList.length > 0 ) {
			String key="";
			for (int i =0; i < draftPostPhotoList.length; i++ ) {
				key = draftPostPhotoList[i];
				NewMediaObjectConn connection = new NewMediaObjectConn (post.getBlog().getXmlRpcUrl(), 
			       		   post.getBlog().getUsername(),post.getBlog().getPassword(), post.getBlog().getId(), key);				
				sender.addConn(connection);
			}
		}
		
		//adding post connection
		BlogConn connection;
		
		String remoteStatus = post.getStatus();
		boolean publish=false;
		if( remoteStatus.equalsIgnoreCase("private") || remoteStatus.equalsIgnoreCase("publish"))
			publish= true;
		
		if(post.getId() == null || post.getId().equalsIgnoreCase("-1")) { //new post
	           connection = new NewPostConn (post.getBlog().getXmlRpcUrl(), 
	        		post.getBlog().getUsername(),post.getBlog().getPassword(), post, publish);
		
		} else { //edit post
			 connection = new EditPostConn (post.getBlog().getXmlRpcUrl(), 
					 post.getBlog().getUsername(),post.getBlog().getPassword(), post, publish);
		
		}
		sender.addConn(connection);
				
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));

		sender.setDialog(connectionProgressView);
		sender.startWorker(); //start sending post
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			sender.quit();
			if (!sender.isError())
				FrontController.getIstance().backAndRefreshView(true);
			else
				displayError(sender.getErrorMessage());
		}
	}
	
	//user save post as localdraft
	public void saveDraftPost() {
		try {
		 draftPostFolder = DraftDAO.storePost(post, draftPostFolder);
		 setPostAsChanged(false); //set the post as not modified because we have saved it.
		 this.isDraft = true; //set as draft
		} catch (Exception e) {
			displayError(e,"Error while saving draft post!");
		}
	}
			
	public boolean dismissView() {
		
		if( isPostChanged() ) {
	    	int result=this.askQuestion("Changes Made, are sure to close this screen?");   
	    	if(Dialog.YES==result) {
	    		try {
	    			if( !isDraft ){ //not previous draft saved post
	    				DraftDAO.removePost(post.getBlog(), draftPostFolder);
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
				DraftDAO.removePost(post.getBlog(), draftPostFolder);
			}
		} catch (Exception e) {
			displayError(e, "Cannot remove temporary files from disk!");
		}
		
		FrontController.getIstance().backAndRefreshView(true);		
		return true;
	}
	
	
	
	public void setSettingsValues(long authoredOn, String password){
		
		if(post.getAuthoredOn() != null ) {
			if ( post.getAuthoredOn().getTime() != authoredOn ) {
				post.setAuthoredOn(authoredOn);
				setPostAsChanged(true);
			}
		} else {
			post.setAuthoredOn(authoredOn);
			setPostAsChanged(true);
		}
		
		if( post.getPassword() != null && !post.getPassword().equalsIgnoreCase(password) ){
			post.setPassword(password);
			setPostAsChanged(true);
		} else {
			if(post.getPassword()== null ){
				post.setPassword(password);
				setPostAsChanged(true);
			}
		}
	}
	
	public void showSettingsView(){			
		settingsView= new PostSettingsView(this, post.getAuthoredOn(), post.getPassword());		
		UiApplication.getUiApplication().pushScreen(settingsView);
	}
	 
	/*
	public void showPreview(){
		Preferences prefs = Preferences.getIstance();
		GetTemplateConn connection = new GetTemplateConn (post.getBlog().getXmlRpcUrl(), 
				Integer.parseInt(post.getBlog().getId()), post.getBlog().getUsername(),
				post.getBlog().getPassword());

		
		connection.addObserver(new SendGetTamplateCallBack()); 
        
		connectionProgressView= new ConnectionInProgressView(
        		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
       
        connection.startConnWork(); //starts connection
        int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
		
	}
	*/
	
	public void showCategoriesView(){			
		catView= new PostCategoriesView(this, post.getBlog().getCategories(), post.getCategories());
		UiApplication.getUiApplication().pushScreen(catView);
	}
	
	
	public void showNewCategoriesView(){			
		NewCategoryView newCatView= new NewCategoryView(this, post.getBlog().getCategories());		
		UiApplication.getUiApplication().pushScreen(newCatView);
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
			draftPostPhotoList = DraftDAO.getPostPhotoList(post.getBlog(), draftPostFolder);
			photoView= new PhotosView(this);
			for (int i = 0; i < draftPostPhotoList.length; i++) {
				String currPhotoPath = draftPostPhotoList[i];
				byte[] data=DraftDAO.loadPostPhoto(post, draftPostFolder, currPhotoPath);
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
			data = DraftDAO.loadPostPhoto(post, draftPostFolder, key);
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
			DraftDAO.removePostPhoto(post.getBlog(), draftPostFolder, key);
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
                // Dialog.alert("Screen was dismissed. No file was selected.");
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
	    System.out.println("addPhoto " + fileName);
	
	    if(fileName == null) 
			fileName= String.valueOf(System.currentTimeMillis()+".jpg");
		
		EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
				
		//check if blog has "photo resize option" selected
		if (post.getBlog().isResizePhotos()){
			EncodedImage rescaled= MultimediaUtils.bestFit2(img, 640, 480);
			img=rescaled;
		} 

		try {
			DraftDAO.storePostPhoto(post, draftPostFolder, data, fileName);
		} catch (Exception e) {
			displayError(e, "Cannot save photo to disk!");
		}
		
		photoView.addPhoto(fileName, img);
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
					
					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}

						//aggiorna le categorie del blog ed aggiorna la view...
						int intValue = ((Integer)resp.getResponseObject()).intValue();
						Blog blog = post.getBlog();
						Category[] categories = blog.getCategories();
						Category[] newCategories = new Category[categories.length+1];
						for (int i = 0; i < categories.length; i++) {
							newCategories[i]= categories[i];
						}
						Category newCat= new Category(String.valueOf(intValue), label);
						newCategories[categories.length] = newCat;
						
						blog.setCategories(newCategories);
						
						try {
							BlogDAO.updateBlog(blog);
						} catch (Exception e) {
							displayError(e, "Cannot update blog information on disk!");
						}              
						catView.addCategory(label, newCategories);
						catView.invalidate();
						backCmd(); //return to catView
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}			
				}
			});
		}
	}
}
