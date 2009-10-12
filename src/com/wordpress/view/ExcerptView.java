package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Post;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class ExcerptView extends StandardBaseView {
	
    private PostController controller; //controller associato alla view
	private HtmlTextField excerptContent;
	private Post post;
	
    public ExcerptView(PostController _controller, Post post, String title) {
    	super(_resources.getString(WordPressResource.MENUITEM_EXCERPT)+" > "+ title, Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
    	this.controller=_controller;
		this.post = post;

        //row Parent
        BorderedFieldManager rowParent = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
        
    	excerptContent = new HtmlTextField(post.getExcerpt());
    	
    	rowParent.add(getLabel(_resources.getString(WordPressResource.LABEL_EXCERPT_CONTENT)));
    	rowParent.add(excerptContent);
        add(rowParent);
        
        ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
        ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
		buttonBACK.setChangeListener(listenerBackButton);
        buttonOK.setChangeListener(listenerOkButton);
        HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
        buttonsManager.add(buttonOK);
		buttonsManager.add(buttonBACK);
		add(buttonsManager); 
        
        add(new LabelField("", Field.NON_FOCUSABLE)); //space after content
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
		if(excerptContent.isDirty()) {
			Log.trace("Excerpt is changed");
			controller.setObjectAsChanged(true);
			post.setExcerpt(excerptContent.getText());
		}
	}
	
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		boolean isModified=excerptContent.isDirty();
		if(!isModified) {
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


