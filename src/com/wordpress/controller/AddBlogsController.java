package com.wordpress.controller;

import java.util.Hashtable;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import org.kxmlrpc.XmlRpcException;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.task.LoadBlogsDataTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.AddBlogsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogAuthConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;


public class AddBlogsController extends BaseController{
	
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

	//0 = user has inserted the url into the main screen
	//1 = user has inserted the url into popup dialog   
	public void addBlogs(int source){
		
		String pass= view.getBlogPass().trim();
		String url= view.getBlogUrl().trim();
		//url=Tools.checkURL(url); //check te presence of xmlrpc file
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
            connection.addObserver(new AddBlogCallBack(source)); 
             connectionProgressView= new ConnectionInProgressView(
            		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
           
             if(source == 1 )
            	 connection.setDiscoveryApiLink(false); //we have asked the xmlrpc endpoint to user
             else
            	 connection.setDiscoveryApiLink(true);
             
            connection.startConnWork(); //starts connection
            int choice = connectionProgressView.doModal();
    		if(choice==Dialog.CANCEL) {
    			connection.stopConnWork(); //stop the connection if the user click on cancel button
    		}
        } else {
        	displayError("Please enter an address and username");
        }
	}
	
	
	private void parseResponse(BlogConnResponse resp) {
		
		Log.debug("found blogs: "+((Blog[])resp.getResponseObject()).length);	
		Blog[]blogs=(Blog[])resp.getResponseObject();
		Queue connectionsQueue = new Queue(blogs.length);
		
		for (int i = 0; i < blogs.length; i++) {
			blogs[i].setMaxPostCount(recentsPostValues[maxPostIndex]);
			blogs[i].setResizePhotos(isResPhotos);	
			blogs[i].setLoadingState(BlogInfo.STATE_ADDED_TO_QUEUE);
			
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
		
		//update the main view with new blogs all STATE_ADDED_TO_QUEUE
		FrontController.getIstance().backAndRefreshView(true); 
		LoadBlogsDataTask loadBlogsTask = new LoadBlogsDataTask(connectionsQueue);
		loadBlogsTask.setProgressListener(listener);

		//push into the Runner
		try {
			Thread.currentThread().sleep(2000);
		} catch (InterruptedException e) {
			Log.error(e, "Error while adding blogs");
		}
		WordPressCore.getInstance().getTasksRunner().enqueue(loadBlogsTask);
	}

	//callback for send post to the blog
	private class AddBlogCallBack implements Observer{
		private int source = 0; //0 = base screen;  1 = popup prompted for detailed xmlrpc endpoint;
		
		public AddBlogCallBack(int source) {
			super();
			this.source = source;
		}
		
		public void update(Observable observable, final Object object) {
			try{
				
				dismissDialog(connectionProgressView);
				
				BlogConnResponse resp=(BlogConnResponse)object;
				
				if(resp.isStopped()){
					return;
				}
				
				if(!resp.isError()) {
					
					parseResponse(resp);				
					
				} else {
					
					final String respMessage=resp.getResponse();
					Log.error(respMessage);
					if(resp.getResponseObject() instanceof XmlRpcException) { //response from xmlrpc server
						displayError(respMessage);
					//} else if(resp.getResponseObject() instanceof XmlPullParserException) {
					} else if(source == 0) {
						//xmlrpc url error
						/*if (source == 1) //popupscreen source
							displayError(_resources.getString(WordPressResource.MESSAGE_XMLRPC_ENDPOINT_FAILED));
							else*/
						UiApplication.getUiApplication().invokeLater(new Runnable() {
							public void run() {
								XmlRpcEndpointDialog pw = new XmlRpcEndpointDialog();
								pw.setDialogClosedListener(new XmlRpcEndpointDialogClosedListener());
								pw.show();
							}
						});
					} else {
						//IO Exception ad others
						displayError(respMessage);
					}
				}		
			} catch (final Exception e) {
				displayError(e,"Error while adding blogs");	
			} 
		}
	}
	
	
	private class XmlRpcEndpointDialogClosedListener implements DialogClosedListener {
		
		public void dialogClosed(Dialog dialog, int choice) {
			if (choice == Dialog.YES) {
				XmlRpcEndpointDialog pw = (XmlRpcEndpointDialog) dialog;
				view.setBlogUrl(pw.getUrlFromField());
				addBlogs(1);
			}
		}
	}
	
	
    private final class XmlRpcEndpointDialog extends Dialog {

        private EditField urlField;

        public XmlRpcEndpointDialog(){
            super(Dialog.D_YES_NO, _resources.getString(WordPressResource.MESSAGE_XMLRPC_ENDPOINT), Dialog.NO, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
            urlField = new EditField(_resources.getString(WordPressResource.LABEL_URL)+ " ", "http://", 100, EditField.EDITABLE);
            urlField.setFilter(new URLTextFilter());
            LabelField descriptionField = new LabelField("ex: http://example.com/some-folder/xmlrpc.php");
            Font fnt = this.getFont().derive(Font.ITALIC);
            descriptionField.setFont(fnt);
            
            net.rim.device.api.ui.Manager delegate = getDelegate();
            if( delegate instanceof DialogFieldManager){
                DialogFieldManager dfm = (DialogFieldManager)delegate;
                net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
                if( manager != null ){
                	manager.insert(descriptionField, 0);
                    manager.insert(urlField, 1);
                    urlField.setCursorPosition(7);
                }
            }
        }    

        public String getUrlFromField(){
          return urlField.getText();
        }
        
    }
	
	/*
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
	*/
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	addBlogs(0); 
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