package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Comment;
import com.wordpress.task.CommentsTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.Queue;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.CommentReplyView;
import com.wordpress.view.CommentView;
import com.wordpress.view.CommentsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.comment.DeleteCommentConn;
import com.wordpress.xmlrpc.comment.EditCommentConn;
import com.wordpress.xmlrpc.comment.GetCommentsConn;
import com.wordpress.xmlrpc.comment.NewCommentConn;

public class RecentCommentsController extends BaseController {
	
	protected CommentsView view = null;
	protected Blog currentBlog;
	protected int postID = -1; //no post id
	protected int offset = 0;
	protected int number = WordPressInfo.DEFAULT_DOWNLOADED_COMMENTS;
	protected String statusFilter = ""; //no post status
	protected GravatarController gravatarController;
	protected CommentsTask task;
	
	protected ConnectionInProgressView connectionProgressView=null;
	protected Comment[] commentList = new Comment[0];
	protected String postTitle = null;
	
	public RecentCommentsController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
		this.gravatarController = new GravatarController(currentBlog);
	}
	
	public void showView() {
		
		if (isFilterActive()) {
			//when a filter is applied to the view, comments are loaded from the server and not from cache
			view= new CommentsView(this, commentList, gravatarController, currentBlog.getName());
			if (postTitle != null) {
				String viewTitle = _resources.getString(WordPressResource.TITLE_COMMENTS);
				viewTitle+= " > "+postTitle;
				view.setTitleText(viewTitle);
			}
			UiApplication.getUiApplication().pushScreen(view);

			String connMessage = _resources.getString(WordPressResource.CONN_LOADING_COMMENTS);
			
			final GetCommentsConn connection = new GetCommentsConn(currentBlog.getXmlRpcUrl(), 
					Integer.parseInt(currentBlog.getId()), currentBlog.getUsername(), 
					currentBlog.getPassword(), postID, statusFilter, offset, number);
			
	        connection.addObserver(new RefreshCommentsCallBack());  
	        connectionProgressView= new ConnectionInProgressView(connMessage);
	        connection.startConnWork(); //starts connection
			int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				connection.stopConnWork(); //stop the connection if the user click on cancel button
			}
		} else {
			loadCommentsFromCache();
			view = new CommentsView(this, commentList, gravatarController, currentBlog.getName());
			UiApplication.getUiApplication().pushScreen(view);
		}
	}

	public Comment[] getCommentList() {
		return commentList;
	}
	
	public void resetViewToAllComments() {
		setStatusFilter("");
		loadCommentsFromCache();
		view.refresh(commentList);
	}
	
	//load comments from the main device cache
	private void loadCommentsFromCache() {
		Vector comments = null;
		try {
			comments = CommentsDAO.loadComments(currentBlog);
			Hashtable vector2Comments = CommentsDAO.vector2Comments(comments);
			commentList =(Comment[]) vector2Comments.get("comments");
			if(vector2Comments.get("error") != null) {
				displayError("Error while loading comments: "+ (String)vector2Comments.get("error"));
			}
		} catch (IOException e) {
			displayError(e, "Error while loading comments from memory");
		} catch (RecordStoreException e) {
			displayError(e, "Error while loading comments from memory");
		}
	}
		
	public void showCommentView(Comment comment) {	
		CommentView commentView= new CommentView(this, comment, currentBlog.getCommentStatusList(), gravatarController);
		UiApplication.getUiApplication().pushScreen(commentView);
	}
	
	public void showReplyView(Comment comment) {	
		CommentReplyView commentView= new CommentReplyView(currentBlog, this, comment, gravatarController);
		UiApplication.getUiApplication().pushScreen(commentView);
	}
	
	public void reply2Comment(Comment comment) {
		
		final NewCommentConn connection = new NewCommentConn(currentBlog.getXmlRpcUrl(), 
				 currentBlog.getUsername(),	currentBlog.getPassword(), currentBlog.getId(), comment);
		
        connection.addObserver(new ReplyCommentCallBack());  
        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	public void updateComments(Comment[] comments, String status, String commentContent) {
		Queue connectionsQueue = new Queue();		
		boolean isModifiedComments = false; //true if there are comments that needs update
		
		for (int i = 0; i < comments.length; i++) {

			Comment comment = comments[i];
			boolean flag = false;
			
			if(commentContent != null && !commentContent.equalsIgnoreCase(comment.getContent()))
				flag = true;
				
			if (!comment.getStatus().equals(status))
				flag = true;
			
			if (flag) {
				comment.setStatus(status);
				if(commentContent != null)
					comment.setContent(commentContent);
	
				EditCommentConn conn = new EditCommentConn(currentBlog.getXmlRpcUrl(), currentBlog.getUsername(),
						currentBlog.getPassword(), currentBlog.getId(), comment);
				connectionsQueue.push(conn);
				isModifiedComments = true;
			}
		}
		
		if(!isModifiedComments) return; //there aren't modified comments
		
		task = new CommentsTask(connectionsQueue);
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));
		task.setProgressListener(new ModifyCommentTaskListener(comments));
		//push into the Runner
		WordPressCore.getInstance().getTasksRunner().enqueue(task);
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			task.stop();
		}
	}
	
	public void deleteComments(Comment[] deleteComments) {
		Queue connectionsQueue = new Queue();
		for (int i = 0; i < deleteComments.length; i++) {
			Comment comment = deleteComments[i];
			DeleteCommentConn conn = new DeleteCommentConn(currentBlog.getXmlRpcUrl(), currentBlog.getUsername(),
					currentBlog.getPassword(), currentBlog.getId(), comment.getID());
			connectionsQueue.push(conn);
		}
		
		task = new CommentsTask(connectionsQueue);
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));
		task.setProgressListener(new DeleteCommentTaskListener(deleteComments));
		//push into the Runner
		WordPressCore.getInstance().getTasksRunner().enqueue(task);
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
			task.stop();
		}
	}
	
	public void refreshComments() {
		String connMessage = _resources.getString(WordPressResource.CONN_REFRESH_COMMENTS);
		
		final GetCommentsConn connection = new GetCommentsConn(currentBlog.getXmlRpcUrl(), 
				Integer.parseInt(currentBlog.getId()), currentBlog.getUsername(), 
				currentBlog.getPassword(), postID, statusFilter, offset, number);
		
        connection.addObserver(new RefreshCommentsCallBack());  
        connectionProgressView= new ConnectionInProgressView(connMessage);
       
        connection.startConnWork(); //starts connection
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	public void loadMoreComments() {
		String connMessage = _resources.getString(WordPressResource.CONN_REFRESH_COMMENTS);
		
		final GetCommentsConn connection = new GetCommentsConn(currentBlog.getXmlRpcUrl(), 
				Integer.parseInt(currentBlog.getId()), currentBlog.getUsername(), 
				currentBlog.getPassword(), postID, statusFilter, commentList.length, number);
		
        connection.addObserver(new LoadMoreCommentsCallBack());  
        connectionProgressView= new ConnectionInProgressView(connMessage);
       
        connection.startConnWork(); //starts connection
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
		
	public void refreshView() {
				
	}
		
	protected Comment[] computeDifference(Comment[] originalComments, Comment[] deleteComments) {
		Vector newComments = new Vector();
		//update and storage the comments cache
		for (int i = 0; i < originalComments.length; i++) {
			Comment	comment = originalComments[i];
			boolean presence = false;
			for (int j = 0; j < deleteComments.length; j++) {
				Comment	modifiedComment = deleteComments[j];
				if (comment.getID() == modifiedComment.getID()) {
					presence = true;
					break;
				}
			}
			if(!presence) {
				newComments.addElement(comment);
			}
		}
		
		Comment[] differences = new Comment[newComments.size()];
		newComments.copyInto(differences);
		return differences;
	}
	
	public void setPostID(int postID) {
		this.postID = postID;
	}
	
	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}
		
	//true if we have set some filters (postID, offset, number, status)
	public boolean isFilterActive() {
		
		if (!statusFilter.equalsIgnoreCase(""))
			return true;

		if(this.postID != -1 )
			return true;
	
		return false;
	}
	
	public void setStatusFilter(String statusFilter) {
		this.statusFilter = statusFilter;
	}
	
	public String getStatusFilter() {
		return this.statusFilter;
	}

	protected void storeComment(Comment[] comments) {
		if(isFilterActive()) return;
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
	
	protected void updateMainCommentCache(Comment[] updatedComments) {
		if(isFilterActive()) {
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
		} else {
			Log.debug("Main Comment Cache is already updated");
		}
	}

	protected void addToMainCommentCache(Comment newComment) {
		if(isFilterActive()) {
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
		} else {
			//not used, in this case we are using the main cache directly
			Log.debug("Main Comment Cache is already updated");
		}
	}
	

	private class DeleteCommentTaskListener implements TaskProgressListener {
		private Comment[] deleteComments;
		private boolean notified = false;

		public DeleteCommentTaskListener(Comment[] comments) {
			super();
			this.deleteComments = comments;
		}

		public void taskComplete(Object obj) {
			if(notified == true)
				return;
			else
				notified = true;
			
			dismissDialog(connectionProgressView);
			
			if (task.isError()) {
				displayError(task.getErrorMsg());
				return;
			}
				
			//remove the deleted comments from the memory comments list
			commentList = computeDifference(commentList, deleteComments);
			if(!isFilterActive()) {
				//update the comments device's cache
				storeComment(commentList);
			} else {
				//When any filter is active we should delete comments selectively from main cache...
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
				Comment[] diffComments = computeDifference(mainCacheComments, deleteComments);
				try{
					CommentsDAO.storeComments(currentBlog, diffComments);
				} catch (IOException e) {
					Log.error(e, "Error while storing comments");
				} catch (RecordStoreException e) {
					Log.error(e, "Error while storing comments");
				} catch (Exception e) {
					Log.error(e, "Error while storing comments");
				} 
				Log.debug("<<< Filtered comments -> deleteFromMainCommentCache");
			}

			if(connectionProgressView != null)
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						view.refresh(commentList);
					}
				});		 
		}
		
		public void taskUpdate(Object obj) {
		
		}	
	}

	private class ModifyCommentTaskListener implements TaskProgressListener {
		private boolean notified = false;
		private Comment[] comments;
		
		public ModifyCommentTaskListener(Comment[] comments) {
			super();
			this.comments = comments;
		}
		
		public void taskComplete(Object obj) {		
			if(notified == true)
				return;
			else
				notified = true;
			
			dismissDialog(connectionProgressView);
			
			if (!task.isError()) {
				//update and storage the comments cache
				for (int i = 0; i < commentList.length; i++) {
					Comment	comment = commentList[i];
					for (int j = 0; j < comments.length; j++) {
						Comment	modifiedComment = comments[j];
						if (comment.getID() == modifiedComment.getID()) {
							commentList[i] = modifiedComment;
							break;
						}
					} 					 
				}
				
				if(isFilterActive()) {
					//When any filter is active we should update comments selectively from main cache...
					updateMainCommentCache(comments);
				} else {
					Log.debug("No Filters found -> store comments cache");
					storeComment(commentList);
				}
				
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {						
						view.refresh(commentList);
					}
				});
				
			} else {
				displayError(task.getErrorMsg());
			}
		}
		
		//listener for the adding blogs task
		public void taskUpdate(Object obj) {
			
		}	
	}
	
	private class RefreshCommentsCallBack implements Observer {

		public void update(Observable observable, final Object object) {

			dismissDialog(connectionProgressView);
			BlogConnResponse resp= (BlogConnResponse) object;
			Vector respVector= null;

			if(resp.isStopped()){
				return;
			}

			if(resp.isError()) {
				final String respMessage=resp.getResponse();
				displayError(respMessage);
				return;
			}

			respVector = (Vector) resp.getResponseObject(); // the response from wp server

			if(!isFilterActive()) {
				try{
					CommentsDAO.storeComments(currentBlog, respVector);
				} catch (IOException e) {
					displayError(e, "Error while storing comments");
				} catch (RecordStoreException e) {
					displayError(e, "Error while storing comments");
				} catch (Exception e) {
					displayError(e, "Error while storing comments");
				} 
			}

			Hashtable vector2Comments = CommentsDAO.vector2Comments(respVector);
			commentList =(Comment[]) vector2Comments.get("comments");
			if(vector2Comments.get("error") != null) {
				displayErrorAndWait("Error while loading comments: "+ (String)vector2Comments.get("error"));
			}

			//delete the gravatar cache
			if(isFilterActive()) {
				Vector emails = new Vector(); //email of the comment author
				int elementLength = commentList.length;
				for(int count = 0; count < elementLength; ++count) {
					String authorEmail = commentList[count].getAuthorEmail();
					if (!authorEmail.equalsIgnoreCase(""))
						emails.addElement(authorEmail);
				}		
				gravatarController.cleanGravatarCache(Tools.toStringArray(emails));
			} else {
				gravatarController.cleanGravatarCache(); //on recent comment without filter we can reset the cache totally
			}

			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					view.refresh(commentList);
				}
			});
		}
	}
	
	//callback for post reply
	private class ReplyCommentCallBack implements Observer {

		public void update(Observable observable, final Object object) {

			dismissDialog(connectionProgressView);
			BlogConnResponse resp= (BlogConnResponse) object;

			if(resp.isStopped()){
				return;
			}
			if(!resp.isError()) {

				try {
					Hashtable newComm = (Hashtable) resp.getResponseObject(); // the response from wp server. (struct)
					Vector tempCommVector = new Vector(1);
					tempCommVector.addElement(newComm);

					Comment[] vector2Comments = (Comment[])CommentsDAO.vector2Comments(tempCommVector).get("comments");

					tempCommVector = null;
					//adding new comment at top of the comments list
					Comment[] newComments = new Comment[commentList.length+1];
					newComments[0] = vector2Comments[0]; //the new comment on top of the list
					for (int i = 0; i < commentList.length; i++) { //copy all prev comment
						newComments[i+1] = commentList[i];
					}
					commentList = newComments;

					if(isFilterActive()) {
						//When any filter is active we should add comments selectively in the main cache...
						addToMainCommentCache(vector2Comments[0]);
					} else {
						Log.debug("No Filters found -> store comments cache");
						storeComment(commentList);
					}
				}
				catch (Exception e) {
					displayError(e, "Error while loading server response");
				} 

				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						view.refresh(commentList);						
					}
				});
				backCmd();
				backCmd();

			} else {
				final String respMessage=resp.getResponse();
				displayError(respMessage);	
			}
		}
	}
	
	private class LoadMoreCommentsCallBack implements Observer {

		public void update(Observable observable, final Object object) {

			dismissDialog(connectionProgressView);
			BlogConnResponse resp= (BlogConnResponse) object;
			Vector respVector= null;

			if(resp.isStopped()){
				return;
			}

			if(resp.isError()) {
				final String respMessage=resp.getResponse();
				displayError(respMessage);
				return;
			}

			respVector = (Vector) resp.getResponseObject(); // the response from wp server
	
			Hashtable vector2Comments = CommentsDAO.vector2Comments(respVector);
			if(vector2Comments.get("error") != null) {
				displayErrorAndWait("Error while loading comments: "+ (String)vector2Comments.get("error"));
			}
			Comment[] newDownloadedComment = (Comment[]) vector2Comments.get("comments");
			
			if(newDownloadedComment.length == 0)
				return;
			
			//remove duplicates from the comment list. When duplicates we are keeping the new downloaded comment istance
			commentList = computeDifference(commentList, newDownloadedComment);
			Comment[] tmpCommentList = new Comment[commentList.length+newDownloadedComment.length];
			int k = 0;
			for (int i = 0; i < commentList.length; i++) {
				tmpCommentList[k] = commentList[i];
				k++;
			}
			for (int i = 0; i < newDownloadedComment.length; i++) {
				tmpCommentList[k] = newDownloadedComment[i];
				k++;
			}
			commentList = tmpCommentList;
			
			if(!isFilterActive()) {
				try{
					CommentsDAO.storeComments(currentBlog, commentList);
				} catch (IOException e) {
					displayError(e, "Error while storing comments");
				} catch (RecordStoreException e) {
					displayError(e, "Error while storing comments");
				} catch (Exception e) {
					displayError(e, "Error while storing comments");
				} 
			}
			
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					view.refresh(commentList);
				}
			});
			
		}
	}
}