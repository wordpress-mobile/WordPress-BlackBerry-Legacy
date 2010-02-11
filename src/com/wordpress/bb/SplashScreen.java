//#preprocess
package com.wordpress.bb;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.utils.ImageUtils;


public class SplashScreen extends MainScreen {
  
   //create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
   
   public SplashScreen() {
		super(Field.USE_ALL_HEIGHT | Field.FIELD_LEFT);
		EncodedImage _theImage= EncodedImage.getEncodedImageResource("wplogo.png");
		int _preferredWidth = -1;
		//Set the preferred width to the image size or screen width if the image is larger than the screen width.
        if (_theImage.getWidth() > Display.getWidth()) {
            _preferredWidth = Display.getWidth();
        }
        if( _preferredWidth != -1) {        	
        	EncodedImage resImg = ImageUtils.resizeEncodedImage(_theImage, _preferredWidth, _theImage.getHeight());
        	_theImage = resImg;
        }
        
        this.add(new BitmapField(_theImage.getBitmap(), Field.FIELD_HCENTER| Field.FIELD_VCENTER));
		SplashScreenListener listener = new SplashScreenListener(this);
		this.addKeyListener(listener);
			
	}

   protected boolean navigationClick(int status, int time) {
     // dismiss();
      //return true;
	   return false;
   }
   
   protected boolean navigationUnclick(int status, int time) {
      return false;
   }
   
   protected boolean navigationMovement(int dx, int dy, int status, int time) {
      return false;
   }
   
   public static class SplashScreenListener implements KeyListener {
      private SplashScreen screen;
      public boolean keyChar(char key, int status, int time) {
         //intercept the ESC and MENU key - exit the splash screen
         boolean retval = false;
         /*switch (key) {
            case Characters.CONTROL_MENU:
            case Characters.ESCAPE:
            screen.dismiss();
            retval = true;
            break;
         }*/
         return retval;
      }
      public boolean keyDown(int keycode, int time) {
         return false;
      }
      public boolean keyRepeat(int keycode, int time) {
         return false;
      }
      public boolean keyStatus(int keycode, int time) {
         return false;
      }
      public boolean keyUp(int keycode, int time) {
         return false;
      }
      public SplashScreenListener(SplashScreen splash) {
         screen = splash;
      }
   }


} 