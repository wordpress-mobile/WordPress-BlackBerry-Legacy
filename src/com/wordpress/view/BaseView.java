package com.wordpress.view;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.view.component.HeaderField;

/**
 * Base class for all Application Screen
 * @author dercoli
 *
 */
public abstract class BaseView extends MainScreen {
	
	protected Field titleField; //main title of the screen
	
	protected static Bitmap _backgroundBitmap = null; 
	
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
		//retrive the bg image based on the screen dimensions
		_backgroundBitmap = WordPressCore.getInstance().getBackgroundBitmap();
	}
	
	public BaseView(long style) {
		super(style);
	}

    public BaseView(String title) {
		super();
		titleField = getTitleField(title);
		this.setTitle(titleField);
	}
    
	public BaseView(String title, long style) {
		super(style);
		titleField = getTitleField(title);
		this.setTitle(titleField);
	}
    
    //return the controller associated with this view
    public abstract BaseController getController();
    
    //create the title filed
	protected Field getTitleField(String title) {
		HeaderField headerField = new HeaderField(title);
		//if you want change color of the title bar, make sure to set 
		//color for background and foreground (to avoid theme colors injection...)
		headerField.setFontColor(Color.WHITE); 
		headerField.setBackgroundColor(Color.BLACK); 
		return (Field)headerField;
	}
	
	public void setTitleText(String title){
		((HeaderField)titleField).setTitle(title);
	}
    
	//common margin
    protected XYEdges margins = new XYEdges(5,5,5,5);
    

    protected BasicEditField getDescriptionTextField(String text) {
    	BasicEditField field = new  BasicEditField(BasicEditField.READONLY){
    	    public void paint(Graphics graphics)
    		    {
    		        graphics.setColor(Color.GRAY);
    		        super.paint(graphics);
    		    }
    		};
    	  	
    		Font fnt = this.getFont().derive(Font.ITALIC);
    	  	field.setFont(fnt);
    	  	
    	  	field.setText(text);
    	  	return field;
    }
    
    //commentContent = new  BasicEditField(BasicEditField.READONLY);
    
    protected LabelField getLabel(String label) {
		
		LabelField lblField = new LabelField(label + " ")
		{
		    public void paint(Graphics graphics)
		    {
		        graphics.setColor(Color.GRAY);
		        super.paint(graphics);
		    }
		};
	  	Font fnt = this.getFont().derive(Font.BOLD);
	  	lblField.setFont(fnt);
		return lblField;
	}

    //TODO: refactor of this 2 methods
    protected LabelField getLabel(String label, long style) {
		
		LabelField lblField = new LabelField(label , style)
		{
		    public void paint(Graphics graphics)
		    {
		        graphics.setColor(Color.GRAY);
		        super.paint(graphics);
		    }
		};
	  	Font fnt = this.getFont().derive(Font.BOLD);
	  	lblField.setFont(fnt);
		return lblField;
	}
}