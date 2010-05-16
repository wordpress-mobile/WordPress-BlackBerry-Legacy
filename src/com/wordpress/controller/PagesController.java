package com.wordpress.controller;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PagesView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.page.DeletePageConn;
import com.wordpress.xmlrpc.page.GetPagesConn;


public class PagesController extends BaseController{
	
	private PagesView view = null;
	private Blog currentBlog;
	private Page[] pages;
	ConnectionInProgressView connectionProgressView=null;
 
	public PagesController(Blog currentBlog) {
		super();
		this.currentBlog=currentBlog;
	}
			
	public void showView(){
		pages = PageDAO.buildPagesArray(currentBlog.getPages());
		this.view= new PagesView(this, pages, countNewPages());
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public String getBlogName() {
		return currentBlog.getName();
	}
	
	//count number of new pages
	private int countNewPages() {
		Vector pages = currentBlog.getPages();
		if(pages == null) 
			return 0;
		
		
		int count = 0;
		int[] viewedPages = currentBlog.getViewedPages();
		int[] newViewedPage = new int[pages.size()];

		for (int i = 0; i < pages.size(); i++) {
			boolean presence = false; 
			Hashtable page = (Hashtable) pages.elementAt(i);
            int pageId = Integer.parseInt( String.valueOf(page.get("page_id")));
            //add the pages into NEW viewed pages array
            newViewedPage[i]= pageId;
			
		    for (int j = 0; j < viewedPages.length; j++) {
		    	int viewedPageID = viewedPages[j];
	            	    	
		    	if (pageId == viewedPageID) {
		    		presence = true;
		    		break;
		    	}
			}
		    
		    if (!presence) 
		    	count++;
		}
		
		currentBlog.setViewedPages(newViewedPage);
		return count;
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
		
    		int pageID = pages[selectedIndex].getID();
    		    						 
			DeletePageConn connection = new DeletePageConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
					 currentBlog.getPassword(), Integer.parseInt(currentBlog.getId()), pageID);
		     
			 connection.addObserver(new deletePageCallBack(selectedIndex)); //not page id, selectedID
		     
		     connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONN_DELETE_PAGE));
	  
		    connection.startConnWork(); //starts connection
		    int choice = connectionProgressView.doModal();
			if(choice==Dialog.CANCEL) {
				Log.trace("Chiusura della conn dialog tramite cancel");
				connection.stopConnWork(); //stop the connection if the user click on cancel button
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
		System.out.println(">>>refreshPosts");
        final GetPagesConn connection = new GetPagesConn (currentBlog.getXmlRpcUrl(),currentBlog.getUsername(),
        		currentBlog.getPassword(),  Integer.parseInt(currentBlog.getId()), currentBlog.getMaxPostCount());
        
        connection.addObserver(new refreshPageCallBack()); 
        String connMsg=_resources.getString(WordPressResource.CONN_LOADING_PAGES);
        connectionProgressView= new ConnectionInProgressView(connMsg);
       
        connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			Log.trace("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}
	}
	
	
	private class deletePageCallBack implements Observer{
		
		int index;
		
		public deletePageCallBack(int index) {
			super();
			this.index = index;
		}

		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					System.out.println(">>>deletePageResponse");
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
							view.refresh(pages , countNewPages());
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
					
					
					}//and run 
			});
		}
	}
		
	
	private class refreshPageCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					System.out.println(">>>loadPageResponse");

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
							
					if(resp.isStopped()){
						return;
					}
					if(!resp.isError()) {
						
						Vector respVector= (Vector) resp.getResponseObject();
						currentBlog.setPages(respVector);
						pages = PageDAO.buildPagesArray(currentBlog.getPages());
						view.refresh(pages , countNewPages());
						
						/*
						//setting the NEW viewed page
						int[] pagesID = new int[pages.length];
						for (int i = 0; i < pages.length; i++) {
							Page curr = pages[i];
							int id = curr.getID();
							pagesID[i] = id;
						}
						currentBlog.setViewedPages(pagesID);
						*/
						
						try{
							BlogDAO.updateBlog(currentBlog);							
						} catch (final Exception e) {
						 	displayError(e,"Error while storing pages");	
						}
						
						
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}

					
					}//end run 
			});
		}
	}

	
	/*
	//callback for post loading
	class loadPostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					System.out.println(">>>loadPostResponse");

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
							
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
						Post post=(Post)resp.getResponseObject();
						FrontController.getIstance().showPost(post);	
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}
				
				}
			});
		}
	} */
}
