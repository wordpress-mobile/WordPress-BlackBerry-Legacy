package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogController;
import com.wordpress.controller.FrontController;


public class BlogView extends BaseView {
	
    private BlogController controller=null;

	private static final int mnuPosts = 100;
	private static final int mnuPages = 110;
	private static final int mnuComments = 120;
	private static final int mnuOptions = 130;
	private static final int mnuRefresh= 140;
	//main menu entries
	private int[] mainMenuItems = {mnuPosts, mnuPages, mnuComments, mnuOptions, mnuRefresh};
	private String[] mainMenuItemsLabel = {
			_resources.getString(WordPressResource.BUTTON_POSTS),
			_resources.getString(WordPressResource.BUTTON_PAGES),
			_resources.getString(WordPressResource.BUTTON_COMMENTS),
			_resources.getString(WordPressResource.BUTTON_OPTIONS),
			_resources.getString(WordPressResource.BUTTON_REFRESH_BLOG)
			};
	      	
	
	public BlogView(BlogController _controller) {
		super(_controller.getBlogName(), Field.FIELD_HCENTER);
		this.controller=_controller;
		list = new BlogListField();
		
		  //Populate the ListField
        for(int count = 0; count < mainMenuItems.length; ++count) {
        	list.insert(count);
        }
               
        add(list);   
        addMenuItem(_goItem);
	}
	
    
    private MenuItem _goItem = new MenuItem( _resources, WordPressResource.BUTTON_OK, 220, 10) {
        public void run() {
        	doSelection();
        }
    };

	private BlogListField list;
    

    private void doSelection(){
    	
    	int i = mainMenuItems[list.getSelectedIndex()];
    	
    	switch (i) {
        
        case (mnuPosts): 
        	controller.showPosts();
            break;
        case (mnuPages):
        	controller.showPages();
            break;
        case (mnuComments):
        	controller.showComments();
            break;

        case (mnuOptions):
        	controller.showBlogOptions();
            break;

        case (mnuRefresh):
        	controller.refreshBlog();
            break;       
        
        default:
        controller.displayError("There was an error with the request.");
        break;

    }
    }

        
  /*
	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			return  this.onClose();
		} else if (c == Characters.ENTER) {
			doSelection();
			return true;
		}

		return super.keyChar(c, status, time);
	}
*/
    
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		doSelection();
		return true;
	}

	
	
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		//controller.backCmd();
		FrontController.getIstance().backAndRefreshView(true);	
		return true;
	}

	public BaseController getController() {
		return controller;
	}   
	
	
	private class BlogListField extends ObjectListField {
        
        Bitmap imgFolder = Bitmap.getBitmapResource("drafts-folder.png");
        Bitmap imgSettings = Bitmap.getBitmapResource("settings.png");
        Bitmap imgRefresh = Bitmap.getBitmapResource("settings.png");  
	    private Bitmap icon;

	    // We are going to take care of drawing the item.
	    public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
	                
	        if ( mainMenuItems[index] == mnuPosts) {
	            icon = imgFolder;
	        }   
	        if ( mainMenuItems[index] == mnuPages) {
	            icon = imgFolder;
	        }  
	        if ( mainMenuItems[index] == mnuComments) {
	            icon = imgFolder;
	        }
	        if ( mainMenuItems[index] == mnuOptions) {
	            icon = imgSettings;
	        }
	        if ( mainMenuItems[index] == mnuRefresh) {
	            icon = imgRefresh;
	        }

	        if (null != icon) {
	            int offsetY = (this.getRowHeight() - icon.getHeight())/2; 
	            graphics.drawBitmap(1,y + offsetY, icon.getWidth(), icon.getHeight(), icon, 0, 0);
	            graphics.drawText(mainMenuItemsLabel[index], icon.getWidth() + 2, y, DrawStyle.ELLIPSIS, width - icon.getWidth() + 2);
	        } else {
	            graphics.drawText("- " + mainMenuItems[index], 0, y, DrawStyle.ELLIPSIS, width - graphics.getFont().getAdvance("- "));
	        }
	    }
	}
}