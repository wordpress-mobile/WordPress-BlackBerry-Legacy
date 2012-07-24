package com.wordpress.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.file.FileConnection;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import net.rim.device.api.io.Base64OutputStream;
import net.rim.device.cldc.io.ssl.TLSIOException;

import com.wordpress.bb.SSLPostingException;
import com.wordpress.io.FileUtils;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.MediaEntry;
import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Log;

public class AtomPubNewMediaObjectConn extends NewMediaObjectConn  {

	public AtomPubNewMediaObjectConn(String hint,	String userHint, String passwordHint, String blogID,
			MediaEntry mediaObj) {
		super(hint, userHint, passwordHint, blogID, mediaObj);
	}
	
	/**
	 * 
	 * @param provider
	 */
	public void run() {
		try{
			String atomPubURL = urlConnessione.endsWith("/") ? urlConnessione : urlConnessione+"/";
			atomPubURL+="wp-app.php/attachments";
			
			HttpConnection conn = null;
			InputStream is = null;
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			String uploadResponse = "";

			byte[] encodedAuthCredential = null;
		
			String login = mUsername+ ":"+this.mPassword; //http auth required by default from AtomPub
			encodedAuthCredential = Base64OutputStream.encode(login.getBytes(), 0, login.length(), false, false);
			
			try	{
				conn = (HttpConnection) ConnectionManager.getInstance().open(atomPubURL);
				conn.setRequestMethod( HttpConnection.POST ); //setupPost method for this conn
				conn.setRequestProperty("Content-Language", "en-US");
				conn.setRequestProperty("x-rim-transcode-content", "none");
				conn.setRequestProperty("Content-Length", Long.toString(JSR75FileSystem.getFileSize(mediaObj.getFilePath())));
				conn.setRequestProperty("Content-Type", mediaObj.getMIMEType());
				conn.setRequestProperty("Content-transfer-encodig", "binary");

				//remove the ext from the file name
				String slug = mediaObj.getFileName();
				int lastIdx = slug.lastIndexOf('.');
				if(lastIdx != -1 && lastIdx != 0) {
					slug = slug.substring(0, lastIdx);
				}
				conn.setRequestProperty("Slug", slug);
		
				if(encodedAuthCredential != null) {
					//Add the authorized header.
					Log.trace("Added the authorized header");
					conn.setRequestProperty("Authorization", "Basic " + new String(encodedAuthCredential));
				}
				OutputStream dout = conn.openOutputStream();
				sendMessage(dout, mediaObj.getFilePath());	
				dout.flush();
				String encoding = conn.getEncoding();

				int ch;
				is = conn.openInputStream();
				while ((ch = is.read()) != -1) {
					bos.write(ch);
				}
				uploadResponse = bos.toString();
				int responsecode = conn.getResponseCode();
				String responsemessage = conn.getResponseMessage();

				if (responsecode == 201) {

				} else if (responsecode == 403) {
					throw new Exception("AtomPub is disabled on your blog. You can enable it by going to Settings > Writing > Remote Publishing, checking the box next to \"Atom Publishing Protocol\", then pressing Save Changes.");
				} else  {
					throw new Exception("HTTP Error "+responsecode+" - "+responsemessage);
				}
			} catch(Exception e) {
				if ( urlConnessione.startsWith( "https" ) && urlConnessione.indexOf( "wordpress.com" ) != -1 ) {
					Object refResponseObj = connResponse.getResponseObject();
					if ( refResponseObj instanceof TLSIOException || refResponseObj instanceof javax.microedition.pki.CertificateException ) {
						throw new SSLPostingException("");
					} else if (refResponseObj instanceof net.rim.device.api.io.ConnectionClosedException) {
						String connectionClosedErrorMessage = ((net.rim.device.api.io.ConnectionClosedException) refResponseObj).getMessage();
						connectionClosedErrorMessage = connectionClosedErrorMessage != null ? connectionClosedErrorMessage.toLowerCase() : null;
						if ( connectionClosedErrorMessage != null && 
								( connectionClosedErrorMessage.indexOf("connection closed") != -1  ||  connectionClosedErrorMessage.indexOf("stream closed") != -1  ) )
							throw new SSLPostingException("");					
					}
				}
				throw e;
			} finally {
				if (bos != null) {
					try {
						bos.close();
					} catch (IOException e) {
					}
				}
				if(is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
				if(conn != null) {
					try {
						conn.close();
					} catch (IOException e) {
					}
				}
			}
			
			Log.trace("response from the AtomPub :" + uploadResponse);

			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
			
			Object response = getResponseProperties(uploadResponse) ;
			
			connResponse.setResponseObject(response);
		} catch (Exception cce) {
			setErrorMessage(cce, "AtomPub Error");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.error("New Media Object Notify Error");
		}
	}
	
	private Hashtable getResponseProperties(String responseMessage) throws Exception {

		Hashtable content = new Hashtable();
		//parse the html and get the attribute for xmlrpc endpoint
		KXmlParser parser = new KXmlParser();
		parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true); //relaxed parser
		ByteArrayInputStream bais = new ByteArrayInputStream(responseMessage.getBytes());
		parser.setInput(bais, "ISO-8859-1");

		while (parser.next() != XmlPullParser.END_DOCUMENT) {
			if (parser.getEventType() == XmlPullParser.START_TAG) {

				if(parser.getName()!=null && parser.getName().trim().equalsIgnoreCase("content")){
					//unfold all attribute
					for (int i = 0; i < parser.getAttributeCount(); i++) {
						String attrName = parser.getAttributeName(i);
						String attrValue = parser.getAttributeValue(i);
						if("src".equals(attrName)) {
							content.put("url", attrValue);
						}
					}
				} else if(parser.getName()!=null && parser.getName().trim().equalsIgnoreCase("title")){
					int nextEvent = parser.next(); 
					if( nextEvent == XmlPullParser.TEXT ) {
						String title = parser.getText();
						content.put("file", title);
					}
				}
			}
		}				

		if(!content.containsKey("file") || !content.containsKey("url")) {
			throw new Exception("The file cannot be uploaded, please check your blog configuration.");
		}
		String file = ((String)content.get("file")).trim();
		String url = ((String)content.get("url")).trim();
		
		if(file.equals("") || url.equals(""))
			throw new Exception("The file cannot be uploaded, please check your blog configuration.");
		
		/*clean the response
		file = StringUtils.replaceAll(file, "/", "\\");
		content.put("file", file);
		url = StringUtils.replaceAll(url, "/", "\\");
		content.put("url", url);*/
		return content;
	}
	
	
	private void sendMessage(OutputStream dout, String filePath) {
		FileConnection fcon = null;
		InputStream imgIs = null;
		try {
			FileConnection filecon = (FileConnection) Connector.open(filePath);
			if (!filecon.exists()) {
				throw new IOException("File does not exist!");
			}
			InputStream inStream = filecon.openDataInputStream();
			byte[] buffer = new byte[1024]; 
			int length = -1;
			while ((length = inStream.read(buffer)) >0 ) {
				dout.write(buffer, 0 , length);
			}
			FileUtils.closeStream(inStream);
			FileUtils.closeConnection(filecon);
		} catch (Exception e) {
			Log.error(e, "Error while sending media");
		} finally {
			if (imgIs != null) {
				try {
					imgIs.close();
				} catch (IOException e) {
				}
			}
			if (fcon != null) {
				try {
					fcon.close();
				} catch (IOException e) {
				}
			}
		}
	}
}