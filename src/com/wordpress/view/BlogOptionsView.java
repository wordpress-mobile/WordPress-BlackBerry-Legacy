package com.wordpress.view;

import java.util.Hashtable;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectChoiceField;
import net.rim.device.api.ui.component.PasswordEditField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogOptionsController;
import com.wordpress.view.component.BorderedFieldManager;

public class BlogOptionsView extends BaseView {
	
    private BlogOptionsController controller= null;
    private VerticalFieldManager _container;
	private BasicEditField userNameField;
	private PasswordEditField passwordField;
	private ObjectChoiceField  maxRecentPost;
	private CheckboxField resizePhoto;
	
	HorizontalFieldManager buttonsManager;
	
	public boolean isResizePhoto(){
		return resizePhoto.getChecked();
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
	
	 public BlogOptionsView(BlogOptionsController blogsController, Hashtable values) {
	    	super(_resources.getString(WordPressResource.TITLE_BLOG_OPTION_VIEW)+" > "+ blogsController.getBlogName(), Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
	    	this.controller=blogsController;
	    	
	    	
	    	VerticalFieldManager internalManager = new VerticalFieldManager( Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR ) {
	    		public void paintBackground( Graphics g ) {
	    			g.clear();
	    			int color = g.getColor();
	    			g.setColor( Color.LIGHTGREY );
	    			g.drawBitmap(0, 0, Display.getWidth(), Display.getHeight(), _backgroundBitmap, 0, 0);
	    			//g.fillRect( 0, 0, Display.getWidth(), Display.getHeight() );
	    			g.setColor( color );
	    		}
	    		
	    		protected void sublayout( int maxWidth, int maxHeight ) {
	    			
	    			int titleFieldHeight = 0;
	    			if ( titleField != null ) {
	    				titleFieldHeight = titleField.getHeight();
	    			}
	    			
	    			int displayWidth = Display.getWidth(); // I would probably make these global
	    			int displayHeight = Display.getHeight();
	    			
	    			super.sublayout( displayWidth, displayHeight - titleFieldHeight );
	    			setExtent( displayWidth, displayHeight - titleFieldHeight );
	    		}
	    		
	    	};
	    	
	    	_container = new VerticalFieldManager( Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR );
	    	internalManager.add( _container );
	    	super.add( internalManager );
	    	
	    	
	    	
	        //loading input data
	        String user = (String)values.get("user");
	        String pass= (String)values.get("pass");
	        String[] recentPost=(String[])values.get("recentpost");
	        int recentPostSelect = ((Integer)values.get("recentpostselected")).intValue();
			boolean isResImg = ((Boolean)values.get("isresphotos")).booleanValue();
	        //end loading
			
            //row username
            BorderedFieldManager rowUserName = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		LabelField lblUserName = getLabel(_resources.getString(WordPressResource.LABEL_USERNAME)); 
            userNameField = new BasicEditField("", user, 60, Field.EDITABLE);
            rowUserName.add(lblUserName);
    		rowUserName.add(userNameField);
            add(rowUserName);
    		
            //row password
            BorderedFieldManager rowPassword = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
    		LabelField lblPassword = getLabel(_resources.getString(WordPressResource.LABEL_PASSWD)); 
            passwordField = new PasswordEditField("", pass, 64, Field.EDITABLE);
            rowPassword.add(lblPassword);
            rowPassword.add(passwordField);
            add(rowPassword);

            //row max recent post
            BorderedFieldManager rowMaxRecentPost = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL
	        		| BorderedFieldManager.BOTTOM_BORDER_NONE);
            maxRecentPost = new ObjectChoiceField (_resources.getString(WordPressResource.LABEL_MAXRECENTPOST), recentPost,recentPostSelect);
            rowMaxRecentPost.add(maxRecentPost);
            add(rowMaxRecentPost);            

            //row resize photos
            BorderedFieldManager rowResizePhotos = new BorderedFieldManager(
	        		Manager.NO_HORIZONTAL_SCROLL
	        		| Manager.NO_VERTICAL_SCROLL);
    		resizePhoto=new CheckboxField(_resources.getString(WordPressResource.LABEL_RESIZEPHOTOS), isResImg);
    		rowResizePhotos.add(resizePhoto);
			//LabelField that displays text in the specified color.
			LabelField lblDesc = getLabel(_resources.getString(WordPressResource.DESCRIPTION_RESIZEPHOTOS)); 
			Font fnt = this.getFont().derive(Font.ITALIC);
			lblDesc.setFont(fnt);
			rowResizePhotos.add(lblDesc);
			add(rowResizePhotos);
			
            ButtonField buttonOK= new ButtonField(_resources.getString(WordPressResource.BUTTON_OK), ButtonField.CONSUME_CLICK);
            ButtonField buttonBACK= new ButtonField(_resources.getString(WordPressResource.BUTTON_BACK), ButtonField.CONSUME_CLICK);
    		buttonBACK.setChangeListener(blogsController.getBackButtonListener());
            buttonOK.setChangeListener(blogsController.getOkButtonListener());
            
            HorizontalFieldManager buttonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            buttonsManager.add(buttonOK);
    		buttonsManager.add(buttonBACK);
    		add(buttonsManager);
    		add(new LabelField("", Field.NON_FOCUSABLE)); //space after buttons
	}
	 
		//override add(Field field) to add field to my personal manager
	 public void add( Field field ) {
		 _container.add( field );
	 }

	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		return controller.dismissView();			
	}
	
	public BaseController getController() {
		return controller;
	}
}