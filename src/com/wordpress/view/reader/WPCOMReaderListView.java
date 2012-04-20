//#preprocess

//#ifdef BlackBerrySDK7.0.0

package com.wordpress.view.reader;

import javax.microedition.io.InputConnection;

import net.rim.blackberry.api.browser.URLEncodedPostData;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.browser.field2.BrowserFieldRequest;
import net.rim.device.api.browser.field2.ProtocolController;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

import org.w3c.dom.Document;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.MainController;
import com.wordpress.utils.http.SimpleCookieManager;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.HTTPPostConn;


public class WPCOMReaderListView extends WPCOMReaderBase
{
    private BrowserField _browserField;    
    private boolean _documentLoaded = false;
    
    private ConnectionInProgressView connectionProgressView = null;
	private final String contentToLoad; //the login form used to by-pass the auth
	private String currentTopic = null; //the current selected topic
	private String topicsPageContent = null; //the content of the topics page that will be cached in this class
	private String detailPageContent = null; //the content of the detail page that will be cached in this class
	private final String username;
	private final String password;
    
    /**
     * Creates a new BrowserFieldScreen object
     * @param request The URI of the content to display in this BrowserFieldScreen
     * @param enableScriptMenu True if a context menu is to be created for this BrowserFieldScreen instance, false otherwise
     */
    public WPCOMReaderListView(String username, String password)
    {    
    	super(_resources.getString(WordPressResource.MENUITEM_READER));
		this.username = username;
		this.password = password;
    	
    	this.contentToLoad = "<head>"
			+ "<script type=\"text/javascript\">"
			+ "function submitform(){document.loginform.submit();} </script>"
			+ "</head>"
			+ "<body onload=\"submitform()\">"
			+ "<form style=\"visibility:hidden;\" name=\"loginform\" id=\"loginform\" action=\""
			+ WordPressInfo.WPCOM_LOGIN_URL
			+ "\" method=\"post\">"
			+ "<input type=\"text\" name=\"log\" id=\"user_login\" value=\""
			+ username
			+ "\"/></label>"
			+ "<input type=\"password\" name=\"pwd\" id=\"user_pass\" value=\""
			+ password
			+ "\" /></label>"
			+ "<input type=\"submit\" name=\"wp-submit\" id=\"wp-submit\" value=\"Log In\" />"
			+ "<input type=\"hidden\" name=\"redirect_to\" value=\""
			+ this.getAuthorizeHybridURL( WordPressInfo.readerURL_v3 ) + "\" />" + "</form>" + "</body>";
    	
    	addKeyListener(new BrowserFieldScreenKeyListener());        
        
        BrowserFieldConfig config = getReaderBrowserDefaultConfig();
        _browserField = new BrowserField(config);
        _browserField.addListener(new InnerBrowserListener());
    	//Add the protocol controller to intercept clicks on the browser
		_browserField.getConfig().setProperty(BrowserFieldConfig.CONTROLLER, new ListViewProtocolController(_browserField));
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

                //Set the connection configuration after the dialog, since it is time consuming task. 
                this.setPreferredConnectionTypes(_browserField);
                    
                _browserField.displayContent(contentToLoad, "");
                
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
                MainController.getIstance().displayError(e, "Error while loading the Web Page");
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
    
    private MenuItem _topicsMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_TOPICS, 100, 100) {
        public void run() {
        	if( topicsPageContent != null ) {
    			UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						WPCOMReaderTopicsView _browserScreen = new WPCOMReaderTopicsView(WPCOMReaderListView.this, null, topicsPageContent);
						UiApplication.getUiApplication().pushScreen(_browserScreen);   
					}
				});
        	} else {
    			UiApplication.getUiApplication().invokeLater(new Runnable() {
					public void run() {
						BrowserFieldRequest request = new BrowserFieldRequest(WPCOMReaderListView.this.getAuthorizeHybridURL( WordPressInfo.readerTopicsURL ));
						WPCOMReaderTopicsView _browserScreen = new WPCOMReaderTopicsView(WPCOMReaderListView.this, request, null);
						UiApplication.getUiApplication().pushScreen(_browserScreen);   
					}
				});
        	}
        }
    };
    
    private MenuItem _refreshMenuItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESH, 200, 200) {
    	public void run() {
    		UiApplication.getUiApplication().invokeLater(new Runnable() {
    			public void run() {
    				BrowserFieldRequest request = new BrowserFieldRequest(WPCOMReaderListView.this.getAuthorizeHybridURL( WordPressInfo.readerURL_v3 ));
    				WPCOMReaderListView.this._browserField.requestContent(request);
    			}
    		});
    	}
    };
    
    
	public String getCurrentTopic() {
		return currentTopic;
	}
	
	public void setNewTopicAndRefreshTheReader(final String newTopicID, final String newTopic) {
		this.currentTopic = newTopic;
		UiApplication.getUiApplication().invokeLater(new Runnable() {
			public void run() {
				setTitleText(newTopic);
				_browserField.getScriptEngine().executeScript("Reader2.load_topic('"+newTopicID+"')", null);
			} //end run
		});
	}
    
    /**
     * @see MainScreen#makeMenu(Menu, int)
     */
    protected void makeMenu(Menu menu, int instance)
    {
        if( _documentLoaded ) {
        	menu.add(_topicsMenuItem);
        	menu.add(_refreshMenuItem);
        }
        super.makeMenu(menu, instance);
    }

    private void loadAndCacheTopicsPage(){
		URLEncodedPostData urlEncoder = new URLEncodedPostData("UTF-8", false);
		urlEncoder.append("redirect_to", this.getAuthorizeHybridURL(WordPressInfo.readerTopicsURL));
		urlEncoder.append("log", this.username);
		urlEncoder.append("pwd", this.password);
		byte[] data = urlEncoder.getBytes();
		final HTTPPostConn connection = new HTTPPostConn( WordPressInfo.WPCOM_LOGIN_URL, data);
		connection.addObserver(new loadAndCacheTopicsPageCallback());
		connection.startConnWork(); //starts connection
    }
    private class loadAndCacheTopicsPageCallback implements Observer {
    	public void update(Observable observable, final Object object) {
    		BlogConnResponse resp= (BlogConnResponse) object;
    		if(resp.isStopped()){
    			return;
    		}
    		if(!resp.isError()) {
    			try {
    				Log.debug("Topics Page cached");
    				String html = (String)resp.getResponseObject();
    				//Log.debug("Content of the topics page: " + html );
    				topicsPageContent = html;
    			} catch (Exception e) {
    				Log.error(e, "Error while loading the topics page");
    				return;
    			}
    		} 
    	}
    } 
    
    private void loadAndCacheDetailPage(){
    	URLEncodedPostData urlEncoder = new URLEncodedPostData("UTF-8", false);
		urlEncoder.append("redirect_to", this.getAuthorizeHybridURL(WordPressInfo.readerDetailURL));
		urlEncoder.append("log", this.username);
		urlEncoder.append("pwd", this.password);
		byte[] data = urlEncoder.getBytes();
		final HTTPPostConn connection = new HTTPPostConn( WordPressInfo.WPCOM_LOGIN_URL, data);
		connection.setCookieManager( new SimpleCookieManager() );
		connection.addObserver(new loadAndCacheDetailPageCallback());
		connection.startConnWork(); //starts connection
    }
    private class loadAndCacheDetailPageCallback implements Observer {
    	public void update(Observable observable, final Object object) {
    		BlogConnResponse resp= (BlogConnResponse) object;
    		if(resp.isStopped()){
    			return;
    		}
    		if(!resp.isError()) {
    			try {
    				String html = (String)resp.getResponseObject();
    				Log.debug("DetailView Page cached");
    				detailPageContent = html;
    			} catch (Exception e) {
    				Log.error(e, "Error while loading the details page");
    				return;
    			}
    		} 
    	}
    }
    
	public BaseController getController() {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected void executeNativeJaveCode(String methodName, Object[] formalParamenters, Class[] formalParametersType) {
		Log.debug("Calling "+ methodName + " on " + this.getClass().getName());
		if( methodName.equalsIgnoreCase("setSelectedTopic") ) {
			this.currentTopic = (String)formalParamenters[0];
			Log.debug("Current Selected Topic: "+ this.currentTopic);
		} else if( methodName.equalsIgnoreCase("setTitle") ) {
			final String title = (String)formalParamenters[0];
			Log.debug("Current title: "+ title);
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					setTitleText(title);
				} //end run
			});
		} else {
			Log.debug("Method not found: " + methodName);
		}
	}
    
    private class ListViewProtocolController extends ProtocolController {

    	public ListViewProtocolController(BrowserField browserField){ super(browserField); }

    	public void handleNavigationRequest(final BrowserFieldRequest request) throws Exception {
    		Log.info(" Requested the following URL: " + request.getURL());
    		if ( request.getURL().equalsIgnoreCase(WordPressInfo.readerDetailURL) ) {
    			 Log.debug("Load the details view in a new view");
    			 
    	    		UiApplication.getUiApplication().invokeLater(new Runnable() {
    	    			public void run() {
    	    				try {
    	    					Object executeScript = _browserField.getScriptEngine().executeScript("jQuery.Storage.get( 'current_item' );", null);
    	    					Log.debug((String)executeScript);
    	    		    		org.json.me.JSONObject currentItem = (org.json.me.JSONObject) new org.json.me.JSONTokener((String)executeScript).nextValue();
   	    		    			//String permalink = currentItem.getString("permalink");
   	    		    			WPCOMReaderDetailView	detailScreen = new WPCOMReaderDetailView(request, detailPageContent);
   	    		    			detailScreen.currentItemJSONObj = currentItem.toString();
   	    		    			UiApplication.getUiApplication().pushScreen(detailScreen);
    	    				} catch (Exception e) {
    	    					Log.error(e, "Error while setting the selectedTopic on Topics view");
    	    				}
    	    			} //end run
    	    		});
    			 
    			 
			/*	 UiApplication.getUiApplication().invokeLater(new Runnable() {
					 public void run() {
						 WPCOMReaderDetailView	detailScreen = new WPCOMReaderDetailView(request, detailPageContent);
    					 UiApplication.getUiApplication().pushScreen(detailScreen);    
					 }
				 });*/
    		} else {
    			//Load the listView in the current View.
    			//This method is called because we are using the WP HTML login form with redirect, otherwise this method is not called, since we are loading static HTML...
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
    	}
    };
       
    /**
     * A class to listen for BrowserField events
     */
    private class InnerBrowserListener extends BrowserFieldListener
    {
	/*
		public void documentCreated(final BrowserField browserField, final ScriptEngine scriptEngine, final Document document)
		throws Exception {
			((EventTarget) document).addEventListener("load",
					new EventListener() {
				public void handleEvent(final Event evt) {
					Log.debug("*** documentCreated");
					_documentLoaded = true;
				}
			}, false);
		}
		*/
		public void documentLoaded(BrowserField browserField, Document document)
				throws Exception {
			super.documentLoaded(browserField, document);
			Log.debug("URL Loaded in the listView: " + browserField.getDocumentUrl() );
    		if( browserField.getDocumentUrl() != null && browserField.getDocumentUrl().startsWith(WordPressInfo.readerURL_v3)) {
    			//the browser has loaded the login form and authenticated the user...
    			_documentLoaded = true;
    			Log.debug("Caching the topics and the detailView...");
	    		//Load the topics page and cache it
	    		loadAndCacheTopicsPage();
	    		//Load the detail page and cache it
	    		loadAndCacheDetailPage();
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
    					if(key == Characters.ESCAPE)
    					{
    						synchronized(Application.getEventLock()) 
    						{
    							close();
    						}
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

}

//#endif