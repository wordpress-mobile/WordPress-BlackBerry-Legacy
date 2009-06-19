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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
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
        XmlRpcWriter            writer = null;
        XmlRpcParser            parser = null;
        // J2ME classes
        HttpConnection          con = null;
        InputStream             in = null;
        OutputStream            out = null;
        // Misc objects for buffering request
        ByteArrayOutputStream   bos = null;
        byte[]                  request;
        int                     messageLength;
        
            bos = new ByteArrayOutputStream();
            xw = new KXmlSerializer();
            xw.setOutput(new OutputStreamWriter(bos));
            writer = new XmlRpcWriter(xw);
            
            writer.writeCall(method, params);
            xw.flush();
            
         
            Log.trace("request sended to the wordpress server: "+bos.toString());
            request = bos.toByteArray();   
            messageLength = request.length;
            
            //con = (HttpConnection) Connector.open(url, Connector.READ_WRITE);
          	con = (HttpConnection) ConnectionManager.getInstance().open(url);
            
      	try {
      		
            con.setRequestMethod(HttpConnection.POST);
            con.setRequestProperty("Content-Length", Integer.toString(messageLength));
            con.setRequestProperty("Content-Type", "text/xml");
            
            // Obtain an output stream
            out = con.openOutputStream();
            // Push the request to the server
            out.write( request );
/*
            
          // List all the response headers from the server.
            // Note: The first call to getHeaderFieldKey() will implicit send
            // the HTTP request to the server.
            Log.debug("Response headers from the server");
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
            	}
            	Log.debug(headerName + " " + headerValue);
            }
            Log.debug("End Response headers from the server");
            
  */          
            // Open an input stream on the server's response
            in = con.openInputStream();
            
            //we now have an input stream. Create a reader and read out each character in the stream.
            //TODO remove this block of code in production
            InputStreamReader isr = new InputStreamReader(in,"UTF-8"); //@see http://java.sun.com/docs/books/tutorial/i18n/text/stream.html
            int ch;
            StringBuffer charBuff=new StringBuffer();
            while ((ch = isr.read()) > -1) {  
               charBuff.append((char)ch);
            }
            String response = charBuff.toString();
            Log.trace("response from the wordpress server: "+response);                      
            
            response = StringUtils.replaceAll(response, "&amp;amp;", "&amp;"); //FIX WP DOUBLE ENCODED AMPESAND;
            ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes());
            //end           
         	         	
            // Parse response from server
            KXmlParser xp = new KXmlParser();
            xp.setInput(new InputStreamReader(bais));
            parser = new XmlRpcParser(xp);
            result = parser.parseResponse();
            
        } catch (Exception x) {
        	Log.error("Error in XmlRpcClient");
        	throw (Exception) x;
        } finally {
            try {
                if (con != null) con.close();
                if (in != null) in.close();
                if (out != null) out.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }//end try/catch
        }//end try/catch/finally
        
        if(result instanceof Exception)
            throw (Exception) result;
        
        return result;
    }//end execute( String, Vector )
    
    /**
     * Called when the return value has been parsed.
     */
    void setParsedObject(Object parsedObject) {
        result = parsedObject;
    }//end objectCompleted( Object )
    
}//end class KXmlRpcClient