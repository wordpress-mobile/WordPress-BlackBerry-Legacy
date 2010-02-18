package com.wordpress.bb;

import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import net.rim.blackberry.api.homescreen.HomeScreen;
import net.rim.device.api.notification.NotificationsManager;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.controller.MainController;
import com.wordpress.controller.NotificationController;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.comment.GetCommentCountConn;

/**
 * Handles new message notification through the various
 * notification mechanisms of the BlackBerry.
 */
public class NotificationHandler {
	private static NotificationHandler instance = null;
	private boolean notificationEnabled;
	private NotificationTask currentNotificationTask = null;
	private MainController guiController = null;
	

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
		this.notificationEnabled = isEnabled;
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
	}

	/**
	 * Shutdown the listener and unsubscribe from any system events.
	 */
	public void shutdown() {
		Log.trace("NotificationHandler stopped");
		cancelNotification();
		stopInnerTask();
		notificationEnabled = false;
	}
		
	public void notifyNewMessages() {
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
	

	private class NotificationTask extends TimerTask implements Observer {
		
		private Queue executionQueue = new Queue(); // queue of BlogInfo to check
		private boolean stopping = false;
		BlogInfo currentBlog = null;
		Vector blogsWithPending = new Vector(); //blogs with new awaiting comments
		
		public void run() {
			try {
				Log.trace("NotificationTask - run method");
				
				//read all the blogs from the filesystem and prepare the relative connections
			//	Hashtable blogsInfo = BlogDAO.getBlogsInfo();
				//BlogInfo[] blogsList =  (BlogInfo[]) blogsInfo.get("list");
					
				BlogInfo[] blogsList = guiController.getApplicationBlogs();
				
				for (int i = 0; i < blogsList.length; i++) {
					BlogInfo blogInfo = blogsList[i];
					Log.trace("Considering the blog - "+ blogInfo.getName() + " - for the notifications queue");
					if (blogInfo.getState() == BlogInfo.STATE_LOADED && blogInfo.isCommentNotifies()) {
						Log.trace("added the blog - "+ blogInfo.getName() + " - to the notifications queue");
						executionQueue.push(blogInfo);
					}
				}
				
		        Log.trace("hahahaha NotificationTask extends TimerTask implements Observer");
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
				connection.startConnWork();
				
			} else {
				//blogWithPending
				BlogInfo[] blogs = new BlogInfo[blogsWithPending.size()];
				for (int i = 0; i < blogs.length; i++) {
					blogs[i] = (BlogInfo) blogsWithPending.elementAt(i);
				}	
				guiController.newCommentNotifies(blogs);
				notifyNewMessages();
				//notifica se ci sono nuovi commenti
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
						currentBlog.setAwaitingModeration(true);
						blogsWithPending.addElement(currentBlog);
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