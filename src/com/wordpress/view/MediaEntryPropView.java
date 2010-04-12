package com.wordpress.view;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.TextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogObjectController;
import com.wordpress.model.MediaEntry;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.HorizontalPaddedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;
import com.wordpress.view.dialog.ErrorView;
import com.wordpress.view.mm.MediaViewMediator;


public class MediaEntryPropView extends StandardBaseView {
	
    private BlogObjectController controller;
    private MediaViewMediator mediaViewMediator;
    private BasicEditField fileNameField;
    private BasicEditField titleField;
    private BasicEditField captionField;
    private BasicEditField descriptionField;
	private ObjectChoiceField mediaVerticalAlignment;
    
    public MediaEntryPropView(BlogObjectController _controller, MediaViewMediator mediaViewMediator) {
    	super(_resources.getString(WordPressResource.MENUITEM_PROPERTIES), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
    	this.controller=_controller;
		this.mediaViewMediator = mediaViewMediator;
		
    	//retrive the media entry from the mediator
    	MediaEntry mediaEntry = mediaViewMediator.getMediaEntry();
    	
        //row FileName
    	fileNameField = new BasicEditField("", "", 100, Field.EDITABLE);
    	addEntry(_resources.getString(WordPressResource.LABEL_FILE_NAME), 
    			fileNameField,
    			mediaEntry.getFileName() != null ? mediaEntry.getFileName() : "");
    	fileNameField.setFilter(TextFilter.get(TextFilter.FILENAME));
    	
        //title
    	titleField = new BasicEditField("", "", 100, Field.EDITABLE);
       	addEntry(_resources.getString(WordPressResource.LABEL_TITLE), 
    			titleField,
    			mediaEntry.getTitle() != null ? mediaEntry.getTitle() : "");
       	
        //caption
       	captionField = new BasicEditField("", "", 100, Field.EDITABLE);
       	addEntry(_resources.getString(WordPressResource.LABEL_CAPTION), 
       			captionField,
    			mediaEntry.getCaption() != null ? mediaEntry.getCaption() : "");

        //description
       	descriptionField = new BasicEditField("", "", 100, Field.EDITABLE);
       	addEntry(_resources.getString(WordPressResource.LABEL_DESC), 
       			descriptionField,
    			mediaEntry.getDescription() != null ? mediaEntry.getDescription() : "");
    
       	
		 //row media object position
        BorderedFieldManager rowMediaObjPosition = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
		 String labelTop= _resources.getString(WordPressResource.LABEL_VERTICAL_ALIGNMENT_TOP);
		 String labelBottom= _resources.getString(WordPressResource.LABEL_VERTICAL_ALIGNMENT_BOTTOM);
		 String[] optLabels = {labelBottom, labelTop};
		 int selectedPosition = 0;

		 //false/0 = bottom, true/1 = top
		 if(mediaEntry.isVerticalAlignmentOnTop())
			 selectedPosition = 1;
		 
		 mediaVerticalAlignment = new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_VERTICAL_ALIGNMENT),optLabels, selectedPosition);
		 rowMediaObjPosition.add(mediaVerticalAlignment);
		 add(rowMediaObjPosition);
		 //-------------
		 
		BaseButtonField buttonOK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
		BaseButtonField buttonBACK = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
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
	    }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	onClose();
	    }
	};
    
	private void saveChanges() {
		
		if(fileNameField.getText().trim().equals("")) {
			ErrorView errView = new ErrorView(_resources.getString(WordPressResource.MESSAGE_FILENAME_NOT_BLANK));
			errView.show();
			return;
		}
		if(!fileNameField.isDataValid()) {
			ErrorView errView = new ErrorView(_resources.getString(WordPressResource.MESSAGE_FILENAME_SPECIAL_CHARS));
			errView.show();
			return;
		}

		controller.setObjectAsChanged(true);
		MediaEntry mediaEntry = mediaViewMediator.getMediaEntry();
		
		if(fileNameField.isDirty()) {
			mediaEntry.setFileName(fileNameField.getText().trim());
		}
		
		if(titleField.isDirty()) {
			mediaEntry.setTitle(titleField.getText().trim());
		}
		
		if(captionField.isDirty()) {
			mediaEntry.setCaption(captionField.getText().trim());
		}
		
		if(descriptionField.isDirty()) {
			mediaEntry.setDescription(descriptionField.getText().trim());
		}
		
		if(mediaVerticalAlignment.isDirty()) {
			int selectedIndex = mediaVerticalAlignment.getSelectedIndex();
			//false/0 = bottom, true/1 = top
			if(selectedIndex == 0) {
				mediaEntry.setVerticalAlignmentOnTop(false);
			} else {
				mediaEntry.setVerticalAlignmentOnTop(true);
			}
		}
		mediaViewMediator.mediaEntryChanged(); //notify the mediator, this update others UI
		controller.backCmd(); //back to prev screen
	}
    
    private void addEntry(String label, BasicEditField valueField, String value){
        BorderedFieldManager rowFileName = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            	
        LabelField fileNameLbl = GUIFactory.getLabel(label, Color.BLACK);   
        valueField.setText(value);
        rowFileName.add( fileNameLbl );
        rowFileName.add( valueField );
        add(rowFileName);
    }
    
	public boolean onClose()   {
			
		boolean isModified=isDirty();
		
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
    		return true;
    	} else {
    		Log.trace("user has selected cancel");
    		return false;
    	}
		
    }
	
	public BaseController getController() {
		return controller;
	}
}