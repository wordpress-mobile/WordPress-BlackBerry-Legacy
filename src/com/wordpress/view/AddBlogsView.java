package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AddBlogsController;
import com.wordpress.controller.BaseController;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;

public class AddBlogsView extends StandardBaseView {
	
    private AddBlogsController controller= null;
	private BasicEditField blogUrlField;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private ObjectChoiceField  maxRecentPost;
	private BorderedFieldManager rowResizePhotos;
	private CheckboxField resizePhoto;
	private HorizontalFieldManager rowImageResizeWidth;
	private HorizontalFieldManager rowImageResizeHeight;
	private BasicEditField imageResizeWidthField;
	private BasicEditField imageResizeHeightField;
	private Integer imageResizeWidth;
	private Integer imageResizeHeight;
		
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
	
	public String getBlogUrl() {
		return blogUrlField.getText();
	}
	
	public void setBlogUrl(String newUrl) {
		 blogUrlField.setText(newUrl);
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
	

	
	public AddBlogsView(AddBlogsController addBlogsController, Hashtable values) {
	    	super(_resources.getString(WordPressResource.TITLE_ADDBLOGS), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=addBlogsController;
	        
	        //loading input data
	        String user= (String)values.get("user");
	        String pass= (String)values.get("pass");
	        String url= (String)values.get("url");
	        String[] recentPost=(String[])values.get("recentpost");
	        int recentPostSelect= ((Integer)values.get("recentpostselected")).intValue();
			boolean isResImg= ((Boolean)values.get("isresphotos")).booleanValue();
			imageResizeWidth = (Integer)values.get("imageResizeWidth");
			imageResizeHeight = (Integer)values.get("imageResizeHeight");

	        //end loading
            //row url
			BorderedFieldManager rowURL = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            //HorizontalFieldManager rowURL = new HorizontalFieldManager();
    		LabelField lblUrl = getLabel(_resources.getString(WordPressResource.LABEL_URL)); 
            blogUrlField = new BasicEditField("", url, 100, Field.EDITABLE);
            blogUrlField.setFilter(new URLTextFilter());
            if(blogUrlField.getTextLength() > 0)
            	blogUrlField.setCursorPosition(blogUrlField.getTextLength());//set the cursor at the end
            rowURL.add(lblUrl);
            rowURL.add(blogUrlField);
            add(rowURL);
            
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
            rowResizePhotos = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL);
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
            
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
    		buttonBACK.setChangeListener(addBlogsController.getBackButtonListener());
            buttonOK.setChangeListener(addBlogsController.getOkButtonListener());
            
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
            buttonsManager.add(buttonBACK);
    		add(buttonsManager); 
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons
    		
            HorizontalFieldManager buttonsManagerGetFreeBlog = new HorizontalFieldManager(Field.FIELD_HCENTER);
            ButtonField buttonGetFreeBlog= new ButtonField(_resources.getString(WordPressResource.GET_FREE_BLOG), ButtonField.CONSUME_CLICK);
            buttonGetFreeBlog.setChangeListener(listenerGetBlogButton);
            buttonsManagerGetFreeBlog.add(buttonGetFreeBlog);
    		add(buttonsManagerGetFreeBlog); 
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after button
    		
    		addMenuItem(_addBlogItem);
    		addMenuItem(_getFreeBlogItem);
	}
	 
	
	private FieldChangeListener listenerGetBlogButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	openWordPressSignUpURL();
	   }
	};
	
	// Enable or disable image resize width/height fields when the "resize image" checkbox changes.
	private FieldChangeListener listenerResizePhotoCheckbox = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(resizePhoto.getChecked() == true) {
	    		addImageResizeWidthField();
	    		addImageResizeHeightField();
	    	}
	    	else {
	    	       	rowResizePhotos.delete(rowImageResizeWidth);
	    	       	rowImageResizeWidth = null;
	    	       	rowResizePhotos.delete(rowImageResizeHeight);
	    	       	rowImageResizeHeight = null;
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
        rowImageResizeWidth = new HorizontalFieldManager();
        rowImageResizeWidth.add( getLabel(_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_WIDTH)));      
        imageResizeWidthField = new BasicEditField(
        		"", 
        		(imageResizeWidth == null ? "" : imageResizeWidth.toString()), 
        		4, 
        		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
        
        imageResizeWidthField.setFocusListener(listenerImageResizeWidthField);
        rowImageResizeWidth.add(imageResizeWidthField);
       	rowResizePhotos.add(rowImageResizeWidth);
	}

	private void addImageResizeHeightField() {
	    rowImageResizeHeight = new HorizontalFieldManager();
	    rowImageResizeHeight.add( getLabel(_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_HEIGHT)));
	    imageResizeHeightField = new BasicEditField(
	    		"", 
	    		(imageResizeHeight == null ? "" : imageResizeHeight.toString()), 
	    		4, 
	    		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
	    
	    imageResizeHeightField.setFocusListener(listenerImageResizeHeightField);
	    rowImageResizeHeight.add(imageResizeHeightField);
    	rowResizePhotos.add(rowImageResizeHeight);
	}
	
	//add blog menu item 
	private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 140, 10) {
		public void run() {
			controller.addBlogs(0);
		}
	};
	
	private void openWordPressSignUpURL(){
		HttpHeaders headers = new HttpHeaders();
    	headers.addProperty("User-Agent", "wp-blackberry/"+ Tools.getAppVersion());
    	Tools.getBrowserSession("http://wordpress.com/signup/?ref=wp-blackberry","/wp-blackberry/AddBlogScreen", headers, null);
	}
	
	//add blog menu item 
	private MenuItem _getFreeBlogItem = new MenuItem( _resources, WordPressResource.GET_FREE_BLOG_MENU_ITEM, 150, 20) {
		public void run() {
			openWordPressSignUpURL();
		}
	};

	   //override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		return controller.discardChange();			
	}
	
	public BaseController getController() {
		return controller;
	}
}