package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.PageDAO;
import com.wordpress.model.Blog;
import com.wordpress.model.Page;
import com.wordpress.utils.log.Log;
import com.wordpress.view.DraftPagesView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class DraftPagesController extends BaseController {
	
	private DraftPagesView view = null;
	ConnectionInProgressView connectionProgressView=null;
	private Blog currentBlog=null;
	private Page[] pages;
	private String[] pagesFileName;
	
	private boolean prevScreenNeedUpdate = false;
	
	public DraftPagesController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
	}

	public void showView() {
	    try {
		    this.view= new DraftPagesView(this,pages);
			UiApplication.getUiApplication().pushScreen(view);
			
		} catch (Exception e) {
	    	displayError(e, "Error while reading drafts phones memory");
		}
	}

	private void loadDraftPages() throws IOException {
		
		//if we can't load file name index, we exit immediately		
		try {
			pagesFileName= PageDAO.loadPagesFileName(currentBlog);
		} catch (Exception e) {
			Log.error(e, "Could not load draft pages index from disk");
			throw new IOException ("Could not load draft pages index from disk");
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
				Log.error(e, "Could not load some pages from disk");
				throw new IOException ("Could not load some pages from disk");
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
		int result=this.askQuestion(_resources.getString(WordPressResource.MESSAGE_DELETE_PAGE));   
    	if(Dialog.YES != result ) return; 
		
		String pageFileName = pagesFileName[selected];
		try {
			PageDAO.removePage(currentBlog, Integer.parseInt(pageFileName) );
		} catch (IOException e) {
			displayError(e, "Error while deleting draft page");
		} catch (RecordStoreException e) {
			displayError(e, "Error while deleting draft page");
		}
		updateViewDraftPageList();
	}

	public void updateViewDraftPageList() {
	 try {
			loadDraftPages();
			view.refresh(pages);
		} catch (Exception e) {
	    	displayError(e, "Error while reading drafts phones memory");
		}
	}
	
	//return back to page list. update screen if it is necessary
	public void toPageList() {
		if(prevScreenNeedUpdate == false)
			backCmd();
		else
			FrontController.getIstance().backAndRefreshView(true); //deep refresh
		
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
	
	//invoke from controller...
	public void refreshView() {
		//set variable that indicate if prev screen need update when dismiss this view
		//could be a draft post submit
		try {
			prevScreenNeedUpdate = true;
		} catch (Exception e) {
		}
	}	
}