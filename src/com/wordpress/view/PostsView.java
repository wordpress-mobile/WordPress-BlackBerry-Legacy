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
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostsController;
import com.wordpress.view.component.PostsListField;

public class PostsView extends BaseView {
	
    private PostsController controller= null;
    private PostsListField listaPost; 
    private HorizontalFieldManager topButtonsManager;
    private VerticalFieldManager dataScroller;
    private ButtonField buttonNewPost;
	private ButtonField buttonDraftPosts;
	private ButtonField buttonRefresh;
	private LabelField lblPostsNumber;

	
	 public PostsView(PostsController _controller, Vector recentPostInfo, int numberOfNewPosts) {
	    	super(_controller.getBlogName()+" > "+_resources.getString(WordPressResource.TITLE_RECENTPOST), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
	        
	    	  //A HorizontalFieldManager to hold the buttons headings.
	        topButtonsManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH);
	        
	        //setup top buttons
	        buttonNewPost = new ButtonField(_resources.getString(WordPressResource.BUTTON_NEW), ButtonField.CONSUME_CLICK);
	        buttonNewPost.setChangeListener(listenerButton);
	        buttonDraftPosts = new ButtonField(_resources.getString(WordPressResource.BUTTON_LOCALDRAFTS),  ButtonField.CONSUME_CLICK);
	        buttonDraftPosts.setChangeListener(listenerButton);
	        buttonRefresh = new ButtonField(_resources.getString(WordPressResource.BUTTON_REFRESH_BLOG),  ButtonField.CONSUME_CLICK);
	        buttonRefresh.setChangeListener(listenerButton);

	        topButtonsManager.add(buttonNewPost);
	        topButtonsManager.add(buttonDraftPosts);
	        topButtonsManager.add(buttonRefresh);
	        
	    	  //A HorizontalFieldManager to hold the posts number label
	        HorizontalFieldManager postNumberManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH | HorizontalFieldManager.FIELD_HCENTER);

	        
	        if(recentPostInfo != null)
	        	lblPostsNumber = getLabel(getNumberOfNewPostLabel(recentPostInfo.size(), numberOfNewPosts));
	        else
	        	lblPostsNumber = getLabel("You are not allowed access to details about this blog.");
	        postNumberManager.add(lblPostsNumber);
	    	
	        //A HorizontalFieldManager to hold the posts list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);


			add(topButtonsManager);
			add(postNumberManager);
			add(new SeparatorField());
			add(dataScroller);
			buildList(recentPostInfo);
	 }
	 
	 
	 private String getNumberOfNewPostLabel(int recentPostNum, int newPostNum) {
		  //set the label for the post number object
	        String numerOfPostLabel = null;
	        
	        if(recentPostNum > 1) 
	        	numerOfPostLabel= recentPostNum+ " " +_resources.getString(WordPressResource.LABEL_POST_NUMBERS); 
	        else 
	        	numerOfPostLabel= recentPostNum+ " " +_resources.getString(WordPressResource.LABEL_POST_NUMBER);
	        
	        
	        numerOfPostLabel+=" (" +newPostNum+" "+_resources.getString(WordPressResource.LABEL_POST_NUMBER_NEW)+")";
	        
	        return numerOfPostLabel;
		 
	 }
	 
	private void buildList(Vector recentPostInfo) {
		removeAllMenuItems();
        
		Hashtable elements[]= new Hashtable[0];
		
		if(recentPostInfo != null) {						
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
	             
	             elements[i]=smallPostData; 
	         }			
		}
						
		listaPost = new PostsListField(); 	        
		listaPost.set(elements);
		listaPost.setEmptyString("Nothing to see here", DrawStyle.LEFT);
		dataScroller.add(listaPost);


		if(recentPostInfo != null && recentPostInfo.size() > 0 ) {
			addMenuItem(_editPostItem);
			addMenuItem(_deletePostItem);
		}
		
		addMenuItem(_refreshPostListItem);
		//addMenuItem(_draftPostsItem);
	}
	 

    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.editPost(selectedPost);
        }
    };
	
	private MenuItem _deletePostItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 210, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.deletePost(selectedPost);
        }
    };
    
    private MenuItem _refreshPostListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH, 220, 10) {
        public void run() {
        	controller.refreshPostsList();
        }
    };
     
    private MenuItem _draftPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_LOCALDRAFT, 120, 10) {
        public void run() {
    	 controller.showDraftPosts(); 
        }
    };
 
    
	private FieldChangeListener listenerButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(field == buttonNewPost){
	    		controller.newPost();	    		
	    	} else if(field == buttonRefresh){
	    		controller.refreshPostsList(); //reload only the posts list
	    	} else if(field == buttonDraftPosts) {
	    		controller.showDraftPosts(); 
	    	}
	   }
	};

    	 
    public void refresh(Vector recentPostInfo, int count){
    	dataScroller.delete(listaPost);
    	lblPostsNumber.setText(getNumberOfNewPostLabel(recentPostInfo.size(), count));
    	buildList(recentPostInfo);
    }

	public BaseController getController() {
		return this.controller;
	}
	
	/*
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		Field fieldWithFocus = this.getFieldWithFocus();
		if(fieldWithFocus == topButtonsManager) { //focus on the top buttons, do not open menu on whell click
			return true;
		}
		else 
		 return super.navigationClick(status,time);
	}
*/
	
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		controller.backCmd();
		return true;
	}

}