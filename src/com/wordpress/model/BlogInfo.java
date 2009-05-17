package com.wordpress.model;

public class BlogInfo {

	public static int STATE_ADDED_TO_QUEUE = 0;
	public static int STATE_LOADING = 1;
	public static int STATE_LOADED = 2;
	public static int STATE_LOADED_WITH_ERROR = 3;
	public static int STATE_ERROR = 4;
	
	private String id;
	private String name;
	private String xmlRpcUrl; //real url for publishing on this blog
	private int state=-1;
	
	
	public BlogInfo(String id, String name, String xmlRpcUrl, int state) {
		super();
		this.id = id;
		this.name = name;
		this.xmlRpcUrl = xmlRpcUrl;
		this.state = state;
	}
	
	public String getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getXmlRpcUrl() {
		return xmlRpcUrl;
	}
	
	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}

	//variable state not considered
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((xmlRpcUrl == null) ? 0 : xmlRpcUrl.hashCode());
		return result;
	}

	//variable state not considered
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BlogInfo other = (BlogInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (xmlRpcUrl == null) {
			if (other.xmlRpcUrl != null)
				return false;
		} else if (!xmlRpcUrl.equals(other.xmlRpcUrl))
			return false;
		return true;
	}



	
}
	
