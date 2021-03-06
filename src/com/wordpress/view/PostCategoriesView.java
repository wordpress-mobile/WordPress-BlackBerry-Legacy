package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Category;
import com.wordpress.view.component.CategoriesListField;


public class PostCategoriesView extends BaseView {
	
    private PostController controller;
    private CategoriesListField checkBoxController;
    private ListField chkField;
    
    public void refreshView(Category[] blogCategories, int[] postCategoriesID) {
    	checkBoxController= new CategoriesListField(blogCategories, postCategoriesID);
    	delete(chkField);
    	this.chkField= checkBoxController.get_checkList();
    	add(chkField);
    	this.invalidate();
    }
    
    public PostCategoriesView(PostController _controller, Category[] blogCategories, int[] postCategoriesID) {
    	super(_resources.getString(WordPressResource.MENUITEM_POST_CATEGORIES));
    	this.controller=_controller;
    	checkBoxController= new CategoriesListField(blogCategories, postCategoriesID);
    	this.chkField= checkBoxController.get_checkList();
    	add(chkField);
    	addMenuItem(_newCategoryContextMenuItem);
    	controller.bumpScreenViewStats("com/wordpress/view/PostCategoriesView", "PostCategories Screen", "", null, "");
    }
    
    private MenuItem _newCategoryContextMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_POST_NEWCATEGORY, 200, 100) {
        public void run() {
        	controller.showNewCategoriesView();
        }
    };
  
	public boolean onClose()   {
		Category[] selectedCategories = checkBoxController.getSelectedCategories();
		controller.setPostCategories(selectedCategories); //sets the post new selected categories
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
	
	
	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus
		// is on the list
		if (getLeafFieldWithFocus() == chkField
				&& instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
	}
	
    /*Override the makeMenu method so we can add a custom menu item
     when the checkbox ListField has focus. */
    protected void makeMenu(Menu menu, int instance)
    {
        Field focus = UiApplication.getUiApplication().getActiveScreen().getLeafFieldWithFocus();
        if(focus == chkField) 
        {
            //The commentsList ListField instance has focus.
            //Add the _toggleItem MenuItem.
        	checkBoxController.changeToggleItemLabel(_resources.getString(WordPressResource.MENUITEM_SELECT), _resources.getString(WordPressResource.MENUITEM_DESELECT));
            menu.add(checkBoxController._toggleItem);
        }
                
        //Create the default menu.
        super.makeMenu(menu, instance);
    }    		
}