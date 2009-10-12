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
import com.wordpress.model.Page;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;

public class PageDAO implements BaseDAO{
		
	    //remove a draft PAGE from the storage
	public static void removePage(Blog blog, int draftId) throws IOException, RecordStoreException {
		String draftFilePath = getPageFilePath(blog, draftId);  	
		JSR75FileSystem.removeFile(draftFilePath);
	/*	String[] draftPostPhotoList = getPagePhotoList(blog, draftId);
		for (int i = 0; i < draftPostPhotoList.length; i++) {
			removePagePhoto(blog, draftId, draftPostPhotoList[i]);
		}
		*/
	}
	/*    
    public static String getPhotoRealPath(Blog blog, int draftId, String photoName) throws IOException, RecordStoreException {
    	String draftPostPath = getPageFilePath(blog, draftId);
    	String photoFilePath = draftPostPath+"p-"+photoName;
    	return photoFilePath;
    }
    
	//load a photos of the draft post
	public static byte[] loadPagePhoto(Blog blog, int draftId, String photoName) throws IOException, RecordStoreException {
		String photoFilePath = getPhotoRealPath(blog, draftId, photoName);
		return JSR75FileSystem.readFile(photoFilePath);
	}
	
	//delete a photos of the draft post
	public static void removePagePhoto(Blog blog, int draftId, String photoName)  throws IOException, RecordStoreException{
		String photoFilePath = getPhotoRealPath(blog, draftId, photoName);
		JSR75FileSystem.removeFile(photoFilePath);
		System.out.println("deleting page photo ok");   	
	}
	
	/*
	public static void storePhotoFast(Blog blog, int draftId,  String completePath, String photoName) throws IOException, RecordStoreException {
		String draftPostPath = getPageFilePath(blog, draftId);
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
		System.out.println("writing page photo ok");   	
	}
	
	
	
	//store a photos of the draft page
	//FIXME can we remove this 
	public static void storePhoto(Blog blog, int draftId, byte[] photoData, String photoName) throws IOException, RecordStoreException {
		String draftPostPath = getPageFilePath(blog, draftId);
		JSR75FileSystem.createFile(draftPostPath);
		String photoFilePath = draftPostPath+"p-"+photoName;
		JSR75FileSystem.createFile(photoFilePath);    	
		DataOutputStream out = JSR75FileSystem.getDataOutputStream(photoFilePath);
		out.write(photoData);
		out.close();
		System.out.println("writing page photo ok");   	
	}
	
	
	//retrive name of the page photos files
		public static String[] getPagePhotoList(Blog blog, int draftId) throws IOException, RecordStoreException {
	    	String blogDraftsPath=getPath(blog);
		
		String[] listDraftFolder = JSR75FileSystem.listFiles(blogDraftsPath);
			Vector listDir= new Vector();
	    	for (int i = 0; i < listDraftFolder.length; i++) {
	    		String path=listDraftFolder[i];
	    		if (!path.endsWith("/")) { //found files
	    			if(!isPageFile(path) && path.startsWith( String.valueOf(draftId) )) { //found draft photo
	    				
        				if(path.endsWith(".rem")) { //check for device content protection
        					path = StringUtils.replaceLast(path, ".rem", "");
        				} 
        				String newDirPath = path.substring(path.indexOf('-')+1, path.length());
	    				listDir.addElement(newDirPath); 
	    			}
	    		}
			}
	    	return Tools.toStringArray(listDir);   	
	}
	*/
	public static int storePage(Blog blog, Page page, int draftId) throws IOException, RecordStoreException {
		String draftFilePath = getPageFilePath(blog, draftId);
		int newPostID= getDraftPageID(blog, draftId);
		JSR75FileSystem.createFile(draftFilePath);
		
		DataOutputStream out = JSR75FileSystem.getDataOutputStream(draftFilePath);
		Serializer ser= new Serializer(out);
		ser.serialize(page2Hashtable(page));
		out.close();
		Log.trace("Page obj stored into memory");
		return newPostID;
	}
	
	
	
	//retrive all page from the storage 
	public static Hashtable loadPage(Blog blog, String fileName) throws IOException, RecordStoreException {
    	String blogDraftsPath=getPath(blog);    	
		String currPageFile = fileName;  		
		String draftFile = blogDraftsPath + currPageFile;
		DataInputStream in = JSR75FileSystem.getDataInputStream(draftFile);
		Serializer ser= new Serializer(in);
		Hashtable page = (Hashtable) ser.deserialize();
		in.close();
    	return page;   	
	}
	
	//retrive all page fileName from the storage 
	public static String[] loadPagesFileName(Blog blog) throws IOException, RecordStoreException {
    	String blogDraftsPath=getPath(blog);

   		String[] listDraftFolder = JSR75FileSystem.listFiles(blogDraftsPath);
    		Vector listDir= new Vector();
        	for (int i = 0; i < listDraftFolder.length; i++) {
        		String path=listDraftFolder[i];
        		if (!path.endsWith("/")) { //found files
        			if(isPageFile(path)) { //found page file
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
		
	
		
	//retrive blog pages Folder
	private static String getPath(Blog blog) throws RecordStoreException, IOException{
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
		String blogDraftsPath=AppDAO.getBaseDirPath()+blogNameMD5+BlogDAO.PAGE_FOLDER_PREFIX;
		return blogDraftsPath;
	}
	
	
	//retrive the page file path
	private static String getPageFilePath(Blog blog, int draftId) throws RecordStoreException, IOException {
		String blogDraftsPath=getPath(blog);
		String draftFolder = blogDraftsPath + String.valueOf(getDraftPageID(blog, draftId));
		return draftFolder;
	}
	
	//return the page draft id
	private static int getDraftPageID(Blog blog, int draftId)  throws IOException, RecordStoreException{	
	    	String blogDraftsPath=getPath(blog);
	    	
	    	if (draftId == -1) {
	    		String[] listDraftFolder = JSR75FileSystem.listFiles(blogDraftsPath);
	    		Vector listDir= new Vector();
	        	for (int i = 0; i < listDraftFolder.length; i++) {
	        		String path=listDraftFolder[i];
	        		if (!path.endsWith("/")) { //found file
	        			if(isPageFile(path)) { //found draft file
	        				
	        				if(path.endsWith(".rem")) { //check for device content protection
	        					path = StringUtils.replaceLast(path, ".rem", "");
	        				}
	        				
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
	
	/**
	 * photo files contains the prefix "p"...
	 * @param path
	 * @return
	 */
	private static boolean isPageFile(String path){
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
	
	//returns array of Page builded from vector returned by wp.getPages response
	public static Page[] buildPagesArray(Vector respVector){
		
		if( respVector == null )
			return new Page[0];
		
		Page[] myPageList =new Page[respVector.size()]; //my page object list
		
		for (int i = 0; i < respVector.size(); i++) {
			Hashtable returnCommentData = (Hashtable)respVector.elementAt(i);
			Page page = PageDAO.hashtable2Page(returnCommentData);
			myPageList[i]= page;
		}
		return myPageList;
	}
	
	//return Page obj builded from the Hashtable returned by wp.getPage response
	public static synchronized Page hashtable2Page(Hashtable returnPageData) {
		
		Page page = new Page();

		//Date dateCreated = ((Date) returnPageData.get("dateCreated"));
		//page.setDateCreated(dateCreated);
		
		Date dateCreated = ((Date) returnPageData.get("date_created_gmt"));
		page.setDateCreatedGMT(dateCreated);
		
		if( returnPageData.get("userid") != null)
			page.setUserID(Tools.decodeInt(returnPageData.get("userid")));
		
		if( returnPageData.get("page_id") != null )
			page.setPageId(Tools.decodeInt(returnPageData.get("page_id")));

		page.setPageStatus((String) returnPageData.get("page_status"));
		String description = ((String) returnPageData.get("description"));
		page.setDescription(description);
		String title = ((String) returnPageData.get("title"));
		page.setTitle(title);
		
		page.setLink((String) returnPageData.get("link"));
		page.setPermaLink((String) returnPageData.get("permaLink"));
		page.setCategories((Vector) returnPageData.get("categories"));
		page.setMt_excerpt((String) returnPageData.get("excerpt"));
		page.setMtTextMore((String) returnPageData.get("text_more"));

		
		if (returnPageData.get("mt_allow_comments") != null) {
			String comments = String.valueOf( returnPageData.get("mt_allow_comments") );
			page.setCommentsEnabled(Integer.parseInt(comments) != 0);
		}
				
		if ( returnPageData.get("mt_allow_pings") != null) {
			String pings = String.valueOf( returnPageData.get("mt_allow_pings"));
			page.setPingsEnabled(Integer.parseInt(pings) != 0);
		}
		
		page.setWpSlug((String) returnPageData.get("wp_slug"));
		page.setWpPassword((String) returnPageData.get("wp_password"));
		page.setWpAuthor( (String) returnPageData.get("wp_author"));
		
	
		if(returnPageData.get("wp_page_parent_id") != null ) {
			String parentID = String.valueOf(returnPageData.get("wp_page_parent_id"));
			page.setWpPageParentID(Integer.parseInt(parentID));
		}
	
		page.setWpPageParentTitle((String) returnPageData.get("wp_page_parent_title"));

		if(returnPageData.get("wp_page_order") != null){
			String pageOrder = String.valueOf( returnPageData.get("wp_page_order") );
			page.setWpPageOrder(Integer.parseInt(pageOrder));
		}
		
		if(returnPageData.get("wp_author_id") != null) {
			String pageAuthorID = String.valueOf( returnPageData.get("wp_author_id") );
			page.setWpAuthorID(Integer.parseInt(pageAuthorID));
		}
		
		//set the prop for photo res
		if(returnPageData.get("IsPhotoResizing") != null) {
			page.setIsPhotoResizing((Boolean) returnPageData.get("IsPhotoResizing"));
		}
		
		page.setWpAuthorDisplayName((String) returnPageData.get("wp_author_display_name"));
		Vector cf=(Vector) returnPageData.get("custom_fields");
		page.setCustomFields(cf);
		
		page.setWpPageTemplate((String) returnPageData.get("wp_page_template"));
		
		
		if(returnPageData.get("mediaObjects") != null) {
			Vector hashedMediaIbjects = (Vector) returnPageData.get("mediaObjects");
			Vector mediaObjects = new Vector(hashedMediaIbjects.size());
			for (int i = 0; i < hashedMediaIbjects.size(); i++) {
				Hashtable tmp = (Hashtable) hashedMediaIbjects.elementAt(i);
				mediaObjects.addElement(new MediaEntry(tmp));
			}
			page.setMediaObjects(mediaObjects);
		}
		
		
		
		return page;
	}
	
	public static synchronized Hashtable page2Hashtable(Page page) {
		Hashtable content = new Hashtable();
		
	//	if (page.getDateCreated()!= null)
	//		content.put("dateCreated", page.getDateCreated());
		
		if(page.getUserID() != -1)
			content.put("userid", new Integer(page.getUserID()));
		if(page.getID() != -1)
			content.put("page_id", new Integer(page.getID()));
		if (page.getPageStatus()!= null)
			content.put("page_status", page.getPageStatus());
		if (page.getDescription()!= null)
			content.put("description", page.getDescription());
		if (page.getTitle()!= null)
			content.put("title", page.getTitle());
		if (page.getLink()!= null)
			content.put("link", page.getLink());
		if (page.getPermaLink()!= null)
			content.put("permaLink", page.getPermaLink());
		if (page.getMt_excerpt()!= null)
			content.put("mt_excerpt", page.getMt_excerpt());
		if (page.getMtTextMore()!= null)
			content.put("mt_text_more", page.getMtTextMore());
		content.put("mt_allow_comments", new Integer(page.isCommentsEnabled() ? 1 : 0));
		content.put("mt_allow_pings", new Integer(page.isPingsEnabled() ? 1 : 0));
		
		if (page.getWpSlug()!= null)
			content.put("wp_slug", page.getWpSlug());
		if (page.getWpPassword()!= null)
			content.put("wp_password", page.getWpPassword());
		if (page.getWpAuthor()!= null)
			content.put("wp_author", page.getWpAuthor());
		
		if(page.getWpPageParentID() != -1)
			content.put("wp_page_parent_id", new Integer(page.getWpPageParentID()));
	
		if(page.getWpPageParentTitle() !=null)
		content.put("wp_page_parent_title", page.getWpPageParentTitle());
		
		if(page.getWpPageOrder() != -1)
			content.put("wp_page_order", new Integer(page.getWpPageOrder()));
		
		if(page.getWpAuthorID() != -1)
			content.put("wp_author_id", new Integer(page.getWpAuthorID()));

		if(page.getDateCreatedGMT() !=null)
			content.put("date_created_gmt", page.getDateCreatedGMT());
		
		if(page.getCustomFields() !=null)
			content.put("custom_fields", page.getCustomFields());
		
		if(page.getWpPageTemplate() !=null)
			content.put("wp_page_template", page.getWpPageTemplate());
		
		if(page.getIsPhotoResizing() !=null)
			content.put("IsPhotoResizing", page.getIsPhotoResizing());
	
		
		//convert media object before save them
		Vector mediaObjects = page.getMediaObjects();
		Vector hashedMediaIbjects = new Vector(mediaObjects.size());
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			hashedMediaIbjects.addElement(tmp.getMediaObjectAsHashtable());
			}
		content.put("mediaObjects", hashedMediaIbjects);
		
		return content;
	}
	
}

