//#preprocess

//#ifdef IS_OS47_OR_ABOVE
package com.wordpress.view;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.decor.Background;
import net.rim.device.api.ui.decor.BackgroundFactory;
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.controller.BaseController;


/**
 * Base view with an image in background
 * @author dercoli
 *
 */
public abstract class StandardBaseViewNew extends BaseView {

	public StandardBaseViewNew(long style) {
		super(style);
		Background bg = BackgroundFactory.createBitmapBackground(_backgroundBitmap, Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_BOTH);
		getMainManager().setBackground(bg);
	}
	
	public StandardBaseViewNew(String title, long style) {
		super(title, style);
		Background bg = BackgroundFactory.createBitmapBackground(_backgroundBitmap,Background.POSITION_X_LEFT, Background.POSITION_Y_TOP, Background.REPEAT_BOTH);
		getMainManager().setBackground(bg);
	}
			
	protected void sublayout( int maxWidth, int maxHeight ) {
		int titleFieldHeight = 0;

		if (Touchscreen.isSupported() == true && bottomButtonsManager != null && isBottomBarVisible) {
			titleFieldHeight += bottomButtonsManager.getHeight();
		}  			   				
		
		int displayWidth = Display.getWidth(); 
		int displayHeight = Display.getHeight();
		
		super.sublayout( displayWidth, displayHeight - titleFieldHeight );
		setExtent( displayWidth, displayHeight - titleFieldHeight );
	}

    //return the controller associated with this view
    public abstract BaseController getController();
}
//#endif