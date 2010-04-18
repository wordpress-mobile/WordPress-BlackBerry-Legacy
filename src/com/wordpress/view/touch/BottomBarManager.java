//#preprocess

//#ifdef IS_OS47_OR_ABOVE
package com.wordpress.view.touch;


import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;

public class BottomBarManager extends Manager {
	private static final int HEIGHT = 54;
	private int fieldWidth;
	private int[] X_PTS;
	private int[] Y_PTS;
	private int[] upperDrawColors;
	
	public BottomBarManager() {
		super(Manager.USE_ALL_WIDTH);
		X_PTS = new int[]{0, 0, getPreferredWidth(), getPreferredWidth()};
        Y_PTS = new int[]{0, HEIGHT, HEIGHT, 0};
        upperDrawColors = new int[]{0x444444, Color.BLACK, Color.BLACK, 0x444444};
	}
	
	protected void sublayout(int maxWidth, int maxHeight) {
		int displayWidth = Display.getWidth();
		int buttonWidth = displayWidth / 5;
		int count = this.getFieldCount();
		for(int i=0; i<count; i++) {
			Field field = this.getField(i);
			this.setPositionChild(field, (i * buttonWidth) + 1, 2);//1
			this.layoutChild(field, buttonWidth - 2, HEIGHT - 3); //HEIGHT - 2
		}
		setExtent(displayWidth, HEIGHT);
		
		this.fieldWidth = displayWidth;
        X_PTS = new int[]{0, 0, fieldWidth, fieldWidth}; 
	}
	
	protected void paintBackground(Graphics graphics) {
		graphics.drawShadedFilledPath(X_PTS, Y_PTS, null, upperDrawColors, null);
		graphics.setColor(Color.BLACK);
		graphics.drawLine(0, 0 , fieldWidth, 0);
		graphics.setColor(Color.GRAY);
		graphics.drawLine(0, 1 , fieldWidth, 1);
	}	
}
//#endif