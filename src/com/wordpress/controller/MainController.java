package com.wordpress.controller;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.DataCollector;
import com.wordpress.view.MainView;


public class MainController extends BaseController implements TaskProgressListener{
	
	private MainView view = null;
	
	public MainController() {
		super();
	}
	
	public void showView(){
		
		int numberOfBlog = 0; 
		
		//reset the state of blogs that are in loading or queue to loading error state
		//.... maybe app crash during adding blog
	   	 try {
			BlogInfo[] blogsList = getBlogsList();
			
			for (int i = 0; i < blogsList.length; i++) {
				BlogInfo blogInfo = blogsList[i];
				Blog blog = BlogDAO.getBlog(blogInfo);
			
				if (blog.getLoadingState() == BlogInfo.STATE_LOADING
						|| blog.getLoadingState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
					blog.setLoadingState(BlogInfo.STATE_LOADED_WITH_ERROR);
					BlogDAO.updateBlog(blog);
				}
			}
			numberOfBlog = blogsList.length;  //get the number of blog
		} catch (Exception e) {
			e.printStackTrace();		
		}
		
		DataCollector.collectData(numberOfBlog); //start the data collengtin here here!!	
		this.view=new MainView(this); //main view init here!.	
		UiApplication.getUiApplication().pushScreen(this.view);
	}
	
	
	public BlogInfo[] getBlogsList() {

		String[] listFiles = new String[0];
		BlogInfo[] blogs = new BlogInfo[0];

		try {
			listFiles = BlogDAO.getBlogsPath();
		} catch (Exception e1) {
			displayError(e1, "Error while loading your blogs index");
			return blogs;
		}

		Vector blogsVector = new Vector();
		for (int i = 0; i < listFiles.length; i++) {
			String currBlogPath = listFiles[i];

			try {
				BlogInfo currBlogInfo = BlogDAO.getBlogInfo(currBlogPath);
				blogsVector.addElement(currBlogInfo);
			} catch (Exception e) {
				displayError(e, "Error while loading blog: "+ currBlogPath);
			}
		}
		
		blogs = new BlogInfo[blogsVector.size()];
		for (int i = 0; i < blogs.length; i++) {
			blogs[i] = (BlogInfo) blogsVector.elementAt(i);
		}
		

		return blogs;
	}


	
	public void deleteBlog(BlogInfo selectedBlog) {
		if (selectedBlog.getState() == BlogInfo.STATE_LOADING || selectedBlog.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
			displayMessage("Loading blog. Try later..");
		} else {
			try {
				BlogDAO.removeBlog(selectedBlog);
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
		
	public void showBlog(BlogInfo selectedBlog){
		
		if (selectedBlog.getState() == BlogInfo.STATE_LOADING || selectedBlog.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
			//blog in caricamento
			displayMessage("Loading blog. Try later..");
		} else {
			
			Blog currentBlog=null;
	   	 try {
	   		 currentBlog=BlogDAO.getBlog(selectedBlog);
	   		 FrontController.getIstance().showBlog(currentBlog);
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
    	int result=this.askQuestion("Are sure to exit?");   
    	if(Dialog.YES==result) {
    		runner.quit(); //stop the runner thread
    		System.exit(0);
    		return true;
    	} else {
    		return false;
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
			BlogInfo blogI = new BlogInfo(blogId, blogName,blogXmlRpcUrl,blogLoadingState);
			view.setBlogItemViewState(blogI); 
		}
	}	
}