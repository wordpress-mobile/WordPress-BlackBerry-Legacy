package com.wordpress.view.reader;

import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;
import org.json.me.JSONTokener;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.utils.PropertyUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.BaseView;

public abstract class WPCOMReaderBase extends BaseView {

	public WPCOMReaderBase(long style) {
		super(style);
	}

	public WPCOMReaderBase(String title, long style) {
		super(title, style);
	}

	public WPCOMReaderBase(String title) {
		super(title);
	}

	protected BrowserFieldConfig getReaderBrowserDefaultConfig() {
		BrowserFieldConfig config = new BrowserFieldConfig();  
		config.setProperty(BrowserFieldConfig.USER_AGENT, "wp-blackberry/"+ PropertyUtils.getIstance().getAppVersion());
	    //Default = TRUE
		config.setProperty(BrowserFieldConfig.MDS_TRANSCODING_ENABLED, Boolean.FALSE);    
        //Default = TRUE
		config.setProperty(BrowserFieldConfig.USER_SCALABLE, Boolean.FALSE);    
        config.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
        config.setProperty(BrowserFieldConfig.ENABLE_COOKIES, Boolean.TRUE);
        config.setProperty(BrowserFieldConfig.NAVIGATION_MODE, BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		return config;
	}  
	
	public String getAuthorizeHybridURL(String URL) {
			
			if( ! isValidHybridURL(URL) ) return URL;
			
			URLEncodedPostData urlEncoder = new URLEncodedPostData("UTF-8", false);
			urlEncoder.append("wpcom-hybrid-auth-token", this.getHybridAuthToken());
			
			if( URL.indexOf("?") != -1 )
				return URL + "&" + urlEncoder.toString();
			else 
				return URL + "?" + urlEncoder.toString();
		}
		
		public boolean isValidHybridURL(String URL) {
			return ( URL.indexOf(WordPressInfo.authorizedHybridHost) != -1 );
		}
	
		public String getHybridAuthToken() {
			String uuid = "mobile-reader-" + Tools.generateDeviceUUID();
			return uuid;
		}
		
		
	    /**
	     * Maps a javascript function to a BlackBerry Java function
	     * @param browserField The BrowserField displayed by this applications's screen
	     */
	    public void extendJavaScript(BrowserField browserField) throws Exception
	    {        
	        ScriptableFunction callNative = new ScriptableFunction()
	        {            
	            public Object invoke(Object thiz, Object[] args) throws Exception
	            {
	            	Log.trace("callNative->invoke with the following request: " + (String)args[0] );
	            	WPCOMReaderBase.this.callNative((String)args[0]); //One long JSON String...
	            	return net.rim.device.api.script.Scriptable.UNDEFINED;
	            }
	        };
	        browserField.extendScriptEngine("BlackBerry.callNative", callNative);        
	    }
		
	    protected void callNative(String payload) {
	    	try {
	    		String jsonString = payload.substring(0, payload.indexOf("&wpcom-hybrid-auth-token="));
	    		String wpcom_hybrid_auth_token= payload.substring(payload.indexOf("&wpcom-hybrid-auth-token="), payload.length() );
	    		wpcom_hybrid_auth_token = StringUtils.replaceLast(wpcom_hybrid_auth_token, "&wpcom-hybrid-auth-token=", "");

	    		WPCOMReaderBase parentClass = WPCOMReaderBase.this;

	    		if ( ! wpcom_hybrid_auth_token.equals( parentClass.getHybridAuthToken() ) ) {
	    			//Token miss-match
	    			Log.error(  "Remote native call failed : Token missmatch" );
	    			return;
	    		}

	    		JSONArray methodsToCall = (JSONArray) new JSONTokener(jsonString).nextValue();
	    		//a single call from the JS code can contain the invocation of more than one native method
	    		for (int i = 0; i < methodsToCall.length(); i++) { 

	    			JSONObject currentMethodToCall = methodsToCall.getJSONObject(i);
	    			String methodName = currentMethodToCall.getString("method");
	    			JSONArray args = currentMethodToCall.getJSONArray("args");

	    			Object[] formalParameters = new Object[args.length()];   //declares the parameters to be passed to the method
	    			Class[] formalParametersType = new Class[args.length()]; //declares the parameters type the method takes

	    			for (int j = 0; j < args.length(); j++) {
	    				formalParameters[j] = args.getString(j); //We know that for now only String parameters are allowed
	    				formalParametersType[j] = String.class;
	    			}

	    			//Execute a single JS->Native method invocation
	    			try {
	    				this.executeNativeJaveCode(methodName, formalParameters, formalParametersType);
	    			} catch (Exception e) {
	    				Log.error( e, "Error while calling the native method "+ methodName + " with the following parameters "+formalParameters.toString()  );
	    			}
	    		}
	    	} catch (JSONException e) {
	    		Log.error( "Error while parsing the native call JSON string: "+ e.getMessage() );
	    	}
	    }
		
		protected abstract void executeNativeJaveCode(String methodName, Object[] formalParamenters, Class[] formalParametersType);
		
		protected class ConnectionDialogClosedListener implements DialogClosedListener {
			public int choice;
			
			public ConnectionDialogClosedListener(){
				super();
			}
			
			public void dialogClosed(Dialog dialog, int choice) {
				Log.trace("dialogClosed");
				this.choice = choice;
				if(choice == Dialog.CANCEL) {
					Log.trace("dialogClosed.CANCEL");
					close();
				}
			}
		}
	
}
