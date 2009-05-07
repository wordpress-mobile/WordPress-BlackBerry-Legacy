package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.model.Blog;
import com.wordpress.model.Category;
import com.wordpress.model.Tag;
import com.wordpress.utils.Preferences;

public class WordPressDAO {
	
	public static String BASE_PATH = "file:///store/home/user/wordpress/";
	public static String BLOGS_BASE_PATH = BASE_PATH+"blogs/";
	public static String INST_FILE= BASE_PATH+"inst"; //check if the app is installed
	public static String APP_PREFS_FILE= BASE_PATH+"prefs"; //check if the app is installed
	
	/**
     * add One  blog to the storage!
     * @param aBlog
     * @return
     */
    public static boolean newBlog(Blog blog, boolean overwrite) throws Exception{
    	String name = blog.getBlogName();   	
    	String filePath=BLOGS_BASE_PATH+name+"/";
    
    	if (JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot add this blog: " + name + " because another blog with same name already exist!");
    	} else {
    		JSR75FileSystem.createDir(BASE_PATH); //FIXME recursive
    		JSR75FileSystem.createDir(BLOGS_BASE_PATH); //FIXME recursive
			JSR75FileSystem.createDir(filePath);
    	}    	
    	
    	JSR75FileSystem.createFile(filePath+"blog"); //create the blog file
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(filePath+"blog");
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
    	String name = blog.getBlogName();   	
    	String filePath=BLOGS_BASE_PATH+name+"/";
    
    	if (!JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot update this blog: " + name + " because not exist!");
    	}  	
    	
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(filePath+"blog");
    	storeBlog(blog, out);
    	System.out.println("blog updated succesfully");    	    	
		out.close();
    	return true;
    }
    
    
    public static Blog getBlog(int aIndex) throws Exception {
        try {
        	String blogName = getBlogsName()[aIndex];
            return loadBlog(blogName);
        } catch (Exception e) {
        	throw new Exception("Failed to load blog: " + e.getMessage());            
        }
    }
    
    public static String[] getBlogsName() throws IOException{
    	String[] listFiles = JSR75FileSystem.listFiles(BLOGS_BASE_PATH);
    	for (int i = 0; i < listFiles.length; i++) {
    		String path=listFiles[i];
    		if (path.endsWith("/")) {
    			listFiles[i]= path.substring(0,path.length()-1);
    		}
		}
    	return listFiles;
    }

	private static void storeBlog(Blog blog, DataOutputStream out)
			throws IOException {
		Serializer ser= new Serializer(out);
    	
    	ser.serialize(blog.getBlogXmlRpcUrl());
    	ser.serialize(blog.getUsername());
    	ser.serialize(blog.getPassword());
    	ser.serialize(blog.getBlogId());
    	ser.serialize(blog.getBlogName());
    	ser.serialize(blog.getBlogUrl());
    	ser.serialize(new Integer(blog.getMaxPostCount()));
    	ser.serialize(new Boolean(blog.isResizePhotos()));
    	ser.serialize(blog.getPageStatusList());
    	ser.serialize(blog.getPostStatusList());
    	ser.serialize(blog.getRecentPostTitles());
    	
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
      
    	String filePath=BLOGS_BASE_PATH+name+"/";
        
    	if (!JSR75FileSystem.isFileExist(filePath)){
    		throw new Exception("Cannot load this blog: " + name + " because not exist!");
    	}   	
    	
    	DataInputStream in = JSR75FileSystem.getDataInputStream(filePath+"blog");
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
    
    public static void removeBlog(String name) throws IOException{
    	String filePath=BLOGS_BASE_PATH+name+"/";
    	if (!JSR75FileSystem.isFileExist(filePath)){
    		throw new IOException("Cannot delete this blog: " + name + " because not exist!");
    	} else {
			JSR75FileSystem.removeFile(filePath);
    	}    
    }
    
    public static boolean readApplicationPreferecens(Preferences pref) throws IOException {
		System.out.println(">>>load application preferences");

		if (!JSR75FileSystem.isFileExist(APP_PREFS_FILE)) {
			return false;
		}

		DataInputStream in = JSR75FileSystem.getDataInputStream(APP_PREFS_FILE);
		Serializer ser = new Serializer(in);

		int tzOffsetIndex = ((Integer) ser.deserialize()).intValue();
		int localeIndex = ((Integer) ser.deserialize()).intValue();
		String videoEncoding = (String) ser.deserialize();
		String audioEncoding = (String) ser.deserialize();
		String photoEncoding = (String) ser.deserialize();
		boolean deviceSideConnection = ((Boolean) ser.deserialize()).booleanValue();
		
		pref.setAudioEncoding(audioEncoding);
		pref.setDeviceSideConnection(deviceSideConnection);
		pref.setLocaleIndex(localeIndex);
		pref.setPhotoEncoding(photoEncoding);
		pref.setTimeZoneIndex(tzOffsetIndex);
		pref.setVideoEncoding(videoEncoding);
		System.out.println("Prefs loading succesfully!");
		
		in.close();
		
		return true;
	}
    
    public static void storeApplicationPreferecens(Preferences pref) throws IOException {
		System.out.println(">>>store application preferences");

	  	JSR75FileSystem.createFile(APP_PREFS_FILE); 
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(APP_PREFS_FILE);
    	
    	int tzOffsetIndex = pref.getTimeZoneIndex();
		int localeIndex = pref.getLocaleIndex();
		String videoEncoding = pref.getVideoEncoding();
		String audioEncoding = pref.getAudioEncoding();
		String photoEncoding = pref.getPhotoEncoding();
		boolean deviceSideConnection = pref.isDeviceSideConnection();
		
		Serializer ser= new Serializer(out);
    	ser.serialize(new Integer(tzOffsetIndex));
    	ser.serialize(new Integer(localeIndex));
    	ser.serialize(videoEncoding);
    	ser.serialize(audioEncoding);
    	ser.serialize(photoEncoding);
    	ser.serialize(new Boolean(deviceSideConnection));
    	
    	out.close();
		System.out.println("Prefs stored succesfully!");
	}
    
    

}
