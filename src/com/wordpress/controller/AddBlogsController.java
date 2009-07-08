package com.wordpress.controller;

import java.util.Hashtable;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import org.kxmlrpc.XmlRpcException;
import org.xmlpull.v1.XmlPullParserException;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.task.LoadBlogsDataTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.Queue;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.AddBlogsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogAuthConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;


public class AddBlogsController extends BaseController implements Observer{
	
	private AddBlogsView view = null;
	private final TaskProgressListener listener; //listener on add a blog task
	ConnectionInProgressView connectionProgressView=null;
	
	private int maxPostIndex= -1;
	private boolean isResPhotos= false;
	public static final int[] recentsPostValues={10,20,30,40,50};
	public static final String[] recentsPostValuesLabel={"10","20","30","40","50"};
	private Hashtable guiValues= new Hashtable();
	
	public AddBlogsController(TaskProgressListener listener) {
		super();
		this.listener = listener;
		guiValues.put("user", "");
		guiValues.put("pass", "");
		guiValues.put("url", "http://");
		guiValues.put("recentpost", recentsPostValuesLabel);
		guiValues.put("recentpostselected", new Integer(0));
		guiValues.put("isresphotos", new Boolean(false));
		this.view= new AddBlogsView(this,guiValues);
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}

	
	public void addBlogs(){
		
		String pass= view.getBlogPass().trim();
		String url= view.getBlogUrl().trim();
		url=Tools.checkURL(url); //check te presence of xmlrpc file
		String user= view.getBlogUser().trim();
		maxPostIndex=view.getMaxRecentPostIndex();
		System.out.println("Max Show posts index: "+maxPostIndex);
		if(maxPostIndex < 0){
			maxPostIndex=0;			
			displayError("Please enter a correct number");
	       return;
		}
		
		isResPhotos= view.isResizePhoto();
		System.out.println("Resize photos : "+isResPhotos);
		
/*		if (pass != null && pass.length() == 0) {
        	pass = null;
            displayError("Please enter a password");
            return;
        }
*/
        if (url != null && user != null && url.length() > 0 && user != null && user.length() > 0) {
            BlogAuthConn connection = new BlogAuthConn (url,user,pass);
            connection.addObserver(this); 
             connectionProgressView= new ConnectionInProgressView(
            		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
           
            connection.startConnWork(); //starts connection
            int choice = connectionProgressView.doModal();
    		if(choice==Dialog.CANCEL) {
    			System.out.println("Chiusura della conn dialog tramite cancel");
    			connection.stopConnWork(); //stop the connection if the user click on cancel button
    		}
        } else {
        	displayError("Please enter an address and username");
        }
	}
	
		
	public void update(Observable observable, Object object) {
		try{
			
		dismissDialog(connectionProgressView);

		BlogConnResponse resp=(BlogConnResponse)object;
		
		if(resp.isStopped()){
			return;
		}
		
		if(!resp.isError()) {
			
			Log.debug("found blogs: "+((Blog[])resp.getResponseObject()).length);	
		 	Blog[]blogs=(Blog[])resp.getResponseObject();
		 	Queue connectionsQueue = new Queue(blogs.length);
			
		 	for (int i = 0; i < blogs.length; i++) {
		 		blogs[i].setMaxPostCount(recentsPostValues[maxPostIndex]);
		 		blogs[i].setResizePhotos(isResPhotos);	
		 		blogs[i].setLoadingState(BlogInfo.STATE_ADDED_TO_QUEUE);
		 		
		    	String url = null;
		    	if ( blogs[i].getXmlRpcUrl() != null ) {
		    		url = blogs[i].getXmlRpcUrl();
		    	} else {
		    		Log.trace("blog xmlrpc url was null");
		    		Log.trace("blog xmlrpc was set to blog url");
		    		url = blogs[i].getUrl();
		    	}
		    	//check the url string
		    	url = Tools.checkURL(url);
		    	blogs[i].setXmlRpcUrl(url); //set the blog xmlrpc url
		    	
		    	if(url == null || url.equalsIgnoreCase(""))
		    		continue; //skip this blog
		    		 		
				try { //if a blog with same name and xmlrpc url exist
					BlogDAO.newBlog(blogs[i], true);
					//add this blog to the queue	
					final BlogUpdateConn connection = new BlogUpdateConn (blogs[i]);       
					connectionsQueue.push(connection); 
				} catch (Exception e) {
					if(e != null && e.getMessage()!= null ) {
						displayMessage("Error while adding blog: " + "\n" + e.getMessage());
					} else {
						displayMessage("Error while adding blog: ");			
					}
				}
		    }
		 	
		 	FrontController.getIstance().backAndRefreshView(true); //update the main view with new blogs
		 	LoadBlogsDataTask loadBlogsTask = new LoadBlogsDataTask(connectionsQueue);
			loadBlogsTask.setProgressListener(this.listener);
			//push into the Runner
			runner.enqueue(loadBlogsTask);
		 	
		} else {
			final String respMessage=resp.getResponse();
			Log.error(respMessage);
			if(resp.getResponseObject() instanceof XmlRpcException) {
				//pass/username errata
				displayError(respMessage);
			} else if(resp.getResponseObject() instanceof XmlPullParserException) {
				//xmlrpc url error
				displayError(_resources.getString(WordPressResource.MESSAGE_COMUNICATION_ERR));
			} else {
				//IO Exception ad others
				displayError(respMessage);
			}
		}		
	
		} catch (final Exception e) {
		 	displayError(e,"Error while adding blogs");	
		} 
	}
	
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	addBlogs();
	   }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	        backCmd();
	   }
	};

	public FieldChangeListener getOkButtonListener() {
		return listenerOkButton;
	}
	   
	public FieldChangeListener getBackButtonListener() {
		return listenerBackButton;
	}
	
	// Utility routine to by-pass the standard dialog box when the screen is closed  
	public boolean discardChange() {
		backCmd();
		return true;
	}

	public void refreshView() {
		
	}
}