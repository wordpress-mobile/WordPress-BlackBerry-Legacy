//#preprocess
package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostsController;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ListActionListener;
import com.wordpress.view.component.ListLoadMoreListener;
import com.wordpress.view.component.GenericListField;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarItem;
//#endif


public class PostsView extends BaseView implements ListActionListener, ListLoadMoreListener {
	
    private PostsController controller= null;
    private GenericListField listaPost; 
    private VerticalFieldManager dataScroller;
	
	 public PostsView(PostsController _controller, Vector recentPostInfo) {
	    	super(_controller.getBlogName(), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
	    	this.setSubTitleText(_resources.getString(WordPressResource.BUTTON_POSTS));        
	        //A HorizontalFieldManager to hold the posts list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);
	        	
	        //#ifdef IS_OS47_OR_ABOVE
	        	initUpBottomBar();
	        //#endif
	        
			add(dataScroller);
			buildList(recentPostInfo);
	 }
	 
	 
	//#ifdef IS_OS47_OR_ABOVE
		private void initUpBottomBar() {
			if (Touchscreen.isSupported() == false) return;
			
			BottomBarItem items[] = new BottomBarItem[4];
			items[0] = new BottomBarItem("bottombar_add.png", "bottombar_add.png", _resources.getString(WordPressResource.MENUITEM_NEW));
			items[1] = new BottomBarItem("bottombar_delete.png", "bottombar_disabled.png", _resources.getString(WordPressResource.MENUITEM_DELETE));
			items[2] = new BottomBarItem("bottombar_browser.png", "bottombar_browser.png", _resources.getString(WordPressResource.MENUITEM_LOCALDRAFTS));
			items[3] = new BottomBarItem("bottombar_refresh.png", "bottombar_refresh.png", _resources.getString(WordPressResource.MENUITEM_REFRESH));		
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
			case 2:
				 controller.showDraftPosts();
				break;
			case 3:
				 controller.refreshPostsList();				
				break;
			default:
				break;
			}
		}
	//#endif

		
//adapter	
	private Hashtable[] prepareItemsForList(Vector recentPostInfo){
		Hashtable elements[]= new Hashtable[0];

		if(recentPostInfo != null) {
			Hashtable postStatusHash = controller.getBlog().getPostStatusList();
			elements= new Hashtable[recentPostInfo.size()];

			//Populate the vector with the elements [title, data, title, data ....]
			for (int i = 0; i < recentPostInfo.size(); i++) {
				Hashtable postData = (Hashtable) recentPostInfo.elementAt(i);
				Hashtable smallPostData = new Hashtable() ;

				String title = (String) postData.get("title");
				if (title == null || title.length() == 0) {
					title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
				}
				smallPostData .put("title", title);

				Date dateCreated = (Date) postData.get("date_created_gmt");
				if (dateCreated != null)
					smallPostData .put("date_created_gmt", dateCreated);

				if ( postData.get("post_status") != null) {
					String statusUndecoded = (String) postData.get("post_status");

					String status = (String) postStatusHash.get(statusUndecoded);

					if(status == null && statusUndecoded.equalsIgnoreCase("future")) {
						status = "Scheduled";
					} 

					if(status != null)
						smallPostData .put("post_status", status);
				}

				elements[i]=smallPostData; 
			}			
		}
		return elements;
	}

		
	private void buildList(Vector recentPostInfo) {
		Hashtable elements[] = this.prepareItemsForList(recentPostInfo); 				
		listaPost = new GenericListField(); 	        
		listaPost.set(elements);
		listaPost.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_POSTS), DrawStyle.LEFT);
		listaPost.setDefautActionListener(this);
		listaPost.setDefautLoadMoreListener(this);
		dataScroller.add(listaPost);		
		dataScroller.invalidate();
		listaPost.setFocus(); //set the focus over the list
		
	    //#ifdef IS_OS47_OR_ABOVE
		if(elements.length == 0) {
			setBottomBarButtonState(1, false); //disable the delete btn
		} else
			setBottomBarButtonState(1, true);
		//#endif
	}
	
    //Override the makeMenu method so we can add a custom menu item
    //if the checkbox ListField has focus.
    protected void makeMenu(Menu menu, int instance)
    {
    	
        Field focus = getLeafFieldWithFocus();
        if(listaPost != null && focus == listaPost) 
        {
        	if(listaPost.getSize() > 0 ) {
        		menu.add(_editPostItem);
        		menu.add(_commentsMenuItem);
        		menu.add(_deletePostItem);
    		}
        }
        
        menu.add(_newPostItem);
        menu.add(_draftPostsItem);
        menu.add(_refreshPostListItem);                
        
        //Create the default menu.
        super.makeMenu(menu, instance);
    }

    
    private MenuItem _newPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_NEW, 200, 10) {
        public void run() {
        	controller.newPost();
        }
    };
    
    private MenuItem _draftPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_LOCALDRAFTS, 210, 10) {
        public void run() {
    	 controller.showDraftPosts(); 
        }
    };
    
    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200000, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.editPost(selectedPost);
        }
    };
	
    private MenuItem _commentsMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_COMMENTS, 200000, 10) {
        public void run() {
        	int selectedPost = listaPost.getSelectedIndex();
        	controller.showComments(selectedPost);
        }
    };
    
    
	private MenuItem _deletePostItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 210000, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.deletePost(selectedPost);
        }
    };
    
    private MenuItem _refreshPostListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH, 220000, 10) {
        public void run() {
        	controller.refreshPostsList();
        }
    };
     	
    public void refresh(Vector recentPostInfo){
    	dataScroller.delete(listaPost);
    	buildList(recentPostInfo);
    }

	public BaseController getController() {
		return this.controller;
	}
	
	//override onClose() to by-pass the standard dialog box when close commentMenuItem is hit  
	public boolean onClose()   {
		controller.backCmd();
		return true;
	}
	
	public void actionPerformed() {
        int selectedPost = listaPost.getSelectedIndex();
        controller.editPost(selectedPost); 
	}

	public void loadMore() {
		Log.debug("loadMore listener more called" );
		controller.loadMorePosts();
	}
    	
	public void addPostsToScreen(Vector recentPostInfo){
		int selectedIndex = listaPost.getSelectedIndex();
		Hashtable elements[] = this.prepareItemsForList(recentPostInfo);
		listaPost.resetListItems(elements);
		listaPost.setFocus();
		listaPost.setSelectedIndex(selectedIndex);
    }
}