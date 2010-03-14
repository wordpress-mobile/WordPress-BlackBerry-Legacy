package com.wordpress.view;

import java.util.Date;
import java.util.TimeZone;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
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
import com.wordpress.utils.CalendarUtils;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.HorizontalPaddedFieldManager;
import com.wordpress.view.dialog.DiscardChangeInquiryView;


public class PostSettingsView extends StandardBaseView {
	
    private BlogObjectController controller; //controller associato alla view
    private DateField  authoredOn;
    private PasswordEditField passwordField;
	private BorderedFieldManager rowPhotoRes;
	private CheckboxField resizePhoto;
	private HorizontalFieldManager rowImageResizeWidth;
	private HorizontalFieldManager rowImageResizeHeight;
	private BasicEditField imageResizeWidthField;
	private BasicEditField imageResizeHeightField;  
	private Integer imageResizeWidth;
	private Integer imageResizeHeight;
	
    
    public PostSettingsView(BlogObjectController _controller, Date postAuth, String password, boolean isResImg, Integer imageResizeWidth, Integer imageResizeHeight ) {
    	super(_resources.getString(WordPressResource.MENUITEM_SETTINGS), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
    	this.controller=_controller;
    	this.imageResizeWidth = imageResizeWidth;
    	this.imageResizeHeight = imageResizeHeight;
    	
    	long datetime = new Date().getTime();;
    	if(postAuth != null ) {
    		datetime = CalendarUtils.adjustTimeToDefaultTimezone(postAuth.getTime());
    	}
			
		//row date 
        BorderedFieldManager rowDate = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
        
        HorizontalFieldManager innerContainerForDateField = new HorizontalPaddedFieldManager(HorizontalFieldManager.NO_HORIZONTAL_SCROLL 
                | HorizontalFieldManager.NO_VERTICAL_SCROLL | HorizontalFieldManager.USE_ALL_WIDTH);

		LabelField lblDate = getLabel(_resources.getString(WordPressResource.LABEL_POST_PUBLISHEDON));   
	    authoredOn= new DateField("", datetime, DateField.DATE_TIME);
	    authoredOn.setTimeZone(TimeZone.getDefault()); //setting the field time zone
		SimpleDateFormat sdFormat = new SimpleDateFormat(_resources.getString(WordPressResource.DEFAULT_DATE_FORMAT));
	    authoredOn.setFormat(sdFormat);
	    innerContainerForDateField.add(lblDate);
	    innerContainerForDateField.add(authoredOn);
	    
	    rowDate.add(innerContainerForDateField);
		add(rowDate); 
		
        //row password
        BorderedFieldManager rowPassword = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL
        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
		LabelField lblPassword = getLabel(_resources.getString(WordPressResource.LABEL_PASSWD));		
        passwordField = new PasswordEditField("", password, 64, Field.EDITABLE);
        rowPassword.add(lblPassword);
        rowPassword.add(passwordField);
    	BasicEditField lblDesc = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_POST_PASSWORD));
		rowPassword.add(lblDesc);
		add(rowPassword);
		
		//resize photo sections
		rowPhotoRes = new BorderedFieldManager(
				Manager.NO_HORIZONTAL_SCROLL
				| Manager.NO_VERTICAL_SCROLL);
		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
		resizePhoto.setChangeListener(listenerResizePhotoCheckbox);
		rowPhotoRes.add(resizePhoto);

		//LabelField that displays text in the specified color.
		BasicEditField lblDescResize = getDescriptionTextField(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS)); 
		rowPhotoRes.add(lblDescResize);

		if(isResImg) {
			addImageResizeWidthField();
			addImageResizeHeightField();
		}

		add(rowPhotoRes);
		
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

	// Enable or disable image resize width/height fields when the "resize image" checkbox changes.
	private FieldChangeListener listenerResizePhotoCheckbox = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	if(resizePhoto.getChecked() == true) {
	    		addImageResizeWidthField();
	    		addImageResizeHeightField();
	    	}
	    	else {
	    	       	rowPhotoRes.delete(rowImageResizeWidth);
	    	       	rowImageResizeWidth = null;
	    	       	rowPhotoRes.delete(rowImageResizeHeight);
	    	       	rowImageResizeHeight = null;
	    	}
	   }
	};
	
	// Recalculate the image resize height whenever the image resize width changes. Aspect ratio is fixed.
	private FocusChangeListener listenerImageResizeWidthField = new FocusChangeListener() {
	    public void focusChanged(Field field, int eventType) {
	    	if((eventType == FocusChangeListener.FOCUS_LOST) && (imageResizeWidthField.isDirty())) {
		    	try {
		    		int newWidth = Integer.parseInt(imageResizeWidthField.getText());
		    		if(newWidth == 0) {
		    			Dialog.alert(_resources.getString(WordPressResource.ERROR_RESIZE_WIDTH));
		    			imageResizeWidthField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_WIDTH));
		    			imageResizeHeightField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		    		}
		    		else {
		    			int newHeight = (int)(newWidth * 0.75);
		    			imageResizeHeightField.setText(Integer.toString(newHeight));
		    		}
		    	}
		    	catch(NumberFormatException e) {
		    		Log.error("Unexpected condition: ImageResizeWidthField was not numeric in BlogOptionsView.");
		    	}
	    	}
	   }
	};

	// Recalculate the image resize width whenever the image resize height changes. Aspect ratio is fixed.
	private FocusChangeListener listenerImageResizeHeightField = new FocusChangeListener() {
	    public void focusChanged(Field field, int eventType) {
	    	if((eventType == FocusChangeListener.FOCUS_LOST) && (imageResizeHeightField.isDirty())) {
		    	try {
		    		int newHeight = Integer.parseInt(imageResizeHeightField.getText());
		    		if(newHeight == 0) {
		    			Dialog.alert(_resources.getString(WordPressResource.ERROR_RESIZE_HEIGHT));
		    			imageResizeWidthField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_WIDTH));
		    			imageResizeHeightField.setText(String.valueOf(ImageUtils.DEFAULT_RESIZE_HEIGHT));
		    		}
		    		else {
		    			int newWidth = (int)((newHeight * 1.3333) + 1);
		    			imageResizeWidthField.setText(Integer.toString(newWidth));
		    		}
		    	}
		    	catch(NumberFormatException e) {
		    		Log.error("Unexpected condition: ImageResizeHeightField was not numeric in BlogOptionsView.");
		    	}
	    	}
	   }
	};

	private void addImageResizeWidthField() {
        rowImageResizeWidth = new HorizontalFieldManager();
        rowImageResizeWidth.add( getLabel(_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_WIDTH)));      
        imageResizeWidthField = new BasicEditField(
        		"", 
        		(imageResizeWidth == null ? "" : imageResizeWidth.toString()), 
        		4, 
        		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
        
        imageResizeWidthField.setFocusListener(null);
        imageResizeWidthField.setFocusListener(listenerImageResizeWidthField);
        rowImageResizeWidth.add(imageResizeWidthField);
       	rowPhotoRes.add(rowImageResizeWidth);
	}

	private void addImageResizeHeightField() {
	    rowImageResizeHeight = new HorizontalFieldManager();
	    rowImageResizeHeight.add( getLabel(_resources.getString(WordPressResource.LABEL_RESIZE_IMAGE_HEIGHT)));
	    imageResizeHeightField = new BasicEditField(
	    		"", 
	    		(imageResizeHeight == null ? "" : imageResizeHeight.toString()), 
	    		4, 
	    		Field.EDITABLE | BasicEditField.FILTER_NUMERIC);
	    
	    imageResizeHeightField.setFocusListener(null);
	    imageResizeHeightField.setFocusListener(listenerImageResizeHeightField);
	    rowImageResizeHeight.add(imageResizeHeightField);
    	rowPhotoRes.add(rowImageResizeHeight);
	}
	
	private void saveChanges() {
		if(authoredOn.isDirty() || passwordField.isDirty() || resizePhoto.isDirty() 
				|| (imageResizeWidthField == null ? false : imageResizeWidthField.isDirty())
				|| (imageResizeHeightField == null ? false : imageResizeHeightField.isDirty())
			){
			
			Log.trace("settings are changed");
			
			if(authoredOn.isDirty()) {
				long gmtTime = CalendarUtils.adjustTimeFromDefaultTimezone(authoredOn.getDate());
				controller.setAuthDate(gmtTime);
			}
			
			if(passwordField.isDirty()) {
				controller.setPassword(passwordField.getText());
			}
						
			Integer resizeWidth = new Integer(ImageUtils.DEFAULT_RESIZE_WIDTH);
			if(imageResizeWidthField != null) {
				resizeWidth = Integer.valueOf(imageResizeWidthField.getText());
			}
			Integer resizeHeight = new Integer(ImageUtils.DEFAULT_RESIZE_HEIGHT);
			if(imageResizeHeightField != null) {
				resizeHeight = Integer.valueOf(imageResizeHeightField.getText());
			}
			
			//Before saving we should do an additional check over img resize width and height.
			//it is necessary when user put a value into width/height field and then press backbutton;
			//the focus lost on those fields is never fired....
			int[] keepAspectRatio = ImageUtils.keepAspectRatio(resizeWidth.intValue(), resizeHeight.intValue());
			resizeWidth = new Integer(keepAspectRatio[0]);
			resizeHeight = new Integer(keepAspectRatio[1]);
		
			if (resizePhoto.isDirty()
					|| (imageResizeWidthField == null ? false : imageResizeWidthField.isDirty())
					|| (imageResizeHeightField == null ? false : imageResizeHeightField.isDirty())
			) {

				controller.setPhotoResizing(resizePhoto.getChecked(), 
						resizeWidth, 
						resizeHeight);
			}
			
			controller.setObjectAsChanged(true);
		} else {
			Log.trace("settings are NOT changed");
		}
	}
	
	
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		
		boolean isModified=false;
		
		if(authoredOn.isDirty() || passwordField.isDirty() || resizePhoto.isDirty()
				|| (imageResizeWidthField == null ? false : imageResizeWidthField.isDirty())
				|| (imageResizeHeightField == null ? false : imageResizeHeightField.isDirty()) ) {
			
			isModified = true;
		}
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