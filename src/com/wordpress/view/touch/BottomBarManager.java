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
        upperDrawColors = new int[]{0xdeebff, 0x9cbeff, 0x9cbeff, 0xdeebff};
        //upperDrawColors = new int[]{Color.WHITE, Color.LIGHTGREY, Color.LIGHTGREY, Color.WHITE};
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
		graphics.setColor(0x212121);
		graphics.drawLine(0, 0 , fieldWidth, 0);
		int alpha = graphics.getGlobalAlpha(); //remove the next lines
		graphics.setGlobalAlpha( 0x44 );
		graphics.setColor(Color.WHITE);
		graphics.drawLine(0, 1 , fieldWidth, 1);
		graphics.drawLine(0, 2 , fieldWidth, 2);
		graphics.setGlobalAlpha( alpha );
	}

	protected void paint(Graphics graphics) {
	//	graphics.setBackgroundColor(Color.LIGHTGREY);
	//	graphics.clear();
		super.paint(graphics);
	}	
}
//#endif