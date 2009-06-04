package com.wordpress.task;

public abstract class TaskImpl implements Task{

	protected TaskProgressListener progressListener;
	protected boolean isError = false;
	protected StringBuffer errorMsg = new StringBuffer();
	
		
	public void setProgressListener(TaskProgressListener progressListener) {
		this.progressListener = progressListener;
	}
	
	public String getErrorMsg() {
		return errorMsg.toString();
	}
	
	public void appendErrorMsg(String err) {
		errorMsg.append(err+"\n");
	}
	
	public boolean isError() {
		return isError;
	}
}
