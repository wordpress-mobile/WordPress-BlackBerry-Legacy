package com.wordpress.controller;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Comment;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.CommentsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.comment.GetCommentsConn;

public class FilteredCommentsController extends CommentsController {
	
	protected String postTitle = null;
	
	public FilteredCommentsController(Blog currentBlog) {
		super(currentBlog);
	}
	
	public void setPostID(int postID) {
		this.postID = postID;
	}
	
	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}
	
	public String getFilterOnStatus() {
		return filterOnStatus;
	}

	public void setFilterOnStatus(String filterOnStatus) {
		this.filterOnStatus = filterOnStatus;
	}
	
	//true if we have set some filters (postID, offset, number, status)
	public boolean isFilterActive() {
		
		if (!filterOnStatus.equalsIgnoreCase(""))
			return true;

		if(this.postID != -1 )
			return true;
	
		return false;
	}
	
	public void showView() {
		view= new CommentsView(this, storedComments, gravatarController, currentBlog.getName());
		if (postTitle != null) {
			String viewTitle = _resources.getString(WordPressResource.TITLE_COMMENTS);
			viewTitle+= " > "+postTitle;
			view.setTitleText(viewTitle);
		}
		UiApplication.getUiApplication().pushScreen(view);
		loadComments();
	}

	private void loadComments() {
		String connMessage = _resources.getString(WordPressResource.CONN_LOADING_COMMENTS);
		
		final GetCommentsConn connection = new GetCommentsConn(currentBlog.getXmlRpcUrl(), 
				Integer.parseInt(currentBlog.getId()), currentBlog.getUsername(), 
				currentBlog.getPassword(), postID, filterOnStatus, offset, number);
		
        connection.addObserver(new LoadCommentsCallBack());  
        connectionProgressView= new ConnectionInProgressView(connMessage);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	
	protected void storeComment(Vector comments) {
	}
	protected void storeComment(Comment[] comments) {
	}
	
	public void cleanGravatarCache() {
		Vector emails = new Vector(); //email of the comment author
		int elementLength = storedComments.length;
		for(int count = 0; count < elementLength; ++count) {
			String authorEmail = storedComments[count].getAuthorEmail();
			if (!authorEmail.equalsIgnoreCase(""))
				emails.addElement(authorEmail);
		}		
		gravatarController.cleanGravatarCache(Tools.toStringArray(emails));
	}
	
	private class LoadCommentsCallBack extends RefreshCommentsCallBack {
		protected void removeGravatarCache() {
			  Log.debug(">>> no gvt cache cleaning on  Filtered comments loading");
			//no gvt cache cleaning on  Filtered comments loading
		}
	}

	protected void deleteFromMainCommentCache(Comment[] deletedComments) {
		Log.debug(">>> Filtered comments -> deleteFromMainCommentCache");
		Vector comments = null;
		try {
			comments = CommentsDAO.loadComments(currentBlog);
		} catch (IOException e) {
			Log.error(e, "Error while loading comments from memory");
		} catch (RecordStoreException e) {
			Log.error(e, "Error while loading comments from memory");
		}

		Comment[] mainCacheComments = (Comment[])CommentsDAO.vector2Comments(comments).get("comments");

		comments = null;
		
		Comment[] newComments = computeDifference(mainCacheComments, deletedComments);
		
		try{
			CommentsDAO.storeComments(currentBlog, newComments);
		} catch (IOException e) {
			Log.error(e, "Error while storing comments");
		} catch (RecordStoreException e) {
			Log.error(e, "Error while storing comments");
		} catch (Exception e) {
			Log.error(e, "Error while storing comments");
		} 
		Log.debug("<<< Filtered comments -> deleteFromMainCommentCache");
	}

	protected void updateMainCommentCache(Comment[] updatedComments) {
		Log.debug(">>> Filtered comments -> updateMainCommentCache");
		Vector comments = null;
		try {
			comments = CommentsDAO.loadComments(currentBlog);
		} catch (IOException e) {
			Log.error(e, "Error while loading comments from memory");
		} catch (RecordStoreException e) {
			Log.error(e, "Error while loading comments from memory");
		}

		Comment[] mainCacheComments = (Comment[])CommentsDAO.vector2Comments(comments).get("comments");
		comments = null;
		
		//update and storage the comments cache
		for (int i = 0; i < mainCacheComments.length; i++) {
			Comment	comment = mainCacheComments[i];
			for (int j = 0; j < updatedComments.length; j++) {
				Comment	modifiedComment = updatedComments[j];
				if (comment.getID() == modifiedComment.getID()) {
					mainCacheComments[i] = modifiedComment;
					break;
				}
			} 					 
		}
		
		try{
			CommentsDAO.storeComments(currentBlog, mainCacheComments);
		} catch (IOException e) {
			Log.error(e, "Error while storing comments");
		} catch (RecordStoreException e) {
			Log.error(e, "Error while storing comments");
		} catch (Exception e) {
			Log.error(e, "Error while storing comments");
		} 
		Log.debug("<<< Filtered comments -> updateMainCommentCache");
	}

	protected void addToMainCommentCache(Comment newComment) {
		Log.debug(">>> Filtered comments -> addToMainCommentCache");
		Vector comments = null;
		try {
			comments = CommentsDAO.loadComments(currentBlog);
		} catch (IOException e) {
			Log.error(e, "Error while loading comments from memory");
		} catch (RecordStoreException e) {
			Log.error(e, "Error while loading comments from memory");
		}

		Comment[] mainCacheComments = (Comment[])CommentsDAO.vector2Comments(comments).get("comments");
		Comment[] newComments = new Comment[mainCacheComments.length+1];
		
		newComments[0] = newComment; //the new comment on top of the list
		for (int i = 0; i < mainCacheComments.length; i++) { //copy all prev comment
			newComments[i+1] = mainCacheComments[i];
		}
		
		try{
			CommentsDAO.storeComments(currentBlog, newComments);
		} catch (IOException e) {
			Log.error(e, "Error while storing comments");
		} catch (RecordStoreException e) {
			Log.error(e, "Error while storing comments");
		} catch (Exception e) {
			Log.error(e, "Error while storing comments");
		} 
		Log.debug("<<< Filtered comments -> addToMainCommentCache");
	}
}