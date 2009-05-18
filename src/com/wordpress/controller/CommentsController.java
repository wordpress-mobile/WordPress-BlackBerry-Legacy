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
import com.wordpress.xmlrpc.ManageCommentsTask;
import com.wordpress.xmlrpc.comment.DeleteCommentConn;
import com.wordpress.xmlrpc.comment.EditCommentConn;
import com.wordpress.xmlrpc.comment.GetCommentsConn;


public class CommentsController extends BaseController{
	
	private CommentsView view = null;
	private Blog currentBlog;
	ConnectionInProgressView connectionProgressView=null;
	 
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
		
		Comment[] myCommentsList = new Comment[0];

		if( comments != null )
			myCommentsList = getComments(comments);
			
		view= new CommentsView(this, myCommentsList);
		UiApplication.getUiApplication().pushScreen(view);

		//check the comments cache
		if( comments == null )
			startGetCommentsConn(false);
	}

	
	public void openComment(Comment comment){
		CommentView commentView= new CommentView(this, comment);
		UiApplication.getUiApplication().pushScreen(commentView);
	}
	
	public void updateComments(Comment[] comments, String status) {
		ManageCommentsTask task = new ManageCommentsTask();
		Preferences prefs = Preferences.getIstance();
		for (int i = 0; i < comments.length; i++) {
			Comment comment = comments[i];
			comment.setStatus(status);

			EditCommentConn conn = new EditCommentConn(currentBlog.getXmlRpcUrl(), currentBlog.getUsername(),
					currentBlog.getPassword(), prefs.getTimeZone(), currentBlog.getId(), comment);
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
				
				Vector storedComments = null;
				try {
					storedComments = CommentsDAO.loadComments(currentBlog);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
						
				//check the comments cache
				for (int i = 0; i < storedComments.size(); i++) {
					 Hashtable returnCommentData = (Hashtable)storedComments.elementAt(i);
					 int commentID=Integer.parseInt((String)returnCommentData.get("comment_id"));
					 
					 
					 for (int j = 0; j < comments.length; j++) {
						Comment	modifiedComment = comments[j];
						if (commentID == modifiedComment.getID()) {
							 returnCommentData.put("status", modifiedComment.getStatus());						
							break;
						}
					} 
					 
				}
								
				try {
					CommentsDAO.storeComments(currentBlog, storedComments);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //store the comments
				
				Comment[] myCommentsList = getComments(storedComments);
				
				view.refresh(myCommentsList, myCommentsList.length);
				
			} else {
				displayError(task.getErrorMessage());
			}
						
		}
	}
	
	public void deleteComments(Comment[] comments) {
		ManageCommentsTask task = new ManageCommentsTask();
		Preferences prefs = Preferences.getIstance();
		for (int i = 0; i < comments.length; i++) {
			Comment comment = comments[i];
			DeleteCommentConn conn = new DeleteCommentConn(currentBlog.getXmlRpcUrl(), currentBlog.getUsername(),
					currentBlog.getPassword(), prefs.getTimeZone(), currentBlog.getId(), comment.getID());
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
				
				Vector storedComments = null;
				try {
					storedComments = CommentsDAO.loadComments(currentBlog);
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
						
				//check the comments cache
				for (int i = 0; i < storedComments.size(); i++) {
					 Hashtable returnCommentData = (Hashtable)storedComments.elementAt(i);
					 int commentID=Integer.parseInt((String)returnCommentData.get("comment_id"));
					 
					 for (int j = 0; j < comments.length; j++) {
						Comment	modifiedComment = comments[j];
						if (commentID == modifiedComment.getID()) {
							storedComments.setElementAt(null, i);					
							break;
						}
					} 
				}
				
				//renew vector of comments
				Vector newCommentsVector = new Vector();
				for (int i = 0; i < storedComments.size(); i++) {
					 Hashtable returnCommentData = (Hashtable)storedComments.elementAt(i);
					 if(returnCommentData != null)
						 newCommentsVector.addElement(returnCommentData);
				}
				
				

				try {
					CommentsDAO.storeComments(currentBlog, newCommentsVector);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} //store the comments
				
				Comment[] myCommentsList = getComments(newCommentsVector);
				
				view.refresh(myCommentsList, myCommentsList.length);
				
			} else {
				displayError(task.getErrorMessage());
			}
			
			
		}
	}
	
	
	private void startGetCommentsConn(boolean refresh){

		Preferences prefs = Preferences.getIstance();
        
		String connMessage = null;
		if( !refresh ) 
			connMessage = _resources.getString(WordPressResource.CONN_LOADING_COMMENTS);
		else
			connMessage = _resources.getString(WordPressResource.CONN_REFRESH_COMMENTS);
		
		final GetCommentsConn connection = new GetCommentsConn(currentBlog.getXmlRpcUrl(), 
				Integer.parseInt(currentBlog.getId()), currentBlog.getUsername(), 
				currentBlog.getPassword(), prefs.getTimeZone(), -1, "", 0, 10);
		
        connection.addObserver(new loadCommentsCallBack(this));  
        connectionProgressView= new ConnectionInProgressView(connMessage);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}	
	}
	
	
	public void updateCommentsList(){
		startGetCommentsConn(true);
	}
	
	
	public void refreshView() {
				
	}
	
	//retun array of comments from wp response
	private Comment[] getComments(Vector respVector){
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
							
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
						
						Vector respVector = (Vector) resp.getResponseObject(); // the response from wp server
						
						try {
							CommentsDAO.storeComments(currentBlog, respVector);
						} catch (UnsupportedEncodingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} //store the comments

						Comment[] myCommentsList = getComments(respVector);
						
						view.refresh(myCommentsList, myCommentsList.length);
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}
				
				}
			});
		}
	}

}

