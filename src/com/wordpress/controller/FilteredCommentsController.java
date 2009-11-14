package com.wordpress.controller;

import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.CommentsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.comment.GetCommentsConn;

public class FilteredCommentsController extends CommentsController{
	
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
				currentBlog.getPassword(), postID, postStatus, offset, number);
		
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

	//called on comments refresh
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
	
}


