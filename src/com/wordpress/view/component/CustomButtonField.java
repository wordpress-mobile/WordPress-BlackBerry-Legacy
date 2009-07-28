package com.wordpress.view.component;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYPoint;
import net.rim.device.api.ui.XYRect;

public class CustomButtonField extends Field
{
    private String label;
	private Bitmap bitmap; //this is not null only into bitmap button
    private int labelLength;       
    private int width;
    private int height;
    private int alignment;    
    private XYPoint labelTopLeftPoint;    
    private boolean isFocusable;
    
    /**
     * Margin for the button
     */
    private final static int DEFAULT_LEFT_MARGIN = 1;
    private final static int DEFAULT_RIGHT_MARGIN = 1;
    private final static int DEFAULT_TOP_MARGIN = 4;
    private final static int DEFAULT_BOTTOM_MARGIN = 4;
    
    /**
     * Padding for the button
     */
    private final static int DEFAULT_LEFT_PADDING = 5;
    private final static int DEFAULT_RIGHT_PADDING = 5;
    private final static int DEFAULT_TOP_PADDING = 4;
    private final static int DEFAULT_BOTTOM_PADDING = 4;
    
    /**
     * Margins around the text box
     */
    private int leftMargin = DEFAULT_LEFT_MARGIN;
    private int rightMargin = DEFAULT_RIGHT_MARGIN;
    private int topMargin = DEFAULT_TOP_MARGIN;
    private int bottomMargin = DEFAULT_BOTTOM_MARGIN;
    
    /**
     * Padding around the text box
     */
    private int leftPadding = DEFAULT_LEFT_PADDING;
    private int rightPadding = DEFAULT_RIGHT_PADDING;
    private int topPadding = DEFAULT_TOP_PADDING;
    private int bottomPadding = DEFAULT_BOTTOM_PADDING;
    
    /**
     * Alignment
     */
     public final static int ALIGNMENT_LEFT = 0x00000001;
     public final static int ALIGNMENT_RIGHT = 0x00000002;
     public final static int ALIGNMENT_TOP = 0x00000004;
     public final static int ALIGNMENT_BOTTOM = 0x00000008;
     public final static int ALIGNMENT_CENTER = 0x00000010;
     
     
    public final static int DEFAULT_BACKGROUND_COLOR_NORMAL = 0x00ffffff;
    public final static int DEFAULT_BACKGROUND_COLOR_ON_FOCUS = 0x009c0000;
    private int backgroundColorNormal = DEFAULT_BACKGROUND_COLOR_NORMAL;
    private int backgroundColorOnFocus = DEFAULT_BACKGROUND_COLOR_ON_FOCUS;

   public final static int ALIGNMENT_DEFAULT = ALIGNMENT_LEFT | ALIGNMENT_TOP;
   
  
    public CustomButtonField(final String label)
    {
        this(label, 0);
    }
    
    public CustomButtonField(final String label, int width)
    {
        super();
        
        this.label = (label == null) ? "" : label;       
        
        this.isFocusable = true;
        Font font = getFont();
        
        labelLength = font.getAdvance(this.label);
        
        this.width = (width != 0) ? width : (labelLength + leftMargin + leftPadding + rightPadding + rightMargin);
        this.height = font.getHeight() + topMargin + topPadding + bottomPadding + bottomMargin;
        
        labelTopLeftPoint = new XYPoint();
        
        setAlignment(ALIGNMENT_DEFAULT);
    }
    
    public CustomButtonField(final Bitmap bitmap)
    {
        super();
        
        this.label = null;
        this.bitmap = bitmap;

        this.isFocusable = true;
        
        this.width = bitmap.getWidth() + leftMargin + leftPadding + rightPadding + rightMargin;
        this.height = bitmap.getHeight() + topMargin + topPadding + bottomPadding + bottomMargin;
        
        labelTopLeftPoint = new XYPoint();
        
        setAlignment(ALIGNMENT_DEFAULT);
    }
      
    
    public void setWidth(int width)
    {
        int displayWidth = Display.getWidth();
        
        if (width > 0 && width <= displayWidth)
        {
            this.width = width;                       
            adjustAlignment();
        }
    }    
  
    public void setWidth(String refStr)
    {
        this.labelLength = getFont().getAdvance(refStr);
        int tempWidth = leftMargin + leftPadding +  labelLength + rightPadding + rightMargin;
        
        setWidth(tempWidth);
    }
   
    public void setHeight(int height)
    {
        this.height = height;
    }
      
    public void setSize(int width, int height)
    {
        setWidth(width);
        setHeight(height);
    }    
   
    public void setAlignment(int alignment)
    {
        if ((alignment & ALIGNMENT_CENTER) != 0)
        {
            this.alignment = alignment;
        }
        else
        {
            this.alignment = 0;
            
            if ((alignment & ALIGNMENT_RIGHT) != 0)
            {
                this.alignment |= ALIGNMENT_RIGHT;
            }
            else
            {
                this.alignment |= ALIGNMENT_LEFT;
            }
                    
            // Vertical alignment
            if ((alignment & ALIGNMENT_BOTTOM) != 0)
            {
                this.alignment |= ALIGNMENT_BOTTOM;
            }
            else
            {
                this.alignment |= ALIGNMENT_TOP;
            }
        }        
        adjustAlignment();
    }
    
    private void adjustAlignment()
    {
        int leftBlankSpace = leftMargin + leftPadding;
        int rightBlankSpace = rightPadding + rightMargin;
       
        int topBlankSpace = topMargin + topPadding;
        int bottomBlankSpace = bottomMargin + bottomPadding;
        
        if ((alignment & ALIGNMENT_CENTER) != 0)
        {
            int emptySpace = width - (leftBlankSpace + labelLength + rightBlankSpace);
            
            labelTopLeftPoint.y = topBlankSpace;
            labelTopLeftPoint.x = leftBlankSpace + emptySpace/2;
        }
        else
        {
            // Horizontal alignment
            if ((alignment & ALIGNMENT_LEFT) != 0)
            {
                labelTopLeftPoint.x = leftBlankSpace;
            }
            else if ((alignment & ALIGNMENT_RIGHT) != 0)
            {       
                labelTopLeftPoint.x = width - (labelLength + rightBlankSpace);
            }
            labelTopLeftPoint.y = topBlankSpace;
        }
    }
    
    public String getText()
    {
        return label;
    }
    
    public int getButtonWidth()
    {
        return width;
    }
    
    public void setLeftMargin(int leftMargin)
    {
        if (leftMargin >= 0)
        {
            this.width -= this.leftMargin;
            this.leftMargin = leftMargin;
            this.width += this.leftMargin;
            
            adjustAlignment();
        }
    }
    
    public void setRightMargin(int rightMargin)
    {
        if (rightMargin >= 0)
        {
            this.width -= this.rightMargin;
            this.rightMargin = rightMargin;
            this.width += this.rightMargin;
            
            adjustAlignment();
        }
    }
    
  
    public void setTopMargin(int topMargin)
    {
        if (topMargin >= 0)
        {
            this.height -= this.topMargin;
            this.topMargin = topMargin;
            this.height += this.topMargin;
            adjustAlignment();
        }
    }
    
   
    public void setBottomMargin(int bottomMargin)
    {
        if (bottomMargin >= 0)
        {
            this.height -= this.bottomMargin;
            this.bottomMargin = bottomMargin;
            this.height -= this.bottomMargin;
            
            adjustAlignment();
        }
    }
        
    public void setMargin(int topMargin, int rightMargin, int bottomMargin,int leftMargin)
    {
        setLeftMargin(leftMargin);
        setRightMargin(rightMargin);
        setTopMargin(topMargin);
        setBottomMargin(bottomMargin);
    }
  
    public void setFocusable(boolean isFocusable)
    {
        this.isFocusable = isFocusable;
    }
    
    public int getPreferredWidth()
    {
        return width;
    }
    
    public int getPreferredHeight()
    {
        return height;
    }
    
    protected void layout(int width, int height)
    {
        setExtent(Math.min(getPreferredWidth(), width), Math.min(getPreferredHeight(), height));
    }
    
    protected void paint(Graphics graphics)
    {
        int w = width - (leftMargin + rightMargin);
        int h = height - (topMargin + bottomMargin);        
    
        if(isFocus() == false)
        {
            graphics.setColor(backgroundColorNormal);
            graphics.fillRoundRect(leftMargin, topMargin, w, h, 6, 6);
            graphics.setColor(0x00394142);
            graphics.drawRoundRect(leftMargin, topMargin, w, h, 6, 6);
            if (label != null)
            	graphics.drawText(label,  labelTopLeftPoint.x, labelTopLeftPoint.y);
            else {
            	int width = bitmap.getWidth();
        		int height = bitmap.getHeight();
            	graphics.drawBitmap( labelTopLeftPoint.x, labelTopLeftPoint.y, width, height, bitmap, 0, 0);
            }
        }
        else
        {            
            graphics.setColor(backgroundColorOnFocus);
            graphics.fillRoundRect(leftMargin, topMargin, w, h, 6, 6);
            graphics.drawRoundRect(leftMargin, topMargin, w, h, 6, 6);
            
            graphics.setColor(0x00ffffff);
            if (label != null)
            	graphics.drawText(label,  labelTopLeftPoint.x, labelTopLeftPoint.y);
            else {
            	int width = bitmap.getWidth();
        		int height = bitmap.getHeight();
            	graphics.drawBitmap( labelTopLeftPoint.x, labelTopLeftPoint.y, width, height, bitmap, 0, 0);
            }
            	
        }        
    }

    public boolean isFocusable()
    {
        return isFocusable;
    }

    public void getFocusRect(XYRect rect)
    {
        rect.set(leftMargin, topMargin, width - (leftMargin + rightMargin), height - (topMargin + bottomMargin));
    }
    
    protected void drawFocus(Graphics graphics, boolean on)
    {
        invalidate();
    }
    
    public boolean keyChar(char key, int status, int time)
    {
        if (key == Characters.ENTER)
        {
            fieldChangeNotify(0);
            return true;
        }
        
        return false;
    }
    
    protected boolean navigationClick(int status, int time) 
    {
        fieldChangeNotify(0);
        return true;
    }    
}