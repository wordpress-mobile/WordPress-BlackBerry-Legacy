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
import com.wordpress.controller.RecentCommentsController;
import com.wordpress.controller.GravatarController;
import com.wordpress.model.Comment;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.CommentsListField;
import com.wordpress.view.component.ListActionListener;

public class CommentsView extends BaseView implements ListActionListener {
	
    private RecentCommentsController controller= null;
    private VerticalFieldManager dataScroller;
	private CommentsListField commentListController;
	private GravatarController gravatarController;
	private ListField commentsList;
	//private boolean pendingMode = false; //a flag to show only pending comments
	
	 public CommentsView(RecentCommentsController _controller, Comment[] comments, GravatarController gvtCtrl, String title) {
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
		
		addMenuItem(_refreshCommentsListItem);
		
		//start the gravatar task
		Vector emails = new Vector(); //email of the comment author
		int elementLength = elements.length;
		for(int count = 0; count < elementLength; ++count) {
			String authorEmail = elements[count].getAuthorEmail();
			if (!authorEmail.equalsIgnoreCase(""))
				emails.addElement(authorEmail);
		}	
		
		if(!gravatarController.isRunning()) {
			gravatarController.startGravatarTask(emails);
		} else {
			Log.trace("...AAAAAAAAAAAAA...");
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
	
    private MenuItem _openCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_OPEN, 1000, 50) {
    	public void run() {
    		Comment selectedComment = getSelectedComment()[0]; //in this case return array with only one comment
    		controller.showCommentView(selectedComment);
    	}
    };
            
    private MenuItem _approveCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_APPROVE, 70000, 100) {
        public void run() {
        	Comment[] selectedComment = getSelectedComment();
        	controller.updateComments(selectedComment, "approve", null);
        }
    };
    
    
    private MenuItem _holdCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_HOLD, 70000, 100) {
        public void run() {
        	Comment[] selectedComment = getSelectedComment();
        	controller.updateComments(selectedComment, "hold", null);
        }
    };
    
    private MenuItem _spamCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SPAM, 70000, 100) {
        public void run() {
        	Comment[] selectedComment = getSelectedComment();
        	controller.updateComments(selectedComment, "spam", null);
        }
    };
    
    
    private MenuItem _deleteCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_DELETE, 70000, 100) {
    	public void run() {
    		Comment[] selectedComment = getSelectedComment();
    		controller.deleteComments(selectedComment);
    		    		
    	}
    };
    
    
    private MenuItem _switchToSingleMode = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SINGLE, 140000, 100) {
    	public void run() {
    		commentListController.setCheckBoxVisible(false);
    	}
    };
    
    private MenuItem _switchToBulkMode = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_MULTIPLE, 140000, 100) {
    	public void run() {    		
    		commentListController.setCheckBoxVisible(true);
    	}
    };

    private MenuItem _showOnlyPendingCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_VIEW_PENDING, 240000, 200) {
    	public void run() {
    		//showPendingComments();
    		gravatarController.stopGravatarTask(); //stop task if already running
    		controller.setStatusFilter("hold");
    		controller.refreshComments();
    	}
    };

    private MenuItem _showAllCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_VIEW_ALL, 240000, 200) {
    	public void run() {
    		//showAllComments();
    		gravatarController.stopGravatarTask(); //stop task if already running
    		controller.resetViewToAllComments();
    	}
    };

    private MenuItem _showSpamCommentsItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_VIEW_SPAM, 241000, 100) {
    	public void run() {
    		gravatarController.stopGravatarTask(); //stop task if already running
    		controller.setStatusFilter("spam");
    		controller.refreshComments();
    	}
    };

    private MenuItem _loadMoreComments = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_LOADMORE, 242000, 200) {
    	public void run() {
    		gravatarController.stopGravatarTask(); //stop task if already running
    		controller.loadMoreComments();
    	}
    };
    
    private MenuItem _refreshCommentsListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH, 243000, 100) {
    	public void run() {
    		gravatarController.stopGravatarTask(); //stop task if already running
    		controller.refreshComments();
    	}
    };
            

    //called when remote loading is finished
    public void refresh(Comment[] comments){
    	dataScroller.delete(commentsList);
    /*	if(pendingMode) {
    		Vector pendingComments = new Vector();
    		for (int i = 0; i < comments.length; i++) {
    			Comment	comment = comments[i];
    			if ( comment.getStatus().equalsIgnoreCase("hold") )
    				pendingComments.addElement(comment);
    			    			
    		}
    		Comment[] onlyPending = new Comment[pendingComments.size()];
    		pendingComments.copyInto(onlyPending);
    		buildList(onlyPending);
    	} else {*/
    		buildList(comments);
    	//}
    }
  /*  
	private void showPendingComments() {
		pendingMode = true;
		refresh(controller.getCommentList());
	}
	
	private void showAllComments() {
		pendingMode = false;
		refresh(controller.getCommentList());
	}
	*/
	public boolean onClose()   {
		gravatarController.deleteObservers(); //remove the observers but continue to working
		gravatarController.stopGravatarTask(); //stop task if already running
		controller.backCmd();
		return true;
	}
	
	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus is on the list
		if (getLeafFieldWithFocus() == commentsList
				&& instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
	}
	
    /*
     * Override the makeMenu method so we can add a custom menu item
     * if the checkbox ListField has focus.
     * 
     * (non-Javadoc)
     * @see net.rim.device.api.ui.container.MainScreen#makeMenu(net.rim.device.api.ui.component.Menu, int)
     */
    protected void makeMenu(Menu menu, int instance)
    {
        
        if(commentsList != null && commentsList.getSize() > 0 ) {
        	
            Field focus = UiApplication.getUiApplication().getActiveScreen().getLeafFieldWithFocus();
            if(focus == commentsList) 
            {
                //The commentsList ListField instance has focus and we are in edit mode
                //Add the _toggleItem MenuItem.
                if(commentListController.isCheckBoxVisible())
                	menu.add(commentListController._toggleItem);
            }
        	
        	if(commentListController.isCheckBoxVisible() ) {
        		//multiple mode
        		menu.add(_approveCommentItem);
        		menu.add(_deleteCommentItem);
        		menu.add(_holdCommentItem);
        		menu.add(_spamCommentItem);
        		menu.add(_switchToSingleMode);
        	} else {
        		//single mode
        		menu.add(_openCommentItem);
        		menu.add(_switchToBulkMode);
        	}
        }
		
		//if(pendingMode) {
			menu.add(_showAllCommentItem);			
		//} else {
			menu.add(_showOnlyPendingCommentItem);
	//	}
			menu.add(_showSpamCommentsItem);
		
		//if(controller.isLoadMoreMenuItemAvailable())
		menu.add(_loadMoreComments);
		
		
        //Create the default menu.
        super.makeMenu(menu, instance);
    }

	public void actionPerformed() {
		if(commentsList != null && commentsList.getSize() > 0) {
			Comment selectedComment = getSelectedComment()[0]; //in this case return array with only one comment
			controller.showCommentView(selectedComment);
		}
	}

	public BaseController getController() {
		return this.controller;
	}
}