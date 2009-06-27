package com.wordpress.view;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYEdges;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.view.component.HeaderField;

/**
 * Base class for all Application Screen
 * @author dercoli
 *
 */
public abstract class BaseView extends MainScreen{
	
	protected Field titleField; //main title of the screen
	
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
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
	/*	LabelField titleField = new LabelField(title, LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH | LabelField.HCENTER);
		Font fnt = this.getFont().derive(Font.BOLD);
		titleField.setFont(fnt);
		return titleField;*/
		HeaderField headerField = new HeaderField(title);
		return (Field)headerField;
	}
    
	//common margin
    protected XYEdges margins = new XYEdges(5,5,5,5);
    
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
	
	
}