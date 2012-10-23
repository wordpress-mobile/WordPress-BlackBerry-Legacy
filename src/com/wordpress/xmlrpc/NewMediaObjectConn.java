package com.wordpress.xmlrpc;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;
import java.util.Vector;

import org.kobjects.base64.Base64;

import net.rim.device.api.crypto.BlockEncryptor;
import net.rim.device.api.crypto.TripleDESKey;
import net.rim.device.api.crypto.TripleDESEncryptorEngine;
import net.rim.device.cldc.io.ssl.TLSIOException;

import com.wordpress.bb.SSLPostingException;
import com.wordpress.model.MediaEntry;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;

public class NewMediaObjectConn extends BlogConn  {

	protected MediaEntry mediaObj =null;	
	protected String blogID;
	
	public MediaEntry getMediaObj() {
		return mediaObj;
	}
	
	public void setMediaObj(MediaEntry mediaObj) {
		this.mediaObj = mediaObj;
	}

	public NewMediaObjectConn(String hint,	String userHint, String passwordHint, String blogID,
			MediaEntry mediaObj) {
		super(hint, userHint, passwordHint);
		this.mediaObj=mediaObj;
		this.blogID=blogID;
	}

	
	/**
	 * 
	 * @param provider
	 */
	public void run() {
		try{

			Hashtable content = new Hashtable(2);
			content.put("name",mediaObj.getFileName());
			content.put("bits", mediaObj); //not loaded the bytearray of content, this is a reference to real file on disk. See the XmlRpcWriter!!
			content.put("type", mediaObj.getMIMEType());

			Vector args = new Vector(4);
			args.addElement(blogID);

			String methodName = "metaWeblog.newMediaObject";
			if( urlConnessione.indexOf("wordpress.com") != -1 && urlConnessione.startsWith("https") ){

				Vector upload_token_args = new Vector(4);
				upload_token_args.addElement(mUsername);
				upload_token_args.addElement(mPassword);

				Object responseKeys = execute("wpcom.blackberryGetUploadingFileKeys", upload_token_args);
				if( connResponse.isError() ) {
					notifyObservers(responseKeys);
					return;		
				} 
				
				Hashtable keysHashtable =(Hashtable)responseKeys;
				String temporaryPassword = (String)keysHashtable.get("temporary_password");
				String secretToken = (String)keysHashtable.get("3des_key");
				byte[] messageBytes = temporaryPassword.getBytes();
				byte[] keyBytes = secretToken.getBytes();
			
				urlConnessione = StringUtils.replaceAll(urlConnessione, "https://", "http://"); //switch to plain HTTP
				methodName = "wpcom.blackberryUploadFile"; //use a private method
				
				TripleDESKey desKey = new TripleDESKey(keyBytes);
				// Create the encryption engine for encrypting the data.
				TripleDESEncryptorEngine encryptionEngine = new TripleDESEncryptorEngine( desKey  );
				//PKCS5FormatterEngine padder = new PKCS5FormatterEngine(encryptionEngine);
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				BlockEncryptor encryptor = new BlockEncryptor(encryptionEngine, output);
				encryptor.write(messageBytes);
				encryptor.close();
				output.flush();
				byte[] cipherBytes = output.toByteArray();
				args.addElement(mUsername);
				args.addElement( Base64.encode( cipherBytes ) );
			} else {
				args.addElement(mUsername);
				args.addElement(mPassword);
			}
			args.addElement(content);
			
			mConnection = null; //Reset the connection since we want switch to HTTP.
			Object response = execute(methodName, args);
			
			//Show a detailed error message for the SSL post issue on WPCOM
			if( connResponse.isError() ) {
				
				if ( urlConnessione.startsWith( "https" ) && urlConnessione.indexOf( "wordpress.com" ) != -1 ) {
					Object refResponseObj = connResponse.getResponseObject();
					if ( refResponseObj instanceof TLSIOException || refResponseObj instanceof javax.microedition.pki.CertificateException ) {
						connResponse.setResponseObject(new SSLPostingException(""));
						connResponse.setResponse("Error while uploading media!");
					} else if (refResponseObj instanceof net.rim.device.api.io.ConnectionClosedException) {
						connResponse.setResponseObject(new SSLPostingException(""));
						connResponse.setResponse("Error while uploading media!");
					} else if (refResponseObj instanceof java.io.InterruptedIOException || refResponseObj instanceof java.io.IOException) {
						String eMsg = ((Exception) refResponseObj).getMessage();
						if ( eMsg != null && ( eMsg.indexOf("connection timed out") != -1 || eMsg.indexOf( "APN is not specified" ) != -1 
								|| eMsg.indexOf( "BIS conn: null" ) != -1 || eMsg.indexOf( "TCP conn" ) != -1 ) ) {
							connResponse.setResponseObject(new SSLPostingException(""));
							connResponse.setResponse("Error while uploading media!");
						}
					}
				}
				
				notifyObservers(connResponse);
				return;		
			}
			
			connResponse.setResponseObject(response);
		} catch (Exception cce) {
			setErrorMessage(cce, "Error while uploading media!");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("New Media Object Notify Error");
		}
	}
}