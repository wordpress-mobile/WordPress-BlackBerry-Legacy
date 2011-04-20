//#preprocess
package com.wordpress.view;

import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.Ui;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BasicListFieldCallBack;
import com.wordpress.view.component.ColoredLabelField;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.TouchEvent;
//#endif

public class WelcomeView extends StandardBaseView {

	private MainController mainController = null;

	private  BitmapField wpPromoBitmapField;
	private  EncodedImage promoImg;
	
   private ListField list;
   private Vector _listData = new Vector();
   private ListCallBack listFieldCallBack = null;

   public WelcomeView() {
	   super(MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL | USE_ALL_HEIGHT);
	   this.mainController=MainController.getIstance();

		 int width = Display.getWidth(); 
		 int height = Display.getHeight();
		 
		 if(width == 240 && height == 320 ) {
			 promoImg = EncodedImage.getEncodedImageResource("wp_blue-m.png");			 
		 } else if(width == 320 && height == 240) {
			 promoImg = EncodedImage.getEncodedImageResource("wp_blue-s.png");
		 } else if(width == 480 && height == 320) { 
			 promoImg = EncodedImage.getEncodedImageResource("wp_blue-m.png");	 
		 } else if(width == 480 && height == 360) {
			 promoImg = EncodedImage.getEncodedImageResource("wp_blue-m.png");	
		 } else if(width == 360 && height == 480) {
			  promoImg = EncodedImage.getEncodedImageResource("wp_blue-l.png");
		 } else if (Display.getHeight() > 480  ) {
			 promoImg = EncodedImage.getEncodedImageResource("wp_blue-l.png");
		 } else {
			 promoImg = EncodedImage.getEncodedImageResource("wp_blue-s.png");	
		 }
		 
	   wpPromoBitmapField =  new BitmapField(promoImg.getBitmap(), Field.FIELD_HCENTER | Field.FIELD_VCENTER) {
		   protected void drawFocus(Graphics graphics, boolean on) {
			   //disabled the default focus behavior so that blue rectangle isn't drawn
		   }
	   };
	   wpPromoBitmapField.setMargin(5, 0, 0, 0);
	   add(wpPromoBitmapField);

		Font fnt = Font.getDefault().derive(Font.BOLD);
		int fntHeight = fnt.getHeight();
		fnt = Font.getDefault().derive(Font.BOLD, fntHeight+2, Ui.UNITS_px);

		HorizontalFieldManager taglineManager = new HorizontalFieldManager(Field.FIELD_HCENTER |Field.USE_ALL_WIDTH);
		LabelField lblField = new ColoredLabelField(_resources.getString(WordPressResource.PROMOSCREEN_TAGLINE), 
				 Color.WHITESMOKE, Field.USE_ALL_WIDTH | DrawStyle.HCENTER);
		lblField.setFont(fnt);
		taglineManager.add(lblField);
		if (width > 320)
			lblField.setMargin( 10, 15, 10, 15 );
		else
			lblField.setMargin( 6, 4, 4, 4 );

		add(taglineManager);
		
	   
	   list = new ListField()
	   {
		   protected void drawFocus(Graphics graphics, boolean on) { } //remove the standard focus highlight
	   };

	   //Set the ListFieldCallback
	   listFieldCallBack = new ListCallBack();
	   list.setCallback(listFieldCallBack);
	   ResourceBundle resourceBundle = WordPressCore.getInstance().getResourceBundle();
	   String emptyListString = resourceBundle.getString(WordPressResource.MESSAGE_NOTHING_TO_SEE_HERE);
	   list.setEmptyString(emptyListString, DrawStyle.LEFT);
	   list.setRowHeight(BasicListFieldCallBack.getRowHeightForSingleLineRow()+ BasicListFieldCallBack.SPACE_BETWEEN_ROW);
	   add(list);
	   _listData.addElement(_resources.getString(WordPressResource.PROMOSCREEN_BUTTON_NEW_TO_WP_BLOG));	 
	   _listData.addElement(_resources.getString(WordPressResource.PROMOSCREEN_BUTTON_HAVE_A_WPCOM_BLOG));
	   _listData.addElement(_resources.getString(WordPressResource.PROMOSCREEN_BUTTON_HAVE_A_WPORG_BLOG));	
	   list.setSize(_listData.size());
   }

	private void defaultItemAction() {
	    int index = list.getSelectedIndex();
        
        if(index == -1) return;
        
        if (index == 0)
        	Tools.openURL(WordPressInfo.BB_APP_SIGNUP_URL);
        else if (index == 1) 
        	mainController.addWPCOMBlogs();
        else
        	mainController.addWPORGBlogs();
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
			defaultItemAction();
			return true;
		}
		return super.navigationClick(status, time);
	}
	        	
    //Allow the space bar to toggle the status of the selected row.
    protected boolean keyChar(char key, int status, int time)
    {
        //If the spacebar was pressed...
        if (key == Characters.SPACE || key == Characters.ENTER)
        {
        	defaultItemAction();
        	return true;
        }
        return false;
    }
    
	//#ifdef IS_OS47_OR_ABOVE
	protected boolean touchEvent(TouchEvent message) {
		Log.trace("touchEvent");
		
		if(!list.getContentRect().contains(message.getX(1), message.getY(1)))
		{       			
			return true; //we are return true bc we are eating the event even if it outside the list
		} 
		
		int eventCode = message.getEvent();

		if(WordPressInfo.isForcelessTouchClickSupported) {
			if (eventCode == TouchEvent.GESTURE) {
				TouchGesture gesture = message.getGesture();
				int gestureCode = gesture.getEvent();
				if (gestureCode == TouchGesture.TAP) {
					defaultItemAction();
					return true;
				}
			} 
			return false;
		} else {
			if(eventCode == TouchEvent.CLICK) {
				defaultItemAction();
				return true;
			}else if(eventCode == TouchEvent.DOWN) {
			} else if(eventCode == TouchEvent.UP) {
			} else if(eventCode == TouchEvent.UNCLICK) {
				//return true; //consume the event: avoid context menu!!
			} else if(eventCode == TouchEvent.CANCEL) {
			}
			return false; 
			//return super.touchEvent(message);
		}
	}
	//#endif
	
	public BaseController getController() {
		return mainController;
	}
	
    private class ListCallBack extends BasicListFieldCallBack {
        
        // Draws the list row.
    	public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) {
    		String currentRow = (String)this.get(list, index);
    		Font originalFont = graphics.getFont();
    		int originalColor = graphics.getColor();
    		int height = list.getRowHeight();
    		
			/*
			 * 42px of row
			 * 6px blank space
			 */
			height = height - SPACE_BETWEEN_ROW;
			w = w - 10;
    		
    		drawBackground(graphics, 5, y, w, height, list.getSelectedIndex() ==  index);
    		drawBorder(graphics, 5, y, w, height);
    		drawSingleLineTextHCentered(graphics, 5, y, w - 5, height, currentRow, list.getSelectedIndex() ==  index, Font.PLAIN);

            graphics.setFont(originalFont);
            graphics.setColor(originalColor);
    	}
    	
		protected void drawBorder(Graphics graphics, int x, int y, int width, int height) {
			graphics.setColor(Color.BLACK);
			graphics.drawLine(x-1, y , x + width-1, y);
			graphics.drawLine(x-1, y, x-1 , y + height-1); //linea verticale sx
			graphics.drawLine(x + width, y-1, x + width , y + height-1); //linea verticale dx
			graphics.drawLine(x-1, y + height - 1, x + width-1, y + height - 1);
		}

		 //Returns the object at the specified index.
        public Object get(ListField list, int index) 
        {
            return _listData.elementAt(index);
        }
        
    }
}