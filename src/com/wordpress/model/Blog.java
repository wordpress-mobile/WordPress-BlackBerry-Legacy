package com.wordpress.model;


public class Blog {

	private String apiKey;
	private String blogId;
	private String blogName;
	private String blogUrl;
	private String blogXmlRpcUrl;
	private String username;
	private String password;

	private Category[] categories = null;

	public Blog(String apiKey, String blogId, String blogName, String blogUrl,String blogXmlRpcUrl, String userName, String pass) {
		this.apiKey = apiKey;
		this.blogId = blogId;
		this.blogName = blogName;
		this.blogUrl = blogUrl;
		this.blogXmlRpcUrl = blogXmlRpcUrl;
		this.username=userName;
		this.password=pass;
	}

	public String getApiKey() {
		return apiKey;
	}

	public String getBlogId() {
		return blogId;
	}

	public Category[] getCategories() {
		return categories;
	}

	public Category getCategory(String aId) {
		if (categories != null) {
			for (int i = 0; i < categories.length; i++) {
				if (categories[i].getId().equals(aId)) {
					return categories[i];
				}
			}
		}
		return null;
	}

	public void setCategories(Category[] aCategories) {
		categories = aCategories;
	}

	public String getBlogName() {
		return blogName;
	}

	public void setBlogName(String blogName) {
		this.blogName = blogName;
	}

	public String getBlogUrl() {
		return blogUrl;
	}

	public void setBlogUrl(String blogUrl) {
		this.blogUrl = blogUrl;
	}

	public String getBlogXmlRpcUrl() {
		return blogXmlRpcUrl;
	}

	public void setBlogXmlRpcUrl(String blogXmlRpcUrl) {
		this.blogXmlRpcUrl = blogXmlRpcUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}