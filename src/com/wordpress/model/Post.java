package com.wordpress.model;

import java.util.Date;


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
    private int[] tags= null;    
    private Date authoredOn = null;
    private boolean convertBreaks = true;
    private boolean allowComments = true;
    private boolean allowTrackback = true;
    private String body = "";
    private String extended = "";
    private String excerpt = "";
        

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
    
    public int[] getTags() {
		return tags;
	}

	public void setTags(int[] tags) {
		this.tags = tags;
	}

}
