//#preprocess

//#ifdef BlackBerrySDK7.0.0

package com.wordpress.view.reader;

import javax.microedition.io.InputConnection;

import com.wordpress.utils.log.Log;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldErrorHandler;
import net.rim.device.api.browser.field2.BrowserFieldRequest;

/**
 * This <code>BrowserFieldErrorHandler</code> implementation is designed 
 * to be used in debug purposes.
 */
public class ReaderBrowserFieldErrorHandler extends BrowserFieldErrorHandler {

    public ReaderBrowserFieldErrorHandler(BrowserField browserField) {
        super(browserField);
    }

    public void displayContentError(String url, InputConnection connection,
            Throwable t) {
        Log.error("*** displayContentError: " + t.getMessage());
        super.displayContentError(url, connection, t);
    }

    public void displayContentError(String url, String errorMessage) {
    	Log.error("*** displayContentError: " + errorMessage);
        super.displayContentError(url, errorMessage);
    }

    public BrowserField getBrowserField() {
        return super.getBrowserField();
    }

    public void navigationRequestError(BrowserFieldRequest request, Throwable t) {
    	Log.error("*** navigationRequestError: " + t.getMessage());
        super.navigationRequestError(request, t);
    }

    public void requestContentError(BrowserFieldRequest request, Throwable t) {
    	Log.error("*** requestContentError: " + t.getMessage());
        super.requestContentError(request, t);
    }

    public InputConnection resourceRequestError(BrowserFieldRequest request,
            Throwable t) {
    	Log.error("*** resourceRequestError: " + t.getMessage());
        return super.resourceRequestError(request, t);
    }
}
//#endif