package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Preferences;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;


public class RefreshBlogController extends BaseController implements Observer{
	
	ConnectionInProgressView connectionProgressView=null;
	private Blog currentBlog=null;
		
	public RefreshBlogController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
	}
	
	public void showView(){
	}

	public void refreshBlog(){
		Preferences prefs = Preferences.getIstance();
		final BlogUpdateConn connection = new BlogUpdateConn (currentBlog);       
        connection.addObserver(this); 
         connectionProgressView= new ConnectionInProgressView(
        		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	public void update(Observable observable, final Object object) {
	
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				
				System.out.println(">>>loadPostsResponse");

				dismissDialog(connectionProgressView);
				BlogConnResponse resp= (BlogConnResponse) object;
						
				if(!resp.isError()) {
					if(resp.isStopped()){
						return;
					}
					currentBlog= (Blog) resp.getResponseObject(); 	//update blogs obj	
					try{
						currentBlog.setLoadingState(BlogInfo.STATE_LOADED);
						BlogDAO.updateBlog(currentBlog);							
					} catch (final Exception e) {
					 	displayError(e,"Error while refreshing blogs");	
					}
					
				} else {
					final String respMessage=resp.getResponse();
				 	displayError(respMessage);	
				}
				
			}
		});
	}

	public void refreshView() {
		
	}
	
}