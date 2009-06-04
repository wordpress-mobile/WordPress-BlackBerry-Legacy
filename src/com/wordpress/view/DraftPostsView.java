package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.MenuItem;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.DraftPostsController;
import com.wordpress.view.component.PostsListField;

public class DraftPostsView extends BaseView {
	
    private DraftPostsController controller= null;

	private PostsListField listaPost; 
	
	 public DraftPostsView(DraftPostsController  _controller, Hashtable[] post) {
	    	super(_controller.getCurrentBlogName()+" > "+_resources.getString(WordPressResource.TITLE_DRAFT_POSTS));
	    	this.controller=_controller;	        
	        buildList(post);
	 }


	private void buildList(Hashtable[] post) {
		removeAllMenuItems();	
		listaPost = new PostsListField(); 	        
		listaPost.setEmptyString("No Draft Posts", DrawStyle.LEFT);
		
		if(post.length > 0 ){
			listaPost.set(post);
			addMenuItem(_editPostItem);
			addMenuItem(_deletePostItem);
		} 
		addMenuItem(_newPostItem);
		add(listaPost);
	}
	
    public void refresh(Hashtable[] post){
    	this.delete(listaPost);
    	buildList(post);
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
}