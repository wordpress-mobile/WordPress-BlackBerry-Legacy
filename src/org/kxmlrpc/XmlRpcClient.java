/* kxmlrpc - XML-RPC for J2ME
 *
 * Copyright (C) 2001  Kyle Gabhart ( kyle@gabhart.com )
 *
 * Contributors: David Johnson ( djohnsonhk@users.sourceforge.net )
 * 				   Stefan Haustein
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * modified: Danilo Ercoli ercoli@gmail.com
 */

package org.kxmlrpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;

import com.wordpress.utils.StringUtils;
import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Log;


/**
 * A single-threaded, reusable XML-RPC client object.
 */
public class XmlRpcClient {
    
    /**
     * Stores the full URL the client will connect with
     */
    String url;
    
    /**
     * Stores the response sent back by the server
     */
    Object result = null;
    
    /**
     * Turns debugging on/off
     */
    boolean debug = true;
    //contains all response headers
    private Hashtable responseHeaders = new Hashtable();
	private HttpConnection con;
	private InputStream in;
	private OutputStream out;
	private XmlRpcWriter writer = null;
	private boolean isStopped = false;
    
    
    /**
     * Constructs an XML-RPC client with a specified string representing a URL.
     *
     * @param url The full URL for the XML-RPC server
     */
    public XmlRpcClient( String url ) {
        this.url = url;
    }//end KxmlRpcClient( String )
    
    /**
     * Construct an XML-RPC client for the specified hostname and port.
     *
     * @param hostname the name of the host server
     * @param the server's port number
     */
    public XmlRpcClient( String hostname, int port ) {
        int delim = hostname.indexOf("/");
        String context = "";
        if (delim>0) {
            context = hostname.substring(delim);
            hostname = hostname.substring(0, delim);
        }
        this.url = "http://" + hostname + ":" + port + context;
    }//end KxmlRpcClient( String, int )
    
    public String getURL() {
        return url;
    }//end getURL()
    
    public void setURL( String newUrl ) {
        url = newUrl;
    }//end setURL( String )
    
        
    /**
     * This method is the brains of the XmlRpcClient class. It opens an
     * HttpConnection on the URL stored in the url variable, sends an XML-RPC
     * request and processes the response sent back from the server.
     *
     * @param method contains the method on the server that the
     * client will access
     * @param params contains a list of parameters to be sent to
     * the server
     * @return the primitive, collection, or custom object
     * returned by the server
     */
    public Object execute(String method, Vector params) throws Exception {
    	// kxmlrpc classes
    	KXmlSerializer          xw = null;
    	
    	XmlRpcParser            parser = null;
    	con = null;
    	in = null;
    	out = null;
    	
    	// Misc objects for buffering request
    	XmlRpcDualOutputStream os = new XmlRpcDualOutputStream();
    	
    	xw = new KXmlSerializer();
    	xw.setOutput(new OutputStreamWriter(os.getOutputStream()));
    	writer = new XmlRpcWriter(xw);
    	
    	writer.writeCall(method, params);
    	xw.flush();
    	os.close(); //close the dual output stream

    	if( isStopped == true ){
    		return null; //if the user has stopped the thread
    	}
    	con = (HttpConnection) ConnectionManager.getInstance().open(url);
    	Log.trace("grandezza del file da inviare "+ Long.toString(os.getMessageLength()));
    	
    	try {
    		
    		con.setRequestMethod(HttpConnection.POST);
    		con.setRequestProperty("Content-Length", Long.toString(os.getMessageLength()));
    		con.setRequestProperty("Content-Type", "text/xml");
    		//con.setRequestProperty("Transfer-encoding", "chunked");
    		
    		// Obtain an output stream
    		out = con.openOutputStream();
    		os.sendRequest(out);
        		    		
    		// List all the response headers from the server.
    		// Note: The first call to getHeaderFieldKey() will implicit send
    		// the HTTP request to the server.
    		Log.trace("==== Response headers from the server");
    		String   key;
    		for( int i = 0;( key = con.getHeaderFieldKey( i ) )!= null; ++i ){
    			String headerName = con.getHeaderFieldKey(i);
    			String headerValue = con.getHeaderField(i);
    			
    			if (headerName == null && headerValue == null) {
    				// No more headers
    				break;
    			}
    			if (headerName == null) {
    				// The header value contains the server's HTTP version
    			} else {
    				responseHeaders.put(headerName , headerValue);
    				Log.trace(headerName + " " + headerValue); 
    			}
    		}
    		Log.trace("=== End Response headers from the server");
    		
    		//find the respose content type from http header
    		String contentType = (String) responseHeaders.get("Content-Type");
    		String encoding = null;
    		if(contentType != null && contentType.indexOf("charset") > -1 ) {
    			String[] encodings = StringUtils.split(contentType, "=");
    			encoding = encodings[1];
    			encoding = StringUtils.replaceAll(encoding, ";", "");
    			
    			if(!StringUtils.isDeviceSupportEncoding(encoding)){
    				//set encoding to UTF-8 if response encoding is not supported
    				encoding = "UTF-8";
    			}
    		} else {
    			Log.debug("Response Content-type without charset");
    			encoding = "UTF-8";
    		}
    		Log.trace("Selected Encoding: "+ encoding);
    		
    		//check response code from the server       
    		int rc = con.getResponseCode();
    		if( rc != HttpConnection.HTTP_OK ){
    			Log.error("XmlRpcClient - Server Response Error. Server returned HTTP response code: "+ rc);
    			throw new Exception("Server returned HTTP response code "+rc);
    		}
    		
    		// Open an input stream on the server's response
    		in = con.openInputStream();
    		
    		int ch;
    		StringBuffer charBuff=new StringBuffer();
    		while ((ch = in.read()) > -1) {  
    			charBuff.append((char)ch);
    		}
    		String response = charBuff.toString();
    		Log.trace("response from the wordpress server: "+response);                                  
    		ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes());
    		
    		// Parse response from server
    		KXmlParser xp = new KXmlParser();
    		xp.setInput(bais, "ISO-8859-1"); //never change!
    		
    		
    		parser = new XmlRpcParser(xp, encoding); //pass the rim encoding
    		result = parser.parseResponse();
    		
    	} catch (Exception x) {
    		Log.error("Error in XmlRpcClient");
    		throw (Exception) x;
    	} finally {
    		os.clean();
    		closeXmlRpcConnection();//end try/catch
    	}//end try/catch/finally
    	
    
    	if(result instanceof Exception)
    		throw (Exception) result;
    	
    	return result;
    }//end execute( String, Vector )

    
    public void stopXmlRpcClient() {
		this.isStopped = true;
		if(writer != null) {
			writer.setStopEncoding(true);
		}
    }
        
	public void closeXmlRpcConnection() {
		try {
		    if (con != null) con.close();
		    if (in != null) in.close();
		    if (out != null) out.close();
		} catch (IOException ioe) {
			Log.error(ioe, "Error while closing xmlrpc conn");
		    ioe.printStackTrace();
		} finally {
			Log.trace("XmlRpc Input/Ouput Stream  set to null");
		    con = null;
		    in = null;
		    out = null;
		}
	}
    
    /**
     * Called when the return value has been parsed.
     */
    void setParsedObject(Object parsedObject) {
        result = parsedObject;
    }

    //get the response headers
	public Hashtable getResponseHeaders() {
		return responseHeaders;
	}
    
}