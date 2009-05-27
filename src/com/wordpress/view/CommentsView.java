package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.CommentsController;
import com.wordpress.model.Comment;
import com.wordpress.view.component.CommentsListField;

public class CommentsView extends BaseView {
	
    private CommentsController controller= null;
    private HorizontalFieldManager topManager;
    private VerticalFieldManager dataScroller;
	private LabelField lblPostsNumber;
	
	private CommentsListField commentListController;
	private ListField commentsList;
	private final Hashtable commentStatusList; 
	
	 public CommentsView(CommentsController _controller, Comment[] comments, Hashtable commentStatusList) {
	    	super(_resources.getString(WordPressResource.TITLE_COMMENTS), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
			this.commentStatusList = commentStatusList;
	                
	    	  //A HorizontalFieldManager to hold the posts number label
	    	topManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH | HorizontalFieldManager.FIELD_HCENTER);

	        lblPostsNumber = getLabel(_resources.getString(WordPressResource.MENUITEM_COMMENTS)+" "+controller.getCommentsCount());
	        topManager.add(lblPostsNumber);
	    	
	        //A Vertical FM to hold the comments list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);

	        
			add(topManager);
			add(new SeparatorField());
			add(dataScroller);
			buildList(comments);
	 }

	//create the list. if comments is null you can't manage comments 
	private void buildList(Comment[] comments) {
		removeAllMenuItems();
        
		Comment elements[]= new Comment[0];
		boolean[] selectedCom = new boolean[0];
		
		if(comments != null) {
			elements = comments;
			selectedCom = new boolean[comments.length];
		}
		commentListController= new CommentsListField(elements,selectedCom);
		
    	this.commentsList= commentListController.get_checkList();
		dataScroller.add(commentsList);
		switchMenu();
		addMenuItem(_refreshCommentsListItem);
	}
	 
	//change the main menu. if we are in multiple edit mode add comment action item
	private void switchMenu(){
		if(commentListController.isCheckBoxVisible()) {
			removeMenuItem(_openCommentItem);
			removeMenuItem(_editModeItem);
			if(commentsList != null && commentsList.getSize() > 0) {
				addMenuItem(_approveCommentItem);
				addMenuItem(_deleteCommentItem);
				addMenuItem(_holdCommentItem);
				addMenuItem(_spamCommentItem);
				addMenuItem(_viewCommentsItem);
			}
		} else {
			removeMenuItem(_viewCommentsItem);
			if(commentsList != null && commentsList.getSize() > 0) {
				addMenuItem(_openCommentItem);
				addMenuItem(_editModeItem);
			}
			removeMenuItem(_approveCommentItem);
			removeMenuItem(_deleteCommentItem);
			removeMenuItem(_holdCommentItem);
			removeMenuItem(_spamCommentItem);
			removeMenuItem(_viewCommentsItem);
		}
	}
	
	private Comment[] getSelectedComment() {
		Comment[] selectedComments = new Comment[1];
		        
		if (commentListController.isCheckBoxVisible()) {
			selectedComments = commentListController.getSelectedComments();
			for (int i = 0; i < selectedComments.length; i++) {
				System.out.println("Selected comment: "+selectedComments[i].getContent());
				
			}
		} else {
			Comment focusedComment = commentListController.getFocusedComment();
			System.out.println("Focus on comment: "+focusedComment.getContent());
			selectedComments[0]= focusedComment;
		}
		return selectedComments;
	}
	
	
    private MenuItem _viewCommentsItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SINGLE, 210, 100) {
    	public void run() {
    		commentListController.setCheckBoxVisible(false);
    		switchMenu();
        }
    };
	
    private MenuItem _openCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_OPEN, 210, 50) {
    	public void run() {
    		Comment selectedComment = getSelectedComment()[0]; //in this case return array with only one comment
    		controller.openComment(selectedComment);
    	}
    };
    
    private MenuItem _editModeItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_MULTIPLE, 220, 100) {
    	public void run() {    		
    		commentListController.setCheckBoxVisible(true);
    		switchMenu();
    	}
    };
    

    private MenuItem _refreshCommentsListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH, 220, 100) {
    	public void run() {
    		controller.refreshComments(true);
    	}
    };
    
    
    private MenuItem _approveCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_APPROVE, 100200, 5) {
        public void run() {
        	Comment[] selectedComment = getSelectedComment();
        	controller.updateComments(selectedComment, "approve", null);
        }
    };
    
    
    private MenuItem _holdCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_HOLD, 100200, 5) {
        public void run() {
        	Comment[] selectedComment = getSelectedComment();
        	controller.updateComments(selectedComment, "hold", null);
        }
    };
    
    private MenuItem _spamCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SPAM, 100300, 5) {
        public void run() {
        	Comment[] selectedComment = getSelectedComment();
        	controller.updateComments(selectedComment, "spam", null);
        }
    };
    
    
    private MenuItem _deleteCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_DELETE, 100400, 5) {
    	public void run() {
    		Comment[] selectedComment = getSelectedComment();
    		controller.deleteComments(selectedComment);
    		    		
    	}
    };
    

    //called when remote loading is finished
    public void refresh(Comment[] comments){
    	dataScroller.delete(commentsList);
    	buildList(comments);
    }

	public BaseController getController() {
		return this.controller;
	}

	/*
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		Field fieldWithFocus = this.getFieldWithFocus();
		if(fieldWithFocus == topManager) { //focus on the top buttons, do not open menu on whell click
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
	
    //Override the makeMenu method so we can add a custom menu item
    //if the checkbox ListField has focus.
    protected void makeMenu(Menu menu, int instance)
    {
        Field focus = UiApplication.getUiApplication().getActiveScreen().getLeafFieldWithFocus();
        if(focus == commentsList) 
        {
            //The commentsList ListField instance has focus and we are in edit mode
            //Add the _toggleItem MenuItem.
            if(commentListController.isCheckBoxVisible())
            	menu.add(commentListController._toggleItem);
        }
                
        //Create the default menu.
        super.makeMenu(menu, instance);
    }    

}

