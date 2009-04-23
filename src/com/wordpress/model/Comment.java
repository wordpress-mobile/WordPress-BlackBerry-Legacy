package com.wordpress.model;

import java.util.Date;

public class Comment{

	private int commentId = -1;
	private int postID = -1;
	private String postTitle = ""; 
	private int blogID = -1;
	private int commentParent;
    private String content = "";
    private String author = "";
    private String authorUrl = "";
    private String authorEmail = ""; 
	private String commentStatus = "";
	private Date dateCreatedGmt;
	private String parent = "";
	private String userID = "";
    private String link = "";
    private String authorIp = "";
    private int offset = 0;
    private int number = 0;
    private String count = "";


	
    public int getID(){
        return commentId;
    }
    public void setID(int commentId){
       this.commentId = commentId;
    }
	
    public int getPostID() {
		return postID;
	}
	
	public void setPostID(int postID) {
		this.postID = postID;
	}
	
	public void setBlogId(int blogId) {
		this.blogID = blogId;
	}
	public int getBlogId() {
		return blogID;
	}

	
public Comment(int commentParent, String content, String author, String author_url, String author_email, String commentStatus)
{
	//this.date_created_gmt = date_created_gmt;
	this.commentStatus = commentStatus;
	this.commentParent = commentParent;
	this.content = content;
	this.author = author;
	this.authorUrl = author_url;
	this.authorEmail = author_email;
	}
	public int getCommentParent(){
		return commentParent;
	}	
	public void setCommentParent(int commentparent){
		commentParent = commentparent;
	}
	public String getContent(){
		return content;
	}
	public void setContent(String cont){
		content = cont;
	}
	public String getAuthor(){
		return author;
	}
	public void setAuthor(String auth){
			author = auth;
	}
	public String getAuthorUrl(){
		return authorUrl;
	}
	public void setAuthorUrl(String authurl){
		authorUrl = authurl;
	}
	public String getAuthorEmail(){
		return authorEmail;
	}
	public void setAuthorEmail(String authemail){
		authorEmail = authemail;
	}
	public void setComment_status(String commentStatus) {
		this.commentStatus = commentStatus;
	}
	public String getComment_status() {
		return commentStatus;
	}
	public void setDate_created_gmt(Date date_created_gmt){
	this.dateCreatedGmt = date_created_gmt;
	}
	public Date getDate_created_gmt(){
		return dateCreatedGmt;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getParent() {
		return parent;
	}
	public void setUser_Id(String user_Id) {
		this.userID = user_Id;
	}
	public String getUser_Id() {
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
	public void setAuthor_Ip(String author_Ip) {
		this.authorIp = author_Ip;
	}
	public String getAuthor_Ip() {
		return authorIp;
	}
	public void setOffset(int offset) {
		this.offset = offset;
	}
	public int getOffset() {
		return offset;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public int getNumber() {
		return number;
	}
	public void setCount(String count) {
		this.count = count;
	}
	public String getCount() {
		return count;
	}
}

