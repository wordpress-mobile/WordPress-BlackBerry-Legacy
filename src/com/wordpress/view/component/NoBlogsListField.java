package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.BlogInfo;

/**
 * This class is a list field that is used only when there are NO blog in the Main Screen.
 * 
 * @author dercoli
 *
 */

public class NoBlogsListField {
    
	private ListField _listField;
    private ListCallBack listFieldCallBack = null;
    
  //create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
       
    public NoBlogsListField() {  
    	listFieldCallBack = new ListCallBack();
    	
    	_listField = new ListField() {
    		protected void drawFocus(Graphics graphics, boolean on) { } //remove the standard focus highlight
    	};
    	
    	_listField.setRowHeight(48);//the others lists have rows of 42pixels height. added 6 pixel of blank space for each row
    	//Set the ListFieldCallback
    	_listField.setCallback(listFieldCallBack);
    	
    	_listField.insert(0); //add the only element to the list
    }
        
    
    public ListField getList() {
		return _listField;
	}
    
    private class ListCallBack extends BasicListFieldCallBack {
		
        // Draws the list row.
    	public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) {
    		Font originalFont = graphics.getFont();
    		int originalColor = graphics.getColor();
    		int height = list.getRowHeight();
    		
			/*
			 * 42px of row
			 * 6px blank space
			 */
			height = height - 6;
			w = w - 10;
			//y = y+3;
    		
    		//drawXXX(graphics, 0, y, width, listField.getRowHeight());
    		drawBackground(graphics, 5, y, w, height, _listField.getSelectedIndex() ==  index);
    		drawBorder(graphics, 5, y, w, height);
    		
    		int leftImageWidth = 0;			 
   				 
            drawText(graphics, leftImageWidth+5, y, w  - leftImageWidth, height, _resources.getString(WordPressResource.LABEL_ADD_YOUR_BLOG), true);

            graphics.setFont(originalFont);
            graphics.setColor(originalColor);
    	}
        
    	
		protected void drawBorder(Graphics graphics, int x, int y, int width,	int height) {
			graphics.setColor(Color.GRAY);
			graphics.drawLine(x-1, y , x + width-1, y);
			
			graphics.drawLine(x-1, y, x-1 , y + height-1); //linea verticale sx
			graphics.drawLine(x + width, y-1, x + width , y + height-1); //linea verticale dx
			
			graphics.drawLine(x-1, y + height - 1, x + width-1, y + height - 1);
		}


		public Object get(ListField listField, int index) {
			return null;
		}
    	    	
    }

} 

