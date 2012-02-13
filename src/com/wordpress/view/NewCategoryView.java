package com.wordpress.view;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Category;
import com.wordpress.utils.log.Log;
import com.wordpress.view.container.BorderedFieldManager;


public class NewCategoryView extends StandardBaseView {
	
    private PostController controller; //controller associato alla view
	private ObjectChoiceField parentCat;
	private BasicEditField catField;
	private final Category[] blogCategories;
	
    public NewCategoryView(PostController _controller, Category[] blogCategories) {
    	super(_resources.getString(WordPressResource.MENUITEM_POST_NEWCATEGORY), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
    	this.controller=_controller;
		this.blogCategories = blogCategories;

    	String[] catTitles = new String [blogCategories.length+1]; //at index 0 we insert some description text
    	for (int i = 0; i < blogCategories.length; i++) {
			Category category = blogCategories[i];
			catTitles[i+1]=category.getLabel();
    	}
    	catTitles[0]= "Optional";
    	    	
        //row Label 
        BorderedFieldManager rowTitle = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
        
		LabelField lblTitle = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_CATEGORY_NAME), Color.BLACK);
		catField = new BasicEditField("", "", 100, Field.EDITABLE);
        rowTitle.add(lblTitle);
        rowTitle.add(catField);
        
        //row Parent
        BorderedFieldManager rowParent = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
        

		parentCat = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_CATEGORY_PARENT)+": ", catTitles, 0);
		rowParent.add(parentCat);
        
		this.add(rowTitle);
		this.add(rowParent);
        addMenuItem(_newCategoryContextMenuItem);
        controller.bumpScreenViewStats("com/wordpress/view/NewCategoryView", "New Category Screen", "", null, "");
    }
    
    private MenuItem _newCategoryContextMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_POST_NEWCATEGORY, 10, 2) {
        public void run() {
        	int parentCatValue= parentCat.getSelectedIndex()-1; //subtract -1 because we add one element at the beginning
        	int id=0;
        	try {
        		if(parentCatValue >= 0 )
        			id = Integer.parseInt( blogCategories[parentCatValue].getId() );
			} catch (Exception e) {
				Log.error(e, "Error while reading parent category ID");
			}
        	controller.newCategory(catField.getText(), id);       	
        }
    };
  
	public boolean onClose()   {
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
}
