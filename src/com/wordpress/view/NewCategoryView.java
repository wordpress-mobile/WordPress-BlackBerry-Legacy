package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Category;


public class NewCategoryView extends BaseView {
	
    private PostController controller; //controller associato alla view
	private ObjectChoiceField parentCat;
	private BasicEditField catField;
	private Category[] blogCategories;
	
    public NewCategoryView(PostController _controller, Category[] blogCategories) {
    	super(_resources.getString(WordPressResource.MENUITEM_POST_NEWCATEGORY));
    	this.controller=_controller;
    	this.blogCategories=blogCategories;
    	
    	String[] catTitles = new String [blogCategories.length];
    	for (int i = 0; i < catTitles.length; i++) {
			Category category = blogCategories[i];
			catTitles[i]=category.getLabel();
    	}
    	    	
        //row Label 
        HorizontalFieldManager rowTitle = new HorizontalFieldManager();
		LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_POST_TITLE));
		catField = new BasicEditField("", "", 100, Field.EDITABLE);
		catField.setMargin(margins);
        rowTitle.add(lblTitle);
        rowTitle.add(catField);
        this.add(rowTitle);
        
        //row Parent
        HorizontalFieldManager rowParent = new HorizontalFieldManager();
		LabelField lblParent = getLabel(_resources.getString(WordPressResource.LABEL_POST_PARENTCATEGORY));
		parentCat = new ObjectChoiceField("", catTitles, 0);
		parentCat.setMargin(margins);
		rowParent.add(lblParent);
		rowParent.add(parentCat);
        this.add(rowParent);
    	
    }
    
    private MenuItem _newCategoryContextMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_POST_NEWCATEGORY, 10, 2) {
        public void run() {
        	        	
        }
    };
  
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
		
}
