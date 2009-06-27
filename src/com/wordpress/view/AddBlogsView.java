package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AddBlogsController;
import com.wordpress.controller.BaseController;
import com.wordpress.view.component.BorderedFieldManager;

public class AddBlogsView extends BaseView {
	
    private AddBlogsController controller= null;
	private BasicEditField blogUrlField;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private ObjectChoiceField  maxRecentPost;
	private CheckboxField resizePhoto;
	
	public boolean isResizePhoto(){
		return resizePhoto.getChecked();
	}
	
	public String getBlogUrl() {
		return blogUrlField.getText();
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
	    	super(_resources.getString(WordPressResource.TITLE_ADDBLOGS));
	    	this.controller=addBlogsController;
	        
	        //loading input data
	        String user= (String)values.get("user");
	        String pass= (String)values.get("pass");
	        String url= (String)values.get("url");
	        String[] recentPost=(String[])values.get("recentpost");
	        int recentPostSelect= ((Integer)values.get("recentpostselected")).intValue();
			boolean isResImg= ((Boolean)values.get("isresphotos")).booleanValue();
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
            BorderedFieldManager rowResizePhotos = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
    		rowResizePhotos.add(resizePhoto);
			//LabelField that displays text in the specified color.
			LabelField lblDesc = getLabel(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS)); 
			Font fnt = this.getFont().derive(Font.ITALIC);
			lblDesc.setFont(fnt);
			rowResizePhotos.add(lblDesc);
			add(rowResizePhotos);
			
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
    		buttonBACK.setChangeListener(addBlogsController.getBackButtonListener());
            buttonOK.setChangeListener(addBlogsController.getOkButtonListener());
            
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		this.add(new LabelField("", Field.NON_FOCUSABLE)); //space before buttons
    		add(buttonsManager); 
    		
    		addMenuItem(_addBlogItem);
	}
	 
	//add blog menu item 
	private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 140, 10) {
		public void run() {
			controller.addBlogs();
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