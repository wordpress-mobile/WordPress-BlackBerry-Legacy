package com.wordpress.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.DraftDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.view.DraftPostsView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class DraftPostsController extends BaseController {
	
	private DraftPostsView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private Blog currentBlog=null;
	private Hashtable loadedPost = null; //loaded post from storare
	private String[] loadedPostTitle = null; //shorcut to post  title
	private int[] loadedPostID = null; //shorcut to post ID
	
	public DraftPostsController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
	}

	public void showView() {
	    try {
	    	
	    	loadPostInfo();
	    		    	
		    this.view= new DraftPostsView(this,loadedPostTitle);
			UiApplication.getUiApplication().pushScreen(view);	    
		} catch (Exception e) {
	    	displayError(e, "Error while reading drafts phones memory");
		}
	}

	private void loadPostInfo() throws IOException, RecordStoreException {
		loadedPost = DraftDAO.getPostsInfo(currentBlog);
		loadedPostTitle = new String[0];
		loadedPostID = new int[0];
		
		if(loadedPost == null) {
		
		} else {
			Enumeration elements = loadedPost.keys();
			loadedPostTitle = new String[loadedPost.size()];
			loadedPostID = new int[loadedPost.size()];
			int i= 0 ;
			for (; elements.hasMoreElements();) {
				String key = (String) elements.nextElement();
				loadedPostID[i]  = Integer.parseInt(key);
				loadedPostTitle[i] = (String)loadedPost.get(key);
				i++;
			}
		}
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