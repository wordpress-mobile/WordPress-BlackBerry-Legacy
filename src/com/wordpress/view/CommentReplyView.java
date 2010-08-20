package com.wordpress.view;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.RecentCommentsController;
import com.wordpress.controller.GravatarController;
import com.wordpress.model.Blog;
import com.wordpress.model.Comment;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.MarkupToolBar;
import com.wordpress.view.component.MarkupToolBarTextFieldMediator;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.container.BorderedFocusChangeListenerPatch;
import com.wordpress.view.dialog.InquiryView;


public class CommentReplyView extends StandardBaseView {
	
	private Blog currentBlog;
	private RecentCommentsController controller;
	private Comment comment;
	
	private HtmlTextField replyContent;
	private LabelField wordCountField;
    
    public CommentReplyView(Blog currentBlog, RecentCommentsController _controller, Comment comment, GravatarController gvtCtrl) {
    	super(_resources.getString(WordPressResource.MENUITEM_COMMENTS_REPLY), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
		this.currentBlog = currentBlog;
    	this.controller=_controller;
		this.comment = comment;
		
        //row from
        BorderedFieldManager outerManagerRowFrom = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL | BorderedFieldManager.BOTTOM_BORDER_NONE);
        
		LabelField lblCommentAuthor = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_AUTHOR),
				Color.BLACK);	
		outerManagerRowFrom.add(lblCommentAuthor);
		outerManagerRowFrom.add(GUIFactory.createSepatorField());
        
        HorizontalFieldManager innerManagerRowFrom = new HorizontalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
        BitmapField gravatarBitmap = new BitmapField(GravatarController.defaultGravatarBitmap.getBitmap(), BitmapField.NON_FOCUSABLE | Manager.FIELD_VCENTER);
        VerticalFieldManager fromDataManager = new VerticalFieldManager(VerticalFieldManager.NO_VERTICAL_SCROLL | 
        		VerticalFieldManager.NO_HORIZONTAL_SCROLL | Manager.FIELD_VCENTER)
        {//add the focus change listener patch
        	public void add( Field field ) {
        		super.add( field );
        		field.setFocusListener(null);
        		field.setFocusListener(new BorderedFocusChangeListenerPatch()); 
        	}
        };
        innerManagerRowFrom.add(gravatarBitmap);
        innerManagerRowFrom.add(new LabelField("  ", LabelField.NON_FOCUSABLE));
        innerManagerRowFrom.add(fromDataManager);
        
        if(comment.getAuthor() != null && !comment.getAuthor().equals("")){
        	LabelField authorName = new LabelField(comment.getAuthor(), LabelField.FOCUSABLE);
			 fromDataManager.add(authorName);
		 }
		 		 
		 if(comment.getAuthorEmail() != null && !comment.getAuthorEmail().equals("")) {
			 LabelField authorEmail = new LabelField(comment.getAuthorEmail(), LabelField.FOCUSABLE);
			 fromDataManager.add(authorEmail);
			 gravatarBitmap.setImage( gvtCtrl.getLatestGravatar(comment.getAuthorEmail() ) );
		 }

		 if(comment.getAuthorUrl() != null && !comment.getAuthorUrl().equals("")) {
			 LabelField authorUrl = new LabelField(comment.getAuthorUrl(), LabelField.FOCUSABLE);
			 fromDataManager.add(authorUrl);
		 }

        outerManagerRowFrom.add(innerManagerRowFrom);
        add(outerManagerRowFrom);
		             	
    	 //original comment content
        BorderedFieldManager commentContentManager = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
		commentContentManager.add(GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_COMMENT),
        		Color.BLACK));
		commentContentManager.add(GUIFactory.createSepatorField());
		BasicEditField commentContent = new  BasicEditField(BasicEditField.READONLY);
		commentContent.setText(comment.getContent()); 
        commentContentManager.add(commentContent);
        add(commentContentManager);
    	
        //Comment Reply Area
        BorderedFieldManager commentReplyManager = new BorderedFieldManager(Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
   
        MarkupToolBarTextFieldMediator mediator = new MarkupToolBarTextFieldMediator();
  		
  		HorizontalFieldManager headerContent = new HorizontalFieldManager(Manager.NO_HORIZONTAL_SCROLL | Manager.USE_ALL_WIDTH);
  		LabelField lblPostContent = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_CONTENT), Color.BLACK, DrawStyle.ELLIPSIS);
  		int fntHeight = Font.getDefault().getHeight();
  		Font fnt = Font.getDefault().derive(Font.PLAIN, fntHeight-4, Ui.UNITS_px);
  		wordCountField = new LabelField("0", Field.USE_ALL_WIDTH | Field.FIELD_HCENTER | DrawStyle.RIGHT);
  		wordCountField.setFont(fnt);
  		mediator.setWcField(wordCountField);

  		headerContent.add(lblPostContent);
		headerContent.add(wordCountField);
		commentReplyManager.add(headerContent);
		commentReplyManager.add(GUIFactory.createSepatorField());
		
		replyContent = new HtmlTextField("", mediator);
		replyContent.setMargin(5,0,5,0);//leave some spaces on the top & bottom
		mediator.setTextField(replyContent);
		commentReplyManager.add(replyContent);
		commentReplyManager.add(GUIFactory.createSepatorField());
		
		MarkupToolBar markupToolBar = new MarkupToolBar(mediator);
		mediator.setTb(markupToolBar);
		markupToolBar.attachTo(commentReplyManager);
		add(commentReplyManager);
        add(new LabelField("", Field.NON_FOCUSABLE)); //space after content

        addMenuItem(_replyCommentItem);
        replyContent.setFocus(); //set the focus on the appropriate element
    }
    
	private MenuItem _replyCommentItem = new MenuItem( _resources, WordPressResource.MENUITEM_POST_SUBMIT, 80000, 1000) {
		 public void run() {
			 replyContent.setDirty(false);
			 //create the new comment as reply
			 Comment reply = new Comment();
			 reply.setContent(replyContent.getText());
			 reply.setParent(comment.getID());
			 reply.setPostID(comment.getPostID());
			 reply.setAuthor(currentBlog.getUsername());
			 controller.reply2Comment(reply);
		 }
	 };

	
	public boolean onClose()   {
		if(!replyContent.isDirty()) {
			controller.backCmd();
			return true; //if no change 
		}
		
		InquiryView infoView= new InquiryView(_resources.getString(WordPressResource.MESSAGE_COMMENT_LOST));
    	int choice=infoView.doModal();  
    	if (choice == Dialog.YES) {
    		controller.backCmd();
    		return true;
    	} else
    		return false;
    }

	public BaseController getController() {
		return this.controller;
	}
}
