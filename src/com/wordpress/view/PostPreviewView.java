package com.wordpress.view;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.InputConnection;

import net.rim.device.api.browser.field.BrowserContent;
import net.rim.device.api.browser.field.BrowserContentChangedEvent;
import net.rim.device.api.browser.field.Event;
import net.rim.device.api.browser.field.RedirectEvent;
import net.rim.device.api.browser.field.RenderingApplication;
import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.browser.field.RenderingSession;
import net.rim.device.api.browser.field.RequestedResource;
import net.rim.device.api.browser.field.UrlRequestedEvent;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Status;
import net.rim.device.api.util.StringUtilities;

import com.wordpress.controller.BaseController;

public class PostPreviewView  extends BaseView implements RenderingApplication {

	 private static final String REFERER = "referer";   
	    
	    private RenderingSession _renderingSession;   
	    private InputConnection  _currentConnection;
	
	public PostPreviewView(String title) {
		super(title);
		
        _renderingSession = RenderingSession.getNewInstance();
        
        // Enable javascript.
        //_renderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.JAVASCRIPT_ENABLED, true);                        
        
        PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread("http://www.google.com", null, null, null, this);
        thread.start();      
			
	}

   
	  public void processConnection(InputConnection connection, Event e) 
	    {
	        // Cancel previous request.
	        if (_currentConnection != null) 
	        {
	            try 
	            {
	                _currentConnection.close();
	            } 
	            catch (IOException e1) 
	            {
	            }
	        }
	        
	        _currentConnection = connection;
	        
	        BrowserContent browserContent = null;
	        
	        try   
	        {
	            browserContent = _renderingSession.getBrowserContent(connection, "", this, e);
	            
	            if (browserContent != null) 
	            {
	                Field field = browserContent.getDisplayableContent();
	                
	                if (field != null) 
	                {
	                    synchronized (Application.getEventLock()) 
	                    {
	                        deleteAll();
	                        add(field);
	                    }
	                }
	                
	                browserContent.finishLoading();
	            }
	                                                         
	        } 
	        catch (RenderingException re) 
	        {
	        	System.out.println(re.getMessage());
	        } 
	        finally 
	        {
	          //  SecondaryResourceFetchThread.doneAddingImages();
	        }
	        
	    }    

	    /**
	     * @see net.rim.device.api.browser.RenderingApplication#eventOccurred(net.rim.device.api.browser.Event)
	     */
	    public Object eventOccurred(Event event) 
	    {
	        int eventId = event.getUID();

	        switch (eventId) 
	        {
	            case Event.EVENT_URL_REQUESTED : 
	            {
	                UrlRequestedEvent urlRequestedEvent = (UrlRequestedEvent) event;    
	                String absoluteUrl = urlRequestedEvent.getURL();
	    
	                HttpConnection conn = null;
	                PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread(urlRequestedEvent.getURL(),
	                                                                                         urlRequestedEvent.getHeaders(), 
	                                                                                         urlRequestedEvent.getPostData(),
	                                                                                         event, this);
	                thread.start();
	    
	                break;

	            } 
	            case Event.EVENT_BROWSER_CONTENT_CHANGED: 
	            {                
	                // Browser field title might have changed update title.
	                BrowserContentChangedEvent browserContentChangedEvent = (BrowserContentChangedEvent) event; 
	            
	                if (browserContentChangedEvent.getSource() instanceof BrowserContent) 
	                { 
	                    BrowserContent browserField = (BrowserContent) browserContentChangedEvent.getSource(); 
	                    String newTitle = browserField.getTitle();
	                    if (newTitle != null) 
	                    {
	                        synchronized (UiApplication.getUiApplication().getAppEventLock()) 
	                        { 
	                            setTitle(newTitle);
	                        }                                               
	                    }                                       
	                }                   

	                break;                

	            } 
	            case Event.EVENT_REDIRECT : 
	            {
	                RedirectEvent e = (RedirectEvent) event;
	                String referrer = e.getSourceURL();
	                
	                switch (e.getType()) 
	                {  
	                    case RedirectEvent.TYPE_SINGLE_FRAME_REDIRECT :
	                        // Show redirect message.
	                        Application.getApplication().invokeAndWait(new Runnable() 
	                        {
	                            public void run() 
	                            {
	                                Status.show("You are being redirected to a different page...");
	                            }
	                        });
	                    
	                    break;
	                    
	                    case RedirectEvent.TYPE_JAVASCRIPT :
	                        break;
	                    
	                    case RedirectEvent.TYPE_META :
	                        // MSIE and Mozilla don't send a Referer for META Refresh.
	                        referrer = null;     
	                        break;
	                    
	                    case RedirectEvent.TYPE_300_REDIRECT :
	                        // MSIE, Mozilla, and Opera all send the original
	                        // request's Referer as the Referer for the new
	                        // request.
	                        Object eventSource = e.getSource();
	                        if (eventSource instanceof HttpConnection) 
	                        {
	                            referrer = ((HttpConnection)eventSource).getRequestProperty(REFERER);
	                        }
	                        
	                        break;
	                    }
	                    
	                    HttpHeaders requestHeaders = new HttpHeaders();
	                    requestHeaders.setProperty(REFERER, referrer);
	                    PrimaryResourceFetchThread thread = new PrimaryResourceFetchThread(e.getLocation(), requestHeaders,null, event, this);
	                    thread.start();
	                    break;

	            } 
	            case Event.EVENT_CLOSE :
	                // TODO: close the appication
	                break;
	            
	            case Event.EVENT_SET_HEADER :        // No cache support.
	            case Event.EVENT_SET_HTTP_COOKIE :   // No cookie support.
	            case Event.EVENT_HISTORY :           // No history support.
	            case Event.EVENT_EXECUTING_SCRIPT :  // No progress bar is supported.
	            case Event.EVENT_FULL_WINDOW :       // No full window support.
	            case Event.EVENT_STOP :              // No stop loading support.
	            default :
	        }

	        return null;
	    }

	    /**
	     * @see net.rim.device.api.browser.RenderingApplication#getAvailableHeight(net.rim.device.api.browser.BrowserContent)
	     */
	    public int getAvailableHeight(BrowserContent browserField) 
	    {
	        // Field has full screen.
	        return Display.getHeight();
	    }

	    /**
	     * @see net.rim.device.api.browser.RenderingApplication#getAvailableWidth(net.rim.device.api.browser.BrowserContent)
	     */
	    public int getAvailableWidth(BrowserContent browserField) 
	    {
	        // Field has full screen.
	        return Display.getWidth();
	    }

	    /**
	     * @see net.rim.device.api.browser.RenderingApplication#getHistoryPosition(net.rim.device.api.browser.BrowserContent)
	     */
	    public int getHistoryPosition(BrowserContent browserField) 
	    {
	        // No history support.
	        return 0;
	    }
	    

	    /**
	     * @see net.rim.device.api.browser.RenderingApplication#getHTTPCookie(java.lang.String)
	     */
	    public String getHTTPCookie(String url) 
	    {
	        // No cookie support.
	        return null;
	    }

	    /**
	     * @see net.rim.device.api.browser.RenderingApplication#getResource(net.rim.device.api.browser.RequestedResource,
	     *      net.rim.device.api.browser.BrowserContent)
	     */
	    public HttpConnection getResource( RequestedResource resource, BrowserContent referrer) 
	    {
	        if (resource == null) 
	        {
	            return null;
	        }

	        // Check if this is cache-only request.
	        if (resource.isCacheOnly()) 
	        {
	            // No cache support.
	            return null;
	        }

	        String url = resource.getUrl();

	        if (url == null) 
	        {
	            return null;
	        }

	        // If referrer is null we must return the connection.
	        if (referrer == null) 
	        {
	            HttpConnection connection = makeConnection(resource.getUrl(), resource.getRequestHeaders(), null);
	            
	            return connection;
	            
	        } 
	        else 
	        {
	            // If referrer is provided we can set up the connection on a separate thread.
	           // SecondaryResourceFetchThread.enqueue(resource, referrer);
	        }

	        return null;
	    }

	    /**
	     * @see net.rim.device.api.browser.RenderingApplication#invokeRunnable(java.lang.Runnable)
	     */
	    public void invokeRunnable(Runnable runnable) 
	    {       
	        (new Thread(runnable)).start();
	    }    
	

	
	public BaseController getController() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	  public static HttpConnection makeConnection(String url, HttpHeaders requestHeaders, byte[] postData) 
	    {
	        HttpConnection conn = null;
	        OutputStream out = null;
	        
	        try 
	        {
	           conn = (HttpConnection) Connector.open(url);           

	        	
	            if (requestHeaders != null) 
	            {
	                // From
	                // http://www.w3.org/Protocols/rfc2616/rfc2616-sec15.html#sec15.1.3
	                //
	                // Clients SHOULD NOT include a Referer header field in a (non-secure) HTTP 
	                // request if the referring page was transferred with a secure protocol.
	                String referer = requestHeaders.getPropertyValue("referer");
	                boolean sendReferrer = true;
	                
	                if (referer != null && StringUtilities.startsWithIgnoreCase(referer, "https:") && !StringUtilities.startsWithIgnoreCase(url, "https:")) 
	                {             
	                    sendReferrer = false;
	                }
	                
	                int size = requestHeaders.size();
	                for (int i = 0; i < size;) 
	                {                    
	                    String header = requestHeaders.getPropertyKey(i);
	                    
	                    // Remove referer header if needed.
	                    if ( !sendReferrer && header.equals("referer")) 
	                    {
	                        requestHeaders.removeProperty(i);
	                        --size;
	                        continue;
	                    }
	                    
	                    String value = requestHeaders.getPropertyValue( i++ );
	                    if (value != null) 
	                    {
	                        conn.setRequestProperty( header, value);
	                    }
	                }                
	            }                          
	            
	            if (postData == null) 
	            {
	                conn.setRequestMethod(HttpConnection.GET);
	            } 
	            else 
	            {
	                conn.setRequestMethod(HttpConnection.POST);

	                conn.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, String.valueOf(postData.length));

	                out = conn.openOutputStream();
	                out.write(postData);

	            }

	        } 
	        catch (IOException e1) 
	        {
	        } 
	        finally 
	        {
	            if (out != null) 
	            {
	                try 
	                {
	                    out.close();
	                } 
	                catch (IOException e2) 
	                {
	                }
	            }
	        }    
	        
	        return conn;
	    }
	
	private class PrimaryResourceFetchThread extends Thread 
	{
	    
	    private PostPreviewView _application;
	    private Event _event;
	    private byte[] _postData;
	    private HttpHeaders _requestHeaders;
	    private String _url;
	    
	    PrimaryResourceFetchThread(String url, HttpHeaders requestHeaders, byte[] postData, 
	                                  Event event, PostPreviewView application) 
	    {
	        _url = url;
	        _requestHeaders = requestHeaders;
	        _postData = postData;
	        _application = application;
	        _event = event;
	    }

	    public void run() 
	    {
	        //HttpConnection connection = makeConnection(_url, _requestHeaders, _postData);
	    	HttpConnection connection = new TestInputConn2http();
	        _application.processConnection(connection, _event);        
	    }
	    
	}
	
	class TestInputConn2http implements HttpConnection {

		public TestInputConn2http(){
			
		}
		
		public long getDate() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		public long getExpiration() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getFile() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getHeaderField(String name) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getHeaderField(int n) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public long getHeaderFieldDate(String name, long def)
				throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getHeaderFieldInt(String name, int def) throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getHeaderFieldKey(int n) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getHost() {
			// TODO Auto-generated method stub
			return null;
		}

		public long getLastModified() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		public int getPort() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getProtocol() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getQuery() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRef() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRequestMethod() {
			// TODO Auto-generated method stub
			return null;
		}

		public String getRequestProperty(String key) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getResponseCode() throws IOException {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getResponseMessage() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public String getURL() {
			// TODO Auto-generated method stub
			return null;
		}

		public void setRequestMethod(String method) throws IOException {
			// TODO Auto-generated method stub
			
		}

		public void setRequestProperty(String key, String value)
				throws IOException {
			// TODO Auto-generated method stub
			
		}

		public String getEncoding() {
			// TODO Auto-generated method stub
			return "text/html";
		}

		public long getLength() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getType() {
			// TODO Auto-generated method stub
			return null;
		}

		public DataInputStream openDataInputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public InputStream openInputStream() throws IOException {
			
	         Class classs;
			try {
				classs = Class.forName("com.wordpress.view.PostPreviewView.TestInputConn2http");
	            //to actually retrieve the resource prefix the name of the file with a "/"
	            InputStream is = classs.getResourceAsStream("/defaultPostTemplate.html");
	            
	            return is;

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}

		public void close() throws IOException {
			// TODO Auto-generated method stub
			
		}

		public DataOutputStream openDataOutputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}

		public OutputStream openOutputStream() throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
}
