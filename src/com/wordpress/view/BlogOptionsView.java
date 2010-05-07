package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogOptionsController;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.container.BorderedFieldManager;

public class BlogOptionsView extends StandardBaseView {
	
    private BlogOptionsController controller= null;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private ObjectChoiceField  maxRecentPost;
	private BorderedFieldManager rowResizePhotos;
	private CheckboxField resizePhoto;
	private BasicEditField imageResizeWidthField;
	private BasicEditField imageResizeHeightField;
	private CheckboxField commentNotifications;
	private CheckboxField enableLocation;
	private Integer imageResizeWidth;
	private Integer imageResizeHeight;
	private CheckboxField enableSignature;
	private BasicEditField signatureField;
	
	HorizontalFieldManager buttonsManager;
	
	public boolean isResizePhoto(){
		return resizePhoto.getChecked();
	}
	
	public Integer getImageResizeWidth() {
		if(imageResizeWidthField != null) {
			return Integer.valueOf(imageResizeWidthField.getText());
		}
		else {
			return new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH);
		}
	}
	
	public Integer getImageResizeHeight() {
		if(imageResizeHeightField != null) {
			return Integer.valueOf(imageResizeHeightField.getText());
		}
		else {
			return new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT);
		}
	}

	//fields for signature
	public boolean isSignatureCheckboxDirty(){
		return enableSignature.isDirty();
	}
	public boolean isSignatureEditFieldDirty(){
		return signatureField.isDirty();
	}
	public boolean  isSignatureEnabled(){
		return enableSignature.getChecked();
	}
	public String  getSignature(){
		return signatureField.getText();
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
			imageResizeWidth = (Integer)values.get("imageResizeWidth");
			imageResizeHeight = (Integer)values.get("imageResizeHeight");
			boolean isLocation = ((Boolean)values.get("islocation")).booleanValue();
			boolean isCommentNotifications = ((Boolean)values.get("iscommentnotifications")).booleanValue();
			boolean isSignatureActive = ((Boolean)values.get("isSignatureActive")).booleanValue();
			String signature = (String)values.get("signature");
	        //end loading
			
            //row username
            BorderedFieldManager rowUserName = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		LabelField lblUserName = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_USERNAME), Color.BLACK); 
            userNameField = new BasicEditField("", user, 60, Field.EDITABLE);
            rowUserName.add(lblUserName);
    		rowUserName.add(userNameField);
            add(rowUserName);
    		
            //row password
            BorderedFieldManager rowPassword = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		LabelField lblPassword = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_PASSWD), Color.BLACK); 
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
            rowResizePhotos = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
    		resizePhoto.setChangeListener(listenerResizePhotoCheckbox);
    		rowResizePhotos.add(resizePhoto);
     		BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS));
			rowResizePhotos.add(lblDesc);
			
			if(isResImg) {
				addImageResizeWidthField();
				addImageResizeHeightField();
			}
            
			add(rowResizePhotos);

            //row comment notifies and location
            BorderedFieldManager commentNotificationManager = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		enableLocation = new CheckboxField(_resources.getString(WordPressResource.LABEL_LOCATION), isLocation);
    		commentNotifications = new CheckboxField(_resources.getString(WordPressResource.LABEL_COMMENT_NOTIFICATIONS), isCommentNotifications);
    		commentNotificationManager.add(commentNotifications);
    		commentNotificationManager.add(enableLocation);
			add(commentNotificationManager);
			
			
            //row Signature
            BorderedFieldManager signatureManager = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL);
    		enableSignature = new CheckboxField(_resources.getString(WordPressResource.DESCRIPTION_ADD_SIGNATURE), isSignatureActive);
    		signatureManager.add(enableSignature);
    		signatureField = new BasicEditField(_resources.getString(WordPressResource.LABEL_SIGNATURE)+": ", signature, 1000, Field.EDITABLE);
    		signatureField.setMargin(5, 0, 5, 0);
    		signatureManager.add(signatureField);
			add(signatureManager);
			
            BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            BaseButtonField buttonBACK= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
            buttonBACK.setChangeListener(blogsController.getBackButtonListener());
            buttonOK.setChangeListener(blogsController.getOkButtonListener());
            
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		add(buttonsManager);
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons
	}

	public boolean onClose()   {
		return controller.dismissView();			
	}
	
	public BaseController getController() {
		return controller;
	}
	
	// Enable or disable image resize width/height fields when the "resize image" checkbox changes.
	private FieldChangeListener listenerResizePhotoCheckbox = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(resizePhoto.getChecked() == true) {
	    		addImageResizeWidthField();
	    		addImageResizeHeightField();
	    	}
	    	else {
	    	       	rowResizePhotos.delete(imageResizeWidthField);
	    	       	imageResizeWidthField = null;
	    	       	rowResizePhotos.delete(imageResizeHeightField);
	    	       	imageResizeHeightField = null;
	    	}
	   }
	};
	
	// Recalculate the image resize height whenever the image resize width changes. Aspect ratio is fixed.
	private FocusChangeListener listenerImageResizeWidthField = new FocusChangeListener() {
	    public void focusChanged(Field field, int eventType) {
	    	if((eventType == FocusChangeListener.FOCUS_LOST) && (imageResizeWidthField.isDirty())) {
		    	try {
		    		int newWidth = Integer.parseInt(imageResizeWidthField.getText());
		    		if(newWidth == 0) {
		    			Dialog.alert(_resources.getString(WordPressResource.ERROR_RESIZE_WIDTH));
		    			imageResizeWidthField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_WIDTH));
		    			imageResizeHeightField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		    		}
		    		else {
		    			int newHeight = (int)(newWidth * 0.75);
		    			imageResizeHeightField.setText(Integer.toString(newHeight));
		    		}
		    	}
		    	catch(NumberFormatException e) {
		    		Log.error("Unexpected condition: ImageResizeWidthField was not numeric in BlogOptionsView.");
		    	}
	    	}
	   }
	};

	// Recalculate the image resize width whenever the image resize height changes. Aspect ratio is fixed.
	private FocusChangeListener listenerImageResizeHeightField = new FocusChangeListener() {
	    public void focusChanged(Field field, int eventType) {
	    	if((eventType == FocusChangeListener.FOCUS_LOST) && (imageResizeHeightField.isDirty())) {
		    	try {
		    		int newHeight = Integer.parseInt(imageResizeHeightField.getText());
		    		if(newHeight == 0) {
		    			Dialog.alert(_resources.getString(WordPressResource.ERROR_RESIZE_HEIGHT));
		    			imageResizeWidthField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_WIDTH));
		    			imageResizeHeightField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		    		}
		    		else {
		    			int newWidth = (int)((newHeight * 1.3333) + 1);
		    			imageResizeWidthField.setText(Integer.toString(newWidth));
		    		}
		    	}
		    	catch(NumberFormatException e) {
		    		Log.error("Unexpected condition: ImageResizeHeightField was not numeric in BlogOptionsView.");
		    	}
	    	}
	   }
	};
	
	private void addImageResizeWidthField() {
        imageResizeWidthField = new BasicEditField(
        		_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_WIDTH)+": ", 
        		(imageResizeWidth == null ? "" : imageResizeWidth.toString()), 
        		4, 
        		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
        
        imageResizeWidthField.setFocusListener(listenerImageResizeWidthField);
       	rowResizePhotos.add(imageResizeWidthField);
	}

	private void addImageResizeHeightField() {
	    imageResizeHeightField = new BasicEditField(
	    		_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_HEIGHT)+": ", 
	    		(imageResizeHeight == null ? "" : imageResizeHeight.toString()), 
	    		4, 
	    		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
	    
	    imageResizeHeightField.setFocusListener(listenerImageResizeHeightField);
    	rowResizePhotos.add(imageResizeHeightField);
	}

}