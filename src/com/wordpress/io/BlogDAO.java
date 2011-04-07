package com.wordpress.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.ControlledAccessException;

import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Category;
import com.wordpress.model.Tag;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.MD5;
import com.wordpress.utils.log.Log;

public class BlogDAO implements BaseDAO {
	
	//You might wonder what happens when a static synchronized method is invoked, since a static method is 
	//associated with a class, not an object. 
	//In this case, the thread acquires the intrinsic lock for the Class object associated with the class. 
			
	/**
     * add Blogs to the storage!
     * @param blog
     * @param overwrite 
     * @return
	 * @throws Exception If a blog with the same xmlrpc url and ID already exist in the app
     */
    public static synchronized void newBlogs(Vector newBlogs) throws Exception {
    	if (newBlogs.size() == 0 ) return ;
    	
    	Vector blogWithError = new Vector();
    	
    	Hashtable storedBlogs = loadBlogs(); //load the app blogs
    	for (int i = 0; i < newBlogs.size(); i++) {
    		Blog blog = (Blog)newBlogs.elementAt(i);
			try {
		    	String nameMD5 = getBlogFolderName(blog);
		    	String filePath = AppDAO.getBaseDirPath()+nameMD5;
		    	if (storedBlogs.get(nameMD5) != null){
		    		blogWithError.addElement(blog);
		    		Log.error("Cannot add '" + blog.getName() + " - " + blog .getXmlRpcUrl()+ "' because that blog already exists in the App");
		    	} else {
		    		JSR75FileSystem.createDir(AppDAO.getBaseDirPath()); 
		    		JSR75FileSystem.createDir(filePath);
		    		JSR75FileSystem.createDir(filePath+DRAFT_FOLDER_PREFIX); //create draft posts folder
		    		JSR75FileSystem.createDir(filePath+PAGE_FOLDER_PREFIX); //create draft page folder
		    		storedBlogs.put(nameMD5, serializeBlogData(blog));
		    	}    					
			} catch (Exception e) {
				blogWithError.addElement(blog);
	    		Log.error(e, "Cannot add '" + blog.getName() + " - " + blog .getXmlRpcUrl()+ "' because that blog already exists in the App");
			}
		}
		AppDAO.storeSecuredAppData(AppDAO.BLOGS_DATA_ID, storedBlogs);
		
		//remove the blogs with error within the blogs vector
		for (int i = 0; i < blogWithError.size(); i++) {
			newBlogs.removeElement(blogWithError.elementAt(i));
		}
    	return;
    }
    
    public static synchronized void setUpFolderStructureForBlogs(Vector newBlogs) throws Exception {
    	if (newBlogs.size() == 0 ) return ;
    	String baseAppPath = AppDAO.getBaseDirPath();
    	JSR75FileSystem.createDir(baseAppPath); 
    	for (int i = 0; i < newBlogs.size(); i++) {
    		BlogInfo blog = (BlogInfo)newBlogs.elementAt(i);
    		String nameMD5 = getBlogFolderName(blog);
    		String filePath = baseAppPath+nameMD5;
    		JSR75FileSystem.createDir(filePath);
    		JSR75FileSystem.createDir(filePath+DRAFT_FOLDER_PREFIX); //create draft posts folder
    		JSR75FileSystem.createDir(filePath+PAGE_FOLDER_PREFIX); //create draft page folder
    	}
    	return;
    }
    
	/**
     * Update all blog info in the storage
     * @param aBlog
     * @return
     */
    public static synchronized boolean updateBlog(Blog blog) throws Exception {   	
    	String name = blog.getName();
    	String nameMD5 = getBlogFolderName(blog);
    	Hashtable blogs = loadBlogs();
    	if (blogs.get(nameMD5) == null){
    		throw new Exception("Cannot update this blog: " + name + " because not exist!");
    	}  	
    	blogs.put(nameMD5, serializeBlogData(blog));
    	AppDAO.storeSecuredAppData(AppDAO.BLOGS_DATA_ID, blogs);
    	Log.debug("blog updated succesfully");    	    	
    	return true;
    }

    public static synchronized boolean[] checkBlogsExistance(Blog[] blogsInfo) throws ControlledAccessException, IOException {		
    	Hashtable loadBlogs = loadBlogs(); //load blogs from storage
    	boolean[] existArray = new boolean[blogsInfo.length];
    	for (int i = 0; i < blogsInfo.length; i++) {
    		try {
    			String blogName = getBlogFolderName(blogsInfo[i]);
    			if (loadBlogs.containsKey(blogName))
    				existArray[i] = true;
    			else 
    				existArray[i] = false;
    		} catch (Exception e) {
    			Log.error(e, "Failed to check the existence for the blog: " + blogsInfo[i].getName()); 
    			existArray[i] = true;
    		}
    	}
    	return existArray;
    }

    public static Blog[] getBlogs() throws Exception {
        try {
        	Hashtable loadBlogs = loadBlogs();
        	Enumeration keys = loadBlogs.keys();
        	Vector blogs = new Vector();
        	while (keys.hasMoreElements()) {
        		String currentKey = (String) keys.nextElement();
        		byte[] blogData = (byte[])loadBlogs.get(currentKey);
        		Blog currentBlog = deserializeBlogData (blogData);
				blogs.addElement(currentBlog);
			}
            Blog[] blogList = new Blog[blogs.size()];
            blogs.copyInto(blogList);
            return blogList;
        } catch (Exception e) {
        	throw new Exception("Failed to load blog: " + e.getMessage());            
        }
    }
		
    //TODO refactor blog and blogInfo. with a common base class
    //reload this blog form disk to memory
    public static synchronized Blog getBlog(Blog blogInfo) throws Exception {
        try {
        	String blogName = getBlogFolderName(blogInfo);
        	Hashtable loadBlogs = loadBlogs();
        	if (loadBlogs.get(blogName) == null){
        		throw new IOException("Cannot load this blog: " + blogName + " because does not exist!");
        	}   
        	byte[] blogData = (byte[])loadBlogs.get(blogName);
        	return deserializeBlogData (blogData);
        } catch (Exception e) {
        	throw new Exception("Failed to load blog: " + e.getMessage());            
        }
    }
    
    public static synchronized Blog getBlog(BlogInfo blogInfo) throws Exception {
        try {
        	String blogName = getBlogFolderName(blogInfo);
        	Hashtable loadBlogs = loadBlogs();
        	if (loadBlogs.get(blogName) == null){
        		throw new IOException("Cannot load this blog: " + blogName + " because does not exist!");
        	}   
        	byte[] blogData = (byte[])loadBlogs.get(blogName);
        	return deserializeBlogData (blogData);
        	
        } catch (Exception e) {
        	throw new Exception("Failed to load blog: " + e.getMessage());            
        }
    }
    
    
	private static void serializeTest (Object obj ) throws IOException {
		Log.trace(">>> serialize test");
		//this tmpStream is used to test writing before real writing into device
		DataOutputStream tmpStream = new DataOutputStream(new ByteArrayOutputStream());
		Serializer serTemp= new Serializer(tmpStream);
		try {
			serTemp.serialize(obj);
		} catch (IOException e) {
			Log.error(e, "serialize test fails");
			throw e;
		} finally {
			//free mem
			serTemp = null;
			tmpStream.close();
			tmpStream = null;
			Log.trace("<<< serialize test");
		}
	}
    
	/*
	private static synchronized void storeBlogPassword(String blogKey, String password)
	throws IOException, ControlledAccessException {
		Hashtable accounts = AppDAO.loadSecuredAppData(AppDAO.WPORG_BLOGS_PASSWORD_ID);
		accounts.put(blogKey, password);
		AppDAO.storeSecuredAppData(AppDAO.WPORG_BLOGS_PASSWORD_ID, accounts);
	}
	
	private static synchronized void loadBlogPassword(String blogKey)
	throws IOException, ControlledAccessException {
		Hashtable accounts = AppDAO.loadSecuredAppData(AppDAO.WPORG_BLOGS_PASSWORD_ID);
		Hashtable account = (String)applicationAccounts.get(blogKey);
		return (String) account.get(AppDAO.PASSWORD_KEY);
		
	}
	*/
	
	private static synchronized byte[] serializeBlogData(Blog blog) throws IOException {

		ByteArrayOutputStream tmpLocation = new ByteArrayOutputStream();

		String wholeErrorMessage = ""; 
		Serializer ser= new Serializer(new DataOutputStream(tmpLocation));
		boolean isError = false;

		ser.serialize(new Integer(blog.getLoadingState()));
		ser.serialize(blog.getXmlRpcUrl());
		ser.serialize(blog.getUsername());
		ser.serialize( blog.getPassword());//password
		//if(!blog.isWPCOMBlog()) storeBlogPassword(nameMD5, blog.getPassword());
		ser.serialize(blog.getId());
		ser.serialize(blog.getName());
		ser.serialize(blog.getUrl());
		ser.serialize(new Integer(blog.getMaxPostCount()));
		ser.serialize(new Boolean(blog.isResizePhotos()));
		ser.serialize(blog.getCommentStatusList());
		ser.serialize(blog.getPageStatusList());
		ser.serialize(blog.getPageTemplates());
		ser.serialize(blog.getPostStatusList());

		try {
			serializeTest(blog.getRecentPostTitles());
			//if test fails don't write into real stream
			ser.serialize(blog.getRecentPostTitles());
		} catch (Exception errPage) {
			isError = true;
			String errorMessage = errPage.getMessage();
			if(errorMessage != null && !errorMessage.trim().equals(""))
				wholeErrorMessage += "Store Recent Post Error" + " - " + errorMessage + "\n";

			ser.serialize(new Vector()); //serialize empty posts vector
		}

		ser.serialize(blog.getViewedPost());

		try {
			serializeTest(blog.getPages());
			//if test fails don't write into real stream
			ser.serialize(blog.getPages());
		} catch (Exception errPage) {
			isError = true;
			String errorMessage = errPage.getMessage();
			if(errorMessage != null && !errorMessage.trim().equals(""))
				wholeErrorMessage += "Store Pages Error" + " - " + errorMessage + "\n";

			ser.serialize(new Vector()); //serialize empty pages vector
		}

		ser.serialize(blog.getViewedPages());

		Category[] categories = blog.getCategories();
		if (categories != null) {
			ser.serialize(new Integer(categories.length));
			for (int i = 0; i < categories.length; i++) {
				ser.serialize(categories[i].getId());
				ser.serialize(categories[i].getLabel());
				ser.serialize(categories[i].getDescription());
				ser.serialize(new Integer(categories[i].getParentCategory()));
				ser.serialize(categories[i].getHtmlUrl());
				ser.serialize(categories[i].getRssUrl());           
			}
		} else {
			ser.serialize(new Integer(0));
		}

		Tag[] tags = blog.getTags();
		if (tags != null) {
			ser.serialize(new Integer(tags.length));
			for (int i = 0; i < tags.length; i++) {
				ser.serialize(new Integer(tags[i].getID()));
				ser.serialize(tags[i].getName());
				ser.serialize(new Integer(tags[i].getCount()));
				ser.serialize(tags[i].getSlug());
				ser.serialize(tags[i].getHtmlURL());
				ser.serialize(tags[i].getRssURL());           
			}
		} else {
			ser.serialize(new Integer(0));
		}

		ser.serialize(new Boolean(blog.isCommentNotifies()));
		ser.serialize(new Boolean(blog.isLocation()));

		// Store image resize dimensions if they have been set.
		Integer imageResizeWidth = blog.getImageResizeWidth();
		if(imageResizeWidth != null) {
			ser.serialize(imageResizeWidth);
		}
		else {
			ser.serialize(new Integer(0));
		}

		Integer imageResizeHeight = blog.getImageResizeHeight();
		if(imageResizeHeight != null) {
			ser.serialize(imageResizeHeight);
		}
		else {
			ser.serialize(new Integer(0));
		}

		ser.serialize(new Boolean(blog.isSignatureEnabled()));
		ser.serialize(blog.getSignature());

		ser.serialize(blog.getStatsUsername());
		ser.serialize(blog.getStatsPassword());

		ser.serialize(new Boolean(blog.isResizeVideos()));
		ser.serialize(blog.getVideoResizeWidth()); //if it is null no problem, the serializer handles null value
		ser.serialize(blog.getVideoResizeHeight()); //if it is null no problem, the serializer handles null value

		ser.serialize(new Boolean(blog.isWPCOMBlog()));

		ser.serialize(new Boolean(blog.isHTTPBasicAuthRequired()));
		ser.serialize(blog.getHTTPAuthUsername());
		ser.serialize(blog.getHTTPAuthPassword());
		ser.serialize(blog.getBlogOptions());
		ser.serialize(blog.getWpcomFeatures());
		ser.serialize(blog.getPostFormats());
		
		if(isError) {
			throw new IOException(wholeErrorMessage);
		}

		return tmpLocation.toByteArray();
	}

       
	private static synchronized Blog deserializeBlogData(byte[] blogData) throws Exception {
		DataInputStream in = new DataInputStream(new ByteArrayInputStream(blogData));
		Serializer ser= new Serializer(in);

		Blog blog;

		int loadingState= ((Integer)ser.deserialize()).intValue();
		String xmlRpcUrl = (String)ser.deserialize();
		String userName = (String)ser.deserialize();
		String password = (String)ser.deserialize();

		String blodId= (String)ser.deserialize();
		String blogName= (String)ser.deserialize();
		String blodUrl= (String)ser.deserialize();
		int maxPostCount= ((Integer)ser.deserialize()).intValue();
		boolean isRes=((Boolean)ser.deserialize()).booleanValue();

		blog = new Blog(blodId, blogName, blodUrl, xmlRpcUrl, userName, password);
		blog.setLoadingState(loadingState);
		blog.setMaxPostCount(maxPostCount);
		blog.setResizePhotos(isRes);


		Hashtable commentStatusList= (Hashtable)ser.deserialize();
		blog.setCommentStatusList(commentStatusList);

		Hashtable pageStatusList= (Hashtable)ser.deserialize();
		blog.setPageStatusList(pageStatusList);

		Hashtable pageTemplates= (Hashtable)ser.deserialize();
		blog.setPageTemplates(pageTemplates);

		Hashtable postStatusList= (Hashtable)ser.deserialize();
		blog.setPostStatusList(postStatusList);

		Vector recentPostTitleList= (Vector)ser.deserialize();
		blog.setRecentPostTitles(recentPostTitleList);

		Vector viewedPostList= (Vector)ser.deserialize();
		blog.setViewedPost(viewedPostList);

		Vector pagesList= (Vector)ser.deserialize();
		blog.setPages(pagesList);

		int[] viewedPagesList= (int[])ser.deserialize();
		blog.setViewedPages(viewedPagesList);


		int categoryLength= ((Integer)ser.deserialize()).intValue();
		Category[] categories;

		if (categoryLength > 0) {
			categories = new Category[categoryLength];
			for (int i = 0; i < categoryLength; i++) {
				categories[i] = new Category((String)ser.deserialize(),
						(String)ser.deserialize(),
						(String)ser.deserialize(),
						((Integer)ser.deserialize()).intValue(),
						(String)ser.deserialize(),
						(String)ser.deserialize()                                              
				);
			}
			blog.setCategories(categories);
		}


		int tagsLength= ((Integer)ser.deserialize()).intValue();
		Tag[] tags ;

		if (tagsLength > 0) {
			tags  = new Tag[tagsLength];
			for (int i = 0; i < tagsLength; i++) {
				int tagID= ((Integer)ser.deserialize()).intValue();
				String tagname = (String)ser.deserialize();
				int count= ((Integer)ser.deserialize()).intValue();
				String slug= (String)ser.deserialize();
				String htmlURL=(String)ser.deserialize();
				String rssURL=(String)ser.deserialize();

				tags[i] = new Tag(tagID, tagname, count, slug, htmlURL, rssURL);
			}
			blog.setTags(tags);
		} 

		//since version 1.0.X
		try {
			Object testObj = ser.deserialize();
			//some devices when reach the end of the input stream doesn't throws EOFException, but returns null.
			if( testObj != null ) {
				boolean isCommentNotifies=((Boolean)testObj).booleanValue();
				blog.setCommentNotifies(isCommentNotifies);       	        		
			} else {
				Log.error("No comment notification info found - End of file was reached. Probably a previous blog data file is loaded" );
			}
		} catch (Exception  e) {
			Log.error("No comment notification info found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  e) {
			Log.error("No comment notification info found - End of file was reached. Probably a previous blog data file is loaded" );
		}

		try {
			Object testObj = ser.deserialize();
			if( testObj != null ) {
				boolean isLocation =((Boolean)testObj).booleanValue();
				blog.setLocation(isLocation);       	
			} else {
				Log.error("No location info found - End of file was reached. Probably a previous blog data file is loaded" );
			}
		} catch (Exception  e) {
			Log.error("No location info found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No location info found - End of file was reached. Probably a previous blog data file is loaded" );
		}


		// Read image resize dimensions
		try {
			Object testObj = ser.deserialize();
			if( testObj != null ) {
				Integer imageResizeWidth = (Integer)testObj;
				blog.setImageResizeWidth(imageResizeWidth);
			} else {
				Log.error("No image resize width found - End of file was reached. Probably a previous blog data file is loaded" );
				blog.setImageResizeWidth(new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH));
			}
		} catch (Exception  e) {
			Log.error("No image resize width found - End of file was reached. Probably a previous blog data file is loaded" );
			blog.setImageResizeWidth(new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH));
		} catch (Throwable  t) {
			Log.error("No image resize width found - End of file was reached. Probably a previous blog data file is loaded" );
			blog.setImageResizeWidth(new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH));
		}


		try {
			Object testObj = ser.deserialize();
			if( testObj != null ) {
				Integer imageResizeHeight = (Integer)testObj;
				blog.setImageResizeHeight(imageResizeHeight);
			} else {
				Log.error("No image resize height found - End of file was reached. Probably a previous blog data file is loaded" );
				blog.setImageResizeHeight(new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT));
			}
		} catch (Exception  e) {
			Log.error("No image resize height found - End of file was reached. Probably a previous blog data file is loaded" );
			blog.setImageResizeHeight(new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		} catch (Throwable  t) {
			Log.error("No image resize height found - End of file was reached. Probably a previous blog data file is loaded" );
			blog.setImageResizeHeight(new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		}

		//since version 1.2
		//reading signature data
		try {
			Object testObj = ser.deserialize();
			//some devices when reach the end of the input stream doesn't throws EOFException, but returns null.
			if( testObj != null ) {
				boolean isSignatureActive =((Boolean)testObj).booleanValue();
				blog.setSignatureEnabled(isSignatureActive);       	        		
			} 

			testObj = ser.deserialize();
			blog.setSignature((String)testObj);       	        		

		} catch (Exception  e) {
			Log.error("No signature info found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No signature info found - End of file was reached. Probably a previous blog data file is loaded" );
		}

		//reading stats auth data
		try {
			Object statsUsr = ser.deserialize();
			Object statsPasswd = ser.deserialize();
			blog.setStatsUsername((String)statsUsr);
			blog.setStatsPassword((String)statsPasswd);
		} catch (Exception  e) {
			Log.error("No stats auth data found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No stats auth data found - End of file was reached. Probably a previous blog data file is loaded" );
		}

		//since version 1.3
		//reading VideoPress resize opt
		try {
			Object testObj = ser.deserialize();
			//some devices when reach the end of the input stream doesn't throws EOFException, but returns null.
			if( testObj != null ) {
				boolean isVideoRes =((Boolean)testObj).booleanValue();
				blog.setResizeVideos(isVideoRes);       	        		
			} 
		} catch (Exception  e) {
			Log.error("No isVideoRes info found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No isVideoRes info found - End of file was reached. Probably a previous blog data file is loaded" );
		}

		try {
			Object testObj = ser.deserialize();
			blog.setVideoResizeWidth((Integer)testObj);
		} catch (Exception  e) {
			Log.error("No video resize width found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No video resize width found - End of file was reached. Probably a previous blog data file is loaded" );
		}

		try {
			Object testObj = ser.deserialize();
			blog.setVideoResizeHeight((Integer)testObj);
		} catch (Exception  e) {
			Log.error("No video resize height found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No video resize height found - End of file was reached. Probably a previous blog data file is loaded" );
		}

		//since version 1.4
		try {
			boolean isWPCOMBlog=((Boolean)ser.deserialize()).booleanValue();
			blog.setWPCOMBlog( isWPCOMBlog );
		} catch (Exception  e) {
			Log.error("No isWPCOM flag found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No isWPCOM flag found  - End of file was reached. Probably a previous blog data file is loaded" );
		}

		//reading HTTP auth data
		try {
			boolean isHttpAuth=((Boolean)ser.deserialize()).booleanValue();
			Object statsUsr = ser.deserialize();
			Object statsPasswd = ser.deserialize();
			blog.setHTTPBasicAuthRequired(isHttpAuth);
			blog.setHTTPAuthUsername((String)statsUsr);
			blog.setHTTPAuthPassword((String)statsPasswd);
		} catch (Exception  e) {
			Log.error("No http auth data found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No http auth data found - End of file was reached. Probably a previous blog data file is loaded" );
		}


		//since version 1.4.1
		//reading blog options
		try {
			Hashtable options= (Hashtable)ser.deserialize();
			blog.setBlogOptions(options);
		} catch (Exception  e) {
			Log.error("No blog options found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No blog options found - End of file was reached. Probably a previous blog data file is loaded" );
		}

		//reading WP.COM blog features
		try {
			Hashtable options = (Hashtable)ser.deserialize();
			blog.setWpcomFeatures(options);
		} catch (Exception  e) {
			Log.error("No WP.COM features found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No WP.COM features found - End of file was reached. Probably a previous blog data file is loaded" );
		}
		
		//since version 1.4.3
		//reading PostFormats
		try {
			Hashtable postFormats = (Hashtable)ser.deserialize();
			blog.setPostFormats(postFormats);
		} catch (Exception  e) {
			Log.error("No PostFormats features found - End of file was reached. Probably a previous blog data file is loaded" );
		} catch (Throwable  t) {
			Log.error("No PostFormats found - End of file was reached. Probably a previous blog data file is loaded" );
		}

		in.close();
		return blog;  
	}

	private static synchronized Hashtable loadBlogs() throws ControlledAccessException, IOException {
		Log.debug(">>> loadBlogs");
		Hashtable blogs = AppDAO.loadSecuredAppData(AppDAO.BLOGS_DATA_ID);
		Log.debug("<<< loadBlogs");
		return blogs;
	}
   
    public static synchronized void removeBlog(BlogInfo blog)  throws IOException, RecordStoreException{
    	String blogName = getBlogFolderName(blog);
    	BlogDAO.removeBlog(blogName);  
    }
    
    public static synchronized void removeBlog(String  blogName)  throws IOException, RecordStoreException{
    	Hashtable loadBlogs = loadBlogs();
    	String filePath=AppDAO.getBaseDirPath()+blogName;
    	
    	if (loadBlogs.get(blogName) == null){
    		throw new IOException("Cannot remove this blog: " + blogName + " because does not exist!");
    	}   
    	JSR75FileSystem.removeFile(filePath);
    	loadBlogs.remove(blogName);
    	AppDAO.storeSecuredAppData(AppDAO.BLOGS_DATA_ID, loadBlogs);
    }
    
    /**
     * Calculate a MD5 hash of the blog object fields. The hash is the location 
     * of the blog in the filesystem.
     * @param blogIdentifier
     * @return
     * @throws UnsupportedEncodingException 
     */
    protected static synchronized String getBlogFolderName(Blog blog) throws UnsupportedEncodingException{
    	if (blog == null) return null;
    	return getBlogFolderName(blog.getId(), blog.getXmlRpcUrl());
    }
    
    protected static synchronized String getBlogFolderName(String blogID, String xmlRpcUrl) throws UnsupportedEncodingException{
    	if(xmlRpcUrl==null || xmlRpcUrl.equals("") || blogID==null || blogID.equals(""))
			return null;
		
    	String union = blogID.concat(xmlRpcUrl);
    	MD5 md5 = new MD5();
	    md5.Update(union, null);
	    String hash = md5.asHex();
	    md5.Final();
	    return hash+"/"; //as directory we return with ending trail slash
    }
    
    protected static synchronized String getBlogFolderName(BlogInfo blog) throws UnsupportedEncodingException{
    	if (blog == null) return null;
    	return getBlogFolderName(blog.getId(), blog.getXmlRpcUrl()); 	   
    }

    public static synchronized byte[] getBlogIco(Blog blog) throws IOException, RecordStoreException {
    	String blogNameMD5 = BlogDAO.getBlogFolderName(blog);
    	String icoFilePath = AppDAO.getBaseDirPath() + blogNameMD5 + SHORT_ICO_FILE;
    	if (!JSR75FileSystem.isFileExist(icoFilePath)){
    		return null;
    	}   	
    	return  JSR75FileSystem.readFile(icoFilePath);
    }
    
	public static synchronized void setBlogIco(Blog blog, byte[] shortIco) throws IOException, RecordStoreException {
		String blogNameMD5 = BlogDAO.getBlogFolderName(blog);
    	String icoFilePath = AppDAO.getBaseDirPath() + blogNameMD5 + SHORT_ICO_FILE;
    	JSR75FileSystem.removeFile(icoFilePath); 
    	if(shortIco != null)
    		JSR75FileSystem.write(icoFilePath, shortIco); 	
	}
}
