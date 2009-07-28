package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;

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
import com.wordpress.controller.PagesController;
import com.wordpress.model.Page;
import com.wordpress.view.component.BitmapButtonField;
import com.wordpress.view.component.PostsListField;

public class PagesView extends BaseView {
	
    private PagesController controller= null;
    private PostsListField  pagesList; 
    private HorizontalFieldManager topButtonsManager;
    private VerticalFieldManager dataScroller;
    private ButtonField buttonNewPost;
	private ButtonField buttonDraftPosts;
	private ButtonField buttonRefresh;
	private LabelField lblPagesNumber;
	private LabelField lblNewPagesNumber;
	private Bitmap _writeBitmap = Bitmap.getBitmapResource("write.png");
	private Bitmap _localDraftBitmap = Bitmap.getBitmapResource("browser.png");
	private Bitmap _refreshBitmap = Bitmap.getBitmapResource("refresh.png");

	
	 public PagesView(PagesController _controller, Page[] pages, int numberOfNewPosts) {
	    	super(_resources.getString(WordPressResource.TITLE_PAGES), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
	        
	    	  //A HorizontalFieldManager to hold the buttons headings.
	        topButtonsManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH);
	        
	        //setup top buttons
	        buttonNewPost = new BitmapButtonField(_writeBitmap, ButtonField.CONSUME_CLICK | Field.FIELD_VCENTER);
	        buttonNewPost.setChangeListener(listenerButton);
	        buttonDraftPosts = new BitmapButtonField(_localDraftBitmap,  ButtonField.CONSUME_CLICK | Field.FIELD_VCENTER);
	        buttonDraftPosts.setChangeListener(listenerButton);
	        buttonRefresh = new BitmapButtonField(_refreshBitmap,  ButtonField.CONSUME_CLICK | Field.FIELD_VCENTER);
	        buttonRefresh.setChangeListener(listenerButton);

	        topButtonsManager.add(buttonNewPost);
	        topButtonsManager.add(buttonDraftPosts);
	        topButtonsManager.add(buttonRefresh);
	        
	        VerticalFieldManager pageNumbersFieldManager = new VerticalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	        		| HorizontalFieldManager.NO_VERTICAL_SCROLL | Field.FIELD_BOTTOM | Field.FIELD_RIGHT | Field.USE_ALL_WIDTH);
	        
	        if(pages != null) {
	        	lblPagesNumber = getLabel(getNumberOfTotalPageLabel(pages.length), Field.FIELD_BOTTOM | Field.FIELD_RIGHT);
	        	lblNewPagesNumber = getLabel(getNumberOfNewPageLabel(numberOfNewPosts), Field.FIELD_BOTTOM | Field.FIELD_RIGHT);
	        }
	        else {
	        	lblPagesNumber = getLabel("N.A.", Field.FIELD_BOTTOM | Field.FIELD_RIGHT);
	        	lblNewPagesNumber = getLabel("", Field.FIELD_BOTTOM | Field.FIELD_RIGHT);
	        }
	        lblPagesNumber.setFont(lblPagesNumber.getFont().derive(Font.PLAIN));
	        lblNewPagesNumber.setFont(lblNewPagesNumber.getFont().derive(Font.PLAIN));
	        pageNumbersFieldManager.add(lblPagesNumber);
	        pageNumbersFieldManager.add(lblNewPagesNumber);
	        topButtonsManager.add(pageNumbersFieldManager);
	    
	        
	        //A HorizontalFieldManager to hold the posts list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);

			add(topButtonsManager);
			add(new SeparatorField());
			add(dataScroller);
			buildList(pages);
	 }

	 private String getNumberOfNewPageLabel(int recentPage) {
	        String numerOfPageLabel = null;
	        numerOfPageLabel=" (" +recentPage+" "+_resources.getString(WordPressResource.LABEL_PAGE_NUMBER_NEW)+")";
	        return numerOfPageLabel;
		 
	 }
	 
	 private String getNumberOfTotalPageLabel(int pageNum) {
	        String numerOfPostLabel = null;
	        
	        if(pageNum > 1) 
	        	numerOfPostLabel= pageNum+ " " +_resources.getString(WordPressResource.LABEL_PAGE_NUMBERS); 
	        else 
	        	numerOfPostLabel= pageNum+ " " +_resources.getString(WordPressResource.LABEL_PAGE_NUMBER);
	        
	        return numerOfPostLabel;
		 
	 }
	 
	private void buildList(Page[] pages) {
		removeAllMenuItems();
        
		Hashtable elements[]= new Hashtable[0];
		
		if(pages != null) {						
			elements= new Hashtable[pages.length];
			//Populate the vector with the elements [title, data, title, data ....]
	        for (int i = 0; i < pages.length; i++) {
	        	Page currentPage = pages[i];
	            String title = currentPage.getTitle();
	             if (title == null || title.length() == 0) {
	                 title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
	             }
	             
	             Hashtable smallPostData = new Hashtable();
	        	 
	             smallPostData .put("title", title);
	             Date dateCreated = currentPage.getDateCreatedGMT();
	             if (dateCreated != null)
	            	 smallPostData .put("date_created_gmt", dateCreated);
	             
	             elements[i]=smallPostData;
	         }			
		}
						
		pagesList = new PostsListField(); 	        
		pagesList.set(elements);
		pagesList.setEmptyString(_resources.getString(WordPressResource.MESSAGE_NO_POSTS), DrawStyle.LEFT);
		dataScroller.add(pagesList);


		if(pages != null && pages.length > 0 ) {
			addMenuItem(_editPostItem);
			addMenuItem(_deletePostItem);
		}
		
		addMenuItem(_refreshPostListItem);
		//addMenuItem(_draftPostsItem);
	}
	 

    private MenuItem _editPostItem = new MenuItem( _resources, WordPressResource.MENUITEM_EDIT, 200, 10) {
        public void run() {
            int selectedPost = pagesList.getSelectedIndex();
            controller.editPage(selectedPost);
        }
    };
	
	private MenuItem _deletePostItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 210, 10) {
        public void run() {
            int selectedPost = pagesList.getSelectedIndex();
            controller.deletePage(selectedPost);
        }
    };
    
    private MenuItem _refreshPostListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH, 220, 10) {
        public void run() {
        	controller.refreshPagesList();
        }
    };
     
    /*
    private MenuItem _draftPostsItem = new MenuItem( _resources, WordPressResource.MENUITEM_LOCALDRAFT, 120, 10) {
        public void run() {
    	 controller.showDraftPages(); 
        }
    };
 */
    
	private FieldChangeListener listenerButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(field == buttonNewPost){
	    		controller.newPage();	    		
	    	} else if(field == buttonRefresh){
	    		controller.refreshPagesList(); //reload only the posts list
	    	} else if(field == buttonDraftPosts) {
	    		controller.showDraftPages(); 
	    	}
	   }
	};

	
	public void refresh(Page[] pages, int count){
		dataScroller.delete(pagesList);
		
		if(pages != null) {
			lblPagesNumber.setText(getNumberOfTotalPageLabel(pages.length));
			lblNewPagesNumber.setText(getNumberOfNewPageLabel(count));
		} else {
			lblPagesNumber.setText("N.A.");
			lblNewPagesNumber.setText("");
		}
		buildList(pages);
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