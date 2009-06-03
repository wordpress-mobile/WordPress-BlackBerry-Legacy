package com.wordpress.task;

public interface Task {

	void execute();
	
	void setProgressListener(final TaskProgressListener progressListener) ;

}

