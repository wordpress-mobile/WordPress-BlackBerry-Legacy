package com.wordpress.view.component;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.container.HorizontalFieldManager;

public class HorizontalPaddedFieldManager extends HorizontalFieldManager {
	    

	public HorizontalPaddedFieldManager() {
		 super(Manager.NO_HORIZONTAL_SCROLL | Manager.NO_VERTICAL_SCROLL);
	}

	public HorizontalPaddedFieldManager(long style) {
		super(style);
	}

	final static int PADDING = 4;
	
    protected void sublayout( int width, int height ) {
        int x = 0;
        int y = 5;
        int availableWidth = width;

        for (int i = 0;  i < getFieldCount();  i++) {
            
        	Field field = getField(i);    
        	availableWidth= availableWidth - PADDING; //remove the space occupied by padding from available width
            layoutChild( field, availableWidth,  getPreferredHeightOfChild(field));
            availableWidth= availableWidth - (field.getWidth()); 
            
            setPositionChild(field, x+(PADDING/2), y);
                                  	
            x += field.getWidth()+(PADDING/2) ;
            y = 5;
        }
     //setExtent(width, height);
     setExtent(width, getPreferredHeight()+10);
    } 
    
    
    public int getPreferredHeight() {
    	int max = 0;
        int count = this.getFieldCount();
        for(int i=0; i<count; i++) {
        	if(max < this.getField(i).getHeight())
        		max = this.getField(i).getHeight();
        }
        return max;
    }

}
