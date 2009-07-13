//#preprocess
package com.wordpress.view;

import java.util.Date;
import java.util.Hashtable;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.controller.CommentsController;
import com.wordpress.model.Comment;
import com.wordpress.view.component.HorizontalPaddedFieldManager;
import com.wordpress.view.component.HtmlTextField;

public class CommentView extends BaseView {
	
    private CommentsController controller= null;
    private HorizontalFieldManager topManager;
    private VerticalFieldManager dataScroller;
	
	private Comment comment; 
	private LabelField lblPostsNumber;
	private LabelField from;
	private LabelField title;
	private LabelField date;
	private LabelField status; //this information can change by user interaction
	private HtmlTextField commentContent;
	private final Hashtable commentStatusList;
	
	 public CommentView(CommentsController _controller, Comment comment, Hashtable commentStatusList) {
	    	super(_resources.getString(WordPressResource.TITLE_COMMENTVIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
			this.comment = comment;
			this.commentStatusList = commentStatusList;
	                
	    	  //A HorizontalFieldManager to hold the posts number label
	    	topManager = new HorizontalPaddedFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH | HorizontalFieldManager.FIELD_HCENTER);

	        lblPostsNumber = getLabel("");
	        topManager.add(lblPostsNumber);
	    	
	        //A Vertical FM to hold the comments list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.VERTICAL_SCROLLBAR);
	        	        
	        //row from
	        HorizontalFieldManager rowFrom = new HorizontalPaddedFieldManager();
			LabelField lblFrom = getLabel(_resources.getString(WordPressResource.LABEL_COMMENT_FROM));
			from = new LabelField("", LabelField.FOCUSABLE);
			rowFrom.add(lblFrom);
			rowFrom.add(from);
	        dataScroller.add(rowFrom);
	        dataScroller.add(new SeparatorField());
	        
	        //row on (post of this comment)
	        HorizontalFieldManager rowOn = new HorizontalPaddedFieldManager();
			LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_COMMENT_ON));
			title = new LabelField("", LabelField.FOCUSABLE);
	        rowOn.add(lblTitle);
	        rowOn.add(title);
	        dataScroller.add(rowOn);
	        dataScroller.add(new SeparatorField());
	        	        
	        //row date
	        HorizontalFieldManager rowDate = new HorizontalPaddedFieldManager();
			LabelField lblDate = getLabel(_resources.getString(WordPressResource.LABEL_COMMENT_DATE));
			date = new LabelField("", LabelField.FOCUSABLE);
	        rowDate.add(lblDate);
	        rowDate.add(date);
	        dataScroller.add(rowDate);
	        dataScroller.add(new SeparatorField());	        
	        
	  		//row status
	        HorizontalFieldManager rowStatus = new HorizontalPaddedFieldManager();
	        LabelField lblStatus = getLabel(_resources.getString(WordPressResource.LABEL_POST_STATUS));
	  		status =new LabelField("", LabelField.FOCUSABLE);
	  		rowStatus.add(lblStatus);
	  		rowStatus.add(status); 
	  		rowStatus.add(new NullField());
	  		dataScroller.add(rowStatus);
	  		dataScroller.add(new SeparatorField()); 
	        	  			
	  		commentContent = new HtmlTextField(" ");
	  		dataScroller.add(commentContent);
	  			        	  		
			add(topManager);
			add(new SeparatorField());
			add(dataScroller);
			
			addMenuItem(_deleteCommentItem);
			//addMenuItem(_nextCommentItem);
			//addMenuItem(_prevCommentItem);
			
			refresh(comment);
	 }
 
	 //refresh the view with the new comment content
	 private void refresh(Comment newComment){
		 this.comment = newComment;
		
		 
		 lblPostsNumber.setText(_resources.getString(WordPressResource.LABEL_COMMENT) + " "
				 + controller.getCommentIndex(comment)+"/"+controller.getCommentsCount());
		 
		 if(newComment.getAuthor() != null)
			 from.setText(comment.getAuthor() + " "+ comment.getAuthorEmail());
		 else 
			 from.setText("");
		 
		 if(newComment.getPostTitle() != null) {
			String commentTitleUnescaped = newComment.getPostTitle();
			title.setText(commentTitleUnescaped);
		 }
		 else 
			title.setText("");
		 
		 if(newComment.getDate_created_gmt() != null) {
				Date dateCreated = comment.getDate_created_gmt();
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
	 
	 	
    private MenuItem _approveCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_APPROVE, 1000, 100) {
        public void run() {
        	Comment[] selectedComment = {comment};
        	controller.updateComments(selectedComment, "approve", commentContent.getText());
        	status.setText("Approved");
        }
    };
    
    
    private MenuItem _holdCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_HOLD, 1020, 100) {
        public void run() {
        	Comment[] selectedComment =  {comment};
        	controller.updateComments(selectedComment, "hold", commentContent.getText());
          	status.setText("Holded");
        }
    };
    
    private MenuItem _spamCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SPAM, 1010, 100) {
        public void run() {
        	Comment[] selectedComment =  {comment};
        	controller.updateComments(selectedComment, "spam", commentContent.getText());
        	status.setText("Spam");
        }
    };
    
    
    private MenuItem _deleteCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_DELETE, 1030, 100) {
    	public void run() {
    		Comment[] selectedComment =  {comment};
    		Comment next = controller.getNextComment(selectedComment[0]);
    		if (next == null)
    			next = controller.getPreviousComment(selectedComment[0]);
    		    		
    		controller.deleteComments(selectedComment);
    		
    		if( next != null ) {
    			System.out.println("Abbiamo altri commeni da visualizzare");
    			refresh(next);
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
    			refresh(next);
    		}
    	}
    };
    
    private MenuItem _prevCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_PREV, 110, 6) {
    	public void run() {
    		
    		Comment prev = controller.getPreviousComment(comment);
    		if (prev == null)
    			controller.displayMessage("There arent next comments");
    		else {
    			refresh(prev);
    			System.out.println("abbiamo altri commenti");
    		}   		
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

