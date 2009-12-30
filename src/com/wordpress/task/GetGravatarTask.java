package com.wordpress.task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import com.wordpress.utils.MD5;
import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Log;

public class GetGravatarTask extends TaskImpl {
	
	private  Enumeration emailsGravatar = null ; //the email as key for gravatar

	public GetGravatarTask(Enumeration keys) {
		super();
		this.emailsGravatar = keys;
	}
	
	public void execute() {
		Log.debug("starting gravatar thread");
		//check if there is img to download
		if (emailsGravatar == null) {
			if(progressListener != null)
				progressListener.taskComplete(null);
			return;
		}
		
		//load all gravatar img
		for (; emailsGravatar.hasMoreElements(); ) {
			
			if(stopping == true) {
				Log.debug("gravatar task was stopped");
				return;
			}
			
			String authorEmail = ((String)emailsGravatar.nextElement()).toLowerCase();
			String hashAuthorEmail = null;
			
			MD5 md5 = new MD5();
			try {
				md5.Update(authorEmail, null);
				hashAuthorEmail = md5.asHex();
				md5.Final();
			} catch (UnsupportedEncodingException e) {
				Log.error(e, "Error while hashing email for gravatar services");
				continue;
			}
			
			String gravURL = "http://www.gravatar.com/avatar/"+hashAuthorEmail+"?s=36&d=404";
			Log.debug("Requesting gravatar for the email: "+ authorEmail);
			Log.debug("Open gravatar url "+ gravURL);
			
			HttpConnection conn = null;
			Hashtable content = new Hashtable(2);			
			content.put("email", authorEmail);
			
			try {
				conn = (HttpConnection) ConnectionManager.getInstance().open(gravURL);
				int rc = conn.getResponseCode();
				InputStream in = null;
				if( rc == HttpConnection.HTTP_OK ){					
					//read the response
					byte[] img = null;
					in = conn.openInputStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					int c;
					while ((c = in.read()) >= 0)
					{
						baos.write(c);
					}
					img = baos.toByteArray();
					content.put("bits", img);					
					
					Log.debug("Ended Gravatar request for the email: "+ authorEmail);
				} else if( rc == HttpConnection.HTTP_NOT_FOUND) {
					Log.debug("Gravatar for email "+ authorEmail+ " not found");
				} else {
					Log.error("Error reading gravatar for email "+ authorEmail);
					Log.error("Error reading gravatar at URL "+ gravURL+ " server response code:"+rc);
				}
				
				if(progressListener != null)
					progressListener.taskUpdate(content);
				
				//closing the connection
				try {
				    if (conn != null) conn.close();
				    if (in != null) in.close();
				} catch (IOException ioe) {
					Log.error(ioe, "Error while closing gravatar http conn");
				} finally {
					Log.trace("gravatar Input/Ouput Stream  set to null");
				    conn = null;
				    in = null;
				}
				
			} catch (Exception e) {
				if(progressListener != null)
					progressListener.taskComplete(null);
				Log.error("Error reading gravatars");
				return; //exit imediately 
			}		
		}//end for
		
		if(progressListener != null)
			progressListener.taskComplete(null);
		
	}

}