package com.wordpress.xmlrpc;

public class BlogConnResponse {
	private boolean error=false;
	private boolean isStopped=false; //se l'utente ha deciso di fermare la connessione
	private String response ="";
	private String cookie ="";
	private Object responseObject=null;
		
	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookies) {
		this.cookie = cookies;
	}

	public Object getResponseObject() {
		return responseObject;
	}

	public void setResponseObject(Object responseObject) {
		this.responseObject = responseObject;
	}

	public boolean isStopped() {
		return isStopped;
	}

	public void setStopped(boolean isStopped) {
		this.isStopped = isStopped;
	}
	
}
