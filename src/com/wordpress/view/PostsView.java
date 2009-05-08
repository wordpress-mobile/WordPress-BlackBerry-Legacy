package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.ListField;
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
    
    
	 public PostsView(PostsController _controller, Vector recentPostInfo) {
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
	        
	        buildList(recentPostInfo);
	 }

/*
	 final class TestListCallback implements ListFieldCallback {
		 private Vector _listElements ;
		 		 
		public TestListCallback(int length) {
			super();
			_listElements= new Vector(length, 1);
		}

		// Draws the list row.
		public void drawListRow(ListField list, Graphics g, int index, int y, int w) {
			// We don't need to draw anything here because it is handled
			// by the paint method of our custom ColouredListField.
		}

		// Returns the object at the specified index.
		public Object get(ListField list, int index) {
			return _listElements.elementAt(index);
		}

		// Returns the first occurence of the given String, beginning the
		// search at index,
		// and testing for equality using the equals method.
		public int indexOfList(ListField list, String p, int s) {
			// return listElements.getSelectedIndex();
			return _listElements.indexOf(p, s);
		}

		// Returns the screen width so the list uses the entire screen width.
		public int getPreferredWidth(ListField list) {
			return Graphics.getScreenWidth();
		}

		// Adds a String element at the specified index.
		public void insert(String toInsert, int index) {
			_listElements.insertElementAt(toInsert, index);
		}

		// Erases all contents.
		public void erase() {
			_listElements.removeAllElements();
		}
	}
	 */
	 
	private void buildList(Vector recentPostInfo) {
		removeAllMenuItems();

		String elements[]= new String[0];
		
		if(recentPostInfo != null) {						
			elements= new String[recentPostInfo.size()];
			//Populate the vector with the elements [title, data, title, data ....]
	        for (int i = 0; i < recentPostInfo.size(); i++) {
	        	 Hashtable postData = (Hashtable) recentPostInfo.elementAt(i);
	             String title = (String) postData.get("title");
	             Date dateCreated = (Date) postData.get("dateCreated");
	             
	             if (title == null || title.length() == 0) {
	                 title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
	             }
	             elements[i]=title;
	            // elements.addElement(dateCreated.toString());
	         }			
		}
						
		listaPost = new ObjectListField(); 	        
		listaPost.set(elements);
		listaPost.setEmptyString("Nothing to see here", DrawStyle.LEFT);
		//TestListCallback listCallback = new TestListCallback(elements.size());
		//listaPost.setCallback(listCallback);

		add(listaPost);

		if(recentPostInfo.size() > 0 ) {
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
        	controller.refreshView();
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
	    		controller.refreshView(); //reload only the posts list
	    	} else if(field == buttonDraftPosts) {
	    		controller.showDraftPosts(); 
	    	}
	   }
	};

    	 
    public void refresh(Vector recentPostInfo){
    	this.delete(listaPost);
    	buildList(recentPostInfo);
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