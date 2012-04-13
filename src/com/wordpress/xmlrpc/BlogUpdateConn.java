package com.wordpress.xmlrpc;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;

import javax.microedition.rms.RecordStoreException;

import org.kxml2.io.KXmlParser;
import org.kxmlrpc.XmlRpcException;
import org.xmlpull.v1.XmlPullParser;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.controller.MainController;
import com.wordpress.io.BlogDAO;
import com.wordpress.io.CommentsDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.MD5;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.MainView;
import com.wordpress.view.component.BasicListFieldCallBack;

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
					if(errorMessage != null && !errorMessage.trim().equals("")) {
						if ( ! wholeErrorMessage.trim().equalsIgnoreCase("") )
							wholeErrorMessage += "\n\n" + errorTitle + " - " + errorMessage;
						else
							wholeErrorMessage = errorTitle + " - " + errorMessage;
					}
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
			
			Vector basicArgs = new Vector(3);
			basicArgs.addElement(String.valueOf(blog.getId()));
			basicArgs.addElement(mUsername);
			basicArgs.addElement(mPassword);
			
			connResponse = new BlogConnResponse();
			//the following calls uses the same connection 
			//These calls can modify the state of the connection to isError=true;
			
			getBlogCategories(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading categories");
			
			//retrive the blog "page status list"
			Hashtable pageStatusList = (Hashtable) getXmlRpcWithParameters("wp.getPageStatusList", basicArgs);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setPageStatusList(pageStatusList);
			checkConnectionResponse("Error while loading Page Status");
						
			//retrieve the blog "page template list"
			Hashtable pageTemplate = (Hashtable) getXmlRpcWithParameters("wp.getPageTemplates", basicArgs);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setPageTemplates(pageTemplate);
			checkConnectionResponse("Error while loading Page Templates");
			
			//retrive the blog "post status list"
			Hashtable statusList = (Hashtable) getXmlRpcWithParameters("wp.getPostStatusList", basicArgs);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setPostStatusList(statusList);
			checkConnectionResponse("Error while loading Post Status");
			
			getTagList(blog);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			checkConnectionResponse("Error while loading Tags");

			Hashtable commentStatusList = (Hashtable) getXmlRpcWithParameters("wp.getCommentStatusList", basicArgs);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setCommentStatusList(commentStatusList);
			checkConnectionResponse("Error while loading Comment Status");
						
			Vector recentPostTitle = getRecentPostTitle(blog.getId(), WordPressInfo.DEFAULT_ITEMS_NUMBER);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setRecentPostTitles(recentPostTitle);
			checkConnectionResponse("Error while loading Recent Post");
			
			
			Vector blogPages = getPages(blog.getId(), WordPressInfo.DEFAULT_ITEMS_NUMBER);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setPages(blogPages);
			checkConnectionResponse("Error while loading Pages");
		
			Vector comments = getComments(blog.getId(), null, null, 0, 100);
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

			Hashtable options = (Hashtable) getXmlRpcWithParameters("wp.getOptions", basicArgs);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false )
				blog.setBlogOptions(options);
			checkConnectionResponse("Error while loading Blog options");
			
			//wpcom.getPostFormats  ( blog_id, username, password ) which returns a struct.
			//http://core.trac.wordpress.org/ticket/15405
			//http://core.trac.wordpress.org/ticket/17094
			Hashtable postFormatRequestParameters = new Hashtable();
			postFormatRequestParameters.put("show-supported", TRUE);
			Vector additionalParameter = new Vector();
			for (int i = 0; i < basicArgs.size(); i++) {
				additionalParameter.addElement(basicArgs.elementAt(i));
			}			
			additionalParameter.addElement(postFormatRequestParameters);
			Hashtable postFormats = (Hashtable) getXmlRpcWithParameters("wp.getPostFormats", additionalParameter);
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(connResponse.isError() == false ) {
				
				if(postFormats != null && postFormats.containsKey("all") 
						&&  postFormats.containsKey("supported")) {
					Vector supportedFormats = (Vector) postFormats.get("supported");
					Hashtable allFormats = (Hashtable) postFormats.get("all");
					Hashtable mergedFormats = new Hashtable();
					//adding the standard Post Formats
					mergedFormats.put("standard", allFormats.get("standard"));

					for (int i = 0; i < supportedFormats.size(); i++) {
						String key = String.valueOf(supportedFormats.elementAt(i));
						String value = (String) allFormats.get(key);
						mergedFormats.put(key, value);
					}	
					blog.setPostFormats(mergedFormats);
				} else {
					blog.setPostFormats(postFormats);
				}
			}
			additionalParameter = null;
			postFormatRequestParameters = null;
			checkConnectionResponse("Error while loading PostFormats");

			if(blog.isWPCOMBlog()) {
				/* 
				 * The method call  wpcom.getFeatures  ( blog_id, username, password ) which returns a struct.
				 * Just a simple way to expose data on WPCOM specific features (VideoPress or space upgrade for instance).
				 * 
				 * Right now the only field in the struct is "videopress_enabled", with a boolean value.
				 * 
				 */
				Hashtable features = (Hashtable) getXmlRpcWithParameters("wpcom.getFeatures", basicArgs);
				if(connResponse.isStopped()) return; //if the user has stopped the connection
				if(connResponse.isError() == false )
					blog.setWpcomFeatures(features);
				checkConnectionResponse("Error while loading Blog features");
			}
			
			BlogDAO.setBlogIco(blog, null);
			byte[] iconData = downloadBlavatar(blog.getXmlRpcUrl());
		/*	if(iconData == null) {
				iconData = downloadAppleTouchIco();
			}*/
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(iconData == null) {
				if(blog.isWPCOMBlog()) {
					iconData = downloadBlavatar("wp4bb.mail.used.for.gravatar1@gmail.com");
				} else {
					iconData = downloadBlavatar("wp4bb.mail.used.for.gravatar2@gmail.com");
				}
			}
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			if(iconData != null) {
				BlogDAO.setBlogIco(blog, iconData);
			}
			
			if(connResponse.isStopped()) return; //if the user has stopped the connection
			//if there was an errors
			if(!isError) {
				connResponse.setResponseObject(blog);
			} else {
				throw new Exception(wholeErrorMessage);
			}
			
		} catch (ClassCastException cce) {
			setErrorMessage(cce, "Error while loading blog");
		}
		catch (Exception e) {
			setErrorMessage(e, "Error while loading blog");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			Log.trace("Blog Update Notify Error");
		}
	}
	
	private byte[] downloadBlavatar(String emailOrSiteAddress) {
		Log.trace(">>> Retrieving blavatar");
		try {
			HTTPGetConn imageConnection;
			Object responseImg;
			String hashAuthorEmail = "";
			
			try {
			String cleanedBlogURL = StringUtils.replaceAll(emailOrSiteAddress, "http://", "");
			cleanedBlogURL = StringUtils.replaceAll(cleanedBlogURL, "https://", "");
			cleanedBlogURL = StringUtils.split(cleanedBlogURL, "/")[0];			
			MD5 md5 = new MD5();
				md5.Update(cleanedBlogURL, null);
				hashAuthorEmail = md5.asHex();
				md5.Final();
			} catch (UnsupportedEncodingException e) {
				Log.error(e, "Error while hashing URL for gravatar services");
			}
			
			if(emailOrSiteAddress.indexOf('@') != -1) //email 
				imageConnection = new HTTPGetConn("http://gravatar.com/avatar/"+hashAuthorEmail+"?s="+MainView.getBlogIconSize()+"&d=404", "", "");
			else
				imageConnection = new HTTPGetConn("http://gravatar.com/blavatar/"+hashAuthorEmail+"?s="+MainView.getBlogIconSize()+"&d=404", "", ""); 
			responseImg = imageConnection.execute("", null); //starts connection without make another thread
			if(connResponse.isStopped()) return null; //if the user has stopped the connection

			if(responseImg == null) {
				Log.trace("no response while retriving blavatar");
				return null;
			}
			if((responseImg instanceof byte[]) == false){
				Log.trace("no valid blavatar image file found");
				return null;
			} else {
				return (byte[])responseImg;				
			}
			
		} catch (Exception e) {
			Log.error(e, "error while retrieving blavatar");
		} finally {
			Log.trace("<<< Retrieving blavatar");
		}
		return null;
	} 
	
	private byte[] downloadAppleTouchIco() {
		Log.trace(">>> Retrieving AppleTouchIco");
		try {
			
			HTTPGetConn imageConnection = new HTTPGetConn(blog.getUrl(), "", "");
			if(blog.isHTTPBasicAuthRequired()) {
				imageConnection.setHttp401Password(blog.getHTTPAuthPassword());
				imageConnection.setHttp401Username(blog.getHTTPAuthUsername());
			}  
			Object responseImg = imageConnection.execute("", null); //starts connection 
			if(connResponse.isStopped()) return null; //if the user has stopped the connection
			if(responseImg == null) {
				Log.trace("no response while retriving the blog hml");
				return null;
			}
			if((responseImg instanceof byte[]) == false){
				Log.trace("invalid response while retriving the blog hml");
				return null;
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

			if(connResponse.isStopped()) return null; //if the user has stopped the connection

			if(icoFullURL == null) {
				Log.trace("no icon url was Found");
				return null;
			}

			String[] tokens = StringUtils.split(icoFullURL, "?");
			if(tokens.length < 2) {
				//not a valid url
				Log.trace("No valid icon url was Found");
				return null;
			}
			String icoURL = tokens[0] + "?s="+BasicListFieldCallBack.getImageHeightForDoubleLineRow()+"&d=404";
			Log.trace("The icon url - " + icoURL);

			imageConnection = new HTTPGetConn(icoURL, "", "");
			if(blog.isHTTPBasicAuthRequired()) {
				imageConnection.setHttp401Password(blog.getHTTPAuthPassword());
				imageConnection.setHttp401Username(blog.getHTTPAuthUsername());
			}  
			responseImg = imageConnection.execute("", null); //starts connection without make another thread
			if(connResponse.isStopped()) return null; //if the user has stopped the connection

			if(responseImg == null) {
				Log.trace("no response while retriving image file");
				return null;
			}
			if((responseImg instanceof byte[]) == false){
				Log.trace("no valid image file found");
				return null;
			} else {
				//Bitmap tmpBitmap = Bitmap.createBitmapFromBytes((byte[])responseImg, 0, -1, 1); //try to build an image immediately
/*				EncodedImage tmp_img = EncodedImage.createEncodedImage((byte[])responseImg, 0, -1);
				if(tmp_img.getHeight() > 32 || tmp_img.getWidth() > 32) {
					tmp_img = ImageUtils.resizeEncodedImage(tmp_img, 32, 32);
					Bitmap tmpBitmap = tmp_img.getBitmap();
					BlogDAO.setBlogIco(blog, JPEGEncodedImage.encode(tmpBitmap, 100).getData());
					*/	
				return (byte[])responseImg;
			}
			
		} catch (Exception e) {
			Log.error(e, "error while retrieving shorcut ico");
		} finally {
			Log.trace("<<< Retrieving Blog Shortcut image file");
		}
		
		return null;
	}
		
	protected synchronized Object getXmlRpcWithParameters(String methodName, Vector args) throws Exception {
		try {
			Log.debug(">>> reading "+methodName+" on the blog : " + blog.getName());
			Object response = execute(methodName, args);
			if (connResponse.isError()) {
				return null;
			}			
			Log.debug("<<< End reading "+methodName+" on the blog : "	+ blog.getName());
			return response;			
		} catch (ClassCastException cce) {
			throw new Exception ("Error while reading "+methodName+" for the blog : "	+ blog.getName());
		}
	}
	
	
	//return the blogs associated with this connection
	public Blog getBlog() {
		return blog;
	}
}