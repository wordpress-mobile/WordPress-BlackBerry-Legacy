package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AddBlogsController;

public class AddBlogsView extends BaseView {
	
    private AddBlogsController addBlogsController= null;
	private BasicEditField blogUrlField;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	
	 public AddBlogsView(AddBlogsController addBlogsController) {
	    	super();
	    	this.addBlogsController=addBlogsController;
	    	//add a screen title
	        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_APPLICATION),
	                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
	        setTitle(title);
	    	
            LabelField lf = new LabelField();
            blogUrlField = new BasicEditField(_resources.getString(WordPressResource.LABEL_BLOGURL), "", 100, Field.EDITABLE);
            blogUrlField.setFilter(new URLTextFilter());
            userNameField = new BasicEditField(_resources.getString(WordPressResource.LABEL_BLOGUSER), "", 60, Field.EDITABLE);
            passwordField = new PasswordEditField(_resources.getString(WordPressResource.LABEL_BLOGPASSWD), "", 64, Field.EDITABLE);
            add(lf);
            add(new SeparatorField());
            add(blogUrlField);
            add(userNameField);
            add(passwordField);
                    
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK));
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK));
    		HorizontalFieldManager hManager = new HorizontalFieldManager();
    		hManager.add(buttonOK);
    		hManager.add(buttonBACK);
    		add(hManager);

            
            blogUrlField.setChangeListener(addBlogsController.getButtonListener());
            userNameField.setChangeListener(addBlogsController.getButtonListener());
            passwordField.setChangeListener(addBlogsController.getButtonListener());
            buttonBACK.setChangeListener(addBlogsController.getBackButtonListener());
            buttonOK.setChangeListener(addBlogsController.getOkButtonListener());
	 }
	 
	   //override onClose() to by-pass the standard dialog box when the screen is closed    
		public boolean onClose()   {
			return addBlogsController.discardChange();			
	    }

}