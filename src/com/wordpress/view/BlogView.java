package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogController;
import com.wordpress.view.component.NotYetImpPopupScreen;

public class BlogView extends BaseView {
	
    private BlogController controller=null;
    		    
    private HorizontalFieldManager topButtonsManager;
	private ButtonField buttonNewPost;
	private ButtonField buttonDraftPosts;
	private ButtonField buttonComments;
	private ButtonField buttonBlogRefresh;

	private ObjectListField listaPost;
	
	
	public BlogView(BlogController _controller , String[] post) {
		super();
		this.controller=_controller;
        //add a screen title
        LabelField title = new LabelField(controller.getBlogName(), LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(title);

        //setup top buttons
        buttonNewPost = new ButtonField(_resources.getString(WordPressResource.BUTTON_NEWPOST));
        buttonNewPost.setChangeListener(listenerButton);
        buttonDraftPosts = new ButtonField(_resources.getString(WordPressResource.BUTTON_DRAFTPOSTS));
        buttonDraftPosts.setChangeListener(listenerButton);
        buttonComments = new ButtonField(_resources.getString(WordPressResource.BUTTON_COMMENTS));
        buttonComments.setChangeListener(listenerButton);
        buttonBlogRefresh = new ButtonField(_resources.getString(WordPressResource.BUTTON_REFRESH_BLOG));
        buttonBlogRefresh.setChangeListener(listenerButton);

        topButtonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
        topButtonsManager.add(buttonNewPost);
        topButtonsManager.add(buttonDraftPosts);
        topButtonsManager.add(buttonComments);
        topButtonsManager.add(buttonBlogRefresh);
		add(topButtonsManager); 
		add(new SeparatorField());
        
		buildList(post);
		
        addMenuItem(_newPostItem);
        addMenuItem(_draftPostsItem);
        addMenuItem(_commentsPostsItem);
        addMenuItem(_optionsPostsItem);
	}
	
	
	private void buildList(String[] post) {
		removeAllMenuItems();
		
		listaPost = new ObjectListField(); 	        
		listaPost.set(post);
		add(listaPost);

		if(post.length > 0 ) {
			addMenuItem(_editPostItem);
			addMenuItem(_deletePostItem);
		}
		
		addMenuItem(_blogOptionsItem);
	}
	 
    public void refresh(String[] post){
    	this.delete(listaPost);
    	buildList(post);
    }

    
    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDITPOST, 110, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.editPost(selectedPost);
        }
    };
          
    private MenuItem _draftPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_DRAFTPOSTS, 120, 10) {
        public void run() {
    	 controller.showDraftPosts(); 
        }
    };

    private MenuItem _newPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_NEWPOST, 110, 10) {
        public void run() {
        	controller.newPost();
        }
    };
	
	private MenuItem _deletePostItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETEPOST, 210, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.deletePost(selectedPost);
        }
    };
    
    private MenuItem _blogOptionsItem = new MenuItem( _resources, WordPressResource.MENUITEM_BLOG_OPTION, 220, 10) {
        public void run() {
        	controller.showBlogOptions();
        }
    };

    
    private MenuItem _refreshBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 150, 10) {
        public void run() {
    //    	int selected = listaBlog.getSelectedIndex();
      //  	mainController.refreshBlog(selected);
        }
    };
    
    
    
    private MenuItem _commentsPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS, 130, 10) {
        public void run() {
        	UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
        }
    };
    
    private MenuItem _optionsPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETUP, 140, 10) {
        public void run() {
           	UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
        }
    };

    
	private FieldChangeListener listenerButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(field == buttonNewPost){
	    		controller.newPost();	    		
	    	} else if(field == buttonBlogRefresh){
	    		UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
	    	} else if(field == buttonComments){
	    		UiApplication.getUiApplication().pushScreen(new NotYetImpPopupScreen());
	    	} else if(field == buttonDraftPosts) {
	    		controller.showDraftPosts(); 
	    	}
	   }
	};

    
    
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		//Field fieldWithFocus = UiApplication.getUiApplication().getActiveScreen().getFieldWithFocus();
		Field fieldWithFocus = this.getFieldWithFocus();
		if(fieldWithFocus == topButtonsManager) { //focus on the top buttons, do not open menu on whell click
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