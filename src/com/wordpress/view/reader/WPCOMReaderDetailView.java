//#preprocess

//#ifdef BlackBerrySDK7.0.0

package com.wordpress.view.reader;

import javax.microedition.io.InputConnection;

import net.rim.blackberry.api.invoke.Invoke;
import net.rim.blackberry.api.invoke.MessageArguments;
import net.rim.blackberry.api.mail.Message;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.BrowserFieldResponse;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
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
	private String datailPageContent;
	private boolean _documentLoaded;
	
	public String currentItemJSONObj = null;
    
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
    			
    			this.setPreferredConnectionTypes(_browserField);
    	         	
    			//If the page is cached load it by using a request obj. Do not load the HTML directly otherwise the JS at onLoad is never triggered!!!
    			if( datailPageContent != null ) {
    				Log.debug("DetailView loaded from the cache");
    				String resourceURI = request.getURL();
    				BrowserFieldResponse browserFieldResponse = new BrowserFieldResponse(resourceURI, datailPageContent.getBytes(), HttpProtocolConstants.CONTENT_TYPE_TEXT_HTML);
    				_browserField.displayContent(browserFieldResponse, request.getURL());  
    			} else {
    				Log.debug("DetailView NOT loaded from the cache");

    				connectionProgressView = new ConnectionInProgressView(
        					_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
        			connectionProgressView.setDialogClosedListener(new ConnectionDialogClosedListener());
        			connectionProgressView.show();
        			
    				_browserField.requestContent(request);
    				
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
    		}
    		catch(Exception e)
    		{
				UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						if (connectionProgressView != null && connectionProgressView.isDisplayed())
							UiApplication.getUiApplication().popScreen(connectionProgressView);
					} //end run
				});
				deleteAll();
    			add(new LabelField("ERROR:\n\n"));
    			add(new LabelField(e.getMessage()));
    		}
    	}
    }
    
    private MenuItem _browserMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_OPEN_IN_BROWSER, 200, 200) {
    	public void run() {
    		try {
				Object executeScript = _browserField.getScriptEngine().executeScript("Reader2.get_article_permalink()", null); 
				final String item = (String) executeScript;
				Tools.openNativeBrowser( item );
			} catch (Exception e) {
				Log.error(e, "Error while loading the permalink");
				Dialog.alert("We encountered a problem retrieving the link of the article");
			}
    	}
    };

    private MenuItem _copyLinkMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_COPY_LINK, 200, 200) {
    	public void run() {
    		try {
				Object executeScript = _browserField.getScriptEngine().executeScript("Reader2.get_article_permalink()", null); 
				final String item = (String) executeScript;
				 // Retrieve the Clipboard object.
				 Clipboard  cp = Clipboard.getClipboard();
				 // Copy to clipboard.
				 cp.put(item);
			} catch (Exception e) {
				Log.error(e, "Error while loading the permalink");
				Dialog.alert("We encountered a problem retrieving the link of the article");
			}
    	}
    };

    private MenuItem _mailLinkMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_MAIL_LINK, 200, 200) {
    	public void run() {
    		String articleLink = null;
    		String articleTitle = null;
    		try {
    			Object executeScript = _browserField.getScriptEngine().executeScript("Reader2.get_article_permalink()", null); 
    			articleLink = (String) executeScript;
    		} catch (Exception e) {
    			Log.error(e, "Error while loading the permalink");
    			Dialog.alert("We encountered a problem retrieving the link of the article");
    			return;
    		}
    		try {
    			Object executeScript = _browserField.getScriptEngine().executeScript("Reader2.get_article_title()", null); 
    			articleTitle = (String) executeScript;
    		} catch (Exception e) {
    			Log.error(e, "Error while loading the title of the current article");
    			Dialog.alert("We encountered a problem retrieving the title of the article");
    			return;
    		}
    		if ( articleLink != null && articleTitle != null ) {
    			try{
    				Message m = new Message();
    				m.setContent( articleLink );
    				m.setSubject( articleTitle );
    				Invoke.invokeApplication(Invoke.APP_TYPE_MESSAGES, new MessageArguments(m));
    			} catch (Exception e) {
    				Log.error(e, "Problem invoking BlackBerry Mail App");
    				Dialog.alert("We encountered a problem calling the BlackBerry Mail App");
    			}
    		}
    	}
    };
    
    /**
     * @see MainScreen#makeMenu(Menu, int)
     */
    protected void makeMenu(Menu menu, int instance)
    {
        //if( _documentLoaded ) {
        	menu.add(_browserMenuItem);
        	menu.add(_mailLinkMenuItem);
        	menu.add(_copyLinkMenuItem);
        //}
        super.makeMenu(menu, instance);
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

    		if ( request.getURL().equalsIgnoreCase(WordPressInfo.readerDetailURL) ) {
    			//Not sure when this branch will be reached
    			if ( WPCOMReaderDetailView.this.datailPageContent != null ) {
    				String resourceURI = request.getURL();
    				BrowserFieldResponse browserFieldResponse = new BrowserFieldResponse(resourceURI,  WPCOMReaderDetailView.this.datailPageContent.getBytes(), HttpProtocolConstants.CONTENT_TYPE_TEXT_HTML);
    				_browserField.setFocus();
    				_browserField.displayContent(browserFieldResponse, request.getURL());  
    			} else {
    				//The page is not cached, load it.
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
		/*public void documentCreated(final BrowserField browserField, final ScriptEngine scriptEngine, final Document document)
		throws Exception {
			((EventTarget) document).addEventListener("load",
					new EventListener() {
				public void handleEvent(final Event evt) {
					Log.debug("*** detailsView documentCreated");
				}
			}, false);
		}
    	*/
    	public void documentLoaded(BrowserField browserField, Document document) throws Exception {
    		super.documentLoaded(browserField, document);
    		Log.debug("*** URL loaded in the detailView: " + browserField.getDocumentUrl() );
    		_documentLoaded = true;
    		UiApplication.getUiApplication().invokeLater(new Runnable() {
    			public void run() {
    				try {
    					if ( currentItemJSONObj != null )
    						_browserField.getScriptEngine().executeScript("Reader2.show_article_details(" + currentItemJSONObj +");", null);
    				} catch (Exception e) {
    					Log.error(e, "Error while setting the selectedTopic on Topics view");
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
		Log.debug("Calling the following method "+ methodName + " on the detail View" );
    }
}

//#endif