package com.wordpress.view.component;

import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.LabelField;

public class ColoredLabelField extends LabelField
{ 	
	int _foregroundColor;
	int _foregroundColorFocus;

	
	public ColoredLabelField(String label) {
		this(label, Color.BLACK);
	}
	
	public ColoredLabelField( String label, int foregroundColor) {
		super(label);
		_foregroundColor = foregroundColor;
		_foregroundColorFocus = _foregroundColor;
	}
	
	public ColoredLabelField( String label, int foregroundColor, long style ) {
		this( label, foregroundColor, foregroundColor, style );
	}
	
	public ColoredLabelField( String label, int foregroundColor, int foregroundColorFocus, long style ) {
		super( label, style );
		_foregroundColor = foregroundColor;
		_foregroundColorFocus = foregroundColorFocus;
	}

	protected void paint( Graphics g ) 
    {
        int oldColour = g.getColor();
        try {
            g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? _foregroundColorFocus : _foregroundColor );
            super.paint( g );
        } finally {
            g.setColor( oldColour );
        }
    }
}