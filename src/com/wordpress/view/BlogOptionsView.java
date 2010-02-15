package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogOptionsController;
import com.wordpress.view.component.BorderedFieldManager;

public class BlogOptionsView extends StandardBaseView {
	
    private BlogOptionsController controller= null;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private ObjectChoiceField  maxRecentPost;
	private CheckboxField resizePhoto;
	private CheckboxField commentNotifications;
	private CheckboxField enableLocation;
	
	HorizontalFieldManager buttonsManager;
	
	public boolean isResizePhoto(){
		return resizePhoto.getChecked();
	}
	
	public boolean isLocation(){
		return enableLocation.getChecked();
	}
	
	public boolean isCommentNotifications(){
		return commentNotifications.getChecked();
	}
	
	public String getBlogUser() {
		return userNameField.getText();
	}
	
	public String getBlogPass() {
		return passwordField.getText();
	}
	
	public int getMaxRecentPostIndex() {
		return maxRecentPost.getSelectedIndex();
	}
	
	 public BlogOptionsView(BlogOptionsController blogsController, Hashtable values) {
	    	super(_resources.getString(WordPressResource.TITLE_BLOG_OPTION_VIEW)+" > "+ blogsController.getBlogName(), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=blogsController;
	    	
	        //loading input data
	        String user = (String)values.get("user");
	        String pass= (String)values.get("pass");
	        String[] recentPost=(String[])values.get("recentpost");
	        int recentPostSelect = ((Integer)values.get("recentpostselected")).intValue();
			boolean isResImg = ((Boolean)values.get("isresphotos")).booleanValue();
			boolean isLocation = ((Boolean)values.get("islocation")).booleanValue();
			boolean isCommentNotifications = ((Boolean)values.get("iscommentnotifications")).booleanValue();
	        //end loading
			
            //row username
            BorderedFieldManager rowUserName = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		LabelField lblUserName = getLabel(_resources.getString(WordPressResource.LABEL_USERNAME)); 
            userNameField = new BasicEditField("", user, 60, Field.EDITABLE);
            rowUserName.add(lblUserName);
    		rowUserName.add(userNameField);
            add(rowUserName);
    		
            //row password
            BorderedFieldManager rowPassword = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		LabelField lblPassword = getLabel(_resources.getString(WordPressResource.LABEL_PASSWD)); 
            passwordField = new PasswordEditField("", pass, 64, Field.EDITABLE);
            rowPassword.add(lblPassword);
            rowPassword.add(passwordField);
            add(rowPassword);

            //row max recent post
            BorderedFieldManager rowMaxRecentPost = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            maxRecentPost = new ObjectChoiceField (_resources.getString(WordPressResource.LABEL_MAXRECENTPOST), recentPost,recentPostSelect);
            rowMaxRecentPost.add(maxRecentPost);
            add(rowMaxRecentPost);            

            //row resize photos
            BorderedFieldManager rowResizePhotos = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL);
    		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
    		rowResizePhotos.add(resizePhoto);
     		BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS));
			rowResizePhotos.add(lblDesc);
			add(rowResizePhotos);
			
            //row comment notifies and location
            BorderedFieldManager commentNotificationManager = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL);
    		enableLocation = new CheckboxField(_resources.getString(WordPressResource.LABEL_LOCATION), isLocation);
    		commentNotifications = new CheckboxField(_resources.getString(WordPressResource.LABEL_COMMENT_NOTIFICATIONS), isCommentNotifications);
    		commentNotificationManager.add(commentNotifications);
    		commentNotificationManager.add(enableLocation);
			add(commentNotificationManager);
			
			
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
    		buttonBACK.setChangeListener(blogsController.getBackButtonListener());
            buttonOK.setChangeListener(blogsController.getOkButtonListener());
            
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		add(buttonsManager);
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons
	}

	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		return controller.dismissView();			
	}
	
	public BaseController getController() {
		return controller;
	}
}