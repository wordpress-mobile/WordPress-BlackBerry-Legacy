package com.wordpress.controller;

import java.util.Hashtable;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Preferences;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.AddBlogsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogAuthConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;
import com.wordpress.xmlrpc.GetBlogsDataTask;
import com.wordpress.xmlrpc.TaskListener;


public class AddBlogsController extends BaseController implements Observer{
	
	private AddBlogsView view = null;
	private final TaskListener listener; //listener on add a blog task
	ConnectionInProgressView connectionProgressView=null;
	
	private int maxPostIndex= -1;
	private boolean isResPhotos= false;
	public static final int[] recentsPostValues={10,20,30,40,50};
	public static final String[] recentsPostValuesLabel={"10","20","30","40","50"};
	private Hashtable guiValues= new Hashtable();
	
	public AddBlogsController(TaskListener listener) {
		super();
		this.listener = listener;
		guiValues.put("user", "mopress");
		guiValues.put("pass", "mopress");
		guiValues.put("url", "http://localhost/wp_mopress");
		guiValues.put("recentpost", recentsPostValuesLabel);
		guiValues.put("recentpostselected", new Integer(0));
		guiValues.put("isresphotos", new Boolean(false));
		this.view= new AddBlogsView(this,guiValues);
	}
	
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}

	/**
	 * check the path of the file xmlrpc.php into the url string
	 */
	private String checkURL(String url){
		System.out.println(">>> checkURL");
		System.out.println("in URL: "+url);
		if(url == null || url.trim().length() == 0 ) {
			return null;
		}
			
		if (url.endsWith("xmlrpc.php")){
			
		} else {
			if (!url.endsWith("/")){
				url+="/";
			}
			url+="xmlrpc.php";
		}
		System.out.println("out URL: "+url);	
		return url;
	}
	
	public void addBlogs(){
		
		String pass= view.getBlogPass();
		String url= view.getBlogUrl();
		url=checkURL(url);
		String user= view.getBlogUser();
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
            Preferences prefs = Preferences.getIstance();
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
		
		if(!resp.isError()) {
							 	
			if(resp.isStopped()){
				return;
			}
			
			System.out.println("Trovati blogs: "+((Blog[])resp.getResponseObject()).length);	
		 	Blog[]blogs=(Blog[])resp.getResponseObject();
		 	
		 	GetBlogsDataTask networkTask = new GetBlogsDataTask(listener); 
			Preferences prefs = Preferences.getIstance();
			
		 	for (int i = 0; i < blogs.length; i++) {
		 		blogs[i].setMaxPostCount(recentsPostValues[maxPostIndex]);
		 		blogs[i].setResizePhotos(isResPhotos);	
		 		blogs[i].setLoadingState(BlogInfo.STATE_ADDED_TO_QUEUE);
				BlogDAO.newBlog(blogs[i], true);
				//add this blog to the queue	
				final BlogUpdateConn connection = new BlogUpdateConn (blogs[i]);       
		        networkTask.addConn(connection); 
		    }
		 	FrontController.getIstance().backAndRefreshView(true); //update the main view with new blogs
		 	networkTask.startTask(); //start getting blog details
		} else {
			final String respMessage=resp.getResponse();
		 	displayError(respMessage);	
		}		
	
		} catch (final Exception e) {
		 	displayError(e,"Error while adding blogs");	
		} 
	}
	
	

	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	//System.out.println("field class name: " + field.getOriginal().getClass().getName());
	    	addBlogs();
	   }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
//	    	System.out.println("field class name: " + field.getOriginal().getClass().getName());
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