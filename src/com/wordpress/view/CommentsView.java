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
	
	 public CommentsView(RecentCommentsController _controller, GravatarController gvtCtrl, String title) {
	    	super(_resources.getString(WordPressResource.TITLE_COMMENTS)+" > "+ title, MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
			this.gravatarController = gvtCtrl;
	      //  this.setSubTitleText(_resources.getString(WordPressResource.MENUITEM_COMMENTS)+" "
	       // 		+ (comments == null ? 0 : comments.length));
	        //A Vertical FM to hold the comments list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL
	                 | VerticalFieldManager.VERTICAL_SCROLLBAR);
			add(dataScroller);
			buildList();
	 }

	//create the list. if comments is null you can't manage comments 
	private void buildList() {
		removeAllMenuItems();
		
		Comment[] comments = getComments();
		
		boolean[] selectedCom = null;
		if(comments != null) {
			selectedCom = new boolean[comments.length];	
		} else {
			comments = new Comment[0];
			selectedCom = new boolean[0];
		}
		
		gravatarController.deleteObservers(); //FIXME non va bene così
		commentListController= new CommentsListField(comments, selectedCom, gravatarController);
    	this.commentsList = commentListController.getCommentListField();
    	commentListController.setDefautActionListener(this);
		dataScroller.add(commentsList);
		
		updateSubTitle(comments);
		
		addMenuItem(_refreshCommentsListItem);
		
		//start the gravatar task
		Vector emails = new Vector(); //email of the comment author
		int elementLength = comments.length;
		for(int count = 0; count < elementLength; ++count) {
			String authorEmail = comments[count].getAuthorEmail();
			if (!authorEmail.equalsIgnoreCase(""))
				emails.addElement(authorEmail);
		}	
		
		if(!gravatarController.isRunning()) {
			gravatarController.startGravatarTask(emails);
		}
	}
	
	private Comment[] getComments() {
		Comment[] comments = controller.getCommentList();
		String postStatusFilter = controller.getStatusFilter();
    	//this filter on view is necessary when your have a filter applied 
    	//(such as hold) and changing the status of a post it should not e visible 
    	//anymore
    	if(postStatusFilter != null && !postStatusFilter.equals("")) {
    		Vector pendingComments = new Vector();
    		for (int i = 0; i < comments.length; i++) {
    			Comment	comment = comments[i];
    			if ( comment.getStatus().equalsIgnoreCase(postStatusFilter) )
    				pendingComments.addElement(comment);
    		}
    		Comment[] filteredComments = new Comment[pendingComments.size()];
    		pendingComments.copyInto(filteredComments);
    		
    		comments = filteredComments;
    	} 
    	
    	return comments;
	}
	
	private void updateSubTitle(Comment[] comments) {
		int count = comments.length;;
		String postStatusFilter = controller.getStatusFilter();
    /*	//this filter on view is necessary when your have a filter applied 
    	//(such as hold) and changing the status of a post it should not e visible 
    	//anymore
    	if(postStatusFilter != null && !postStatusFilter.equals("")) {
    		for (int i = 0; i < comments.length; i++) {
    			Comment	comment = comments[i];
    			if ( comment.getStatus().equalsIgnoreCase(postStatusFilter) )
    				count++;
    		}
    	} else {
    		count = comments.length;
    	}
*/
    	String localizedStatusText = "";
    	if (postStatusFilter.equals(RecentCommentsController.PENDINGS_STATUS)){
    		localizedStatusText = _resources.getString(WordPressResource.LABEL_PENDING)+" ";
    	} else if (postStatusFilter.equals(RecentCommentsController.SPAM_STATUS)) {
    		localizedStatusText = _resources.getString(WordPressResource.LABEL_SPAM)+" ";
    	}
		//update comment number
	    this.setSubTitleText(localizedStatusText + _resources.getString(WordPressResource.MENUITEM_COMMENTS)+" "+count);
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
    		showCommentView(selectedComment);
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

    private MenuItem _showOnlyPendingCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SHOW_PENDINGS, 240000, 200) {
    	public void run() {
    		controller.showCommentsByType(RecentCommentsController.PENDINGS_STATUS);
    	}
    };

    private MenuItem _showAllCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SHOW_ALL, 240000, 200) {
    	public void run() {
    		controller.resetViewToAllComments();
    	}
    };

    private MenuItem _showSpamCommentsItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SHOW_SPAM, 241000, 100) {
    	public void run() {
    		controller.showCommentsByType(RecentCommentsController.SPAM_STATUS);    	
    	}
    };

    private MenuItem _refreshCommentsListItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH, 322000, 100) {
    	public void run() {
    		gravatarController.stopGravatarTask(); //stop task if already running
    		controller.refreshComments();
    	}
    };
    
    private MenuItem _loadMoreComments = new MenuItem( _resources, WordPressResource.MENUITEM_LOADMORE, 323000, 200) {
    	public void run() {
    		gravatarController.stopGravatarTask(); //stop task if already running
    		controller.loadMoreComments();
    	}
    };

    //called when remote loading is finished
    public void refresh(){
    	dataScroller.delete(commentsList);
   		buildList();
    }

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
        
        if ( controller.getStatusFilter().equals("")) {
        	menu.add(_showOnlyPendingCommentItem);
        	menu.add(_showSpamCommentsItem);
        } else if ( controller.getStatusFilter().equals(RecentCommentsController.PENDINGS_STATUS)) {
        	menu.add(_showAllCommentItem);			
        	menu.add(_showSpamCommentsItem);
        } else {
        	menu.add(_showAllCommentItem);
        	menu.add(_showOnlyPendingCommentItem);
        }
        
		menu.add(_loadMoreComments);
        //Create the default menu.
        super.makeMenu(menu, instance);
    }

	public void actionPerformed() {
		if(commentsList != null && commentsList.getSize() > 0) {
			Comment selectedComment = getSelectedComment()[0]; //in this case return array with only one comment
			showCommentView(selectedComment);
		}
	}
	
	private void showCommentView(Comment comment) {	
		Comment[] comments = getComments();
		CommentView commentView= new CommentView(controller, comment, comments, gravatarController);
		UiApplication.getUiApplication().pushScreen(commentView);
	}

	public BaseController getController() {
		return this.controller;
	}
}