//#preprocess
package com.wordpress.view;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.util.LongIntHashtable;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.ColoredLabelField;
import com.wordpress.view.component.EmbossedButtonField;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchEvent;
//#endif

public class GUIFactory {

	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}

	protected static int BTN_COLOUR_BACKGROUND_FOCUS = 0x5292f7;
	
	protected static synchronized BaseButtonField createButton(String label, long style) {
		LongIntHashtable colourTable = new LongIntHashtable();
		colourTable.put(EmbossedButtonField.COLOUR_BACKGROUND_FOCUS, BTN_COLOUR_BACKGROUND_FOCUS);
		EmbossedButtonField btn = new EmbossedButtonField(label, style, colourTable);
        btn.setMargin(0, 4, 0, 4);
		return btn;
	}
	
	 protected static synchronized BasicEditField getDescriptionTextField(String text) {
    	BasicEditField field = new  BasicEditField(BasicEditField.READONLY){
    	    public void paint(Graphics graphics)
    		    {
    		        graphics.setColor(Color.GRAY);
    		        super.paint(graphics);
    		    }
    		};
    	  	
    		Font fnt = Font.getDefault().derive(Font.ITALIC);
    	  	field.setFont(fnt);
    	  	
    	  	field.setText(text);
    	  	return field;
    }
	
	 protected static synchronized SeparatorField createSepatorField() {
		 SeparatorField sep = new SeparatorField() {
				protected void paint( Graphics g ) 
			    {
			        int oldColour = g.getColor();
			        try {
			            g.setColor( Color.GRAY );
			            super.paint( g );
			        } finally {
			            g.setColor( oldColour );
			        }
			    }
		 };
		 return sep;
	 }
	 
	 
	 protected static synchronized LabelField getLabel(String label, int color) {
			LabelField lblField = new ColoredLabelField(label + " ", color);
		  	Font fnt = Font.getDefault().derive(Font.BOLD);
		  	lblField.setFont(fnt);
			return lblField;
		}

	 protected static synchronized LabelField getLabel(String label) {
			LabelField lblField = new ColoredLabelField(label + " ", Color.GRAY);
		  	Font fnt = Font.getDefault().derive(Font.BOLD);
		  	lblField.setFont(fnt);
			return lblField;
		}
	
	 protected static synchronized LabelField getLabel(String label, long style) {
		 LabelField lblField = new ColoredLabelField(label + " ", Color.GRAY, style);
		 Font fnt = Font.getDefault().derive(Font.BOLD);
		 lblField.setFont(fnt);
		 return lblField;
	 }	
	 
	 public static synchronized LabelField createClickableLabel(String label, long style) {
		return createClickableLabel(label, label, style);
	 }
	
	 public static synchronized LabelField createClickableLabel(String label, final String url, long style) {
		 
		 return new LabelField(label, LabelField.FOCUSABLE | style ) {
			 
			 public void paint(Graphics graphics) {
				 if(this.isFocus())
					 graphics.setColor(Color.WHITE);
				 else
					 graphics.setColor(Color.BLUE);	
				 super.paint(graphics);
			 }

			 private void performDefaultActionOnItem() {
				 Tools.getBrowserSession(url);
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
			 
		        
			 
			 private MenuItem myContextMenuItemA = new MenuItem(_resources.getString(WordPressResource.MENUITEM_OPEN_URL), 10, 2) {
		            public void run() {
		            	performDefaultActionOnItem();
		            }
		        };
		     
		        protected void makeContextMenu(ContextMenu contextMenu) {
		            contextMenu.addItem(myContextMenuItemA);
		        }
		 };
		 
	 }
	 
}
