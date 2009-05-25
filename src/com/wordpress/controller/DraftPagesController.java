package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;

import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.view.DraftPagesView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class DraftPagesController extends BaseController {
	
	private DraftPagesView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private Blog currentBlog=null;
	private Page[] pages;
	private String[] pagesFileName;
	private boolean isLoadError= false;
	private String loadErrorMessage= "";
	
	public DraftPagesController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
	}

	public void showView() {
	    try {
	    	loadDraftPages();	    	
		    this.view= new DraftPagesView(this,pages);
			UiApplication.getUiApplication().pushScreen(view);
			
			if(isLoadError) {
				displayError(loadErrorMessage);
			}
			
		} catch (Exception e) {
	    	displayError(e, "Error while reading drafts phones memory");
		}
	}

	private void loadDraftPages()  {
		
		//if we can't load file name index, we exit immediately		
		try {
			pagesFileName= PageDAO.loadPagesFileName(currentBlog);
		} catch (Exception e) {
			isLoadError = true;
			loadErrorMessage = "Could not load draft pages index from disk";
			return;
		}
		
		//try to read data from storage.
		Vector loadedPages = new Vector();
		Vector loadedPagesFileName = new Vector();
		
		for (int i = 0; i < pagesFileName.length; i++) {
			String fileName = pagesFileName[i];
			
			Hashtable loadPage;
			try {
				loadPage = PageDAO.loadPage(currentBlog, fileName);		
				Page hashtable2Page = PageDAO.hashtable2Page(loadPage);
				loadedPages.addElement(hashtable2Page);		
				loadedPagesFileName.addElement(fileName);
			} catch (Exception e) {
				isLoadError = true;
				loadErrorMessage = "Could not load some pages from disk";
			}
		}
		
		pages = new Page[loadedPages.size()];
		pagesFileName = new String[loadedPages.size()];
		loadedPages.copyInto(pages);
		loadedPagesFileName.copyInto(pagesFileName);
	}


	public String getCurrentBlogName() {
		return currentBlog.getName();
	}

	
	public void deletePage(int selected){
		String pageFileName = pagesFileName[selected];
		try {
			PageDAO.removePage(currentBlog, Integer.parseInt(pageFileName) );
		} catch (IOException e) {
			displayError(e, "Error while deleting draft page");
		} catch (RecordStoreException e) {
			displayError(e, "Error while deleting draft page");
		}
		refreshView();
	}

	
	public void newPage() {
		if (currentBlog != null) {
			Page page =new Page();
			PageController ctrl=new PageController(currentBlog, page);
			ctrl.showView();
		}
	}
	
	/** starts the post loading */
	public void editPage(int selected) {
		try {
			if (selected != -1) {
				String pageFileName = pagesFileName[selected];
				PageController ctrl=new PageController(currentBlog, pages[selected], Integer.parseInt(pageFileName));
				ctrl.showView();
			}
		} catch (Exception e) {
			displayError(e, "Error while loading draft page");
		}
	}	
	

	public void refreshView() {
		 try {
			loadDraftPages();
			view.refresh(pages);
		} catch (Exception e) {
	    	displayError(e, "Error while reading drafts phones memory");
		}
	}
}