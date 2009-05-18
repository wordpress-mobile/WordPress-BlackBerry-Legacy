package com.wordpress.view;

import java.util.Date;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.CommentsController;
import com.wordpress.model.Comment;

public class CommentView extends BaseView {
	
    private CommentsController controller= null;
    private HorizontalFieldManager topManager;
    private VerticalFieldManager dataScroller;
	private LabelField lblPostsNumber;
	private LabelField status; //this information can change by user interaction
	private Comment comment; 
	
	 public CommentView(CommentsController _controller, Comment comment) {
	    	super(_resources.getString(WordPressResource.TITLE_COMMENTVIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
			this.comment = comment;
	                
	    	  //A HorizontalFieldManager to hold the posts number label
	    	topManager = new HorizontalFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
	            | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH | HorizontalFieldManager.FIELD_HCENTER);

	        lblPostsNumber = getLabel("Sample fixed Label");
	        topManager.add(lblPostsNumber);
	    	
	        //A Vertical FM to hold the comments list
	        dataScroller = new VerticalFieldManager(VerticalFieldManager.VERTICAL_SCROLL | VerticalFieldManager.VERTICAL_SCROLLBAR);
	        	        
	        //row from
	        HorizontalFieldManager rowFrom = new HorizontalFieldManager();
			LabelField lblFrom = getLabel(_resources.getString(WordPressResource.LABEL_COMMENT_FROM));
			LabelField from = new LabelField(comment.getAuthor() + " "+ comment.getAuthorEmail(), LabelField.FOCUSABLE);
			from.setMargin(margins);
			rowFrom.add(lblFrom);
			rowFrom.add(from);
	        dataScroller.add(rowFrom);
	        dataScroller.add(new SeparatorField());
	        
	        //row on (post of this comment)
	        HorizontalFieldManager rowOn = new HorizontalFieldManager();
			LabelField lblTitle = getLabel(_resources.getString(WordPressResource.LABEL_COMMENT_ON));
			LabelField title = new LabelField(comment.getPostTitle(), LabelField.FOCUSABLE);
	        title.setMargin(margins);
	        rowOn.add(lblTitle);
	        rowOn.add(title);
	        dataScroller.add(rowOn);
	        dataScroller.add(new SeparatorField());
	        	        
	        //row date
	        HorizontalFieldManager rowDate = new HorizontalFieldManager();
			LabelField lblDate = getLabel(_resources.getString(WordPressResource.LABEL_COMMENT_DATE));
			//formatting date value
			Date dateCreated = comment.getDate_created_gmt();
	        SimpleDateFormat sdFormat3 = new SimpleDateFormat("yyyy/MM/dd hh:mm");
	        String format = sdFormat3.format(dateCreated);
			//end
	        LabelField date = new LabelField(format, LabelField.FOCUSABLE);
	        date.setMargin(margins);
	        rowDate.add(lblDate);
	        rowDate.add(date);
	        dataScroller.add(rowDate);
	        dataScroller.add(new SeparatorField());	        
	        
	  		//row status
	        HorizontalFieldManager rowStatus = new HorizontalFieldManager();
	  		LabelField lblStatus =getLabel(_resources.getString(WordPressResource.LABEL_POST_STATUS));
	  		status =new LabelField(comment.getStatus(), LabelField.FOCUSABLE); //TODO decodificare
	  		rowStatus.add(lblStatus);
	  		rowStatus.add(status); 
	  		rowStatus.add(new NullField());
	  		dataScroller.add(rowStatus);
	  		dataScroller.add(new SeparatorField()); 
	        	  			  		
	  		LabelField commentContent= new LabelField(comment.getContent(), LabelField.FOCUSABLE);
	  		dataScroller.add(commentContent);
	  			        	  		
			add(topManager);
			add(new SeparatorField());
			add(dataScroller);

			addMenuItem(_approveCommentItem);
			addMenuItem(_deleteCommentItem);
			addMenuItem(_holdCommentItem);
			addMenuItem(_spamCommentItem);	
			
	 }
 
	 	
    private MenuItem _approveCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_APPROVE, 1000, 5) {
        public void run() {
        	Comment[] selectedComment = {comment};
        	controller.updateComments(selectedComment, "approve");
        	status.setText("Approved");
        }
    };
    
    
    private MenuItem _holdCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_HOLD, 1020, 5) {
        public void run() {
        	Comment[] selectedComment =  {comment};
        	controller.updateComments(selectedComment, "hold");
          	status.setText("Holded");
        }
    };
    
    private MenuItem _spamCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SPAM, 1010, 5) {
        public void run() {
        	Comment[] selectedComment =  {comment};
        	controller.updateComments(selectedComment, "spam");
        	status.setText("Spam");
        }
    };
    
    
    private MenuItem _deleteCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_DELETE, 1030, 5) {
    	public void run() {
    		Comment[] selectedComment =  {comment};
    		controller.deleteComments(selectedComment);
    	}
    };
    

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

