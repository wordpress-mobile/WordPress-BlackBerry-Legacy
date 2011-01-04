package com.wordpress.controller;

import java.util.Vector;

import net.rim.device.api.system.EncodedImage;
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
import com.wordpress.view.BlogView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.BlogUpdateConn;


public class BlogController extends BaseController {
	
	private BlogView view = null;
	private BlogInfo currentBlogInfo;
	private Blog currentBlog = null;
	ConnectionInProgressView connectionProgressView=null;
	
 
	public BlogController(BlogInfo currentBlog) {
		super();
		this.currentBlogInfo=currentBlog;
	}
				
	public String getBlogName() {
		return currentBlogInfo.getName();
	}

	public EncodedImage getBlogIcon() {
		EncodedImage _theImage = null;
		try {
			if(BlogDAO.getBlogIco(currentBlog) != null) {
				_theImage = EncodedImage.createEncodedImage(BlogDAO.getBlogIco(currentBlog), 0, -1);
			}
		} catch (Exception e) {
			Log.error("no valid shortcut ico found in the blog obj");
			_theImage = null;
		}

		if (_theImage == null ) {
			if(currentBlog.isWPCOMBlog()) {
				_theImage = EncodedImage.getEncodedImageResource("wp_blue-s.png");
			} else {
				_theImage = EncodedImage.getEncodedImageResource("wp_grey-s.png");
			}
		}
		
		return _theImage;
	}

	public void showView(){
		try {
			this.currentBlog = BlogDAO.getBlog(currentBlogInfo);
		} catch (Exception e) {
			displayError(e, "Loading Blog Error");
		}
		this.view= new BlogView(this);
		UiApplication.getUiApplication().pushScreen(view);
	}
		
	public void showComments() {
		if (currentBlog != null) {
			FrontController.getIstance().showCommentsView(currentBlog);
		}
	}
	
	public void showPosts() {
		if (currentBlog != null) {
			FrontController.getIstance().showPostsView(currentBlog);
		}
	}
	
	public void showPages() {
		if (currentBlog != null) {
			PagesController ctrl=new PagesController(currentBlog);
			ctrl.showView();
		}
	}
	
	
	public void showMediaLibrary() {
		if (currentBlog != null) {
			MediaLibrariesController ctrl = new MediaLibrariesController(currentBlog);
			ctrl.showView();
		}
	}
	
	public void showStats() {
		if (currentBlog != null) {
			StatsController ctrl = new StatsController(currentBlog);
			ctrl.showView();
		}
	}
	
	/** refresh all blog information */
	public void refreshBlog(){
		if(currentBlog != null) {
			
			final BlogUpdateConn connection = new BlogUpdateConn (currentBlog);       
	        connection.addObserver(new RefreshBlogCallBack()); 
	         connectionProgressView= new ConnectionInProgressView(
	        		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
	       
	        connection.startConnWork(); //starts connection
					
			int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				connection.stopConnWork(); //stop the connection if the user click on cancel button
			}
			
		}
	 }
	
	private class RefreshBlogCallBack implements Observer {

		public void update(Observable observable, final Object object) {

			Log.trace(">>>Refreshing Blog Response");
			
			dismissDialog(connectionProgressView);

			BlogConnResponse resp= (BlogConnResponse) object;

			if(resp.isStopped()){
				return;
			}
			if(!resp.isError()) {
				try{
					currentBlog = (Blog) resp.getResponseObject(); 	//update blogs obj	
					currentBlog.setLoadingState(BlogInfo.STATE_LOADED);
					BlogDAO.updateBlog(currentBlog);							
					CommentsDAO.cleanGravatarCache(currentBlog);
				} catch (final Exception e) {
					displayErrorAndWait(e,"Error while storing the blog data");
				}

			} else {

				currentBlog.setLoadingState(BlogInfo.STATE_ERROR);
				final String respMessage = resp.getResponse();
				displayError(respMessage);

				try {
					BlogDAO.updateBlog(currentBlog);
				} catch (Exception e) {
					displayErrorAndWait(e,"Error while storing the blog data");	
				}
			}//end else

			//update app blog
			WordPressCore wpCore = WordPressCore.getInstance();
			Vector applicationBlogs = wpCore.getApplicationBlogs();
			
			//update application blogs
			final BlogInfo currentBlogI = new BlogInfo(currentBlog);
			for(int count = 0; count < applicationBlogs.size(); ++count)
			{
				BlogInfo applicationBlogTmp = (BlogInfo)applicationBlogs.elementAt(count);
				if (applicationBlogTmp.equals(currentBlogI) )		
				{
					applicationBlogs.setElementAt(currentBlogI, count);
					//update the main blogs view
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							MainController.getIstance().updateBlogListEntry(currentBlogI);
						}
					});
					break;
				}
			}
			
			//update the title
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					view.setBlogHeader(currentBlogI.getName(), getBlogIcon());
				}
			});
			

		}//end update
	}

		
	public void showBlogOptions() {
		if (currentBlog != null) {
			FrontController.getIstance().showBlogOptions(currentBlog);
		}
	}

	//called from the front controller
	public void refreshView() {
	}

	public BlogInfo getCurrentBlogInfo() {
		return currentBlogInfo;
	}	
}