package com.wordpress.view.component;

import com.wordpress.view.GUIFactory;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;


public abstract class BasicListFieldCallBack implements ListFieldCallback {

	protected int focusBgColor = GUIFactory.LIST_COLOUR_BACKGROUND_FOCUS;
	
	public final static int SPACE_BETWEEN_ROW = 6; //if you want some space between rows this is the predef dim
	protected static final int MAX_ROW_HEIGHT = 80;
	protected static final int MIN_ROW_HEIGHT = 42;
	protected static final int PADDING = 4;
		
	public static int getImageHeightForDoubleLineRow() {
		return getRowHeightForDoubleLineRow() - 8;
	}
	
	public static int getImageHeightForSingleLineRow() {
		return getRowHeightForSingleLineRow() - 8;
	}
	
	private static int getSecondRowTextHeight() {
		Font fnt = Font.getDefault();
		int fntHeight = fnt.getHeight();
		return ((4*(fntHeight))/5);
	}
	
	public static int getRowHeightForDoubleLineRow() {
		Font fnt = Font.getDefault();
		int fntHeight = fnt.getHeight();

		int rowH = PADDING + fntHeight + PADDING + getSecondRowTextHeight() + PADDING;
		
		if(rowH > MAX_ROW_HEIGHT)
			return MAX_ROW_HEIGHT;
		else if (rowH < 42)
			return MIN_ROW_HEIGHT;
		else return rowH;
	}
	
	public static int getRowHeightForSingleLineRow() {
		Font fnt = Font.getDefault().derive(Font.BOLD);
		int fntHeight = fnt.getHeight();

		int rowH = PADDING + fntHeight + PADDING ;
		
		if(rowH > MAX_ROW_HEIGHT)
			return MAX_ROW_HEIGHT;
		else if (rowH < 42)
			return MIN_ROW_HEIGHT;
		else return rowH;
	}

	protected int drawLeftImage(Graphics graphics, int x, int y, int height, Bitmap leftImage) {

		int imageWidth = leftImage.getWidth();
		int imageHeight = leftImage.getHeight();
		int imageTop = y + ((height - imageHeight) / 2);
		//int imageLeft = x + ((height - imageWidth) / 2);

		// Image on left side
		graphics.drawBitmap(x, imageTop, imageWidth, imageHeight, leftImage, 0, 0);

		return imageWidth;
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
		if (selected) {
			graphics.setColor(this.focusBgColor);
		} else {
			graphics.setColor(Color.WHITESMOKE);
		}
		graphics.fillRect(x - 1, y - 1, width + 2, height + 1);
	}

	protected void drawBorder(Graphics graphics, int x, int y, int width, int height) {
		graphics.setColor(Color.GRAY);
		graphics.drawLine(x, y - 1, x + width, y - 1);
		graphics.drawLine(x, y + height - 1, x + width, y + height - 1);
	}

	//SingleLine of Text in the row
	protected int drawSingleLineText(Graphics graphics, int x, int y, int width, int height, String title, boolean selected, int fontStyle) {
		Font fnt = null;
		if(fontStyle != -1 ) {
			fnt = Font.getDefault().derive(fontStyle);
		} else {
			fnt = Font.getDefault().derive(Font.PLAIN);
		}
		graphics.setFont(fnt);

		if (selected) {
			graphics.setColor(Color.WHITE);
		} else {
			graphics.setColor(Color.BLACK);
		}
		
		if (title != null) {
			int fntHeight = fnt.getHeight();
			int textTop = y + ((height - fntHeight) / 2);
			
			return   graphics.drawText(title, x + PADDING,  textTop , DrawStyle.LEFT
					| DrawStyle.TOP | DrawStyle.ELLIPSIS, width - x - (PADDING * 2));
		}
		return 0;
	}
	
	protected int drawTextOnFirstRow(Graphics graphics, int x, int y, int width, int height, String title, boolean selected) {
		int myColor = Color.BLACK;
		if (selected) {
			myColor=Color.WHITE;
		} else {
			myColor= Color.BLACK;
		}
		return drawTextOnFirstRow(graphics, x, y, width, height, title, myColor);
	}

	protected int drawTextOnFirstRow(Graphics graphics, int x, int y, int width, int height, String title, int myColor) {
		graphics.setColor(myColor);
		graphics.setFont(Font.getDefault().derive(Font.BOLD));	 
		if (title != null) {
			return   graphics.drawText(title, x + PADDING , y + PADDING, DrawStyle.LEFT
					| DrawStyle.TOP | DrawStyle.ELLIPSIS, width - x - (PADDING * 2));
		}
		return 0;
	}

	protected void drawSecondRowText(Graphics graphics, int x, int y, int width, int height, String status, boolean selected) {
		int fh = getSecondRowTextHeight();
		graphics.setFont(Font.getDefault().derive(Font.PLAIN, fh));
		if (selected) {
			graphics.setColor(Color.WHITE);
		} else {
			graphics.setColor(Color.DARKGRAY);
		}
		graphics.drawText(status, x + PADDING, y - PADDING + (height - fh),
				DrawStyle.LEFT | DrawStyle.TOP | DrawStyle.ELLIPSIS, width - (PADDING * 2));
	}

	public int indexOfList(ListField listField, String prefix, int start) {
		return listField.getSelectedIndex();
	}

	public int getPreferredWidth(ListField listField) {
		return Display.getWidth();
	}
}

/*
protected int drawLeftImage(Graphics graphics, int x, int y, int height, EncodedImage leftImage) {

	int imageWidth = leftImage.getWidth();
	int imageHeight = leftImage.getHeight();
	int imageTop = y + ((height - imageHeight) / 2);
	int imageLeft = x + ((height - imageWidth) / 2);

	// Image on left side -- draw only the first frame
	graphics.drawImage(imageLeft, imageTop, imageWidth, imageHeight, leftImage, 0, 0, 0);

	return height;
}
*/


/*
protected int drawLeftImageCentered(Graphics graphics, int x, int y, int height, Bitmap leftImage, int width) {

	int imageWidth = leftImage.getWidth();
	int imageHeight = leftImage.getHeight();
	int imageTop = y + ((height - imageHeight) / 2);
	int imageLeft = x + ((width - imageWidth) / 2);
	
	// Image on left side
	graphics.drawBitmap(imageLeft, imageTop, imageWidth, imageHeight, leftImage, 0, 0);

	return imageWidth;
}

*/

/*	 
protected void drawBackground(Graphics graphics, int x, int y, int width, int height, boolean selected) {
	Bitmap toDraw = null;
	if (selected) {
		toDraw = bgSelected;
		int imgWidth = toDraw.getWidth();
		while (width > -2) {
			graphics.drawBitmap(x - 1, y - 1, width + 2, height + 1, toDraw, 0, 0);
			width -= imgWidth;
			// Overlap a little bit to avoid border issues
			x += imgWidth - 2;
		}
	} else {

		graphics.setColor(Color.WHITE);
        graphics.fillRect(x - 1, y - 1, width + 2, height + 1);

	}
}
 */