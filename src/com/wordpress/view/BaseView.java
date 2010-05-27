//#preprocess
package com.wordpress.view;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.NotificationHandler;
import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.view.component.HeaderField;

//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.VirtualKeyboard;
import net.rim.device.api.ui.Touchscreen;
import com.wordpress.view.touch.BottomBarButtonField;
import com.wordpress.view.touch.BottomBarItem;
import com.wordpress.view.touch.BottomBarManager;
//#endif

/**
 * Base class for all Application Screen
 * @author dercoli
 *
 */
public abstract class BaseView extends MainScreen {
	
	protected Field titleField; //main title of the screen
	
	protected static Bitmap _backgroundBitmap = null; 
	
	//create a variable to store the ResourceBundle for localization support
	protected static ResourceBundle _resources;
	
	static {
		//retrieve a reference to the ResourceBundle for localization support
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
		//retrive the bg image based on the screen dimensions
		_backgroundBitmap = WordPressCore.getInstance().getBackgroundBitmap();
	}
	
	public BaseView(long style) {
		super(style);
	}

    public BaseView(String title) {
		super();
		titleField = getTitleField(title);
		super.add(titleField);
	}
    
	public BaseView(String title, long style) {
		super(style);
		titleField = getTitleField(title);
		super.add(titleField);
	}
    
	
    protected void onDisplay() {
        super.onDisplay();
        NotificationHandler.getInstance().cancelNotification();
    }

    protected void onUndisplay() {
        NotificationHandler.getInstance().cancelNotification();
        super.onUndisplay();
    }
    
    //return the controller associated with this view
    public abstract BaseController getController();
    
    //create the title filed
	protected Field getTitleField(String title) {
		HeaderField headerField = new HeaderField(title);
		return (Field)headerField;
	}
	
	public void setTitleText(String title){
		if(titleField!= null)
			((HeaderField)titleField).setTitle(title);
	}
	
	public void setSubTitleText(String title){
		if(titleField!= null)
			((HeaderField)titleField).setSubTitle(title);
	}
	
    protected BasicEditField getDescriptionTextField(String text) {
    	return GUIFactory.getDescriptionTextField(text);
    }
    
    /**
     * TOUCHSCREEN ADDED
     */
  //#ifdef IS_OS47_OR_ABOVE         
	private Field hoverField;
	protected BottomBarManager bottomButtonsManager = null; 
	protected volatile boolean isBottomBarVisible = false;
	
	protected void sublayout( int maxWidth, int maxHeight ) {
		
		/* We MUST remove the bottom bar when the virtual keyboad is visible on the screen.  
		  * There is in no lister to catch that event, and we really won't setup a polling 
		  * thread on the virtual kb status. Instead we want to rearrange twice the View when 
		  * kb is displayed or removed from screen. this could take a little bit more time 
		  * during hide/show of  kb, but don't requires another thread or timertask to do that.
		  */		  
		
		//Log.trace("==Layout della classe base view");
		if (Touchscreen.isSupported() == true && bottomButtonsManager != null
				&& VirtualKeyboard.isSupported() ) {
			//Log.trace("A");
			int kbVisibility = this.getVirtualKeyboard().getVisibility();
			
			if( kbVisibility == VirtualKeyboard.SHOW || kbVisibility == VirtualKeyboard.SHOW_FORCE ) {
				//Log.trace("B");
				if(isBottomBarVisible) {
					isBottomBarVisible = false;
					setStatus(null);
					//Log.trace("C");
				}
			} else {
				//Log.trace("D");
				if(!isBottomBarVisible) {
					isBottomBarVisible = true;
					setStatus(bottomButtonsManager);
					//Log.trace("E");
				}
			}
		}
		super.sublayout(maxWidth, maxHeight);
	}
	
	protected void setBottomBarButtonState(int pos, boolean editable) {	
		if (Touchscreen.isSupported() == false) return;
		BottomBarButtonField btn = (BottomBarButtonField) bottomButtonsManager.getField(pos);
		btn.setEditable(editable);
	}
	
	protected void initializeBottomBar(BottomBarItem[] items) {
		if (Touchscreen.isSupported() == false) return;
		
		bottomButtonsManager = new BottomBarManager();
		int len = Math.min(items.length, 5);
		for(int i=0; i<len; i++) {
			BottomBarButtonField button;
			if(items[i] == null) {
				button = new BottomBarButtonField();
				button.setEditable(false);
			}
			else {
				
				button = new BottomBarButtonField(
						items[i].getEnabledBitmap() != null ? Bitmap.getBitmapResource(items[i].getEnabledBitmap()) : null,
						items[i].getDisabledBitmap() != null ? Bitmap.getBitmapResource(items[i].getDisabledBitmap()) : null,
						items[i].getTooltip());
				button.setCookie(new Integer(i));
				button.setEditable(true);
				button.setChangeListener(shortcutButtonChangeListener);
			}
			bottomButtonsManager.add(button);
		}
		setStatus(bottomButtonsManager);
	}
	
	protected FieldChangeListener shortcutButtonChangeListener = new FieldChangeListener() {
		public void fieldChanged(Field field, int context) {
			if(field instanceof BottomBarButtonField) {
				Integer itemIdx = (Integer)field.getCookie();
				if(context == BottomBarButtonField.CHANGE_CLICK) {
					bottomBarActionPerformed(itemIdx.intValue());
					hoverField = null;
				}
				else if(context == BottomBarButtonField.CHANGE_HOVER_GAINED) {
					hoverField = field;
					invalidate();
				}
				else if(context == BottomBarButtonField.CHANGE_HOVER_LOST) {
					hoverField = null;
					invalidate();
				}
			}
		}
	};
	
	protected void bottomBarActionPerformed(int mnuItem) {
		
	}

	
	protected void paint(Graphics graphics) {
		super.paint(graphics);
		
		if (Touchscreen.isSupported()) 
		if(hoverField != null && hoverField instanceof BottomBarButtonField) {
			
			String tooltip = ((BottomBarButtonField)hoverField).getTooltip();
			if(tooltip == null) return;
			
			Font font = Font.getDefault();
			int tooltipWidth = font.getAdvance(tooltip) + 4;
			int tooltipHeight = font.getHeight() + 4;
			
			XYRect fieldRect = hoverField.getExtent();
			
			int x = fieldRect.x - 1;
			if(x + tooltipWidth > Display.getWidth()) {
				x = Display.getWidth() - tooltipWidth;
			}
			int y = Display.getHeight() - fieldRect.height - tooltipHeight - 12;
			
			graphics.pushRegion(x, y, tooltipWidth, tooltipHeight, 0, 0);
			graphics.setColor(Color.WHITE);
			graphics.fillRect(0, 0, tooltipWidth, tooltipHeight);
			graphics.setColor(Color.BLACK);
			graphics.drawRect(0, 0, tooltipWidth, tooltipHeight);
			graphics.drawText(tooltip, 2, 2);
			graphics.popContext(); 
		}
	}  
	//#endif
}