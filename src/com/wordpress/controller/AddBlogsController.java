//#preprocess
package com.wordpress.controller;

import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.io.MalformedURIException;
import net.rim.device.api.io.URI;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.VirtualKeyboard;
//#endif
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import org.kxmlrpc.XmlRpcException;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.task.LoadBlogsDataTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.Queue;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.AddBlogsView;
import com.wordpress.view.AddWPCOMBlogsView;
import com.wordpress.view.StandardBaseView;
import com.wordpress.view.component.CheckBoxPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.WaitScreen;
import com.wordpress.xmlrpc.BlogAuthConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;


public class AddBlogsController extends BaseController {
	
	private StandardBaseView view = null;
	private final TaskProgressListener listener; //listener on add a blog task
	ConnectionInProgressView connectionProgressView=null;
	private boolean isWPCOMCall = false; //true when adding a wp.com account 
	private BlogAuthConn connection;
	private String userInsertedURL = null;
	
	public AddBlogsController(TaskProgressListener listener, boolean isWPCOMBlog) {
		super();
		this.listener = listener;
		
		if(isWPCOMBlog) {
			this.view= new AddWPCOMBlogsView(this);
		} else {
			this.view= new AddBlogsView(this);
		}
		
	}
		
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}

	/*
	 * used when adding WP.COM blogs
	 */
	public void addWPCOMBlogs(String user, String passwd){
		user = user.trim();
		passwd = passwd.trim();
		isWPCOMCall = true;
        if (user != null && user != null && user.length() > 0) {
        	connection = new BlogAuthConn ("http://wordpress.com",user,passwd);
            connection.addObserver(new AddBlogCallBack(1, user, passwd)); 
             connectionProgressView= new ConnectionInProgressView(
            		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
                        
            connection.setDiscoveryApiLink(true);
             
            connection.startConnWork(); //starts connection
            int choice = connectionProgressView.doModal();
    		if(choice==Dialog.CANCEL) {
    			connection.stopConnWork(); //stop the connection if the user click on cancel button
    		}
        }
	}
		
	//0 = user has inserted the url into the main screen
	//1 = user has inserted the url into popup dialog   
	public void addBlogs(int source, String URL, String user, String passwd){
					
		if( URL == null 
			|| URL.trim().length() <= 0
			|| URL.trim().equalsIgnoreCase("http://")
			|| URL.trim().equalsIgnoreCase("https://")
			|| URL.trim().length() <= 0){
			displayError("Please, insert a valid address!");
			return;
		}
		
		try {
			URI.create(URL);
		} catch( MalformedURIException ex1 ) {
			displayError("Please, insert a valid address!");
			return;
		} catch (IllegalArgumentException ex2 ) {
			displayError("Please, insert a valid address!");
			return;
		}
		
		user = user.trim();
		passwd = passwd.trim();
        if ( user != null  && user.length() > 0 &&  passwd != null  && passwd.length() > 0) {
        	userInsertedURL = URL;
            connection = new BlogAuthConn (URL,user,passwd);
            connection.addObserver(new AddBlogCallBack(source, user, passwd)); 
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
        	displayError("Please, insert a valid username/password combination");
        }
	}
		
	
	private void parseResponse(BlogConnResponse resp) {
		try {
			
			Log.debug("found blogs: "+((Blog[])resp.getResponseObject()).length);	
			Blog[] serverBlogs = (Blog[]) resp.getResponseObject();
			if (isWPCOMCall) {
				AccountsController.storeWPCOMAccount(serverBlogs);
			}
			boolean[] existenceCheck = BlogDAO.checkBlogsExistance(serverBlogs);
			Vector addedBlog = new Vector(); //array of blog added to the app. this could be different from the response of the server

			//skip blogs already available in the app
			for (int i = 0; i < existenceCheck.length; i++) {
				if(existenceCheck[i] == false) {
					addedBlog.addElement(serverBlogs[i]);
				}
			}
			
			serverBlogs = new Blog[addedBlog.size()];
			addedBlog.copyInto(serverBlogs);
			addedBlog = new Vector();

			//show the blog selector popup when there are more then 1 blog associated with the account
			if(serverBlogs.length > 1) {
				String title = _resources.getString(WordPressResource.TITLE_ADDBLOGS_SELECTOR_POPUP);
				String[] blogNames = new String[serverBlogs.length];
				for (int i = 0; i < blogNames.length; i++) {
					blogNames[i] = serverBlogs[i].getName();
				}
				final CheckBoxPopupScreen selScr = new CheckBoxPopupScreen(title, blogNames);


				UiApplication.getUiApplication().invokeAndWait(new Runnable() {
					public void run() {
						selScr.pickItems();
						WaitScreen connectionProgressView = new WaitScreen(
								_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
						UiApplication.getUiApplication().pushScreen(connectionProgressView);
					}
				});
				
				boolean selection[] = selScr.getSelectedItems();
				for (int i = 0; i < selection.length; i++) {
					if(selection[i]){
						serverBlogs[i].setImageResizeWidth(new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH));
						serverBlogs[i].setImageResizeHeight(new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT));
						serverBlogs[i].setLoadingState(BlogInfo.STATE_ADDED_TO_QUEUE);
						if (isWPCOMCall) {
							serverBlogs[i].setWPCOMBlog(true);
						}
						addedBlog.addElement(serverBlogs[i]);
					}
				}
			} else if(serverBlogs.length == 1) {
				serverBlogs[0].setImageResizeWidth(new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH));
				serverBlogs[0].setImageResizeHeight(new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT));
				serverBlogs[0].setLoadingState(BlogInfo.STATE_ADDED_TO_QUEUE);
				if (isWPCOMCall) {
					serverBlogs[0].setWPCOMBlog(true);
				}
				addedBlog.addElement(serverBlogs[0]);		
			} else if(serverBlogs.length == 0) {
				displayMessage(_resources.getString(WordPressResource.MESSAGE_NO_OTHER_BLOGS_FOR_ACCOUNT));
			}
			
			serverBlogs = null;

			if (addedBlog.size() == 0 ){
				FrontController.getIstance().backToMainView();	
				return; //no blogs added
			}

			//store the new blogs 
			BlogDAO.newBlogs(addedBlog);

			Queue connectionsQueue = new Queue(addedBlog.size());
			for (int i = 0; i < addedBlog.size(); i++) {
				try { 
					//add this blog to the queue	
					final BlogUpdateConn connection = new BlogUpdateConn ((Blog)addedBlog.elementAt(i));       
					connectionsQueue.push(connection); 
				} catch (Exception e) {
					if(e != null && e.getMessage()!= null ) {
						displayMessage(e.getMessage());
					} else {
						displayMessage("Error while adding blog");			
					}
				}
			}

			if (connectionsQueue.isEmpty()) {
				FrontController.getIstance().backToMainView();			
				return;
			}

			//adds the new blogs to the app cached blogs list
			Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();
			for (int i = 0; i < addedBlog.size(); i++) {
				Blog loadedBlog = (Blog)addedBlog.elementAt(i);
				BlogInfo blogI = new BlogInfo(loadedBlog);
				applicationBlogs.addElement(blogI);
			}

			FrontController.getIstance().backToMainView();
			LoadBlogsDataTask loadBlogsTask = new LoadBlogsDataTask(connectionsQueue);
			loadBlogsTask.setProgressListener(listener);

			//push into the Runner
			try {
				Thread.currentThread().sleep(2000);
			} catch (InterruptedException e) {
				Log.error(e, "Error while adding blogs");
			}

			WordPressCore.getInstance().getTasksRunner().enqueue(loadBlogsTask);

		} catch (Exception e) {
			Log.error(e, "Error while adding blogs");
		}
	}

	//callback for send post to the blog
	private class AddBlogCallBack implements Observer {
		private int source = 0; //0 = base screen;  1 = popup prompted for detailed xmlrpc endpoint;
		private final String user;
		private final String passwd;
		
		public AddBlogCallBack(int source, String user, String passwd) {
			super();
			this.source = source;
			this.user = user;
			this.passwd = passwd;
		}
		
		public void update(Observable observable, final Object object) {
			try{
				
				dismissDialog(connectionProgressView);
				
				BlogConnResponse resp=(BlogConnResponse)object;
				
				if(resp.isStopped()){
					return;
				}
				
				if( !resp.isError() ) {
					parseResponse(resp);
					return;
				} 
				
				//Error handling
				if(resp.getResponseObject() instanceof XmlRpcException) { //response from xmlrpc server
					XmlRpcException responseObject = (XmlRpcException) resp.getResponseObject();
					if(responseObject.code == 403) { //bad login 
						displayError( _resources.getString(WordPressResource.MESSAGE_BAD_USERNAME_PASSWORD));
					} else {
						displayError(responseObject.getMessage());
					}
				} else if(source == 0) {
					if(connection.keepGoing) //HTTP Auth btn cancel NOT pressed
						UiApplication.getUiApplication().invokeLater(new Runnable() {
							public void run() {
								XmlRpcEndpointDialog pw = new XmlRpcEndpointDialog();
								pw.setDialogClosedListener(new XmlRpcEndpointDialogClosedListener(user, passwd));
								pw.show();
							}
						});
				} else {
					//IO Exception ad others errors
					showUnrecoverableErrorDialog((Exception)resp.getResponseObject());
				}
				
			} catch (final Exception e) {
				  displayError(e, "Error while adding blogs");   //Something went really wrong here :(
			} 
		}
	}
	
	public void showUnrecoverableErrorDialog(final Exception e) {
	  	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		Screen scr = UiApplication.getUiApplication().getActiveScreen();
		if(scr != null) {
	    	VirtualKeyboard virtKbd = scr.getVirtualKeyboard();
	    	if(virtKbd != null)
	    		virtKbd.setVisibility(VirtualKeyboard.HIDE);
		}
    	//#endif
		
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				new UnrecoverableErrorDialog(e).doModal();
			}
		});
	}
	
	private class UnrecoverableErrorDialog extends Dialog {

		private String getTitle(final Exception e, boolean isWPCom) {
			if(isWPCom) {
				return (e != null && e.getMessage() != null) ? "Unable to connect to WordPress.com: " + e.getMessage()+"." : "Unable to connect to WordPress.com";
			} else {				
				return (e != null && e.getMessage() != null) ? "Unable to connect to " + userInsertedURL + " : " + e.getMessage()+"." : "Unable to connect to " + userInsertedURL;
			}
		}
		
		public UnrecoverableErrorDialog(final Exception e) {
			super(Dialog.D_OK, "Unable to connect to WordPress" , 1, Bitmap.getPredefinedBitmap(Bitmap.EXCLAMATION), Dialog.GLOBAL_STATUS);
			ResourceBundle _resources =  ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
			getLabel().setText(getTitle(e, isWPCOMCall)); //Set the real error message here.
			net.rim.device.api.ui.Manager delegate = getDelegate();

			if( ! (delegate instanceof DialogFieldManager) ) return; //Just to make sure everything is ok with the UI. Don't think this will never happen.
			DialogFieldManager dfm = (DialogFieldManager)delegate;
			net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
			if( manager == null ) return;

			final String solutionURL = e != null ? Tools.getFAQLink( e ) : null;
			//Check if we have an FAQ entry for this Exception on the .org site.
			if ( solutionURL != null ){
				ButtonField goToTheSolutionBtnField = new ButtonField( _resources.getString( WordPressResource.BUTTON_READ_SOLUTION ));
				goToTheSolutionBtnField.setChangeListener(new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						Tools.openURL( solutionURL );
						close();
					}
				});
				manager.insert(goToTheSolutionBtnField, manager.getFieldCount());
			} else {
				//No FAQ for this error.
				if(isWPCOMCall) {
					LabelField descriptionField = new LabelField("Please, review your connection settings and try again.");
					Font fnt = this.getFont().derive(Font.ITALIC);
					descriptionField.setFont(fnt);
					manager.insert(descriptionField, manager.getFieldCount());

					ButtonField readTheFAQBtnField = new ButtonField("FAQ");
					readTheFAQBtnField.setChangeListener(new FieldChangeListener() {
						public void fieldChanged(Field field, int context) {
							Tools.openURL( WordPressInfo.SUPPORT_FAQ_URL );
							close();
						}
					});
					manager.insert(readTheFAQBtnField, manager.getFieldCount());
				} else {
					LabelField descriptionField = new LabelField("Please, verify " + userInsertedURL + " with the on-line tool available at http://xmlrpc.eritreo.it, and review your connection settings." );
					Font fnt = this.getFont().derive(Font.ITALIC);
					descriptionField.setFont(fnt);
					manager.insert(descriptionField, manager.getFieldCount());
				}//End .ORG branch
				
				ButtonField reportIssueBtnField = new ButtonField( "Open Settings" );
				reportIssueBtnField.setChangeListener(new FieldChangeListener() {
					public void fieldChanged(Field field, int context) {
						close();
						if(context == 0)
							FrontController.getIstance().showSetupView();
					}
				});
				manager.insert(reportIssueBtnField, manager.getFieldCount()); 
			}
		}
	}
	
	private class XmlRpcEndpointDialogClosedListener implements DialogClosedListener {
		private final String user;
		private final String passwd;

		public XmlRpcEndpointDialogClosedListener(String user, String passwd) {
			super();
			this.user = user;
			this.passwd = passwd;
		}

		public void dialogClosed(Dialog dialog, int choice) {
			if (choice == Dialog.OK) {
				XmlRpcEndpointDialog pw = (XmlRpcEndpointDialog) dialog;
				addBlogs(1, pw.getUrlFromField(), this.user, this.passwd);
			}
		}
	}
	
	
    private final class XmlRpcEndpointDialog extends Dialog {

        private EditField urlField;

        public XmlRpcEndpointDialog(){
            super(Dialog.D_OK, _resources.getString(WordPressResource.MESSAGE_UNABLE_TO_FIND_WORDPRESS), Dialog.D_OK, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
            urlField = new EditField(_resources.getString(WordPressResource.LABEL_URL)+ " ", "http://", 100, EditField.EDITABLE);
            urlField.setFilter(new URLTextFilter());
            LabelField descriptionField = new LabelField("ex: http://wordpress.com/xmlrpc.php");
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
	
	// Utility routine to by-pass the standard dialog box when the screen is closed  
	public boolean discardChange() {
		backCmd();
		return true;
	}

	public void refreshView() {
		
	}
}