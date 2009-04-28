package com.wordpress.view;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BlogIOController;
import com.wordpress.controller.MainBlogController;

public class MainBlogView extends MainScreen {
	
    private MainBlogController controller=null;
    
    		
    //create a variable to store the ResourceBundle for localization support
    private static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
	

	public MainBlogView(MainBlogController _controller) {
		super();
		this.controller=_controller;
        //add a screen title
        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_BLOGVIEW),
                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(title);
        add(new LabelField("add information on blogs here!"));
        
        addMenuItem(_recentPostsItem);
        addMenuItem(_newPostItem);
        addMenuItem(_draftPostsItem);
	}
	
	
    private MenuItem _newPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_NEWPOST, 110, 10) {
        public void run() {
        	controller.newPost();
        }
    };
       
    private MenuItem _draftPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_DRAFTPOSTS, 120, 10) {
        public void run() {
    	 controller.showDraftPosts(); 
        }
    };

    private MenuItem _recentPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_RECENTPOSTS, 130, 10) {
        public void run() {

        	controller.showRecentPosts();
        }
    };

    
    private MenuItem _setupItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETUP, 1000, 10) {
        public void run() {

        }
    };

}