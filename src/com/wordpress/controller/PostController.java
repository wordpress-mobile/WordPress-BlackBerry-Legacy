package com.wordpress.controller;

import java.io.IOException;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Category;
import com.wordpress.model.MediaObject;
import com.wordpress.model.Post;
import com.wordpress.utils.FileUtils;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PhotosView;
import com.wordpress.view.PostView;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.component.MultimediaPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.post.EditPostConn;
import com.wordpress.xmlrpc.post.NewPostConn;


public class PostController extends BaseController implements Observer{
	
	private PostView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private Post post=null;
	private BlogIOController blogController= BlogIOController.getIstance();
	private int draftPostId=-1; //identify a draft post id
	
	public static final int PHOTO=1;
	public static final int BROWSER=4;
	
	
	//used when loading new post/recent post
	public PostController(Post post) {
		super();	
		this.post=post;
	}
	
	//used when loading draft post
	public PostController(Post post,int draftPostId) {
		this(post);
		this.draftPostId=draftPostId;
	}
	
	public void showView() {
		this.view= new PostView(this, post);
		UiApplication.getUiApplication().pushScreen(view);
	}
		
	public String[] getAvailableCategories(){
		Category[] availableCategories = post.getBlog().getCategories();
		String[] categoryLabels;
		if (availableCategories != null) {
            categoryLabels = new String[availableCategories.length];
            for (int i = 0; i < availableCategories.length; i++) {
                categoryLabels[i] = availableCategories[i].getLabel();
            }
            
		} else {
			categoryLabels= new String[0];
		}
		return categoryLabels;
	}
	
	//return the post category n.b:change it
	public int getPostCategoryIndex(){
		int primaryIndex = -1; 
		Category primaryCategory = post.getPrimaryCategory();
		if(primaryCategory == null) return primaryIndex;
		
		Category[] availableCategories = post.getBlog().getCategories();  
		if (availableCategories != null) {
            for (int i = 0; i < availableCategories.length; i++) {
                if (availableCategories[i].equals(primaryCategory)) {
                    primaryIndex = i;
                }
            }
		}
		return primaryIndex;
	}
	  
	public void sendPostToBlog() {
		final BlogConn connection;
		Preferences prefs = Preferences.getIstance();
		
		if(post.getId() == null || post.getId().equalsIgnoreCase("-1")) { //new post
	           connection = new NewPostConn (post.getBlog().getBlogXmlRpcUrl(), 
	        		post.getBlog().getUsername(),post.getBlog().getPassword(),prefs.getTimeZone(), post, view.getPostState().isPublished());
		} else { //edit post
			if (!view.getPostState().isModified()) { //post without change
				return;
			}
			 connection = new EditPostConn (post.getBlog().getBlogXmlRpcUrl(), 
					 post.getBlog().getUsername(),post.getBlog().getPassword(),prefs.getTimeZone(), post, view.getPostState().isPublished());
		}
		connection.addObserver(this); 
        connectionProgressView= new ConnectionInProgressView(
       		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
      
       connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}			
	}
	
	public void saveDraftPost() {
		try {
		 blogController.saveDraftPost(post, draftPostId);
		 view.getPostState().setModified(false); //set the post as saved
		} catch (Exception e) {
			displayError(e,"Error while saving draft post!");
		}
	}
	
	public void update(Observable observable, Object object) {
		dismissDialog(connectionProgressView);
		BlogConnResponse resp= (BlogConnResponse) object;
		if(!resp.isError()) {
			if(resp.isStopped()){
				return;
			}
			FrontController.getIstance().backAndRefreshView(true);
			//backCmd();
		} else {
			final String respMessage=resp.getResponse();
		 	displayError(respMessage);	
		}
	}
		
	public boolean dismissView() {
		if(view.getPostState().isModified()){
	    	int result=this.askQuestion("Changes Made, are sure to close this screen?");   
	    	if(Dialog.YES==result) {
	    		FrontController.getIstance().backAndRefreshView(false);
	    		//backCmd();
	    		return true;
	    	} else {
	    		return false;
	    	}
		} else {
			FrontController.getIstance().backAndRefreshView(false);
			//backCmd();
			return true;
		}
	}
	
	
	public void showPhotosView(){
		PhotosView scr= new PhotosView(this,post);
		UiApplication.getUiApplication().pushScreen(scr);
	}
	
	//* called by photoview */
	public void showMultimediaSelectionBox(){
		int response= BROWSER;
		
		//if(MultimediaUtils.supportPhotoCapture()) {
	    	MultimediaPopupScreen multimediaPopupScreen = new MultimediaPopupScreen();
	    	UiApplication.getUiApplication().pushModalScreen(multimediaPopupScreen); //modal screen...
			response = multimediaPopupScreen.getResponse();
		//}
		
		switch (response) {
		case BROWSER:
           	 String imageExtensions[] = {"jpg", "jpeg","bmp", "png", "gif"};
             FileSelectorPopupScreen fps = new FileSelectorPopupScreen(null, imageExtensions);
             fps.pickFile();
             String theFile = fps.getFile();
             if (theFile == null){
                 Dialog.alert("Screen was dismissed. No file was selected.");
             } else {
            	 String[] fileNameSplitted = StringUtils.split(theFile, ".");
            	 String ext= fileNameSplitted[fileNameSplitted.length-1];
         
            	 try {
					byte[] readFile = FileUtils.readFile(theFile);
					MediaObject mmObj= new MediaObject();
					mmObj.setContentType(ext); //setting the content type as the file extension
					mmObj.setMediaData(readFile);
					//sendMultimediaContent(mmObj, textField);
					displayMessage("File ok!");
										
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Error while sending file to blog");
				}

             }					
			break;
			
		case PHOTO:
			
			break;
			
		default:
			break;
		}
	
	
		
		
	}

	public void refreshView() {
	
	}

}