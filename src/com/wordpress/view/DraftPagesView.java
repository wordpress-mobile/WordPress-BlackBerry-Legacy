//#preprocess
package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.MenuItem;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.DraftPagesController;
import com.wordpress.model.Page;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ListActionListener;
import com.wordpress.view.component.PostsListField;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif

public class DraftPagesView extends BaseView implements ListActionListener {
	
    private DraftPagesController controller= null;
	private PostsListField pageListField; 
	
	 public DraftPagesView(DraftPagesController  _controller, Page[] pages) {
	    	super(_resources.getString(WordPressResource.TITLE_DRAFT_PAGES)+" > "+_controller.getCurrentBlogName());
	    	this.controller=_controller;	        
	        buildList(pages);
	 }
	 
	 //#ifdef IS_OS47_OR_ABOVE
	 private void initUpBottomBar(int size) {
		 if (Touchscreen.isSupported() == false) return;

		 int numberOfButtons = 1;
		 if( size > 1 ){
			 numberOfButtons = 2;
		 }
		 BottomBarItem items[] = new BottomBarItem[numberOfButtons];
		 items[0] = new BottomBarItem("write_page-vs.png", "write_page-vs.png", _resources.getString(WordPressResource.MENUITEM_NEW));
		 if(numberOfButtons == 2)
			 items[1] = new BottomBarItem("stop.png", "stop.png", _resources.getString(WordPressResource.MENUITEM_DELETE));

		 initializeBottomBar(items);
	 }

	 protected void bottomBarActionPerformed(int mnuItem) {
		 switch (mnuItem) {
		 case 0:
			 controller.newPage();
			 break;
		 case 1:
			 int selectedPost = pageListField.getSelectedIndex();
			 controller.deletePage(selectedPost);
			 break;
		 default:
			 break;
		 }
	 }
	 //#endif


	 private void buildList(Page[] pages) {
		 removeAllMenuItems();	
		 pageListField = new PostsListField();
		 pageListField.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_DRAFT_PAGES), DrawStyle.LEFT);
		 pageListField.setDefautActionListener(this);
		 
		 Hashtable[] draftPageInfo = new Hashtable[0];
		 
		 if(pages != null) {
			 draftPageInfo = new Hashtable[pages.length];
			 
			 for (int i = 0; i < pages.length; i++) {
				 String title = pages[i].getTitle();
				 if (title == null || title.length() == 0) {
					 title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
				 }
				 
				 Hashtable smallPostData = new Hashtable();
				 smallPostData .put("title", title);
				 Date dateCreated =  pages[i].getDateCreatedGMT();
				 if (dateCreated != null)
					 smallPostData .put("date_created_gmt", dateCreated);
				 
				 draftPageInfo[i] = smallPostData;
			 }
			 
			 if( pages.length > 0 ){
				 pageListField.set(draftPageInfo);
				 addMenuItem(_editItem);
				 addMenuItem(_deleteItem);
			 } 
			 
		 }
		 
		 //#ifdef IS_OS47_OR_ABOVE
		 int size = 0;
		 if ((pages != null) && pages.length > 0)
			 size = 3;
		 initUpBottomBar(size);
		 //#endif
		 
		 addMenuItem(_newItem);
		 add(pageListField);
	 }
	
    public void refresh(Page[] pages){
    	this.delete(pageListField);
    	buildList(pages);
    }
	
    protected void onVisibilityChange(boolean visible) {
    	super.onVisibilityChange(visible);
    	Log.trace("onVisibilityChange DraftPageView "+ visible);
    	if(visible) 
    		controller.updateViewDraftPageList();
    }
    
	public boolean onClose()   {
		controller.toPageList();
		return true;
	}

    
    private MenuItem _deleteItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 220, 10) {
        public void run() {
            int selectedPage = pageListField.getSelectedIndex();
            controller.deletePage(selectedPage);    
        }
    };
    
    private MenuItem _editItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200, 10) {
        public void run() {
            int selectedPage = pageListField.getSelectedIndex();
            controller.editPage(selectedPage);            
        }
    };
    
    private MenuItem _newItem = new MenuItem( _resources, WordPressResource.MENUITEM_NEW, 210, 10) {
        public void run() {
            controller.newPage();    
        }
    };
    
	public BaseController getController() {
		return controller;
	}
	
	public void actionPerformed() {
        int selectedPost = pageListField.getSelectedIndex();
        controller.editPage(selectedPost);    
	}
	
}

