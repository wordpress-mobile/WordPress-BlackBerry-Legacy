package com.wordpress.view.reader;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldHistory;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import org.w3c.dom.Document;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.ConnectionInProgressView;


/**
 * The MainScreen class for the Browser Field 2 Demo application 
 */
public class WPCOMReaderDetailView extends WPCOMReaderBase
{
    private BrowserField _browserField;    
    private boolean _documentLoaded = false;
    
    private ConnectionInProgressView connectionProgressView = null;
	private BrowserFieldRequest request = null;
	private final String currentItem;

    
    /**
     * Creates a new BrowserFieldScreen object
     * @param request The URI of the content to display in this BrowserFieldScreen
     * @param enableScriptMenu True if a context menu is to be created for this BrowserFieldScreen instance, false otherwise
     */
    public WPCOMReaderDetailView(BrowserFieldRequest request, String currentItem)
    {    
    	super(_resources.getString(WordPressResource.TITLE_SETTINGS_VIEW));
    	
    	this.request = request;
		this.currentItem = currentItem;
    	addKeyListener(new BrowserFieldScreenKeyListener());        
        BrowserFieldConfig config = getReaderBrowserDefaultConfig();
        _browserField = new BrowserField(config);
        _browserField.addListener(new InnerBrowserListener());
        try {
			extendJavaScript(_browserField);
		} catch (Exception e) {
			Log.error(e, "Error while binding JS code to Native Code");
		}
        add(_browserField);
    }
 
    
    /**
     * @see Screen#onUiEngineAttached(boolean)     
     */
    protected void onUiEngineAttached(boolean attached)
    {
        if(attached)
        {
            try
            {
                _browserField.requestContent(request);
                connectionProgressView = new ConnectionInProgressView(
            			_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
            	connectionProgressView.setDialogClosedListener(new ConnectionDialogClosedListener());
                connectionProgressView.show();
            }
            catch(Exception e)
            {                
                deleteAll();
                add(new LabelField("ERROR:\n\n"));
                add(new LabelField(e.getMessage()));
            }
        }
    }
    
    
    /**      
     * @see MainScreen#onSavePrompt()
     */
    public boolean onSavePrompt()
    {
        // Prevent the save dialog from being displayed
        return true;
    }   

      
    /**
     * A class to listen for BrowserField events
     */
    private class InnerBrowserListener extends BrowserFieldListener
    {
    	public void documentLoaded(BrowserField browserField, Document document) {
    		Log.debug("DetailView has loaded the following URL : " + browserField.getDocumentUrl() );
    		//the browser has loaded the login form and authenticated the user...
    		_documentLoaded = true;
    		if ( connectionProgressView != null ) {
    			UiApplication.getUiApplication().invokeLater(new Runnable() {
    				public void run() {
    					UiApplication.getUiApplication().popScreen(connectionProgressView);
    					connectionProgressView = null;
    				} //end run
    			});
    		} 
    		if ( request.getURL().startsWith(WordPressInfo.readerDetailURL) ) {
    			UiApplication.getUiApplication().invokeLater(new Runnable() {
    				public void run() {
    					try {
    						_browserField.getScriptEngine().executeScript("bb_test = "+currentItem+"; Reader2.show_article_details(bb_test)", null);
    					} catch (Exception e) {
    						Log.error(e, "Error while setting the item on the view");
    					}
    				} //end run
    			});
    		}
    	}
    }
        
    /**
     * A KeyListener implementation
     */
    private class BrowserFieldScreenKeyListener implements KeyListener
    {
    	/**
    	 * @see KeyListener#keyChar(char, int, int)
    	 */
    	public boolean keyChar(final char key, int status, int time)
    	{            
    		if(key == 'p' || key == Characters.ESCAPE)
            {
                Runnable previousRunnable = new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            BrowserFieldHistory browserFieldHistory = WPCOMReaderDetailView.this._browserField.getHistory();
                            if(browserFieldHistory.canGoBack())
                            {
                                browserFieldHistory.goBack();
                            }
                            else
                            {
                                if(key == Characters.ESCAPE)
                                {
                                    synchronized(Application.getEventLock()) 
                                    {
                                        close();
                                    }
                                }
                            }
                        }
                        catch(Exception e)
                        {                            
                            System.out.println("Error executing js:previous(): " + e.getMessage());                                      
                        }
                    }
                };
                new Thread(previousRunnable).start();
                return true;
            }
    		return false;            
    	}

        
       /**
        * @see KeyListener#keyDown(int, int)
        */
        public boolean keyDown(int keycode, int time)
        {
            return false;
        }

        
       /**
        * @see KeyListener#keyRepeat(int, int)
        */
        public boolean keyRepeat(int keycode, int time)
        {
            return false;
        }

        
       /**
        * @see KeyListener#keyStatus(int, int)
        */
        public boolean keyStatus(int keycode, int time)
        {
            return false;
        }

        
       /**
        * @see KeyListener#keyUp(int, int)
        */
        public boolean keyUp(int keycode, int time)
        {
            return false;
        }
    }


	public BaseController getController() {
		// TODO Auto-generated method stub
		return null;
	}
    
	protected void executeNativeJaveCode(String methodName, Object[] formalParamenters, Class[] formalParametersType) {
		Log.debug("Trying to call the following method "+ methodName + " on " + this.getClass().getName());
    }
}
