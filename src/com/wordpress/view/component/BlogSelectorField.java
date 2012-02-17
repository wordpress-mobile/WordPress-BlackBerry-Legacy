//#preprocess
package com.wordpress.view.component;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;
import com.wordpress.view.GUIFactory;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.util.LongIntHashtable;

public class BlogSelectorField extends LabelField {

	private static final long COLOUR_BORDER              = 0xc5fd60b0047307a1L;
	private static final int BEVEL    = 2;
	private static final long COLOUR_TEXT                = 0x16a6e940230dba6bL;
	private static final long COLOUR_TEXT_FOCUS          = 0xe208bcf8cb684c98L;
	private static final long COLOUR_BACKGROUND          = 0x8d733213d6ac8b3bL;
	private static final long COLOUR_BACKGROUND_FOCUS    = GUIFactory.BTN_COLOUR_BACKGROUND_FOCUS; 
	
	protected ResourceBundle _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	protected String contextMenuItemLabel = _resources.getString(WordPressResource.MENUITEM_OPEN);
	private String[] choices;
	private String label;
	
    protected Bitmap dropDownBitmap = Bitmap.getBitmapResource("drop_down_arrow.png");
    protected Bitmap dropDownBitmapFocus = Bitmap.getBitmapResource("drop_down_arrow_white.png");
	private final int PADDING = 6;
	private int fieldMaxHeight = 0;
	private int fieldMaxWeight = 0;
    private LongIntHashtable _colourTable;
	private boolean _pressed;
	
	protected void layout(int width, int height) {
		this.fieldMaxWeight = width;
		super.layout(width, fieldMaxHeight);
		setExtent(width, fieldMaxHeight);
	}
	
    protected void makeContextMenu(ContextMenu contextMenu) {
      //remove the context menu
    }
    
	public void setFieldMaxHeight(int fieldMaxHeight) {
		this.fieldMaxHeight = fieldMaxHeight;
	}

	public BlogSelectorField(String[] choices, int iSetTo, long style) {
		super(choices[iSetTo], style);
		this.label = choices[iSetTo];
		this.choices = choices;
		_colourTable = new LongIntHashtable();
		_colourTable.put(EmbossedButtonField.COLOUR_BACKGROUND_FOCUS, GUIFactory.BTN_COLOUR_BACKGROUND_FOCUS);
	}

    public int getColour( long colourKey ) 
    {
        if( _colourTable != null ) {
            int colourValue = _colourTable.get( colourKey );
            if( colourValue >= 0 ) {
                return colourValue;
            }
        }
            
        // Otherwise, just use some reasonable default colours
        if( colourKey == COLOUR_BORDER ) {
            return 0x212121;
        } else if( colourKey == COLOUR_TEXT ) {
            return 0xD6D6D6;
        } else if( colourKey == COLOUR_TEXT_FOCUS ) {
            return Color.WHITE;
        } else if( colourKey == COLOUR_BACKGROUND ) {
            return isStyle( Field.READONLY ) ? 0x777777 : 0x424242;
        } else if( colourKey == COLOUR_BACKGROUND_FOCUS ) {
            return isStyle( Field.READONLY ) ? GUIFactory.BTN_COLOUR_BACKGROUND_FOCUS : 0x185AB5;
        } else {
            throw new IllegalArgumentException();
        }
    }
	
    protected void onUnfocus()
    {
        super.onUnfocus();
        if( _pressed ) {
            _pressed = false;
            invalidate();
        }
    }
    	
    protected void paintBackground( Graphics g)
    {
    	int oldColour = g.getBackgroundColor();
    	int oldAlpha = g.getGlobalAlpha();
    	try {
    		// Border
    		g.setColor( getColour ( COLOUR_BORDER ) );
    		g.fillRect( 1, 0, fieldMaxWeight - 2, fieldMaxHeight );
    		g.fillRect( 0, 1, fieldMaxWeight,     fieldMaxHeight - 2 );

    		// Base color
    		g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BACKGROUND_FOCUS ) : getColour( COLOUR_BACKGROUND ) );
    		g.fillRect( 1, 1, fieldMaxWeight - 2, fieldMaxHeight - 2 );

    		// Highlight and lowlight
    		g.setGlobalAlpha( 0x44 );
    		g.setColor( _pressed ? Color.BLACK : Color.WHITE );
    		g.fillRect( 1, 1, fieldMaxWeight - 2, BEVEL );
    		g.setColor( _pressed ? Color.WHITE : Color.BLACK );
    		g.fillRect( 0, fieldMaxHeight - BEVEL - 1, fieldMaxWeight, BEVEL );

    		// Base color
    		g.setGlobalAlpha( 0xFF );
    		g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BACKGROUND_FOCUS ) : getColour( COLOUR_BACKGROUND ) );
    		g.fillRect( 2, 2, fieldMaxWeight - 4, fieldMaxHeight - 4 );

    	} finally {
    		g.setBackgroundColor( oldColour );
    		g.setGlobalAlpha( oldAlpha );
    	}
    }
	
    protected void drawFocus( Graphics g, boolean on )
    {
    	XYRect _drawFocusTempRect = new XYRect();
    	getFocusRect( _drawFocusTempRect );
    
        boolean oldDrawStyleFocus = g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS );
        boolean notEmpty = g.pushContext( _drawFocusTempRect.x, _drawFocusTempRect.y, _drawFocusTempRect.width, _drawFocusTempRect.height, 0, 0 );

        try {
            if( notEmpty ) {
                g.setDrawingStyle( Graphics.DRAWSTYLE_FOCUS, on );
                paintBackground( g );
                paint( g );
            }
        } finally {
            g.popContext();
            g.setDrawingStyle( Graphics.DRAWSTYLE_FOCUS, oldDrawStyleFocus );
        }
    }

    public void paint(Graphics g) {		 
    	int oldColour = g.getColor();
    	try {           
    		Bitmap currentDropDownBitmap = g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? dropDownBitmapFocus : dropDownBitmap;
    		
    		//Draw the drop down menu bitmap
    		int imageWidth = currentDropDownBitmap.getWidth();
    		int imageHeight = currentDropDownBitmap.getHeight();
    		int imageY =  (this.fieldMaxHeight - imageHeight) / 2; 		
    		int imageX = this.fieldMaxWeight  - ( imageWidth + PADDING + PADDING ); 
    		g.drawBitmap(imageX, imageY, imageWidth, imageHeight, currentDropDownBitmap, 0, 0);
    		
    		Font fnt = Font.getDefault().derive(Font.PLAIN);
    		int fullTextWidth = fnt.getAdvance(label);
    		int maxFontHeight = fnt.getHeight() * 2 ; //Set the max hight of the font to double of the device font height. We don't want giants here! 
    		//int maxFontSizeInPixel = Ui.convertSize(21, Ui.UNITS_pt, Ui.UNITS_px);
    		
    		int availableWidthForText =  this.fieldMaxWeight - imageWidth - (PADDING * 3) - 2; // PAD + text + ( 2 px ) + IMG + PAD + PAD
    		int availableHeightForText =  this.fieldMaxHeight - (PADDING * 2);
    		
    		//Iterate to find the best font size height that fit the width of the row
    		if( fullTextWidth < availableWidthForText && fnt.getHeight() < availableHeightForText && fnt.getHeight() <  maxFontHeight )  {
    			while(  fullTextWidth < availableWidthForText && fnt.getHeight() < availableHeightForText && fnt.getHeight() <  maxFontHeight ) {
    				fnt = fnt.derive( fnt.getStyle(),  fnt.getHeight() + 1 ); 
    				fullTextWidth =  fnt.getAdvance(label);
    			}
    			fnt = fnt.derive( fnt.getStyle(),  fnt.getHeight() - 1 );
    		} else {
    			//nothing for now
    		}
    		g.setFont(fnt);
    		g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_TEXT_FOCUS ) : getColour( COLOUR_TEXT ) );
    		int textTop =  (this.fieldMaxHeight - fnt.getHeight()) / 2;
    		g.drawText( label, PADDING, textTop , DrawStyle.TOP | DrawStyle.ELLIPSIS, availableWidthForText);
    		
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
		 _pressed = true;
		 invalidate();
		 fieldChangeNotify( 0 );
		 _pressed = false;
		 invalidate();
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
  	
    protected boolean navigationUnclick(int status, int time) {
        _pressed = false;
        invalidate();
        return true;
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
  		if(eventCode == TouchEvent.CLICK) {
  			Log.trace("TouchEvent.CLICK");
  			performDefaultActionOnItem();
  			return true;
  		} else if ( eventCode == TouchEvent.UNCLICK) {
  	        _pressed = false;
  	        invalidate();
  	        return true;
  		}

  		//return false;
  		return super.touchEvent(message);
  	}
  	//#endif
	 
	public String[] getChoices() {
		return choices;
	}
	public void setChoices(String[] choices) {
		this.choices = choices;
	}

	public void setSelectedIndex(int sel) {
		this.label = choices[sel];
		this.invalidate();
	}
}
