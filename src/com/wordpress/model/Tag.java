package com.wordpress.model;

public class Tag{
	private int blogId = -1;
    private String struct = "";
    private int tagID;
    private String name = "";
    private int count;
    private String slug;
    private String htmlURL;
    private String rssURL;
  
    
public Tag(int blogid, int tag_id, String name, int count, String slug, String html_url, String rss_url){
	this.blogId = blogid;
	this.tagID=tag_id;
	this.name=name;
	this.count=count;
	this.slug=slug;
	this.htmlURL=html_url;
	this.rssURL=rss_url;
}

	
	public void setBlogID(int blogId) {
		this.blogId = blogId;
	}

	public int getBlogID() {
		return blogId;
	}
	public void setStruct(String struct) {
	this.struct = struct;
}

	public String getStruct() {
	return struct;
}

	public void setTagID(int tag_id) {
		this.tagID = tag_id;
	}

	public int getTagID() {
		return tagID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getCount() {
		return count;
	}

	public void setSlug(String slug) {
		this.slug = slug;
	}

	public String getSlug() {
		return slug;
	}

	public void setHtmlURL(String html_url) {
		this.htmlURL = html_url;
	}

	public String getHtmlURL() {
		return htmlURL;
	}

	public void setRssURL(String rss_url) {
		this.rssURL = rss_url;
	}

	public String getRssURL() {
		return rssURL;
	}

}