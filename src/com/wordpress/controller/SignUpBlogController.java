package com.wordpress.controller;

import java.util.Vector;

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
import com.wordpress.view.SignUpBlogView;
import com.wordpress.view.StandardBaseView;
import com.wordpress.view.component.CheckBoxPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogAuthConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogSignUpConn;
import com.wordpress.xmlrpc.BlogUpdateConn;


public class SignUpBlogController extends BaseController {
	
	private StandardBaseView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private BlogSignUpConn connection;
	
	public SignUpBlogController() {
		super();
		this.view= new SignUpBlogView(this);
	}
		
	public void showView(){
		UiApplication.getUiApplication().pushScreen(view);
	}

	public void signup(String blogName, String user, String email, String passwd){
		user = user.trim();
		passwd = passwd.trim();
		blogName = blogName.trim();
		email = email.trim();

		if (user != null && user != null && user.length() > 0) {
        	connection = new BlogSignUpConn ("https://wordpress.com/xmlrpc.php", user, passwd, blogName, email);
            connection.addObserver(new BlogSignUpCallBack()); 
             connectionProgressView= new ConnectionInProgressView(
            		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
             
            connection.startConnWork(); //starts connection
            int choice = connectionProgressView.doModal();
    		if(choice==Dialog.CANCEL) {
    			connection.stopConnWork(); //stop the connection if the user click on cancel button
    		}
        }
	}
		
	//callback for send post to the blog
	private class BlogSignUpCallBack implements Observer {
		
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
				 	displayError(respMessage);	
				}		
			} catch (final Exception e) {
				displayError(e,"Error while during signup, please try later.");	
			} 
		}
	}
		
	
	private void parseResponse(BlogConnResponse resp) {
		try {
			FrontController.getIstance().backAndRefreshView(false);	
			
		} catch (Exception e) {
			Log.error(e, "Error while adding blogs");
		}
	}

	public void refreshView() {
		
	}
}