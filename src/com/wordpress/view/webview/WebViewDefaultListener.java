package com.wordpress.view.webview;

import org.w3c.dom.Document;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldListener;

import com.wordpress.utils.log.Log;

/**
 * A class to listen for BrowserField events
 */
public class WebViewDefaultListener extends BrowserFieldListener {
	
	private WebView webView;

	public WebViewDefaultListener( WebView wb ) {
		this.webView = wb;
	}
	 
	public void documentLoaded(BrowserField browserField, Document document) throws Exception {
		super.documentLoaded(browserField, document);
		Log.debug("URL Loaded in the listView: " + browserField.getDocumentUrl() );
		webView.setViewTitle();
	}
	
	public void documentError(BrowserField browserField, Document document) throws Exception {
		webView.setViewTitle("Error!");
		super.documentError(browserField, document);
	}
}

