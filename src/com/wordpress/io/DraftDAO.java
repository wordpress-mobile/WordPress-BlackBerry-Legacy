package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Blog;
import com.wordpress.model.Post;
import com.wordpress.utils.Tools;

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
        
    //load a photos of the draft post
	public static byte[] loadPostPhoto(Post draftPost, int draftId, String photoName) throws IOException, RecordStoreException {
    	String draftPostPath = getPostFilePath(draftPost.getBlog(), draftId);
    	String photoFilePath = draftPostPath+"p-"+photoName;
    	return JSR75FileSystem.readFile(photoFilePath);
	}
	
    //delete a photos of the draft post
	public static void removePostPhoto(Blog blog, int draftId, String photoName)  throws IOException, RecordStoreException{
    	String draftBlogPath = getPath(blog);
    	String photoFilePath = draftBlogPath+draftId+"p-"+photoName;
    	JSR75FileSystem.removeFile(photoFilePath);
		System.out.println("deleting draft photo ok");   	
	}
    
    //store a photos of the draft post
	public static void storePostPhoto(Post draftPost, int draftId, byte[] photoData, String photoName) throws IOException, RecordStoreException {
    	String draftPostPath = getPostFilePath(draftPost.getBlog(), draftId);
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
    	ser.serialize(draftPost.getId());
    	ser.serialize(draftPost.getTitle());
    	ser.serialize(draftPost.getAuthor());
    	ser.serialize(draftPost.getAuthoredOn());
    	ser.serialize(draftPost.getPassword());
    	ser.serialize(draftPost.getBody());
    	ser.serialize(draftPost.getStatus());
    	ser.serialize(draftPost.getCategories());
    	ser.serialize(draftPost.getTags());
    	out.close();
		System.out.println("writing draft post ok");
		return newPostID;
	}
	
	
	//retrive drafts post info 
	public static Hashtable getPostsInfo(Blog blog) throws IOException, RecordStoreException {
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
   	
        	Hashtable infos= new Hashtable(listDir.size());
        	
        	for (int i = 0; i < listDir.size(); i++) {
        		String currPostFile = (String)listDir.elementAt(i);
        		Post loadDraftPost = loadPost(blog, Integer.parseInt(currPostFile));
        		String title = loadDraftPost.getTitle();
    		    if (title == null || title.length() == 0) {
    		    	title = "No title";
    		    }

        		infos.put(currPostFile,title);
			}
        	return infos;   	
	}
	
	
	
	//retrive draft post by id from storage
	public static Post loadPost(Blog blog, int draftId) throws IOException, RecordStoreException {
		String blogDraftsPath=getPath(blog);
		String draftFile = blogDraftsPath + String.valueOf(draftId);
    	DataInputStream in = JSR75FileSystem.getDataInputStream(draftFile);
    	
    	Serializer ser= new Serializer(in);
    	String id = (String) ser.deserialize();
    	String title = (String) ser.deserialize();
    	String author = (String) ser.deserialize();
    	Date authOn = (Date) ser.deserialize();
    	String password = (String) ser.deserialize();
    	String body = (String) ser.deserialize();
    	String status = (String) ser.deserialize();
    	int[] cat = (int[])ser.deserialize();
    	String tags = (String)ser.deserialize();
    	Post draft= new Post(blog,id,title,author,authOn);
    	draft.setBody(body);
    	draft.setCategories(cat);
    	draft.setTags(tags);
    	draft.setPassword(password);
    	draft.setStatus(status);
    	in.close();
    	System.out.println("loading draft post ok");
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
		        			listDir.addElement(Integer.valueOf(path)); //draft folder are label as  1, 2, 3, ...
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
		if(path.indexOf('p') == -1 ) return true;
		return false;
	}
	
}
