package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.Bitmap;
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
import com.wordpress.io.AccountsDAO;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.task.LoadBlogsDataTask;
import com.wordpress.task.TaskProgressListener;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.AddBlogsView;
import com.wordpress.view.AddWPCOMBlogsView;
import com.wordpress.view.StandardBaseView;
import com.wordpress.view.component.CheckBoxPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogAuthConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;


public class AddBlogsController extends BaseController {
	
	private StandardBaseView view = null;
	private final TaskProgressListener listener; //listener on add a blog task
	ConnectionInProgressView connectionProgressView=null;
	private boolean isWPCOMCall = false; //true when adding a wp.com account 
	
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
		isWPCOMCall = true;
        if (user != null && user != null && user.length() > 0) {
            BlogAuthConn connection = new BlogAuthConn ("http://wordpress.com",user,passwd);
            connection.addObserver(new AddBlogCallBack(1, user, passwd)); 
             connectionProgressView= new ConnectionInProgressView(
            		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
                        
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
	
	
	//0 = user has inserted the url into the main screen
	//1 = user has inserted the url into popup dialog   
	public void addBlogs(int source, String URL, String user, String passwd){	
        if (URL != null && user != null && URL.length() > 0 && user != null && user.length() > 0) {
            BlogAuthConn connection = new BlogAuthConn (URL,user,passwd);
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
        	displayError("Please enter an address and username");
        }
	}
		
	
	private void storeWPCOMAccount(Blog[] serverBlogs) {
		if (!isWPCOMCall) {
			return;
		}
		if (serverBlogs.length == 0) return;
	
		try {
			Hashtable loadAccounts = AccountsDAO.loadAccounts();
				
			Blog tmpBlog = serverBlogs[0];
			String username = tmpBlog.getUsername();
			String passwd = tmpBlog.getPassword();
			Hashtable accountInfo = new Hashtable();
			accountInfo.put("username", username);
			accountInfo.put("passwd", passwd);
			accountInfo.put("blogs_number", ""+serverBlogs.length);
			
			Object object = loadAccounts.get(username);
			if(object == null) {
				//new account detected
				loadAccounts.put(username, accountInfo);
				AccountsDAO.storeAccounts(loadAccounts);
			} else {
				//account already available inside the app
				//TODO update all blogs info associated with this account
			}
		} catch (IOException e) {
			Log.error(e, "Error while storing account info");
		} catch (RecordStoreException e) {
			Log.error(e, "Error while storing account info");
		}
	}
	
	private void parseResponse(BlogConnResponse resp) {
						
		Log.debug("found blogs: "+((Blog[])resp.getResponseObject()).length);	
		Blog[] serverBlogs = (Blog[]) resp.getResponseObject();

		if (isWPCOMCall) {
			storeWPCOMAccount(serverBlogs);
		}
				
		//show the blog selector popup
		String title = _resources.getString(WordPressResource.TITLE_ADDBLOGS_SELECTOR_POPUP);
		String[] blogNames = new String[serverBlogs.length];
		for (int i = 0; i < blogNames.length; i++) {
			blogNames[i] = serverBlogs[i].getName();
		}
		final CheckBoxPopupScreen selScr = new CheckBoxPopupScreen(title, blogNames);
		
		UiApplication.getUiApplication().invokeAndWait(new Runnable() {
			public void run() {
				selScr.pickBlogs();
			}
		});
		
		Vector addedBlog = new Vector();
		boolean selection[] = selScr.getSelectedBlogs();
		for (int i = 0; i < selection.length; i++) {
			if(selection[i]){
				addedBlog.addElement(serverBlogs[i]);
			}
		}
		serverBlogs = new Blog[addedBlog.size()];
		addedBlog.copyInto(serverBlogs);
		addedBlog = new Vector();
		
		Queue connectionsQueue = new Queue(serverBlogs.length);
		for (int i = 0; i < serverBlogs.length; i++) {
			serverBlogs[i].setImageResizeWidth(new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH));
			serverBlogs[i].setImageResizeHeight(new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT));
			serverBlogs[i].setLoadingState(BlogInfo.STATE_ADDED_TO_QUEUE);
			if (isWPCOMCall) {
				serverBlogs[i].setWPCOMBlog(true);
			}
			
			try { //if a blog with same name and xmlrpc url exist
				BlogDAO.newBlog(serverBlogs[i], true);
				//add this blog to the queue	
				final BlogUpdateConn connection = new BlogUpdateConn (serverBlogs[i]);       
				connectionsQueue.push(connection); 
				addedBlog.addElement(serverBlogs[i]); //add this blog to the list of just added blog
			} catch (Exception e) {
				if(e != null && e.getMessage()!= null ) {
					displayMessage(e.getMessage());
				} else {
					displayMessage("Error while adding blog");			
				}
			}
		}
		
		//no blogs added
		if (connectionsQueue.isEmpty()) {
			FrontController.getIstance().backAndRefreshView(false);			
			return;
		}
		
		Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();

		for (int i = 0; i < addedBlog.size(); i++) {
			Blog loadedBlog = (Blog)addedBlog.elementAt(i);
			BlogInfo blogI = new BlogInfo(loadedBlog);
			applicationBlogs.addElement(blogI);
		}
		
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
				
				if(!resp.isError()) {
					
					parseResponse(resp);				
					
				} else {
					
					final String respMessage=resp.getResponse();
					Log.error(respMessage);
					if(resp.getResponseObject() instanceof XmlRpcException) { //response from xmlrpc server
						displayError(respMessage);
					} else if(source == 0) {
						UiApplication.getUiApplication().invokeLater(new Runnable() {
							public void run() {
								XmlRpcEndpointDialog pw = new XmlRpcEndpointDialog();
								pw.setDialogClosedListener(new XmlRpcEndpointDialogClosedListener(user, passwd));
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
		private final String user;
		private final String passwd;

		public XmlRpcEndpointDialogClosedListener(String user, String passwd) {
			super();
			this.user = user;
			this.passwd = passwd;
		}

		public void dialogClosed(Dialog dialog, int choice) {
			if (choice == Dialog.YES) {
				XmlRpcEndpointDialog pw = (XmlRpcEndpointDialog) dialog;
				addBlogs(1, pw.getUrlFromField(), this.user, this.passwd);
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
	
	// Utility routine to by-pass the standard dialog box when the screen is closed  
	public boolean discardChange() {
		backCmd();
		return true;
	}

	public void refreshView() {
		
	}
}