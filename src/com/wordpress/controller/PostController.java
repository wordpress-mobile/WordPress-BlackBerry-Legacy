package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PostView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.post.EditPostConn;
import com.wordpress.xmlrpc.post.NewPostConn;


public class PostController extends BaseController implements Observer{
	
	private PostView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private Post post=null;
	private BlogController blogController= BlogController.getIstance();
	private int draftPostId=-1; //identify a draft post id
	
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
			FrontController.getIstance().backToMainView();	
		} else {
			final String respMessage=resp.getResponse();
		 	displayError(respMessage);	
		}
	}
		
	public  boolean dismissView() {
		if(view.getPostState().isModified()){
	    	int result=this.askQuestion("Changes Made, are sure to close this screen?");   
	    	if(Dialog.YES==result) {
	    		FrontController.getIstance().backToMainView();
	    		return true;
	    	} else {
	    		return false;
	    	}
		} else {
			FrontController.getIstance().backToMainView();
			return true;
		}
	}	
}