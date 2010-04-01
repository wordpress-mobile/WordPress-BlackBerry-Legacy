package com.wordpress.view;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.util.LongIntHashtable;

import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.EmbossedButtonField;

public class GUIFactory {

	protected static int BTN_COLOUR_BACKGROUND_FOCUS = 0x5292f7;

	
	
	protected static synchronized BaseButtonField createButton(String label, long style) {
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
	
	 protected static synchronized SeparatorField createSepatorField() {
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
	 
	 
	 protected static synchronized LabelField getLabel(String label, int color) {
			LabelField lblField = new ColoredLabelField(label + " ", color);
		  	Font fnt = Font.getDefault().derive(Font.BOLD);
		  	lblField.setFont(fnt);
			return lblField;
		}

	 protected static synchronized LabelField getLabel(String label) {
			LabelField lblField = new ColoredLabelField(label + " ", Color.GRAY);
		  	Font fnt = Font.getDefault().derive(Font.BOLD);
		  	lblField.setFont(fnt);
			return lblField;
		}
	
	 protected static synchronized LabelField getLabel(String label, long style) {
		 LabelField lblField = new ColoredLabelField(label + " ", Color.GRAY, style);
		 Font fnt = Font.getDefault().derive(Font.BOLD);
		 lblField.setFont(fnt);
		 return lblField;
	 }	
}
