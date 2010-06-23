//#preprocess
package com.wordpress.view.component;

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
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchEvent;
//#endif

public class ClickableLabelField extends LabelField {
	
	protected ResourceBundle _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	protected int textColor = Color.GRAY;
	

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
      
  	
      
  	//#ifdef IS_OS47_OR_ABOVE
  	protected boolean touchEvent(TouchEvent message) {
  		Log.trace(">>> touchEvent");
  		int eventCode = message.getEvent();
  		
  		// Get the screen coordinates of the touch event
  		if(eventCode == TouchEvent.CLICK) {
  			Log.trace("TouchEvent.CLICK");
  			performDefaultActionOnItem();
  			return true;
			} 
			return false; 
  	}
  	//#endif
	 
      
	 
  	protected MenuItem myContextMenuItemA = new MenuItem(_resources.getString(WordPressResource.MENUITEM_OPEN), 10, 2) {
          public void run() {
          	performDefaultActionOnItem();
          }
      };
   
      protected void makeContextMenu(ContextMenu contextMenu) {
          contextMenu.addItem(myContextMenuItemA);
      }
}