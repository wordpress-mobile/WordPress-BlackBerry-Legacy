package com.wordpress.xmlrpc;

import java.util.Vector;

import com.wordpress.utils.log.Log;
import com.wordpress.xmlrpc.BlogConn;

public class ParameterizedBlogConn extends BlogConn  {
	
	private Vector parametes = null;
	private String methodName = null;
	
	public ParameterizedBlogConn(String URL,  String methodName, Vector additionalParameters ) {
		super(URL);
		this.methodName = methodName;
		if(additionalParameters != null)
			this.parametes = additionalParameters;
		else 
			this.parametes = new Vector();
	}

	public void run() {
		
		try {
			Log.debug(">>> Parameterized XML-RPC call: "+methodName);
			Object response = execute(methodName, parametes);
			if (connResponse.isError()) {
				notifyObservers(connResponse);
				return;	
			}	
			connResponse.setResponseObject(response);
			Log.debug("<<< Parameterized XML-RPC call: "+methodName);			
		} catch (Exception cce) {
			setErrorMessage(cce, "Parameterized XML-RPC call: "+methodName+" Error");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error(">>> Parameterized XML-RPC call: "+methodName+" Notify error");
		}
	}
}