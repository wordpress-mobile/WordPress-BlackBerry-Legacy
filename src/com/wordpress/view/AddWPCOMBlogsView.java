//#preprocess
package com.wordpress.view;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.VirtualKeyboard;
//#endif
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AccountsController;
import com.wordpress.controller.AddBlogsController;
import com.wordpress.controller.BaseController;
import com.wordpress.utils.ImageUtils;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ClickableLabelField;
import com.wordpress.view.component.SelectorPopupScreen;
import com.wordpress.view.container.BorderedFieldManager;


public class AddWPCOMBlogsView extends StandardBaseView {
	
    private AddBlogsController controller = null;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
			
	public AddWPCOMBlogsView(AddBlogsController addBlogsController) {
	    	super(_resources.getString(WordPressResource.MENUITEM_ADDBLOG), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
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
	        wpClassicHeaderBitmapField.setMargin(5,0,3,0);
	        add(wpClassicHeaderBitmapField);

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
           
            if(AccountsController.getAccountsNumber() > 0 ) {
            	ClickableLabelField lblMyAccounts = new ClickableLabelField(_resources.getString(WordPressResource.LABEL_EXISTING_WPCOM_ACCOUNTS)+"...",
            			LabelField.FOCUSABLE | LabelField.ELLIPSIS);
            	lblMyAccounts.setMargin(2, 5, 5, 5);
            	FieldChangeListener existingAccountListener = new FieldChangeListener() {
            		public void fieldChanged(Field field, int context) {
            			if(context == 0) {
            		    	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
        			    	VirtualKeyboard virtKbd = getVirtualKeyboard();
        			    	if(virtKbd != null)
        			    		virtKbd.setVisibility(VirtualKeyboard.HIDE);
        			    	//#endif
        			    	
            				String[] accountsList = AccountsController.getAccountsName();
            				String title = _resources.getString(WordPressResource.TITLE_WPCOM_ACCOUNTS_SELECTOR_POPUP);
            				SelectorPopupScreen selScr = new SelectorPopupScreen(title, accountsList);
            				selScr.pickItem();
            				int selection = selScr.getSelectedItem();
            				if(selection != -1) {
            					String selectedUserName = accountsList[selection];
            					String passwd = AccountsController.getAccountPassword(selectedUserName);
            					userNameField.setFocus(); //XXX: trick to avoid issue with embossedbutton field 
            					controller.addWPCOMBlogs(selectedUserName, passwd);
            				}
            			}
            		}
            	};
            	lblMyAccounts.setChangeListener(existingAccountListener);
            	add(lblMyAccounts);
            }
            
            BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_SIGN_IN), ButtonField.CONSUME_CLICK);
            buttonOK.setChangeListener(listenerOkButton);
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		add(buttonsManager); 
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons
    		addMenuItem(_addBlogItem);
    		this.controller.bumpScreenViewStats("com/wordpress/view/AddWPCOMBlogsView", "AddWPCOMBlogs Screen", "", null, "");
	}
	 

	private void addBlog() {
		if (userNameField.getText().trim().length() == 0
				||  passwordField.getText().trim().length() == 0
		) {
			return;
		}
		//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		VirtualKeyboard virtKbd = getVirtualKeyboard();
		if(virtKbd != null)
			virtKbd.setVisibility(VirtualKeyboard.HIDE);
		//#endif
		controller.addWPCOMBlogs(userNameField.getText(), passwordField.getText()); 
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