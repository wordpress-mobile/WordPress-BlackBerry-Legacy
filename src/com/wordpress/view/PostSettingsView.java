package com.wordpress.view;

import java.util.Date;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.model.Preferences;
import com.wordpress.utils.SimpleTimeZone;


public class PostSettingsView extends BaseView {
	
    private BlogObjectController controller; //controller associato alla view
    private DateField  authoredOn;
    private PasswordEditField passwordField;
    
    
    public PostSettingsView(BlogObjectController _controller, Date postAuth, String password) {
    	super(_resources.getString(WordPressResource.MENUITEM_SETTINGS));
    	this.controller=_controller;
		long datetime= (postAuth == null) ? new Date().getTime() : postAuth.getTime();
 
		//row date 
        HorizontalFieldManager rowDate = new HorizontalFieldManager();
		LabelField lblDate = getLabel(_resources.getString(WordPressResource.LABEL_POST_PUBLISHEDON));   
	    authoredOn= new DateField("", datetime, DateField.DATE_TIME);
	    authoredOn.setMargin(margins);
	    SimpleTimeZone timeZone = Preferences.getIstance().getTimeZone();
	    authoredOn.setTimeZone(timeZone); //setting the field time zone
		rowDate.add(lblDate);
		rowDate.add(authoredOn);
  		this.add(rowDate); 
		
        //row password
        HorizontalFieldManager rowPassword = new HorizontalFieldManager();
		LabelField lblPassword = getLabel(_resources.getString(WordPressResource.LABEL_BLOGPASSWD));		
        passwordField = new PasswordEditField("", password, 64, Field.EDITABLE);
        passwordField.setMargin(margins);
        rowPassword.add(lblPassword);
        rowPassword.add(passwordField);
        add(rowPassword);
        
        
		//LabelField that displays text in the specified color.
		LabelField lblDesc = getLabel(_resources.getString(WordPressResource.DESCRIPTION_POST_PASSWORD)); 
		Font fnt = this.getFont().derive(Font.ITALIC);
		lblDesc.setFont(fnt);
		add(lblDesc);
    }
    
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
    /*    SimpleDateFormat sdFormat3 = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        String format = sdFormat3.format(new Date(authoredOn.getDate()));
        System.out.println("la data selezionata formatta è "+ format);
		System.out.println("la data selezionata è "+ authoredOn.getDate()); */
		controller.setSettingsValues(authoredOn.getDate(), passwordField.getText());
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
}