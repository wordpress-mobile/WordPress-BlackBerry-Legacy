//#preprocess
package com.wordpress.view.component;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.MainController;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.ImageManipulator;
import com.wordpress.utils.log.Log;
import com.wordpress.view.MainView;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.XYRect;
import net.rim.device.api.ui.component.LabelField;
//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.util.LongIntHashtable;

public class BlogSelectorField extends LabelField {

	public static final int PADDING = 5;

	protected ResourceBundle _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	protected String contextMenuItemLabel = _resources.getString(WordPressResource.MENUITEM_OPEN);

	//Default icons used as blog's status
	private Bitmap imgImportant = Bitmap.getBitmapResource("important.png");
	private Bitmap wp_blue = Bitmap.getBitmapResource("wordpress-logo-100-blue.png");
	private Bitmap wp_grey = Bitmap.getBitmapResource("wordpress-logo-100-grey.png");
	private Bitmap pendingActivation = Bitmap.getBitmapResource("pending_activation.png"); //not used yet
	protected Bitmap dropDownBitmap = Bitmap.getBitmapResource("drop_down_arrow.png");
	protected Bitmap dropDownBitmapFocus = Bitmap.getBitmapResource("drop_down_arrow_white.png");
	
	private static final int BEVEL    = 2;
	private static final long COLOUR_BORDER              = 0xc5fd60b0047307a1L;
	private static final long COLOUR_BORDER_FOCUS        = 0xc5fd60b0047337a1L;
	private static final long COLOUR_TEXT                = 0x16a6e940230dba6bL;
	private static final long COLOUR_TEXT_FOCUS          = 0xe208bcf8cb684c98L;
	private static final long COLOUR_BACKGROUND          = 0x8d733213d6ac8b3bL;
	private static final long COLOUR_BACKGROUND_FOCUS    = 0x3e2cc79e4fd151d3L; 
	
	private String[] choices;
	private int selectedIndex;
	private String label;
	private Bitmap blogIcon;
	private int fieldMaxHeight = 0;
	private int fieldMaxWeight = 0;
    private LongIntHashtable _colourTable;
	private boolean _pressed;
	
	
	public void invalidate_hack() {
		BlogInfo[] blogCaricati = MainController.getIstance().getApplicationBlogs();
		blogIcon = createBlogIconField(blogCaricati[selectedIndex]);
		this.invalidate();
	}
	
	protected void layout(int width, int height) {
		this.fieldMaxWeight = width;
		this.fieldMaxHeight = MainView.getHeaderChildsMaxHeight();
		BlogInfo[] blogCaricati = MainController.getIstance().getApplicationBlogs();
		blogIcon = createBlogIconField(blogCaricati[selectedIndex]);
		super.layout(width, fieldMaxHeight);
		setExtent(width, fieldMaxHeight);
	}
	
    protected void makeContextMenu(ContextMenu contextMenu) {
      //remove the context menu
    }

	public BlogSelectorField(String[] choices, int iSetTo, long style) {
		super(choices[iSetTo], style);
		this.selectedIndex = iSetTo;
		this.label = choices[iSetTo];
		this.choices = choices;
	}

    public int getColour( long colourKey ) 
    {
        if( _colourTable != null ) {
            int colourValue = _colourTable.get( colourKey );
            if( colourValue >= 0 ) {
                return colourValue;
            }
        }
            
        if( colourKey == COLOUR_BORDER ) {
            return 0x979797;
        } else if( colourKey == COLOUR_BORDER_FOCUS ) {
            return 0x212121;
        } else if( colourKey == COLOUR_TEXT ) {
            return 0x323232;
        } else if( colourKey == COLOUR_TEXT_FOCUS ) {
            return Color.WHITE;
        } else if( colourKey == COLOUR_BACKGROUND ) {
            return 0xe0e0e0;
        } else if( colourKey == COLOUR_BACKGROUND_FOCUS ) {
            return 0x21759b;
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
    		g.setColor(  g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BORDER_FOCUS ) : getColour( COLOUR_BORDER ) );
    		g.fillRect( 1, 0, fieldMaxWeight - 2, fieldMaxHeight );
    		g.fillRect( 0, 1, fieldMaxWeight,     fieldMaxHeight - 2 );

    		// Base color
    		g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BACKGROUND_FOCUS ) : getColour( COLOUR_BACKGROUND ) );
    		g.fillRect( 1, 1, fieldMaxWeight - 2, fieldMaxHeight - 2 );

    		// Highlight and lowlight
    		g.setGlobalAlpha( 0x44 );
    		g.setColor( _pressed ? Color.BLACK : Color.WHITE );
    		g.fillRect( 1, 1, fieldMaxWeight - 2, BEVEL );
    		if (  g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ) {
    			g.setColor( _pressed ? Color.WHITE : Color.BLACK );
    			g.fillRect( 0, fieldMaxHeight - BEVEL - 1, fieldMaxWeight, BEVEL ); 
    		} else {
    			g.setColor( Color.GRAY );
    			g.fillRect( 0, fieldMaxHeight - BEVEL - 1, fieldMaxWeight, BEVEL );
    		}
    		
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
    
    // PADDING + blogIcon + PADDING + label + PADDING + dropdown + PADDING
    public void paint(Graphics g) {
    	int oldColour = g.getColor();
    	try {           
    		//Draw the drop down menu bitmap
    		int imageWidth = blogIcon.getWidth();
    		int imageHeight = blogIcon.getHeight();
    		int imageY =  (this.fieldMaxHeight - imageHeight) / 2; 		
    		int imageX = PADDING; 
    		g.drawBitmap(imageX, imageY, imageWidth, imageHeight, blogIcon, 0, 0);
    		
    		//Draw the blog icon
    		Bitmap currentDropDownBitmap = g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? dropDownBitmapFocus : dropDownBitmap;
    		imageWidth = currentDropDownBitmap.getWidth();
    		imageHeight = currentDropDownBitmap.getHeight();
    		imageY =  (this.fieldMaxHeight - imageHeight) / 2; 		
    		imageX = this.fieldMaxWeight  - ( imageWidth + PADDING ); 
    		g.drawBitmap(imageX, imageY, imageWidth, imageHeight, currentDropDownBitmap, 0, 0);
    		
    		//Draw the text
    		Font fnt = Font.getDefault().derive(Font.PLAIN, blogIcon.getHeight() - 8 );
    		int availableWidthForText =  this.fieldMaxWeight - currentDropDownBitmap.getWidth() - blogIcon.getWidth() - ( 4 * PADDING );
    		g.setFont(fnt);
    		g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_TEXT_FOCUS ) : getColour( COLOUR_TEXT ) );
    		int textTop =  (this.fieldMaxHeight - fnt.getHeight()) / 2;
    		g.drawText( label, PADDING +  blogIcon.getWidth() + PADDING, textTop , DrawStyle.TOP | DrawStyle.ELLIPSIS, availableWidthForText);
    		
    	} finally {
    		g.setColor( oldColour );
    	}
    }	 
	/*    
    public void setDirty( boolean dirty ) {
        // We never want to be dirty or muddy
    }
     
    public void setMuddy( boolean muddy ) {
        // We never want to be dirty or muddy
    }
     */    
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
  				} else if ( gestureCode == TouchGesture.HOVER ) {
  					//do not show the context menu
  					return true;
  				}
  			} 
  			return false;
  		} else {
  			if(eventCode == TouchEvent.CLICK) {
  				performDefaultActionOnItem();
  				return true;
  			} else if ( eventCode == TouchEvent.UNCLICK) {
  				_pressed = false;
  				invalidate();
  				return true;
  			}
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
		this.selectedIndex = 0;
	}
	
	public void setSelectedIndex(int sel) {
		this.selectedIndex = sel;
		this.label = choices[sel];
		BlogInfo[] blogCaricati = MainController.getIstance().getApplicationBlogs();
		blogIcon = createBlogIconField(blogCaricati[selectedIndex]);
		this.invalidate();
	}
	
	 private Bitmap createBlogIconField( BlogInfo currentRow ){
		 int stato = currentRow.getState();
		 int maxIconWidth = MainView.getBlogIconSize();
		 Bitmap icon = null;
		 if(stato == BlogInfo.STATE_PENDING_ACTIVATION) {
			 icon = pendingActivation;
		 } else if ( stato == BlogInfo.STATE_LOADING || stato == BlogInfo.STATE_ADDED_TO_QUEUE ) { 
			 // do nothing
		 } else if (stato == BlogInfo.STATE_LOADED_WITH_ERROR ||  stato == BlogInfo.STATE_ERROR) {
			 icon = imgImportant;
		 } else if( stato == BlogInfo.STATE_LOADED ) {
			 if(currentRow.getBlogIcon() != null) {
				 try {
					 icon =  Bitmap.createBitmapFromPNG(currentRow.getBlogIcon(), 0, -1);
					// BitmapField  test = new BitmapField( icon, Field.NON_FOCUSABLE | FIELD_HCENTER | FIELD_VCENTER );
					// test.setBorder(BorderFactory.createRoundedBorder(new XYEdges(6,6,6,6)));
					// return test;
				 } catch (Exception e) {
					 Log.error("no valid shortcut ico found in the blog obj");
				 }
			 }
		 } 
		 
		 //still null there was an error during img generation process
		 //or it is a fresh blog
		 if( icon == null) {
			 if(currentRow.isWPCOMBlog()) {
				 icon = wp_blue;
			 } else {
				 icon = wp_grey;
			 }
		 }
		 
		 if( icon.getWidth() != maxIconWidth ) {
			// Calculate the new scale based on the region sizes
				// Scale / Zoom
				// 0.1 = 1000%
				// 0.5 = 200%
				// 1 = 100%
				// 2 = 50%
				// 4 = 25%
			int	resultantScaleX = Fixed32.div(Fixed32.toFP(maxIconWidth), Fixed32.toFP(icon.getWidth()));
			icon = ImageManipulator.scale(icon, resultantScaleX);
		 }
	 
		 return icon;
	 }
	
}
