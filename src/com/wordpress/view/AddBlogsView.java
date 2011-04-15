//#preprocess
package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.VirtualKeyboard;
//#endif
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AddBlogsController;
import com.wordpress.controller.BaseController;
import com.wordpress.utils.ImageUtils;
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
	    	EncodedImage classicHeaderImg = EncodedImage.getEncodedImageResource("wp_grey-s.png");
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
            
            BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_SIGN_IN), ButtonField.CONSUME_CLICK);
            buttonOK.setChangeListener(listenerOkButton);
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		add(buttonsManager); 
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons

    		addMenuItem(_addBlogItem);
	}

	private void addBlog() {
		if (userNameField.getText().trim().length() == 0
				||  passwordField.getText().trim().length() == 0
				||  blogUrlField.getText().trim().length() == 0
		) {
			return;
		}
		//#ifdef IS_OS47_OR_ABOVE
		VirtualKeyboard virtKbd = getVirtualKeyboard();
		if(virtKbd != null)
			virtKbd.setVisibility(VirtualKeyboard.HIDE);
		//#endif
		controller.addBlogs(0, blogUrlField.getText(), userNameField.getText(), passwordField.getText()); 
	}
	
	private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.BUTTON_SIGN_IN, 140, 10) {
		public void run() {
			addBlog();
		}
	};
	
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	addBlog();
	   }
	};

	public boolean onClose()   {
		return controller.discardChange();			
	}
	
	public BaseController getController() {
		return controller;
	}
}