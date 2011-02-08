package com.wordpress.model;

import java.util.Date;

public class Comment {

	private String commentId = null;
	private String postID = null;
	private String commentParent=null;
	private String postTitle = "";
	private String content = "";
	private String author = "";
	private String authorUrl = "";
	private String authorEmail = "";
	private String commentStatus = "";
	private Date dateCreatedGmt;
	private String userID = "";
	private String link = "";
	private String authorIp = "";
	
	public Comment() {
	
	}

	public String getID() {
		return commentId;
	}

	public void setID(String commentId) {
		this.commentId = commentId;
	}

	public String getPostID() {
		return postID;
	}

	public void setPostID(String postID) {
		this.postID = postID;
	}

	public String getParent() {
		return commentParent;
	}

	public void setParent(String commentparent) {
		commentParent = commentparent;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String cont) {
		content = cont;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String auth) {
		author = auth;
	}

	public String getAuthorUrl() {
		return authorUrl;
	}

	public void setAuthorUrl(String authurl) {
		authorUrl = authurl;
	}

	public String getAuthorEmail() {
		return authorEmail;
	}

	public void setAuthorEmail(String authemail) {
		authorEmail = authemail;
	}

	public void setStatus(String commentStatus) {
		this.commentStatus = commentStatus;
	}

	public String getStatus() {
		return commentStatus;
	}

	public void setDateCreatedGMT(Date date_created_gmt) {
		this.dateCreatedGmt = date_created_gmt;
	}

	public Date getDateCreatedGMT() {
		return dateCreatedGmt;
	}

	public void setUserId(String user_Id) {
		this.userID = user_Id;
	}

	public String getUserId() {
		return userID;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	public String getPostTitle() {
		return postTitle;
	}

	public void setAuthorIp(String author_Ip) {
		this.authorIp = author_Ip;
	}

	public String getAuthorIp() {
		return authorIp;
	}
}
