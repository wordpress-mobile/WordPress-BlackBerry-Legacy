package com.wordpress.view;

import net.rim.device.api.ui.MenuItem;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Category;


public class PostStatusView extends BaseView {
	
    private PostController controller; //controller associato alla view
    
    public PostStatusView(PostController _controller, Category[] blogCategories, int[] postCategoriesID) {
    	super(_resources.getString(WordPressResource.MENUITEM_POST_CATEGORIES));
    	this.controller=_controller;
    }
    
    private MenuItem _newCategoryContextMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_POST_NEWCATEGORY, 10, 2) {
        public void run() {
        }
    };
  
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {

		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
	
		
}


