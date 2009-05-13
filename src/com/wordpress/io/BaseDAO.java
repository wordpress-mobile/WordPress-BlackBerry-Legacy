package com.wordpress.io;

public interface BaseDAO {
	
	public static final String BASE_PATH = "file:///store/home/user/wordpress/";
	public static final String INST_FILE= BASE_PATH+"inst"; //check if the app is installed
	public static final String APP_PREFS_FILE= BASE_PATH+"prefs"; //check if the app is installed
	public static final String DRAFT_FOLDER_PREFIX = "d/";
	public static final String COMMENTS_FILE = "comments";
	public static final String BLOG_FILE = "blog";
	
/*	
  	file:///store/home/user/wordpress/ (ROOT)
	file:///store/home/user/wordpress/inst (install file)
	file:///store/home/user/wordpress/prefs (preferences files)
	file:///store/home/user/wordpress/md5(blogname)/ (root of the blog)
	file:///store/home/user/wordpress/md5(blogname)/comments (comments of the blog)
	file:///store/home/user/wordpress/md5(blogname)/d/id (post file)
	file:///store/home/user/wordpress/md5(blogname)/d/id-p-imgName (post imgs file)
	
*/
}
