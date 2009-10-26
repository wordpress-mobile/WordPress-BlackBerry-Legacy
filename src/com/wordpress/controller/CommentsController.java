package com.wordpress.controller;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Comment;
import com.wordpress.task.CommentsTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.CommentReplyView;
import com.wordpress.view.CommentView;
import com.wordpress.view.CommentsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.InfoView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.comment.DeleteCommentConn;
import com.wordpress.xmlrpc.comment.EditCommentConn;
import com.wordpress.xmlrpc.comment.GetCommentsConn;
import com.wordpress.xmlrpc.comment.NewCommentConn;


public abstract class CommentsController extends BaseController {
	
	protected CommentsView view = null;
	protected Blog currentBlog;
	protected int postID = -1; //no post id
	protected int offset = 0;
	protected int number = 100;
	protected String postStatus = ""; //no post status
	protected GravatarController gravatarController;
	protected CommentsTask task;
	
	protected ConnectionInProgressView connectionProgressView=null;
	protected Comment[] storedComments = new Comment[0];

	public CommentsController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
		this.gravatarController = new GravatarController(currentBlog);
	}
				
	public int getCommentsCount() {
		if(storedComments == null) 
			return 0;
		else 
			return storedComments.length;
	}
	
	public int  getCommentIndex(Comment currentComment) {
		int index = -1;
		for (int i = 0; i < storedComments.length; i++) {
			Comment	comment = storedComments[i];
				if (comment.getID() == currentComment.getID()) {
					index = i;
					break;
				}
		}
		return index+1;
	}
	
	public Comment getPreviousComment(Comment currentComment) {
		int index = -1;
		for (int i = 0; i < storedComments.length; i++) {
			Comment	comment = storedComments[i];
				if (comment.getID() == currentComment.getID()) {
					index = i;
					break;
				}
		}

		if(storedComments.length > index+1) {
			return storedComments[index+1];
		} else
		
		return null;
	}
	
	/**
	 * 	
	 * @param currentComment
	 * @return the next comment from the comments list.
	 */
	public Comment getNextComment(Comment currentComment){		
		int index = -1;
		for (int i = 0; i < storedComments.length; i++) {
			Comment	comment = storedComments[i];
				if (comment.getID() == currentComment.getID()) {
					index = i;
					break;
				}
		}
		//index = 0 mean that currentComment is the most recent comment
		if(index > 0) {
			return storedComments[index-1];
		} else
		
		return null;				
	}
	
	public void openComment(Comment comment) {	
		CommentView commentView= new CommentView(this, comment, currentBlog.getCommentStatusList(), gravatarController);
		UiApplication.getUiApplication().pushScreen(commentView);
	}
	

	
	//show a preview of the post/page of the comment
	public void showPostOrPage(Comment comment) {
	/*	String postID = String.valueOf( comment.getPostID() );
		
		final GetPostConn connection = new GetPostConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(), currentBlog, postID);
        
        connection.addObserver(new loadPostCallBack());  
        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_LOADING_POST));
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
		*/
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
	
	
	public void refreshComments(boolean refresh) {

		String connMessage = null;
		if( !refresh ) 
			connMessage = _resources.getString(WordPressResource.CONN_LOADING_COMMENTS);
		else
			connMessage = _resources.getString(WordPressResource.CONN_REFRESH_COMMENTS);
		
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
	
	public void refreshView() {
				
	}
	
	
	protected abstract void storeComment(Vector comments);
	

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
			
			if(connectionProgressView != null)
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						connectionProgressView.close();
					}
				});
			
			if (!task.isError()) {
				//delete comments from the list
				//count the deleted comments 
				int numOfdelete=deleteComments.length;
				//create new comment array
				Comment[] newComments = new Comment[storedComments.length - numOfdelete];
				
				//update and storage the comments cache
				int k = 0;
				for (int i = 0; i < storedComments.length; i++) {
					Comment	comment = storedComments[i];
					
					boolean presence = false;
					for (int j = 0; j < deleteComments.length; j++) {
						Comment	modifiedComment = deleteComments[j];
						if (comment.getID() == modifiedComment.getID()) {
							presence = true;
							break;
						}
					}
					
					if(!presence) {
						newComments[k] = comment;
						k++;
					}
					
				}
				
				storedComments = newComments;
				storeComment(CommentsDAO.comments2Vector(storedComments));

				if(connectionProgressView != null)
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							view.refresh(storedComments);
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
			
			if(connectionProgressView != null)
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						connectionProgressView.close();
					}
				});
			
			if (!task.isError()) {
				
				//update and storage the comments cache
				for (int i = 0; i < storedComments.length; i++) {
					Comment	comment = storedComments[i];
					for (int j = 0; j < comments.length; j++) {
						Comment	modifiedComment = comments[j];
						if (comment.getID() == modifiedComment.getID()) {
							storedComments[i] = modifiedComment;
							break;
						}
					} 					 
				}
				
				storeComment(CommentsDAO.comments2Vector(storedComments));
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						view.refresh(storedComments);
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
	
	//callback for post loading
	private class LoadCommentsCallBack implements Observer {
				
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					Vector respVector= null;
					
					if(resp.isStopped()){
						return;
					}
					if(!resp.isError()) {
						
						respVector = (Vector) resp.getResponseObject(); // the response from wp server
						
						storeComment(respVector);
						storedComments = CommentsDAO.vector2Comments(respVector);
						
						gravatarController.cleanGravatarCache();
						view.refresh(storedComments);
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}
				
				}
			});
		}
	}
	
	
	
	//callback for post reply
	private class ReplyCommentCallBack implements Observer {
		
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
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
							Comment[] vector2Comments = CommentsDAO.vector2Comments(tempCommVector);
							tempCommVector = null;
							//adding new comment at top of the comments list
							Comment[] newComments = new Comment[storedComments.length+1];
							newComments[0] = vector2Comments[0]; //the new comment on top of the list
							for (int i = 0; i < storedComments.length; i++) { //copy all prev comment
								newComments[i+1] = storedComments[i];
							}
							storedComments = newComments;
							
							storeComment(CommentsDAO.comments2Vector(storedComments));
						}
						catch (Exception e) {
							displayError(e, "Error while loading server response");
						} 
						
						
						view.refresh(storedComments);						
						backCmd();
						backCmd();
						
						
					} else {
						final String respMessage=resp.getResponse();
						displayError(respMessage);	
					}
					
				}
			});
		}
	}
}