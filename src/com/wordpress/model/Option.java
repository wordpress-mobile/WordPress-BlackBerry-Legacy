package com.wordpress.model;


public class Option{
	private int blogID = -1;
	private String name="";
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	private String value = "";

	public Option(String option, String value){
		this.name=option;
		this.value=value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
	
	public void setBlogId(int blogId) {
		this.blogID = blogId;
	}
	public int getBlogId() {
		return blogID;
	}

}