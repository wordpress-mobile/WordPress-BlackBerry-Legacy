package com.wordpress.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.task.AsyncRunner;
import com.wordpress.task.GetGravatarTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;

/**
 * Load the comment gravatar imgs from a list of email.
 * 
 * - notify aboserver with null, when gravatar are loaded from disk
 * - notify aboserver with String (email) the gravatar of that email are ready 
 * 
 * @author dercoli
 *
 */
public class GravatarController extends Observable {
	
	public static EncodedImage defaultGravatarBitmap= EncodedImage.getEncodedImageResource("gravatar.png");
	
	//the email as key and Encoded img as value -- used for store gvt into disk
	private Hashtable commentsGravatar = new Hashtable(); 

	private GetGravatarTask gvtTask = null;
	private Blog currentBlog;
	private boolean running = false;
	private GravatarTaskListener gravatarCallBack = null;


	public GravatarController(Blog blog) {
		this.currentBlog = blog; 
		//load the previous gravatar from disk
		try {
			commentsGravatar = CommentsDAO.loadGravatars(currentBlog); //load prev stored gravat
			if(commentsGravatar == null ) 
				commentsGravatar = new Hashtable();
			
		} catch (IOException e) {
			Log.error("Error while reading gravatars "+e.getMessage());
			commentsGravatar = new Hashtable();
		} catch (RecordStoreException e) {
			Log.error("Error while reading gravatars "+e.getMessage());
			commentsGravatar = new Hashtable();
		}
	}
		
	public boolean isRunning() {
		return running;
	}
	
	public void startGravatarTask(final Vector _elements) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				 Log.debug(">>> startGravatarTask");
				Hashtable missingGravatar = new Hashtable();
						
					int elementLength = _elements.size();
					//Populate the missing hashtable
					for(int count = 0; count < elementLength; ++count)
					{
						String authorEmail = ((String)_elements.elementAt(count)).toLowerCase();
						if (!authorEmail.equalsIgnoreCase("") && !commentsGravatar.containsKey(authorEmail)) {
							Log.trace("email is missing:" +authorEmail);   
							missingGravatar.put(authorEmail, ""); //put the email for gravatar download
						} else
							Log.trace("email is already present:" +authorEmail);
					}
					
					gravatarCallBack = new GravatarTaskListener();
					Enumeration keys = missingGravatar.keys();
					gvtTask = new GetGravatarTask(keys);
					gvtTask.setProgressListener(gravatarCallBack);
					
					/*push into the Runner
					WordPressCore.getInstance().getTasksRunner().enqueue(gvtTask);
					*/
					AsyncRunner asyncRunner = new AsyncRunner(gvtTask);
					asyncRunner.start();
					running = true;
				
			}//and run 
		});
	}	
	
	
	public boolean isGravatarAvailable(String authorEmail) {
		   if (authorEmail == null || authorEmail.length() == 0 || authorEmail.equalsIgnoreCase("")) {
			   return false;
		   } else {
			   Object gvtObj = commentsGravatar.get(authorEmail.toLowerCase());
			   if(gvtObj != null && gvtObj instanceof EncodedImage) {
				   return true;
			   } else {
				   return false;
			   }
		   }
	   }
	
	   public EncodedImage getLatestGravatar(String authorEmail) {
		   EncodedImage gravatarBitmap = null;
		   authorEmail = authorEmail.toLowerCase();
	//	   Log.trace("getLatestGravatar on email :" +authorEmail);
		   if (authorEmail == null || authorEmail.length() == 0 || authorEmail.equalsIgnoreCase("")) {
			   gravatarBitmap = defaultGravatarBitmap;
		   } else {
			   Object gvtObj = commentsGravatar.get(authorEmail);
			   if(gvtObj != null && gvtObj instanceof EncodedImage) {
				   EncodedImage img = (EncodedImage) commentsGravatar.get(authorEmail);
				   gravatarBitmap = img;
			   } else {
				   gravatarBitmap = defaultGravatarBitmap;
			   }
		   }
		   return gravatarBitmap;
	   }
	   
	   
	   public void stopGravatarTask() {
		   if(gvtTask != null) {
			   
			   gvtTask.setProgressListener(null); //remove the listener so no update where done into gravatar cache
			   gvtTask.stop();
			   gvtTask = null;

			   if(gravatarCallBack != null) {
				   gravatarCallBack.taskComplete(null); //store the partial data into memory
				   gravatarCallBack = null;
			   }
			   
		   }
		   running = false;
	   }
	   
	   //remove all the cache
	   public void cleanGravatarCache(){
		   Log.debug(">>> cleanGravatarCache");
		   CommentsDAO.cleanGravatarCache(currentBlog);
		   //commentsGravatar = new Hashtable();
		   commentsGravatar.clear();
	   }
	   
	   //remove only the selected elements from the gravatar cache
	   public void cleanGravatarCache(String emails[]){
		   Log.debug(">>> cleanGravatarCache -- emails[]");
		   CommentsDAO.cleanGravatarCache(currentBlog);
		   for (int i = 0; i < emails.length; i++) {
			   commentsGravatar.remove(emails[i]);	
			   Log.debug("removed gravatar for :"+emails[i]);
		   }
		   try {
			   CommentsDAO.storeGravatars(currentBlog, commentsGravatar);
		   } catch (IOException e) {
			   Log.error("Error while storing gravatars "+e.getMessage());	
		   } catch (RecordStoreException e) {
			   Log.error("Error while storing gravatars "+e.getMessage());
		   }
	   }
	   
	   private class GravatarTaskListener implements TaskProgressListener {
		   private boolean isModified = false;
		   
		   public void taskComplete(Object obj) {
			   running = false;		
			   if(isModified == false) 
				   return;
			   else
				   isModified = false;

			   try {
				   CommentsDAO.storeGravatars(currentBlog, commentsGravatar);
			   } catch (IOException e) {
				   Log.error("Error while storing gravatars "+e.getMessage());	
			   } catch (RecordStoreException e) {
				   Log.error("Error while storing gravatars "+e.getMessage());
			   }
		   }
		   
		   public void taskUpdate(Object obj) {
			   isModified = true;
			   Hashtable content = (Hashtable)obj;
			   String email = (String) content.get("email");
			   EncodedImage img = null;
			   
			   if(content.get("bits") != null) { 
				   byte[] imgBytes = null;
				   imgBytes = (byte[]) content.get("bits");
				   try {
					   img = EncodedImage.createEncodedImage(imgBytes, 0, -1);
					   commentsGravatar.put(email, img);
					} catch (Exception e) {
						Log.error(e, "gravatar for "+email+" is corrupted, using default gvt");
						commentsGravatar.put(email, ""); //put an empty string into the hashtable
					}
			   } else {
				   commentsGravatar.put(email, ""); //put an empty string into the hashtable
			   }
			   notifyObservers(email); //One gvt is loaded, notify observers
		   }
	   }
}