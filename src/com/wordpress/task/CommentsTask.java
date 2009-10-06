package com.wordpress.task;

import com.wordpress.utils.Queue;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class CommentsTask extends TaskImpl implements Observer {
	
	private Queue executionQueue = null; // queue of BlogConn
	
	public CommentsTask(Queue executionQueue) {
		this.executionQueue = executionQueue;
	}
	
	public void execute() {
		next();
	}

	private void next() {
		if (stopping == true)
			return; //listerners notified into stop method
		
		if (!executionQueue.isEmpty()) {
			BlogConn blogConn = (BlogConn) executionQueue.pop();
			blogConn.addObserver(this);
			blogConn.startConnWork();
		} else {
			//end
			if (progressListener != null)
				progressListener.taskComplete(null);	
		}
	}

	public void update(Observable observable, final Object object) {
		BlogConnResponse resp= (BlogConnResponse) object;
		if(!resp.isError()) {
			if(resp.isStopped()){
				stopping = true;
			} else {
				if (resp.getResponseObject() instanceof Boolean ) {
					boolean booleanValue = ((Boolean)resp.getResponseObject()).booleanValue();
					if( !booleanValue) {
						appendErrorMsg("One comment was not changed" +"\n");
						isError=true;
					}
					
				} else {
					appendErrorMsg("One comment was not changed"+"\n");
					isError=true;
				}
			}
		} else {
			final String respMessage=resp.getResponse();
			appendErrorMsg(respMessage+"\n");
			isError=true;
		}
		next(); // call to next
	}
}

