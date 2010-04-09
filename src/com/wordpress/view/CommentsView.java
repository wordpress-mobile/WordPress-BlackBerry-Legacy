package com.wordpress.view;

import java.util.Vector;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.CommentsController;
import com.wordpress.controller.GravatarController;
import com.wordpress.model.Comment;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.CommentsListField;
import com.wordpress.view.component.ListActionListener;

public class CommentsView extends BaseView implements ListActionListener {
	
    private CommentsController controller= null;
    private VerticalFieldManager dataScroller;
	private CommentsListField commentListController;
	private GravatarController gravatarController;
	private ListField commentsList;
	
	 public CommentsView(CommentsController _controller, Comment[] comments, GravatarController gvtCtrl, String title) {
	    	super(_resources.getString(WordPressResource.TITLE_COMMENTS)+" > "+ title, MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
			gravatarController = gvtCtrl;
	                
	        this.setSubTitleText(_resources.getString(WordPressResource.MENUITEM_COMMENTS)+" "+controller.getCommentsCount());
	    	
	        //A Vertical FM to hold the comments list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);
	        
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
		gravatarController.deleteObservers(); //FIXME non va bene così
		commentListController= new CommentsListField(elements, selectedCom, gravatarController);
    	this.commentsList= commentListController.getCommentListField();
    	commentListController.setDefautActionListener(this);
		dataScroller.add(commentsList);
		
		//update comment number
	    this.setSubTitleText(_resources.getString(WordPressResource.MENUITEM_COMMENTS)+" "+controller.getCommentsCount());
		
		switchMenu();
		addMenuItem(_refreshCommentsListItem);
		
		//start the gravatar task
		Vector emails = new Vector(); //email of the comment author
		int elementLength = elements.length;
		for(int count = 0; count < elementLength; ++count) {
			String authorEmail = elements[count].getAuthorEmail();
			if (!authorEmail.equalsIgnoreCase(""))
				emails.addElement(authorEmail);
		}	
		
		//the pending mode is only a view over the full mode, isn't needed a new synchro for gravatars.
	/*	if(!controller.isPendingMode()) {
			gravatarController.startGravatarTask(emails);
		}
		*/
		if(!gravatarController.isRunning()) {
			gravatarController.startGravatarTask(emails);
		} else {
			Log.trace("...AAAAAAAAAAAAA...");
		}
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
		
		removeMenuItem(_showAllCommentItem);
		removeMenuItem(_showOnlyPendingCommentItem);
		
		if(controller.isPendingMode()) {
			addMenuItem(_showAllCommentItem);			
		} else {
			addMenuItem(_showOnlyPendingCommentItem);
		}
	}
	
	private Comment[] getSelectedComment() {
		Comment[] selectedComments = new Comment[1];
		        
		if (commentListController.isCheckBoxVisible()) {
			selectedComments = commentListController.getSelectedComments();
			for (int i = 0; i < selectedComments.length; i++) {
				Log.trace("Selected comment: "+selectedComments[i].getContent());
			}
		} else {
			Comment focusedComment = commentListController.getFocusedComment();
			Log.trace("Focus on comment: "+focusedComment.getContent());
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
    		gravatarController.stopGravatarTask(); //stop task if already running
    		controller.refreshComments();
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
    
    private MenuItem _showOnlyPendingCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_VIEW_PENDING, 1000400, 200) {
    	public void run() {
    		//gravatarController.stopGravatarTask(); //stop task if already running
    		controller.showPendingComments();	    		
    	}
    };

    private MenuItem _showAllCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_VIEW_ALL, 1000400, 200) {
    	public void run() {
    		//gravatarController.stopGravatarTask(); //stop task if already running
    		controller.showAllComments();
    	}
    };


    //called when remote loading is finished
    public void refresh(Comment[] comments){
    	dataScroller.delete(commentsList);
    	if(controller.isPendingMode()) {
    		Vector pendingComments = new Vector();
    		for (int i = 0; i < comments.length; i++) {
    			Comment	comment = comments[i];
    			if ( comment.getStatus().equalsIgnoreCase("hold") )
    				pendingComments.addElement(comment);
    			    			
    		}
    		Comment[] onlyPending = new Comment[pendingComments.size()];
    		pendingComments.copyInto(onlyPending);
    		buildList(onlyPending);
    	} else {
    		buildList(comments);
    	}
    }

	public BaseController getController() {
		return this.controller;
	}

	public boolean onClose()   {
		gravatarController.deleteObservers(); //remove the observers but continue to working
		gravatarController.stopGravatarTask(); //stop task if already running
		controller.backCmd();
		return true;
	}
	
	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus
		// is on the list
		if (getLeafFieldWithFocus() == commentsList
				&& instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
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

	public void actionPerformed() {
		if(commentsList != null && commentsList.getSize() > 0) {
			Comment selectedComment = getSelectedComment()[0]; //in this case return array with only one comment
			controller.openComment(selectedComment);
		}
	}
}