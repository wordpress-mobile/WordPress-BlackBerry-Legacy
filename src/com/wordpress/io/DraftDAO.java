package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Blog;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.Post;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;

public class DraftDAO implements BaseDAO{
	
    //remove a draft post from the storage
    public static void removePost(Blog blog, int draftId) throws IOException, RecordStoreException {
    	String draftFilePath = getPostFilePath(blog, draftId);  	
    	JSR75FileSystem.removeFile(draftFilePath);
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
        				String newDirPath; 
        				
        				if(path.endsWith(".rem")) { //check for device content protection
        					newDirPath = StringUtils.replaceLast(path, ".rem", "");
        				} else
        					newDirPath = path;
        				
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
	        				
	        				if(path.endsWith(".rem")) { //check for device content protection
	        					path = StringUtils.replaceLast(path, ".rem", "");
	        				}
	        				
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
		Log.trace("found file: "+path);
		//check that is not a photo file 
		if(path.indexOf('p') == -1 ) { 
			try {
				if(path.endsWith(".rem")) { //check for device content protection
					path = StringUtils.replaceLast(path, ".rem", "");
				}
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
		if(post.getIsPhotoResizing() != null) {
			content.put("IsPhotoResizing", post.getIsPhotoResizing());
		}
		if(post.getImageResizeWidth() != null) {
			content.put("imageResizeWidth", post.getImageResizeWidth());
		}
		if(post.getImageResizeHeight() != null) {
			content.put("imageResizeHeight", post.getImageResizeHeight());
		}
		if(post.getCustomFields() != null ){
			content.put("custom_fields", post.getCustomFields());
		}
        
		//convert media object before save them
		Vector mediaObjects = post.getMediaObjects();
		Vector hashedMediaIbjects = new Vector(mediaObjects.size());
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			hashedMediaIbjects.addElement(tmp.serialize());
			}
		content.put("mediaObjects", hashedMediaIbjects);
		
        content.put("mt_convert_breaks", post.isConvertLinebreaksEnabled() ? "1" : "0");
        content.put("mt_allow_comments", new Integer(post.isCommentsEnabled() ? 1 : 0));
        content.put("mt_allow_pings", new Integer(post.isTrackbackEnabled() ? 1 : 0));
        content.put("isLocation", new Boolean(post.isLocation()));
        content.put("isLocationPublic", new Boolean(post.isLocationPublic()));

		if(post.isSignatureEnabled() != null) {
			content.put("isSignatureActive", post.isSignatureEnabled());
		}
        String signature = post.getSignature();
		if(signature != null) {
			content.put("signature", signature);
		}
		return content;
	}	
	
	public static synchronized Post hashtable2Post(Hashtable postData, Blog blog) {
		Post post = new Post(blog);
		
        post.setId(Tools.decodeString(postData.get("postid")));
        post.setTitle((String) postData.get("title"));
        post.setAuthor((String) postData.get("userid"));
        post.setBody((String) postData.get("description"));
        post.setExtendedBody((String) postData.get("mt_text_more"));
        post.setExcerpt((String) postData.get("mt_excerpt"));
        Date date_created_gmt = (Date) postData.get("date_created_gmt");
        if (date_created_gmt != null )
        	post.setAuthoredOn(date_created_gmt.getTime());
        
        post.setTags( (String) postData.get("mt_keywords"));
        post.setPassword((String) postData.get("wp_password"));
        post.setStatus((String) postData.get("post_status"));
        
        
        String link = (String) postData.get("link");
        if (link != null ) {
            post.setLink(link);
        }
        
        String breaks = (String) postData.get("mt_convert_breaks");
        if (breaks != null && !breaks.equals("__default__")) {
            post.setConvertLinebreaksEnabled(breaks.equals("1"));
        }
        
        Integer comments = (Integer) postData.get("mt_allow_comments");
        if (comments != null) {
            post.setCommentsEnabled(comments.intValue() != 0);
        }
        Integer trackback = (Integer) postData.get("mt_allow_pings");
        if (trackback != null) {
            post.setTrackbackEnabled(trackback.intValue() != 0);
        }
        
		//set the prop for photo res
		if(postData.get("IsPhotoResizing") != null) {
			post.setIsPhotoResizing((Boolean) postData.get("IsPhotoResizing"));
		}
		
		if(postData.get("imageResizeWidth") != null) {
			post.setImageResizeWidth((Integer) postData.get("imageResizeWidth"));
		}
		
		if(postData.get("imageResizeHeight") != null) {
			post.setImageResizeHeight((Integer) postData.get("imageResizeHeight"));
		}

		if(postData.get("custom_fields") != null) {
			Vector cf = (Vector) postData.get("custom_fields");
			post.setCustomFields(cf);
		}
		
		if(postData.get("mediaObjects") != null) {
			Vector hashedMediaIbjects = (Vector) postData.get("mediaObjects");
			Vector mediaObjects = new Vector(hashedMediaIbjects.size());
			for (int i = 0; i < hashedMediaIbjects.size(); i++) {
				Hashtable tmp = (Hashtable) hashedMediaIbjects.elementAt(i);
				MediaEntry tmpMedia = MediaEntry.deserialize(tmp);
				if(tmpMedia != null )
					mediaObjects.addElement(tmpMedia);
				}
		post.setMediaObjects(mediaObjects);
		}
		
		if(postData.get("isLocation") != null) {
			boolean isLocation =((Boolean)postData.get("isLocation")).booleanValue();
			post.setLocation(isLocation);    
		}
		
		if(postData.get("isLocationPublic") != null) {
			boolean isLocationPublic =((Boolean)postData.get("isLocationPublic")).booleanValue();
			post.setLocationPublic(isLocationPublic);    
		}
		
		if(postData.get("isSignatureActive") != null) {
			Boolean isSignatureActive = ((Boolean)postData.get("isSignatureActive"));
			post.setSignatureEnabled(isSignatureActive);
		}
		
		if(postData.get("signature") != null) {
			post.setSignature((String)postData.get("signature"));
		} 
		
        return post;
	}
}