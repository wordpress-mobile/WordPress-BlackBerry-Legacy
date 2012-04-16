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
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.DefaultListKeyListener;
import com.wordpress.view.component.ListActionListener;
import com.wordpress.view.component.ListLoadMoreListener;
import com.wordpress.view.component.GenericListField;

//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif

public class PagesView extends BaseView implements ListActionListener, ListLoadMoreListener {
	
    private PagesController controller= null;
    private GenericListField  pagesList; 
    private VerticalFieldManager dataScroller;
    private DefaultListKeyListener defaultListKeyListener;
	
	 public PagesView(PagesController _controller, Page[] pages) {
	    	super(_controller.getBlogName(), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.setSubTitleText(_resources.getString(WordPressResource.BUTTON_PAGES)); 
	    	this.controller=_controller;
	        //A HorizontalFieldManager to hold the posts list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);
	        
	        //#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
        	initUpBottomBar();
        	//#endif
		
        	defaultListKeyListener = new DefaultListKeyListener(this, pagesList);
        	addKeyListener( defaultListKeyListener );

        	add(dataScroller);
			buildList(pages);
			
			controller.bumpScreenViewStats("com/wordpress/view/PagesView", "Pages List Screen", "", null, "");
	 }
	 
	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		private void initUpBottomBar() {
			if (Touchscreen.isSupported() == false) return;
			
			BottomBarItem items[] = new BottomBarItem[4];
			items[0] = new BottomBarItem("bottombar_add.png", "bottombar_add.png", _resources.getString(WordPressResource.MENUITEM_NEW));
			items[1] = new BottomBarItem("bottombar_delete.png", "bottombar_disabled.png", _resources.getString(WordPressResource.MENUITEM_DELETE));
			items[2] = new BottomBarItem("bottombar_browser.png", "bottombar_browser.png", _resources.getString(WordPressResource.MENUITEM_LOCALDRAFTS));
			items[3] = new BottomBarItem("bottombar_refresh.png", "bottombar_refresh.png", _resources.getString(WordPressResource.MENUITEM_REFRESH));
			
			initializeBottomBar(items);
		}
		
		protected void bottomBarActionPerformed(int mnuItem) {
			switch (mnuItem) {
			case 0:
				controller.newPage();	
				break;
			case 1:
	            int selectedPage = pagesList.getSelectedIndex();
	            controller.deletePage(selectedPage);
				break;
			case 2:
				controller.showDraftPages(); 
				break;
			case 3:
				controller.refreshPagesList(); //reload only the posts list			
				break;
			default:
				break;
			}
		}
	//#endif
	  
	private Hashtable[] prepareItemsForList(Page[] pages){
		Hashtable postStatusHash = controller.getBlog().getPageStatusList();
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

				if ( currentPage.getPageStatus() != null) {
					String statusUndecoded = currentPage.getPageStatus();
					String status = (String) postStatusHash.get(statusUndecoded);

					if(status == null && statusUndecoded.equalsIgnoreCase("future")) {
						status = "Scheduled";
					} 
					if(status != null)
						smallPostData .put("post_status", status);
				}

				elements[i]=smallPostData;
			}			
		}
		return elements;
	}	
		
	private void buildList(Page[] pages) {
		Hashtable elements[] = this.prepareItemsForList(pages); 						
		pagesList = new GenericListField(); 	        
		pagesList.set(elements);
		pagesList.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_PAGES), DrawStyle.LEFT);
		pagesList.setDefautActionListener(this);
		pagesList.setDefautLoadMoreListener(this);
		
		dataScroller.add(pagesList);
		pagesList.setFocus(); //set the focus over the list
		
		defaultListKeyListener.setListObj(pagesList);
		
	    //#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
			if(elements.length == 0) {
				setBottomBarButtonState(1, false); //disable the delete btn
			} else
				setBottomBarButtonState(1, true);
    	//#endif
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
     
	public void refresh(Page[] pages){
		dataScroller.delete(pagesList);
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

	// ListLoadMoreListener methods
	public void loadMore() {
		Log.debug("loadMore listener more called" );
		controller.loadMorePosts();
	}
	public void refreshList() {
		controller.refreshPagesList();	
	}
	//End of ListLoadMoreListener
	
	public void addPostsToScreen(Page[] pages){
		int selectedIndex = pagesList.getSelectedIndex();
		Hashtable elements[] = this.prepareItemsForList(pages);
		pagesList.resetListItems(elements);
		pagesList.setFocus();
		pagesList.setSelectedIndex(selectedIndex);
    }
}