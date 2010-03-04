package com.wordpress.bb;

import java.io.IOException;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.media.control.VolumeControl;
import javax.microedition.rms.RecordStoreException;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.controller.MainController;
import com.wordpress.controller.NotificationController;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Comment;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.CommentReplyView;
import com.wordpress.view.CommentView;
import com.wordpress.view.CommentsView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.comment.GetCommentCountConn;
import com.wordpress.xmlrpc.comment.GetCommentsConn;

/**
 * Handles new message notification through the various
 * notification mechanisms of the BlackBerry.
 */
public class NotificationHandler {
	private static NotificationHandler instance = null;
	private boolean isNotificationEnabled;
	private NotificationTask currentNotificationTask = null;
	private NotificationDetailsTask currentDetailsTask = null;
	private MainController guiController = null;
	private Hashtable awaitingCommentsID = new Hashtable(); //key: blog.xmlrpcurl, value: int[] commentsID

	private NotificationHandler() {
	}
	
	/**
	 * Gets the NotificationHandler instance.
	 * 
	 * @return Single instance of NotificationHandler
	 */
	public static synchronized NotificationHandler getInstance() {
		if(instance == null) {
			instance = new NotificationHandler();
		}
		return instance;
	}

	/**
	 * set the gui screen that show notification of new messages
	 * @param guiController
	 */
	public void setGuiController(MainController guiController) {
		this.guiController = guiController;
	}
	
	/**
	 * Sets whether notifications are enabled.
	 * 
	 * @param isEnabled True to enable notifications, false to disable
	 */
	public void setEnabled(boolean isEnabled, int updateTimeIndex) {
		
		if(updateTimeIndex == 0) return;
		int updateInterval = NotificationController.decodeInterval(updateTimeIndex);
		Log.trace("updateInterval ms : " + updateInterval);
		if(updateInterval == 0) return;
		
		Log.trace("NotificationHandler enabled");
		this.isNotificationEnabled = isEnabled;
		stopInnerTask(); //it is not necessary here, anyway we have put one more checks
		currentNotificationTask = new NotificationTask();
		
		WordPressCore.getInstance().getTimer().schedule(currentNotificationTask, updateInterval, updateInterval); //1mins check
	}
	
	
	private void stopInnerTask() {
		awaitingCommentsID.clear(); //clean the hashtable that contains comments id
		if(currentNotificationTask != null) {
			currentNotificationTask.stopping = true;
			currentNotificationTask.cancel();
			currentNotificationTask = null;
		}
		if(currentDetailsTask != null) {
			currentDetailsTask.stopping = true;
			currentDetailsTask = null;
		}
	}

	/**
	 * Shutdown the listener and unsubscribe from any system events.
	 */
	public void shutdown() {
		Log.trace("NotificationHandler stopped");
		cancelNotification();
		stopInnerTask();
		isNotificationEnabled = false;
	}
	
	//start the task that gets the awaiting comments details
	private void getAwaitingDetails(){
		currentDetailsTask = new NotificationDetailsTask();
		currentDetailsTask.run();
	}
	
	private void notifyNewMessages() {
		long sourceId =  WordPressInfo.COMMENTS_UID;
		NotificationsManager.triggerImmediateEvent(sourceId, 0, this, null);
		setAppIcon(true);
	}

	/**
	 * Cancel all existing notifications.
	 */
	public void cancelNotification() {
		long sourceId =  WordPressInfo.COMMENTS_UID;
		NotificationsManager.cancelImmediateEvent(sourceId, 0, this, null);
		setAppIcon(false);
	}
	
	private void setAppIcon(boolean newMessages) {
		//controllare se l'applicazione è in bg
		
		if(newMessages) {
			HomeScreen.updateIcon(WordPressInfo.getNewCommentsIcon());
		}
		else {
			HomeScreen.updateIcon(WordPressInfo.getIcon());
		}
	}
	

	private class NotificationDetailsTask implements Observer {
		
		private Queue executionQueue = null; // queue of BlogInfo to check
		private boolean stopping = false;
		BlogInfo currentBlog = null;
		private boolean isNewCommentInAwatingModeration = false;
		
		public void run() {
			try {
				Log.trace("NotificationDetailsTask - run method");
				
				BlogInfo[] blogsList = guiController.getApplicationBlogs();
				executionQueue = new Queue();
				
				for (int i = 0; i < blogsList.length; i++) {
					BlogInfo blogInfo = blogsList[i];
					Log.trace("Considering the blog - "+ blogInfo.getName() + " - for the notifications details queue");
					if (blogInfo.getState() == BlogInfo.STATE_LOADED && blogInfo.isAwaitingModeration() && blogInfo.isCommentNotifies()) {
						Log.trace("added the blog - "+ blogInfo.getName() + " - to the notifications details queue");
						executionQueue.push(blogInfo);
					}
				}
				
		        next();
		        
			} catch (Throwable  e) {
				Log.error(e, "Serious Error in NotificationDetailsTask: " + e.getMessage());
			} 			  
		}
	
		
		private void next() {
			Log.trace("NotificationDetailsTask - next method");
			if (stopping  == true)
				return; //listerners notified into stop method
			
			if (!executionQueue.isEmpty()) {
				
				BlogInfo blogInfo = (BlogInfo) executionQueue.pop();
				this.currentBlog = blogInfo;
			
				//blog is correctly loaded within the app
				final GetCommentsConn connection = new GetCommentsConn(currentBlog.getXmlRpcUrl(), 
						Integer.parseInt(currentBlog.getId()), currentBlog.getUsername(), 
						currentBlog.getPassword(), -1, "", 0, 100);

				connection.addObserver(this);
				connection.startConnWorkBackground();
				
			} else {
				if(isNewCommentInAwatingModeration) {
					Log.trace("NotificationDetailsTask - ci sono nuovi commenti da notificare");

					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							notifyNewMessages();
							guiController.refreshView(); //update the main view
						}
					});
				}
				//notifica se ci sono nuovi commenti
				Log.trace("NotificationDetailsTask - END");
			}
		}

		private void storeComment(final BlogInfo blog, final Vector comments) {
			Log.trace(">>> storeNotificationComments");
			boolean foreground = false;
			boolean store = false;
			final Screen scr;
			
			synchronized(UiApplication.getEventLock()) {
				UiApplication uiApplication = UiApplication.getUiApplication();
				foreground = uiApplication.isForeground();
				scr = uiApplication.getActiveScreen();
			}

			if(!foreground) {
				Log.trace("application is in BG, store comments...");
				//the app is in background, this not ensure that we aren't in comments loading phase
				//but this condition is good enought
				store = true;
			} else {
				Log.trace("application is in ForeGround");
				if (scr instanceof CommentsView || scr instanceof CommentReplyView
						|| scr instanceof CommentView) {
					Log.trace("comment view is opened, do not store new comment");
					store = false;
				} else { 
					store = true;
					Log.trace("store comments...");
				}
			}

			if(store){
				//TODO you could update the comments view here
				try{
					CommentsDAO.storeComments(blog, comments);
				} catch (IOException e) {
					Log.error(e, "Error while storing comments");
				} catch (RecordStoreException e) {
					Log.error(e, "Error while storing comments");
				} catch (Exception e) {
					Log.error(e, "Error while storing comments");
				} 
			}
			Log.trace("<<< storeNotificationComments");
		}
		
		public void update(Observable observable, final Object object) {
			
			try{ 
				BlogConnResponse resp= (BlogConnResponse) object;
				if(!resp.isError()) {
					Vector respVector = (Vector) resp.getResponseObject(); // the response from wp server
					Hashtable vector2Comments = CommentsDAO.vector2Comments(respVector);
					Comment[] commentsFromServer =(Comment[]) vector2Comments.get("comments");
					if(vector2Comments.get("error") != null) {
						Log.error("Error while loading comments: "+ (String)vector2Comments.get("error"));
					}
					
					//retrive the previous comments ID for the current blog
					int[] previousComments = (int[])awaitingCommentsID.get(currentBlog.getXmlRpcUrl());
					
					//extract the new comments ID and put in the hashtable of Ids for the current blog
					int[] newCommentsIDList = new int[commentsFromServer.length];
					Log.trace("retrived comments from server # "+ commentsFromServer.length);
					for (int i = 0; i < newCommentsIDList.length; i++) {
						Comment	comment = commentsFromServer[i];
						newCommentsIDList[i] = comment.getID();
					}
					awaitingCommentsID.put(currentBlog.getXmlRpcUrl(), newCommentsIDList);
					
					if(previousComments == null) {
						Log.trace("this is the first time, store the comments");
						isNewCommentInAwatingModeration = true;
						storeComment(currentBlog, respVector);
					} else {
						//check if there are available new comments for moderation
						Log.trace("this is NOT the first time, checing for comments");
						boolean presence = false;
						for (int i = 0; i < commentsFromServer.length; i++) {
							Comment	commentFromServer = commentsFromServer[i];
							//check the presence of this comment only if it is in awaiting of moderation 
							Log.trace("stato del commento "+ commentFromServer.getStatus());
							if  (!commentFromServer.getStatus().equalsIgnoreCase("hold")){
								continue;
							}
							
							for (int j = 0; j < previousComments.length; j++) {
								if (previousComments[j] == commentFromServer.getID()) {
									presence = true;
									break;
								}
							}
							
							if(!presence) {
								Log.trace("commento non trovato nella cache locale");
								isNewCommentInAwatingModeration = true;
								break;
							}
						}
						
						if(!presence)
							storeComment(currentBlog, respVector);
					}
				} else {
					final String respMessage=resp.getResponse();
					Log.error("errore nel GetComments "+ respMessage);
				}		
				
				next();
				
			} catch (Throwable  e) {
				Log.error(e, "Serious Error in NotificationTask: " + e.getMessage());
			} 
		}//end callback
	}	
	
	private class NotificationTask extends TimerTask implements Observer {
		
		private Queue executionQueue = null; // queue of BlogInfo to check
		private boolean stopping = false;
		BlogInfo currentBlog = null;
		private boolean isAwatingModeration = false;
		
		public void run() {
			try {
				Log.trace("NotificationTask - run method");
				
				//read all the blogs from the filesystem and prepare the relative connections					
				BlogInfo[] blogsList = guiController.getApplicationBlogs();
				executionQueue = new Queue();
				
				for (int i = 0; i < blogsList.length; i++) {
					BlogInfo blogInfo = blogsList[i];
					Log.trace("Considering the blog - "+ blogInfo.getName() + " - for the notifications queue");
					if (blogInfo.getState() == BlogInfo.STATE_LOADED && blogInfo.isCommentNotifies()) {
						Log.trace("added the blog - "+ blogInfo.getName() + " - to the notifications queue");
						executionQueue.push(blogInfo);
					}
				}
				
		        Log.trace("Attempting to retrive new comments");
		        next();
		        
			} catch (Throwable  e) {
				cancel();
				Log.error(e, "Serious Error in NotificationTask: " + e.getMessage());
				//When NotificationTask throws an exception, it calls cancel on itself 
				//to remove itself from the Timer. 
				//It then logs the exception.
				//Because the exception never propagates back into the Timer thread, others Tasks continue to function even after 
				//NotificationTask fails.
				currentNotificationTask = new NotificationTask();
				WordPressCore.getInstance().getTimer().schedule(currentNotificationTask, 24*60*60*1000, 24*60*60*1000); //24h check
			} 			  
		}
	
		
		private void next() {
			Log.trace("NotificationTask - next method");
			if (stopping  == true)
				return; //listerners notified into stop method
			
			if (!executionQueue.isEmpty()) {
				
				BlogInfo blogInfo = (BlogInfo) executionQueue.pop();
				this.currentBlog = blogInfo;
				//blog is correctly loaded within the app
				GetCommentCountConn connection = new GetCommentCountConn(blogInfo.getXmlRpcUrl(), 
						blogInfo.getUsername(), blogInfo.getPassword(), -1);
				
				connection.addObserver(this);
				connection.startConnWorkBackground();
				
			} else {
				
				if (isAwatingModeration) {
					getAwaitingDetails();  //retrive awaiting comments details
				}
				
				Log.trace("NotificationTask - next method end");
			}
		}

		public void update(Observable observable, final Object object) {
				/*{
	            approved:(new String("14")), awaiting_moderation:(new String("1")), spam:(new String("4")), total_comments:(new Number(19))
			}
			*/	try{ 
				BlogConnResponse resp= (BlogConnResponse) object;
				Hashtable respObj= null;
				
				Log.trace("risposta è del tipo "+ resp.getResponseObject().getClass().getName());

				if(!resp.isError()) {
					
					respObj = (Hashtable) resp.getResponseObject(); // the response from wp server
					String pendingCommentsValue= String.valueOf(respObj.get("awaiting_moderation"));
					int pendingComments = Integer.parseInt(pendingCommentsValue);
					Log.trace("ci sono commenti pendenti # " + pendingComments);
					currentBlog.setAwaitingModeration(pendingComments);
					if(pendingComments > 0) {
						isAwatingModeration = true;
					}	
				} else {
					final String respMessage=resp.getResponse();
					Log.error("errore nel GetCommentsCount "+ respMessage);
				}		
				next();
			} catch (Throwable  e) {
				cancel();
				Log.error(e, "Serious Error in NotificationTask: " + e.getMessage());
				//When NotificationTask throws an exception, it calls cancel on itself 
				//to remove itself from the Timer. 
				//It then logs the exception.
				//Because the exception never propagates back into the Timer thread, others Tasks continue to function even after 
				//NotificationTask fails.
				currentNotificationTask = new NotificationTask();
				WordPressCore.getInstance().getTimer().schedule(currentNotificationTask, 24*60*60*1000, 24*60*60*1000); //24h check
			} 
		}//end callback
	}	
}