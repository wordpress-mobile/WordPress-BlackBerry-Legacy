package com.wordpress.view;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.util.LongIntHashtable;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.Tools;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ClickableLabelField;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.EmbossedButtonField;


public class GUIFactory {

	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}

	public static int BTN_COLOUR_BACKGROUND_FOCUS = 0x21759b;
	public static int LIST_COLOUR_BACKGROUND_FOCUS = 0x21759b;//0xe7f3ff; 	
	
	public static synchronized BaseButtonField createButton(String label, long style) {
		LongIntHashtable colourTable = new LongIntHashtable();
		colourTable.put(EmbossedButtonField.COLOUR_BACKGROUND_FOCUS, BTN_COLOUR_BACKGROUND_FOCUS);
		EmbossedButtonField btn = new EmbossedButtonField(label, style, colourTable);
        btn.setMargin(0, 4, 0, 4);
		return btn;
	}
	
	 protected static synchronized BasicEditField getDescriptionTextField(String text) {
    	BasicEditField field = new  BasicEditField(BasicEditField.READONLY){
    	    public void paint(Graphics graphics)
    		    {
    		        graphics.setColor(Color.GRAY);
    		        super.paint(graphics);
    		    }
    		};
    	  	
    		Font fnt = Font.getDefault().derive(Font.ITALIC);
    	  	field.setFont(fnt);
    	  	
    	  	field.setText(text);
    	  	return field;
    }
	
	 public static synchronized SeparatorField createSepatorField() {
		 SeparatorField sep = new SeparatorField() {
				protected void paint( Graphics g ) 
			    {
			        int oldColour = g.getColor();
			        try {
			            g.setColor( Color.GRAY );
			            super.paint( g );
			        } finally {
			            g.setColor( oldColour );
			        }
			    }
		 };
		 return sep;
	 }
	 
	 public static synchronized LabelField getLabel(String label, int color) {
			LabelField lblField = new ColoredLabelField(label + " ", color);
		  	Font fnt = Font.getDefault().derive(Font.BOLD);
		  	lblField.setFont(fnt);
			return lblField;
	}
	 
	 public static synchronized LabelField getLabel(String label,int fgColor, long style) {
		 LabelField lblField = new ColoredLabelField(label + " ", fgColor, style);
		 Font fnt = Font.getDefault().derive(Font.BOLD);
		 lblField.setFont(fnt);
		 return lblField;
	 }	
	 
	 public static synchronized LabelField createURLLabelField(String label, final String url, long style) {
		 ClickableLabelField clickableLabelField = new ClickableLabelField(label, LabelField.FOCUSABLE | style );
		 clickableLabelField.setTextColor(Color.BLUE);
		 clickableLabelField.setContextMenuText(_resources.getString(WordPressResource.LABEL_VISIT_SITE));
		 
		 FieldChangeListener listener = new FieldChangeListener() {
			 public void fieldChanged(Field field, int context) {
				 Tools.openURL(url);
			 }
		 };
		 clickableLabelField.setChangeListener(listener);
		 return clickableLabelField;
	 }
	 
}
