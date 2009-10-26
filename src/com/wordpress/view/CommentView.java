//#preprocess
package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.controller.CommentsController;
import com.wordpress.controller.GravatarController;
import com.wordpress.model.Comment;
import com.wordpress.utils.Tools;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.BorderedFocusChangeListenerPatch;
import com.wordpress.view.component.HorizontalPaddedFieldManager;
import com.wordpress.view.component.HtmlTextField;

public class CommentView extends StandardBaseView {
	
    private CommentsController controller= null;
    private final GravatarController gvtCtrl;
    private VerticalFieldManager fromDataManager;
	
	private Comment comment; 
	private LabelField authorName;
	private LabelField authorEmail;
	private LabelField authorUrl;
	private BitmapField gravatarBitmapField;
	private LabelField title;
	private LabelField date;
	private LabelField status; //this information can change by user interaction
	private HtmlTextField commentContent;
	private final Hashtable commentStatusList;
	
	 public CommentView(CommentsController _controller, Comment comment, Hashtable commentStatusList, GravatarController gvtCtrl) {
	    	super(_resources.getString(WordPressResource.TITLE_COMMENTVIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
			this.comment = comment;
			this.commentStatusList = commentStatusList;
			this.gvtCtrl = gvtCtrl;
	   
	        //row from
	        BorderedFieldManager outerManagerFrom = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
	        
			LabelField lblCommentAuthor = getLabel(_resources.getString(WordPressResource.LABEL_AUTHOR));	
			outerManagerFrom.add(lblCommentAuthor);
	        HorizontalFieldManager rowFrom = new HorizontalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
	        gravatarBitmapField = new BitmapField(GravatarController.defaultGravatarBitmap.getBitmap(), BitmapField.NON_FOCUSABLE | Manager.FIELD_VCENTER);
	        fromDataManager = new VerticalFieldManager(VerticalFieldManager.NO_VERTICAL_SCROLL | VerticalFieldManager.NO_HORIZONTAL_SCROLL 
	        		| Manager.FIELD_VCENTER)
	        {//add the focus change listener patch
	        	public void add( Field field ) {
	        		super.add( field );
	        		field.setFocusListener(new BorderedFocusChangeListenerPatch()); 
	        	}
	        };
	        
			authorName = new LabelField("", LabelField.FOCUSABLE);
			authorEmail = new LabelField("", LabelField.FOCUSABLE);
			authorUrl = new LabelField("", LabelField.FOCUSABLE);
	        rowFrom.add(gravatarBitmapField);
	        rowFrom.add(new LabelField("  ", LabelField.NON_FOCUSABLE));
	        rowFrom.add(fromDataManager);
	        outerManagerFrom.add(rowFrom);
	        add(outerManagerFrom);
	        
	        //second manager
	        BorderedFieldManager outerManagerInfo = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
			LabelField lblCommentInfo = getLabel(_resources.getString(WordPressResource.LABEL_INFORMATIONS));	
			outerManagerInfo.add(lblCommentInfo);
			
	        //post of this comment
	        HorizontalFieldManager rowOn = new HorizontalPaddedFieldManager();
			LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_COMMENT_ON)+":");
			title = new LabelField("", LabelField.FOCUSABLE);
	        rowOn.add(lblTitle);
	        rowOn.add(title);
	        outerManagerInfo.add(rowOn);
	        	        
	        //date
	        HorizontalFieldManager rowDate = new HorizontalPaddedFieldManager();
			LabelField lblDate = getLabel(_resources.getString(WordPressResource.LABEL_DATE)+":");
			date = new LabelField("", LabelField.FOCUSABLE);
	        rowDate.add(lblDate);
	        rowDate.add(date);
	        outerManagerInfo.add(rowDate);
	        
	  		//status
	        HorizontalFieldManager rowStatus = new HorizontalPaddedFieldManager();
	        LabelField lblStatus = getLabel(_resources.getString(WordPressResource.LABEL_POST_STATUS)+":");
	  		status =new LabelField("", LabelField.FOCUSABLE);
	  		rowStatus.add(lblStatus);
	  		rowStatus.add(status); 
	  		outerManagerInfo.add(rowStatus);
	  		add(outerManagerInfo);
	  		
	  		//comment data
	        BorderedFieldManager outerManagerComment = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL );
	        outerManagerComment.add(getLabel(_resources.getString(WordPressResource.LABEL_CONTENT)));
	        commentContent = new HtmlTextField(" ");
	  		outerManagerComment.add(commentContent);
	  		add(outerManagerComment);	  			        	  		
	  		
	  		add(new LabelField("", Field.NON_FOCUSABLE)); //space after content			

	  		addMenuItem(_deleteCommentItem);
			addMenuItem(_replyCommentItem);
			
			setViewValues(comment);
	 }

	 private LabelField getAuthorUrlField(String url) {
		 
		 return new LabelField(url, LabelField.FOCUSABLE) {
		        
			 private MenuItem myContextMenuItemA = new MenuItem(_resources.getString(WordPressResource.MENUITEM_OPEN_URL), 10, 2) {
		            public void run() {
		            	Tools.getBrowserSession(getText());
		            }
		        };
		     
		        protected void makeContextMenu(ContextMenu contextMenu) {
		            contextMenu.addItem(myContextMenuItemA);
		        }
		 };
	 }
	 
	 //refresh the view with the new comment content
	 private void setViewValues(Comment newComment){
		 this.comment = newComment;
				 
		 this.setTitleText(_resources.getString(WordPressResource.LABEL_COMMENT) + " "
				 + controller.getCommentIndex(comment)+"/"+controller.getCommentsCount());
		 
		 gravatarBitmapField.setImage(gvtCtrl.getLatestGravatar(newComment.getAuthorEmail()));
		 
		 fromDataManager.deleteAll();
		 if(newComment.getAuthor() != null && !newComment.getAuthor().equals("")){
			 authorName.setText(newComment.getAuthor());
			 fromDataManager.add(authorName);
		 }
		 		 
		 if(newComment.getAuthorEmail() != null && !newComment.getAuthorEmail().equals("")) {
			 authorEmail.setText(newComment.getAuthorEmail());
			 fromDataManager.add(authorEmail);
		 }

		 if(newComment.getAuthorUrl() != null && !newComment.getAuthorUrl().equals("")) {
			 authorUrl = getAuthorUrlField(newComment.getAuthorUrl());
			 fromDataManager.add(authorUrl);
		 }
		 
		 if(newComment.getPostTitle() != null) {
			String commentTitleUnescaped = newComment.getPostTitle();
			title.setText(commentTitleUnescaped);
		 }
		 else 
			title.setText("");
		 
		 if(newComment.getDateCreatedGMT() != null) {
				Date dateCreated = comment.getDateCreatedGMT();
		        SimpleDateFormat sdFormat3 = new SimpleDateFormat("yyyy/MM/dd hh:mm");
		        String format = sdFormat3.format(dateCreated);
		        date.setText(format); 
 		 } else 
			 date.setText("");
		 
		 //retrive the string of comment state
		 if(newComment.getStatus() != null) {	 
			 if(commentStatusList != null && commentStatusList.containsKey(newComment.getStatus())){
				 String decodedState = (String) commentStatusList.get(newComment.getStatus());
				 status.setText(decodedState); 
			 } else {
				 status.setText(newComment.getStatus());
			 }

			
			 //remove unused menu Item
			removeMenuItem(_holdCommentItem);
			removeMenuItem(_spamCommentItem);
			removeMenuItem(_approveCommentItem);

			if(comment.getStatus().equalsIgnoreCase("approve")) {
				addMenuItem(_holdCommentItem);
				addMenuItem(_spamCommentItem);	
			} else if(comment.getStatus().equalsIgnoreCase("hold")){
				addMenuItem(_approveCommentItem);
				addMenuItem(_spamCommentItem);	
			} else if(comment.getStatus().equalsIgnoreCase("spam")){
				addMenuItem(_approveCommentItem);
				addMenuItem(_holdCommentItem);
			}

		 } else {
			 status.setText("");
			 addMenuItem(_approveCommentItem);
			 addMenuItem(_holdCommentItem);
			 addMenuItem(_spamCommentItem);
		 }
		 
		 if(newComment.getContent() != null) {
			 String content= BlogObjectController.buildBodyFieldContentFromHtml((newComment.getContent()));			 
			 commentContent.setText(content);
		 }
		else 
			 commentContent.setText("");
	 }
	 
    private MenuItem _approveCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_APPROVE, 100000, 100) {
        public void run() {
        	Comment[] selectedComment = {comment};
        	controller.updateComments(selectedComment, "approve", commentContent.getText());
        	status.setText("Approved");
        }
    };
    
    private MenuItem _spamCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SPAM, 101000, 100) {
        public void run() {
        	Comment[] selectedComment =  {comment};
        	controller.updateComments(selectedComment, "spam", commentContent.getText());
        	status.setText("Spam");
        }
    };
    
    private MenuItem _holdCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_HOLD, 102000, 100) {
        public void run() {
        	Comment[] selectedComment =  {comment};
        	controller.updateComments(selectedComment, "hold", commentContent.getText());
          	status.setText("Holded");
        }
    };
    
    
    private MenuItem _deleteCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_DELETE, 103000, 100) {
    	public void run() {
    		Comment[] selectedComment =  {comment};
    		Comment next = controller.getNextComment(selectedComment[0]);
    		if (next == null)
    			next = controller.getPreviousComment(selectedComment[0]);
    		    		
    		controller.deleteComments(selectedComment);
    		
    		if( next != null ) {
    			System.out.println("Abbiamo altri commeni da visualizzare");
    			setViewValues(next);
    		} else
    			controller.backCmd();
    		
    	}
    };
    
    private MenuItem _nextCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_NEXT, 100, 5) {
    	public void run() {
    		
    		Comment next = controller.getNextComment(comment);
    		if (next == null)
    			controller.displayMessage("There arent next comments");   	
    		else {
    			System.out.println("abbiamo altri commenti");
    			setViewValues(next);
    		}
    	}
    };
    
    private MenuItem _prevCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_PREV, 110, 6) {
    	public void run() {
    		
    		Comment prev = controller.getPreviousComment(comment);
    		if (prev == null)
    			controller.displayMessage("There arent next comments");
    		else {
    			setViewValues(prev);
    			System.out.println("abbiamo altri commenti");
    		}   		
    	}
    };

	 private MenuItem _replyCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_REPLY, 104000, 100) {
		 public void run() {
			 controller.showReplyView(comment);
		 }
	 };
	 
	 private MenuItem _openPostCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_OPEN_POST, 105000, 100) {
		 public void run() {
			 controller.showPostOrPage(comment);
		 }
	 };

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
	
    //Override the makeMenu method so we can add a custom menu item on fly
    protected void makeMenu(Menu menu, int instance)
    {
		Comment next = controller.getNextComment(comment);
		if (next != null)
			menu.add(_nextCommentItem);
		
		Comment prev = controller.getPreviousComment(comment);
		if (prev != null)
			menu.add(_prevCommentItem);
			   	
                
        //Create the default menu.
        super.makeMenu(menu, instance);
    }  

}

