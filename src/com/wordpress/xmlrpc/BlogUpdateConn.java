package com.wordpress.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;

import org.kxml2.io.KXmlParser;
import org.kxmlrpc.XmlRpcException;
import org.xmlpull.v1.XmlPullParser;

import com.wordpress.io.BlogDAO;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;

public class BlogUpdateConn extends BlogConn  {
	
	private Blog blog;
	
	private String wholeErrorMessage = ""; 
	private boolean isError = false;
	
	public BlogUpdateConn(Blog blog) {
		super(blog.getXmlRpcUrl(), blog.getUsername(), blog.getPassword());
		this.blog=blog;
		if(blog.isHTTPBasicAuthRequired()) {
			this.setHttp401Password(blog.getHTTPAuthPassword());
			this.setHttp401Username(blog.getHTTPAuthUsername());
		}
	}

	private void checkConnectionResponse(String errorTitle) throws Exception {
		if(connResponse.isError()) {
			if ( connResponse.getResponseObject() instanceof XmlRpcException) {
				//do nothing. here we capturing all permission denied for blog...
				//or xmlrpc method missing (old wp)"
			/*	XmlRpcException responseObject = (XmlRpcException) connResponse.getResponseObject();
				if(responseObject.code == 403) { //bad login 
					connResponse.setResponseObject(new Exception(_resources.getString(WordPressResource.MESSAGE_BAD_USERNAME_PASSWORD)));
					connResponse.setResponse("");
					throw new Exception(_resources.getString(WordPressResource.MESSAGE_BAD_USERNAME_PASSWORD));
				}
			} else if ( connResponse.getResponseObject() instanceof IOException) {
				//if IO exception occurred we should exit immediately 
				throw (Exception) connResponse.getResponseObject();
			*/} else {
				Exception currentError = (Exception) connResponse.getResponseObject();
				isError = true;
				if(currentError != null) {
					String errorMessage = currentError.getMessage();
					if(errorMessage != null && !errorMessage.trim().equals(""))
					wholeErrorMessage += errorTitle + " - " + errorMessage + "\n";
				}
			}
		} 
		
		connResponse.setError(false);
		connResponse.setStopped(false);
		connResponse.setResponse("");
		connResponse.setResponseObject(null);
	}
	
	/**
	 * refresh blog
	 * @param provider
	 */
	public void run() {
		try {
			
			connResponse = new BlogConnResponse();
			//the following calls uses the same connection 
			//These calls can modify the state of the connection to isError=true;
			getBlogCategories(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading categories");
			
			getPageStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Page Status");
			
			getPageTemplates(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Page Templates");
			
			getPostStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Post Status");
			
			getTagList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Tags");

			getCommentStatusList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Comment Status");
						
			Vector recentPostTitle = getRecentPostTitle(blog.getId(), blog.getMaxPostCount());
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setRecentPostTitles(recentPostTitle);
			checkConnectionResponse("Error while loading Recent Post");
			
			
			Vector blogPages = getPages(Integer.parseInt(blog.getId()), blog.getMaxPostCount());
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setPages(blogPages);
			checkConnectionResponse("Error while loading Pages");
		
			Vector comments = getComments(Integer.parseInt(blog.getId()), -1, "", 0, 100);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false ) {
				try{
					CommentsDAO.storeComments(blog, comments);
				} catch (IOException e) {
					Log.error(e, "Error while storing comments");
				} catch (RecordStoreException e) {
					Log.error(e, "Error while storing comments");
				} catch (Exception e) {
					Log.error(e, "Error while storing comments");
				} 
			}
			checkConnectionResponse("Error while loading comments");

			Hashtable options = getOptions(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setBlogOptions(options);
			checkConnectionResponse("Error while loading Blog options");
			
			if(blog.isWPCOMBlog()) {
				Hashtable features = getFeatures(blog);
				if(connResponse.isStopped()) return; //if the user has stopped the connection
				if(connResponse.isError() == false )
					blog.setWpcomFeatures(features);
				checkConnectionResponse("Error while loading Blog features");
			}
			
			BlogDAO.setBlogIco(blog, null);
			downloadIcoFile(); 	//downloading the blog ico file
			
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			//if there was an errors
			if(!isError) {
				connResponse.setResponseObject(blog);
			} else {
				throw new Exception(wholeErrorMessage);
			}
			
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "Error while loading blog:");
		}
		catch (Exception e) {
			setErrorMessage(e, "Error while loading blog:");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.trace("Blog Update Notify Error");
		}
	}
	
	private void downloadIcoFile() {
		Log.trace(">>> Retrieving Blog Shortcut image file");
		try {
			HTTPGetConn imageConnection = new HTTPGetConn(blog.getUrl(), "", "");
			if(blog.isHTTPBasicAuthRequired()) {
				imageConnection.setHttp401Password(blog.getHTTPAuthPassword());
				imageConnection.setHttp401Username(blog.getHTTPAuthUsername());
			}  
			Object responseImg = imageConnection.execute("", null); //starts connection 
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(responseImg == null) {
				Log.trace("no response while retriving the blog hml");
				return;
			}
			if((responseImg instanceof byte[]) == false){
				Log.trace("invalid response while retriving the blog hml");
				return;
			}

			String icoFullURL = null;

			byte[] response = (byte[])responseImg;
			Log.trace("RESPONSE received - " + new String(response));


			KXmlParser parser = new KXmlParser();
			parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true); //relaxed parser
			ByteArrayInputStream bais = new ByteArrayInputStream(response);
			parser.setInput(bais, "ISO-8859-1");

			while (parser.next() != XmlPullParser.END_DOCUMENT) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					String rel="";
					String href="";
					//link tag
					if(parser.getName()!=null && parser.getName().trim().equalsIgnoreCase("link")){
						//unfold all attribute
						for (int i = 0; i < parser.getAttributeCount(); i++) {
							String attrName = parser.getAttributeName(i);
							String attrValue = parser.getAttributeValue(i);
							if("rel".equals(attrName))
								rel = attrValue;
							else if("href".equals(attrName))
								href = attrValue;

						}
						if(rel.equals("apple-touch-icon")){
							icoFullURL = href;
						}

					}//end link tag
				}
			}				

			if(connResponse.isStopped()) return; //if the user has stopped the connection

			if(icoFullURL == null) {
				Log.trace("no icon url was Found");
				return;
			}

			String[] tokens = StringUtils.split(icoFullURL, "?");
			if(tokens.length < 2) {
				//not a valid url
				Log.trace("No valid icon url was Found");
				return;
			}
			String icoURL = tokens[0] + "?s=32&d=404";
			Log.trace("The icon url - " + icoURL);

			imageConnection = new HTTPGetConn(icoURL, "", "");
			if(blog.isHTTPBasicAuthRequired()) {
				imageConnection.setHttp401Password(blog.getHTTPAuthPassword());
				imageConnection.setHttp401Username(blog.getHTTPAuthUsername());
			}  
			responseImg = imageConnection.execute("", null); //starts connection without make another thread
			if(connResponse.isStopped()) return; //if the user has stopped the connection

			if(responseImg == null) {
				Log.trace("no response while retriving image file");
				return;
			}
			if((responseImg instanceof byte[]) == false){
				Log.trace("no valid image file found");
				return;
			} else {
				//Bitmap tmpBitmap = Bitmap.createBitmapFromBytes((byte[])responseImg, 0, -1, 1); //try to build an image immediately
				EncodedImage tmp_img = EncodedImage.createEncodedImage((byte[])responseImg, 0, -1);
				if(tmp_img.getHeight() > 32 || tmp_img.getWidth() > 32) {
					tmp_img = ImageUtils.resizeEncodedImage(tmp_img, 32, 32);
					Bitmap tmpBitmap = tmp_img.getBitmap();
					BlogDAO.setBlogIco(blog, JPEGEncodedImage.encode(tmpBitmap, 100).getData());	
				} else {
					BlogDAO.setBlogIco(blog, (byte[])responseImg);
				}
			}
			
		} catch (Exception e) {
			Log.error(e, "error while retrieving shorcut ico");
		} finally {
			Log.trace("<<< Retrieving Blog Shortcut image file");
		}
	}
		
	protected synchronized Hashtable getOptions(Blog blog) throws Exception {
		try {
			Log.debug(">>> reading Blog options for the blog : " + blog.getName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wp.getOptions", args);
			if (connResponse.isError()) {
				return null;
			}
			
			Hashtable optionsStructs = (Hashtable) response;
			/*
			Enumeration k = optionsStruct.keys();
			while (k.hasMoreElements()) {
				String key = (String) k.nextElement();
				Log.trace("==== " + key + " ==== ");
				Hashtable currentOption = (Hashtable) optionsStruct.get(key);
				Enumeration innerkeys = currentOption.keys();
				while (innerkeys.hasMoreElements()) {
					String innerkey = (String) innerkeys.nextElement();
					Log.trace("innerkey " + innerkey + "; ");
					Log.trace("innervalue " + String.valueOf( currentOption.get(innerkey) )+ "; "); 
				}
			}
			*/
			Log.debug("<<< End reading Blog options for the blog : "	+ blog.getName());
			return optionsStructs;
		} catch (ClassCastException cce) {
			throw new Exception ("Error while reading blog options");
		}
	}
	
	/* 
	 * The method call  wpcom.getFeatures  ( blog_id, username, password ) which returns a struct.
	 * Just a simple way to expose data on WPCOM specific features (VideoPress or space upgrade for istance).
	 * 
	 * Right now the only field in the struct is "videopress_enabled", with a boolean value.
	 * 
	 */
	protected synchronized Hashtable getFeatures(Blog blog) throws Exception {
		try {
			Log.debug(">>> reading WP.COM Blog Features : " + blog.getName());

			Vector args = new Vector(3);
			args.addElement(String.valueOf(blog.getId()));
			args.addElement(mUsername);
			args.addElement(mPassword);

			Object response = execute("wpcom.getFeatures", args);
			if (connResponse.isError()) {
				return null;
			}
			
			Hashtable features = (Hashtable) response;
			/*Enumeration elements = features.keys();
			for (; elements.hasMoreElements();) {
				String key = (String) elements.nextElement();
				Log.trace("key: " + key);
				Log.trace("value: " + features.get(key));
			}*/
			Log.debug("<<< reading WP.COM Blog Features : "	+ blog.getName());
			return features;
		} catch (ClassCastException cce) {
			throw new Exception ("Error while reading blog Features");
		}
	}
	
	
	//return the blogs associated with this connection
	public Blog getBlog() {
		return blog;
	}
}