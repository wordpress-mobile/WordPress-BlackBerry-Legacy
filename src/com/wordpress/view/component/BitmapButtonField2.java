package com.wordpress.view.component;


import net.rim.device.api.system.*;
import net.rim.device.api.ui.*;

public class BitmapButtonField2 extends BaseButtonField
{
    private Bitmap[] _bitmaps;
    private static final int NORMAL = 0;
    private static final int FOCUS = 1;
    
    public BitmapButtonField2( Bitmap normalState )
    {        
        this( normalState, normalState, 0 );
    }
    
    public BitmapButtonField2( Bitmap normalState, Bitmap focusState )
    {        
        this( normalState, focusState, 0 );
    }
    
    public BitmapButtonField2( Bitmap normalState, Bitmap focusState, long style )
    {        
        super( Field.FOCUSABLE | style );
        
        if( (normalState.getWidth() != focusState.getWidth())
            || (normalState.getHeight() != focusState.getHeight()) ){
            
            throw new IllegalArgumentException( "Image sizes don't match" );
        }
        
        _bitmaps = new Bitmap[] { normalState, focusState };
    }
    
    public void setImage( Bitmap normalState ){
        _bitmaps[NORMAL] = normalState;
        invalidate();
    }
    
    public void setFocusImage( Bitmap focusState ){
        _bitmaps[FOCUS] = focusState;
        invalidate();
    }
    
    public int getPreferredWidth() {
        return _bitmaps[NORMAL].getWidth();
    }
    
    public int getPreferredHeight() {
        return _bitmaps[NORMAL].getHeight();
    }
    
    protected void layout( int width, int height ) {
        setExtent( _bitmaps[NORMAL].getWidth(), _bitmaps[NORMAL].getHeight() );
    }
    
    protected void paint( Graphics g ) {
        int index = g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? FOCUS : NORMAL;
        g.drawBitmap( 0, 0, _bitmaps[index].getWidth(), _bitmaps[index].getHeight(), _bitmaps[index], 0, 0 );
    }
    
    /**
     * With this commented out the default focus will show through
     * If an app doesn't want focus colours then it should override this and do nothing
     **/
    /*
    protected void paintBackground( Graphics g ) {
        // Nothing to do here
    }
    */
    
    protected void drawFocus( Graphics g, boolean on ) {
        // Paint() handles it all
        g.setDrawingStyle( Graphics.DRAWSTYLE_FOCUS, true );
        paintBackground( g );
        paint( g );
    }
}

