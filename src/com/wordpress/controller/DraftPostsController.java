package com.wordpress.controller;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.view.DraftPostsView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class DraftPostsController extends BaseController {
	
	private DraftPostsView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private BlogIOController blogController= BlogIOController.getIstance();

	private Blog currentBlog=null;
	private Object[] mPosts = null;
	
	
	public DraftPostsController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
	}


	public String getCurrentBlogName() {
		return currentBlog.getBlogName();
	}

	
	public void deletePost(int selected){
		int result=this.askQuestion("Delete selected post?");   
    	if(Dialog.YES==result) {
    		if(selected != -1){
    			int id = ((Integer) mPosts[selected * 2]).intValue();
    	        try {
    				blogController.removeDraftPost(currentBlog, id);
    			} catch (Exception e) {
    				displayError(e, "Error while deleting draft post data");
    			}
    			refreshUI();
    		}	    		
    	} else {
    	
    	}
	}

	/** starts the  post loading */
	public void editPost(int selected){
		if(selected != -1){
			
            Post post = (Post) mPosts[1 + (selected * 2)];
            int id = ((Integer) mPosts[selected * 2]).intValue();
            try {
				blogController.updateDraftPost(post, currentBlog, id);
			} catch (Exception e) {
				displayError(e, "Error while loading draft post data");
			}
			FrontController.getIstance().showDraftPost(post,id);       
		}	     	
	}	
	
	private void refreshUI() {
		try {
			String [] postCaricati = getPostsTitle();
			if(postCaricati == null) return;
			view.refresh(postCaricati);
		} catch (RecordStoreException e) {
			displayError(e, "Error while reading rms");
		} catch (IOException e) {
			displayError(e, "Error while reading from phones memory");
		}
	}
	
	public void showView() {
	    try {
	    	String [] postCaricati = getPostsTitle();
			if(postCaricati == null) return;
		    this.view= new DraftPostsView(this,postCaricati);
			UiApplication.getUiApplication().pushScreen(view);
	    } catch (RecordStoreException e) {
	    	displayError(e, "Error while reading rms");
		} catch (IOException e) {
	    	displayError(e, "Error while reading from phones memory");
		}
	}


	private String[] getPostsTitle() throws RecordStoreException, IOException {
		if(currentBlog== null) return null;
		
		mPosts = BlogIOController.getIstance().getDraftPostList(currentBlog);
		Vector draftPostTitles= new Vector();
		String title="";

		for (int i = 1; i < mPosts.length; i += 2) {
		    title = ((Post) mPosts[i]).getTitle();
		    if (title == null || title.length() == 0) {
		    	title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
		    }
		    draftPostTitles.addElement(title);
		}
		
		final String[] postCaricati= new String[draftPostTitles.size()];
		draftPostTitles.copyInto(postCaricati);
		return postCaricati;
	}

}