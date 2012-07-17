package com.wordpress.view.webview;

import java.io.UnsupportedEncodingException;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.model.Preferences;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.BaseView;
import com.wordpress.view.dialog.ConnectionInProgressView;


public class WebView extends BaseView {
	ConnectionInProgressView connectionProgressView=null;
	private BrowserFieldConfig myBrowserFieldConfig;
	private BrowserField browserField;
	
	public WebView(String URL) {
		super(_resources.getString(WordPressResource.TITLE_PREVIEW));
		myBrowserFieldConfig = new BrowserFieldConfig();
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.NAVIGATION_MODE,BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.ALLOW_CS_XHR,Boolean.TRUE);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.ENABLE_GEARS,Boolean.FALSE);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.USER_AGENT, Tools.getAppDefaultUserAgent());

		browserField = new BrowserField(myBrowserFieldConfig);
		browserField.addListener( new WebViewDefaultListener( this ) );
		add(browserField);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.CONTROLLER, new WebViewDefaultProtocolController(this, browserField));
		if ( Preferences.getIstance().isDebugMode() )
			myBrowserFieldConfig.setProperty(BrowserFieldConfig.ERROR_HANDLER, new WebViewDefaultErrorHandler(browserField));
		browserField.requestContent( URL );

		MainController.getIstance().bumpScreenViewStats("com/wordpress/view/WebView", "Web View", "", null, "");
	}

	public WebView(byte[] data, String contentType) {
		super(_resources.getString(WordPressResource.TITLE_PREVIEW));
		
		String encoding = null;
		
		if(contentType != null && contentType.indexOf("charset") > -1 ) {
			String[] encodings = StringUtils.split(contentType, "=");
			encoding = encodings[1];
			encoding = StringUtils.replaceAll(encoding, ";", "");
			
			if(!StringUtils.isDeviceSupportEncoding(encoding)){
				//set encoding to UTF-8 if response encoding is not supported
				Log.trace("Response charset is not supported by device");
				encoding = "UTF-8";
			}
		} else {
			Log.debug("Response Content-type without charset");
			encoding = "UTF-8";
		}
		Log.trace("Selected Encoding: "+ encoding);
		
		String html = "";
		try {
			html = new String(data, encoding);
		} catch (UnsupportedEncodingException e) {
			//never fall here
		}
			
		myBrowserFieldConfig = new BrowserFieldConfig();
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.NAVIGATION_MODE,BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.ALLOW_CS_XHR,Boolean.TRUE);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.ENABLE_GEARS,Boolean.FALSE);

		browserField = new BrowserField(myBrowserFieldConfig);
		if (contentType!= null) {
			browserField.displayContent(data, contentType, "http://localhost");
		} else {
			browserField.displayContent(html, "http://localhost");
		}
		add(browserField);
		
        MainController.getIstance().bumpScreenViewStats("com/wordpress/view/WebView", "Web View", "", null, "");
	}
	
	protected void setViewTitle(String newTitle) {
		this.setTitleText(newTitle);
	}
	protected void setViewTitle() {
		if ( browserField == null ) return;
		String newTitle = "";
		
		if ( browserField.getDocumentTitle() != null )
			newTitle = browserField.getDocumentTitle();
		else if ( browserField.getDocumentUrl() != null )
			newTitle = browserField.getDocumentTitle();
			
		this.setTitleText(newTitle);
	}
	
	public BaseController getController() {
		return null;
	}
}