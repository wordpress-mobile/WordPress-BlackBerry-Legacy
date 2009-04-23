package com.wordpress.model;

public class Author {
	private int blogID;
	private int userID;
	private String userLogin = "";
	private String displayName = "";
	private String userEmail = "";
	private byte[] meta;

	public Author(int blogId, int userID, String userLogin,
			String displayName, String userEmail, byte[] meta) {
		this.blogID = blogId;
		this.userID = userID;
		this.userEmail = userEmail;
		this.displayName = displayName;
		this.userLogin = userLogin;
		this.meta = meta;
	}

	public int getBlogId() {
		return blogID;
	}

	public void setBlogId(int blogId) {
		this.blogID = blogId;

	}

	public void setUserID(int user_id) {
		this.userID = user_id;
	}

	public int getUserID() {
		return userID;
	}

	public void setUserLogin(String user_login) {
		this.userLogin = user_login;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setDisplayName(String display_name) {
		this.displayName = display_name;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setUserEmail(String user_email) {
		this.userEmail = user_email;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setMeta(byte[] meta_value) {
		this.meta = meta_value;
	}

	public byte[] getMeta() {
		return meta;
	}
}