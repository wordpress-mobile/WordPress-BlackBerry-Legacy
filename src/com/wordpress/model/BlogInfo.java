package com.wordpress.model;

import java.util.Hashtable;

public class BlogInfo {

	public static int STATE_ADDED_TO_QUEUE = 0;
	public static int STATE_LOADING = 1;
	public static int STATE_LOADED = 2;
	public static int STATE_LOADED_WITH_ERROR = 3;
	public static int STATE_ERROR = 4;
	public static int STATE_PENDING_ACTIVATION = 5; //blog created within the app that should be activated
	
	private boolean isWPCOMBlog = false;
	private String id;
	private String name;
	private String xmlRpcUrl; //real url for publishing on this blog
	private String username;
	private String password;
	private int state = -1;

	private byte[] shortcutIcon = null;
	
	private boolean isHTTPBasicAuthRequired = false;
	private String HTTPAuthUsername = null; //could be used only for self-hosted blog - this data could be different from stats auth
	private String HTTPAuthPassword = null; //could be used used for self-hosted blog - this data could be different from stats auth 
	
	private boolean isCommentNotifies = false; //true when comment notifies is active
	
	private int[] commentsID = new int[0];
	private Hashtable commentsSummary = new Hashtable();
//    approved:(new String("14")), awaiting_moderation:(new String("1")), spam:(new String("4")), total_comments:(new Number(19))
	private boolean isCommentsDownloadNecessary = false;
	

	public BlogInfo(Blog currentBlog) {
		super();
		this.id = currentBlog.getId(); 
		this.name = currentBlog.getName();
		this.xmlRpcUrl = currentBlog.getXmlRpcUrl();
		this.username = currentBlog.getUsername();
		this.password = currentBlog.getPassword();
		this.state = currentBlog.getLoadingState();
		this.isCommentNotifies = currentBlog.isCommentNotifies();
		this.isWPCOMBlog = currentBlog.isWPCOMBlog();
		this.isHTTPBasicAuthRequired = currentBlog.isHTTPBasicAuthRequired();
		this.HTTPAuthUsername= currentBlog.getHTTPAuthUsername();
		this.HTTPAuthPassword = currentBlog.getHTTPAuthPassword();
		this.shortcutIcon = currentBlog.getShortcutIcon();
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
	
	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getState() {
		return state;
	}
	
	public void setState(int state) {
		this.state = state;
	}

	public boolean isCommentNotifies() {
		return isCommentNotifies;
	}
	
	public void setCommentNotifies(boolean isCommentNotifies) {
		this.isCommentNotifies = isCommentNotifies;
	}
	
	/*
	 * true when there are comments in the pending state
	 */
	public boolean isAwaitingModeration() {
		if(getAwaitingModeration() > 0)
			return true;
		else return false;
	}

	public void setCommentsSummary(Hashtable commentsSummary) {
		this.commentsSummary = commentsSummary;
	}

	public void setAwaitingModeration(int commNum) {
		commentsSummary.put("awaiting_moderation", String.valueOf(commNum));
	}

	public int getAwaitingModeration() {
		if (commentsSummary.get("awaiting_moderation") == null) return 0;		
		String pendingCommentsValue= String.valueOf(commentsSummary.get("awaiting_moderation"));
		int pendingComments = Integer.parseInt(pendingCommentsValue);
		return pendingComments;
	}

	public int getTotalNumbersOfComments() {
		if (commentsSummary.get("total_comments") == null) return 0;	
		String pendingCommentsValue= String.valueOf(commentsSummary.get("total_comments"));
		int pendingComments = Integer.parseInt(pendingCommentsValue);
		return pendingComments;
	}

	public int[] getCommentsID() {
		return commentsID;
	}

	public void setCommentsID(int[] commentsID) {
		this.commentsID = commentsID;
	}

	public boolean isCommentsDownloadNecessary() {
		return isCommentsDownloadNecessary;
	}

	public void setCommentsDownloadNecessary(boolean isNeed) {
		this.isCommentsDownloadNecessary = isNeed;
	}

	public boolean isWPCOMBlog() {
		return isWPCOMBlog;
	}

	public boolean isHTTPBasicAuthRequired() {
		return isHTTPBasicAuthRequired;
	}

	public void setHTTPBasicAuthRequired(boolean isHTTPBasicAuthRequired) {
		this.isHTTPBasicAuthRequired = isHTTPBasicAuthRequired;
	}
	
	public String getHTTPAuthUsername() {
		return HTTPAuthUsername;
	}

	public String getHTTPAuthPassword() {
		return HTTPAuthPassword;
	}
	
	public byte[] getShortcutIcon() {
		return shortcutIcon;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isWPCOMBlog ? 1231 : 1237);
		result = prime * result
				+ ((xmlRpcUrl == null) ? 0 : xmlRpcUrl.hashCode());
		return result;
	}

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
		if (isWPCOMBlog != other.isWPCOMBlog)
			return false;
		if (xmlRpcUrl == null) {
			if (other.xmlRpcUrl != null)
				return false;
		} else if (!xmlRpcUrl.equals(other.xmlRpcUrl))
			return false;
		return true;
	}
}
	
