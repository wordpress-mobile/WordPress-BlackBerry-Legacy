package com.wordpress.model;

import java.util.Hashtable;
import java.util.Vector;

public class Blog {

	private int loadingState=-1; //loading state of this blog. see BlogInfo constants
	
	private String id;
	private String name;
	private String url; //user inserted blogs url
	private String xmlRpcUrl; //real url for publishing on this blog
	private String username;
	private String password;
	
	private boolean isResizePhotos=false;
	private int maxPostCount=-1;

	private Category[] categories = null;
	private Hashtable postStatusList=null; 	
	private Hashtable pageStatusList=null;
	private Hashtable pageTemplates=null;
	private Hashtable commentStatusList=null; 
	private Tag[] tags=null;
	
	private Vector recentPostTitles = null; //response of mt.getRecentPostTitles
	private Vector viewedPost = new Vector(); //the viewed post (similar to response of previous mt.getRecentPostTitles) 
		
	private Vector pages = null;
	private int[] viewedPages = new int[0]; //the viewed page. only the ID of the page as String
	
	public Vector getViewedPost() {
		return viewedPost;
	}

	public void setViewedPost(Vector viewedPost) {
		this.viewedPost = viewedPost;
	}
	
	public Blog(String blogId, String blogName, String blogUrl,String blogXmlRpcUrl, 
			String userName, String pass) {
		this.id = blogId;
		this.name = blogName;
		this.url = blogUrl;
		this.xmlRpcUrl = blogXmlRpcUrl;
		this.username=userName;
		this.password=pass;
	}

	public String getId() {
		return id;
	}

	public Category[] getCategories() {
		return categories;
	}

	public void setCategories(Category[] aCategories) {
		categories = aCategories;
	}

	public String getName() {
		return name;
	}

	/*public void setBlogName(String blogName) {
		this.blogName = blogName;
	}
*/
	public String getUrl() {
		return url;
	}
/*
	public void setBlogUrl(String blogUrl) {
		this.blogUrl = blogUrl;
	}
*/
	public String getXmlRpcUrl() {
		return xmlRpcUrl;
	}
/*
	public void setBlogXmlRpcUrl(String blogXmlRpcUrl) {
		this.blogXmlRpcUrl = blogXmlRpcUrl;
	}
*/
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

	public boolean isResizePhotos() {
		return isResizePhotos;
	}

	public void setResizePhotos(boolean isResizePhotos) {
		this.isResizePhotos = isResizePhotos;
	}

	public int getMaxPostCount() {
		return maxPostCount;
	}

	public void setMaxPostCount(int maxPostCount) {
		this.maxPostCount = maxPostCount;
	}

	public Hashtable getPostStatusList() {
		return postStatusList;
	}

	public void setPostStatusList(Hashtable postStatusList) {
		this.postStatusList = postStatusList;
	}

	public Hashtable getPageStatusList() {
		return pageStatusList;
	}

	public void setPageStatusList(Hashtable pageStatusList) {
		this.pageStatusList = pageStatusList;
	}
	
	public Tag[] getTags() {
		return tags;
	}

	public void setTags(Tag[] tags) {
		this.tags = tags;
	}
	
	public Vector getRecentPostTitles() {
		return recentPostTitles;
	}

	public void setRecentPostTitles(Vector recentPostTitles) {
		this.recentPostTitles = recentPostTitles;
	}

	public Hashtable getCommentStatusList() {
		return commentStatusList;
	}

	public void setCommentStatusList(Hashtable commentStatusList) {
		this.commentStatusList = commentStatusList;
	}

	public int getLoadingState() {
		return loadingState;
	}

	public void setLoadingState(int loadingState) {
		this.loadingState = loadingState;
	}

	public Vector getPages() {
		return pages;
	}

	public int[] getViewedPages() {
		return viewedPages;
	}

	public void setPages(Vector pages) {
		this.pages = pages;
	}

	public void setViewedPages(int[] viewedPages) {
		this.viewedPages = viewedPages;
	}

	public Hashtable getPageTemplates() {
		return pageTemplates;
	}

	public void setPageTemplates(Hashtable pageTemplates) {
		this.pageTemplates = pageTemplates;
	}
}