package com.wordpress.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Log;

public class HTTPPostConn extends BlogConn  {

	private final byte[] postContent;

	public HTTPPostConn (String url, byte[] postContent) {
		super(url, "", "");
		this.postContent = postContent;
	}
	
	//we have overrided execute method, because there isn't xml-rpc conn, but only a simple POST http conn 
	protected Object execute(String aCommand, Vector aArgs){
		isWorking=true;
		
		HttpConnection conn = null;
		String response = null;
			
		try {
			
			conn = (HttpConnection) ConnectionManager.getInstance().open(urlConnessione);
			conn.setRequestMethod( HttpConnection.POST ); //setupPost method for this conn
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			if(postContent != null) {
				DataOutputStream dos = new DataOutputStream( conn.openOutputStream() );
				dos.write(postContent);
				dos.flush();
				dos.close();
			}
			
			int rc = conn.getResponseCode();
			if( rc == HttpConnection.HTTP_OK ){
				
				//read the response
				
				InputStream in = conn.openInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				int c;
				while ((c = in.read()) >= 0)
				{
					baos.write(c);
				}
				response = new String (baos.toByteArray());
				Log.trace("Response from HTTP POST conn: "+response);
			} else {
			    // deal with errors, warnings, redirections, etc.
				throw new Exception(""+conn.getResponseCode());
			}

		} catch (Exception e) {
			 setErrorMessage(e, "A server communications error occurred:");
		}
		Log.trace("termine richiesta HTTP-POST");
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
			setErrorMessage(cce, "Get Template error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("Get Template error: Notify error"); 		
		}
	}
}