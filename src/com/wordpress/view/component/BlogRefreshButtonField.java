package com.wordpress.view.component;

import com.wordpress.utils.ImageManipulator;
import com.wordpress.view.GUIFactory;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.util.LongIntHashtable;


public class BlogRefreshButtonField extends BaseButtonField
{
    
	private Bitmap[] _bitmaps;
	private int fieldMaxSize;
    private static final int NORMAL = 0;
    private static final int FOCUS = 1;
    private static final int PADDING = 5;
    
    private static final int BEVEL    = 2;
    
    private LongIntHashtable _colourTable;
    private boolean _pressed;
    
    private int _width;
    private int _height;
    
   
    public BlogRefreshButtonField(  )
    {        
        super( Field.FOCUSABLE );
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
        if( colourKey == EmbossedButtonField.COLOUR_BORDER ) {
            return 0x212121;
        } else if( colourKey == EmbossedButtonField.COLOUR_TEXT ) {
            return 0xD6D6D6;
        } else if( colourKey == EmbossedButtonField.COLOUR_TEXT_FOCUS ) {
            return Color.WHITE;
        } else if( colourKey == EmbossedButtonField.COLOUR_BACKGROUND ) {
            return isStyle( Field.READONLY ) ? 0x777777 : 0x424242;
        } else if( colourKey == EmbossedButtonField.COLOUR_BACKGROUND_FOCUS ) {
            return isStyle( Field.READONLY ) ? GUIFactory.BTN_COLOUR_BACKGROUND_FOCUS : 0x185AB5;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    
	protected void layout(int width, int height) {
		
		/* Reload the bitmap here */
		createRefreshIcon( fieldMaxSize - ( 2* PADDING ) );
        _width = fieldMaxSize;
        _height = fieldMaxSize;
		//super.layout(fieldMaxSize, fieldMaxSize);
		setExtent(fieldMaxSize, fieldMaxSize);
	}
    
	private Bitmap createRefreshIcon( int iconSize ) {
		
		Bitmap icon = Bitmap.getBitmapResource("icon_titlebar_refresh.png");	
		if( icon.getWidth() != iconSize ) {
			// Calculate the new scale based on the region sizes
			// Scale / Zoom
			// 0.1 = 1000%
			// 0.5 = 200%
			// 1 = 100%
			// 2 = 50%
			// 4 = 25%
			int	resultantScaleX = Fixed32.div(Fixed32.toFP(iconSize), Fixed32.toFP(icon.getWidth()));
			icon = ImageManipulator.scale(icon, resultantScaleX);
		}
		
		Bitmap focusIcon = Bitmap.getBitmapResource("icon_titlebar_refresh.png");	
		if( focusIcon.getWidth() != iconSize ) {
			// Calculate the new scale based on the region sizes
			// Scale / Zoom
			// 0.1 = 1000%
			// 0.5 = 200%
			// 1 = 100%
			// 2 = 50%
			// 4 = 25%
			int	resultantScaleX = Fixed32.div(Fixed32.toFP(iconSize), Fixed32.toFP(focusIcon.getWidth()));
			focusIcon = ImageManipulator.scale(focusIcon, resultantScaleX);
		}
		
		_bitmaps = new Bitmap[] { icon, focusIcon };
		return icon;
	}
    
    public int getPreferredWidth() {
        return fieldMaxSize;
    }
    
    public int getPreferredHeight() {
        return fieldMaxSize;
    }
     
	public void setFieldMaxSize(int fieldMaxHeight) {
		this.fieldMaxSize = fieldMaxHeight;
	}

	
    protected void onUnfocus()
    {
        super.onUnfocus();
        if( _pressed ) {
            _pressed = false;
            invalidate();
        }
    }
    
    protected boolean navigationClick(int status, int time) {
        _pressed = true;
        invalidate();
        return super.navigationClick( status, time );
    }
    
    protected boolean navigationUnclick(int status, int time) {
        _pressed = false;
        invalidate();
        return true;
    }
	
    protected void paint( Graphics g ) {
        int index = g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? FOCUS : NORMAL;
        g.drawBitmap( PADDING, PADDING, _bitmaps[index].getWidth(), _bitmaps[index].getHeight(), _bitmaps[index], 0, 0 );
    }
    
    protected void paintBackground( Graphics g)
    {
        int oldColour = g.getBackgroundColor();
        int oldAlpha = g.getGlobalAlpha();
        try {
            // Border
            g.setColor( getColour( EmbossedButtonField.COLOUR_BORDER ) );
            g.fillRect( 1, 0, _width - 2, _height );
            g.fillRect( 0, 1, _width,     _height - 2 );
            
            // Base color
            g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( EmbossedButtonField.COLOUR_BACKGROUND_FOCUS ) : getColour( EmbossedButtonField.COLOUR_BACKGROUND ) );
            g.fillRect( 1, 1, _width - 2, _height - 2 );
            
            // Highlight and lowlight
            g.setGlobalAlpha( 0x44 );
            g.setColor( _pressed ? Color.BLACK : Color.WHITE );
            g.fillRect( 1, 1, _width - 2, BEVEL );
            g.setColor( _pressed ? Color.WHITE : Color.BLACK );
            g.fillRect( 0, _height - BEVEL - 1, _width, BEVEL );
            
            // Base color
            g.setGlobalAlpha( 0xFF );
            g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( EmbossedButtonField.COLOUR_BACKGROUND_FOCUS ) : getColour( EmbossedButtonField.COLOUR_BACKGROUND ) );
            g.fillRect( 2, 2, _width - 4, _height - 4 );
            
        } finally {
            g.setBackgroundColor( oldColour );
            g.setGlobalAlpha( oldAlpha );
        }
    }
    
    protected void drawFocus( Graphics g, boolean on ) {
        // Paint() handles it all
        g.setDrawingStyle( Graphics.DRAWSTYLE_FOCUS, true );
        paintBackground( g );
        paint( g );
    }
}

