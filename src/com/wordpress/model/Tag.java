package com.wordpress.model;


public class Tag {
	
    private int id;
    private String name = "";
    private int count;
    private String slug="";
    private String htmlURL="";
    private String rssURL="";
  
    public Tag(int tag_id, String name, int count, String slug, String html_url, String rss_url) {
		this.id = tag_id;
		this.name = name;
		this.count = count;
		this.slug = slug;
		this.htmlURL = html_url;
		this.rssURL = rss_url;
	}

	
	public int getID() {
		return id;
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