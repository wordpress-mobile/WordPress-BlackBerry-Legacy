package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.NotificationHandler;
import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Preferences;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.DataCollector;
import com.wordpress.utils.log.Log;
import com.wordpress.view.MainView;


public class MainController extends BaseController implements TaskProgressListener{
	
	private MainView view = null;
	private Vector applicationBlogs = null;
	
	public MainController() {
		super();
		WordPressCore wpCore = WordPressCore.getInstance();
		applicationBlogs = wpCore.getApplicationBlogs();
	}
	
	public void showView(){
		
		
		int numberOfBlog = 0; 
		
		//reset the state of blogs that are in loading or queue to loading error state
		//.... maybe app crash during adding blog
		Log.trace(">>> Checking blogs data");
	   	 try {
	   		Hashtable blogsInfo = BlogDAO.getBlogsInfo();
			BlogInfo[] blogsList =  (BlogInfo[]) blogsInfo.get("list");
			if(blogsInfo.get("error") != null )
				displayError((String)blogsInfo.get("error"));
						
			for (int i = 0; i < blogsList.length; i++) {
				BlogInfo blogInfo = blogsList[i];
				Blog blog = BlogDAO.getBlog(blogInfo);
			
				if (blog.getLoadingState() == BlogInfo.STATE_LOADING
						|| blog.getLoadingState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
					blog.setLoadingState(BlogInfo.STATE_LOADED_WITH_ERROR);
					BlogDAO.updateBlog(blog);
				}
				
				applicationBlogs.addElement(blogInfo);
			}
			numberOfBlog = blogsList.length;  //get the number of blog
		} catch (Exception e) {
			Log.error(e, "Error while reading stored blog");
		}
		Log.trace("<<< Checking blogs data");

		//stats and update stuff!
		try {
			DataCollector dtc = new DataCollector();
			dtc.collectData(numberOfBlog); //start data gathering here
		} catch (Exception e) {
			//don't propagate this Exception
		}

		//schedule the update check task at startup
		WordPressCore.getInstance().getTimer().schedule(new CheckUpdateTask(), 24*60*60*1000, 24*60*60*1000); //24h check
		//set the notification screen
		NotificationHandler.getInstance().setGuiController(this);
		
		this.view=new MainView(this); //main view init here!.	
		UiApplication.getUiApplication().pushScreen(this.view);
	}
	

	private class CheckUpdateTask extends TimerTask {
		public void run() {
			try {
				Log.trace("CheckUpdateTask");
				DataCollector dtc = new DataCollector();
				if (!dtc.isCollectedDataExpired()){
					Log.trace("delay time is not over");
					return;
				}
				dtc.collectData(applicationBlogs.size()); //start data gathering here
			} catch (Throwable  e) {
				cancel();
				Log.error(e, "Serious Error in CheckUpdateTask: " + e.getMessage());
				//When CheckUpdateTask throws an exception, it calls cancel on itself 
				//to remove itself from the Timer. 
				//It then logs the exception.
				//Because the exception never propagates back into the Timer thread, others Tasks continue to function even after 
				//CheckUpdateTask fails.
				WordPressCore.getInstance().getTimer().schedule(new CheckUpdateTask(), 24*60*60*1000, 24*60*60*1000); //24h check
			} 			  
		}
	}
		
	public void deleteBlog(BlogInfo selectedBlog) {
		if (selectedBlog.getState() == BlogInfo.STATE_LOADING || selectedBlog.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
			displayMessage("Loading blog. Try later..");
		} else {
			try {
				
				BlogDAO.removeBlog(selectedBlog);
				
	    		for (int i = 0; i < applicationBlogs.size(); i++) {
	    			
	    			BlogInfo currentBlog = (BlogInfo) applicationBlogs.elementAt(i);
	    							
	    			if (selectedBlog.equals(currentBlog) ) {
	    				applicationBlogs.removeElementAt(i);
	    				return;
	    			}
	    		}
				
			} catch (IOException e) {
				displayError(e, "Error while deleting the blog");
			} catch (RecordStoreException e) {
				displayError(e, "Error while deleting the blog");
			}
		}
	}

	public void addBlogs() {
		AddBlogsController ctrl=new AddBlogsController(this);
		ctrl.showView();
	}
		
	public BlogInfo[] getApplicationBlogs() {
		BlogInfo[] blogCaricati = new BlogInfo[applicationBlogs.size()];
		for (int i = 0; i < blogCaricati.length; i++) {
			blogCaricati[i] = (BlogInfo) applicationBlogs.elementAt(i);
		}	
		return blogCaricati;
	}

	public void showBlog(BlogInfo selectedBlog) {

		if (selectedBlog.getState() == BlogInfo.STATE_LOADING || selectedBlog.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
			//blog in caricamento
			displayMessage("Loading blog. Try later..");
		} else {
			
		Blog currentBlog=null;
	   	 try {
	   		 currentBlog=BlogDAO.getBlog(selectedBlog);
	   		 FrontController.getIstance().showBlog(currentBlog);
	   		 
			if(selectedBlog.isAwaitingModeration())
			for(int count = 0; count < applicationBlogs.size(); ++count)
	    	{
	    		BlogInfo blog = (BlogInfo)applicationBlogs.elementAt(count);
	    		
	    		if (selectedBlog.equals(blog) )		
	    		{
	    			selectedBlog.setAwaitingModeration(false);
	    			applicationBlogs.setElementAt(selectedBlog, count);
	    			view.setBlogItemViewState(selectedBlog);
	    			break;
	    		}
	    	}
	   		 
	   		 
			} catch (Exception e) {
				displayError(e, "Show Blog Error");
			}
		}
	}
			
	public void refreshView() {
		view.refreshBlogList();
	}	
	
	// Utility routine to ask question about exit application
	public synchronized boolean exitApp() {
		
/*		boolean inLoadingState = AddBlogsMediator.getIstance().isInLoadingState();
		if( inLoadingState ) {
			displayMessage("There are blogs in loading... Wait until blogs are loaded");
			return false;
		}
	*/	
		//background on close is selected
		if(Preferences.getIstance().isBackgroundOnClose()) {
			Log.debug("background on close is selected...");
			UiApplication.getUiApplication().requestBackground();
			return false;
		}
		
    	int result=this.askQuestion(_resources.getString(WordPressResource.MESSAGE_EXIT_APP));   
    	if(Dialog.YES==result) {
    		WordPressCore.getInstance().exitWordPress();
    		return true;
    	} else {
    		return false;
    	}
	}

	//listener for new comments in awaiting state
	public void newCommentNotifies(BlogInfo[] blogs) {
		for (int i = 0; i < blogs.length; i++) {
			BlogInfo blogI = blogs[i];
			view.setBlogItemViewState(blogI);	
			
			//update application blogs
			for(int count = 0; count < applicationBlogs.size(); ++count)
	    	{
	    		BlogInfo blog = (BlogInfo)applicationBlogs.elementAt(count);
	    		
	    		if (blogI.equals(blog) )		
	    		{
	    			applicationBlogs.setElementAt(blogI, count);
	    			break;
	    		}
	    	}
			
			
		}
	}
	
	//listener for the adding blogs task
	public void taskComplete(Object obj) {
		taskUpdate(obj);		
	}
	
	//listener for the adding blogs task
	public void taskUpdate(Object obj) {
		synchronized (view) {
			Blog loadedBlog = (Blog)obj;
			String blogName = loadedBlog.getName();
			String blogXmlRpcUrl=loadedBlog.getXmlRpcUrl();
			String blogId= loadedBlog.getId();
			int blogLoadingState = loadedBlog.getLoadingState();
			String usr = loadedBlog.getUsername();
			String passwd = loadedBlog.getPassword();
			BlogInfo blogI = new BlogInfo(blogId, blogName, blogXmlRpcUrl, usr, passwd,blogLoadingState, loadedBlog.isCommentNotifies());
			view.setBlogItemViewState(blogI); 
		
			//update application blogs
			for(int count = 0; count < applicationBlogs.size(); ++count)
	    	{
	    		BlogInfo blog = (BlogInfo)applicationBlogs.elementAt(count);
	    		
	    		if (blogI.equals(blog) )		
	    		{
	    			applicationBlogs.setElementAt(blogI, count);
	    			break;
	    		}
	    	}
		}
	}

//used in the add blog controller to add new blogs
   public static void taskStart(Object obj) {
	   Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();
		Vector loadedBlogs = (Vector)obj;
			for (int i = 0; i < loadedBlogs.size(); i++) {
				Blog loadedBlog = (Blog)loadedBlogs.elementAt(i);
				String blogName = loadedBlog.getName();
				String blogXmlRpcUrl=loadedBlog.getXmlRpcUrl();
				String blogId= loadedBlog.getId();
				int blogLoadingState = loadedBlog.getLoadingState();
				String usr = loadedBlog.getUsername();
				String passwd = loadedBlog.getPassword();
				BlogInfo blogI = new BlogInfo(blogId, blogName, blogXmlRpcUrl, usr, passwd,blogLoadingState, loadedBlog.isCommentNotifies());
				applicationBlogs.addElement(blogI);
			}
	}	
}