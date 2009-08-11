package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;

public class DraftDAO implements BaseDAO{
	
    //remove a draft post from the storage
    public static void removePost(Blog blog, int draftId) throws IOException, RecordStoreException {
    	String draftFilePath = getPostFilePath(blog, draftId);  	
    	JSR75FileSystem.removeFile(draftFilePath);
    	String[] draftPostPhotoList = getPostPhotoList(blog, draftId);
    	for (int i = 0; i < draftPostPhotoList.length; i++) {
    		removePostPhoto(blog, draftId, draftPostPhotoList[i]);
		}
    }
        
    public static String getPhotoRealPath(Blog blog, int draftId, String photoName) throws IOException, RecordStoreException {
    	String draftPostPath = getPostFilePath(blog, draftId);
    	String photoFilePath = draftPostPath+"p-"+photoName;
    	return photoFilePath;
    }
    //load a photos of the draft post
	public static byte[] loadPostPhoto(Blog blog, int draftId, String photoName) throws IOException, RecordStoreException {
    	String photoFilePath = getPhotoRealPath(blog, draftId, photoName);
    	return JSR75FileSystem.readFile(photoFilePath);
	}
	
    //delete a photos of the draft post
	public static void removePostPhoto(Blog blog, int draftId, String photoName)  throws IOException, RecordStoreException{
    	String photoFilePath = getPhotoRealPath(blog, draftId, photoName);
    	JSR75FileSystem.removeFile(photoFilePath);
		System.out.println("deleting draft photo ok");   	
	}
    
    //store a photos of the draft post
	public static void storePhotoFast(Blog blog, int draftId, String completePath, String photoName) throws IOException, RecordStoreException {
    	String draftPostPath = getPostFilePath(blog, draftId);
    	JSR75FileSystem.createFile(draftPostPath);
    	String photoFilePath = draftPostPath+"p-"+photoName;
    	JSR75FileSystem.createFile(photoFilePath);    	
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(photoFilePath);
    
    	//read from the source and store the photo
    	FileConnection source;
    	if(!completePath.startsWith("file:///")) {
    		source = (FileConnection) Connector.open("file:///" + completePath, Connector.READ);
    	} else {
    		source = (FileConnection) Connector.open(completePath, Connector.READ);
    	}
    	  
    	InputStream inStream = source.openInputStream();
    	byte[] buffer = new byte[1024];
    	int length = -1;
    	while ((length = inStream.read(buffer)) > 0) {
    		out.write(buffer, 0 , length);
    	}
    	
    	inStream.close();
    	out.close();
		System.out.println("writing draft photo ok");   	
	}
		
    //store a photos of the draft post 
	//FIXME can we remove this 
	public static void storePhoto(Blog blog, int draftId, byte[] photoData, String photoName) throws IOException, RecordStoreException {
    	String draftPostPath = getPostFilePath(blog, draftId);
    	JSR75FileSystem.createFile(draftPostPath);
    	String photoFilePath = draftPostPath+"p-"+photoName;
    	JSR75FileSystem.createFile(photoFilePath);    	
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(photoFilePath);
    	out.write(photoData);
    	out.close();
		System.out.println("writing draft photo ok");   	
	}
	
	
	//retrive name of draft photos files
	public static String[] getPostPhotoList(Blog blog, int draftId) throws IOException, RecordStoreException {
    	String blogDraftsPath=getPath(blog);
   // 	String postFile=getDraftFilePath(draftPost, draftId);
    	
   		String[] listDraftFolder = JSR75FileSystem.listFiles(blogDraftsPath);
    		Vector listDir= new Vector();
        	for (int i = 0; i < listDraftFolder.length; i++) {
        		String path=listDraftFolder[i];
        		if (!path.endsWith("/")) { //found files
        			if(!isPostFile(path) && path.startsWith( String.valueOf(draftId) )) { //found draft photo
        				String newDirPath = path; 
        				newDirPath = path.substring(path.indexOf('-')+1, path.length());
        				listDir.addElement(newDirPath); 
        			}
        		}
    		}
        	return Tools.toStringArray(listDir);   	
	}
    
	public static int storePost(Post draftPost, int draftId) throws IOException, RecordStoreException {
    	String draftFilePath = getPostFilePath(draftPost.getBlog(), draftId);
    	int newPostID= getDraftPostID(draftPost.getBlog(), draftId);
    	JSR75FileSystem.createFile(draftFilePath);    	
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(draftFilePath);
    	Serializer ser= new Serializer(out);
    	Hashtable post2Hashtable = post2Hashtable(draftPost);
    	ser.serialize(post2Hashtable);
    	ser.serialize(draftPost.getCategories());
    	out.close();
		System.out.println("writing draft post ok");
		return newPostID;
	}
	
	
	//retrive drafts index from disk 
	public static String[]  getPostsInfo(Blog blog) throws IOException, RecordStoreException {
    	String blogDraftsPath=getPath(blog);

   		String[] listDraftFolder = JSR75FileSystem.listFiles(blogDraftsPath);
    		Vector listDir= new Vector();
        	for (int i = 0; i < listDraftFolder.length; i++) {
        		String path=listDraftFolder[i];
        		if (!path.endsWith("/")) { //found files
        			if(isPostFile(path)) { //found draft file
        				String newDirPath = path; 
        				listDir.addElement(newDirPath); //draft file are label as  1, 2, 3, ...
        			}
        		}
    		}
   	
    	String[] files = new String[listDir.size()];
        listDir.copyInto(files);
        return files;
        	
	}
	
		
	//retrive draft post by id from storage
	public static Post loadPost(Blog blog, int draftId) throws IOException, RecordStoreException {
		String blogDraftsPath=getPath(blog);
		String draftFile = blogDraftsPath + String.valueOf(draftId);
    	DataInputStream in = JSR75FileSystem.getDataInputStream(draftFile);
 
    	Serializer ser= new Serializer(in);
    	Hashtable postHas = (Hashtable) ser.deserialize();
    	int[] postCat = (int[]) ser.deserialize();
    	Post draft = hashtable2Post(postHas, blog);
    	draft.setCategories(postCat);
    	in.close();
 
    	Log.trace("loading draft post ok");
    	return draft;		
	}


	//retrive blog draftsFolder
	private static String getPath(Blog blog) throws RecordStoreException, IOException{
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String blogDraftsPath=AppDAO.getBaseDirPath()+blogNameMD5+BlogDAO.DRAFT_FOLDER_PREFIX;
    	return blogDraftsPath;
	}
	
	
	//retrive the draft post file
	private static String getPostFilePath(Blog blog, int draftId) throws RecordStoreException, IOException {
		
    	String blogDraftsPath=getPath(blog);
    	String draftFolder = blogDraftsPath + String.valueOf(getDraftPostID(blog, draftId));
    	
		return draftFolder;
	}
	
	private static int getDraftPostID(Blog blog, int draftId)  throws IOException, RecordStoreException{	
	    	String blogDraftsPath=getPath(blog);
	    	
	    	//retrive the post file name
	    	//file:///store/home/user/wordpress/blogs/blogname/drafts/draftID/
	    	if (draftId == -1) {
	    		String[] listDraftFolder = JSR75FileSystem.listFiles(blogDraftsPath);
	    		Vector listDir= new Vector();
	        	for (int i = 0; i < listDraftFolder.length; i++) {
	        		String path=listDraftFolder[i];
	        		if (!path.endsWith("/")) { //found file
	        			if(isPostFile(path)) { //found draft file
		        			listDir.addElement(Integer.valueOf(path)); //draft files are label as  1, 2, 3, ...
	        			}
	        		}
	    		}
	        	//find the max int
	        	int max=-1;
	        	for (int i = 0; i < listDir.size(); i++) {
					int tmp= ((Integer)listDir.elementAt(i)).intValue();
					if(tmp > max) max = tmp;
				}
	        	max = max+ 1; //new draft folder
	        	draftId=max;
	    	} 
	    	return draftId;
	}
	
	private static boolean isPostFile(String path){
		//check that is not a photo file 
		if(path.indexOf('p') == -1 ) { 
			try {
				Integer.valueOf(path);
				return true;
			} catch (NumberFormatException numExc){
				return false;
			}
		}
		return false;
	}
	
	public static synchronized Hashtable post2Hashtable(Post post) {
		
        Hashtable content = new Hashtable();
        if (post.getId() != null) {
        	 content.put("postid", post.getId());
        }
        if (post.getTitle() != null) {
            content.put("title", post.getTitle());
        }
        if (post.getBody() != null) {
            content.put("description", post.getBody());
        }
        if (post.getExtendedBody() != null) {
            content.put("mt_text_more", post.getExtendedBody());
        }
        if (post.getExcerpt() != null) {
            content.put("mt_excerpt", post.getExcerpt());
        }
        if (post.getAuthoredOn() != null) {
        	content.put("date_created_gmt", post.getAuthoredOn());
        }
        if (post.getTags() != null) {
        	content.put("mt_keywords", post.getTags());
        }
        if (post.getStatus() != null) {
        	content.put("post_status", post.getStatus());
        }
        if (post.getPassword() != null) {
        	content.put("wp_password", post.getPassword());
        }
        if (post.getLink() != null) {
        	content.put("link", post.getLink());
        }	     
            	
		if(post.getIsPhotoResizing() !=null) {
			content.put("IsPhotoResizing", post.getIsPhotoResizing());
		}
	
        
        content.put("mt_convert_breaks", post.isConvertLinebreaksEnabled() ? "1" : "0");
        content.put("mt_allow_comments", new Integer(post.isCommentsEnabled() ? 1 : 0));
        content.put("mt_allow_pings", new Integer(post.isTrackbackEnabled() ? 1 : 0));
		return content;
	}	
	
	public static synchronized Post hashtable2Post(Hashtable postData, Blog blog) {
		Post aPost = new Post(blog);
		
        aPost.setId(Tools.decodeString(postData.get("postid")));
        aPost.setTitle((String) postData.get("title"));
        aPost.setAuthor((String) postData.get("userid"));
        aPost.setBody((String) postData.get("description"));
        aPost.setExtendedBody((String) postData.get("mt_text_more"));
        aPost.setExcerpt((String) postData.get("mt_excerpt"));
        Date date_created_gmt = (Date) postData.get("date_created_gmt");
        if (date_created_gmt != null )
        	aPost.setAuthoredOn(date_created_gmt.getTime());
        
        aPost.setTags( (String) postData.get("mt_keywords"));
        aPost.setPassword((String) postData.get("wp_password"));
        aPost.setStatus((String) postData.get("post_status"));
        
        
        String link = (String) postData.get("link");
        if (link != null ) {
            aPost.setLink(link);
        }
        
        String breaks = (String) postData.get("mt_convert_breaks");
        if (breaks != null && !breaks.equals("__default__")) {
            aPost.setConvertLinebreaksEnabled(breaks.equals("1"));
        }
        
        Integer comments = (Integer) postData.get("mt_allow_comments");
        if (comments != null) {
            aPost.setCommentsEnabled(comments.intValue() != 0);
        }
        Integer trackback = (Integer) postData.get("mt_allow_pings");
        if (trackback != null) {
            aPost.setTrackbackEnabled(trackback.intValue() != 0);
        }
        
		//set the prop for photo res
		if(postData.get("IsPhotoResizing") != null) {
			aPost.setIsPhotoResizing((Boolean) postData.get("IsPhotoResizing"));
		}
        
        return aPost;
	}
}
