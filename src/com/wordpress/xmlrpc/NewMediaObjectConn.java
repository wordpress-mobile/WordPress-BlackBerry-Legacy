package com.wordpress.xmlrpc;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.cldc.io.ssl.TLSIOException;

import com.wordpress.bb.SSLPostingException;
import com.wordpress.model.MediaEntry;
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
			content.put("bits", mediaObj); //not loaded the bytearray of content, this is a reference to real file on disk. Look ad XmlRpcWriter!!
			content.put("type", mediaObj.getMIMEType());

			Vector args = new Vector(4);
			args.addElement(blogID);
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(content);

			Object response = execute("metaWeblog.newMediaObject", args);
			if(connResponse.isError()) {
				//Show a detailed error message for the SSL post issue on WPCOM
				if ( urlConnessione.startsWith( "https" ) && urlConnessione.indexOf( "wordpress.com" ) != -1 ) {
					if (connResponse.getResponseObject() instanceof TLSIOException || (connResponse.getResponseObject() instanceof javax.microedition.pki.CertificateException) ) {
						connResponse.setResponseObject(new SSLPostingException(""));
					} 
				}	
				notifyObservers(connResponse);
				return;		
			}
			connResponse.setResponseObject(response);
		} catch (Exception cce) {
			setErrorMessage(cce, "New Media upload error");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("New Media Object Notify Error");
		}
	}
}