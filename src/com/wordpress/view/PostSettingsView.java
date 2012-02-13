package com.wordpress.view;

import java.util.Date;
import java.util.TimeZone;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.DateField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.controller.PostController;
import com.wordpress.utils.CalendarUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.container.BorderedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class PostSettingsView extends StandardBaseView {
	
    private BlogObjectController controller; //controller associato alla view
    private DateField  authoredOn;
    private PasswordEditField passwordField;
	
	private BorderedFieldManager discussionSettingContainer;
	BorderedFieldManager rowDate;
	BorderedFieldManager rowPassword;
			
	//discussion fields
	private CheckboxField enableCommentsField;
	private CheckboxField enablePingbacksAndTrackbacksField;
			
	//used from page view e post view
    public PostSettingsView(BlogObjectController _controller, Date postAuth, String password) {
    	super(_resources.getString(WordPressResource.MENUITEM_SETTINGS), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
    	this.controller=_controller;

    	long datetime = new Date().getTime();
    	if(postAuth != null ) {
    		datetime = CalendarUtils.adjustTimeToDefaultTimezone(postAuth.getTime());
    	}
			
		//row date 
        rowDate = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
        
	    authoredOn= new DateField(_resources.getString(WordPressResource.LABEL_POST_PUBLISHEDON), datetime, DateField.DATE_TIME);
	    authoredOn.setTimeZone(TimeZone.getDefault()); //setting the field time zone
		SimpleDateFormat sdFormat = new SimpleDateFormat(_resources.getString(WordPressResource.DEFAULT_DATE_FORMAT));
	    authoredOn.setFormat(sdFormat);
	    
	    rowDate.add(authoredOn);
		add(rowDate); 
		
	    //row password
        if(_controller instanceof PostController) {
			rowPassword = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
	        passwordField = new PasswordEditField(_resources.getString(WordPressResource.LABEL_PASSWD)+": ", password, 64, Field.EDITABLE);
	        rowPassword.add(passwordField);
	    	BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_POST_PASSWORD));
			rowPassword.add(lblDesc);
			add(rowPassword);
        }
		
		//discussion settings 
		discussionSettingContainer = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
		discussionSettingContainer.add(
        		GUIFactory.getLabel(_resources.getString(WordPressResource.TITLE_DISCUSSION_SETTINGS),Color.BLACK)
        		);
		discussionSettingContainer.add(GUIFactory.createSepatorField());

		enableCommentsField = new CheckboxField(_resources.getString(WordPressResource.LABEL_ALLOW_COMMENTS), controller.isCommentsAllowed());
		discussionSettingContainer.add(enableCommentsField);
		enablePingbacksAndTrackbacksField = new CheckboxField(_resources.getString(WordPressResource.LABEL_ALLOW_PINGBACKS_TRACKBACKS), controller.isPingbacksAndTrackbacksAllowed());
		discussionSettingContainer.add(enablePingbacksAndTrackbacksField);
		add(discussionSettingContainer);
		
        BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
        BaseButtonField buttonBACK= GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
		buttonBACK.setChangeListener(listenerBackButton);
        buttonOK.setChangeListener(listenerOkButton);
        HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
        buttonsManager.add(buttonOK);
		buttonsManager.add(buttonBACK);
		add(buttonsManager); 
        
        add(new LabelField("", Field.NON_FOCUSABLE)); //space after content
        
        controller.bumpScreenViewStats("com/wordpress/view/PostSettingsView", "PostSettings Screen", "", null, "");
    }
    
    private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	saveChanges();
	    	controller.backCmd();
	    }
	};

	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	onClose();
	    }
	};

	private void saveChanges() {
		if(hasChanges()){

			Log.trace("settings are changed");

			if(authoredOn.isDirty()) {
				long gmtTime = CalendarUtils.adjustTimeFromDefaultTimezone(authoredOn.getDate());
				controller.setAuthDate(gmtTime);
			}

			if(passwordField == null ? false : passwordField.isDirty()) {
				controller.setPassword(passwordField.getText());
			}

			if(enableCommentsField == null ? false : enableCommentsField.isDirty()) {
				controller.setCommentsAllowed(enableCommentsField.getChecked());
			}
			if(enablePingbacksAndTrackbacksField == null ? false : enablePingbacksAndTrackbacksField.isDirty()) {
				controller.setPingbacksAndTrackbacksAllowed(enablePingbacksAndTrackbacksField.getChecked());
			}

			controller.setObjectAsChanged(true);
		} else {
			Log.trace("settings are NOT changed");
		}
	}
	
	private boolean hasChanges() {
		return (
				authoredOn.isDirty() 
				|| (passwordField == null ? false : passwordField.isDirty())
				|| (enableCommentsField == null ? false : enableCommentsField.isDirty())
				|| (enablePingbacksAndTrackbacksField == null ? false : enablePingbacksAndTrackbacksField.isDirty())
		);
	}

	public boolean onClose()   {

		if(!hasChanges()) {
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
    		saveChanges();
    		controller.backCmd();    		
    		return true;
    	} else {
    		Log.trace("user has selected cancel");
    		controller.backCmd();
    		return false;
    	}
    }
	
	public BaseController getController() {
		return controller;
	}
}