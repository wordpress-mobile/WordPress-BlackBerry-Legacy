package com.wordpress.task;

public abstract class TaskImpl implements Task {

	protected TaskProgressListener progressListener;
	protected boolean isError = false;
	protected Exception originalException = null;
	protected StringBuffer errorMsg = new StringBuffer();
	protected boolean stopping = false; //flag that is true when user has stopped the task
	
		
	public void setProgressListener(TaskProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	public String getErrorMsg() {
		return errorMsg.toString();
	}

	public Exception getOriginalException() {
		return originalException;
	}
	
	public void appendErrorMsg(String err) {
		errorMsg.append(err+"\n");
	}
	
	public boolean isError() {
		return isError;
	}
	
	public boolean isStopped() {
		return stopping;
	}
	
	public void stop() {
		if(this.stopping == true) return; //already stopped
		
		this.stopping = true;
		if (progressListener != null)
			progressListener.taskComplete(null);
	}
}
