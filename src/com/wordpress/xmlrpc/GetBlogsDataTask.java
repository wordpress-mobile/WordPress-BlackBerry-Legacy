package com.wordpress.xmlrpc;

import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.Queue;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;

public class GetBlogsDataTask extends Observable{

	private final Queue executionQueue = new Queue(); // queue of BlogConn
	private WorkerThread worker = null;
	private BlogUpdateConn blogConn; //the current blog conn 

	private boolean stopping = false;
	private boolean started = false;
	private final TaskListener listener;

	public GetBlogsDataTask(TaskListener listener) {
		this.listener = listener;
	}

	public void startTask() {
		started = true;
		worker = new WorkerThread();
		worker.run();
	}
	  
	public void stopTask() {
	    stopping = true;
	}

	public void addConn(BlogUpdateConn blogConn) {
		executionQueue.push(blogConn);
	}

	private class WorkerThread implements Runnable, Observer {

		public void run() {
			next();
		}

		private void next() {
			if (!executionQueue.isEmpty() && stopping == false) {
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
					listener.taskUpdate(currentBlog); //update listener task. 
				} catch (final Exception e) {

					//TODO err
					System.out.println(e.getMessage());
				}
			} else { //there was an xml-rpc error
				Blog currentBlog =  blogConn.getBlog();
				currentBlog.setLoadingState(BlogInfo.STATE_LOADED_WITH_ERROR);
				try {
					BlogDAO.updateBlog(currentBlog);
					listener.taskUpdate(currentBlog); //update listener task. 
				} catch (final Exception e) {
					//TODO err
					System.out.println(e.getMessage());	
				}
				//TODO err
				System.out.println(resp.getResponse());
			}
			
			next(); // call to next
		}
	}
}