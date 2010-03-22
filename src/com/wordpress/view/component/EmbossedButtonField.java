//#preprocess
package com.wordpress.view.component;

import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.Touchscreen;
//#endif
import net.rim.device.api.util.LongIntHashtable;

/**
 * A custom button field
 */
public class EmbossedButtonField extends BaseButtonField
{
    public static final long COLOUR_BORDER              = 0xc5fd60b0047307a1L; //net.rim.device.api.ui.component.EmbossedButtonField.COLOUR_BORDER
    public static final long COLOUR_TEXT                = 0x16a6e940230dba6bL; //net.rim.device.api.ui.component.EmbossedButtonField.COLOUR_TEXT
    public static final long COLOUR_TEXT_FOCUS          = 0xe208bcf8cb684c98L; //net.rim.device.api.ui.component.EmbossedButtonField.COLOUR_TEXT_FOCUS
    public static final long COLOUR_BACKGROUND          = 0x8d733213d6ac8b3bL; //net.rim.device.api.ui.component.EmbossedButtonField.COLOUR_BACKGROUND
    public static final long COLOUR_BACKGROUND_FOCUS    = 0x3e2cc79e4fd151d3L; //net.rim.device.api.ui.component.EmbossedButtonField.COLOUR_BACKGROUND_FOCUS
    
    private static final int XPADDING = initializeXPadding();
    private static final int YPADDING = initializeYPadding();
    
    private static int initializeXPadding() {
    	//#ifdef IS_OS47_OR_ABOVE
    	if (Touchscreen.isSupported()) {
    		return 14;    		
    	}  
    	//#endif
    	return Display.getWidth() > 320 ? 7 : 5;
    }

    private static int initializeYPadding() {
    	//#ifdef IS_OS47_OR_ABOVE
    	if (Touchscreen.isSupported()) {
    		return 8;    		
    	}
    	//#endif
    	return Display.getWidth() > 320 ? 4 : 3;
    }

    private static final int BEVEL    = 2;
    
    private LongIntHashtable _colourTable;
    private Font _buttonFont;
    private String _text;
    private boolean _pressed;
    
    private int _width;
    private int _height;
    
    public EmbossedButtonField( String text )
    {
        this(text, 0);
    }
    
    public EmbossedButtonField( String text, long style )
    {
        this( text, style, null );
    }
    
    public EmbossedButtonField( String text, long style, LongIntHashtable colourTable )
    {
        super( Field.FOCUSABLE | style);
        _text = text;
        setColourTable( colourTable );
    }

    public void setColourTable( LongIntHashtable colourTable )
    {
        _colourTable = colourTable;
        invalidate();
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
            return isStyle( Field.READONLY ) ? 0x5292f7 : 0x185AB5;
        } else {
            throw new IllegalArgumentException();
        }
    }
    
    public void setText(String text)
    {
        _text = text;
    }
    
    public void applyFont()
    {
        _buttonFont = getFont().derive(Font.BOLD);
    }
    
    public int getPreferredWidth()
    {
        return 2 * XPADDING + _buttonFont.getAdvance( _text );
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
    
    public int getPreferredHeight()
    {
        return 2 * YPADDING + _buttonFont.getHeight();
    }
        
    protected void layout( int width, int height )
    {
    	if(isStyle(USE_ALL_WIDTH)) {
    		setExtent( width , getPreferredHeight() );	
    	} else {
    		int tmpW = getPreferredWidth();
    		if (tmpW > width) 
    			tmpW = width;
    		setExtent(tmpW , getPreferredHeight() );
    	}
    	
        _width = getWidth();
        _height = getHeight();
    }
    
    protected void paint( Graphics g )
    {
        int oldColour = g.getColor();
        Font oldFont = g.getFont();
        try {
            g.setFont(_buttonFont);
            g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_TEXT_FOCUS ) : getColour( COLOUR_TEXT ) );
            g.drawText( _text, 0, YPADDING, DrawStyle.HCENTER | DrawStyle.ELLIPSIS, _width ); 
        } finally {
            g.setFont( oldFont );
            g.setColor( oldColour );
        }
    }
    
    protected void paintBackground( Graphics g)
    {
        int oldColour = g.getBackgroundColor();
        int oldAlpha = g.getGlobalAlpha();
        try {
            // Border
            g.setColor( getColour( COLOUR_BORDER ) );
            g.fillRect( 1, 0, _width - 2, _height );
            g.fillRect( 0, 1, _width,     _height - 2 );
            
            // Base color
            g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BACKGROUND_FOCUS ) : getColour( COLOUR_BACKGROUND ) );
            g.fillRect( 1, 1, _width - 2, _height - 2 );
            
            // Highlight and lowlight
            g.setGlobalAlpha( 0x44 );
            g.setColor( _pressed ? Color.BLACK : Color.WHITE );
            g.fillRect( 1, 1, _width - 2, BEVEL );
            g.setColor( _pressed ? Color.WHITE : Color.BLACK );
            g.fillRect( 0, _height - BEVEL - 1, _width, BEVEL );
            
            // Base color
            g.setGlobalAlpha( 0xFF );
            g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BACKGROUND_FOCUS ) : getColour( COLOUR_BACKGROUND ) );
            g.fillRect( 2, 2, _width - 4, _height - 4 );
            
        } finally {
            g.setBackgroundColor( oldColour );
            g.setGlobalAlpha( oldAlpha );
        }
    }
}
