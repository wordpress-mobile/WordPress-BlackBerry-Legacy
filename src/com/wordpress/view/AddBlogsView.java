package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AddBlogsController;
import com.wordpress.controller.BaseController;

public class AddBlogsView extends BaseView {
	
    private AddBlogsController controller= null;
	private BasicEditField blogUrlField;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private ObjectChoiceField  maxRecentPost;
	private CheckboxField resizePhoto;
	
	HorizontalFieldManager buttonsManager;
	
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
	    	this.controller=addBlogsController;
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
           
            add(blogUrlField);
            add(userNameField);
            add(passwordField);
            add(new SeparatorField());
            //new ObjectChoiceField(_resources.getString(WordPressResource.LABEL_AUDIOENCODING),lines,selectedIndex);            
            maxRecentPost = new ObjectChoiceField (_resources.getString(WordPressResource.LABEL_MAXRECENTPOST), recentPost,recentPostSelect);
            add(maxRecentPost);            
            
    		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
			add(resizePhoto);

			//LabelField that displays text in the specified color.
			LabelField lblDesc = new LabelField(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS))
			{
			    public void paint(Graphics graphics)
			    {
			        graphics.setColor(Color.GRAY);
			        super.paint(graphics);
			    }
			};
			Font fnt = this.getFont().derive(Font.ITALIC);
			lblDesc.setFont(fnt);
			add(lblDesc);
			
			
			addMenuItem(_addBlogItem);
						
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK));
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK));
    		buttonBACK.setChangeListener(addBlogsController.getBackButtonListener());
            buttonOK.setChangeListener(addBlogsController.getOkButtonListener());
            buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		add(buttonsManager); 
    		add(new LabelField());
	}
	 
	 // Handle trackball clicks.
		protected boolean navigationClick(int status, int time) {
			Field fieldWithFocus = UiApplication.getUiApplication().getActiveScreen().getFieldWithFocus();
			if(fieldWithFocus == buttonsManager) { //focus on the bottom buttons, do not open menu on whell click
				return true;
			}
			else 
			 return super.navigationClick(status,time);
		}
	 
	    //add blog menu item 
	    private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 140, 10) {
	        public void run() {
	        	controller.addBlogs();
	        }
	    };

	   //override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		return controller.discardChange();			
	}
	
	public BaseController getController() {
		return controller;
	}
}