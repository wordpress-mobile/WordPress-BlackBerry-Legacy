package com.wordpress.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Category;
import com.wordpress.model.Tag;
import com.wordpress.utils.MD5;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;

public class BlogDAO implements BaseDAO {
	
	//You might wonder what happens when a static synchronized method is invoked, since a static method is 
	//associated with a class, not an object. 
	//In this case, the thread acquires the intrinsic lock for the Class object associated with the class. 
		
	
	/**
     * add One  blog to the storage!
     * @param blog
     * @param overwrite 
     * @return
	 * @throws Exception If a blog with the same xmlrpc url and ID already exist in the app
     */
    public static synchronized boolean newBlog(Blog blog, boolean overwrite) throws Exception{
    	String name = blog.getName();
    	String nameMD5=getBlogFolderName(blog);
    	String filePath=AppDAO.getBaseDirPath()+nameMD5;
    
    	if (JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot add this blog: " + name + " because another blog with same name already exist!");
    	} else {
    		JSR75FileSystem.createDir(AppDAO.getBaseDirPath()); 
    		JSR75FileSystem.createDir(filePath);
    		JSR75FileSystem.createDir(filePath+DRAFT_FOLDER_PREFIX); //create draft posts folder
    		JSR75FileSystem.createDir(filePath+PAGE_FOLDER_PREFIX); //create draft page folder
    	}    	
    	
    	JSR75FileSystem.createFile(filePath+BLOG_FILE); //create the blog file
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(filePath+BLOG_FILE);
    	storeBlog(blog, out);
    	    	
		out.close();
		Log.trace("Scrittura blog on memory terminata con successo");   	
    	return true;
    }
    
    
	/**
     * Update all blog info in the storage
     * @param aBlog
     * @return
     */
    public static synchronized boolean updateBlog(Blog blog) throws Exception {   	
    	String name = blog.getName();
    	String nameMD5=getBlogFolderName(blog);
    	String filePath=AppDAO.getBaseDirPath()+nameMD5;
    
    	if (!JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot update this blog: " + name + " because not exist!");
    	}  	
    	
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(filePath+BLOG_FILE);
    	storeBlog(blog, out);
    	Log.debug("blog updated succesfully");    	    	
		out.close(); //already closed during store
    	return true;
    }
    
    /**
     * Load the blogs informations from the fs.  
     * @return An Hashtable with the following keys: "list" type of BlogInfo[] - "error" type of String
     * @throws IOException
     * @throws RecordStoreException
     */
	public static synchronized Hashtable getBlogsInfo() throws IOException, RecordStoreException {
		
		Hashtable result = new Hashtable();
				
		String[] listFiles = new String[0];
		BlogInfo[] blogs = new BlogInfo[0];

		try {
			listFiles = BlogDAO.getBlogsPath();
		} catch (IOException e2) {
			Log.error(e2, "Error while loading blogs index");
			throw new IOException("Error while loading blogs index -> " + e2.getMessage());
		} catch (RecordStoreException e2) {
			Log.error(e2, "Error while loading blogs index");
			throw new RecordStoreException("Error while loading blogs index -> " + e2.getMessage());
		}

		Vector blogsVector = new Vector();
		for (int i = 0; i < listFiles.length; i++) {
			String currBlogPath = listFiles[i];
			
			try {
				BlogInfo currBlogInfo = BlogDAO.getBlogInfo(currBlogPath);
				blogsVector.addElement(currBlogInfo);
			} catch (Exception e) {
				
				Log.error(e, "Error while loading blog: "+ currBlogPath);
			//	Log.error("Trying to delete the blog with fs corrupted: "+ currBlogPath);				 
			/*	try {
					BlogDAO.removeBlog(currBlogPath);
					Log.error("The blog with fs corrupted was deleted : "+ currBlogPath);
				} catch (Exception e1) {
					Log.error(e, "Error while deleting blog: "+ currBlogPath);
				} 
				*/
				result.put("error", "The folder "+ currBlogPath +" is corrupted. Please delete it");
			}
		}
		
		blogs = new BlogInfo[blogsVector.size()];
		for (int i = 0; i < blogs.length; i++) {
			blogs[i] = (BlogInfo) blogsVector.elementAt(i);
		}		
		
		result.put("list",blogs);
		
		return result;
	}
    
    
    //TODO refactor blog and blogInfo. with a common base class
    //reload this blog form disk to memory
    public static synchronized Blog getBlog(Blog blogInfo) throws Exception {
        try {
        	String blogName = getBlogFolderName(blogInfo);
            return loadBlog(blogName);
        } catch (Exception e) {
        	throw new Exception("Failed to load blog: " + e.getMessage());            
        }
    }
    
    public static synchronized Blog getBlog(BlogInfo blogInfo) throws Exception {
        try {
        	//String blogName = getBlogsPath()[aIndex];
        	String blogName = getBlogFolderName(blogInfo);
            return loadBlog(blogName);
        } catch (Exception e) {
        	throw new Exception("Failed to load blog: " + e.getMessage());            
        }
    }
    
    public static synchronized String[] getBlogsPath() throws IOException, RecordStoreException{
    	String[] listFilesAndDir = JSR75FileSystem.listFiles(AppDAO.getBaseDirPath());
    	Vector listDir= new Vector();
    	
    	for (int i = 0; i < listFilesAndDir.length; i++) {
    		String path=listFilesAndDir[i];
    		if (path.endsWith("/")) { //found directory
    			listDir.addElement(path);
    		}
		}
    	return Tools.toStringArray(listDir);
    }
    
    /**
     * Retrive a small set of infos for blog
     * @return
     * @throws Exception
     */
    public static synchronized BlogInfo getBlogInfo(String blogPath) throws Exception{
		Blog loadedBlog = loadBlog(blogPath);
		String blogName = loadedBlog.getName();
		String blogXmlRpcUrl=loadedBlog.getXmlRpcUrl();
		String blogId= loadedBlog.getId();
		int blogLoadingState = loadedBlog.getLoadingState();
		boolean notifies= loadedBlog.isCommentNotifies();
		String usr = loadedBlog.getUsername();
		String passwd = loadedBlog.getPassword();
		BlogInfo blogI = new BlogInfo(blogId, blogName,blogXmlRpcUrl,usr, passwd, blogLoadingState, notifies);
   		return blogI;
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
    
	private static synchronized void storeBlog(Blog blog, DataOutputStream out)
			throws IOException {
		
		String wholeErrorMessage = ""; 
		Serializer ser= new Serializer(out);
		boolean isError = false;
		
		ser.serialize(new Integer(blog.getLoadingState()));
    	ser.serialize(blog.getXmlRpcUrl());
    	ser.serialize(blog.getUsername());
    	ser.serialize(blog.getPassword());
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
        
        out.close();

        //if there was an errors
		if(isError) {
			throw new IOException(wholeErrorMessage);
		}
	}
    
   
    private static synchronized Blog loadBlog(String name) throws Exception {
    	Log.debug("loading blog " + name + " from file system");
    	
    	String filePath=AppDAO.getBaseDirPath()+name;
        
    	if (!JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot load this blog: " + name + " because not exist!");
    	}   	
    	
    	DataInputStream in = JSR75FileSystem.getDataInputStream(filePath+BLOG_FILE);
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
        		
        } catch (EOFException  e) {
        	Log.error("No comment notification info found - End of file was reached. Probably a previous blog data file is loaded" );
		}
        
        try {
        	Object testObj = ser.deserialize();
        	if( testObj != null ) {
        		boolean isLocation =((Boolean)testObj).booleanValue();
        		blog.setLocation(isLocation);       	
        	} else {
        		Log.error("No comment notification info found - End of file was reached. Probably a previous blog data file is loaded" );
        	}
        } catch (EOFException  e) {
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
        		blog.setImageResizeWidth(new Integer(640));
        	}
        } catch (EOFException  e) {
        	Log.error("No image resize width found - End of file was reached. Probably a previous blog data file is loaded" );
        	blog.setImageResizeWidth(new Integer(640));
		}

        try {
        	Object testObj = ser.deserialize();
        	if( testObj != null ) {
        		Integer imageResizeHeight = (Integer)testObj;
        		blog.setImageResizeHeight(imageResizeHeight);
        	} else {
        		Log.error("No image resize height found - End of file was reached. Probably a previous blog data file is loaded" );
        		blog.setImageResizeHeight(new Integer(480));
        	}
        } catch (EOFException  e) {
        	Log.error("No image resize height found - End of file was reached. Probably a previous blog data file is loaded" );
        	blog.setImageResizeHeight(new Integer(480));
		}

        in.close();
        return blog;     
     } 
    
    public static synchronized void removeBlog(BlogInfo blog)  throws IOException, RecordStoreException{
    	String blogName = getBlogFolderName(blog);
    	 BlogDAO.removeBlog(blogName);  
    }
    
    public static synchronized void removeBlog(String  blogName)  throws IOException, RecordStoreException{
    	String filePath=AppDAO.getBaseDirPath()+blogName;
    	
    	if (!JSR75FileSystem.isFileExist(filePath)){
    		throw new IOException("Cannot delete this blog: " + blogName + " because not exist!");
    	} else {
			JSR75FileSystem.removeFile(filePath);
    	}    
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

}
