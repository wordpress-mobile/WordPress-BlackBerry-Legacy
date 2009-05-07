package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogController;
import com.wordpress.view.component.NotYetImpPopupScreen;

public class BlogView extends BaseView {
	
    private BlogController controller=null;
    		    
    private VerticalFieldManager mainButtonsManager;
    private ButtonField buttonPosts;
    private ButtonField buttonPages;
	private ButtonField buttonComments;
	private ButtonField buttonBlogRefresh;
	private ButtonField buttonOptions;

	public BlogView(BlogController _controller) {
		super(_controller.getBlogName(), Field.FIELD_HCENTER);
		this.controller=_controller;

        //setup screen buttons
        buttonPosts = new ButtonField(_resources.getString(WordPressResource.BUTTON_POSTS));
        buttonPosts.setChangeListener(listenerButton);
        buttonPages = new ButtonField(_resources.getString(WordPressResource.BUTTON_PAGES));
        buttonPages.setChangeListener(listenerButton);
        buttonOptions = new ButtonField(_resources.getString(WordPressResource.BUTTON_OPTIONS));
        buttonOptions.setChangeListener(listenerButton);
        buttonComments = new ButtonField(_resources.getString(WordPressResource.BUTTON_COMMENTS));
        buttonComments.setChangeListener(listenerButton);
        buttonBlogRefresh = new ButtonField(_resources.getString(WordPressResource.BUTTON_REFRESH_BLOG));
        buttonBlogRefresh.setChangeListener(listenerButton);

        mainButtonsManager = new VerticalFieldManager(Field.FIELD_HCENTER);
        mainButtonsManager.add(buttonPosts);
        mainButtonsManager.add(buttonPages);
        mainButtonsManager.add(buttonComments);
        mainButtonsManager.add(buttonOptions);
        mainButtonsManager.add(buttonBlogRefresh);
		add(mainButtonsManager); 
	}
	
    /* Not yet used
    private MenuItem _blogOptionsItem = new MenuItem( _resources, WordPressResource.MENUITEM_BLOG_OPTION, 220, 10) {
        public void run() {
        	controller.showBlogOptions();
        }
    };

    
    private MenuItem _refreshBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 150, 10) {
        public void run() {
        	controller.refreshBlog();
        }
    };
    
    
    
    private MenuItem _commentsPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS, 130, 10) {
        public void run() {
        	UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
        }
    };
    */
    
	private FieldChangeListener listenerButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(field == buttonPosts){
	    		controller.showPosts();	
	    	} else if(field == buttonBlogRefresh){
	    		controller.refreshBlog(); //reload only the posts list
	    	} else if(field == buttonComments){
	    		UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
	    	} else if(field == buttonPages) {
	    		UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
	    	} else if(field == buttonOptions) {
	    		controller.showBlogOptions();
	    	}
	   }
	};

    
    
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		Field fieldWithFocus = this.getFieldWithFocus();
		if(fieldWithFocus == mainButtonsManager) { //focus on the top buttons, do not open menu on whell click
			return true;
		}
		else 
		 return super.navigationClick(status,time);
	}

	
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		controller.backCmd();
		return true;
	}

	public BaseController getController() {
		return controller;
	}   
}