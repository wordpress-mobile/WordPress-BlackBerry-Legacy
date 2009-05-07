package com.wordpress.xmlrpc.post;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Blog;
import com.wordpress.xmlrpc.BlogConn;

public abstract class PostConn extends BlogConn {

	public PostConn(String url, String user, String password, TimeZone timezone) {
		super(url, user, password, timezone);
	}

	
}