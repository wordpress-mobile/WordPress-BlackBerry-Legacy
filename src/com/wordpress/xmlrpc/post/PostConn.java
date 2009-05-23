package com.wordpress.xmlrpc.post;

import com.wordpress.xmlrpc.BlogConn;

public abstract class PostConn extends BlogConn {

	public PostConn(String url, String user, String password) {
		super(url, user, password);
	}

	
}