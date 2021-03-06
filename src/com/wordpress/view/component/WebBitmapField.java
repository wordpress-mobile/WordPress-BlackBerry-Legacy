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
    protected EncodedImage bitmap = null;  
    protected HTTPGetConn connection = null;
    protected String URL = null;
  
    public WebBitmapField(String url, Bitmap imgLoading,  long style)
    {  
    	super(imgLoading, style);
    	Log.trace("Building WebBitmapField with URL: "+url);
    	 this.URL = url; 
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
    			bitmap = EncodedImage.createEncodedImage(response, 0,response.length);   
    		} catch (Exception e) {
    			Log.error(e, "The URL resource was downloaded, but the WebBitmap failed to set the img in the field");
    			bitmap = EncodedImage.getEncodedImageResource("mime_unknown.png");
    		}						

    	} else {
    		bitmap = EncodedImage.getEncodedImageResource("mime_unknown.png");
    		final String respMessage = resp.getResponse();
    		Log.error(respMessage);
    	}

    	UiApplication.getUiApplication().invokeLater(new Runnable() {
    		public void run() {
    			setImage(bitmap);  
    		}
    	});

    	connection = null;
    }
}  