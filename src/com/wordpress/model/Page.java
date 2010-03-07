package com.wordpress.model;

import java.util.Date;
import java.util.Vector;

public class Page {
	
	private Date dateCreated = null;
	private int userID = -1;
	private int pageID = -1;
	private String pageStatus = null;
	private String description = "";
	private String title = "";
	private String link = null;
	private String permaLink;
	private Vector categories = new Vector();
	private String mtExcerpt = "";
	private String mtTextMore = "";
	private boolean mtAllowComments = true;
	private boolean mtAllowPings = true;
	private String wpSlug = "";
	private String wpPassword = "";
	private String wpAuthor;
	private int wpPageParentID = -1;
	private String wpPageParentTitle;
	private int wpPageOrder = -1;
	private int wpAuthorID = -1;
	private String wpAuthorDisplayName;
	private Date dateCreatedGMT;
	private Vector customFields = new Vector();
	private String wpPageTemplate;
    private Boolean isPhotoResizing = null; // 0 = false ; 1 = true //null = get option from blog settings
    private Integer imageResizeWidth = null;
    private Integer imageResizeHeight = null;
    private Vector mediaObjects = new Vector();
    
	
	public Page(int pageID, String Title, String description, Date dateCreated) {
		this.pageID = pageID;
		this.title = Title;
		this.description = description;
		this.dateCreated = dateCreated;
	}
	
	public Page() {}
	
	public int getWpAuthorID() {
		return wpAuthorID;
	}

	public void setWpAuthorID(int wpAuthorID) {
		this.wpAuthorID = wpAuthorID;
	}


	public void setWpSlug(String wp_slug) {
		this.wpSlug = wp_slug;
	}

	public String getWpSlug() {
		return wpSlug;
	}

	public void setWpPassword(String wp_password) {
		this.wpPassword = wp_password;
	}

	public String getWpPassword() {
		return wpPassword;
	}

	public void setWpPageParentID(int wp_parent_id) {
		this.wpPageParentID = wp_parent_id;
	}

	public int getWpPageParentID() {
		return wpPageParentID;
	}

	public void setWpPageOrder(int wp_page_order) {
		this.wpPageOrder = wp_page_order;
	}

	public int getWpPageOrder() {
		return wpPageOrder;
	}

	public void setWpAuthor(String wp_author_id) {
		this.wpAuthor = wp_author_id;
	}

	public String getWpAuthor() {
		return wpAuthor;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setMt_excerpt(String mt_excerpt) {
		this.mtExcerpt = mt_excerpt;
	}

	public String getMt_excerpt() {
		return mtExcerpt;
	}

	public void setMtTextMore(String mt_text_more) {
		this.mtTextMore = mt_text_more;
	}

	public String getMtTextMore() {
		return mtTextMore;
	}

	public boolean isCommentsEnabled() {
		return mtAllowComments;
	}

	public void setCommentsEnabled(boolean aEnabled) {
		mtAllowComments = aEnabled;
	}

	public boolean isPingsEnabled() {
		return mtAllowPings;
	}

	public void setPingsEnabled(boolean bEnabled) {
		mtAllowPings = bEnabled;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	public Date getDateCreated() {
		return dateCreated;
	}

	public void setCustomFields(Vector custom_field) {
		this.customFields = custom_field;
	}

	public Vector getCustomFields() {
		return customFields;
	}

	public void setPageId(int pageId) {
		this.pageID = pageId;
	}

	public int getID() {
		return pageID;
	}

	public void setPageStatus(String page_status) {
		this.pageStatus = page_status;
	}

	public String getPageStatus() {
		return pageStatus;
	}

	public void setUserID(int userid) {
		this.userID = userid;
	}

	public int getUserID() {
		return userID;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getLink() {
		return link;
	}

	public void setPermaLink(String permaLink) {
		this.permaLink = permaLink;
	}

	public String getPermaLink() {
		return permaLink;
	}

	public void setCategories(Vector categories) {
		this.categories = categories;
	}

	public Vector getCategories() {
		return categories;
	}

	public void setWpPageParentTitle(String wp_page_parent_title) {
		this.wpPageParentTitle = wp_page_parent_title;
	}

	public String getWpPageParentTitle() {
		return wpPageParentTitle;
	}

	public void setWpAuthorDisplayName(String wp_author_display_name) {
		this.wpAuthorDisplayName = wp_author_display_name;
	}

	public String getWpAuthorDisplayName() {
		return wpAuthorDisplayName;
	}


	public void setDateCreatedGMT(Date date_created_gmt) {
		this.dateCreatedGMT = date_created_gmt;
	}

	public Date getDateCreatedGMT() {
		return dateCreatedGMT;
	}

	public void setWpPageTemplate(String wpPageTemplate) {
		this.wpPageTemplate = wpPageTemplate;
	}

	public String getWpPageTemplate() {
		return wpPageTemplate;
	}

	public Boolean getIsPhotoResizing() {
		return isPhotoResizing;
	}

	public void setIsPhotoResizing(Boolean isPhotoResizing) {
		this.isPhotoResizing = isPhotoResizing;
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
	
	public Vector getMediaObjects() {
		return mediaObjects;
	}

	public void setMediaObjects(Vector mediaObjects) {
		this.mediaObjects = mediaObjects;
	}
}
