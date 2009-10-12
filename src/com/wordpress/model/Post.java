package com.wordpress.model;

import java.util.Date;
import java.util.Vector;


/**
 * 
 * @author dercoli
 *
 */
public class Post {

    private Blog blog;
    private String id = null;
    private String title = null;
    private String author = null;
    private int[] categories = null;
    private String tags= null;    
    private Date authoredOn = null;
    private String password=null;
    private boolean convertBreaks = true;
    private boolean allowComments = true;
    private boolean allowTrackback = true;
    private String body = "";
    private String status = null;
    private String extended = "";
    private String excerpt = "";
    private String link = null;
    private Boolean isPhotoResizing = null; // 0 = false ; 1 = true //null = get option from blog settings
	private Vector customFields = new Vector();  
    private Vector mediaObjects = new Vector();

    public Post(Blog aBlog) {
        blog = aBlog;
    }

    public Post(Blog aBlog, String aId, String aTitle, String aAuthor, Date aAuthoredOn) {
        blog = aBlog;
        id = aId;
        title = aTitle;
        author = aAuthor;
        authoredOn = aAuthoredOn;
    }

    public Blog getBlog() {
        return blog;
    }

    public String getId() {
        return id;
    }

    public void setId(String aId) {
        id = aId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String aTitle) {
        title = aTitle;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String aAuthor) {
        author = aAuthor;
    }

    public int[] getCategories() {
        return categories;
    }

    public void setCategories(int[] cats) {
        this.categories = cats;
    }

    public Date getAuthoredOn() {
        return authoredOn;
    }

    public void setAuthoredOn(long aAuthored) {
        authoredOn = new Date(aAuthored);
    }

    public boolean isConvertLinebreaksEnabled() {
        return convertBreaks;
    }

    public void setConvertLinebreaksEnabled(boolean aEnabled) {
        convertBreaks = aEnabled;
    }

    public boolean isCommentsEnabled() {
        return allowComments;
    }

    public void setCommentsEnabled(boolean aEnabled) {
        allowComments = aEnabled;
    }

    public boolean isTrackbackEnabled() {
        return allowTrackback;
    }

    public void setTrackbackEnabled(boolean aEnabled) {
        allowTrackback = aEnabled;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String aBody) {
        body = aBody;
    }

    public String getExtendedBody() {
        return extended;
    }

    public void setExtendedBody(String aExtended) {
        extended = aExtended;
    }

    public String getExcerpt() {
        return excerpt;
    }

    public void setExcerpt(String aExcerpt) {
        excerpt = aExcerpt;
    }
    
    public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public Boolean getIsPhotoResizing() {
		return isPhotoResizing;
	}

	public void setIsPhotoResizing(Boolean isPhotoResizing) {
		this.isPhotoResizing = isPhotoResizing;
	}
	
	public void setCustomFields(Vector custom_field) {
		this.customFields = custom_field;
	}

	public Vector getCustomFields() {
		return customFields;
	}

	public Vector getMediaObjects() {
		return mediaObjects;
	}

	public void setMediaObjects(Vector mediaObjects) {
		this.mediaObjects = mediaObjects;
	}
}
