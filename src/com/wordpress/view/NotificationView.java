package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.NotificationController;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.CheckBoxListField;
import com.wordpress.view.dialog.DiscardChangeInquiryView;

public class NotificationView extends StandardBaseView {
	
    private NotificationController controller= null;
	private ObjectChoiceField  updateInterval;
	
	private CheckBoxListField checkBoxController = null;
    private ListField chkField;
	
	HorizontalFieldManager buttonsManager;
		
	public int getMaxRecentPostIndex() {
		return updateInterval.getSelectedIndex();
	}
	
	 public NotificationView(NotificationController blogsController, String[] blogName, boolean blogSelected[]) {
	    	super(_resources.getString(WordPressResource.TITLE_NOTIFICATION_VIEW), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=blogsController;
	    	

            BorderedFieldManager notifyRow = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL );
            
            
            LabelField lblDescReset = getLabel(_resources.getString(WordPressResource.NOTIFICATION_DESC)); 
            Font fnt = this.getFont().derive(Font.ITALIC);
   		 	lblDescReset.setFont(fnt);
   		 	notifyRow.add(lblDescReset);
   		 	
   		 	checkBoxController= new CheckBoxListField(blogName, blogSelected);
   		 	this.chkField= checkBoxController.get_checkList();
//   		 	add(chkField);
            
   		 	String[] values = {"120 minutes", "90 minutes", "60 minutes", "30 minutes", "15 minutes", "10 minutes", "5 minutes"};
            updateInterval = new ObjectChoiceField (_resources.getString(WordPressResource.NOTIFICATION_INTERVAL_LABEL), values,0);
            notifyRow.add(updateInterval);
            add(notifyRow);            

            BorderedFieldManager listContainer = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL );
            
            listContainer.add(chkField);
            add(listContainer);
            
            
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
    		buttonBACK.setChangeListener(listenerOkButton);
            buttonOK.setChangeListener(listenerBackButton);
            
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		add(buttonsManager);
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons
	}

	 
	 
	 private boolean isModified() {
			boolean isModified=false;
			
		/*	String pass= view.getBlogPass();
			String user= view.getBlogUser();
			int maxPostIndex=view.getMaxRecentPostIndex();
			int valueMaxPostCount=AddBlogsController.recentsPostValues[maxPostIndex];
			boolean isResPhotos = view.isResizePhoto();
			 
			if(!blog.getUsername().equals(user) || !blog.getPassword().equals(pass)
				|| blog.getMaxPostCount() != valueMaxPostCount || isResPhotos != blog.isResizePhotos() ) {
				isModified=true;
			}*/
			return isModified;
		}
	 
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		
		boolean isModified = this.isModified();
		
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
    		saveAndBack();
    		return true;
    	} else {
    		Log.trace("user has selected cancel");
    		return false;
    	}		
	}
	
	public BaseController getController() {
		return controller;
	}
		
	
	//called when user click the OK button
	private void  saveAndBack(){
		boolean isModified = this.isModified();
		
		if(!isModified) {
			controller.backCmd();
			return;
		}

		//fai le modifiche qua
		
		controller.backCmd();
	}
	
	private FieldChangeListener listenerOkButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	saveAndBack();
	   }
	};


	private FieldChangeListener listenerBackButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	controller.backCmd();
	   }
	};
	
    //Override the makeMenu method so we can add a custom menu item
    //if the checkbox ListField has focus.
    protected void makeMenu(Menu menu, int instance)
    {
        Field focus = UiApplication.getUiApplication().getActiveScreen().getLeafFieldWithFocus();
        if(focus == chkField) 
        {
            //The commentsList ListField instance has focus.
            //Add the _toggleItem MenuItem.
        	checkBoxController.changeToggleItemLabel(_resources.getString(WordPressResource.MENUITEM_SELECT), _resources.getString(WordPressResource.MENUITEM_DESELECT));
            menu.add(checkBoxController._toggleItem);
        }
                
        //Create the default menu.
        super.makeMenu(menu, instance);
    }    
	
	
}