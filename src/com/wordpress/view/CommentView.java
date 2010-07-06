//#preprocess
package com.wordpress.view;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Hashtable;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.RecentCommentsController;
import com.wordpress.controller.GravatarController;
import com.wordpress.model.Comment;
import com.wordpress.utils.MD5;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.container.BorderedFieldManager;

public class CommentView extends StandardBaseView {
	
    private RecentCommentsController controller= null;
    private final GravatarController gvtCtrl;
	private Comment comment; 
	private BorderedFieldManager outerManagerFrom;
	private BasicEditField authorName;
	private BasicEditField authorEmail;
	private BasicEditField authorUrl;
	private BitmapField gravatarBitmapField;
	private LabelField visitSiteLabelField;
	private LabelField title;
	private LabelField date;
	private LabelField status; //this information can change by user interaction
	private HtmlTextField commentContent;
	private final Hashtable commentStatusList;
	private Comment[] comments;
	
	 public CommentView(RecentCommentsController _controller, Comment comment, Comment[] comments, GravatarController gvtCtrl) {
	    	super(_resources.getString(WordPressResource.TITLE_COMMENTVIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
			this.comment = comment;
			this.comments = comments;
			this.commentStatusList = controller.getCommentStatusList();
			this.gvtCtrl = gvtCtrl;
	   
	        //row from
	        outerManagerFrom = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
	        
			LabelField lblCommentAuthor = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_AUTHOR),
					Color.BLACK);	
			outerManagerFrom.add(lblCommentAuthor);
			outerManagerFrom.add(GUIFactory.createSepatorField());
	                
	        gravatarBitmapField = new BitmapField(GravatarController.defaultGravatarBitmap.getBitmap(), BitmapField.NON_FOCUSABLE | Manager.FIELD_VCENTER);
	        outerManagerFrom.add(gravatarBitmapField);
	        
			authorName = new BasicEditField(_resources.getString(WordPressResource.LABEL_NAME)+": ", "", 255, Field.EDITABLE);
			authorName.setMargin(5, 0, 0, 0);
			outerManagerFrom.add(authorName);

			authorEmail = new BasicEditField(_resources.getString(WordPressResource.LABEL_EMAIL)+": ", "", 255, Field.EDITABLE | BasicEditField.FILTER_EMAIL);
			authorEmail.setMargin(5, 0, 0, 0);
			outerManagerFrom.add(authorEmail);
			
			authorUrl = new BasicEditField(_resources.getString(WordPressResource.LABEL_URL)+": ", "", 255, Field.EDITABLE | BasicEditField.FILTER_URL) {
				protected MenuItem myContextMenuItemA = new MenuItem(_resources.getString(WordPressResource.LABEL_VISIT_SITE), 10, 2) {
					public void run() {
						Tools.getNativeBrowserSession(getText());
					}
				};

				protected void makeContextMenu(ContextMenu contextMenu) {
					contextMenu.addItem(myContextMenuItemA);
				}
			};
			authorUrl.setMargin(5, 0, 0, 0);
			outerManagerFrom.add(authorUrl);
			add(outerManagerFrom);
	        
	        //Informations box
	        BorderedFieldManager outerManagerInfo = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
			LabelField lblCommentInfo = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_INFORMATIONS),
					Color.BLACK);	
			outerManagerInfo.add(lblCommentInfo);
			outerManagerInfo.add(GUIFactory.createSepatorField());
			
	  		 //post of this comment
	        HorizontalFieldManager rowOn = new HorizontalFieldManager();
			LabelField lblTitle = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_COMMENT_ON)+": ", Color.BLACK);
			title = new LabelField("", LabelField.FOCUSABLE);
	        rowOn.add(lblTitle);
	        rowOn.add(title);
	        rowOn.setMargin(5, 0, 0, 0);
	        outerManagerInfo.add(rowOn);
	        	        
	        //date
	        HorizontalFieldManager rowDate = new HorizontalFieldManager();
			LabelField lblDate = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_DATE)+": ", Color.BLACK);
			date = new LabelField("", LabelField.FOCUSABLE);
	        rowDate.add(lblDate);
	        rowDate.add(date);
	        rowDate.setMargin(5, 0, 0, 0);
	        outerManagerInfo.add(rowDate);
	        
	  		//status
	        HorizontalFieldManager rowStatus = new HorizontalFieldManager();
	        LabelField lblStatus = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_POST_STATUS)+": ", Color.BLACK);
	  		status =new LabelField("", LabelField.FOCUSABLE);
	  		rowStatus.add(lblStatus);
	  		rowStatus.add(status); 
	  		rowStatus.setMargin(5, 0, 0, 0);
	  		outerManagerInfo.add(rowStatus);
	  		
	  		add(outerManagerInfo);
	  		
	  		//comment data
	        BorderedFieldManager outerManagerComment = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL );
	        outerManagerComment.add(GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_CONTENT),
	        		Color.BLACK));
	        outerManagerComment.add(GUIFactory.createSepatorField());
	        commentContent = new HtmlTextField(" ");
	  		outerManagerComment.add(commentContent);
	  		add(outerManagerComment);	  			        	  		
	  		
	  		add(new LabelField("", Field.NON_FOCUSABLE)); //space after content			

	  		addMenuItem(_deleteCommentItem);
			addMenuItem(_replyCommentItem);
			
			setViewValues(comment);
	 }

	 //refresh the view with the new comment content
	 private void setViewValues(Comment newComment){
		 this.comment = newComment;

		 this.setTitleText(_resources.getString(WordPressResource.LABEL_COMMENT) + " "
				 + (getCommentIndex(comment)+1)+"/"+ comments.length);

		 String authorEmailStr = comment.getAuthorEmail();
		 String fullScreenGravatarURL = null;
		 if(gvtCtrl.isGravatarAvailable(authorEmailStr)) {
			 String hashAuthorEmail = null;
			 MD5 md5 = new MD5();
			 try {
				 md5.Update(authorEmailStr, null);
				 hashAuthorEmail = md5.asHex();
				 md5.Final();
				 int dimension = Math.min(Display.getHeight(),Display.getWidth());
				 dimension = Math.min(512, dimension); //gvt service image size limit is 512px
				 fullScreenGravatarURL = "http://www.gravatar.com/avatar/"+hashAuthorEmail+"?s="+dimension+"&d=mm";
			 } catch (UnsupportedEncodingException e) {
				 Log.error(e, "Error while hashing email for gravatar services");
			 }
		 }

		 if(fullScreenGravatarURL != null) {
			 gravatarBitmapField = createClicableBitmapField(gvtCtrl.getLatestGravatar(newComment.getAuthorEmail()).getBitmap(), fullScreenGravatarURL);
			 gravatarBitmapField.setSpace(2,2);
		 } else {
			 gravatarBitmapField = null; //new BitmapField(gvtCtrl.getLatestGravatar(newComment.getAuthorEmail()).getBitmap(), BitmapField.NON_FOCUSABLE | Field.FIELD_HCENTER);
		 }

		outerManagerFrom.deleteAll();
		LabelField lblCommentAuthor = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_AUTHOR),
				Color.BLACK);	
		outerManagerFrom.add(lblCommentAuthor);
		outerManagerFrom.add(GUIFactory.createSepatorField());
    	
		if(gravatarBitmapField != null){
			outerManagerFrom.add(gravatarBitmapField);
		}

		 if(newComment.getAuthor() != null){
			authorName.setText(newComment.getAuthor());
		 } else {
			 authorName.setText("");
		 }
		 //authorName.setDirty(false);
		 outerManagerFrom.add(authorName);

		 if(newComment.getAuthorEmail() != null) {
			authorEmail.setText(newComment.getAuthorEmail());
		 } else {
			 authorEmail.setText("");
		 }
		// authorEmail.setDirty(false);
		 outerManagerFrom.add(authorEmail);

		 if(newComment.getAuthorUrl() != null) {
			authorUrl.setText(newComment.getAuthorUrl());
		 } else {
			 authorUrl.setText("");
		 }
		// authorUrl.setDirty(false);
		 outerManagerFrom.add(authorUrl);
		 
		 visitSiteLabelField = GUIFactory.createURLLabelField(_resources.getString(WordPressResource.LABEL_VISIT_SITE).toLowerCase(),
					"http://", LabelField.FOCUSABLE);
		 FieldChangeListener listener = new FieldChangeListener() {
			 public void fieldChanged(Field field, int context) {
				 if(!authorUrl.getText().trim().equals("")) {
					 Tools.getNativeBrowserSession(authorUrl.getText());
				 }
			 }
		 };
		 visitSiteLabelField.setChangeListener(null); //bc the field already have a listener
		 visitSiteLabelField.setChangeListener(listener);
		 outerManagerFrom.add(visitSiteLabelField);

		 if(newComment.getPostTitle() != null) {
			 String commentTitleUnescaped = newComment.getPostTitle();
			 title.setText(commentTitleUnescaped);
		 }
		 else {
			 title.setText("");
		 }

		 if(newComment.getDateCreatedGMT() != null) {
			 Date dateCreated = comment.getDateCreatedGMT();
			 SimpleDateFormat sdFormat3 = new SimpleDateFormat("yyyy/MM/dd hh:mm");
			 String format = sdFormat3.format(dateCreated);
			 date.setText(format); 
		 } else {
			 date.setText("");
		 }

		 //retrive the string of comment state
		 if(newComment.getStatus() != null) {	 
			 if(commentStatusList != null && commentStatusList.containsKey(newComment.getStatus())){
				 String decodedState = (String) commentStatusList.get(newComment.getStatus());
				 status.setText(decodedState); 
			 } else {
				 status.setText(newComment.getStatus());
			 }
		 } else {
			 status.setText("");
		 }

		 if(newComment.getContent() != null) {
			 String content= newComment.getContent();			 
			 commentContent.setText(content);
		 } else {
			 commentContent.setText("");
		 }
		 //commentContent.setDirty(false);

		 this.setDirty(false);
	 }

	 /* check if a comment is modified. When changes are made, send it to the server */
	 private void updateComment(String status) {
		 boolean isModified = false;
		 
		 if (status != null)  {
			 isModified = true;
			 comment.setStatus(status); 
		 }
		 
		 if(commentContent.isDirty()) {
			 if(!commentContent.getText().trim().equals(comment.getContent())) {
				 comment.setContent(commentContent.getText());
				 isModified = true;
			 }
		 }
		 
		 if(authorName.isDirty()) {
			 if(!authorName.getText().trim().equals(comment.getAuthor())) {
				 comment.setAuthor(authorName.getText());
				 isModified = true;
			 }
		 }

		 if(authorEmail.isDirty()) {
			 if(!authorEmail.getText().trim().equals(comment.getAuthorEmail())) {
				 comment.setAuthorEmail(authorEmail.getText());
				 isModified = true;
			 }
		 }
		 
		 if(authorUrl.isDirty()){
			 if(!authorUrl.getText().trim().equals(comment.getAuthorUrl())) {
				 comment.setAuthorUrl(authorUrl.getText());
				 isModified = true;
			 }			 
		 }
		 
		 if(isModified) {
			 Comment[] selectedComment = {comment};
			 controller.updateComment(selectedComment);
		 }
		
		 // avoid dirty when empty characters are at the end of field'content...
		// authorName.setDirty(false);
		// authorEmail.setDirty(false);
		// authorUrl.setDirty(false);
		// commentContent.setDirty(false);
		 this.setDirty(false);
	 }
	 
	 private MenuItem _updateCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_UPDATE, 99000, 100) {
		 public void run() {
			 updateComment(null);
		 }
	 };
	 
    private MenuItem _approveCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_APPROVE, 100000, 100) {
        public void run() {
        	updateComment("approve");
    		if(commentStatusList.containsKey("approve")){
    			String decodedState = (String) commentStatusList.get("approve");
    			status.setText(decodedState); 
    		} else {
    			status.setText("Approved");
    		}
        }
    };
    
    private MenuItem _spamCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_SPAM, 101000, 100) {
    	public void run() {
    		updateComment("spam");
    		if(commentStatusList.containsKey("spam")){
    			String decodedState = (String) commentStatusList.get("spam");
    			status.setText(decodedState); 
    		} else {
    			status.setText("Spam");
    		}
    	}
    };
    
    private MenuItem _holdCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_HOLD, 102000, 100) {
    	public void run() {
    		updateComment("hold");
    		if(commentStatusList.containsKey("hold")){
    			String decodedState = (String) commentStatusList.get("hold");
    			status.setText(decodedState); 
    		} else {
    			status.setText("Pending");
    		}
    	}
    };
    
    private MenuItem _deleteCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_DELETE, 103000, 100) {
    	public void run() {
    		Comment[] selectedComment =  {comment};
    		Comment next = getNextComment(selectedComment[0]);
    		if (next == null)
    			next = getPreviousComment(selectedComment[0]);
    		    		
    		//delete the comment from the main cache
    		controller.deleteComments(selectedComment);

    		//update the local comments cache
    		if( next != null ) {
    			int removedElementIndex = getCommentIndex(comment);
    			Comment[] newCommentList = new Comment[comments.length-1];
    			int j = 0;
    			for (int i = 0; i < comments.length; i++) {
    				if(i != removedElementIndex) {
    					newCommentList[j] = comments[i];
    					j++;
    				}
				}
    			comments = newCommentList;
    		
    			setViewValues(next);
    		} else
    			controller.backCmd();
    	}
    };
    
    private MenuItem _nextCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_NEXT, 1000, 50) {
    	public void run() {

    		//check if there are made changes on the current comments
    		boolean flag = discardCommentChanged();
    		if(!flag) { 
    			return;
    		}

    		Comment next = getNextComment(comment);
    		if (next == null) {
    			//never falls here
    			controller.displayMessage("There arent next comments");   	
    		} else {
    			setViewValues(next);
    		}
    	}
    };

    private MenuItem _prevCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_PREV, 1100, 60) {
    	public void run() {
    		
    		boolean flag = discardCommentChanged();
    		if(!flag) { 
    			return;
    		}
    		
    		Comment prev = getPreviousComment(comment);
    		if (prev == null) {
    			//never falls here
    			controller.displayMessage("There arent next comments");
    		} else {
    			setViewValues(prev);
    		}   		
    	}
    };

    private MenuItem _replyCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_COMMENTS_REPLY, 104000, 100) {
    	public void run() {
    		boolean flag = discardCommentChanged();
    		if(!flag) { 
    			return;
    		} else {
    			controller.showReplyView(comment);
    		}
    	}
    };

	 private int  getCommentIndex(Comment currentComment) {
		 int index = -1;
		 for (int i = 0; i < comments.length; i++) {
			 Comment	comment = comments[i];
			 if (comment.getID() == currentComment.getID()) {
				 index = i;
				 break;
			 }
		 }
		 return index;
	 }

	 private Comment getPreviousComment(Comment currentComment) {
		 int index = -1;
		 for (int i = 0; i < comments.length; i++) {
			 Comment	comment = comments[i];
			 if (comment.getID() == currentComment.getID()) {
				 index = i;
				 break;
			 }
		 }
		 if(comments.length > index+1) {
			 return comments[index+1];
		 } else
			 return null;
	 }

	 /**
	  * 	
	  * @param currentComment
	  * @return the next comment from the comments list.
	  */
	 private Comment getNextComment(Comment currentComment){
		 int index = -1;
		 for (int i = 0; i < comments.length; i++) {
			 Comment	comment = comments[i];
			 if (comment.getID() == currentComment.getID()) {
				 index = i;
				 break;
			 }
		 }
		 //index = 0 mean that currentComment is the most recent comment
		 if(index > 0) {
			 return comments[index-1];
		 } else

			 return null;				
	 }
	 
	public BaseController getController() {
		return this.controller;
	}

	private boolean discardCommentChanged() {
		//we are checking the dirty state not on all fields
		if(commentContent.isDirty() || authorEmail.isDirty() 
				|| authorName.isDirty() 
				|| authorUrl.isDirty()) {
			int result= controller.askQuestion(_resources.getString(WordPressResource.MESSAGE_UNSAVED_CHANGES_LOST));   
			if(Dialog.YES==result) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	public boolean onClose()   {
		boolean flag = discardCommentChanged();
		if(flag) { 
			//changes made and do nothing is selected
			controller.backCmd();
			return true;
		} else {
			return false;
		}
	}
	
	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus
		// is on the author url field
		if (getLeafFieldWithFocus() == visitSiteLabelField && instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
	}

    //Override the makeMenu method so we can add a custom menu item on fly
    protected void makeMenu(Menu menu, int instance)
    {
		if(commentContent.isDirty() || authorEmail.isDirty() 
				|| authorName.isDirty() 
				|| authorUrl.isDirty())
			menu.add(_updateCommentItem);
    	
		
		 //retrive the string of comment state
		 if(comment.getStatus() != null) {
			 if(comment.getStatus().equalsIgnoreCase("approve")) {
				 menu.add(_holdCommentItem);
				 menu.add(_spamCommentItem);	
			 } else if(comment.getStatus().equalsIgnoreCase("hold")){
				 menu.add(_approveCommentItem);
				 menu.add(_spamCommentItem);	
			 } else if(comment.getStatus().equalsIgnoreCase("spam")){
				 menu.add(_approveCommentItem);
				 menu.add(_holdCommentItem);
			 }
		 } else {
			 menu.add(_approveCommentItem);
			 menu.add(_holdCommentItem);
			 menu.add(_spamCommentItem);
		 }
		
		Comment next = getNextComment(comment);
		if (next != null)
			menu.add(_nextCommentItem);
		
		Comment prev = getPreviousComment(comment);
		if (prev != null)
			menu.add(_prevCommentItem);
                
        //Create the default menu.
        super.makeMenu(menu, instance);
    }  

    
    private BitmapField createClicableBitmapField(Bitmap bitmap, final String largePhotoURL) {

		BitmapField img = new BitmapField(bitmap, Field.FOCUSABLE) {

			/**
			 * Overrides default implementation.  Performs default action if the 
			 * 4ways trackpad was clicked; otherwise, the default action occurs.
			 * 
			 * @see net.rim.device.api.ui.Screen#navigationClick(int,int)
			 */
			protected boolean navigationClick(int status, int time) {
				Log.trace(">>> navigationClick");

				if ((status & KeypadListener.STATUS_TRACKWHEEL) == KeypadListener.STATUS_TRACKWHEEL) {
					Log.trace("Input came from the trackwheel");
					// Input came from the trackwheel
					return super.navigationClick(status, time);

				} else if ((status & KeypadListener.STATUS_FOUR_WAY) == KeypadListener.STATUS_FOUR_WAY) {
					Log.trace("Input came from a four way navigation input device");
					Tools.getNativeBrowserSession(largePhotoURL);
					return true;
				}
				return super.navigationClick(status, time);
			}

			/**
			 * Overrides default.  Enter key will take default action on selected item.
			 *  
			 * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
			 * 
			 */
			protected boolean keyChar(char c, int status, int time) {
				Log.trace(">>> keyChar");
				// Close this screen if escape is selected.
				if (c == Characters.ENTER) {
					Tools.getNativeBrowserSession(largePhotoURL);
					return true;
				}
				return super.keyChar(c, status, time);
			}

			//#ifdef IS_OS47_OR_ABOVE
			protected boolean touchEvent(TouchEvent message) {
				Log.trace(">>> touchEvent");
				int eventCode = message.getEvent();

				// Get the screen coordinates of the touch event
				if(eventCode == TouchEvent.CLICK) {
					Log.trace("TouchEvent.CLICK");
					Tools.getNativeBrowserSession(largePhotoURL);
					return true;
				} 
				return false; 
			}
			//#endif

			protected MenuItem myContextMenuItemA = new MenuItem(_resources.getString(WordPressResource.MENUITEM_OPEN), 10, 2) {
				public void run() {
					Tools.getNativeBrowserSession(largePhotoURL);
				}
			};

			protected void makeContextMenu(ContextMenu contextMenu) {
				contextMenu.addItem(myContextMenuItemA);
			}
		};
		return img;
	}
    
    
	//#ifdef IS_OS47_OR_ABOVE
    protected boolean touchEvent(TouchEvent message) {
    	Log.trace(">>> touchEvent");
    	int eventCode = message.getEvent();

    	if(eventCode == TouchEvent.GESTURE) {
    		TouchGesture gesture = message.getGesture();
    		int gestureCode = gesture.getEvent();
    		Log.trace(">>> TouchEvent.GESTURE ->  "+gestureCode);
    		
    	
    		switch(gestureCode) {
    		
	    		case TouchGesture.SWIPE:
	    			if(gesture.getSwipeDirection() == TouchGesture.SWIPE_EAST) {
	    				Comment next = getNextComment(comment);
	        			if (next != null) {
	        		  		//check if there are made changes on the current comments
	        	    		boolean flag = discardCommentChanged();
	        	    		if(!flag) { 
	        	    			return true;
	        	    		} else {
	        	    			setViewValues(next);
	        	    		}
	        			}
	    				return true;
	    			}
	    			if(gesture.getSwipeDirection() == TouchGesture.SWIPE_WEST) {
	    				Comment next = getPreviousComment(comment);
	        			if (next != null) {
	        		  		//check if there are made changes on the current comments
	        	    		boolean flag = discardCommentChanged();
	        	    		if(!flag) { 
	        	    			return true;
	        	    		} else {
	        	    			setViewValues(next);
	        	    		}	        				
	        			}
	    				return true;
	    			}
	    			return false;
    		}
    	}

    	return super.touchEvent(message); 
    }
	//#endif
    
}

