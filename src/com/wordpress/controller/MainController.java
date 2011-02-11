package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.Status;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.AppDAO;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.CommentsDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Comment;
import com.wordpress.model.Preferences;
import com.wordpress.task.LoadBlogsDataTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.DataCollector;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.MainView;
import com.wordpress.view.dialog.ConnectionDialogClosedListener;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogGetActivationStatusConn;
import com.wordpress.xmlrpc.BlogUpdateConn;


public class MainController extends BaseController implements TaskProgressListener {
	
	private MainView view = null;
	private Vector applicationBlogs = null;
	private Hashtable applicationAccounts = new Hashtable();
	
	private CheckForNewActivatedBlog checkForNewActivatedBlog = null;
	private long lastCheckTime = -1L;
	private ConnectionInProgressView connectionProgressView = null;
	
	private static MainController singletonObject;
	
	public static MainController getIstance() {
		if (singletonObject == null) {
			singletonObject = new MainController();
		}
		return singletonObject;
	}
    
    //singleton
    private MainController() {
    	super();
		WordPressCore wpCore = WordPressCore.getInstance();
		applicationBlogs = wpCore.getApplicationBlogs();
    }
	
	public void showView(){
		//loading accounts data.
		try {
			applicationAccounts = AppDAO.loadAccounts();
		} catch (ControlledAccessException e) {
			this.displayError(e, "Error while reading accounts data.");
		} catch (IOException e) {
			this.displayError(e, "Error while reading accounts data.");
		}
		//check the FS writing status
		try {
			String xmlrpcTmpDir;
			xmlrpcTmpDir = AppDAO.getXmlRpcTempFilesPath();
			if(JSR75FileSystem.isFileExist(xmlrpcTmpDir)) {
				JSR75FileSystem.removeFile(xmlrpcTmpDir);
			}
			JSR75FileSystem.createDir(xmlrpcTmpDir);
			if(!JSR75FileSystem.isFileExist(xmlrpcTmpDir)) {
				throw new IOException("Unable to write temp files. Please check application permissions, and set the cache files location to the SD card.");
			}
		} catch (RecordStoreException e) {
	    	Log.error(e, "Error while writing on the FS");
	    	this.displayError("Unable to access temp files. Please check application permissions, and set the cache files location to the SD card.");
		} catch (IOException e) {
			Log.error(e, "Error while writing on the FS");
			this.displayError("Unable to access temp files. Please check application permissions, and set the cache files location to the SD card.");
		}
		
		int numberOfBlog = 0; 
				
		Log.trace(">>> Checking blogs data");
	   	 try {
	   	
	   		Blog[] blogsList =  BlogDAO.getBlogs();
			for (int i = 0; i < blogsList.length; i++) {
				Blog blog = blogsList[i];
				BlogInfo blogInfo = new BlogInfo(blog);
				//reset the state of blogs that are in loading or queue to loading error state
				//.... maybe app there was a crash during adding blog
				if (blogInfo.getState() == BlogInfo.STATE_LOADING
						|| blogInfo.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
					blog.setLoadingState(BlogInfo.STATE_LOADED_WITH_ERROR);
					BlogDAO.updateBlog(blog);
					blogInfo.setState(BlogInfo.STATE_LOADED_WITH_ERROR);
				}
				
				//loading blog comments and check if there are comments in awaiting state
				int awaitingCommentsCount = 0;
				Vector loadComments = CommentsDAO.loadComments(blog);
				Hashtable vector2Comments = CommentsDAO.vector2Comments(loadComments);
				Comment[] tmpComments = (Comment[]) vector2Comments.get("comments");
				if(vector2Comments.get("error") == null) {
					for (int j = 0; j < tmpComments.length; j++) {
						Comment tmp = tmpComments[j];
						if(	tmp.getStatus().equalsIgnoreCase("hold") )
							awaitingCommentsCount++;
					}
					Log.trace("awaiting comments # :"+awaitingCommentsCount);
					
					blogInfo.setAwaitingModeration(awaitingCommentsCount);
				}
				applicationBlogs.addElement(blogInfo);
			}
			numberOfBlog = blogsList.length;  //get the number of blog
			
			//recreate the folders structure if missing
			BlogDAO.setUpFolderStructureForBlogs(applicationBlogs);
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
		
		WordPressCore wpCore = WordPressCore.getInstance();
		//schedule the update check task at startup
		wpCore.getTimer().schedule(new CheckUpdateTask(), 24*60*60*1000, 24*60*60*1000); //24h check
		
		this.view=new MainView(this); //main view init here!.	
		UiApplication.getUiApplication().pushScreen(this.view);
	
		//chapi "post startup" registration
		//SharingHelper sHelper = SharingHelper.getInstance();
		//sHelper.addCHAPIListener();
		//sHelper.checkPendingRequest();
		
	}
	

	private class CheckUpdateTask extends TimerTask {
		public void run() {
			try {
				Log.trace("CheckUpdateTask");
				DataCollector dtc = new DataCollector();
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
			displayMessage(_resources.getString(WordPressResource.MESSAGE_LOADING_BLOGS));
		} else {
			try {
				
				int result=this.askQuestion(_resources.getString(WordPressResource.MESSAGE_DELETE_BLOG));   
		    	if(Dialog.YES!=result) return;
				
				BlogDAO.removeBlog(selectedBlog);
				
	    		for (int i = 0; i < applicationBlogs.size(); i++) {
	    			
	    			BlogInfo currentBlog = (BlogInfo) applicationBlogs.elementAt(i);
	    							
	    			if (selectedBlog.equals(currentBlog) ) {
	    				applicationBlogs.removeElementAt(i);
	    				//ping the stats endpoint with new blog #
	    				DataCollector dtc = new DataCollector();
	    				dtc.pingStatsEndpoint(applicationBlogs.size());
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
	
	public void deleteBlogsByAccount(String username) {
			try {

				Vector removedBlog = new Vector();
				for (int i = 0; i < applicationBlogs.size(); i++) {
					BlogInfo currentBlogInfo = (BlogInfo) applicationBlogs.elementAt(i);
					if(currentBlogInfo.isWPCOMBlog() && 
							currentBlogInfo.getUsername().equalsIgnoreCase(username)) {
						BlogDAO.removeBlog(currentBlogInfo);
						removedBlog.addElement(currentBlogInfo);
					}
				}

				//rebuilding the app blogs list
	    		for (int i = 0; i < removedBlog.size(); i++) {
	    			BlogInfo currentBlog = (BlogInfo) removedBlog.elementAt(i);	 
	    			for (int j = 0; j < applicationBlogs.size(); j++) {
	    				BlogInfo selectedBlog = (BlogInfo) applicationBlogs.elementAt(j);
	    				if (selectedBlog.equals(currentBlog) ) {
	    					applicationBlogs.removeElementAt(j);
	    					break;
	    				}
	    			}
	    		}

	    		//ping the stats endpoint with new blog #s
	    		DataCollector dtc = new DataCollector();
	    		dtc.pingStatsEndpoint(applicationBlogs.size());

	    		//update the view
	    		this.view.refreshBlogList();
			} catch (IOException e) {
				displayError(e, "Error while deleting the blog");
			} catch (RecordStoreException e) {
				displayError(e, "Error while deleting the blog");
			}
		}
	

	public void addWPORGBlogs() {
		AddBlogsController ctrl = new AddBlogsController(this, false);
		ctrl.showView();
	}
		
	public void addWPCOMBlogs() {
		AddBlogsController ctrl = new AddBlogsController(this, true);
		ctrl.showView();
	}
	
	public Hashtable getApplicationAccounts() {
		return applicationAccounts;
	}
	
	
	//check if there are blogs in loading right now
	public synchronized boolean isLoadingBlogs() {
		BlogInfo[] applicationBlogs = getApplicationBlogs();
		for (int i = 0; i < applicationBlogs.length; i++) {
			BlogInfo selectedBlog = applicationBlogs[i];
			if (selectedBlog.getState() == BlogInfo.STATE_LOADING || selectedBlog.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
				return true;
			}
		}
		return false;
	}
	
	public synchronized BlogInfo[] getApplicationBlogs() {
		BlogInfo[] blogCaricati = new BlogInfo[applicationBlogs.size()];
		for (int i = 0; i < blogCaricati.length; i++) {
			blogCaricati[i] = (BlogInfo) applicationBlogs.elementAt(i);
		}	
		return blogCaricati;
	}

	public void showBlog(BlogInfo selectedBlog) {
		if (selectedBlog.getState() == BlogInfo.STATE_LOADING || selectedBlog.getState() == BlogInfo.STATE_ADDED_TO_QUEUE) {
			//blog in not available yet. loading in progress
			displayMessage(_resources.getString(WordPressResource.MESSAGE_LOADING_BLOGS));
		} else if (selectedBlog.getState() == BlogInfo.STATE_PENDING_ACTIVATION) {
			//you should activate the blog first.
			//start the check here
			displayMessage(_resources.getString(WordPressResource.MESSAGE_PENDING_ACTIVATION_BLOG));
		} else {
			FrontController.getIstance().showBlog(selectedBlog);
		}
	}
			
	public void refreshView() {
		Log.debug("Refreshing main view...");
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

	/*listener for new comments in awaiting state
	public void newCommentNotifies(BlogInfo[] blogs) {
		
		for (int i = 0; i < blogs.length; i++) {
			BlogInfo blogI = blogs[i];
			view.setBlogItemViewState(blogI);			
		}
	}*/
	
	public void updateBlogListEntry(BlogInfo blogInfo){
		view.setBlogItemViewState(blogInfo); 
	}
	
	//listener for the adding blogs task
	public void taskComplete(Object obj) {
		taskUpdate(obj);
		//ping the stats endpoint with new blog #
		DataCollector dtc = new DataCollector();
		dtc.pingStatsEndpoint(applicationBlogs.size());
	}
	
	//listener for the adding blogs task
	public void taskUpdate(Object obj) {
		synchronized (view) {
			Blog loadedBlog = (Blog)obj;
			BlogInfo loadedBlogInfo = new BlogInfo(loadedBlog);
			view.setBlogItemViewState(loadedBlogInfo); 
		
			//update application blogs (refresh controller have a similar code)
			for(int count = 0; count < applicationBlogs.size(); ++count)
	    	{
	    		BlogInfo applicationBlogTmp = (BlogInfo)applicationBlogs.elementAt(count);
	    		
	    		if (loadedBlogInfo.equals(applicationBlogTmp) )		
	    		{
	    			loadedBlogInfo.setAwaitingModeration(applicationBlogTmp.getAwaitingModeration());
	    			loadedBlogInfo.setCommentNotifies(applicationBlogTmp.isCommentNotifies());
	    			applicationBlogs.setElementAt(loadedBlogInfo, count);
	    			break;
	    		}
	    	}
		}
	}

	
	public synchronized void checkForNewActivatedBlog(int index) {
		Log.trace("MainController checkForNewActivatedBlog");
		
		if(lastCheckTime != -1L) { //first check
			long currentTime = System.currentTimeMillis();
			// Get difference in milliseconds
			long diffMillis = currentTime - lastCheckTime;
			long  diffMins = diffMillis/(60*1000); //one minute
			if(diffMins < 2) {
				Status.show("slow down baby...");
				Log.trace("slow down baby...");
				return;
			}
		} 
		
		lastCheckTime = System.currentTimeMillis();

		if(((BlogInfo)applicationBlogs.elementAt(index)).getState() == BlogInfo.STATE_PENDING_ACTIVATION){
			BlogInfo applicationBlogTmp = (BlogInfo)applicationBlogs.elementAt(index);
			//chiamare la procedura per controllare l'attivazione di un blog
			BlogGetActivationStatusConn connection = new BlogGetActivationStatusConn ("https://wordpress.com/xmlrpc.php", applicationBlogTmp.getBlogURL());		        
			
			connectionProgressView= new ConnectionInProgressView(
	    			_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
			connection.addObserver(new BlogGetActivationStatusConnCallBack(applicationBlogTmp, connectionProgressView)); 	        
			connectionProgressView.setDialogClosedListener(new ConnectionDialogClosedListener(connection));
	        connectionProgressView.show();
			connection.startConnWork();
		}
	}
	
	
	public void startPendingActivationSchedule() {
		Log.trace("startPendingActivationSchedule");
		if(checkForNewActivatedBlog == null)
			checkForNewActivatedBlog = new CheckForNewActivatedBlog();
		else {
			checkForNewActivatedBlog.cancel(); //ensure the timer is canceled
			checkForNewActivatedBlog = new CheckForNewActivatedBlog();
		}
		WordPressCore wpCore = WordPressCore.getInstance();
		//schedule the update check task at startup
		wpCore.getTimer().schedule(checkForNewActivatedBlog, 3*60*1000, 3*60*1000); //3mins check
	}
	
	private class CheckForNewActivatedBlog  extends TimerTask {
		public void run() {
			try {
				for (int i=0; i< applicationBlogs.size(); i++){
					if(((BlogInfo)applicationBlogs.elementAt(i)).getState() == BlogInfo.STATE_PENDING_ACTIVATION){
						BlogInfo applicationBlogTmp = (BlogInfo)applicationBlogs.elementAt(i);
						//chiamare la procedura per controllare l'attivazione di un blog
						BlogGetActivationStatusConn connection = new BlogGetActivationStatusConn ("https://wordpress.com/xmlrpc.php", applicationBlogTmp.getBlogURL());		        
						connection.addObserver(new BlogGetActivationStatusConnCallBack(applicationBlogTmp)); 	        
						connection.startConnWork();
						return;
					}
				}
				
				//no blog in pending state cancel the task
				cancel();
			} catch (Throwable  e) {
				cancel();
				Log.error(e, "Serious Error in CheckForNewActivatedBlog: " + e.getMessage());
				//When CheckUpdateTask throws an exception, it calls cancel on itself 
				//to remove itself from the Timer. 
				//It then logs the exception.
				//Because the exception never propagates back into the Timer thread, others Tasks continue to function even after 
				//CheckForNewActivatedBlog fails.
				checkForNewActivatedBlog = new CheckForNewActivatedBlog();
				 WordPressCore.getInstance().getTimer().schedule(checkForNewActivatedBlog, 3*60*1000, 3*60*1000); //3mins check
			} 			  
		}
	}

	//callback for signup to the blog
	private class BlogGetActivationStatusConnCallBack implements Observer {
		
		private BlogInfo currentBlogInfo;
		
		public BlogGetActivationStatusConnCallBack(BlogInfo currentBlogTmp) {
			super();
			this.currentBlogInfo = currentBlogTmp;
		}
		
		ConnectionInProgressView dialog;
		public BlogGetActivationStatusConnCallBack(BlogInfo currentBlogTmp, ConnectionInProgressView dialog) {
			super();
			this.currentBlogInfo = currentBlogTmp;
			this.dialog = dialog;
		}

		public void update(Observable observable, final Object object) {
			Log.trace("BlogGetActivationStatusConnCallBack update");
			if(dialog != null)
				MainController.this.dismissDialog(dialog);
			
			BlogConnResponse resp = (BlogConnResponse)object;
			if(resp.isStopped()){
				return;
			}
			if(resp.isError()) {
				final String respMessage=resp.getResponse();
				if(dialog != null)
					MainController.this.displayError(respMessage);
				Log.error(respMessage);
				return;
			}

			final Hashtable tempData = (Hashtable)resp.getResponseObject();
			try {
				//update the blog on FS
				Blog tempFullBlog = BlogDAO.getBlog(currentBlogInfo);
				BlogDAO.removeBlog(currentBlogInfo); //we should remove the blog bc we have changed the ID
				tempFullBlog.setId(String.valueOf(tempData.get(" blog_id")));
				tempFullBlog.setLoadingState(BlogInfo.STATE_ADDED_TO_QUEUE);
				Vector tmpBlogList = new Vector(1);
				tmpBlogList.addElement(tempFullBlog);
				BlogDAO.newBlogs(tmpBlogList);
				
				//update the blog in memory
				currentBlogInfo.setId(String.valueOf(tempData.get(" blog_id")));
				currentBlogInfo.setState(BlogInfo.STATE_ADDED_TO_QUEUE);
				
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						try {
							MainController.this.refreshView();
						} catch (Exception e) {
							Log.error(e, "Error while refreshing the blog view");
						}
					}
				});
				
				//schedule the execution of blog loading
				Queue connectionsQueue = new Queue(1);
				final BlogUpdateConn connection = new BlogUpdateConn (tempFullBlog);       
				connectionsQueue.push(connection); 
				LoadBlogsDataTask loadBlogsTask = new LoadBlogsDataTask(connectionsQueue);
				loadBlogsTask.setProgressListener(MainController.this);

				//push into the Runner
				try {
					Thread.currentThread().sleep(2000);
				} catch (InterruptedException e) {
					Log.error(e, "Error while adding blogs");
				}

				WordPressCore.getInstance().getTasksRunner().enqueue(loadBlogsTask);
				
			} catch (IOException e) {
				Log.error(e,"IOException while finalizing the activation");
			} catch (RecordStoreException e) {
				Log.error(e,"RecordStoreException while finalizing the activation");
			} catch (Exception e) {
				Log.error(e,"Exception while finalizing the activation");
			}
			
		}
	}
}