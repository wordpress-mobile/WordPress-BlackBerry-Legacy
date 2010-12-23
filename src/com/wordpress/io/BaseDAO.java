package com.wordpress.io;


public interface BaseDAO {
	
	public static final String DEVICE_STORE_PATH ="file:///store/home/user/wordpress/";
	//hte SDCARD path is defined on AppDao.java since it is dynamic
	public static final String LOG_FILE_PREFIX = "log";
	public static final String APP_TMP_XMLRPC_FILE = "tmp_xmlrpc"; //used in xmlrpc calls
	public static final String APP_TMP_IMG_FILE = "tmp_image"; //used when resize opt is active
	public static final String DRAFT_FOLDER_PREFIX = "d/";
	public static final String PAGE_FOLDER_PREFIX = "p/";
	public static final String COMMENTS_FILE = "comments";
	public static final String MEDIALIBRARY_FILE = "media_library";
	public static final String GRAVATARS_FILE = "gravatars";

	
/*	
  	file:///store/home/user/wordpress/ (ROOT)
	file:///store/home/user/wordpress/md5(blogname)/ (root of the blog)
	file:///store/home/user/wordpress/md5(blogname)/comments (comments of the blog)
	file:///store/home/user/wordpress/md5(blogname)/d/id (post file)
	file:///store/home/user/wordpress/md5(blogname)/p/id (page file)
	
	blogname =  getBlogFolderName(blog.getId(), blog.getXmlRpcUrl());
*/
	
}
