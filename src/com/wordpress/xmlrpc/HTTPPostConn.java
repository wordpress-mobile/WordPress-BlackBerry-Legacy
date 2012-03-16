package com.wordpress.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.http.SimpleCookieManager;
import com.wordpress.utils.log.Log;

public class HTTPPostConn extends BlogConn  {

	private final byte[] postContent;
	private SimpleCookieManager cm = null;
	
	public HTTPPostConn (String url, byte[] postContent) {
		super(url, "", "");
		this.postContent = postContent;
	}
	
	//we have override execute method, because there isn't xml-rpc conn, but only a simple POST http conn 
	protected Object execute(String aCommand, Vector aArgs){
		isWorking=true;
		Hashtable responseHeaders = new Hashtable();
		HttpConnection conn = null;
		String response = null;
		int numberOfRedirection = 0;
		boolean keepGoing = true;
		try {
			while( keepGoing && numberOfRedirection < BlogConn.MAX_NUMBER_OF_REDIRECTIONS ){

				conn = (HttpConnection) ConnectionManager.getInstance().open(urlConnessione);
				conn.setRequestMethod( HttpConnection.POST ); //setupPost method for this conn
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				if( cm != null ) 
					cm.addCookiesToRequest(conn);
			
				if(postContent != null) {
					DataOutputStream dos = new DataOutputStream( conn.openOutputStream() );
					dos.write(postContent);
					dos.flush();
					dos.close();
				}

				// List all the response headers from the server.
				// Note: The first call to getHeaderFieldKey() will implicit send
				// the HTTP request to the server.
				//Log.trace("==== Response headers from the server");
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
						if( cm != null && headerName.equalsIgnoreCase("Set-Cookie")) 
							cm.storeCookie(conn, headerValue);
						//Log.trace(headerName + " " + headerValue); 
					}
				}
				//Log.trace("=== End Response headers from the server");	

				int rc = conn.getResponseCode();
				Log.trace("Server response  code: "+rc);
				switch( rc ){

				case HttpConnection.HTTP_MOVED_PERM:
				case HttpConnection.HTTP_MOVED_TEMP:
				case HttpConnection.HTTP_SEE_OTHER:
				case HttpConnection.HTTP_TEMP_REDIRECT:
					numberOfRedirection++;
					String URL = conn.getHeaderField( "Location" );
					if( URL != null && URL.startsWith( "/*" ) ){
						StringBuffer b = new StringBuffer();
						b.append( conn.getProtocol()+"//" );
						b.append( conn.getHost() );
						b.append( ':' );
						b.append( conn.getPort() );
						b.append( URL );
						urlConnessione = b.toString();
					} else if( URL != null && URL.startsWith( "http" ) ){
						urlConnessione = URL;
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
					response = new String(baos.toByteArray());
					//Log.trace("Response from HTTP POST conn: "+response);
					keepGoing = false;
					conn.close();
					break;

				default:
					throw new Exception(""+conn.getResponseCode());
				}//end switch

			}//end while 
		} catch (Exception e) {
			setErrorMessage(e, "A server communications error occurred:");
		}
		
		if( keepGoing == true && numberOfRedirection == BlogConn.MAX_NUMBER_OF_REDIRECTIONS) {
			setErrorMessage("Reached the maximum number of redirects");
		}
		
		Log.trace("termine richiesta HTTP-POST");
		isWorking = false;
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

	public void setCookieManager(SimpleCookieManager dummyCookieManager) {
		this.cm = dummyCookieManager;
	}
}