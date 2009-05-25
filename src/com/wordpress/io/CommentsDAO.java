package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Blog;
import com.wordpress.model.Comment;

public class CommentsDAO implements BaseDAO{
	
	//retrive the draft post file
	public static Vector loadComments(Blog blog) throws IOException, RecordStoreException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+COMMENTS_FILE;
    	
    	if (!JSR75FileSystem.isFileExist(commentsFilePath)){
    		return null;
    	}   	
    	
    	DataInputStream in = JSR75FileSystem.getDataInputStream(commentsFilePath);
    	Serializer ser= new Serializer(in);
    	Vector comments = (Vector) ser.deserialize();
    	
    	in.close();
    	return comments;
	}
		
	
	//store comments
	public static void storeComments(Blog blog, Vector comments) throws IOException, RecordStoreException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=AppDAO.getBaseDirPath()+blogNameMD5+COMMENTS_FILE;
    	
    	JSR75FileSystem.createFile(commentsFilePath); //create the file
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(commentsFilePath);

    	Serializer ser= new Serializer(out);
    	ser.serialize(comments);
		out.close();
		
		System.out.println("Scrittura commenti terminata con successo");   	
	}
	
	
	
	//retun array of comments from wp response
	public static synchronized Comment[] vector2Comments(Vector respVector){
		
		if( respVector == null )
			return new Comment[0];
		
		Comment[] myCommentsList =new Comment[respVector.size()]; //my comment object list
		
		for (int i = 0; i < respVector.size(); i++) {
			 Hashtable returnCommentData = (Hashtable)respVector.elementAt(i);
			 
			
			Comment comment = new Comment();
			 
			int commentID=Integer.parseInt((String)returnCommentData.get("comment_id"));
	        int commentParent=Integer.parseInt((String) returnCommentData.get("parent"));
            String status=(String) returnCommentData.get("status");
            comment.setDate_created_gmt((Date) returnCommentData.get("date_created_gmt"));
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
            myCommentsList[i]=comment; //add comment to my return list

		}
		return myCommentsList;
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
	        hash.put("date_created_gmt", currentComment.getDate_created_gmt());
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
