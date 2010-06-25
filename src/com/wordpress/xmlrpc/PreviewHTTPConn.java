package com.wordpress.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Log;

public class PreviewHTTPConn extends BlogConn  {

	protected Hashtable responseHeaders = new Hashtable();
	
	public PreviewHTTPConn(String url) {
		super(url, "", "");
	}

	//we have overrided execute method, because there isn't xml-rpc conn, but only a simple http conn 
	protected Object execute(String aCommand, Vector aArgs){
		isWorking=true;
		
		HttpConnection conn = null;
		byte[] response = null;
		
		try {
			conn = (HttpConnection) ConnectionManager.getInstance().open(urlConnessione);
			int rc = conn.getResponseCode();
			if( rc == HttpConnection.HTTP_OK ){
				
				//read the response
				InputStream in = conn.openInputStream();
				
				//read headers
				String key;
				for (int i = 0; (key = conn.getHeaderFieldKey(i)) != null; ++i) {
					String headerName = conn.getHeaderFieldKey(i);
					String headerValue = conn.getHeaderField(i);

					if (headerName == null && headerValue == null) {
						// No more headers
						break;
					}
					if (headerName == null) {
						// The header value contains the server's HTTP version
					} else {
						responseHeaders.put(headerName, headerValue);
					}
				}
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int c;
				while ((c = in.read()) >= 0)
				{
					baos.write(c);
				}
				response = baos.toByteArray();
				
			} else {
			    // deal with errors, warnings, redirections, etc.
				throw new Exception(""+conn.getResponseCode());
			}

		} catch (Exception e) {
			 setErrorMessage(e, "A server communications error occurred:");
		}
		Log.trace("termine richiesta HTTP-GET");
		isWorking=false;
		return response;
	}
	

	public Hashtable getResponseHeaders() {
		return responseHeaders;
	}

	public void run() {
		try {
			
			Object response = execute("", null);

			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;	
			}
			
			Hashtable responseHash = new Hashtable();
			responseHash.put("data", response);
			responseHash.put("headers", responseHeaders);
			connResponse.setResponseObject(responseHash);
		}
		catch (Exception cce) {
			setErrorMessage(cce, "Get Template error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("Get Template error: Notify error"); 		
		}
	}
}

