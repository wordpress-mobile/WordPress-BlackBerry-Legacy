/*-
 * Copyright (c) 2008, Derek Konigsberg
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the project nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 * Modified by Danilo Ercoli, ercoli@gmail.com, 
 * on 30 september 2009
 */

package com.wordpress.view.container;


import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;

/**
 * Provides a container that emulates the behavior of a vertical field
 * manager, adding a border to the enclosed fields.
 * 
 * note: Each time a field is added, the manager adds a FocusChangeListener to the field!
 * This is a trick around focus-lost repaint issue, when background image was set into Main Manager.
 */
public class BorderedFieldManager extends Manager {
	/** The border width. */
	private static int borderWidth = 6;
	
	/**
	 * Show a normal border on the bottom.
	 */
	public static long BOTTOM_BORDER_NORMAL = 0x0000000000000L;
	
	/**
	 * Do not show a border on the bottom.
	 * Used to eliminate excessive border space on
	 * vertically stacked instances. 
	 */
	public static long BOTTOM_BORDER_NONE = 0x0000000000020L;
	
	/**
	 * Show a line on the bottom of the border.
	 */
	public static long BOTTOM_BORDER_LINE = 0x0000000000040L;
	
	private boolean bottomBorderNone;
	private boolean bottomBorderLine;
	
	/**
	 * Instantiates a new bordered field manager.
	 */
	public BorderedFieldManager() {
        super(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL | BOTTOM_BORDER_NORMAL);
    	long style = this.getStyle();
        bottomBorderNone = ((style & BOTTOM_BORDER_NONE) == BOTTOM_BORDER_NONE);
        bottomBorderLine = ((style & BOTTOM_BORDER_LINE) == BOTTOM_BORDER_LINE);
    }

    /**
     * Instantiates a new bordered field manager.
     * 
     * @param style the style
     */
    public BorderedFieldManager(long style) {
        super(style);
        bottomBorderNone = ((style & BOTTOM_BORDER_NONE) == BOTTOM_BORDER_NONE);
        bottomBorderLine = ((style & BOTTOM_BORDER_LINE) == BOTTOM_BORDER_LINE);
    }

    /* (non-Javadoc)
     * @see net.rim.device.api.ui.Manager#paint(net.rim.device.api.ui.Graphics)
     */
    protected void paint(Graphics graphics) {
        int width = this.getWidth();
        int height = this.getHeight();
        int foregroundColor = graphics.getColor();
        int backgroundColor = graphics.getBackgroundColor();

        // Paint the fill for the field
     //   graphics.setColor(Color.LIGHTGREY);
    //    graphics.fillRect(0, 0, width, height);

        // Paint the rounded rectangular cutout section for the contents
        graphics.setColor(Color.WHITE);
        graphics.fillRoundRect(borderWidth, borderWidth, 
        		width - (borderWidth * 2), height - (bottomBorderNone ? borderWidth : (borderWidth * 2)), 15, 15);

        // Paint the inner border of the cutout section
        graphics.setColor(Color.DARKGRAY);
        graphics.drawRoundRect(borderWidth, borderWidth, 
        		width - (borderWidth * 2), height - (bottomBorderNone ? borderWidth : (borderWidth * 2)), 15, 15);

        if(bottomBorderLine) {
        	graphics.drawLine(0, height - 1, width - 1, height - 1);
        }
        
        // Resume normal painting of the contents
        graphics.setColor(foregroundColor);
        super.paint(graphics);
    }
    
    /* (non-Javadoc)
     * @see net.rim.device.api.ui.Manager#sublayout(int, int)
     */
    protected void sublayout(int maxWidth, int maxHeight) {
        int count = this.getFieldCount();
        int y = borderWidth+2;
        for(int i=0; i<count; i++) {
        	y += 2;
            Field field = this.getField(i);
            this.setPositionChild(field, borderWidth*2, y + field.getMarginTop());
            this.layoutChild(field, maxWidth - (borderWidth * 4) , 
            		getPreferredHeightOfChild(field) );
            y += field.getHeight() + field.getMarginTop() + field.getMarginBottom();
        }
        setExtent(maxWidth, getPreferredHeight());
    }
    
    /* (non-Javadoc)
     * @see net.rim.device.api.ui.Field#getPreferredWidth()
     */
    public int getPreferredWidth() {
    	return Display.getWidth();
    }
    
    /* (non-Javadoc)
     * @see net.rim.device.api.ui.Field#getPreferredHeight()
     */
    public int getPreferredHeight() {
        int sum = (bottomBorderNone ? borderWidth : (borderWidth * 2));
        sum+=2;
        int count = this.getFieldCount();
        for(int i=0; i<count; i++) {
    		sum += 2;
    		Field tmpField = this.getField(i);
            sum += tmpField.getHeight() + tmpField.getMarginBottom() + tmpField.getMarginTop() ;
        }
        sum += 2+2;
        return sum;
    }
    
    
	public void add( Field field ) {
		super.add( field );
		 //Each field can have only one focus listener object. If you want to provide a new focus listener for a field, 
		 //you must first invoke this method with null remove the old listener.
		field.setFocusListener(null);
		field.setFocusListener(new BorderedFocusChangeListenerPatch()); //add the focus change listener patch
	}
	
}