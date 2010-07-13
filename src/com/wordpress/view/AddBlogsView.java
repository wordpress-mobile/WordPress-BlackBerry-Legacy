package com.wordpress.view;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AddBlogsController;
import com.wordpress.controller.BaseController;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.Tools;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.container.BorderedFieldManager;


public class AddBlogsView extends StandardBaseView {
	
    private AddBlogsController controller= null;
	private BasicEditField blogUrlField;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
			
	public AddBlogsView(AddBlogsController addBlogsController) {
	    	super(Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=addBlogsController;
	        
			//Set the preferred width to the image size or screen width if the image is larger than the screen width.
	    	EncodedImage classicHeaderImg = EncodedImage.getEncodedImageResource("logo-wporg-login.png");
			int _preferredWidth = -1;
	        if (classicHeaderImg.getWidth() > Display.getWidth()) {
	            _preferredWidth = Display.getWidth();
	        }
	        if( _preferredWidth != -1) {        	
	        	EncodedImage resImg = ImageUtils.resizeEncodedImage(classicHeaderImg, _preferredWidth, classicHeaderImg.getHeight());
	        	classicHeaderImg = resImg;
	        }
	        BitmapField wpClassicHeaderBitmapField =  new BitmapField(classicHeaderImg.getBitmap(), Field.FIELD_HCENTER | Field.FIELD_VCENTER);
	        wpClassicHeaderBitmapField.setMargin(5, 0, 0, 0);
	        add(wpClassicHeaderBitmapField);
	        
            //row url
			BorderedFieldManager rowURL = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
           	LabelField lblUrl = GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_URL), Color.BLACK); 
           	rowURL.add(lblUrl);
           	rowURL.add(GUIFactory.createSepatorField());
           	blogUrlField = new BasicEditField("", "http://", 100, Field.EDITABLE);
            blogUrlField.setFilter(new URLTextFilter());
            if(blogUrlField.getTextLength() > 0)
            	blogUrlField.setCursorPosition(blogUrlField.getTextLength());//set the cursor at the end
            rowURL.add(blogUrlField);
            add(rowURL);

            BorderedFieldManager rowUserName = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL 
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            rowUserName.add(
      				 GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_USERNAME), Color.BLACK)
      				 );
            rowUserName.add(GUIFactory.createSepatorField());
            userNameField = new BasicEditField("", "", 60, Field.EDITABLE);
            rowUserName.add(userNameField);
            add(rowUserName);

            BorderedFieldManager rowPassword = new BorderedFieldManager(
            		Manager.NO_HORIZONTAL_SCROLL
            		| Manager.NO_VERTICAL_SCROLL 
            );
            rowPassword.add(
            		GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_PASSWD), Color.BLACK)
            );
            rowPassword.add(GUIFactory.createSepatorField());
            passwordField = new PasswordEditField("", "", 64, Field.EDITABLE);
            rowPassword.add(passwordField);
            add(rowPassword);
            
            BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            BaseButtonField buttonBACK= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
    		buttonBACK.setChangeListener(listenerBackButton);
            buttonOK.setChangeListener(listenerOkButton);
            
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
            buttonsManager.add(buttonBACK);
    		add(buttonsManager); 
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons

    		addMenuItem(_addBlogItem);
    		addMenuItem(_getFreeBlogItem);
	}
	 	
	//add blog menu item 
	private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 140, 10) {
		public void run() {
			controller.addBlogs(0, blogUrlField.getText(), userNameField.getText(), passwordField.getText());
		}
	};
	
	//add blog menu item 
	private MenuItem _getFreeBlogItem = new MenuItem( _resources, WordPressResource.GET_FREE_BLOG_MENU_ITEM, 150, 20) {
		public void run() {
			Tools.openURL(WordPressInfo.BB_APP_SIGNUP_URL);
		}
	};
	
	
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	controller.addBlogs(0, blogUrlField.getText(), userNameField.getText(), passwordField.getText()); 
	   }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	controller.backCmd();
	   }
	};

	public boolean onClose()   {
		return controller.discardChange();			
	}
	
	public BaseController getController() {
		return controller;
	}
}