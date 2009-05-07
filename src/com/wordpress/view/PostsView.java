package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostsController;

public class PostsView extends BaseView {
	
    private PostsController controller= null;
    private ObjectListField listaPost; 
    private HorizontalFieldManager topButtonsManager;
	private ButtonField buttonNewPost;
	private ButtonField buttonDraftPosts;
	private ButtonField buttonRefresh;
    
    
	 public PostsView(PostsController _controller, String[] post) {
	    	super(_controller.getBlogName()+" > "+_resources.getString(WordPressResource.TITLE_RECENTPOST));
	    	this.controller=_controller;
	        	        
	        //setup top buttons
	        buttonNewPost = new ButtonField(_resources.getString(WordPressResource.BUTTON_NEWPOST));
	        buttonNewPost.setChangeListener(listenerButton);
	        buttonDraftPosts = new ButtonField(_resources.getString(WordPressResource.BUTTON_DRAFTPOSTS));
	        buttonDraftPosts.setChangeListener(listenerButton);
	        buttonRefresh = new ButtonField(_resources.getString(WordPressResource.BUTTON_REFRESH_BLOG));
	        buttonRefresh.setChangeListener(listenerButton);

	        topButtonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
	        topButtonsManager.add(buttonNewPost);
	        topButtonsManager.add(buttonDraftPosts);
	        topButtonsManager.add(buttonRefresh);
			add(topButtonsManager); 
			add(new SeparatorField());
	        
	        buildList(post);
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
		
		addMenuItem(_refreshPostListItem);
		addMenuItem(_draftPostsItem);
	}
	 

    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDITPOST, 200, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.editPost(selectedPost);
        }
    };
	
	private MenuItem _deletePostItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETEPOST, 210, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.deletePost(selectedPost);
        }
    };
    
    private MenuItem _refreshPostListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH_POSTSLIST, 220, 10) {
        public void run() {
        	controller.refreshPosts();
        }
    };
     
    private MenuItem _draftPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_DRAFTPOSTS, 120, 10) {
        public void run() {
    	 controller.showDraftPosts(); 
        }
    };
 
    
	private FieldChangeListener listenerButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(field == buttonNewPost){
	    		controller.newPost();	    		
	    	} else if(field == buttonRefresh){
	    		controller.refreshPosts(); //reload only the posts list
	    	} else if(field == buttonDraftPosts) {
	    		controller.showDraftPosts(); 
	    	}
	   }
	};

    	 
    public void refresh(String[] post){
    	this.delete(listaPost);
    	buildList(post);
    }


	public BaseController getController() {
		return this.controller;
	}
	
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
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

}