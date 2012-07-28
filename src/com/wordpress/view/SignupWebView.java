//#preprocess

//#ifdef BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0

package com.wordpress.view;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.controller.MainController;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.webview.WebView;
import com.wordpress.view.webview.WebViewDefaultProtocolController;

public class SignupWebView extends WebView{

	public SignupWebView(String URL) {
		super(URL);
		this.setWebViewDefaultProtocolController( new MyWebViewDefaultProtocolController(this, browserField) );
		MainController.getIstance().bumpScreenViewStats("com/wordpress/view/SignupWebView", "SignupWeb View", "", null, "");
	}

	private class MyWebViewDefaultProtocolController extends WebViewDefaultProtocolController{
		public MyWebViewDefaultProtocolController(WebView wb, BrowserField browserField) {
			super(wb, browserField);
		}
		public void handleNavigationRequest(final BrowserFieldRequest request) throws Exception {
			final String requestedURL = request.getURL();
			Log.debug("SignupWeb requested the following URL: " + requestedURL);
			if ( requestedURL.indexOf("wordpress.com") != -1 && requestedURL.indexOf("signup") != -1 ){
				super.handleNavigationRequest(request);
			} else if ( request.getURL().startsWith("wordpress://") ) {
				//String[] splittedURL = StringUtils.split( request.getURL(), "username=");
				//final String username = splittedURL[ splittedURL.length -1 ]; 
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						UiApplication.getUiApplication().popScreen(SignupWebView.this);
						MainController.getIstance().addWPCOMBlogs();
					}
				});
			} else {
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						WebView wv = new WebView( requestedURL );
						UiApplication.getUiApplication().pushScreen(wv);
						wv.loadRequest();
					}
				});
			}
		}
	}
}
//#endif