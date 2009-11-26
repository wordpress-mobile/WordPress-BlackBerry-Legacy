package com.wordpress.view.component;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;


public abstract class BasicListFieldCallBack implements ListFieldCallback {
	
    protected Bitmap bg = Bitmap.getBitmapResource("bg_light.png");
	protected Bitmap bgSelected = Bitmap.getBitmapResource("bg_blue.png");
	protected static final int PADDING = 2;
    
	
    protected int drawLeftImage(Graphics graphics, int x, int y, int height, EncodedImage leftImage) {
        
        int imageWidth = leftImage.getWidth();
        int imageHeight = leftImage.getHeight();
        int imageTop = y + ((height - imageHeight) / 2);
        int imageLeft = x + ((height - imageWidth) / 2);

        // Image on left side -- draw only the first frame
        graphics.drawImage(imageLeft, imageTop, imageWidth, imageHeight, leftImage, 0, 0, 0);

        return height;
    }
    
    
    protected int drawLeftImage(Graphics graphics, int x, int y, int height, Bitmap leftImage) {
        
        int imageWidth = leftImage.getWidth();
        int imageHeight = leftImage.getHeight();
        int imageTop = y + ((height - imageHeight) / 2);
        int imageLeft = x + ((height - imageWidth) / 2);

        // Image on left side
        graphics.drawBitmap(imageLeft, imageTop, imageWidth, imageHeight, leftImage, 0, 0);

        return height;
    }
    
    protected int drawRightImage(Graphics graphics, int y, int width, int height, Bitmap toDraw) {
        // Image on right side

        int imageWidth = 0;
        int imageHeight;
        int imageTop;
        int imageLeft;
        imageWidth = toDraw.getWidth();
        imageHeight = toDraw.getHeight();

        imageTop = y + ((height - imageHeight) / 2);
        imageLeft = (width - height) + ((height - imageWidth) / 2);
        graphics.drawBitmap(imageLeft, imageTop, imageWidth, imageHeight, toDraw, 0, 0);

        return height;
    }

	
	protected void drawBackground(Graphics graphics, int x, int y, int width, int height, boolean selected) {
		Bitmap toDraw = null;
		if (selected) {
			toDraw = bgSelected;
		} else {
			toDraw = bg;
		}
		
		int imgWidth = toDraw.getWidth();
		while (width > -2) {
			graphics.drawBitmap(x - 1, y - 1, width + 2, height + 1, toDraw, 0, 0);
			width -= imgWidth;
			// Overlap a little bit to avoid border issues
			x += imgWidth - 2;
		}
	}
	
	protected void drawBorder(Graphics graphics, int x, int y, int width, int height) {
		
		graphics.setColor(Color.GRAY);
		graphics.drawLine(x, y - 1, x + width, y - 1);
		graphics.drawLine(x, y + height - 1, x + width, y + height - 1);
	}
	
	//SingleLine of Text in the row
    protected int drawText(Graphics graphics, int x, int y, int width, int height, String title, boolean selected) {
        int fontHeight = ((int) ((3* height) / 5)) - (PADDING * 2);
        graphics.setFont(Font.getDefault().derive(Font.BOLD, fontHeight));

        if (selected) {
            graphics.setColor(Color.BLACK);
        } else {
            graphics.setColor(Color.GRAY);
        }

        if (title != null) {
        	// Title is vertically centered
        return   graphics.drawText(title, x + PADDING + 3, y + PADDING + 2 + (fontHeight / 2), DrawStyle.LEFT
                    | DrawStyle.TOP | DrawStyle.ELLIPSIS, width - x - (PADDING * 2));
        }

        return 0;
    }
	
	 protected int drawFirstRowMainText(Graphics graphics, int x, int y, int width, int height, String title, boolean selected) {
		 int myColor = Color.BLACK;
		 
	/*	 if (selected) {
			 myColor=Color.BLACK;
		 } else {
			 myColor= Color.GRAY;
		 }
		 */
		return drawFirstRowMainText(graphics, x, y, width, height, title, myColor);
	 }
	 
		//text on the first row that fill 2/3 of the vertical space
	 protected int drawFirstRowMainText(Graphics graphics, int x, int y, int width, int height, String title, int myColor) {
		 graphics.setColor(myColor);
	//	 int fontHeight = ((int) ((3* height) / 5)) - (PADDING * 2);
		 int fontHeight = ((int) ((3* height) / 6)) - (PADDING * 2);
		 graphics.setFont(Font.getDefault().derive(Font.BOLD, fontHeight));	 
		 
		 if (title != null) {
			 // Title takes top 2/3 of list item
			 return   graphics.drawText(title, x + PADDING + 3, y + PADDING + 2, DrawStyle.LEFT
					 | DrawStyle.TOP | DrawStyle.ELLIPSIS, width - x - (PADDING * 2));
		 }
		 
		 return 0;
	    }
    

	    protected void drawSecondRowText(Graphics graphics, int x, int y, int width, int height, String status, boolean selected) {
	       // int fontHeight = ((2* height) / 5) - (PADDING * 2);
	    	 int fontHeight = ((3* height) / 6) - (PADDING * 2);
	        graphics.setFont(Font.getDefault().derive(Font.PLAIN, fontHeight));

	        if (selected) {
	            graphics.setColor(Color.BLACK);
	        } else {
	            graphics.setColor(Color.LIGHTGREY);
	        }
	        graphics.drawText(status, x + PADDING + 5, y - 4 + (height - fontHeight),
	                DrawStyle.LEFT | DrawStyle.TOP, width - PADDING);
	    }


    	public int indexOfList(ListField listField, String prefix, int start) {
    		return listField.getSelectedIndex();
    	}
	    
    	public int getPreferredWidth(ListField listField) {
    		return Display.getWidth();
    	}
}
