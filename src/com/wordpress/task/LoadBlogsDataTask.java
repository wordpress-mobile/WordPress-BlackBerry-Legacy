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
		if (!resp.isError()) {
			if (resp.isStopped()) {
				return;
			}
			Blog currentBlog = (Blog) resp.getResponseObject(); // update
			try {
				currentBlog.setLoadingState(BlogInfo.STATE_LOADED);
				BlogDAO.updateBlog(currentBlog);
			} catch (final Exception e) {
				currentBlog.setLoadingState(BlogInfo.STATE_ERROR);
			}
			progressListener.taskUpdate(currentBlog); //update listener task.
		} else { //there was an xml-rpc error
			Blog currentBlog =  blogConn.getBlog();
			currentBlog.setLoadingState(BlogInfo.STATE_LOADED_WITH_ERROR);
			try {
				BlogDAO.updateBlog(currentBlog);
				progressListener.taskUpdate(currentBlog); //update listener task. 
			} catch (final Exception e) {
				currentBlog.setLoadingState(BlogInfo.STATE_ERROR);	
			}
		}
		next(); // call to next blog conn
	}

}