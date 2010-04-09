//#preprocess
package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PagesController;
import com.wordpress.model.Page;
import com.wordpress.view.component.ListActionListener;
import com.wordpress.view.component.PostsListField;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif

public class PagesView extends BaseView implements ListActionListener {
	
    private PagesController controller= null;
    private PostsListField  pagesList; 
    private VerticalFieldManager dataScroller;

	
	 public PagesView(PagesController _controller, Page[] pages, int numberOfNewPosts) {
	    	super(_resources.getString(WordPressResource.TITLE_PAGES)+ " > "+ _controller.getBlogName(), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
	        
	        if(pages != null) {
	        	String subTitleValue = getNumberOfTotalPageLabel(pages.length)+ " " + getNumberOfNewPageLabel(numberOfNewPosts);
	        	this.setSubTitleText(subTitleValue);
	        } else {
	        	this.setSubTitleText("");
	        }
	        
	        //A HorizontalFieldManager to hold the posts list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);
	        
	        //#ifdef IS_OS47_OR_ABOVE
        	initUpBottomBar();
        	//#endif
		
			add(dataScroller);
			buildList(pages);
	 }
	 
		//#ifdef IS_OS47_OR_ABOVE
		private void initUpBottomBar() {
			if (Touchscreen.isSupported() == false) return;
			
			BottomBarItem items[] = new BottomBarItem[3];
			items[0] = new BottomBarItem("write.png", "write.png", _resources.getString(WordPressResource.MENUITEM_NEW));
			items[1] = new BottomBarItem("browser.png", "browser.png", _resources.getString(WordPressResource.MENUITEM_LOCALDRAFTS));
			items[2] = new BottomBarItem("refresh.png", "refresh.png", _resources.getString(WordPressResource.MENUITEM_REFRESH));
			
			initializeBottomBar(items);
		}
		
		protected void bottomBarActionPerformed(int mnuItem) {
			switch (mnuItem) {
			case 0:
				controller.newPage();	
				break;
			case 1:
				controller.showDraftPages(); 
				break;
			case 2:
				controller.refreshPagesList(); //reload only the posts list			
				break;
			default:
				break;
			}
		}
	//#endif
	 

	 private String getNumberOfNewPageLabel(int recentPage) {
	        String numerOfPageLabel = null;
	        numerOfPageLabel=" (" +recentPage+" "+_resources.getString(WordPressResource.LABEL_PAGE_NUMBER_NEW)+")";
	        return numerOfPageLabel;
		 
	 }
	 
	 private String getNumberOfTotalPageLabel(int pageNum) {
	        String numerOfPostLabel = null;
	        
	        if(pageNum > 1) 
	        	numerOfPostLabel= pageNum+ " " +_resources.getString(WordPressResource.LABEL_PAGE_NUMBERS); 
	        else 
	        	numerOfPostLabel= pageNum+ " " +_resources.getString(WordPressResource.LABEL_PAGE_NUMBER);
	        
	        return numerOfPostLabel;
		 
	 }
	 
	private void buildList(Page[] pages) {
        
		Hashtable elements[]= new Hashtable[0];
		
		if(pages != null) {						
			elements= new Hashtable[pages.length];
			//Populate the vector with the elements [title, data, title, data ....]
	        for (int i = 0; i < pages.length; i++) {
	        	Page currentPage = pages[i];
	            String title = currentPage.getTitle();
	             if (title == null || title.length() == 0) {
	                 title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
	             }
	             
	             Hashtable smallPostData = new Hashtable();
	        	 
	             smallPostData .put("title", title);
	             Date dateCreated = currentPage.getDateCreatedGMT();
	             if (dateCreated != null)
	            	 smallPostData .put("date_created_gmt", dateCreated);
	             
	             elements[i]=smallPostData;
	         }			
		}
						
		pagesList = new PostsListField(); 	        
		pagesList.set(elements);
		pagesList.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_POSTS), DrawStyle.LEFT);
		pagesList.setDefautActionListener(this);
		
		dataScroller.add(pagesList);
		pagesList.setFocus(); //set the focus over the list
	}
	 

    //Override the makeMenu method so we can add a custom menu item
    //if the checkbox ListField has focus.
    protected void makeMenu(Menu menu, int instance)
    {
    	
        Field focus = getLeafFieldWithFocus();
        if(pagesList != null && focus == pagesList) 
        {
        	if(pagesList.getSize() > 0 ) {
        		menu.add(_editPageMenuItem);
        		menu.add(_deletePageMenuItem);
    		}
        }
        
        menu.add(_newPageMenuItem);
        menu.add(_draftPageMenuItem);
        menu.add(_refreshPageListItem);                
        
        //Create the default menu.
        super.makeMenu(menu, instance);
    }

    private MenuItem _newPageMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_NEW, 200, 10) {
        public void run() {
        	controller.newPage();
        }
    };
    
    private MenuItem _draftPageMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_LOCALDRAFTS, 210, 10) {
        public void run() {
    	 controller.showDraftPages(); 
        }
    };
 
    private MenuItem _editPageMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200000, 10) {
    	public void run() {
    		int selectedPage = pagesList.getSelectedIndex();
    		controller.editPage(selectedPage);
    	}
    };
    
	private MenuItem _deletePageMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 210000, 10) {
        public void run() {
            int selectedPage = pagesList.getSelectedIndex();
            controller.deletePage(selectedPage);
        }
    };
    
    private MenuItem _refreshPageListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH, 220000, 10) {
        public void run() {
        	controller.refreshPagesList();
        }
    };
     

    	
	public void refresh(Page[] pages, int count){
		dataScroller.delete(pagesList);
				
        if(pages != null) {
        	String subTitleValue = getNumberOfTotalPageLabel(pages.length)+ " " + getNumberOfNewPageLabel(count);
        	this.setSubTitleText(subTitleValue);
        } else {
        	this.setSubTitleText("");
        }
		
		buildList(pages);
	}

	public BaseController getController() {
		return this.controller;
	}
	
	public boolean onClose()   {
		controller.backCmd();
		return true;
	}
	
	public void actionPerformed() {
        int selectedPage = pagesList.getSelectedIndex();
        controller.editPage(selectedPage);    
	}
}