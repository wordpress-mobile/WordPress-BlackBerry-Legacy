package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
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
import com.wordpress.view.component.BitmapButtonField;
import com.wordpress.view.component.PostsListField;

public class PostsView extends BaseView {
	
    private PostsController controller= null;
    private PostsListField listaPost; 
    private HorizontalFieldManager topButtonsManager;
    private VerticalFieldManager dataScroller;
    private BitmapButtonField buttonNewPost;
	private ButtonField buttonDraftPosts;
	private ButtonField buttonRefresh;
	private LabelField lblTotalPostsNumber;
	private LabelField lblNewPostsNumber;
	private Bitmap _writeBitmap = Bitmap.getBitmapResource("write.png");
	private Bitmap _localDraftBitmap = Bitmap.getBitmapResource("browser.png");
	private Bitmap _refreshBitmap = Bitmap.getBitmapResource("refresh.png");

	
	 public PostsView(PostsController _controller, Vector recentPostInfo, int numberOfNewPosts) {
	    	super(_resources.getString(WordPressResource.TITLE_RECENTPOST)+ " > "+ _controller.getBlogName(), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
	        
	    	  //A HorizontalFieldManager to hold the buttons headings.
	        topButtonsManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH);
	        
	        //setup top buttons and posts number
	        buttonNewPost = new BitmapButtonField(_writeBitmap, ButtonField.CONSUME_CLICK | Field.FIELD_VCENTER);
	        buttonNewPost.setChangeListener(listenerButton);
	        buttonDraftPosts = new BitmapButtonField(_localDraftBitmap,  ButtonField.CONSUME_CLICK | Field.FIELD_VCENTER);
	        buttonDraftPosts.setChangeListener(listenerButton);
	        buttonRefresh = new BitmapButtonField(_refreshBitmap,  ButtonField.CONSUME_CLICK | Field.FIELD_VCENTER);
	        buttonRefresh.setChangeListener(listenerButton);

	        topButtonsManager.add(buttonNewPost);
	        topButtonsManager.add(buttonDraftPosts);
	        topButtonsManager.add(buttonRefresh);
	        
		    VerticalFieldManager postNumbersFieldManager = new VerticalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	        		| HorizontalFieldManager.NO_VERTICAL_SCROLL | Field.FIELD_BOTTOM | Field.FIELD_RIGHT | Field.USE_ALL_WIDTH);
	        
	        if(recentPostInfo != null) {
	        	lblTotalPostsNumber = getLabel(getNumberOfTotalPostLabel(recentPostInfo.size()), Field.FIELD_BOTTOM | Field.FIELD_RIGHT);
	        	lblNewPostsNumber = getLabel(getNumberOfNewPostLabel(numberOfNewPosts), Field.FIELD_BOTTOM | Field.FIELD_RIGHT);
	        } else {
	        	lblTotalPostsNumber = getLabel("N.A.", Field.FIELD_BOTTOM | Field.FIELD_RIGHT);
	        	lblNewPostsNumber = getLabel("", Field.FIELD_BOTTOM | Field.FIELD_RIGHT);
	        }
	        lblTotalPostsNumber.setFont(lblTotalPostsNumber.getFont().derive(Font.PLAIN));
	        lblNewPostsNumber.setFont(lblNewPostsNumber.getFont().derive(Font.PLAIN));
	        postNumbersFieldManager.add(lblTotalPostsNumber);
	        postNumbersFieldManager.add(lblNewPostsNumber);
	        topButtonsManager.add(postNumbersFieldManager);
	        
	        //A HorizontalFieldManager to hold the posts list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);
	        	

			add(topButtonsManager);
			add(new SeparatorField());
			add(dataScroller);
			buildList(recentPostInfo);
	 }

	 
	 private String getNumberOfNewPostLabel(int newPostNum) {
		  //set the label for the post number object
	        String numerOfPostLabel="(" +newPostNum+" "+_resources.getString(WordPressResource.LABEL_POST_NUMBER_NEW)+")";
	        
	        return numerOfPostLabel;
		 
	 }
	 
	 private String getNumberOfTotalPostLabel(int recentPostNum) {
		  //set the label for the post number object
	        String numerOfPostLabel = null;       
	        if(recentPostNum > 1) 
	        	numerOfPostLabel= recentPostNum+ " " +_resources.getString(WordPressResource.LABEL_POST_NUMBERS); 
	        else 
	        	numerOfPostLabel= recentPostNum+ " " +_resources.getString(WordPressResource.LABEL_POST_NUMBER);
	        
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
		listaPost.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_POSTS), DrawStyle.LEFT);
		dataScroller.add(listaPost);


		if(recentPostInfo != null && recentPostInfo.size() > 0 ) {
			addMenuItem(_editPostItem);
			addMenuItem(_commentsMenuItem);
			addMenuItem(_deletePostItem);
		}
		
		addMenuItem(_refreshPostListItem);
		dataScroller.invalidate();
		listaPost.setFocus(); //set the focus over the list
	}
	 

    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200, 10) {
        public void run() {
            int selectedPost = listaPost.getSelectedIndex();
            controller.editPost(selectedPost);
        }
    };
	
    private MenuItem _commentsMenuItem = new MenuItem(_resources, WordPressResource.MENUITEM_COMMENTS, 200, 10) {
        public void run() {
        	int selectedPost = listaPost.getSelectedIndex();
        	controller.showComments(selectedPost);
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
    	
        if(recentPostInfo != null) {
        	lblTotalPostsNumber.setText(getNumberOfTotalPostLabel(recentPostInfo.size()));
        	lblNewPostsNumber.setText(getNumberOfNewPostLabel(count));
        } else {
        	lblTotalPostsNumber.setText("N.A.");
        	lblNewPostsNumber.setText("");
        }
    	
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