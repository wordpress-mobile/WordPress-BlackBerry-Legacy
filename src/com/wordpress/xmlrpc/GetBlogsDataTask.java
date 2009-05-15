package com.wordpress.xmlrpc;

import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.Queue;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;

public class GetBlogsDataTask {

	private final Queue executionQueue = new Queue(); // queue of BlogConn
	private WorkerThread worker = null;

	private boolean stopping = false;
	private boolean started = false;

	public GetBlogsDataTask() {

	}

	public void startWorker() {
		started = true;
		worker = new WorkerThread();
		worker.run();

	}
	
	public void taskCompleted() {

	}	
  
	public void quit() {
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
				BlogUpdateConn blogConn = (BlogUpdateConn) executionQueue.pop();
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
					BlogDAO.updateBlog(currentBlog);
				} catch (final Exception e) {
					//TODO err
					System.out.println(e.getMessage());
				}
			}
			next(); // call to next
		}
	}
}