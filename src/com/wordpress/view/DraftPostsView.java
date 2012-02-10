//#preprocess
package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.MenuItem;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.DraftPostsController;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ListActionListener;
import com.wordpress.view.component.GenericListField;

//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif

public class DraftPostsView extends BaseView implements ListActionListener {
	
    private DraftPostsController controller= null;

	private GenericListField listaPost; 
	
	 public DraftPostsView(DraftPostsController  _controller, Hashtable[] post) {
	    	super(_controller.getCurrentBlogName());
	    	this.setSubTitleText(_resources.getString(WordPressResource.MENUITEM_LOCALDRAFTS));  
	    	this.controller=_controller;
	        buildList(post);
	 }

	 
	 //#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
	 private void initUpBottomBar(int size) {
		 if (Touchscreen.isSupported() == false) return;

		 int numberOfButtons = 1;
		 if( size > 1 ){
			 numberOfButtons = 2;
		 }
		 BottomBarItem items[] = new BottomBarItem[numberOfButtons];
		 items[0] = new BottomBarItem("bottombar_add.png", "bottombar_add.png", _resources.getString(WordPressResource.MENUITEM_NEW));
		 if(numberOfButtons == 2)
			 items[1] = new BottomBarItem("bottombar_delete.png", "bottombar_delete.png", _resources.getString(WordPressResource.MENUITEM_DELETE));

		 initializeBottomBar(items);
	 }

	 protected void bottomBarActionPerformed(int mnuItem) {
		 switch (mnuItem) {
		 case 0:
			 controller.newPost();
			 break;
		 case 1:
			 int selectedPost = listaPost.getSelectedIndex();
			 controller.deletePost(selectedPost);
			 break;
		 default:
			 break;
		 }
	 }
	 //#endif
	 

	private void buildList(Hashtable[] post) {
		removeAllMenuItems();	
		listaPost = new GenericListField(); 	        
		listaPost.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_DRAFT_POSTS), DrawStyle.LEFT);
		listaPost.setDefautActionListener(this);
		
		if( (post != null) && post.length > 0 ){
			listaPost.set(post);
			addMenuItem(_editPostItem);
			addMenuItem(_deletePostItem);
		} 
		addMenuItem(_newPostItem);
		
        //#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		int size = 0;
		if ((post != null) && post.length > 0)
			size = 3;
    	initUpBottomBar(size);
    	//#endif
		
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