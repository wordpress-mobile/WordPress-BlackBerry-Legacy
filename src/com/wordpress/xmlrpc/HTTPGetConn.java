package com.wordpress.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.Base64OutputStream;

import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Log;

public class HTTPGetConn extends BlogConn  {

	private Hashtable responseHeaders = new Hashtable();
	private String cookie = null;
	
	public HTTPGetConn(String url, String username, String password) {
		super(url, username, password);
	}
	
    //get the response headers
	public Hashtable getResponseHeaders() {
		return responseHeaders;
	}

	
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	//we have overrided execute method, because there isn't xml-rpc conn, but only a simple http conn 
	protected Object execute(String aCommand, Vector aArgs) {
		isWorking=true;
		
		HttpConnection conn = null;
		Object response = null;
		byte[] encodedAuthCredential = null;
		
		try {
			int numberOfRedirection = 0;
			keepGoing = true;
			boolean askToUser = false;
			
	  		 while( keepGoing && numberOfRedirection < BlogConn.MAX_NUMBER_OF_REDIRECTIONS ){
	  		
				conn = (HttpConnection) ConnectionManager.getInstance().open(urlConnessione);

				if(encodedAuthCredential != null) {
                    //Add the authorized header.
					Log.trace("Added the authorized header");
                    conn.setRequestProperty("Authorization", "Basic " + new String(encodedAuthCredential));
				}
				if ( cookie != null )
					conn.setRequestProperty("Cookie", cookie);
				
				// List all the response headers from the server.
	    		// Note: The first call to getHeaderFieldKey() will implicit send
	    		// the HTTP request to the server.
	    		Log.trace("==== Response headers from the server");
	    		for( int i = 0; conn.getHeaderFieldKey(i)!= null; ++i ){
	    			String headerName = conn.getHeaderFieldKey(i);
	    			String headerValue = conn.getHeaderField(i);
	    			
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
	    		
				
				int rc = conn.getResponseCode();
				Log.trace("Server response  code: "+rc);
				switch( rc ){			  
		            case HttpConnection.HTTP_MOVED_PERM:
		            case HttpConnection.HTTP_MOVED_TEMP:
		            case HttpConnection.HTTP_SEE_OTHER:
		            case HttpConnection.HTTP_TEMP_REDIRECT:
		            numberOfRedirection++;
		             String URL = conn.getHeaderField( "Location" );
		              if( URL != null && URL.startsWith( 
		                                            "/*" ) ){
		                StringBuffer b = new StringBuffer();
		                b.append( conn.getProtocol()+"//" );
		                b.append( conn.getHost() );
		                b.append( ':' );
		                b.append( conn.getPort() );
		                b.append( URL );
		                urlConnessione = b.toString();
		              }
		              conn.close();
		              break;
		         
		            case HttpConnection.HTTP_OK:
						//read the response
						
						InputStream in = conn.openInputStream();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						int c;
						while ((c = in.read()) >= 0)
						{
							baos.write(c);
						}
						response = baos.toByteArray();
						keepGoing = false;
		            	break;
		         
		            case (HttpConnection.HTTP_UNAUTHORIZED):
		            	  //Connection is 401 UnAuthorized.

		            	//A login and password is required, try wt username and password from the blog first, if fails asks to user
		            	if (askToUser == false) {
		            		//checks if there are http 401 data in the blog data
		            		if(this.http401Password == null) {
		            			http401Password = this.mPassword;
		            			http401Username = this.mUsername;
		            		}
		            	} else {
		            		showHTTPAuthDialog();
		            	}
		             	
		            	if(http401Password != null) {
		            		String login = this.http401Username+ ":"+this.http401Password;
		            		//Encode the login information in Base64 format.
		            		encodedAuthCredential = Base64OutputStream.encode(login.getBytes(), 0, login.length(), false, false);
		            	} 
		            	askToUser = true;
		            	break;
		            	
		            default:
		            	throw new Exception(""+conn.getResponseCode());
				}
	  		 }//end while
	  		 
	  		 /*
	  		  * an http get could return an with empty body response.
	  		 //check the response from the server
	  		 if(response == null) {
	  			 Log.trace("HTTP RESPONSE IS EMPTY.");
	  			 setErrorMessage("Server Response Empty");
	  		 }
	  		 */
		} catch (Exception e) {
			 setErrorMessage(e, "A server communications error occurred:");
		}
		Log.trace("termine richiesta HTTP-GET");
		isWorking=false;
		return response;
	}
	

	public void run() {
		try {
			
			Object response = execute("", null);

			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;	
			}
			connResponse.setResponseObject(response);
		}
		catch (Exception cce) {
			setErrorMessage(cce, "Connection Error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("Connection Error: Notification error"); 		
		}
	}
}