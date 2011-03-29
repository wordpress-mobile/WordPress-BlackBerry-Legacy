//#preprocess
package com.wordpress.bb;

import java.io.IOException;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.blackberry.api.homescreen.HomeScreen;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.blackberry.api.messagelist.ApplicationIcon;
import net.rim.blackberry.api.messagelist.ApplicationIndicator;
import net.rim.blackberry.api.messagelist.ApplicationIndicatorRegistry;
//#endif
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.controller.MainController;
import com.wordpress.controller.NotificationController;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Comment;
import com.wordpress.model.Preferences;
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
	private NotificationTask currentNotificationTask = null;
	private NotificationDetailsTask currentDetailsTask = null;

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
	 * Sets whether notifications are enabled.
	 * 
	 * @param isEnabled True to enable notifications, false to disable
	 */
	public void setCommentsNotification(boolean isEnabled, int updateTimeIndex) {
		
		if(updateTimeIndex == 0) return;
		int updateInterval = NotificationController.decodeInterval(updateTimeIndex);
		Log.trace("updateInterval ms : " + updateInterval);
		if(updateInterval == 0) return;
		
		Log.trace("NotificationHandler enabled");
		stopInnerTask(); //it is not necessary here, anyway we have put one more checks
		currentNotificationTask = new NotificationTask();
		
		WordPressCore.getInstance().getTimer().schedule(currentNotificationTask, updateInterval, updateInterval); //1mins check
	}
	
	
	private void stopInnerTask() {
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
		//#ifdef IS_OS47_OR_ABOVE
		try{
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();    
			if ( reg.getApplicationIndicator() != null ) 
				reg.unregister();
		}catch(Exception e){
			Log.error(e, "Error while un-registering the application indicator");
		}
		//#endif
	}
	
	//start the task that gets the awaiting comments details
	private void startNotificationDetailsTask(){
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
		if(newMessages) {
			HomeScreen.updateIcon(WordPressInfo.getNewCommentsIcon());
		}
		else {
			HomeScreen.updateIcon(WordPressInfo.getIcon());
		}
		
		//#ifdef IS_OS47_OR_ABOVE
		updateAplicationIndicator();
		//#endif
	}

	private void updateAplicationIndicator() {
		//#ifdef IS_OS47_OR_ABOVE
		try{
			ApplicationIndicatorRegistry reg = ApplicationIndicatorRegistry.getInstance();
			ApplicationIndicator indicator = null;
			if ( reg.getApplicationIndicator() != null ) {
				indicator = reg.getApplicationIndicator();
			} else {
				EncodedImage image = EncodedImage.getEncodedImageResource("wpmini-blue.png");
				ApplicationIcon icon = new ApplicationIcon( image );				
				indicator = reg.register( icon, false, true);				
				indicator.setIcon(icon);
			}
			BlogInfo[] blogCaricati = MainController.getIstance().getApplicationBlogs();
			int totalComments = 0;
			for(int i= 0; i < blogCaricati.length; i++) {
				if(blogCaricati[i].isAwaitingModeration())
					totalComments+=  blogCaricati[i].getAwaitingModeration();
			}
			indicator.setValue(totalComments);
			if(totalComments > 0)
				indicator.setVisible(true);
			else
				indicator.setVisible(false);
		} catch (Exception e) {
			Log.error(e, "Error while registering/updating the application indicator");
		}
		//#endif
	}

	private class NotificationDetailsTask implements Observer {
		
		private Queue executionQueue = null; // queue of BlogInfo to check
		private boolean stopping = false;
		BlogInfo currentBlog = null;
		private boolean isNewCommentInAwatingModeration = false;
		
		public void run() {
			try {
				Log.trace("NotificationDetailsTask - run method");
				
				BlogInfo[] blogsList = MainController.getIstance().getApplicationBlogs();
				executionQueue = new Queue();
				
				for (int i = 0; i < blogsList.length; i++) {
					BlogInfo blogInfo = blogsList[i];
					Log.trace("Considering the blog - "+ blogInfo.getName() + " - for the notifications details queue");
					if (blogInfo.getState() == BlogInfo.STATE_LOADED && blogInfo.isAwaitingModeration() && blogInfo.isCommentNotifies()) {
						Log.trace("added the blog - "+ blogInfo.getName() + " - because there are comments awaiting moderation");
						executionQueue.push(blogInfo);
					} else if (blogInfo.getState() == BlogInfo.STATE_LOADED && blogInfo.isCommentsDownloadNecessary() && blogInfo.isCommentNotifies()) {
						Log.trace("added the blog - "+ blogInfo.getName() + " - becouse there are new comments to download");
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
						currentBlog.getId(), currentBlog.getUsername(), 
						currentBlog.getPassword(), null, null, 0, WordPressInfo.DEFAULT_DOWNLOADED_COMMENTS);
				if(currentBlog.isHTTPBasicAuthRequired()) {
					connection.setHttp401Password(currentBlog.getHTTPAuthPassword());
					connection.setHttp401Username(currentBlog.getHTTPAuthUsername());
				}				
				connection.addObserver(this);
				connection.startConnWorkBackground();
				
			} else {
				if(isNewCommentInAwatingModeration) {
					Log.trace("NotificationDetailsTask - ci sono nuovi commenti da notificare");

					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							notifyNewMessages();
							MainController.getIstance().refreshView(); //update the main view
						}
					});
				} else {
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							//#ifdef IS_OS47_OR_ABOVE
							updateAplicationIndicator();
							//#endif
							MainController.getIstance().refreshView(); //update the main view
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
			
			try { 
				BlogConnResponse resp= (BlogConnResponse) object;
				if(!resp.isError()) {
					Vector respVector = (Vector) resp.getResponseObject(); // the response from wp server
					Hashtable vector2Comments = CommentsDAO.vector2Comments(respVector);
					Comment[] commentsFromServer =(Comment[]) vector2Comments.get("comments");
					if(vector2Comments.get("error") != null) {
						Log.error("Error while loading comments: "+ (String)vector2Comments.get("error"));
					}
					
					//retrive the previous comments ID List for  current blog
					String[] newCommentsIDList = new String[commentsFromServer.length];
					Log.trace("retrived comments from server # "+ commentsFromServer.length);
					for (int i = 0; i < newCommentsIDList.length; i++) {
						Comment	comment = commentsFromServer[i];
						newCommentsIDList[i] = comment.getID();
					}

					String[] previousComments = currentBlog.getCommentsID();
					currentBlog.setCommentsID(newCommentsIDList);
			
					//check if there are available new comments for moderation
					boolean presence = false;
					for (int i = 0; i < commentsFromServer.length; i++) {
						Comment	commentFromServer = commentsFromServer[i];
						//check the presence of this comment only if it is in awaiting of moderation 
						Log.trace("comment state: "+ commentFromServer.getStatus());
						if  (!commentFromServer.getStatus().equalsIgnoreCase("hold")){
							continue;
						}

						for (int j = 0; j < previousComments.length; j++) {
							if (previousComments[j].equalsIgnoreCase(commentFromServer.getID())) {
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

					if(presence == false || currentBlog.isCommentsDownloadNecessary())
						storeComment(currentBlog, respVector);
					
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
		private boolean isNecessaryGetCommentsTask = false;
		
		public void run() {
			try {
				Log.trace("NotificationTask - run method");
				
				//read all the blogs from the filesystem and prepare the relative connections					
				BlogInfo[] blogsList = MainController.getIstance().getApplicationBlogs();
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
				restartTaskAfterFails(e);
			} 			  
		}
	
		//When NotificationTask throws an exception, it calls cancel on itself 
		//to remove itself from the Timer. 
		//It then logs the exception.
		//Because the exception never propagates back into the Timer thread, others Tasks continue to function even after 
		//NotificationTask fails.
		private void restartTaskAfterFails(Throwable  e) {
			int updateTimeIndex = Preferences.getIstance().getUpdateTimeIndex();
			cancel();
			Log.error(e, "Serious Error in NotificationTask: " + e.getMessage());
			
			int updateInterval = NotificationController.decodeInterval(updateTimeIndex);
			Log.trace("updateInterval ms : " + updateInterval);
			if(updateInterval == 0) updateInterval = 24*60*60*1000; //24h check
			
			currentNotificationTask = new NotificationTask();
			WordPressCore.getInstance().getTimer().schedule(currentNotificationTask, updateInterval, updateInterval); 
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
						blogInfo.getUsername(), blogInfo.getPassword(), "-1");
				if(currentBlog.isHTTPBasicAuthRequired()) {
					connection.setHttp401Password(currentBlog.getHTTPAuthPassword());
					connection.setHttp401Username(currentBlog.getHTTPAuthUsername());
				}
				connection.addObserver(this);
				connection.startConnWorkBackground();
				
			} else {
				
				if (isNecessaryGetCommentsTask) {
					startNotificationDetailsTask();  //retrive awaiting comments details
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
					if(pendingComments > 0) {
						isNecessaryGetCommentsTask = true;
					} 
					
					//check to see if the number of totalcomments has changed
					String totalComments = String.valueOf(respObj.get("total_comments"));
					Log.trace("total blog comments "+ totalComments);
					int parsedTotalComments = Integer.parseInt(totalComments);
					if(currentBlog.getTotalNumbersOfComments() != parsedTotalComments) {
						currentBlog.setCommentsDownloadNecessary(true);
						isNecessaryGetCommentsTask = true;
					} else {
						currentBlog.setCommentsDownloadNecessary(false);
					}
					currentBlog.setCommentsSummary(respObj);
				} else {
					final String respMessage=resp.getResponse();
					Log.error("errore nel GetCommentsCount "+ respMessage);
				}		
				next();
			} catch (Throwable  e) {
				restartTaskAfterFails(e);
			} 
		}//end callback
	}	
}