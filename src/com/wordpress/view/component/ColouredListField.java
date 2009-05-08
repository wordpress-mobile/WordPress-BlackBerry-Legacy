package com.wordpress.view.component;

import net.rim.device.api.ui.*;
import net.rim.device.api.ui.component.*;
import net.rim.device.api.ui.Graphics;

public class ColouredListField extends ListField 
{

    private static final int LIGHT_TEXT  = Color.WHITE;
    private static final int DARK_TEXT  = Color.BLACK;
    private static final int BACKGROUND  = Color.DARKBLUE;
    private static final int HIGHLIGHT   = Color.BLUE;
    private final int[] cols = new int[]{BACKGROUND,HIGHLIGHT,LIGHT_TEXT,BACKGROUND};
    private boolean hasFocus;   //=false

    public ColouredListField() 
    {
        super();
    }
    
    public ColouredListField(int size)
    {
        super(size);
    }

    public ColouredListField(int numRows, long style) 
    {
        super(numRows,style);
    }

    //Handles moving the focus within this field.
    public int moveFocus(int amount, int status, int time) 
    {
    	int selectedIndex = getSelectedIndex();
        invalidate(selectedIndex);
        
        if (selectedIndex == getSize()-2) {
        	if( amount > 0) //block forward scrolling 
        		return 0; 
        	else
        		return super.moveFocus(2*amount,status,time); //scrolling double
        }
        return super.moveFocus(2*amount,status,time); //scrolling double
    }

    //Invoked when this field receives the focus.
    public void onFocus(int direction) 
    {
        hasFocus = true;
        super.onFocus(direction);
    }

    //Invoked when a field loses the focus.
    public void onUnfocus() 
    {
        hasFocus = false;
        super.onUnfocus();
        invalidate();
    }

    //Over ride paint to produce the alternating colours.
    public void paint(Graphics graphics) 
    {
        //Get the current clipping region as it will be the only part that requires repainting
        XYRect redrawRect = graphics.getClippingRect();
        if(redrawRect.y < 0)
        {
            throw new IllegalStateException("Clipping rectangle is wrong.");
        }

        //Determine the start location of the clipping region and end.
        int rowHeight = getRowHeight();

        int curSelected;
        
        //If the ListeField has focus determine the selected row.
        if (hasFocus) 
        {
             curSelected = getSelectedIndex();
        } 
        else 
        {
            curSelected = -1;
        }

        int startLine = redrawRect.y / rowHeight;
        int endLine = (redrawRect.y + redrawRect.height - 1) / rowHeight;
        endLine = Math.min(endLine, getSize() - 1);
        int y = startLine * rowHeight;

        //Setup the data used for drawing.
        int[] yInds = new int[]{y, y, y + rowHeight, y + rowHeight};
        int[] xInds = new int[]{0, getPreferredWidth(), getPreferredWidth(), 0};

        //Get the ListFieldCallback.
        //This sample assumes that the object returned by the get
        //method of the callback is a String or has a toString method.
        //If this is not the case you will need to add the required logic
        //for your implementation.
        ListFieldCallback callBack = this.getCallback();

        //Draw each row
        for(; startLine <= endLine; ++startLine) 
        {               
            if (startLine % 2 == 0 && startLine != curSelected) 
            {
             //Draw the even and non selected rows.
                graphics.setColor(LIGHT_TEXT);
                graphics.drawShadedFilledPath(xInds, yInds, null, cols, null);
                graphics.drawText((String)callBack.get(this, startLine), 0, yInds[0]);
                graphics.setColor(DARK_TEXT);
            } 
            else 
            {
                //Draw the odd or selected rows.
                graphics.drawText((String)callBack.get(this, startLine), 0, yInds[0]);
            }
            
            //Assign new values to the y axis moving one row down.
            y += rowHeight;
            yInds[0] = y;
            yInds[1] = yInds[0];
            yInds[2] = y + rowHeight;
            yInds[3] = yInds[2];
        }
    }
}

