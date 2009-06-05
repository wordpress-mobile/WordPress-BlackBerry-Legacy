package com.wordpress.view;

import java.util.Date;
import java.util.TimeZone;

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.utils.CalendarUtils;


public class PostSettingsView extends BaseView {
	
    private BlogObjectController controller; //controller associato alla view
    private DateField  authoredOn;
    private PasswordEditField passwordField;
	private CheckboxField resizePhoto;
    
    
    public PostSettingsView(BlogObjectController _controller, Date postAuth, String password, boolean isResImg) {
    	super(_resources.getString(WordPressResource.MENUITEM_SETTINGS));
    	this.controller=_controller;
    	
    	long datetime = new Date().getTime();;
    	if(postAuth != null ) {
    		datetime = CalendarUtils.adjustTimeToDefaultTimezone(postAuth.getTime());
    	}
			
		//row date 
        HorizontalFieldManager rowDate = new HorizontalFieldManager();
		LabelField lblDate = getLabel(_resources.getString(WordPressResource.LABEL_POST_PUBLISHEDON));   
	    authoredOn= new DateField("", datetime, DateField.DATE_TIME);
	    authoredOn.setMargin(margins);
	    authoredOn.setTimeZone(TimeZone.getDefault()); //setting the field time zone
		SimpleDateFormat sdFormat = new SimpleDateFormat(_resources.getString(WordPressResource.DEFAULT_DATE_FORMAT), Locale.getDefault());
	    authoredOn.setFormat(sdFormat);
		rowDate.add(lblDate);
		rowDate.add(authoredOn);
  		add(rowDate); 
		add(new SeparatorField());
		
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
		add(new SeparatorField());
		
		//resize photo sections
		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
		add(resizePhoto);

		//LabelField that displays text in the specified color.
		LabelField lblDescResize = getLabel(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS)); 
		lblDescResize.setFont(fnt);
		add(lblDescResize);
    }
    
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		long gmtTime = CalendarUtils.adjustTimeFromDefaultTimezone(authoredOn.getDate()); 
		controller.setSettingsValues(gmtTime, passwordField.getText(), resizePhoto.getChecked());
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
}