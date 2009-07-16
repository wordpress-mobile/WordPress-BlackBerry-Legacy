package com.wordpress.controller;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.log.Log;
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
				
				Log.trace(">>>loadPostsResponse");

				dismissDialog(connectionProgressView);
				BlogConnResponse resp= (BlogConnResponse) object;
						
				if(resp.isStopped()){
					return;
				}
				if(!resp.isError()) {
					try{
						currentBlog= (Blog) resp.getResponseObject(); 	//update blogs obj	
						currentBlog.setLoadingState(BlogInfo.STATE_LOADED);
						BlogDAO.updateBlog(currentBlog);							
					} catch (final Exception e) {
						
						if(currentBlog != null) {
							currentBlog.setLoadingState(BlogInfo.STATE_LOADED_WITH_ERROR);
							try {
								BlogDAO.updateBlog(currentBlog);
							} catch (Exception e2) {
								Log.error(e, "Error while saving blogs");
							}
						}											
					 	displayError(e,"Error while saving new blog info");	
					}
					
				} else {
					
					currentBlog.setLoadingState(BlogInfo.STATE_ERROR);
					try {
						BlogDAO.updateBlog(currentBlog);
					} catch (Exception e) {
						Log.error(e, "Error while saving blogs");
					}
					
					final String respMessage=resp.getResponse();
				 	displayError(respMessage);	
				}
				
			}
		});
	}

	public void refreshView() {
		
	}
	
}