package com.wordpress.view.component;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ButtonField;

/**
 * Button field with a bitmap as its label.
 */
public class BitmapButtonField extends ButtonField {
	private Bitmap bitmap;
	
	public BitmapButtonField(Bitmap bitmap) {
		super();
		this.bitmap = bitmap;
	}
	
	public BitmapButtonField(Bitmap bitmap, long style ) {
		super(style);
		this.bitmap = bitmap;
	}
	
	protected void layout(int width, int height) {
		setExtent(getPreferredWidth(), getPreferredHeight());
	}
	
	public int getPreferredWidth() {
		return bitmap.getWidth();
	}
	
	public int getPreferredHeight() {
		return bitmap.getHeight();
	}
	
	protected void paint(Graphics graphics) {
		super.paint(graphics);
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		graphics.drawBitmap(0, 0, width, height, bitmap, 0, 0);
	}
}