//#preprocess
package com.wordpress.view.component;


import com.wordpress.utils.ImageManipulator;
import com.wordpress.view.MainView;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.Touchscreen;
//#endif
import net.rim.device.api.ui.UiApplication;


public class BlogRefreshButtonField extends BaseButtonField
{
    
	Bitmap[] _bitmaps;
	private GIFEncodedImage[] animatedBitmaps;
	private int _currentFrame;          //The current frame in  the animation sequence.
	private AnimatorThread _animatorThread;
	private boolean isAnimating = false;
	
	private static final int NORMAL = 0;
    private static final int FOCUS = 1;
    
    public static final int PADDING = initializePadding();
    private static int initializePadding() {
    	//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
    	if (Touchscreen.isSupported()) {
    		return 10;    		
    	}  
    	//#endif
    	return Display.getWidth() > 360 ? 7 : 5;
    }
    
    private static final int BEVEL    = 2;
	private static final long COLOUR_BORDER              = 0xc5fd60b0047307a1L;
	private static final long COLOUR_BORDER_FOCUS        = 0xc5fd60b0047337a1L;
	private static final long COLOUR_TEXT                = 0x16a6e940230dba6bL;
	private static final long COLOUR_TEXT_FOCUS          = 0xe208bcf8cb684c98L;
	private static final long COLOUR_BACKGROUND          = 0x8d733213d6ac8b3bL;
	private static final long COLOUR_BACKGROUND_FOCUS    = 0x3e2cc79e4fd151d3L; 
	
	private boolean _pressed;
    private int fieldMaxSize;
    private int _width;
    private int _height;
    
    
    public BlogRefreshButtonField(  )
    {        
        super( Field.FOCUSABLE );
		GIFEncodedImage icon = (GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-blog-gif.bin");
		GIFEncodedImage focusIcon = (GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-blog-gif.bin");
		animatedBitmaps = new GIFEncodedImage[] { icon, focusIcon };
    }
    
    public int getColour( long colourKey ) 
    {
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
    
    
    private void createRefreshIcon( int iconSize ) {
    	if ( iconSize <= 32 ) {
	    	animatedBitmaps = new GIFEncodedImage[] { 
					(GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-blog-small-gif.bin"),
					(GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-blog-small-gif.bin")
					};
    	} else {
    		animatedBitmaps = new GIFEncodedImage[] { 
    				(GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-blog-gif.bin"),
    				(GIFEncodedImage)EncodedImage.getEncodedImageResource("loading-blog-gif.bin")
    		};
    	}
 
    	Bitmap icon = null;
    	Bitmap focusIcon = null;
    	
		if ( iconSize >=  72 ) {
    		icon = Bitmap.getBitmapResource("icon_titlebar_refresh_72px.png");	
    		focusIcon = Bitmap.getBitmapResource("icon_titlebar_refresh_focus_72px.png");
    	} else if ( iconSize >= 48 ) {
    		icon = Bitmap.getBitmapResource("icon_titlebar_refresh_48px.png");	
    		focusIcon = Bitmap.getBitmapResource("icon_titlebar_refresh_focus_48px.png");
    	} else if ( iconSize >= 32 ) {
    		icon = Bitmap.getBitmapResource("icon_titlebar_refresh_32px.png");	
    		focusIcon = Bitmap.getBitmapResource("icon_titlebar_refresh_focus_32px.png");
    	} else {
    		icon = Bitmap.getBitmapResource("icon_titlebar_refresh_26px.png");	
    		focusIcon = Bitmap.getBitmapResource("icon_titlebar_refresh_focus_26px.png");
    	} 
    	
    	if( icon.getWidth() > iconSize ) {
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

    	if( focusIcon.getWidth() > iconSize ) {
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
    }
    
    
	protected void layout(int width, int height) {
		this.fieldMaxSize = MainView.getHeaderChildsMaxHeight();
		createRefreshIcon( fieldMaxSize - ( 2* PADDING ) );
		_width = fieldMaxSize;
        _height = fieldMaxSize;
		setExtent(fieldMaxSize, fieldMaxSize);
	}
       
    public int getPreferredWidth() {
        return fieldMaxSize != 0 ? fieldMaxSize : MainView.getHeaderChildsMaxHeight();
    }
    
    public int getPreferredHeight() {
    	 return fieldMaxSize != 0 ? fieldMaxSize : MainView.getHeaderChildsMaxHeight();
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
    
    /**
     * A public way to click this button
     */
    public void clickButton() 
    {
    	if( isAnimating ) return;
    	fieldChangeNotify( 0 );
    }
    
    protected void paint( Graphics g ) {
    	int index = g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? FOCUS : NORMAL;
    	if( isAnimating == false ){
    		g.drawBitmap( ( ( fieldMaxSize - _bitmaps[index].getWidth() ) / 2 ),  ( fieldMaxSize - _bitmaps[index].getWidth() ) / 2, _bitmaps[index].getWidth(), _bitmaps[index].getHeight(), _bitmaps[index], 0, 0 );
    	} else {
    		//Draw the animation frame.
    		g.drawImage( ( ( fieldMaxSize - animatedBitmaps[index].getWidth() ) / 2 ) + animatedBitmaps[index].getFrameLeft(_currentFrame), 
    				( ( fieldMaxSize - animatedBitmaps[index].getWidth() ) / 2 ) + animatedBitmaps[index].getFrameTop(_currentFrame),
    				animatedBitmaps[index].getFrameWidth(_currentFrame), animatedBitmaps[index].getFrameHeight(_currentFrame), animatedBitmaps[index], _currentFrame, 0, 0);
    	}
    }
    
    protected void paintBackground( Graphics g)
    {
        int oldColour = g.getBackgroundColor();
        int oldAlpha = g.getGlobalAlpha();
        try {
            // Border
        	g.setColor(  g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BORDER_FOCUS ) : getColour( COLOUR_BORDER ) );
            g.fillRect( 1, 0, _width - 2, _height );
            g.fillRect( 0, 1, _width,     _height - 2 );
            
            // Base color
            g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BACKGROUND_FOCUS ) : getColour( COLOUR_BACKGROUND ) );
            g.fillRect( 1, 1, _width - 2, _height - 2 );
            
            // Highlight and lowlight
            g.setGlobalAlpha( 0x44 );
            g.setColor( _pressed ? Color.BLACK : Color.WHITE );
            g.fillRect( 1, 1, _width - 2, BEVEL );
            if (  g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ) {
            	g.setColor( _pressed ? Color.WHITE : Color.BLACK );
            	g.fillRect( 0, _height - BEVEL - 1, _width, BEVEL );
            } else {
            	g.setColor( Color.GRAY );
            	g.fillRect( 0, _height - BEVEL - 1, _width, BEVEL );
            }
            
            // Base color
            g.setGlobalAlpha( 0xFF );
            g.setColor( g.isDrawingStyleSet( Graphics.DRAWSTYLE_FOCUS ) ? getColour( COLOUR_BACKGROUND_FOCUS ) : getColour( COLOUR_BACKGROUND ) );
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
    
    protected void onDisplay()
    {
    	if( isAnimating ) {
	    	//Start the animation thread.
	    	_animatorThread = new AnimatorThread(this);
	    	_animatorThread.start();
    	}
    	super.onDisplay();
    }

    
    //Stop the animation thread when the screen the field is on is
    //popped off of the display stack.
    protected void onUndisplay()
    {
        if( isAnimating ) {
        	_animatorThread.stop();
        	_animatorThread = null;
        }
        super.onUndisplay();
    }
    
    public void startAnimation() {
    	if ( isAnimating ) return; //already animating
    	isAnimating = true;
    	//Start the animation thread.
    	_animatorThread = new AnimatorThread(this);
    	_animatorThread.start();
    }
    
    public void stopAnimation() {
    	if ( isAnimating == false ) return;
    	isAnimating = false;
    	_animatorThread.stop();
    	_animatorThread = null;
    	_currentFrame = 0;
    	this.invalidate();
    }
    
    //A thread to handle the animation.
    private class AnimatorThread extends Thread
    {
        private BlogRefreshButtonField _theField;
        private boolean _keepGoing = true;
        private int _totalFrames;     //The total number of frames in the image.
        private int _loopCount;       //The number of times the animation has looped (completed).
        private int _totalLoops;      //The number of times the animation should loop (set in the image).

        public AnimatorThread(BlogRefreshButtonField theField)
        {
            _theField = theField;
            _totalFrames = animatedBitmaps[0].getFrameCount();
            _totalLoops = animatedBitmaps[0].getIterations();

        }

        public synchronized void stop()
        {
            _keepGoing = false;
        }

        public void run()
        {
        	
            while(_keepGoing)
            {
                //Invalidate the field so that it is redrawn.
                UiApplication.getUiApplication().invokeAndWait(new Runnable()
                {
                    public void run()
                    {
                        _theField.invalidate();
                    }
                });

                try
                {
                    //Sleep for the current frame delay before
                    //the next frame is drawn.
                    sleep(animatedBitmaps[0].getFrameDelay(_currentFrame) * 10);
                }
                catch (InterruptedException iex)
                {} //Couldn't sleep.

                //Increment the frame.
                ++_currentFrame;

                if (_currentFrame == _totalFrames)
                {
                    //Reset back to frame 0 if we have reached the end.
                    _currentFrame = 0;

                    ++_loopCount;

                    //Check if the animation should continue.
                    if (_loopCount == _totalLoops)
                    {
                        _keepGoing = false;
                    }
                }
            }
        }
    }
}
