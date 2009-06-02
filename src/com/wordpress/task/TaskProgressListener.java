package com.wordpress.task;

public interface TaskProgressListener {
		
		void taskUpdate(Object obj);
		
		void taskComplete(Object obj);
}
