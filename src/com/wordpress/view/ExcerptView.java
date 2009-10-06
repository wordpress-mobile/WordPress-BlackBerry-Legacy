package com.wordpress.view;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.PostController;
import com.wordpress.model.Post;
import com.wordpress.view.component.BorderedFieldManager;
import com.wordpress.view.component.HtmlTextField;


public class ExcerptView extends BaseView {
	
    private PostController controller; //controller associato alla view
    private VerticalFieldManager _container;
	private HtmlTextField excerptContent;
	private Post post;
	
    public ExcerptView(PostController _controller, Post post, String title) {
    	super(_resources.getString(WordPressResource.MENUITEM_EXCERPT)+" > "+ title, Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR);
    	this.controller=_controller;
		this.post = post;

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
    	
        //row Parent
        BorderedFieldManager rowParent = new BorderedFieldManager(
        		Manager.NO_HORIZONTAL_SCROLL
        		| Manager.NO_VERTICAL_SCROLL);
        
    	excerptContent = new HtmlTextField(post.getExcerpt());
    	
    	rowParent.add(getLabel(_resources.getString(WordPressResource.LABEL_EXCERPT_CONTENT)));
    	rowParent.add(excerptContent);
        add(rowParent);
        
        add(new LabelField("", Field.NON_FOCUSABLE)); //space after content
    }
    
	public void add( Field field ) {
		_container.add( field );
	}
    
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
		if(excerptContent.isDirty()) {
			controller.setObjectAsChanged(true);
			post.setExcerpt(excerptContent.getText());
		}
		controller.backCmd();
		return true;
    }
	
	public BaseController getController() {
		return controller;
	}
}


