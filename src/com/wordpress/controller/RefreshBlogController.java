package com.wordpress.controller;

import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.CommentsDAO;
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
			Log.trace("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	public void update(Observable observable, final Object object) {
	
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				
				Log.trace(">>>Refreshing Blog Response");

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
						CommentsDAO.cleanGravatarCache(currentBlog);
						
					} catch (final Exception e) {
					 	displayError(e,"Error while storing the blog data");	
					}
					
				} else {
					
					currentBlog.setLoadingState(BlogInfo.STATE_ERROR);
					final String respMessage=resp.getResponse();
					displayError(respMessage);
					
					try {
						BlogDAO.updateBlog(currentBlog);
					} catch (Exception e) {
						displayError(e,"Error while storing the blog data");	
					}
				}//end else
				
				//update app blog
				WordPressCore wpCore = WordPressCore.getInstance();
				Vector applicationBlogs = wpCore.getApplicationBlogs();
				//update application blogs
				BlogInfo currentBlogI = new BlogInfo(currentBlog);
				for(int count = 0; count < applicationBlogs.size(); ++count)
		    	{
		    		BlogInfo applicationBlogTmp = (BlogInfo)applicationBlogs.elementAt(count);
		    		if (applicationBlogTmp.equals(currentBlogI) )		
		    		{
		    			applicationBlogs.setElementAt(currentBlogI, count);
		    			break;
		    		}
		    	}
				
				//update the main blogs view
				MainController.getIstance().updateBlogListEntry(currentBlogI);
				
			}//end run
		});
		

		
	}//end update

	public void refreshView() {
		
	}
	
}