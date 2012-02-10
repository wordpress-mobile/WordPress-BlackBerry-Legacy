//#preprocess

//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0

package com.wordpress.view.touch;


import com.wordpress.view.GUIFactory;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.TouchEvent;

public class BottomBarButtonField extends Field {
	private Bitmap enabledBitmap;
	private Bitmap disabledBitmap;
	private int bitmapWidth;
	private int bitmapHeight;
	private boolean hasHover;
	
	private static final int  btn_colour_background_focus = GUIFactory.BTN_COLOUR_BACKGROUND_FOCUS;
	public static final int CHANGE_HOVER_GAINED = 100;
	public static final int CHANGE_HOVER_LOST = 101;
	public static final int CHANGE_CLICK = 110;
	private final String tooltip;
	
	public String getTooltip() {
		return tooltip;
	}

	public BottomBarButtonField() {
		this(null, null, null);
	}
	
	public BottomBarButtonField(Bitmap enabledBitmap, Bitmap disabledBitmap, String tooltip) {
		this.tooltip = tooltip;
		if(enabledBitmap != null) {
			this.enabledBitmap = enabledBitmap;
			this.disabledBitmap = disabledBitmap;
			this.bitmapWidth = enabledBitmap.getWidth();
			this.bitmapHeight = enabledBitmap.getHeight();
			if(disabledBitmap == null || disabledBitmap.getWidth() != bitmapWidth || disabledBitmap.getHeight() != bitmapHeight) {
				this.disabledBitmap = this.enabledBitmap;
			}
		}
	}
	
	protected void layout(int width, int height) {
		setExtent(width, height);
	}
	
	protected boolean touchEvent(TouchEvent message) {
		boolean result;

		int x = message.getX(1);
		int y = message.getY(1);
		if(x < 0 || x >= this.getWidth() || y < 0 || y >= this.getWidth()) {
			if(hasHover) {
				fieldChangeNotify(CHANGE_HOVER_LOST);
				hasHover = false;
				invalidate();
			}
			result = false;
		}
		else {
			if(!isEditable()) {
				result = false;
			}
			else {
				switch(message.getEvent()) {
				case TouchEvent.DOWN:
					hasHover = true;
					invalidate();
					fieldChangeNotify(CHANGE_HOVER_GAINED);
					result = true;
					break;
				case TouchEvent.UP:
					hasHover = false;
					invalidate();
					fieldChangeNotify(CHANGE_HOVER_LOST);
					result = true;
					break;
				case TouchEvent.CLICK:
					fieldChangeNotify(CHANGE_CLICK);
					result = true;
					break;
				default:
					result = super.touchEvent(message);
				}
			}
		}
		return result;
	}
	
	public void setEditable(boolean editable) {
		super.setEditable(editable);
		this.hasHover = false;
		invalidate();
	}
	
	protected void paint(Graphics graphics) {
		int width = this.getWidth();
		int height = this.getHeight();

		Bitmap bitmap;
		if(this.isEditable()) {
			bitmap = enabledBitmap;
		}
		else {
			bitmap = disabledBitmap;
		}
		if(hasHover) {
			graphics.pushContext(0, 0, width, height, 0, 0);
			graphics.setGlobalAlpha(255);
			graphics.setColor(btn_colour_background_focus);
			graphics.fillRoundRect(1, 1, width-1, height-1,15, 15);
			graphics.popContext();
		}
		if(bitmap != null) {
			graphics.drawBitmap(
					(width/2) - (bitmapWidth/2),
					height - 10 - bitmapHeight,
					bitmapWidth, bitmapHeight,
					bitmap, 0, 0);
		}
	}
}
//#endif