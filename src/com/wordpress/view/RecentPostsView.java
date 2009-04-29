package com.wordpress.view;

import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.RecentPostsController;

public class RecentPostsView extends BaseView {
	
    private RecentPostsController controller= null;
    private ObjectListField listaPost; 
	
	 public RecentPostsView(RecentPostsController _controller, String[] post) {
	    	super();
	    	this.controller=_controller;
	    	//add a screen title
	        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_RECENTPOST),
	                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
	        String blogName= controller.getCurrentBlogName();
	        setTitle(blogName+" > "+ title); 
	        
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
    
    private MenuItem _refreshPostListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHPOSTSLIST, 220, 10) {
        public void run() {
        	controller.refreshPosts();
        }
    };

    	 
    public void refresh(String[] post){
    	this.delete(listaPost);
    	buildList(post);
    }
    
	public BaseController getController() {
		return controller;
	}
}