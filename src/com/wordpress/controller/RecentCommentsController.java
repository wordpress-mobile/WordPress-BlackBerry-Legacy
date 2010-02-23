package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Comment;
import com.wordpress.utils.log.Log;
import com.wordpress.view.CommentsView;

public class RecentCommentsController extends CommentsController {
	
	public RecentCommentsController(Blog currentBlog) {
		super(currentBlog);
	}
	
	public void showView() {
		Vector comments = null;
		try {
			comments = CommentsDAO.loadComments(currentBlog);
			Hashtable vector2Comments = CommentsDAO.vector2Comments(comments);
			storedComments =(Comment[]) vector2Comments.get("comments");
			if(vector2Comments.get("error") != null) {
				displayError("Error while loading comments: "+ (String)vector2Comments.get("error"));
			}
		} catch (IOException e) {
			displayError(e, "Error while loading comments from memory");
		} catch (RecordStoreException e) {
			displayError(e, "Error while loading comments from memory");
		}

		
		view = new CommentsView(this, storedComments, gravatarController, currentBlog.getName());
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	protected void storeComment(Vector comments) {	
		try{
			CommentsDAO.storeComments(currentBlog, comments);
		} catch (IOException e) {
			displayError(e, "Error while storing comments");
		} catch (RecordStoreException e) {
			displayError(e, "Error while storing comments");
		} catch (Exception e) {
			displayError(e, "Error while storing comments");
		} 
	}
	
	protected void storeComment(Comment[] comments) {	
		try{
			CommentsDAO.storeComments(currentBlog, comments);
		} catch (IOException e) {
			displayError(e, "Error while storing comments");
		} catch (RecordStoreException e) {
			displayError(e, "Error while storing comments");
		} catch (Exception e) {
			displayError(e, "Error while storing comments");
		} 
	}
	
	
	//called on comments refresh
	public void cleanGravatarCache() {
		gravatarController.cleanGravatarCache(); //on recent comment without filter we can reset the cache totally
	}

	protected void deleteFromMainCommentCache(Comment[] comments) {
		//not used, in this case we are using the main cache directly
		 Log.debug(">>> no deleteFromMainCommentCache for recent comment");
	}

	protected void updateMainCommentCache(Comment[] comments) {
		Log.debug(">>> no updateMainCommentCache for recent comment");
	}

	protected void addToMainCommentCache(Comment newComment) {
		Log.debug(">>> no addToMainCommentCache for recent comment");
	}
	

/*	

	public void showPendingComments(){
		setFilterOnStatus("hold");
		refreshComments();
	}
	
	public void showAllComments(){
		setFilterOnStatus("");
		Screen scr=UiApplication.getUiApplication().getActiveScreen();
		UiApplication.getUiApplication().popScreen(scr);
		showView();			
	}
	*/
}