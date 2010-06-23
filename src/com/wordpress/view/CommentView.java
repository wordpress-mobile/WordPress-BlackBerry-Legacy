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
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

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
import com.wordpress.view.container.BorderedFocusChangeListenerPatch;

public class CommentView extends StandardBaseView {
	
    private RecentCommentsController controller= null;
    private final GravatarController gvtCtrl;
    private HorizontalFieldManager rowFrom;
    private VerticalFieldManager rowFromTextManager;
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
	
	 public CommentView(RecentCommentsController _controller, Comment comment, Hashtable commentStatusList, GravatarController gvtCtrl) {
	    	super(_resources.getString(WordPressResource.TITLE_COMMENTVIEW), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
	    	this.controller=_controller;
			this.comment = comment;
			this.commentStatusList = commentStatusList;
			this.gvtCtrl = gvtCtrl;
	   
	        //row from
	        BorderedFieldManager outerManagerFrom = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
	        
			LabelField lblCommentAuthor = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_AUTHOR),
					Color.BLACK);	
			outerManagerFrom.add(lblCommentAuthor);
			outerManagerFrom.add(GUIFactory.createSepatorField());
	        rowFrom = new HorizontalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
	                
	        gravatarBitmapField = new BitmapField(GravatarController.defaultGravatarBitmap.getBitmap(), BitmapField.NON_FOCUSABLE | Manager.FIELD_VCENTER);

	        rowFromTextManager = new VerticalFieldManager(VerticalFieldManager.NO_VERTICAL_SCROLL | VerticalFieldManager.NO_HORIZONTAL_SCROLL 
	        		| Manager.FIELD_VCENTER)
	        {//add the focus change listener patch
	        	public void add( Field field ) {
	        		super.add( field );
	        		field.setFocusListener(null);
	        		field.setFocusListener(new BorderedFocusChangeListenerPatch()); 
	        	}
	        };
	        
			authorName = new LabelField("", LabelField.FOCUSABLE);
			authorEmail = new LabelField("", LabelField.FOCUSABLE);
			authorUrl = new LabelField("", LabelField.FOCUSABLE);
	        rowFrom.add(gravatarBitmapField);
	        rowFrom.add(new LabelField("  ", LabelField.NON_FOCUSABLE));
	        rowFrom.add(rowFromTextManager);
	        outerManagerFrom.add(rowFrom);
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
			LabelField lblTitle = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_COMMENT_ON)+":", Color.BLACK);
			title = new LabelField("", LabelField.FOCUSABLE);
	        rowOn.add(lblTitle);
	        rowOn.add(title);
	        rowOn.setMargin(5, 0, 0, 0);
	        outerManagerInfo.add(rowOn);
	        	        
	        //date
	        HorizontalFieldManager rowDate = new HorizontalFieldManager();
			LabelField lblDate = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_DATE)+":", Color.BLACK);
			date = new LabelField("", LabelField.FOCUSABLE);
	        rowDate.add(lblDate);
	        rowDate.add(date);
	        rowDate.setMargin(5, 0, 0, 0);
	        outerManagerInfo.add(rowDate);
	        
	  		//status
	        HorizontalFieldManager rowStatus = new HorizontalFieldManager();
	        LabelField lblStatus = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_POST_STATUS)+":", Color.BLACK);
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
				 + controller.getCommentIndex(comment)+"/"+controller.getCommentsCount());
		 
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
			 gravatarBitmapField = new BitmapField(gvtCtrl.getLatestGravatar(newComment.getAuthorEmail()).getBitmap(), BitmapField.NON_FOCUSABLE | Manager.FIELD_VCENTER);
		 }
		 
		 rowFrom.deleteAll();
		 rowFrom.add(gravatarBitmapField);
	     rowFrom.add(new LabelField("  ", LabelField.NON_FOCUSABLE));
		 
		 rowFromTextManager.deleteAll();
		 if(newComment.getAuthor() != null && !newComment.getAuthor().equals("")){
			 authorName.setText(newComment.getAuthor());
			 rowFromTextManager.add(authorName);
		 }
		 		 
		 if(newComment.getAuthorEmail() != null && !newComment.getAuthorEmail().equals("")) {
			 authorEmail.setText(newComment.getAuthorEmail());
			 rowFromTextManager.add(authorEmail);
		 }

		 if(newComment.getAuthorUrl() != null && !newComment.getAuthorUrl().equals("")) {
			 authorUrl = GUIFactory.createURLLabelField(newComment.getAuthorUrl(), newComment.getAuthorUrl(), LabelField.FOCUSABLE);
			 rowFromTextManager.add(authorUrl);
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
		 rowFrom.add(rowFromTextManager);
		 		 
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
			 String content= newComment.getContent();			 
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
	 
	public BaseController getController() {
		return this.controller;
	}

	public boolean onClose()   {
		controller.backCmd();
		return true;
	}
	
	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus
		// is on the author url field
		if (getLeafFieldWithFocus() == authorUrl && instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
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

    
    private BitmapField createClicableBitmapField(Bitmap bitmap, final String largePhotoURL) {

		BitmapField img = new BitmapField(bitmap, 
				Field.FIELD_VCENTER | Field.FOCUSABLE) {

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
	    				Comment next = controller.getNextComment(comment);
	        			if (next != null) {
	        				setViewValues(next);
	        			}
	    				return true;
	    			}
	    			if(gesture.getSwipeDirection() == TouchGesture.SWIPE_WEST) {
	    				Comment next = controller.getPreviousComment(comment);
	        			if (next != null) {
	        				setViewValues(next);
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

