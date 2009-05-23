package com.wordpress.controller;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Comment;
import com.wordpress.model.Preferences;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.CommentView;
import com.wordpress.view.CommentsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.comment.DeleteCommentConn;
import com.wordpress.xmlrpc.comment.EditCommentConn;
import com.wordpress.xmlrpc.comment.GetCommentsConn;
import com.wordpress.xmlrpc.comment.ManageCommentsTask;


public class CommentsController extends BaseController{
	
	private CommentsView view = null;
	private Blog currentBlog;
	ConnectionInProgressView connectionProgressView=null;
	private Comment[] storedComments;
		

	public CommentsController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
	}
				
	public void showView() {

		Vector comments = null;
		try {
			comments = CommentsDAO.loadComments(currentBlog);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace(); //TODO err
		} catch (Exception e) {
			e.printStackTrace(); //TODO err
		}
		storedComments = buildCommentsArray(comments);		
		view= new CommentsView(this, storedComments);
		UiApplication.getUiApplication().pushScreen(view);
	
	}


	public int getCommentsCount() {
		if(storedComments == null) return 0;
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
		CommentView commentView= new CommentView(this, comment);
		UiApplication.getUiApplication().pushScreen(commentView);
	}
	
	public void updateComments(Comment[] comments, String status, String commentContent) {
		ManageCommentsTask task = new ManageCommentsTask();
		Preferences prefs = Preferences.getIstance();

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
				task.addConn(conn);
				isModifiedComments = true;
			}
		}
		
		if(!isModifiedComments) return; //there aren't modified comments
		
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));

		task.setDialog(connectionProgressView);
		task.startWorker(); //start sending post
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			task.quit();
			
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
								
				try {
					CommentsDAO.storeComments(currentBlog, buildCommentVector(storedComments));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //store the comments
				view.refresh(storedComments);
				
			} else {
				displayError(task.getErrorMessage());
			}
						
		}
	}
	
	public void deleteComments(Comment[] deleteComments) {
		ManageCommentsTask task = new ManageCommentsTask();
		Preferences prefs = Preferences.getIstance();
		for (int i = 0; i < deleteComments.length; i++) {
			Comment comment = deleteComments[i];
			DeleteCommentConn conn = new DeleteCommentConn(currentBlog.getXmlRpcUrl(), currentBlog.getUsername(),
					currentBlog.getPassword(), currentBlog.getId(), comment.getID());
			task.addConn(conn);
		}
		
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING));

		task.setDialog(connectionProgressView);
		task.startWorker(); //start sending post
		
		int choice = connectionProgressView.doModal();
		if(choice == Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			task.quit();
			
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
							
				try {
					CommentsDAO.storeComments(currentBlog, buildCommentVector(storedComments));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //store the comments
				
							
				view.refresh(storedComments);
				
			} else {
				displayError(task.getErrorMessage());
			}
			
			
		}
	}
	
	
	public void refreshComments(boolean refresh){

		Preferences prefs = Preferences.getIstance();
        
		String connMessage = null;
		if( !refresh ) 
			connMessage = _resources.getString(WordPressResource.CONN_LOADING_COMMENTS);
		else
			connMessage = _resources.getString(WordPressResource.CONN_REFRESH_COMMENTS);
		
		final GetCommentsConn connection = new GetCommentsConn(currentBlog.getXmlRpcUrl(), 
				Integer.parseInt(currentBlog.getId()), currentBlog.getUsername(), 
				currentBlog.getPassword(), -1, "", 0, 100);
		
        connection.addObserver(new loadCommentsCallBack(this));  
        connectionProgressView= new ConnectionInProgressView(connMessage);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}	
	}
	
	
	
	public void refreshView() {
				
	}
	

	
	//callback for post loading
	private class loadCommentsCallBack implements Observer {
		
		private CommentsController ctrl;
		
		public loadCommentsCallBack(CommentsController ctrl) {
			this.ctrl= ctrl;
		}
		
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					Vector respVector= null;
					
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
						
						respVector = (Vector) resp.getResponseObject(); // the response from wp server
						
						try {
							CommentsDAO.storeComments(currentBlog, respVector);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} //store the comments

						storedComments = buildCommentsArray(respVector);
						
						Comment[] myCommentsList = buildCommentsArray(respVector);
						
						view.refresh(myCommentsList);
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}
				
				}
			});
		}
	}
	
	//retun array of comments from wp response
	private Comment[] buildCommentsArray(Vector respVector){
		
		if( respVector == null )
			return new Comment[0];
		
		Comment[] myCommentsList =new Comment[respVector.size()]; //my comment object list
		
		for (int i = 0; i < respVector.size(); i++) {
			 Hashtable returnCommentData = (Hashtable)respVector.elementAt(i);
			 
			
			Comment comment = new Comment();
			 
			int commentID=Integer.parseInt((String)returnCommentData.get("comment_id"));
	        int commentParent=Integer.parseInt((String) returnCommentData.get("parent"));
            String status=(String) returnCommentData.get("status");
            comment.setDate_created_gmt((Date) returnCommentData.get("date_created_gmt"));
            comment.setUserId( (String)returnCommentData.get("user_id") ) ;
            comment.setID(commentID);
            comment.setParent(commentParent);
            comment.setStatus(status);
            comment.setContent( (String) returnCommentData.get("content") );
            comment.setLink((String) returnCommentData.get("link"));
            comment.setPostID(Integer.parseInt((String)returnCommentData.get("post_id")));
            comment.setPostTitle((String) returnCommentData.get("post_title"));
            comment.setAuthor((String) returnCommentData.get("author"));
            comment.setAuthorEmail((String) returnCommentData.get("author_email"));
            comment.setAuthorUrl((String) returnCommentData.get("author_url"));
            comment.setAuthorIp((String) returnCommentData.get("author_ip"));
            myCommentsList[i]=comment; //add comment to my return list

		}
		return myCommentsList;
	}
	
	private Vector buildCommentVector(Comment[] comments){
		Vector commentsVector= new Vector();
		if( comments == null )
			return commentsVector;
		
		for (int i = 0; i < comments.length; i++) {
			Comment currentComment = comments[i];
	        Hashtable hash = new Hashtable(13);
	        hash.put("comment_id", String.valueOf(currentComment.getID()));
	        hash.put("parent", String.valueOf(currentComment.getParent()));
	        hash.put("status", currentComment.getStatus());
	        hash.put("date_created_gmt", currentComment.getDate_created_gmt());
	        hash.put("user_id", currentComment.getUserId());
	        hash.put("content", currentComment.getContent());
	        hash.put("link", currentComment.getLink());
	        hash.put("post_id", String.valueOf(currentComment.getPostID()));
	        hash.put("author", currentComment.getAuthor());
	        hash.put("post_title", currentComment.getPostTitle());
	        hash.put("author_email", currentComment.getAuthorEmail());
	        hash.put("author_url", currentComment.getAuthorUrl());
	        hash.put("author_ip", currentComment.getAuthorIp());
	        commentsVector.addElement(hash);        
		}
		return commentsVector;
	}
	

}

