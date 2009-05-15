package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Blog;
import com.wordpress.model.Category;
import com.wordpress.model.Tag;
import com.wordpress.utils.MD5;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.Tools;

public class BlogDAO implements BaseDAO {
	

	
	/**
     * add One  blog to the storage!
     * @param aBlog
     * @return
	 * @throws Exception 
     */
    public static boolean newBlog(Blog blog, boolean overwrite) throws Exception{
    	String name = blog.getName();
    	String nameMD5=getBlogFolderName(blog);
    	String filePath=AppDAO.getBaseDirPath()+nameMD5;
    
    	if (JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot add this blog: " + name + " because another blog with same name already exist!");
    	} else {
    		JSR75FileSystem.createDir(AppDAO.getBaseDirPath()); 
    		JSR75FileSystem.createDir(filePath);
    		JSR75FileSystem.createDir(filePath+DRAFT_FOLDER_PREFIX); //create draft posts folder
    	}    	
    	
    	JSR75FileSystem.createFile(filePath+BLOG_FILE); //create the blog file
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(filePath+BLOG_FILE);
    	storeBlog(blog, out);
    	    	
		out.close();
		System.out.println("Scrittura terminata con successo");   	
    	return true;
    }
    
    
	/**
     * Update all blog info in the storage
     * @param aBlog
     * @return
     */
    public static boolean updateBlog(Blog blog) throws Exception{   	
    	String name = blog.getName();
    	String nameMD5=getBlogFolderName(blog);
    	String filePath=AppDAO.getBaseDirPath()+nameMD5;
    
    	if (!JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot update this blog: " + name + " because not exist!");
    	}  	
    	
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(filePath+BLOG_FILE);
    	storeBlog(blog, out);
    	System.out.println("blog updated succesfully");    	    	
		out.close();
    	return true;
    }
    
    
    public static Blog getBlog(int aIndex) throws Exception {
        try {
        	String blogName = getBlogsPath()[aIndex];
            return loadBlog(blogName);
        } catch (Exception e) {
        	throw new Exception("Failed to load blog: " + e.getMessage());            
        }
    }
    
    private static String[] getBlogsPath() throws IOException, RecordStoreException{
    	String[] listFilesAndDir = JSR75FileSystem.listFiles(AppDAO.getBaseDirPath());
    	Vector listDir= new Vector();
    	
    	for (int i = 0; i < listFilesAndDir.length; i++) {
    		String path=listFilesAndDir[i];
    		if (path.endsWith("/")) { //found directory
    			listDir.addElement( path.substring(0,path.length()-1)	);
    		}
		}
    	return Tools.toStringArray(listDir);
    }
    
    /**
     * Retrive the name of all blogs
     * @return
     * @throws Exception
     */
    public static String[] getBlogsName() throws Exception{
    	String[] listFiles = getBlogsPath();
    	String blogName[] = new String[listFiles.length];
    	
    	for (int i = 0; i < listFiles.length; i++) {
    		   		
    		Blog loadedBlog = loadBlog(listFiles[i]);
    		blogName[i]=loadedBlog.getName();
		}
    	return blogName;    	
    }
    

    
	private static void storeBlog(Blog blog, DataOutputStream out)
			throws IOException {
		Serializer ser= new Serializer(out);
    	
    	ser.serialize(blog.getXmlRpcUrl());
    	ser.serialize(blog.getUsername());
    	ser.serialize(blog.getPassword());
    	ser.serialize(blog.getId());
    	ser.serialize(blog.getName());
    	ser.serialize(blog.getUrl());
    	ser.serialize(new Integer(blog.getMaxPostCount()));
    	ser.serialize(new Boolean(blog.isResizePhotos()));
    	ser.serialize(blog.getPageStatusList());
    	ser.serialize(blog.getPostStatusList());
    	ser.serialize(blog.getRecentPostTitles());
    	ser.serialize(blog.getViewedPost());
    	
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
	}
    
   
    private static Blog loadBlog(String name) throws Exception {
    	System.out.println("carico il blog " + name + " dal file system");
    	
    	String filePath=AppDAO.getBaseDirPath()+name+"/";
        
    	if (!JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot load this blog: " + name + " because not exist!");
    	}   	
    	
    	DataInputStream in = JSR75FileSystem.getDataInputStream(filePath+BLOG_FILE);
    	Serializer ser= new Serializer(in);
    	
        Blog blog;

        String xmlRpcUrl = (String)ser.deserialize();
        String userName = (String)ser.deserialize();
        String password = (String)ser.deserialize();
        String blodId= (String)ser.deserialize();
        String blogName= (String)ser.deserialize();
        String blodUrl= (String)ser.deserialize();
        int maxPostCount= ((Integer)ser.deserialize()).intValue();
        boolean isRes=((Boolean)ser.deserialize()).booleanValue();
        
        blog = new Blog(blodId, blogName, blodUrl, xmlRpcUrl, userName, password);
        blog.setMaxPostCount(maxPostCount);
        blog.setResizePhotos(isRes);
        
        Hashtable pageStatusList= (Hashtable)ser.deserialize();
        blog.setPageStatusList(pageStatusList);
        Hashtable postStatusList= (Hashtable)ser.deserialize();
        blog.setPostStatusList(postStatusList);
        Vector recentPostTitleList= (Vector)ser.deserialize();
        blog.setRecentPostTitles(recentPostTitleList);
        Vector viewedPostList= (Vector)ser.deserialize();
        blog.setViewedPost(viewedPostList);

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
        
        in.close();
        return blog;     
     } 
    
    public static void removeBlog(int aIndex)  throws IOException, RecordStoreException{
    	String blogName = getBlogsPath()[aIndex];
    	String filePath=AppDAO.getBaseDirPath()+blogName+"/";
    	
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
    protected static String getBlogFolderName(Blog blog) throws UnsupportedEncodingException{
    	if (blog == null) return null;
    	String xmlrpcURL=blog.getXmlRpcUrl(); 
    	if(xmlrpcURL==null || xmlrpcURL.equals(""))
			return null;
		
 	    MD5 md5 = new MD5();
   	    md5.Update(xmlrpcURL, null);
   	    String hash = md5.asHex();
   	    md5.Final();
   	    return hash+"/"; //as directory we return with ending trail slash
    }

}
