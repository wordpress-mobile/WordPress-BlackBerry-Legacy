package com.wordpress.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.DraftDAO;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.model.Post;
import com.wordpress.view.DraftPostsView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class DraftPostsController extends BaseController {
	
	private DraftPostsView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private Blog currentBlog=null;
	private String[] loadedPostTitle = null; //shorcut to post  title
	private int[] loadedPostID = null; //shorcut to post ID
	private boolean isLoadError= false;
	private String loadErrorMessage= "";
	
	
	public DraftPostsController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
	}

	public void showView() {
	    try {
	    	
	    	loadPostInfo();
	    		    	
		    this.view= new DraftPostsView(this,loadedPostTitle);
			UiApplication.getUiApplication().pushScreen(view);
			
			if(isLoadError) {
				displayError(loadErrorMessage);
			}
			
		} catch (Exception e) {
	    	displayError(e, "Error while reading drafts phones memory");
		}
	}

	private void loadPostInfo() throws IOException, RecordStoreException {
		//if we can't load file name index, we exit immediately		
		String[] loadedPostIndex = null;
		try {
			loadedPostIndex = DraftDAO.getPostsInfo(currentBlog);
		} catch (Exception e) {
			isLoadError = true;
			loadErrorMessage = "Could not load draft pages index from disk";
			return;
		}
		
		//try to read data from storage.
		Vector vectorPostTitle = new Vector();
		Vector vectorPostFileName = new Vector();
	    
    	for (int i = 0; i < loadedPostIndex.length; i++) {
    		try{
	    		String currPostFile = (String)loadedPostIndex[i];    		
	    		Post loadDraftPost = DraftDAO.loadPost(currentBlog, Integer.parseInt(currPostFile));
	    		String title = loadDraftPost.getTitle();
			    if (title == null || title.length() == 0) {
			    	title = "No title";
			    }
			    
			    vectorPostTitle.addElement(title);
			    vectorPostFileName.addElement(currPostFile);
    		} catch (Exception e) {
				isLoadError = true;
				loadErrorMessage = "Could not load some pages from disk";
			}
		}
    	
    	
		loadedPostTitle = new String[vectorPostTitle.size()];
		loadedPostID = new int[vectorPostTitle.size()];
		
		for (int i = 0; i < vectorPostTitle.size(); i++) {
			loadedPostID[i] = Integer.parseInt((String)vectorPostFileName.elementAt(i));	
		}
		vectorPostTitle.copyInto(loadedPostTitle);
	}


	public String getCurrentBlogName() {
		return currentBlog.getName();
	}

	
	public void deletePost(int selected){
		int draftPostID = loadedPostID[selected];
		try {
			DraftDAO.removePost(currentBlog, draftPostID);
			refreshView();
		} catch (IOException e) {
	    	displayError(e, "Error while deleteing draft post");
		} catch (RecordStoreException e) {
			displayError(e, "Error while deleteing draft post");
		}
	}
	
	
	public void newPost() {
		if (currentBlog != null) {
			FrontController.getIstance().newPost(currentBlog); // show the new post view
		}
	}
	
	/** starts the post loading */
	public void editPost(int selected) {
		try {
			if (selected != -1) {
				int draftPostID = loadedPostID[selected];
				Post post = DraftDAO.loadPost(currentBlog, draftPostID);
				FrontController.getIstance().showDraftPost(post, draftPostID);
			}
		} catch (Exception e) {
			displayError(e, "Error while loading draft post");
		}
	}	
	

	public void refreshView() {
		 try {
			loadPostInfo();
			view.refresh(loadedPostTitle);
		} catch (Exception e) {
	    	displayError(e, "Error while reading drafts phones memory");
		}
	}
}