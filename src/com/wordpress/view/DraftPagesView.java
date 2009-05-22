package com.wordpress.view;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.DraftPagesController;
import com.wordpress.model.Page;

public class DraftPagesView extends BaseView {
	
    private DraftPagesController controller= null;
	private ObjectListField pageListField; 
	
	 public DraftPagesView(DraftPagesController  _controller, Page[] pages) {
	    	super(_controller.getCurrentBlogName()+" > "+_resources.getString(WordPressResource.TITLE_DRAFT_PAGES));
	    	this.controller=_controller;	        
	        buildList(pages);
	 }


	private void buildList(Page[] pages) {
		removeAllMenuItems();	
		pageListField = new ObjectListField();
		pageListField.setEmptyString("No Draft Pages", DrawStyle.LEFT);
		
		String[] draftPageTitle = new String[0];
		
		if(pages != null) {
			draftPageTitle = new String[pages.length];
			for (int i = 0; i < pages.length; i++) {
				String title = pages[i].getTitle();
				draftPageTitle[i] = title;
			}
		}
		
		if(pages.length > 0 ){
			pageListField.set(draftPageTitle);
			addMenuItem(_editItem);
			addMenuItem(_deleteItem);
		} 
		
		addMenuItem(_newItem);
		add(pageListField);
	}
	
    public void refresh(Page[] pages){
    	this.delete(pageListField);
    	buildList(pages);
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

