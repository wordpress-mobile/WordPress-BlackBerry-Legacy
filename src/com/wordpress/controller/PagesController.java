package com.wordpress.controller;

import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.task.StopConnTask;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PagesView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.ParameterizedBlogConn;
import com.wordpress.xmlrpc.page.GetPagesConn;


public class PagesController extends BaseController{
	
	private PagesView view = null;
	private Blog currentBlog;
	private Page[] pages;
	ConnectionInProgressView connectionProgressView=null;
	
	protected GetPagesConn loadMoreConnection = null;
	private boolean isLoadingMore = false;
	private boolean hasMorePosts = true;
 
	public PagesController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
	}
			
	public void showView(){
		pages = PageDAO.buildPagesArray(currentBlog.getPages());
		this.view= new PagesView(this, pages);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public Blog getBlog() {
		return currentBlog;
	}
	
	public String getBlogName() {
		return currentBlog.getName();
	}
	
	public void editPage(int selected){
		Page selectedPage = pages[selected];
		PageController ctrl=new PageController(currentBlog, selectedPage);
		ctrl.showView();
	}	

	public void deletePage(int selectedIndex){
		if(selectedIndex == -1) return;
		
		int result=this.askQuestion(_resources.getString(WordPressResource.MESSAGE_DELETE_PAGE));   
		
    	if(Dialog.YES==result) {
		
    		String pageID = pages[selectedIndex].getID();
    		
			Vector args = new Vector(5);
			args.addElement(currentBlog.getId());
			args.addElement(currentBlog.getUsername());
			args.addElement(currentBlog.getPassword());
			args.addElement(pageID);
    				 
			ParameterizedBlogConn connection = new ParameterizedBlogConn (currentBlog.getXmlRpcUrl(),
					"wp.deletePage", args);
			if(currentBlog.isHTTPBasicAuthRequired()) {
				connection.setHttp401Password(currentBlog.getHTTPAuthPassword());
				connection.setHttp401Username(currentBlog.getHTTPAuthUsername());
			}
			 connection.addObserver(new DeletePageCallBack(selectedIndex)); //not page id, selectedID
		     
		     connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_DELETE_PAGE));
	  
		    connection.startConnWork(); //starts connection
		    int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				WordPressCore.getInstance().getTasksRunner().enqueue(new StopConnTask(connection));
			}
    	}		
	}
	
			
	public void showDraftPages(){
		if(currentBlog != null) {
			DraftPagesController ctrl=new DraftPagesController(currentBlog);
			ctrl.showView();
		}
	}
		

	public void newPage() {
		if (currentBlog != null) {
			Page page =new Page();
			PageController ctrl=new PageController(currentBlog, page);
			ctrl.showView();
		}
	}

	//called from the front controller
	public void refreshView() {
		refreshPagesList();
	}
	
	public void refreshPagesList() {

		//stop the load more and reset the variable here
		if( isLoadingMore ) view.showLoadMoreStatus( false );
		isLoadingMore = false;
		hasMorePosts = true;
		if( loadMoreConnection != null ) loadMoreConnection.stopConnWork();
				
        final GetPagesConn connection = new GetPagesConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(),  currentBlog.getId(), WordPressInfo.DEFAULT_ITEMS_NUMBER);
		if(currentBlog.isHTTPBasicAuthRequired()) {
			connection.setHttp401Password(currentBlog.getHTTPAuthPassword());
			connection.setHttp401Username(currentBlog.getHTTPAuthUsername());
		}
        connection.addObserver(new RefreshPageCallBack()); 
        String connMsg=_resources.getString(WordPressResource.CONN_LOADING_PAGES);
        connectionProgressView= new ConnectionInProgressView(connMsg);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			WordPressCore.getInstance().getTasksRunner().enqueue(new StopConnTask(connection));
		}
	}
	
	public void loadMorePosts() {
		Log.debug("Ctrl load more posts");
		if ( hasMorePosts == false || isLoadingMore ) {
			return;
		}
		view.showLoadMoreStatus(true);
		isLoadingMore = true;
        int numerberOfPosts = 0;
        numerberOfPosts = Math.max(currentBlog.getPages().size(), WordPressInfo.DEFAULT_ITEMS_NUMBER);
        if (hasMorePosts) {
            numerberOfPosts += WordPressInfo.DEFAULT_ITEMS_NUMBER;
        } else {
            //removing this block you will enable the refresh of posts when reached the end of the list and no more posts are available
            isLoadingMore = false;
            return;
        }
		
		loadMoreConnection = new GetPagesConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(), currentBlog.getId(), numerberOfPosts);
		loadMoreConnection.addObserver(new LoadMorePagesCallBack());       
		loadMoreConnection.startConnWork(); //starts connection
	}
	
	private class LoadMorePagesCallBack implements Observer{
		public void update(Observable observable, final Object object) {

			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run(){
					view.showLoadMoreStatus(false);
				}
			});

			int numberOfRequestedPosts = loadMoreConnection.getMaxPagesNumber();
			loadMoreConnection = null;
			isLoadingMore = false;
			BlogConnResponse resp = (BlogConnResponse) object;
			if(resp.isStopped()){
				return;
			}
			if(!resp.isError()) {

				final Vector respVector= (Vector) resp.getResponseObject();
				// If we asked for more and we got what we had, there are no more posts to load
				if ( numberOfRequestedPosts > WordPressInfo.DEFAULT_ITEMS_NUMBER && (respVector.size() <= currentBlog.getPages().size()))
				{
					hasMorePosts = false;
				}
				else if (respVector.size() == WordPressInfo.DEFAULT_ITEMS_NUMBER)
				{
					//we should reset the flag otherwise when you refresh this blog you can't get more than CHUNK_SIZE posts
					hasMorePosts = true;
				}

				currentBlog.setPages(respVector);
				pages = PageDAO.buildPagesArray(respVector);

				try{
					BlogDAO.updateBlog(currentBlog);							
				} catch (final Exception e) {
				}

				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run(){
						view.addPostsToScreen(pages);
					}
				});
			} else {
				hasMorePosts = false; //error response, do not keep downloading more posts
			}
		}
	}
	
	private class DeletePageCallBack implements Observer{
		
		int index;
		
		public DeletePageCallBack(int index) {
			super();
			this.index = index;
		}

		public void update(Observable observable, final Object object) {

			Log.debug(">>>deletePageResponse");
			dismissDialog(connectionProgressView);
			BlogConnResponse resp= (BlogConnResponse) object;
			if(resp.isStopped()){
				return;
			}
			if(!resp.isError()) {

				Boolean deleteResp= (Boolean) resp.getResponseObject();
				if(deleteResp.booleanValue()) {
					//delete ok
					Vector blogPages = currentBlog.getPages();
					blogPages.removeElementAt(index);
					currentBlog.setPages(blogPages);
					pages = PageDAO.buildPagesArray(blogPages);

					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run(){
							view.refresh(pages);
						}
					});

					try{
						BlogDAO.updateBlog(currentBlog);							
					} catch (final Exception e) {
						displayError(e,"Error while update local blog data");	
					}

				} else {
					displayError("Error while deleting Page");
				}
			} else {  
				final String respMessage=resp.getResponse();
				displayError(respMessage);	
			}
		}
	}
		
	
	private class RefreshPageCallBack implements Observer{
		public void update(Observable observable, final Object object) {

			Log.debug(">>>loadPageResponse");

			dismissDialog(connectionProgressView);
			BlogConnResponse resp= (BlogConnResponse) object;

			if(resp.isStopped()){
				return;
			}
			if(!resp.isError()) {

				Vector respVector= (Vector) resp.getResponseObject();
				currentBlog.setPages(respVector);
				pages = PageDAO.buildPagesArray(respVector);

				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run(){
						view.refresh(pages);
					}
				});

				try{
					BlogDAO.updateBlog(currentBlog);							
				} catch (final Exception e) {
					displayError(e,"Error while storing pages");	
				}


			} else {
				final String respMessage=resp.getResponse();
				displayError(respMessage);	
			}
		}
	}
}
