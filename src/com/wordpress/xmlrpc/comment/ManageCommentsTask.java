package com.wordpress.xmlrpc.comment;

import java.util.Hashtable;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.model.Preferences;
import com.wordpress.utils.Queue;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;

public class ManageCommentsTask {

	private final Queue executionQueue = new Queue(); // queue of BlogConn
	private WorkerThread worker = null;
	Preferences prefs = Preferences.getIstance();
	
	private boolean stopping = false;
	private boolean started = false;
	private boolean taskCompleted = false;
	private boolean isError = false; // true if there was errors in the connections
	private StringBuffer errorMessage=new StringBuffer();
	
	private Dialog connectionProgressView;

	public ManageCommentsTask() {
		}

	 public void setDialog(Dialog dlg){
		 this.connectionProgressView = dlg;
	 }

	 public boolean isError() {
		 return isError;
	 }
	 	 
	 public String getErrorMessage() {
		 return errorMessage.toString();
	 }
	
	public void startWorker() {
		started = true;
		worker = new WorkerThread();
		worker.run();
	}
	
	public boolean isTaskCompleted() {
		return taskCompleted;
	}	
  
	public void quit() {
	    stopping = true;
	}

	public void addConn(BlogConn blogConn) {
		executionQueue.push(blogConn);
	}
	
	private class WorkerThread implements Runnable, Observer {

		public void run() {
			next();
		}

		private void next() {
			
			if (!executionQueue.isEmpty() && stopping == false) {
				BlogConn blogConn = (BlogConn) executionQueue.pop();
				blogConn.addObserver(this);
				blogConn.startConnWork();
			} else {
				taskCompleted = true; 
				
				if(connectionProgressView != null)
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						connectionProgressView.close();
					}
				});
			
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
							errorMessage.append("Some Comments are not modified" +"\n");
							isError=true;
						}
						
					} else {
						errorMessage.append("Cannot modified comments"+"\n");
						isError=true;
					}
				}
			} else {
				final String respMessage=resp.getResponse();
				errorMessage.append(respMessage+"\n");
				isError=true;
			}
		
			next(); // call to next
		}
	}
}