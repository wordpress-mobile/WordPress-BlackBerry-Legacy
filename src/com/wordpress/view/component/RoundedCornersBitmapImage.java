package com.wordpress.view.component;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.BitmapField;

public class RoundedCornersBitmapImage extends BitmapField{
	
	public RoundedCornersBitmapImage(Bitmap icon, long l) {
		super(icon, l);
	}

	protected void paint(Graphics graphics) {
		Bitmap bmp = this.getBitmap();

		//http://supportforums.blackberry.com/t5/Java-Development/Rounded-rectangle-clipping-area/td-p/508349
		
		int w = bmp.getWidth();
		int h = bmp.getHeight();
		int arc = 14;

		int xPts[] = {0,         0,         arc,        //top left corner
				w-arc,     w,         w,            //top right corner
				w,         w,         w-arc,        //bottom right corner
				arc,         0 ,     0};            //bottom left corner    

		int yPts[] = {arc,        0,        0,            //top left corner
				0,        0,         arc,        //top right corner
				h-arc,    h,        h,            //bottom right corner
				h,        h,        h-arc};        //bottom left corner

		//To be honest, i don't quite get what the following 5 lines are doing... ^_^           
		int fAngle = Fixed32.toFP(0);
		int dvx = Fixed32.cosd(fAngle);
		int dux = -Fixed32.sind(fAngle);
		int dvy = Fixed32.sind(fAngle);         
		int duy = Fixed32.cosd(fAngle);
		byte end = Graphics.CURVEDPATH_END_POINT;
		byte curve = Graphics.CURVEDPATH_QUADRATIC_BEZIER_CONTROL_POINT;

		//each corner has 3 points:
		//the 2 end points and the middle point is draw as a curve.
		byte types [] = { end, curve, end,
				end, curve, end,
				end, curve, end,
				end, curve, end
				};

		graphics.drawTexturedPath(xPts, yPts, types, null, 0, 0, dvx, dux, dvy, duy, bmp);
		
	}
}
