//#preprocess

//#ifdef BlackBerrySDK7.0.0

package com.wordpress.view.reader;

import javax.microedition.io.InputConnection;

import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.ProtocolController;
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
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.ConnectionInProgressView;

public class WPCOMReaderDetailView extends WPCOMReaderBase
{
    private BrowserField _browserField;    
    
    private ConnectionInProgressView connectionProgressView = null;
	private BrowserFieldRequest request = null;
	private final String datailPageContent;
    
    /**
     * Creates a new BrowserFieldScreen object
     * @param request The URI of the content to display in this BrowserFieldScreen
     * @param enableScriptMenu True if a context menu is to be created for this BrowserFieldScreen instance, false otherwise
     */
    public WPCOMReaderDetailView(BrowserFieldRequest request, String datailPageContent)
    {    
    	super(_resources.getString(WordPressResource.MENUITEM_READER));
    	
    	this.request = request;
		this.datailPageContent = datailPageContent;
    	addKeyListener(new BrowserFieldScreenKeyListener());        
        BrowserFieldConfig config = getReaderBrowserDefaultConfig();
        _browserField = new BrowserField(config);
        _browserField.addListener(new InnerBrowserListener());
        _browserField.getConfig().setProperty(BrowserFieldConfig.CONTROLLER, new DetailViewProtocolController(_browserField));
        _browserField.getConfig().setProperty(BrowserFieldConfig.ERROR_HANDLER, new ReaderBrowserFieldErrorHandler(_browserField) );
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
    			connectionProgressView = new ConnectionInProgressView(
    					_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
    			connectionProgressView.setDialogClosedListener(new ConnectionDialogClosedListener());
    			connectionProgressView.show();
    			
    			this.setPreferredConnectionTypes(_browserField);
            	
            	if( datailPageContent != null ) {
                	 _browserField.displayContent(datailPageContent, "http://wordpress.com");
                } else {
                	_browserField.requestContent(request);
                }
    	
    			int res = UiApplication.getUiApplication().invokeLater(new Runnable() {
    				public void run() {
    					if ( connectionProgressView.isDisplayed())
    						UiApplication.getUiApplication().popScreen(connectionProgressView);
    					connectionProgressView = null;
    				} //end run
    			}, 2000, false);

    			if ( res == -1 ) { //timer failed, remove the dialog immediately
    				UiApplication.getUiApplication().invokeLater(new Runnable() {
    					public void run() {
    						if ( connectionProgressView.isDisplayed())
    							UiApplication.getUiApplication().popScreen(connectionProgressView);
    						connectionProgressView = null;
    					} //end run
    				});
    			}
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

    private class DetailViewProtocolController extends ProtocolController {

    	public DetailViewProtocolController(BrowserField browserField){ super(browserField); }

    	public void handleNavigationRequest(final BrowserFieldRequest request) throws Exception {
    		Log.info("DetailView requested the following URL: " + request.getURL());
    		//Load the details view
    		if ( request.getURL().equalsIgnoreCase(WordPressInfo.readerDetailURL) ) {
    			//Load the URL in the current View. 
    			try {
    				final InputConnection ic = handleResourceRequest(request);
    				UiApplication.getUiApplication().invokeLater(new Runnable() {
    					public void run() {
    						_browserField.setFocus();
    						_browserField.displayContent(ic, request.getURL());  
    					}
    				});
    			} catch (Exception e) { 
    				Log.error(e, "handleNavigationRequest");
    			}
    		} else {
    			Tools.openNativeBrowser(request.getURL());
    		}
    	}
    };
    
    
    /**
     * A class to listen for BrowserField events
     */
    private class InnerBrowserListener extends BrowserFieldListener
    {
    	public void documentLoaded(BrowserField browserField, Document document) {
    		Log.debug("URL loaded: " + browserField.getDocumentUrl() );
    
    		UiApplication.getUiApplication().invokeLater(new Runnable() {
    			public void run() {
    				try {
    					//_browserField.getScriptEngine().executeScript("bb_test = "+currentItem+"; Reader2.show_article_details(bb_test);", null);
    					_browserField.executeScript("Reader2.show_article_details();");
    				} catch (Exception e) {
    					Log.error(e, "Error while setting the item on the view");
    				}
    			} //end run
    		});
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
                    	synchronized(Application.getEventLock()) 
                    	{
                    		close();
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
		return null;
	}
    
	protected void executeNativeJaveCode(String methodName, Object[] formalParamenters, Class[] formalParametersType) {
		Log.debug("Trying to call the following method "+ methodName + " on " + this.getClass().getName());
    }
}

//#endif