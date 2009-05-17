package com.wordpress.controller;

import java.util.Vector;

import com.wordpress.model.BlogInfo;

public class AddBlogsMediator {
	
	private static AddBlogsMediator singletonObject;
	private Vector blogsInfoList = new Vector();
	

	// Note that the constructor is private
	private AddBlogsMediator() {	
	}
	
	public void addBlog(BlogInfo blogInfo){
		blogsInfoList.addElement(blogInfo);
	}
	
	public void updateState(BlogInfo blogInfo){
		for (int i = 0; i < blogsInfoList.size(); i++) {
			BlogInfo  blogInfoTmp = (BlogInfo) blogsInfoList.elementAt(i);
			if (blogInfoTmp.getId().equals(blogInfo.getId()) && 
					blogInfoTmp.getXmlRpcUrl().equals(blogInfo.getXmlRpcUrl()) ) {
				
				blogsInfoList.setElementAt(blogInfo, i);
			}
		}
	}
	
	public static AddBlogsMediator getIstance() {
		if (singletonObject == null) {
			singletonObject = new AddBlogsMediator();
		}
		return singletonObject;
	}
	
	public BlogInfo[] getBlogs(){
		BlogInfo[] returnValue = new BlogInfo[blogsInfoList.size()];
		blogsInfoList.copyInto(returnValue);
		return returnValue;
	}
	
	/**
	 * 
	 * @return true if there are blogs on londing state.
	 */
	public boolean isInLoadingState() {
		BlogInfo[] blogs = getBlogs();
		for (int j = 0; j < blogs.length; j++) {
			if (blogs[j].getState()  ==  BlogInfo.STATE_LOADING 
	  					||blogs[j].getState() == BlogInfo.STATE_ADDED_TO_QUEUE )
				return true;
		}
		return false;
	}
	
	
}