package com.wordpress.view.component;


import net.rim.device.api.system.Display;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

public class HeaderField extends Field {

	private String title;
	private Font titleFont;
	private int titleFontColor;

	private String subTitle = null;
    private Font subTitleFont;
    private int subTitleFontColor;
    
    private int[] X_PTS;
	private int[] Y_PTS;
	private int[] upperDrawColors;
        
    private int fieldWidth;
    private int fieldHeight;
    
    public HeaderField(String title) {
        super(Field.NON_FOCUSABLE);
        this.title =title;
        this.subTitleFont = Font.getDefault().derive(Font.PLAIN);
        this.titleFont = Font.getDefault().derive(Font.PLAIN);
        
        upperDrawColors = new int[]{0x464646, 0x414141, 0x414141, 0x464646};
        this.titleFontColor = 0xcccccc;
		this.subTitleFontColor = 0x21759b;
            
        this.fieldWidth = Display.getWidth();
        calculateFieldHeight();
        X_PTS = new int[]{0, 0, getWidth(), getWidth()}; 
        Y_PTS = new int[]{0, getHeight(), getHeight(), 0};
    }
    
    public void setTitle(String title) {
        this.title = title;
        invalidate();
    }
    
    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
        invalidate();
    }
         
    protected void layout(int width, int height) {
    	this.fieldWidth = Display.getWidth();
    	calculateFieldHeight();
        setExtent(getPreferredWidth(), getPreferredHeight());
     
        X_PTS = new int[]{0, 0, getWidth(), getWidth()};
        Y_PTS = new int[]{0, getHeight(), getHeight(), 0};
    }
    
    public int getPreferredWidth() {
        return fieldWidth;
    }
    
    public int getPreferredHeight() {
    	return fieldHeight;
    }
   
    private void calculateFieldHeight() {
    	if(subTitle == null || subTitle.equalsIgnoreCase(""))
    		this.fieldHeight =  titleFont.getHeight() + 6;
    	else
    		this.fieldHeight = titleFont.getHeight() + subTitleFont.getHeight() + 6 + 2;
    }
    
	protected void paintBackground(Graphics graphics) {
		graphics.drawShadedFilledPath(X_PTS, Y_PTS, null, upperDrawColors, null);
		graphics.setColor(0x363636);
		graphics.drawLine(0, Y_PTS[1]-1 , X_PTS[2], Y_PTS[1]-1);
	}
    
    protected void paint(Graphics graphics) {
        
    	graphics.setFont(titleFont);
        graphics.setColor(titleFontColor);       
        graphics.drawText(title, 1, 3, DrawStyle.HCENTER | DrawStyle.ELLIPSIS, getWidth());
        
        if (subTitle != null && !subTitle.equalsIgnoreCase("")) {
        	graphics.setColor(subTitleFontColor);
        	graphics.setFont(subTitleFont);
        	graphics.drawText(subTitle, 1, titleFont.getHeight()+2+3, DrawStyle.HCENTER | DrawStyle.ELLIPSIS, getWidth());
        }
    }
}