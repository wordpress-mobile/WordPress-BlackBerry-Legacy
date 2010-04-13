package com.wordpress.view.container;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.container.VerticalFieldManager;

/**
 * Provides a container that emulates the behavior of a vertical field
 * manager, adding a pad to the enclosed fields.
 */
public class VerticalPaddedFieldManager extends VerticalFieldManager {
	
	/** The border width. */
	private static int borderWidth = 4;
	
		
	/**
	 * Instantiates a new bordered field manager.
	 */
	public VerticalPaddedFieldManager() {
        super();
    }

    /**
     * Instantiates a new bordered field manager.
     * 
     * @param style the style
     */
    public VerticalPaddedFieldManager(long style) {
        super(style);
    }

    
    /* (non-Javadoc)
     * @see net.rim.device.api.ui.Manager#sublayout(int, int)
     */
    protected void sublayout(int maxWidth, int maxHeight) {
        int count = this.getFieldCount();
        int y = borderWidth;
        for(int i=0; i<count; i++) {
    		y += 2;
            Field field = this.getField(i);
            this.setPositionChild(field, 8, y);
            this.layoutChild(field, maxWidth - (borderWidth * 2) - 6, getPreferredHeightOfChild(field));
            y += field.getHeight();
        }
        setExtent(maxWidth, getPreferredHeight());
    }
    
    
    
    /* (non-Javadoc)
     * @see net.rim.device.api.ui.Field#getPreferredHeight()
     */
    public int getPreferredHeight() {
        int sum = (borderWidth * 2);
        int count = this.getFieldCount();
        for(int i=0; i<count; i++) {
    		sum += 2;
            sum += this.getField(i).getHeight();
        }
        sum += 2;
        return sum;
    }
}
