package com.wordpress.view;

import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.DraftPostsController;

public class DraftPostsView extends BaseView {
	
    private DraftPostsController controller= null;

	private ObjectListField listaPost; 
	
	 public DraftPostsView(DraftPostsController  _controller, String[] post) {
	    	super();
	    	this.controller=_controller;
	    	//add a screen title
	        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_DRAFTPOST),
	                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
	        String blogName= controller.getCurrentBlogName();
	        setTitle(blogName+" > "+ title);  
	        
	        buildList(post);
	 }


	private void buildList(String[] post) {
		removeAllMenuItems();	
		listaPost = new ObjectListField(); 	        
		
		if(post.length > 0 ){
			listaPost.set(post);
			addMenuItem(_editPostItem);
			addMenuItem(_deletePostItem);
		} else {
			listaPost.set(new String[]{"No Draft posts"});
		}
		addMenuItem(_newPostItem);
		add(listaPost);
	}
	
    public void refresh( String[] post){
    	this.delete(listaPost);
    	buildList(post);
    }
	
    private MenuItem _deletePostItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETEPOST, 220, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.deletePost(selectedPost);    
        }
    };
    
    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDITPOST, 200, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.editPost(selectedPost);            
        }
    };
    
    private MenuItem _newPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_NEWPOST, 210, 10) {
        public void run() {
            controller.newPost();    
        }
    };
    
	public DraftPostsController getController() {
		return controller;
	}
}