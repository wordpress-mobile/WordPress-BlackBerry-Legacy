package com.wordpress.view.component;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;

import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.HTTPGetConn;

public class WebBitmapField extends BitmapField implements Observer  
{  
    private EncodedImage bitmap = null;  
    private HTTPGetConn connection = null;
  
    public WebBitmapField(String url, EncodedImage imgLoading,  long style)
    {  
    	super(imgLoading.getBitmap(), style);
    	Log.trace("Building WebBitmapField with URL: "+url);
    	  
        try  
        {  
    		connection = new HTTPGetConn(url, "", "");
	        connection.addObserver(this);  	       
	        connection.startConnWorkBackground(); //starts connection
        }  
        catch (Exception e) {}  
    }  
  
    //Invoked when the screen this field is attached to is popped off the display stack. 
    protected void onUndisplay() {
    	if (connection != null)
    		connection.stopConnWork();
        super.onUndisplay();
    }
    
    public Bitmap getBitmap()  
    {  
        if (bitmap == null) return null;  
        return bitmap.getBitmap();  
    }  
  
    
    public void update(Observable observable, final Object object) {

    	BlogConnResponse resp = (BlogConnResponse) object;

    	if(resp.isStopped()){
    		return;
    	}

    	if(!resp.isError()) {						
    		try {
    			final byte[] response = (byte[]) resp.getResponseObject();

    			UiApplication.getUiApplication().invokeLater(new Runnable() {
    				public void run() {
    					bitmap = EncodedImage.createEncodedImage(response, 0,  
    							response.length);  
    					setImage(bitmap);  
    				}
    			});
    		} catch (Exception e) {
    			Log.error(e, "The URL resource was downloaded, but the WebBitmap failed to set the img in the field");
    		}						

    	} else {
    		final String respMessage = resp.getResponse();
    		Log.error(respMessage);
    	}
    }
}  