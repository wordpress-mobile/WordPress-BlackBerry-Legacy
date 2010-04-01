package com.wordpress.view.dialog;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.DialogFieldManager;

import com.wordpress.bb.WordPressResource;


public class CredentialDialog extends Dialog {
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
	
    private EditField userNameField;
    private PasswordEditField passWordField;

    public CredentialDialog(){
        super(Dialog.D_OK_CANCEL, _resources.getString(WordPressResource.MESSAGE_AUTH_REQUIRED), Dialog.CANCEL, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
        userNameField = new EditField(_resources.getString(WordPressResource.LABEL_USERNAME)+ " ", "", 100, EditField.EDITABLE);
        passWordField = new PasswordEditField(_resources.getString(WordPressResource.LABEL_PASSWD)+ " ", "", 100, EditField.EDITABLE);
        
        net.rim.device.api.ui.Manager delegate = getDelegate();
        if( delegate instanceof DialogFieldManager){
            DialogFieldManager dfm = (DialogFieldManager)delegate;
            net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
            if( manager != null ){
                manager.insert(userNameField, 0);
                manager.insert(passWordField, 1);
            }
        }
    }    

    public String getUserName(){
      return userNameField.getText();
    }

    public String getPassWord(){
      return passWordField.getText();
    }
    
}