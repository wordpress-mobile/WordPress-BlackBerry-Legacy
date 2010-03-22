package com.wordpress.view.component;


import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

public class HeaderField extends Field {
    private Font titleFont;
    private Font subTitleFont;
    private String title;
    private String subTitle = null;
    private boolean showTitle;
    private int fieldWidth;
    private int fieldHeight;
    private int fontColor;
    private int backgroundColor;
    
    private final int[] upperDrawColors;
	private int[] X_PTS;
	private int[] Y_PTS;
    
    public HeaderField(String title) {
        super(Field.NON_FOCUSABLE);
        this.title =title;
        this.showTitle = true;
        this.fontColor = -1;
        this.subTitleFont = Font.getDefault().derive(Font.PLAIN);
        this.titleFont = Font.getDefault().derive(Font.BOLD);
        this.backgroundColor = -1;
        
        this.fieldWidth = Display.getWidth();
        calculateFieldHeight();
        upperDrawColors = new int[]{0xdeebff, 0x9cbeff, 0x9cbeff, 0xdeebff};
        X_PTS = new int[]{0, 0, fieldWidth, fieldWidth};
        Y_PTS = new int[]{0, fieldHeight-1, fieldHeight-1, 0};  
    }

    
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }
    
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        invalidate();
    }
    
    public void setFontColor(int fontColor) {
        this.fontColor = fontColor;
        invalidate();
    }
    
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        invalidate();
    }

    
    public void showTitle(boolean bool) {
        showTitle = bool;
        invalidate();
    }
    
    protected void layout(int width, int height) {
    	this.fieldWidth = Display.getWidth();
    	calculateFieldHeight();
        X_PTS = new int[]{0, 0, fieldWidth, fieldWidth};
        Y_PTS = new int[]{0, fieldHeight-1, fieldHeight-1, 0};  
        setExtent(getPreferredWidth(), getPreferredHeight());
    }
    
    public int getPreferredWidth() {
        return fieldWidth;
    }
    
    public int getPreferredHeight() {
    	return fieldHeight;
    }
    
    private void calculateFieldHeight() {
    	if(subTitle == null || subTitle.equalsIgnoreCase(""))
    		this.fieldHeight =  titleFont.getHeight()+2;
    	else
    		this.fieldHeight = titleFont.getHeight() + subTitleFont.getHeight() + 4;
    }
    
    protected void paint(Graphics graphics) {
        if(fontColor == -1) {
            fontColor = graphics.getColor();
        }
        graphics.setFont(titleFont);
        int preferredWidth = this.getPreferredWidth();
        int preferredHeight = this.getPreferredHeight();
        
        graphics.drawShadedFilledPath(X_PTS, Y_PTS, null, upperDrawColors, null);
        
        if(backgroundColor != -1) {
            graphics.setColor(backgroundColor);
            graphics.drawLine(0, preferredHeight - 1 , preferredWidth, preferredHeight - 1);
        } else {
        	graphics.setColor(Color.GRAY);
        	graphics.drawLine(0, preferredHeight - 1, preferredWidth, preferredHeight - 1);           
        }
       
        graphics.setColor(fontColor);
        
        if(showTitle) {
            graphics.drawText(title, 1, 0, DrawStyle.HCENTER | DrawStyle.ELLIPSIS, preferredWidth);
        }
        
        graphics.setColor(Color.GRAY);
        graphics.setFont(subTitleFont);
        if (subTitle != null && !subTitle.equalsIgnoreCase("")) {
        	graphics.drawText(subTitle, 1, titleFont.getHeight()+2, DrawStyle.HCENTER | DrawStyle.ELLIPSIS, preferredWidth);
        }
        
    }
}