package com.wordpress.model;

import java.util.Date;


/**
 * 
 * @author dercoli
 *
 */
public class Post extends BlogEntry {

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
    private String permaLink = null;
    private String link = null;
	private boolean isLocation = false; //true when per-post location setting is active
	private boolean isLocationPublic = true; //true when location setting is public
	private Boolean isSignatureEnabled= null; // 0 = false ; 1 = true //null = get option from blog settings
	private String signature=null;

	public Post(Blog aBlog) {
		blog = aBlog;
		//check if the blog has the location enabled by default
		//boolean location = aBlog.isLocation();
		//this.setLocation(location);
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

	public String getPermaLink() {
		return permaLink;
	}

	public void setPermaLink(String link) {
		this.permaLink = link;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}
	
	public boolean isLocation() {
		return isLocation;
	}

	public void setLocation(boolean isLocation) {
		this.isLocation = isLocation;
	}

	public boolean isLocationPublic() {
		return isLocationPublic;
	}

	public void setLocationPublic(boolean isLocationPublic) {
		this.isLocationPublic = isLocationPublic;
	}
	
	public Boolean isSignatureEnabled() {
		return isSignatureEnabled;
	}

	public void setSignatureEnabled(Boolean isSignatureActive) {
		this.isSignatureEnabled = isSignatureActive;
	}
	
	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}	
}
