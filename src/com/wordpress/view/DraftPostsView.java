package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.MenuItem;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.DraftPostsController;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ListActionListener;
import com.wordpress.view.component.PostsListField;

public class DraftPostsView extends BaseView implements ListActionListener {
	
    private DraftPostsController controller= null;

	private PostsListField listaPost; 
	
	 public DraftPostsView(DraftPostsController  _controller, Hashtable[] post) {
	    	super(_resources.getString(WordPressResource.TITLE_DRAFT_POSTS)+" > "+_controller.getCurrentBlogName());
	    	this.controller=_controller;	        
	        buildList(post);
	 }


	private void buildList(Hashtable[] post) {
		removeAllMenuItems();	
		listaPost = new PostsListField(); 	        
		listaPost.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_DRAFT_POSTS), DrawStyle.LEFT);
		listaPost.setDefautActionListener(this);
		
		if( (post != null) && post.length > 0 ){
			listaPost.set(post);
			addMenuItem(_editPostItem);
			addMenuItem(_deletePostItem);
		} 
		addMenuItem(_newPostItem);
		add(listaPost);
	}
	
    public void refresh(Hashtable[] post) {
    	this.delete(listaPost);
    	buildList(post);
    }
    
    protected void onDisplay() {
    	super.onDisplay();
    	Log.trace("onDisplay DraftPostView");
    }
    
    protected void onVisibilityChange(boolean visible) {
    	super.onVisibilityChange(visible);
    	Log.trace("onVisibilityChange DraftPostView "+ visible);
    	if(visible) 
    		controller.updateViewDraftPostList();
    }
   
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		controller.toPostsList();
		return true;
	}

    
    private MenuItem _deletePostItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 220, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.deletePost(selectedPost);    
        }
    };
    
    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            Log.trace("selected draft post " + selectedPost);
            controller.editPost(selectedPost);            
        }
    };
    
    private MenuItem _newPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_NEW, 210, 10) {
        public void run() {
            controller.newPost();    
        }
    };
    
	public BaseController getController() {
		return controller;
	}


	public void actionPerformed() {
		 int selectedPost = listaPost.getSelectedIndex();
         Log.trace("selected draft post " + selectedPost);
         controller.editPost(selectedPost); 
	}
	
}