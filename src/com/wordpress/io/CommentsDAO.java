package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import com.wordpress.model.Blog;

public class CommentsDAO implements BaseDAO{
	
	//retrive the draft post file
	public static Vector loadComments(Blog blog) throws UnsupportedEncodingException, IOException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=BASE_PATH+blogNameMD5+COMMENTS_FILE;
    	
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
	public static void storeComments(Blog blog, Vector comments) throws UnsupportedEncodingException, IOException {
		
		String blogNameMD5=BlogDAO.getBlogFolderName(blog);
    	String commentsFilePath=BASE_PATH+blogNameMD5+COMMENTS_FILE;
    	
    	JSR75FileSystem.createFile(commentsFilePath); //create the file
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(commentsFilePath);

    	Serializer ser= new Serializer(out);
    	ser.serialize(comments);
		out.close();
		
		System.out.println("Scrittura commenti terminata con successo");   	
	}
	
	
}
