/*
 *  Danilo Ercoli - ercoli@gmail.com
 */

package org.kxmlrpc;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.Base64OutputStream;

import org.kxml2.io.KXmlParser;
import org.kxml2.io.KXmlSerializer;

import com.wordpress.io.FileUtils;
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
    
	//401 HTTP Auth data
    private String http401Username = null;
	private String http401Password = null;
    
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
    }
    
    public void setURL( String newUrl ) {
        url = newUrl;
    }
     
	public void setHttp401Username(String http401Username) {
		this.http401Username = http401Username;
	}
	
	public void setHttp401Password(String http401Password) {
		this.http401Password = http401Password;
	}
  
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
    	KXmlSerializer xw = null; 	
    	XmlRpcParser parser = null;
    	con = null;
    	in = null;
    	out = null;

    	// Misc objects for buffering request
    	XmlRpcDualOutputStream os = new XmlRpcDualOutputStream();
    	
    	xw = new KXmlSerializer();
    	OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os.getOutputStream());
		xw.setOutput(outputStreamWriter);
    	writer = new XmlRpcWriter(xw);
    	writer.writeCall(method, params);
    	xw.flush();	
    	outputStreamWriter.flush();
    	outputStreamWriter.close();
    	os.close(); //close the dual output stream
    	
    	if( isStopped == true ){
    		return null; //if the user has stopped the thread
    	}

    	Log.trace("grandezza del file da inviare "+ Long.toString(os.getMessageLength()));

    	byte[] encodedAuthCredential = null;
		if(http401Password != null) {
			String login = this.http401Username+ ":"+this.http401Password;
			//Encodes login information in Base64 format.
			encodedAuthCredential = Base64OutputStream.encode(login.getBytes(), 0, login.length(), false, false);
		} 
    	
    	con = (HttpConnection) ConnectionManager.getInstance().open(url);
    	try {
    		
    		con.setRequestMethod(HttpConnection.POST);
    		con.setRequestProperty("Content-Length", Long.toString(os.getMessageLength()));
     	    con.setRequestProperty("Content-Type", "text/xml");
     		if(encodedAuthCredential != null) {
                //Add the authorized header.
				Log.trace("Added the authorized header");
				con.setRequestProperty("Authorization", "Basic " + new String(encodedAuthCredential));
			}
     	    
    		// Obtain an output stream
    		out = con.openOutputStream();
    		os.sendRequest(out);

    		// List all the response headers from the server.
    		// Note: The first call to getHeaderFieldKey() will implicit send
    		// the HTTP request to the server.
    		Log.trace("==== Response headers from the server");
    		
    		for( int i = 0; con.getHeaderFieldKey(i)!= null; ++i ){
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
    		KXmlParser xp = new KXmlParser();
    		//put the parser in the relaxed mode
    		xp.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true); 
    	//	if(Log.getDefaultLogLevel() >= Log.TRACE) {
    		int ch;
    		StringBuffer charBuff=new StringBuffer();
    		//get rid of junk characters before xml respons.  60 = '<'
    		ch = in.read();
    		while ( ch != 60 && ch != -1) {
    			ch = in.read();
    		}
    		charBuff.append((char)ch);
    		while ((ch = in.read()) > -1) {  
    			charBuff.append((char)ch);
    		}
    		String response = charBuff.toString();
    		Log.trace("response from the wordpress server: "+response);                                  
    		ByteArrayInputStream bais = new ByteArrayInputStream(response.getBytes());

    		// Parse response from server
    		xp.setInput(bais, "ISO-8859-1"); //never change!
//    		} else {
  //  			xp.setInput(in, "ISO-8859-1"); //never change!
    //		}
    		
    		parser = new XmlRpcParser(xp, encoding); //pass the rim encoding
    		result = parser.parseResponse();
    		
    	} catch (Exception x) {
    		Log.error(x, "Error in XmlRpcClient");
    		throw (Exception) x;
    	} catch (Throwable  t) { //capturing the JVM error. 
    		Log.error(t, "Serious Error in XmlRpcClient: " + t.getMessage());
    		throw new RuntimeException("Connection Failure");
    	}
    	finally {
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
			FileUtils.closeConnection(con);
			FileUtils.closeStream(in);
			FileUtils.closeStream(out);
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