package com.wordpress.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.browser.field.BrowserContent;
import net.rim.device.api.browser.field.RequestedResource;
import net.rim.device.api.io.http.HttpHeaders;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.util.StringUtilities;

import com.wordpress.io.JSR75FileSystem;
import com.wordpress.utils.conn.ConnectionManager;


public class SecondaryResourceFetchThread extends Thread 
{

    /**
     * Callback browser field.
     */
    private BrowserContent _browserField;
    
    /**
     * Images to retrieve.
     */
    private Vector _imageQueue;
    
    /**
     * True is all images have been enqueued.
     */
    private boolean _done;
    
    /**
     * Sync object.
     */
    private static Object _syncObject = new Object();
    
    /**
     * Secondary thread.
     */
    private static SecondaryResourceFetchThread _currentThread;
    
    
    /**
     * Enqueues secondary resource for a browser field.
     * 
     * @param resource - resource to retrieve.
     * @param referrer - call back browsr field.
     */
    public static void enqueue(RequestedResource resource, BrowserContent referrer) 
    {
        if (resource == null) 
        {
            return;
        }
        
        synchronized( _syncObject ) 
        {
            
            // Create new thread.
            if (_currentThread == null) 
            {
                _currentThread = new SecondaryResourceFetchThread();
                _currentThread.start();
            } 
            else 
            {
                // If thread alread is running, check that we are adding images for the same browser field.
                if (referrer != _currentThread._browserField) 
                {  
                    synchronized( _currentThread._imageQueue) 
                    {
                        // If the request is for a different browser field,
                        // clear old elements.
                        _currentThread._imageQueue.removeAllElements();
                    }
                }
            }   
            
            synchronized( _currentThread._imageQueue) 
            {
                _currentThread._imageQueue.addElement(resource);
            }
            
            _currentThread._browserField = referrer;
        }
    }
    
    /**
     * Constructor
     *
     */
    private SecondaryResourceFetchThread() 
    {
        _imageQueue = new Vector();        
    }
    
    /**
     * Indicate that all images have been enqueued for this browser field.
     */
    public static void doneAddingImages() 
    {
        synchronized( _syncObject ) 
        {
            if (_currentThread != null) 
            {
                _currentThread._done = true;
            }
        }
    }
    
    /**
     * Indicate that all images have been enqueued for this browser field.
     */
    public static void stopAllActivity() 
    {
        synchronized( _syncObject ) 
        {
            if (_currentThread != null) 
            {
            	 _currentThread._imageQueue.removeAllElements(); //just remove all enqueed elements
             //   _currentThread.interrupt();
               // _currentThread = null;
            }
        }
    }
    
    
    public void run() 
    {
        while (true) 
        {
            if (_done) 
            {
                // Check if we are done requesting images.
                synchronized( _syncObject ) 
                {
                    synchronized( _imageQueue ) 
                    {
                        if (_imageQueue.size() == 0) 
                        {
                            _currentThread = null;   
                            break;
                        }
                    }
                }
            }
            
            RequestedResource resource = null;
                              
            // Request next image.
            synchronized( _imageQueue ) 
            {
                if (_imageQueue.size() > 0) 
                {
                    resource = (RequestedResource)_imageQueue.elementAt(0);
                    _imageQueue.removeElementAt(0);
                }
            }
            
            if (resource != null) 
            {
                //remote connection
            	if(resource.getUrl().startsWith("http")) {
            		HttpConnection connection = makeConnection(resource.getUrl(), resource.getRequestHeaders(), null);
            		resource.setHttpConnection(connection);
            	} else { //local connection
            		try {
						resource.setHttpConnection(new LocalHttpConn(JSR75FileSystem.readFile(resource.getUrl())));
					} catch (IOException e) {

					}
            	}
                
                // Signal to the browser field that resource is ready.
                if (_browserField != null) 
                {            
                    _browserField.resourceReady(resource);
                }
            }
        }       
    }   
    
    
    
    public static HttpConnection makeConnection(String url, HttpHeaders requestHeaders, byte[] postData) 
    {
        HttpConnection conn = null;
        OutputStream out = null;
        
        try 
        {
        	conn = (HttpConnection) ConnectionManager.getInstance().open(url);
        	conn.setRequestProperty("User-Agent","wp-blackberry/"+ Tools.getAppVersion());
        	
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
}
