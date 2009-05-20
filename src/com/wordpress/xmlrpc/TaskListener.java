package com.wordpress.xmlrpc;

public interface TaskListener {
	
	void taskUpdate(Object obj);
	
	void taskComplete(Object obj);

}
