package com.wordpress.task;

import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.Queue;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;
/**
 * Task used to load additional blog data in background.  
 * @author dercoli
 *
 */
public class LoadBlogsDataTask extends TaskImpl implements Observer {
	
	private Queue executionQueue = null; // queue of BlogConn
	private BlogUpdateConn blogConn; //the current blog conn 
	
	public LoadBlogsDataTask(Queue executionQueue) {
		this.executionQueue = executionQueue;
	}
	
	public void execute() {
		next();
	}

	private void next() {
		if (executionQueue != null && !executionQueue.isEmpty()) {
			blogConn = (BlogUpdateConn) executionQueue.pop();
			blogConn.addObserver(this);
			blogConn.startConnWork();
		}
	}

	public void update(Observable observable, final Object object) {
		BlogConnResponse resp = (BlogConnResponse) object;
		if (resp.isStopped()) {
			return;
		}
		Blog currentBlog = null;
		if (!resp.isError()) {
			try {
				currentBlog = (Blog) resp.getResponseObject(); 
				currentBlog.setLoadingState(BlogInfo.STATE_LOADED);
			} catch (final Exception e) {
				//there was an error, get the blog from the conn and mark it
				currentBlog =  blogConn.getBlog();
				currentBlog.setLoadingState(BlogInfo.STATE_LOADED_WITH_ERROR);
			}
		} else { 
			currentBlog =  blogConn.getBlog();
			currentBlog.setLoadingState(BlogInfo.STATE_ERROR);
		}
		
		try {
			BlogDAO.updateBlog(currentBlog);
		} catch (final Exception e) {
			currentBlog.setLoadingState(BlogInfo.STATE_ERROR);	
		}
		progressListener.taskUpdate(currentBlog); //update listener task.
		
		
		next(); // call to next blog conn
	}

}