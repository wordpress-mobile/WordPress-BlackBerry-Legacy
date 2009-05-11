package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Category;
import com.wordpress.view.component.CheckBoxListField;


public class CategoriesView extends BaseView {
	
    private PostController controller; //controller associato alla view
    private ListField chkField;
    private Category[] blogCategories;
    
    public CategoriesView(PostController _controller, Category[] blogCategories, int[] postCategoriesID) {
    	super(_resources.getString(WordPressResource.MENUITEM_POST_CATEGORIES));
    	this.controller=_controller;
    	this.blogCategories=blogCategories;
    	
    	String[] catTitles = new String [blogCategories.length];
    	boolean[] catCheck = new boolean [blogCategories.length];
    	for (int i = 0; i < catCheck.length; i++) {
			Category category = blogCategories[i];
			catTitles[i]=category.getLabel();
			catCheck[i]=false;
			
			if(postCategoriesID != null) {
				for (int j = 0; j < postCategoriesID.length; j++) {
					if(postCategoriesID[j] == Integer.parseInt(category.getId()) ){
						catCheck[i]=true;
						break;
					}
				}
			}
    	}
    	
    	this.chkField= new CheckBoxListField(catTitles,catCheck ).get_checkList();
    	add(chkField);
    	add(new SeparatorField());
 		      
    	 //row new cat 
        HorizontalFieldManager rowTitle = new HorizontalFieldManager();
		LabelField lblTitle = getLabel(_resources.getString(WordPressResource.MENUITEM_POST_NEWCATEGORY));   
		
    	Bitmap imgOpen = Bitmap.getBitmapResource("disclosure-indicator.png"); 
  		BitmapField bfOpenCat = new BitmapField(imgOpen, BitmapField.FOCUSABLE)
  		{			
  		    //override context menu      
	        protected void makeContextMenu(ContextMenu contextMenu) {
	        	 contextMenu.addItem(_newCategoryContextMenuItem);
	       }
  		};
  		bfOpenCat.setMargin(margins);
  		bfOpenCat.setSpace(5, 5);
  		rowTitle.add(lblTitle);
  		rowTitle.add(bfOpenCat);
  		this.add(rowTitle);
  		
  		
    }
    
    private MenuItem _newCategoryContextMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_POST_NEWCATEGORY, 10, 2) {
        public void run() {
        	controller.showNewCategoriesView();
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
