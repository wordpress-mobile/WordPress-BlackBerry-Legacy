package com.wordpress.view.webview;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.ProtocolController;

public class WebViewDefaultProtocolController extends ProtocolController {

	private final WebView wb;

	public WebViewDefaultProtocolController( WebView wb, BrowserField browserField){ 
		super(browserField);
		this.wb = wb;
	}

	public void handleNavigationRequest(final BrowserFieldRequest request) throws Exception {
		wb.setViewTitle(WordPressCore.getInstance().getResourceBundle().getString(WordPressResource.LOADING_WITH_THREE_DOTS));
		super.handleNavigationRequest(request);
	}
}
