package com.wordpress.task;

import com.wordpress.xmlrpc.BlogConn;

public final class StopConnTask implements Task {

	private BlogConn conn;
	
	public StopConnTask( BlogConn conn) {
		this.conn = conn;
	}
	
	public void execute() {
		conn.stopConnWork();
	}

	public void setProgressListener(TaskProgressListener progressListener) {
		// TODO Auto-generated method stub
	}
}
