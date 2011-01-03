//#preprocess
package com.wordpress.view;

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

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.SignUpBlogController;
import com.wordpress.utils.ImageUtils;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.container.BorderedFieldManager;


public class SignUpBlogView extends StandardBaseView {
	
    private SignUpBlogController controller = null;
    
    private BorderedFieldManager rowBlogName;
    private BasicEditField blogNameField;
    private ColoredLabelField blogNameErrorField;
    
    private BorderedFieldManager rowUserName;
    private BasicEditField userNameField;
    private ColoredLabelField userNameErrorField;
    
    private BorderedFieldManager rowEmail;
    private BasicEditField emailField;
    private ColoredLabelField emailErrorField;
	
    private BorderedFieldManager rowPassword;
    private PasswordEditField passwordField;
    private ColoredLabelField passwordErrorField;
			
	public SignUpBlogView(SignUpBlogController addBlogsController) {
	    	super( Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
//_resources.getString(WordPressResource.TITLE_VIEW_SIGNUP),
	    	this.controller=addBlogsController;
	    	
			//Set the preferred width to the image size or screen width if the image is larger than the screen width.
	    	EncodedImage classicHeaderImg = EncodedImage.getEncodedImageResource("logo-wpcom-login.png");
			int _preferredWidth = -1;
	        if (classicHeaderImg.getWidth() > Display.getWidth()) {
	            _preferredWidth = Display.getWidth();
	        }
	        if( _preferredWidth != -1) {        	
	        	EncodedImage resImg = ImageUtils.resizeEncodedImage(classicHeaderImg, _preferredWidth, classicHeaderImg.getHeight());
	        	classicHeaderImg = resImg;
	        }
	        BitmapField wpClassicHeaderBitmapField =  new BitmapField(classicHeaderImg.getBitmap(), Field.FIELD_HCENTER | Field.FIELD_VCENTER);
	        add(wpClassicHeaderBitmapField);
	        
	        //intro text
	        BasicEditField introTextField = new BasicEditField(BasicEditField.READONLY);
	        introTextField.setText(_resources.getString(WordPressResource.MESSAGE_SIGNUP_BLOG));
	        introTextField.setMargin(10, 10, 2, 10);
	    	add(introTextField);
	        
            rowBlogName = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL 
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            
            rowBlogName.add(GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_BLOG_ADDRESS), Color.BLACK));
            rowBlogName.add(GUIFactory.createSepatorField());
            blogNameField = new BasicEditField("", "yourblog.wordpress.com", 60, Field.EDITABLE);
            rowBlogName.add(blogNameField);
            add(rowBlogName);
            
            rowUserName = new BorderedFieldManager(
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
            
            rowEmail = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL 
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            rowEmail.add(
      				 GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_EMAIL), Color.BLACK)
      				 );
            rowEmail.add(GUIFactory.createSepatorField());
            emailField = new BasicEditField("", "", 100, BasicEditField.FILTER_EMAIL);
            rowEmail.add(emailField);
            add(rowEmail);
            
            rowPassword = new BorderedFieldManager(
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
                       
            BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_SIGN_UP), ButtonField.CONSUME_CLICK);
            buttonOK.setChangeListener(listenerOkButton);
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		add(buttonsManager); 
    		
    		//TOS disclaimer
    		BasicEditField mainTextField = new BasicEditField(BasicEditField.READONLY);
        	mainTextField.setText("You agree to the fascinating terms of service at http://wordpress.com/tos by submitting this form.");
        	mainTextField.setMargin(10, 10, 1, 10);
        	add(mainTextField);
        	LabelField urlAddr = GUIFactory.createURLLabelField("More info", "http://wordpress.com/tos", LabelField.FOCUSABLE);
        	urlAddr.setMargin(0, 10, 2, 10);
        	add(urlAddr);
        	add(new LabelField("", Field.NON_FOCUSABLE));
        	
    		addMenuItem(_addBlogItem);
    		
    		//preparing the error fields
    		blogNameErrorField = new ColoredLabelField("", Color.RED);
    		blogNameErrorField.setMargin(2, 5, 5, 5);
    		userNameErrorField = new ColoredLabelField("", Color.RED);
    		userNameErrorField.setMargin(2, 5, 5, 5);
    		emailErrorField = new ColoredLabelField("", Color.RED);
    		emailErrorField.setMargin(2, 5, 5, 5);
    		passwordErrorField = new ColoredLabelField("", Color.RED);
    		passwordErrorField.setMargin(2, 5, 5, 5);
	}
	 

	private void signUp() {
		if (userNameField.getText().trim().length() == 0
				||  passwordField.getText().trim().length() == 0
				||  blogNameField.getText().trim().length() == 0
				||  emailField.getText().trim().length() == 0
		) {
			return;
		}
		//#ifdef IS_OS47_OR_ABOVE
		VirtualKeyboard virtKbd = getVirtualKeyboard();
		if(virtKbd != null)
			virtKbd.setVisibility(VirtualKeyboard.HIDE);
		//#endif
		controller.signup(blogNameField.getText(),userNameField.getText(), emailField.getText(), passwordField.getText()); 
	}
	
	private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.BUTTON_SIGN_UP, 140, 10) {
		public void run() {
			signUp();
		}
	};
	
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	signUp();
	    }
	};
	
	// Utility routine to by-pass the standard dialog box when the screen is closed  
	public boolean onClose(){
		controller.backCmd();
		return true;
	}
	
	public BaseController getController() {
		return controller;
	}
}