package com.wordpress.view.component;

import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.ui.Graphics;

/**
 * Provides common code for rendering an animated GIF. inside various fields.
 */
public class ThrobberRenderer {
	
    private GIFEncodedImage _image;     //The image to draw.
    private int _currentFrame;          //The current frame in  the animation sequence.
    private int _totalFrames;
    private int _width;                 //The width of the image (background frame).
    private int _height;                //The height of the image (background frame).	

	public ThrobberRenderer(GIFEncodedImage image) {
        //Store the image and it's dimensions.
        _image = image;
        _width = image.getWidth();
        _height = image.getHeight();
        _totalFrames = _image.getFrameCount();
	}
	
	protected void paint(Graphics graphics)
	{
		//Draw the animation frame.
		graphics.drawImage(_image.getFrameLeft(_currentFrame), _image.getFrameTop(_currentFrame),
				_image.getFrameWidth(_currentFrame), _image.getFrameHeight(_currentFrame), _image, _currentFrame, 0, 0);
	}


	/**
	 * Resets the graphic to the starting position.
	 */
	public void resetPosition() {
		_currentFrame = 0;;
	}
	
	/**
	 * Advances the graphic to the next position.
	 */
	public void nextPosition() {
        //Increment the frame.
        ++_currentFrame;

        if (_currentFrame == _totalFrames)
        {
            //Reset back to frame 0 if we have reached the end.
        	resetPosition();
        }
	}
}
