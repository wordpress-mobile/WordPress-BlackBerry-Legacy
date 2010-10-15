package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.EncodedImage;

import com.wordpress.bb.WordPressCore;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Comment;
import com.wordpress.utils.log.Log;

public class CommentsDAO implements BaseDAO {
	
	public static synchronized Vector loadComments(Blog blog) throws IOException, RecordStoreException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+COMMENTS_FILE;
    	
    	if (!JSR75FileSystem.isFileExist(commentsFilePath)){
    		return null;
    	}   	
    	
		FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
		DataInputStream in = fc.openDataInputStream();
    	Serializer ser= new Serializer(in);
    	Vector comments = (Vector) ser.deserialize();
    	in.close();
    	fc.close();
    	return comments;
	}
		
	
	//store comments, no update in the app blog list needs here
	public static synchronized void storeComments(BlogInfo blog, Vector comments) throws IOException, RecordStoreException {
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	storeCommentsToDisk(blogNameMD5, comments);  	
	}
	
	//store comments and updates comment waiting notification info
	public static synchronized void storeComments(Blog blog, Vector comments) throws IOException, RecordStoreException {
		Log.trace(">>> storeComments(Blog blog, Vector comments)");
		Hashtable vector2Comments = CommentsDAO.vector2Comments(comments);
		Comment[] tmp = (Comment[]) vector2Comments.get("comments");
		if(vector2Comments.get("error") == null) {
			upgradeMainAppCommentsNumber(blog, tmp);	
		}
		tmp = null;
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
		storeCommentsToDisk(blogNameMD5, comments);
		Log.trace("<<< storeComments(Blog blog, Vector comments)");
	}
	
	//store comments and updates comment waiting notification info
	public static synchronized void storeComments(Blog blog, Comment[] comments) throws IOException, RecordStoreException {
		Log.trace(">>> storeComments(Blog blog, Comment[] comments)");
		upgradeMainAppCommentsNumber(blog, comments);
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
		storeCommentsToDisk(blogNameMD5, CommentsDAO.comments2Vector(comments));
		Log.trace("<<< storeComments(Blog blog, Comment[] comments)");
	}
	
	
	private static void upgradeMainAppCommentsNumber(Blog blog, Comment[] comments){
		Log.trace(">>> upgradeMainAppCommentsNumber");
		
		int count = 0;
		for (int i = 0; i < comments.length; i++) {
			Comment tmp = comments[i];
			if(	tmp.getStatus().equalsIgnoreCase("hold") )
			count++;
		}
		Log.trace("awaiting comments # :"+count);
		
		//upgrade awaiting notification #				
		Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();
		for (int i = 0; i < applicationBlogs.size(); i++) {
			BlogInfo blogInfo = (BlogInfo) applicationBlogs.elementAt(i);
			if( blogInfo.getXmlRpcUrl().equalsIgnoreCase(blog.getXmlRpcUrl())
					&& blogInfo.getId().equalsIgnoreCase(blog.getId()) 
					) {
				Log.trace("updated blog with xmlrpc url :"+blogInfo.getXmlRpcUrl());		
				blogInfo.setAwaitingModeration(count);
			}
		}
		Log.trace("<<< upgradeMainAppCommentsNumber");
	}
	

	private static void storeCommentsToDisk(String blogNameMD5, Vector comments) throws IOException, RecordStoreException{
		
		String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+COMMENTS_FILE;
    	
    	JSR75FileSystem.createFile(commentsFilePath); //create the file
    	FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
    	DataOutputStream out = fc.openDataOutputStream();
    	Serializer ser= new Serializer(out);
    	ser.serialize(comments);
    	out.close();
    	fc.close();
		Log.debug("Scrittura commenti terminata con successo"); 
	}
	
	
	public static Hashtable loadGravatars(Blog blog) throws IOException, RecordStoreException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+GRAVATARS_FILE;
    	
    	if (!JSR75FileSystem.isFileExist(commentsFilePath)){
    		return null;
    	}   	
    	
		FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
		DataInputStream in = fc.openDataInputStream();
    	Serializer ser= new Serializer(in);
    	Hashtable gvt = (Hashtable) ser.deserialize();
    	in.close();
    	fc.close();
    	
    	Log.debug("Gravatars loaded from device memory");
    	Log.trace(">>> start Creating imgs for Gravatars");
       	Hashtable storeGvt = new Hashtable();
    	Enumeration keys = gvt.keys();
    	for(; keys.hasMoreElements(); ){
    		String nextElement = (String)keys.nextElement();
    		Log.trace("Creating imgs for Gravatar "+ nextElement);
    		if (gvt.get(nextElement) instanceof byte[]) {
    			byte[] imgBytes = null;
    			imgBytes = (byte[]) gvt.get(nextElement);
    			try {
    				EncodedImage img = EncodedImage.createEncodedImage(imgBytes, 0, -1);
    				storeGvt.put(nextElement, img);
    			} catch (Exception e) {
    				Log.error(e, "img gravatar for "+nextElement+" is corrupted, using default gvt");
    			}
    		} else {
    			storeGvt.put(nextElement, "");
    		}
    	}
    	return storeGvt;
	}
		
	
	//store comments
	public static synchronized void storeGravatars(Blog blog, Hashtable gvt) throws IOException, RecordStoreException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+GRAVATARS_FILE;
    	
    	JSR75FileSystem.createFile(commentsFilePath); //create the file
    	FileConnection fc = JSR75FileSystem.openFile(commentsFilePath);
    	DataOutputStream out = fc.openDataOutputStream();

    	Log.trace(">>> storeGravatars starts storing imgs Gravatar");
    	Hashtable storeGvt = new Hashtable();
    	Enumeration keys = gvt.keys();
    	for(; keys.hasMoreElements(); ){
    		String nextElement = (String)keys.nextElement();
    		
    		if(gvt.get(nextElement) instanceof EncodedImage ) {
    			EncodedImage tmpImg = (EncodedImage)gvt.get(nextElement);
    			byte[] data = tmpImg.getData();
    			storeGvt.put(nextElement, data);
    		} else {
    			storeGvt.put(nextElement, "");
    		}
    		Log.trace(">>> storing  bytes of the imgs gravatar "+ nextElement);
    	}
    	
    	Serializer ser= new Serializer(out);
    	ser.serialize(storeGvt);
		out.close();
		fc.close();
		Log.debug("Gravatars stored into device memory");   	
	}
	
	public static synchronized void cleanGravatarCache(Blog currentBlog){
		//we should clean the gravatars cached imgs
		try {
			CommentsDAO.storeGravatars(currentBlog, new Hashtable());
		} catch (IOException e) {
			Log.error("Error while cleaning gravatars cache"+e.getMessage());
		} catch (RecordStoreException e) {
			Log.error("Error while cleaning gravatars cache"+e.getMessage());
		}
	}
	
	
	/**
	 *  Get comments Objs from wp response
	 *   
	 * @param respVector - the Vector containing the response from the server 
	 * @return an Hashtable with the following keys: "comments", and optionally "error" - if there was an error during the 
	 * building of the comments obj
	 */
	public static synchronized Hashtable vector2Comments(Vector respVector){
		Hashtable response = new Hashtable(2);
		String errorMsg = null;
		
		if( respVector == null ) {
			response.put("comments", new Comment[0]);
			return response;
		}
		
		Vector commentObjList = new Vector();

		for (int i = 0; i < respVector.size(); i++) {
			try {
				Hashtable returnCommentData = (Hashtable)respVector.elementAt(i);
				 
				Comment comment = new Comment();
				 
				int commentID=Integer.parseInt((String)returnCommentData.get("comment_id"));
		        int commentParent=Integer.parseInt((String) returnCommentData.get("parent"));
	            String status=(String) returnCommentData.get("status");
	            comment.setDateCreatedGMT((Date) returnCommentData.get("date_created_gmt"));
	            comment.setUserId( (String)returnCommentData.get("user_id") ) ;
	            comment.setID(commentID);
	            comment.setParent(commentParent);
	            comment.setStatus(status);
	            comment.setContent( (String) returnCommentData.get("content") );
	            comment.setLink((String) returnCommentData.get("link"));
	            comment.setPostID(Integer.parseInt((String)returnCommentData.get("post_id")));
	            comment.setPostTitle((String) returnCommentData.get("post_title"));
	            comment.setAuthor((String) returnCommentData.get("author"));
	            comment.setAuthorEmail((String) returnCommentData.get("author_email"));
	            comment.setAuthorUrl((String) returnCommentData.get("author_url"));
	            comment.setAuthorIp((String) returnCommentData.get("author_ip"));
	            commentObjList.addElement(comment); //add comment to my return list
			} catch(Exception e) {
				errorMsg = e.getMessage();
				response.put("error", errorMsg);
			}
		}
		
		Comment[] myCommentsList =new Comment[commentObjList.size()]; //my comment object list
		commentObjList.copyInto(myCommentsList);
		response.put("comments", myCommentsList);
		return response;
	}
	
	public static synchronized Vector comments2Vector(Comment[] comments){
		Vector commentsVector= new Vector();
		if( comments == null )
			return commentsVector;
		
		for (int i = 0; i < comments.length; i++) {
			Comment currentComment = comments[i];
	        Hashtable hash = new Hashtable(13);
	        hash.put("comment_id", String.valueOf(currentComment.getID()));
	        hash.put("parent", String.valueOf(currentComment.getParent()));
	        hash.put("status", currentComment.getStatus());
	        if(currentComment.getDateCreatedGMT() != null)
	        	hash.put("date_created_gmt", currentComment.getDateCreatedGMT());
	        hash.put("user_id", currentComment.getUserId());
	        hash.put("content", currentComment.getContent());
	        hash.put("link", currentComment.getLink());
	        hash.put("post_id", String.valueOf(currentComment.getPostID()));
	        hash.put("author", currentComment.getAuthor());
	        hash.put("post_title", currentComment.getPostTitle());
	        hash.put("author_email", currentComment.getAuthorEmail());
	        hash.put("author_url", currentComment.getAuthorUrl());
	        hash.put("author_ip", currentComment.getAuthorIp());
	        commentsVector.addElement(hash);        
		}
		return commentsVector;
	}
	
	
}
