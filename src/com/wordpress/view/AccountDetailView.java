package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AccountsController;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.io.AppDAO;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class AccountDetailView extends StandardBaseView {
	
    private AccountsController controller = null;
	private ColoredLabelField userNameField;
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
	        //wpClassicHeaderBitmapField.setMargin(2,0,2,0);
	        add(wpClassicHeaderBitmapField);

	        String user = (String)accountData.get("username");
	        String pass = (String)accountData.get("passwd");
	        
	        userNameField = new ColoredLabelField(_resources.getString(WordPressResource.LABEL_USERNAME)+": " + user, Color.BLACK);
	        userNameField.setMargin(0, 0, 0, 10);
	        add(userNameField);
	        
            BorderedFieldManager rowPassword = new BorderedFieldManager(
            		Manager.NO_HORIZONTAL_SCROLL
            		| Manager.NO_VERTICAL_SCROLL);
            rowPassword.add(
            		GUIFactory.getLabel(_resources.getString(WordPressResource.LABEL_PASSWD), Color.BLACK)
            );
            rowPassword.add(GUIFactory.createSepatorField());
            passwordField = new PasswordEditField("", pass, 64, Field.EDITABLE);
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
    	//	addMenuItem(_addBlogItem);
    		controller.bumpScreenViewStats("com/wordpress/view/AccountDetailView", "Account Detail Screen", "", null, "");
	}
	 		
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	saveAndBack();
	   }
	};

	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	dismissView();
	   }
	};

	//called when user click the OK button
	private void  saveAndBack(){
		try {
			if(isDirty()){
				String pass = passwordField.getText();
				accountData.put(AppDAO.PASSWORD_KEY, pass);
				AppDAO.storeAccounts(MainController.getIstance().getApplicationAccounts());
			} 
			controller.backCmd();
		} catch (Exception e) {
			controller.displayErrorAndWait("Error while saving Account Informations.");
		}
	}
	
	private boolean dismissView() {
		if(!this.isDirty()) {
			controller.backCmd();
			return true;
		}

		String quest=_resources.getString(WordPressResource.MESSAGE_INQUIRY_DIALOG_BOX);
		DiscardChangeInquiryView infoView= new DiscardChangeInquiryView(quest);
		int choice=infoView.doModal();    	 
		if(Dialog.DISCARD == choice) {
			Log.trace("user has selected discard");
			controller.backCmd();
			return true;
		}else if(Dialog.SAVE == choice) {
			Log.trace("user has selected save");
			saveAndBack();
			return true;
		} else {
			Log.trace("user has selected cancel");
			return false;
		}
	}

	
	public boolean onClose()   {
		controller.backCmd();
		return true;
	}
	
	public BaseController getController() {
		return controller;
	}
}