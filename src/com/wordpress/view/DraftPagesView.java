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
import com.wordpress.view.component.PostsListField;

public class DraftPagesView extends BaseView {
	
    private DraftPagesController controller= null;
	private PostsListField pageListField; 
	
	 public DraftPagesView(DraftPagesController  _controller, Page[] pages) {
	    	super(_resources.getString(WordPressResource.TITLE_DRAFT_PAGES)+" > "+_controller.getCurrentBlogName());
	    	this.controller=_controller;	        
	        buildList(pages);
	 }


	 private void buildList(Page[] pages) {
		 removeAllMenuItems();	
		 pageListField = new PostsListField();
		 pageListField.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_DRAFT_PAGES), DrawStyle.LEFT);
		 
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
    
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		controller.toPageList();
		return true;
	}

    
    private MenuItem _deleteItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 220, 10) {
        public void run() {
            int selectedPost = pageListField.getSelectedIndex();
            controller.deletePage(selectedPost);    
        }
    };
    
    private MenuItem _editItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200, 10) {
        public void run() {
            int selectedPost = pageListField.getSelectedIndex();
            controller.editPage(selectedPost);            
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
}

