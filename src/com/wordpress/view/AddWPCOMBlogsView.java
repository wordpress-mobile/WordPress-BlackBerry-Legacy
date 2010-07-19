package com.wordpress.view;

import java.util.Enumeration;
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

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AddBlogsController;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.io.AccountsDAO;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.Tools;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ClickableLabelField;
import com.wordpress.view.component.SelectorPopupScreen;
import com.wordpress.view.container.BorderedFieldManager;


public class AddWPCOMBlogsView extends StandardBaseView {
	
    private AddBlogsController controller = null;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
			
	public AddWPCOMBlogsView(AddBlogsController addBlogsController) {
	    	super(Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);

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
     		
            final Hashtable accounts = MainController.getIstance().getApplicationAccounts();
            if(accounts.size() > 0 ) {
            	ClickableLabelField lblMyAccounts = new ClickableLabelField(_resources.getString(WordPressResource.LABEL_EXISTING_WPCOM_ACCOUNTS),
            			LabelField.FOCUSABLE | LabelField.ELLIPSIS);
            	lblMyAccounts.setTextColor(Color.BLUE);
            	lblMyAccounts.setMargin(2, 5, 5, 5);
            	FieldChangeListener existingAccountListener = new FieldChangeListener() {
            		public void fieldChanged(Field field, int context) {
            			if(context == 0) {
            				Enumeration k = accounts.keys();
            				String[] accountsList = new String[accounts.size()];
            				int i = 0;
            				while (k.hasMoreElements()) {
            					String key = (String) k.nextElement();
            					accountsList[i] = key;
            					i++;
            				}
            				String title = _resources.getString(WordPressResource.TITLE_WPCOM_ACCOUNTS_SELECTOR_POPUP);
            				SelectorPopupScreen selScr = new SelectorPopupScreen(title, accountsList);
            				selScr.pickItem();
            				int selection = selScr.getSelectedItem();
            				if(selection != -1) {
            					String selectedUserName = accountsList[selection];
            					Hashtable selectedAccount = (Hashtable) accounts.get(selectedUserName); 
            					String passwd = (String) selectedAccount.get(AccountsDAO.PASSWORD_KEY);
            					userNameField.setFocus(); //trick to avoid issue with clickableLabel focus 
            					controller.addWPCOMBlogs(selectedUserName, passwd);
            				}
            			}
            		}
            	};
            	lblMyAccounts.setChangeListener(existingAccountListener);
            	add(lblMyAccounts);
            }
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
	 		
	private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 140, 10) {
		public void run() {
			controller.addWPCOMBlogs(userNameField.getText(), passwordField.getText());
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
	    	controller.addWPCOMBlogs(userNameField.getText(), passwordField.getText()); 
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