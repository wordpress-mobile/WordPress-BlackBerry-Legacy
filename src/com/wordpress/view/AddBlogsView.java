package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
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
	private ObjectChoiceField  maxRecentPost;
	private CheckboxField resizePhoto;
	
	public boolean isResizePhoto(){
		return resizePhoto.getChecked();
	}
	
	public String getBlogUrl() {
		return blogUrlField.getText();
	}
	public String getBlogUser() {
		return userNameField.getText();
	}
	
	public String getBlogPass() {
		return passwordField.getText();
	}
	
	public int getMaxRecentPostIndex() {
		return maxRecentPost.getSelectedIndex();
	}
	
	 public AddBlogsView(AddBlogsController addBlogsController, Hashtable values) {
	    	super();
	    	this.addBlogsController=addBlogsController;
	    	//add a screen title
	        LabelField title = new LabelField(_resources.getString(WordPressResource.TITLE_ADDBLOGS),
	                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
	        setTitle(title);
	    	
	        //loading input data
	        String user= (String)values.get("user");
	        String pass= (String)values.get("pass");
	        String url= (String)values.get("url");
	        String[] recentPost=(String[])values.get("recentpost");
	        int recentPostSelect= ((Integer)values.get("recentpostselected")).intValue();
			boolean isResImg= ((Boolean)values.get("isresphotos")).booleanValue();
	        //end loading
			
            blogUrlField = new BasicEditField(_resources.getString(WordPressResource.LABEL_BLOGURL), url, 100, Field.EDITABLE);
            blogUrlField.setFilter(new URLTextFilter());
            userNameField = new BasicEditField(_resources.getString(WordPressResource.LABEL_BLOGUSER), user, 60, Field.EDITABLE);
            passwordField = new PasswordEditField(_resources.getString(WordPressResource.LABEL_BLOGPASSWD), pass, 64, Field.EDITABLE);
            LabelField lf = new LabelField();
            add(lf);
            add(new SeparatorField());
            add(blogUrlField);
            add(userNameField);
            add(passwordField);
            add(new SeparatorField());
            //new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_AUDIOENCODING),lines,selectedIndex);            
            maxRecentPost = new ObjectChoiceField (_resources.getString(WordPressResource.LABEL_MAXRECENTPOST), recentPost,recentPostSelect);
            add(maxRecentPost);            
            
    		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
			add(resizePhoto);
			add(new LabelField(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS)));
			
			addMenuItem(_addBlogItem);
			
			/*
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK));
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK));
    		buttonBACK.setChangeListener(addBlogsController.getBackButtonListener());
            buttonOK.setChangeListener(addBlogsController.getOkButtonListener());
            HorizontalFieldManager hManager = new HorizontalFieldManager();
            hManager.add(buttonOK);
    		hManager.add(buttonBACK);
    		add(hManager);    	*/	
	}
	 
	    //add blog menu item 
	    private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 140, 10) {
	        public void run() {
	        	addBlogsController.addBlogs();
	        }
	    };

	   //override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		return addBlogsController.discardChange();			
	}
}