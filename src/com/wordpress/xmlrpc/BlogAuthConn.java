package com.wordpress.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.io.HttpConnection;

import net.rim.device.api.io.Base64OutputStream;

import org.kxml2.io.KXmlParser;
import org.kxmlrpc.XmlRpcClient;
import org.xmlpull.v1.XmlPullParser;

import com.wordpress.model.Blog;
import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.Log;

public class BlogAuthConn extends BlogConn  {

	private String xPingbackString = null;
	private boolean discoveryApiLink = false;
	
	public void setDiscoveryApiLink(boolean discoveryApiLink) {
		this.discoveryApiLink = discoveryApiLink;
	}

	public BlogAuthConn(String hint,String userHint, String passwordHint) {
		super(hint, userHint, passwordHint);
	}
	
	private void resetConnectionResponse() {
		connResponse.setError(false);
		connResponse.setStopped(false);
		connResponse.setResponse("");
		connResponse.setResponseObject(null);
	}
	
	private Object guessUrl(){
		Vector args;
		resetConnectionResponse();
		args = new Vector(2);
		args.addElement(this.mUsername);
		args.addElement(this.mPassword);
		
		// blogger_getUsersBlogs
		Object response = execute("wp.getUsersBlogs", args);
		if(connResponse.isStopped()) return null; //if the user has stopped the connection
		if(connResponse.isError()) {
			resetConnectionResponse();		
			//try with old blogger xml-rpc call
			args.insertElementAt("",0); //blogger api need key
			response = execute("blogger.getUsersBlogs", args);
			if(connResponse.isStopped()) return null; //if the user has stopped the connection
			if(connResponse.isError()) {
				response = null; //if still error there, no reset response
			}
		}
		return response;
	}
 
	/**
	 * Return the content of the url as string. 
	 * 
	 * @param URL
	 * @return
	 */
	
	private String getHtml(String URL) {
		isWorking=true;
		HttpConnection conn = null;
		String response = null;
		int numberOfRedirection = 0;
		byte[] encodedAuthCredential = null;
		
		if(http401Password != null) {
			String login = this.http401Username+ ":"+this.http401Password;
			//Encode the login information in Base64 format.
			try {
				encodedAuthCredential = Base64OutputStream.encode(login.getBytes(), 0, login.length(), false, false);
			} catch (IOException e) {
				Log.error(e, "Error while encoding auth credentials");
			}
		} 

		try {
			keepGoing = true;
			while( URL != null && keepGoing && numberOfRedirection < BlogConn.MAX_NUMBER_OF_REDIRECTIONS ){

				conn = (HttpConnection) ConnectionManager.getInstance().open(URL);
				
				if(encodedAuthCredential != null) {
                    //Add the authorized header.
					Log.trace("Added the authorized header");
                    conn.setRequestProperty("Authorization", "Basic " + new String(encodedAuthCredential));
				}

				// List all the response headers from the server.
				// Note: The first call to getHeaderFieldKey() will implicit send
				// the HTTP request to the server.
				Log.trace("==== Response headers from the server");
				String   key;
				for( int i = 0;( key = conn.getHeaderFieldKey( i ) )!= null; ++i ){
					String headerName = conn.getHeaderFieldKey(i);
					String headerValue = conn.getHeaderField(i);

					if (headerName == null && headerValue == null) {
						// No more headers
						break;
					}
					if (headerName == null) {
						// The header value contains the server's HTTP version
					} else {
						//set the x-pingback url
						if(headerName.equalsIgnoreCase("X-Pingback") && headerValue != null) {
							xPingbackString = (String)headerValue;
						}
						Log.trace(headerName + " " + headerValue); 
					}
				}
				Log.trace("=== End Response headers from the server");

				int rc = conn.getResponseCode();
				Log.trace("Html server response  code: "+rc);
				switch( rc ){			  
				case HttpConnection.HTTP_MOVED_PERM:
				case HttpConnection.HTTP_MOVED_TEMP:
				case HttpConnection.HTTP_SEE_OTHER:
				case HttpConnection.HTTP_TEMP_REDIRECT:
					numberOfRedirection++;
					URL = conn.getHeaderField( "Location" );
					if( URL != null && URL.startsWith( 
					"/*" ) ){
						StringBuffer b = new StringBuffer();
						b.append( conn.getProtocol()+"//" );
						b.append( conn.getHost() );
						b.append( ':' );
						b.append( conn.getPort() );
						b.append( URL );
						URL = b.toString();
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
					response = new String (baos.toByteArray());
					URL = null; //exit the while
					break;
				case (HttpConnection.HTTP_UNAUTHORIZED):
					//Connection is 401 UnAuthorized.
					showHTTPAuthDialog();
					if(http401Password != null) {
						String login = this.http401Username+ ":"+this.http401Password;
						//Encode the login information in Base64 format.
						encodedAuthCredential = Base64OutputStream.encode(login.getBytes(), 0, login.length(), false, false);
					} 
				break;
				default:
					URL = null;
				break;
				}
			}//end while
		} catch (Exception e) {
			Log.error("Html server connetion error: "+e.getMessage());
		}
		return response;
	}


	private String getRSDMetaTagHref(String urlConnessione) {
		//get the html code
		String htmlString = getHtml(urlConnessione); 
		
		//parse the html and get the attribute for xmlrpc endpoint
		if(htmlString != null) {
			try {				
				KXmlParser parser = new KXmlParser();
				parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true); //relaxed parser
				ByteArrayInputStream bais = new ByteArrayInputStream(htmlString.getBytes());
				parser.setInput(bais, "ISO-8859-1");
				
				while (parser.next() != XmlPullParser.END_DOCUMENT) {
					if (parser.getEventType() == XmlPullParser.START_TAG) {
						String rel="";
						String type="";
						String href="";
					//	Log.trace("start tag: " + parser.getName());
						//link tag
						if(parser.getName()!=null && parser.getName().trim().equalsIgnoreCase("link")){
						  //unfold all attribute
							for (int i = 0; i < parser.getAttributeCount(); i++) {
							  String attrName = parser.getAttributeName(i);
							  String attrValue = parser.getAttributeValue(i);
					           if("rel".equals(attrName))
					        	   rel = attrValue;
					           else if("type".equals(attrName))
					        	   type = attrValue;
					           else if("href".equals(attrName))
					        	   href = attrValue;
					           
						//	  Log.trace("attribute name: "+ parser.getAttributeName(i));
						//	  Log.trace("attribute value: "+parser.getAttributeValue(i));
					        }
							
						  if(rel.equals("EditURI") && type.equals("application/rsd+xml")){
							  return href;
						  }
						
						}//end link tag
					}
				}				
			} catch (Exception ex) {
				Log.error("RSD meta discovery error");
				Log.error(ex.getMessage());
			}
		}
		return null;
	}

	
	
	private String getWordPressApiLink(String urlConnessione) {
		//get the html code
		String htmlString = getHtml(urlConnessione); 
		
		//parse the html and get the attribute for xmlrpc endpoint
		if(htmlString != null) {
			try {				
				KXmlParser parser = new KXmlParser();
				ByteArrayInputStream bais = new ByteArrayInputStream(htmlString.getBytes());
				parser.setInput(bais, "ISO-8859-1");
				
				while (parser.next() != XmlPullParser.END_DOCUMENT) {
					if (parser.getEventType() == XmlPullParser.START_TAG) {
						String name="";
						String apiLink="";
					//	Log.trace("start tag: " + parser.getName());
						//link tag
						if(parser.getName()!=null && parser.getName().trim().equalsIgnoreCase("api")){
						  //unfold all attribute
							for (int i = 0; i < parser.getAttributeCount(); i++) {
							  String attrName = parser.getAttributeName(i);
							  String attrValue = parser.getAttributeValue(i);
					           if("name".equals(attrName))
					        	   name = attrValue;
					           else if("apiLink".equals(attrName))
					        	   apiLink = attrValue;
							//  Log.trace("attribute name: "+ parser.getAttributeName(i));
							//  Log.trace("attribute value: "+parser.getAttributeValue(i));
					        }
							
						  if(name.equals("WordPress") ){
							  return apiLink;
						  }
						
						}//end link tag
					}
				}				
			} catch (Exception ex) {
				Log.error("Api Link discovery error");
				Log.error(ex.getMessage());
			}
		}
		return null;
	}
	
	/**
	 * Load blogs 
	 * 
	 */
	public void run() {
		Vector args;

		args = new Vector(2);
		args.addElement(this.mUsername);
		args.addElement(this.mPassword);
		
		if(discoveryApiLink == true) {
			Log.trace("Started the ApiLink discovery process");
			/*
			 * 0. try to locate the RSD meta tag
			 * 1. try the X-ping back header as xmlrpc endpoint if fails
			 * 2. try user inserted url
			 */
			String xmlRpcWordPressEndPoint = null;
	
			String rsdMeta = getRSDMetaTagHref(urlConnessione);
			if(rsdMeta != null) {
				Log.trace("Found RSD meta tag: "+rsdMeta);
				xmlRpcWordPressEndPoint = getWordPressApiLink(rsdMeta);
			} else
				Log.trace("NOT Found RSD meta tag");
			
			//try with the X-pingback header field if xmlRpcWordPressEndPoint is null 
			if(xmlRpcWordPressEndPoint == null) {
				if(xPingbackString != null ){
					Log.trace("NOT Found ApiLink, trying to find it by x-pingback header");
					xmlRpcWordPressEndPoint = getWordPressApiLink(xPingbackString+"?rsd");
				}
			}
			
			//we have found the xmlrpc real endpoint
			if(xmlRpcWordPressEndPoint != null){
				urlConnessione = xmlRpcWordPressEndPoint;
				mConnection = new XmlRpcClient(urlConnessione);
				Log.trace("ApiLink found at: "+urlConnessione);
			} else {
				Log.trace("ApiLink NOT found");
			}
			
			Log.trace("Ended the ApiLink discovery process");
		}
		
		if(connResponse.isStopped()) return ; //if the user has stopped the connection
		Object response = guessUrl();
		if(connResponse.isError()) {
			notifyObservers(connResponse);
			return;		
		}
	
		try {
			Vector blogs = (Vector) response;
			Blog[] myBlogs= new Blog[blogs.size()];
			
			Hashtable blogData = null;
			for (int i = 0; i < blogs.size(); i++) {
				blogData = (Hashtable) blogs.elementAt(i);
			
				Log.trace("blogId: "+String.valueOf(blogData.get("blogid")));
				Log.trace("blogName: "+(String) blogData.get("blogName"));
				Log.trace("blogURL: " +(String) blogData.get("url"));
				Log.trace("blogXMLRPC: " +(String) blogData.get("xmlrpc"));
			
				String url = null;
				if ( blogData.get("xmlrpc") != null ) {
					url = (String)blogData.get("xmlrpc");
				} else {
					Log.trace("blog xmlrpc response url was null");
					Log.trace("blog xmlrpc url was set to connection url: "+urlConnessione);
					url = urlConnessione; 
				}
				
				if(url == null || url.equalsIgnoreCase(""))
					continue; //skip this blog
				
				Blog currentBlog= new Blog(String.valueOf(blogData.get("blogid")),
						(String)blogData.get("blogName"),
						(String)blogData.get("url"), 
						url, 
						this.mUsername, 
						this.mPassword);
				
				if(http401Password != null && http401Username != null) {
					if(!http401Password.trim().equalsIgnoreCase("") 
							&& !http401Username.trim().equalsIgnoreCase("")) {
						currentBlog.setHTTPAuthPassword(http401Password);
						currentBlog.setHTTPAuthUsername(http401Username);
						currentBlog.setHTTPBasicAuthRequired(true);
						Log.trace("HTTP stored in the Blog data");
					}
				}
				myBlogs[i]=currentBlog;		
			}		
			
			connResponse.setResponseObject(myBlogs);
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "Loading Error");
		} catch (Exception e) {
			setErrorMessage(e, "Invalid server response");
		}
		
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("Loading Error"); 
		}
	}
}