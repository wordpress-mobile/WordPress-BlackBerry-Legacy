//#preprocess
package com.wordpress.bb;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.log.Log;


public class SplashScreen extends MainScreen {
  
   //create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	private EncodedImage image;
	private LabelField versionLabel;
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
   
   public SplashScreen() {
		super(Field.USE_ALL_HEIGHT | Field.FIELD_LEFT);
		
		image = EncodedImage.getEncodedImageResource("wordpress_home_460.png");
		
		 int width = Display.getWidth(); 
		 if(width == 240) {
			 image= EncodedImage.getEncodedImageResource("wordpress_home_220.png");
		 } else if(width == 320) {
			 image= EncodedImage.getEncodedImageResource("wordpress_home_300.png");			 
		 } else if(width == 360) {
			 image= EncodedImage.getEncodedImageResource("wordpress_home_340.png");
		 } else if(width == 480) { 
			 image= EncodedImage.getEncodedImageResource("wordpress_home_460.png");
		 } else {
			 int _preferredWidth = -1;
			//Set the preferred width to the image size or screen width if the image is larger than the screen width.
	        if (image.getWidth() > Display.getWidth()) {
	            _preferredWidth = Display.getWidth();
	        }
	        if( _preferredWidth != -1) {        	
	        	EncodedImage resImg = ImageUtils.resizeEncodedImage(image, _preferredWidth, image.getHeight());
	        	image = resImg;
	        } 
		 }
        
        this.add(new BitmapField(image.getBitmap(), Field.FIELD_HCENTER| Field.FIELD_VCENTER));
    	String version = PropertyUtils.getAppVersion(); //read from the alx files
        if(version == null || version.trim().equals("")) { //read value from jad file
        	//MIDlet-Version
        	version = PropertyUtils.getIstance().get("MIDlet-Version");
        	if(version == null)
        		version = "";
        }
        
        versionLabel = new LabelField(version, Field.FIELD_HCENTER| Field.FIELD_VCENTER){
		    public void paint(Graphics graphics)
		    {
		        graphics.setColor(Color.GRAY);
		        super.paint(graphics);
		    }
		};
	  	Font fnt = this.getFont().derive(Font.BOLD,9, Ui.UNITS_pt);
	  	versionLabel.setFont(fnt);
        this.add(versionLabel);
        
        SplashScreenListener listener = new SplashScreenListener(this);
		this.addKeyListener(listener);
	}
/*
   protected void sublayout(int width, int height) {
	//   layoutDelegate(width - 80, height - 80);
	 //  setPositionDelegate(10, 10);
	   super.sublayout(width, height);
	   Log.trace("getDelegate().getHeight() " +getDelegate().getHeight());
	   setExtent(width,  getDelegate().getHeight());
	   Log.trace("getHeight() " +getHeight());
	   int imgHeight = image.getHeight()-20;
	   setPosition(0, (height - imgHeight)/2);
	  }   
   */
   

   protected void sublayout(int width, int height) {
	   Log.trace("labelFieldSize : " +Ui.convertSize(9, Ui.UNITS_pt, Ui.UNITS_px));
	   
	   int fieldsHeight = image.getHeight()+ Ui.convertSize(9, Ui.UNITS_pt, Ui.UNITS_px);
	   Log.trace("fieldsHeight Height " + fieldsHeight);
	   
	   layoutDelegate(width, Math.min(height,fieldsHeight));
	   setPositionDelegate(0, (height - fieldsHeight )/2);
	   
	  // super.sublayout(width, height);
	   Log.trace("getDelegate().getHeight() " +getDelegate().getHeight());
	   setExtent(width,  height);
	   setPosition(0,0);
	   Log.trace("getHeight() " +getHeight());
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