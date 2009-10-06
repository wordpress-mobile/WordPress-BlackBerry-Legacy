package com.wordpress.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.bb.WordPressCore;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
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
	
	public static Bitmap defaultGravatarBitmap = Bitmap.getBitmapResource("gravatar.png"); //i've copied resource here for performances
	private Hashtable commentsGravatar = new Hashtable(); //the email as key and img bytes as value
	private GetGravatarTask gvtTask = null;
	private Blog currentBlog;

	public GravatarController(Blog blog) {
		this.currentBlog = blog; 
	}
		
	public void startGravatarTask(final Vector _elements) {
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				Hashtable missingGravatar = new Hashtable();
				try {
					commentsGravatar = CommentsDAO.loadGravatars(currentBlog); //load prev stored gravat
					if(commentsGravatar == null ) 
						commentsGravatar = new Hashtable();
					else
						notifyObservers(null); //all gvt are loaded from disk
					
					int elementLength = _elements.size();
					//Populate the missing hashtable
					for(int count = 0; count < elementLength; ++count)
					{
						String authorEmail = (String)_elements.elementAt(count);
						if (!authorEmail.equalsIgnoreCase("") && !commentsGravatar.containsKey(authorEmail)) {
							Log.debug("email is missing:" +authorEmail);   
							missingGravatar.put(authorEmail, ""); //put the email for gravatar download
						} else
							Log.debug("email is already present:" +authorEmail);
					}
					
					GravatarCallBack gravatarCallBack = new GravatarCallBack();
					Enumeration keys = missingGravatar.keys();
					gvtTask = new GetGravatarTask(keys);
					gvtTask.setProgressListener(gravatarCallBack);
					//push into the Runner
					WordPressCore.getInstance().getTasksRunner().enqueue(gvtTask);
					
				} catch (IOException e) {
					Log.error("Error while reading gravatars "+e.getMessage());
				} catch (RecordStoreException e) {
					Log.error("Error while reading gravatars "+e.getMessage());
				}
				
			}//and run 
		});
		
	}	
	
	   public Bitmap getLatestGravatar(String authorEmail) {
		   Bitmap gravatarBitmap = null;
		   if (authorEmail == null || authorEmail.length() == 0 || authorEmail.equalsIgnoreCase(""))
			   gravatarBitmap = defaultGravatarBitmap;
		   else {
			   byte[] img = (byte[]) commentsGravatar.get(authorEmail);
			   if(img != null && img.length > 0) {
				   gravatarBitmap = EncodedImage.createEncodedImage(img, 0, -1).getBitmap();
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
		   }
	   }
	   
	   private class GravatarCallBack implements TaskProgressListener {
		   
		   public void taskComplete(Object obj) {
			   try {
				   CommentsDAO.storeGravatars(currentBlog, commentsGravatar);
			   } catch (IOException e) {
				   Log.error("Error while storing gravatars "+e.getMessage());	
			   } catch (RecordStoreException e) {
				   Log.error("Error while storing gravatars "+e.getMessage());
			   }
		   }
		   
		   public void taskUpdate(Object obj) {
			   
			   Hashtable content = (Hashtable)obj;
			   String email = (String) content.get("email");
			   byte[] img = null;
			   
			   if(content.get("bits") != null)
				   img = (byte[]) content.get("bits");
			   else
				   img = new byte[0]; //put an empty array.
			   
			   commentsGravatar.put(email, img);
			   
			   notifyObservers(email); //One gvt is loaded, notify observers
			   
		   }
	   }
}