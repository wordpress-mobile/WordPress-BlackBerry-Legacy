package com.wordpress.view;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.SeparatorField;
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
	        LabelField title = new LabelField(_resources.getString(WordPressResource.APPLICATION_TITLE),
	                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
	        setTitle(title);
	    	
	        //add fields to display a screen label that identified the city,
            //as well as information on population, state, and tourist attractions
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
            add(buttonOK);
            add(buttonBACK);

            
            blogUrlField.setChangeListener(addBlogsController.getButtonListener());
            userNameField.setChangeListener(addBlogsController.getButtonListener());
            passwordField.setChangeListener(addBlogsController.getButtonListener());
            buttonBACK.setChangeListener(addBlogsController.getButtonListener());
            buttonOK.setChangeListener(addBlogsController.getButtonListener());
	 }
}