package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.CommentsController;
import com.wordpress.controller.PostsController;
import com.wordpress.model.Comment;

public class CommentsView extends BaseView {
	
    private CommentsController controller= null;
    private ObjectListField listaPost; 
    private HorizontalFieldManager topManager;
    private VerticalFieldManager dataScroller;
    private ButtonField buttonNewPost;
	private ButtonField buttonDraftPosts;
	private ButtonField buttonRefresh;
	private LabelField lblPostsNumber;

	
	 public CommentsView(CommentsController _controller, Comment[] comments) {
	    	super(_controller.getBlogName()+" > "+_resources.getString(WordPressResource.TITLE_COMMENTS), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
	                
	    	  //A HorizontalFieldManager to hold the posts number label
	    	topManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH | HorizontalFieldManager.FIELD_HCENTER);

	        lblPostsNumber = getLabel("numero ");
	        topManager.add(lblPostsNumber);
	    	
	        //A Vertical FM to hold the comments list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);

			add(topManager);
			add(new SeparatorField());
			add(dataScroller);
			buildList(comments);
	 }

	 
	private void buildList(Comment[] comments) {
		removeAllMenuItems();
        
		String elements[]= new String[0];
		
		if(comments != null) {						
			elements= new String[comments.length];

	        for (int i = 0; i < comments.length; i++) {
	        	
	        	 Comment comment = comments[i];
	             String title = comment.getContent();
	             Date dateCreated = comment.getDate_created_gmt();
	             
	             if (title == null || title.length() == 0) {
	                 title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
	             }
	             elements[i]=title;
	         }			
		}
						
		listaPost = new ObjectListField(); 	        
		listaPost.set(elements);
		listaPost.setEmptyString("Nothing to see here", DrawStyle.LEFT);
		//TestListCallback listCallback = new TestListCallback(elements.size());
		//listaPost.setCallback(listCallback);

		dataScroller.add(listaPost);

		if(comments.length > 0 ) {
			addMenuItem(_editPostItem);
			addMenuItem(_deletePostItem);
		}
		
		addMenuItem(_refreshPostListItem);
		//addMenuItem(_draftPostsItem);
	}
	 

    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDITPOST, 200, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            //controller.editPost(selectedPost);
        }
    };
	
	private MenuItem _deletePostItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETEPOST, 210, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
           // controller.deletePost(selectedPost);
        }
    };
    
    private MenuItem _refreshPostListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH_POSTSLIST, 220, 10) {
        public void run() {
        	controller.refreshView();
        }
    };

    
	private FieldChangeListener listenerButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(field == buttonNewPost){
	    		//controller.newPost();	    		
	    	} else if(field == buttonRefresh){
	    		controller.refreshView(); //reload only the posts list
	    	} else if(field == buttonDraftPosts) {
	    		//controller.showDraftPosts(); 
	    	}
	   }
	};

    	 
    public void refresh(Comment[] comments, int count){
    	dataScroller.delete(listaPost);
    	lblPostsNumber.setText("New Post " + count);
    	buildList(comments);
    }

	public BaseController getController() {
		return this.controller;
	}

	
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		Field fieldWithFocus = this.getFieldWithFocus();
		if(fieldWithFocus == topManager) { //focus on the top buttons, do not open menu on whell click
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

