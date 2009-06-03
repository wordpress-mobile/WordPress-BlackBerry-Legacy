package com.wordpress.task;

public interface LocalTask extends Task {
	
	public boolean isError();
	public String getErrorMsg();

}
