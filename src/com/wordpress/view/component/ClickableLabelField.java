//#preprocess
package com.wordpress.view.component;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
//#endif

public class ClickableLabelField extends LabelField {
	
	protected ResourceBundle _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	protected int textColor = Color.BLUE;
	protected String contextMenuItemLabel = _resources.getString(WordPressResource.MENUITEM_OPEN);
	
	public ClickableLabelField(String text,  long style) {
		super(text, style);
	}
	
	public void setTextColor(int textColor) {
		this.textColor = textColor;
	}

	public void paint(Graphics g) {		 
	       int oldColour = g.getColor();
	        try {           
	            if(g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ) {
	                g.setColor( Color.WHITE );
	            } else {
	                g.setColor( textColor );
	            }
	            super.paint( g );
	        } finally {
	            g.setColor( oldColour );
	        }
	 }
	 
	 
    public void setDirty( boolean dirty ) {
        // We never want to be dirty or muddy
    }
     
    public void setMuddy( boolean muddy ) {
        // We never want to be dirty or muddy
    }
         
	 protected void performDefaultActionOnItem() {
		 fieldChangeNotify( 0 );
	 }
	 
      /**
       * Overrides default implementation.  Performs default action if the 
       * 4ways trackpad was clicked; otherwise, the default action occurs.
       * 
       * @see net.rim.device.api.ui.Screen#navigationClick(int,int)
       */
  	protected boolean navigationClick(int status, int time) {
  		Log.trace(">>> navigationClick");
  		
  		if ((status & KeypadListener.STATUS_TRACKWHEEL) == KeypadListener.STATUS_TRACKWHEEL) {
  			Log.trace("Input came from the trackwheel");
  			// Input came from the trackwheel
  			return super.navigationClick(status, time);
  			
  		} else if ((status & KeypadListener.STATUS_FOUR_WAY) == KeypadListener.STATUS_FOUR_WAY) {
  			Log.trace("Input came from a four way navigation input device");
  			performDefaultActionOnItem();
  			return true;
  		}
  		return super.navigationClick(status, time);
  	}
  	
      /**
       * Overrides default.  Enter key will take default action on selected item.
       *  
       * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
       * 
       */
  	protected boolean keyChar(char c, int status, int time) {
  		Log.trace(">>> keyChar");
  		// Close this screen if escape is selected.
  		if (c == Characters.ENTER) {
  			performDefaultActionOnItem();
  			return true;
  		}
  		return super.keyChar(c, status, time);
  	}
      
  	
      
  	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
  	protected boolean touchEvent(TouchEvent message) {
  		int eventCode = message.getEvent();
  		Log.trace(">>> touchEvent - "+ eventCode);
  		// Get the screen coordinates of the touch event
        int x = message.getX(1);
        int y = message.getY(1);
        // Check to ensure point is within this field
        if(x < 0 || y < 0 || x > getExtent().width || y > getExtent().height) {
            return false;
        }
        if(WordPressInfo.isForcelessTouchClickSupported) {
        	if (eventCode == TouchEvent.GESTURE) {
        		TouchGesture gesture = message.getGesture();
        		int gestureCode = gesture.getEvent();
        		if (gestureCode == TouchGesture.TAP) {
        			performDefaultActionOnItem();
        			return true;
        		} else if (gestureCode == TouchGesture.HOVER) {
        			return true;
        		}
        	} 
        } else {
        	if(eventCode == TouchEvent.CLICK) {
        		performDefaultActionOnItem();
        		return true;
        	}
        }
  		return super.touchEvent(message);
  	}
  	//#endif
	 
  
  	public void setContextMenuText(String text) {
  		myContextMenuItemA.setText(text);
  	}
	 
  	protected MenuItem myContextMenuItemA = new MenuItem(_resources.getString(WordPressResource.MENUITEM_OPEN), 10, 2) {
          public void run() {
          	performDefaultActionOnItem();
          }
      };
   
      protected void makeContextMenu(ContextMenu contextMenu) {
          contextMenu.addItem(myContextMenuItemA);
      }
}
