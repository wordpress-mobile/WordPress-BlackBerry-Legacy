package com.wordpress.model;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.controller.AccountsController;
import com.wordpress.utils.log.Log;

public class Blog {

	private int loadingState=-1; //loading state of this blog. see BlogInfo constants
	
	private boolean isWPCOMBlog = false;
	
	private String id;
	private String name;
	private String url; //user inserted blogs url
	private String xmlRpcUrl; //real url for publishing on this blog
	private String username;
	private String password;
	
	private boolean isResizePhotos = false;
    private Integer imageResizeWidth = null;
    private Integer imageResizeHeight = null;

    //VideoPress video Resize Options
	private boolean isResizeVideos = false;
    private Integer videoResizeWidth = null;
    private Integer videoResizeHeight = null;    
    
	private boolean isCommentNotifies=false; //true when comment notifies is active
	private boolean isLocation=false; //true when location is active
	
	private boolean isSignatureEnabled=false; //true add a signature at the end of each post
	private String signature=null;

	private int maxPostCount = 50 ;

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
	
	private String statsUsername = null; //this data could be different from http auth
	private String statsPassword = null; //this data could be different from http auth 
	
	private boolean isHTTPBasicAuthRequired = false;
	private String HTTPAuthUsername = null; //could be used only for self-hosted blog - this data could be different from stats auth
	private String HTTPAuthPassword = null; //could be used used for self-hosted blog - this data could be different from stats auth 
	
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


	public String getUrl() {
		return url;
	}

	public String getXmlRpcUrl() {
		return xmlRpcUrl;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		if(!isWPCOMBlog) {
			return password;
		} else {
			try {
				return AccountsController.getAccountPassword(username);
			} catch (Exception e) {
				Log.trace(e, "Error while reading the account password");
				return password;
			}
		}
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

	public void setImageResizeWidth(Integer imageResizeWidth) {
		this.imageResizeWidth = imageResizeWidth;
	}
	
	public Integer getImageResizeWidth() {
		return imageResizeWidth;
	}
	
	public void setImageResizeHeight(Integer imageResizeHeight) {
		this.imageResizeHeight = imageResizeHeight;
	}
	
	public Integer getImageResizeHeight() {
		return imageResizeHeight;
	}
	
	public boolean isResizeVideos() {
		return isResizeVideos;
	}

	public void setResizeVideos(boolean isResizeVideos) {
		this.isResizeVideos = isResizeVideos;
	}

	public Integer getVideoResizeWidth() {
		return videoResizeWidth;
	}

	public void setVideoResizeWidth(Integer videoResizeWidth) {
		this.videoResizeWidth = videoResizeWidth;
	}

	public Integer getVideoResizeHeight() {
		return videoResizeHeight;
	}

	public void setVideoResizeHeight(Integer videoResizeHeight) {
		this.videoResizeHeight = videoResizeHeight;
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

	public boolean isCommentNotifies() {
		return isCommentNotifies;
	}

	public void setCommentNotifies(boolean isCommentNotifies) {
		this.isCommentNotifies = isCommentNotifies;
	}

	public boolean isLocation() {
		return isLocation;
	}

	public void setLocation(boolean isLocation) {
		this.isLocation = isLocation;
	}
	
	public String getStatsPassword() {
		return statsPassword;
	}
	
	public void setStatsPassword(String statsPassword) {
		this.statsPassword = statsPassword;
	}
	
	public String getStatsUsername() {
		return statsUsername;
	}
	
	public void setStatsUsername(String statsUsername) {
		this.statsUsername = statsUsername;
	}
	
	public String getHTTPAuthUsername() {
		return HTTPAuthUsername;
	}

	public void setHTTPAuthUsername(String httpAuthUsername) {
		this.HTTPAuthUsername = httpAuthUsername;
	}

	public String getHTTPAuthPassword() {
		return HTTPAuthPassword;
	}

	public void setHTTPAuthPassword(String httpAuthPassword) {
		this.HTTPAuthPassword = httpAuthPassword;
	}

	public boolean isSignatureEnabled() {
		return isSignatureEnabled;
	}

	public void setSignatureEnabled(boolean isSignatureActive) {
		this.isSignatureEnabled = isSignatureActive;
	}

	
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public boolean isWPCOMBlog() {
		return isWPCOMBlog;
	}

	public void setWPCOMBlog(boolean isWPCOMBlog) {
		this.isWPCOMBlog = isWPCOMBlog;
	}

	public boolean isHTTPBasicAuthRequired() {
		return isHTTPBasicAuthRequired;
	}

	public void setHTTPBasicAuthRequired(boolean isHTTPBasicAuthRequired) {
		this.isHTTPBasicAuthRequired = isHTTPBasicAuthRequired;
	}	
}