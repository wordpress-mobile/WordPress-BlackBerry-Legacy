package com.wordpress.controller;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.DraftPostsView;
import com.wordpress.view.RecentPostsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.post.DeletePostConn;
import com.wordpress.xmlrpc.post.RecentPostConn;


public class DraftPostsController extends BaseController {
	
	private DraftPostsView view = null;
	ConnectionInProgressView connectionProgressView=null;

	private Blog currentBlog=null;
	 private Object[] mPosts = null;
	
	
	public DraftPostsController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
	}


	public String getCurrentBlogName() {
		return currentBlog.getBlogName();
	}

	
	public void deletePost(int postID){
		
	}

	public void showView() {
	    try {

	    	if(currentBlog== null) return;
	    	mPosts = BlogController.getIstance().getDraftPostList(currentBlog);
	    	
			final String[] postCaricati= new String[mPosts.length];
			String title="";
	
			mPosts = BlogController.getIstance().getDraftPostList(currentBlog);
	
		    for (int i = 1; i < mPosts.length; i += 2) {
		        title = ((Post) mPosts[i]).getTitle();
		        if (title == null || title.length() == 0) {
		        	title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
		        }
		        postCaricati[i]=title;
		    }
		    
		    this.view= new DraftPostsView(this,postCaricati);
			UiApplication.getUiApplication().pushScreen(view);

	    } catch (RecordStoreException e) {
	    	displayError(e, "Error while reading rms");
		} catch (IOException e) {
	    	displayError(e, "Error while reading from phones memory");
		}
	}

}