package com.wordpress.view;

import java.util.Hashtable;

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

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AccountsController;
import com.wordpress.controller.BaseController;
import com.wordpress.utils.ImageUtils;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.container.BorderedFieldManager;


public class AccountDetailView extends StandardBaseView {
	
    private AccountsController controller = null;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private final Hashtable accountData;
			
	public AccountDetailView(AccountsController addBlogsController, Hashtable accountData) {
	    	super(_resources.getString(WordPressResource.TITLE_VIEW_ACCOUNT), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=addBlogsController;
			this.accountData = accountData;
            
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

	        
	        String user = (String)accountData.get("username");
	        String pass = (String)accountData.get("passwd");
	        BorderedFieldManager credentialOptionsRow = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);

	        credentialOptionsRow.add(
	        		GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_CREDENTIALS), Color.BLACK)
	        );
	        credentialOptionsRow.add(GUIFactory.createSepatorField());
	        userNameField = new BasicEditField(_resources.getString(WordPressResource.LABEL_USERNAME)+": ", user, 60, Field.EDITABLE);
	        userNameField.setMargin(5, 0, 5, 0);
	        credentialOptionsRow.add(userNameField);
	        passwordField = new PasswordEditField(_resources.getString(WordPressResource.LABEL_PASSWD)+": ", pass, 64, Field.EDITABLE);
	        passwordField.setMargin(5, 0, 5, 0);
	        credentialOptionsRow.add(passwordField);
	        add(credentialOptionsRow); 
            
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
	}
	 		
	private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 140, 10) {
		public void run() {
			
		}
	};
	
	

	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	   }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	controller.backCmd();
	   }
	};

	
	public boolean onClose()   {
		controller.backCmd();
		return true;
	}
	
	public BaseController getController() {
		return controller;
	}
}