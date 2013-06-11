package com.wordpress.controller;

import java.io.ByteArrayInputStream;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.Blog;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.StatsView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.HTTPGetConn;

/**
 * In order to make requests for WPCOM stats for a given blog you need some specific information. 
 * In order to get this additional info a new URL was created:

https://public-api.wordpress.com/get-user-blogs/1.0/
http://stats.wordpress.com/api/1.0/

It requires SSL, uses HTTP BASIC AUTHENTICATION and returns XML data
that looks like:

<?xml version="1.0" encoding="UTF-8"?>
<userinfo>
       <apikey>XXXXXXXXXXX</apikey>
       <blog>
               <id>XYXYXY</id>
               <url>http://example.wordpress.com/</url>
       </blog>
       <blog>
               <id>XYXYXY</id>
               <url>http://myblog.example.com/</url>
       </blog>
</userinfo>

Optionally you can get the data in JSON format -
https://public-api.wordpress.com/getuserblogs.php?f=json
 */
public class StatsController extends BaseController {
	
	private StatsView view = null;
	
	ConnectionInProgressView connectionProgressView = null;
	private Blog currentBlog = null;
	private String apiKey = null;
	private String blogStatID = null;
	
	private int type = TYPE_VIEW;
	private int interval = INTERVAL_7DAYS;                     //last interval used to retrieve stats  
	private int intervalForTypeView = INTERVAL_TYPE_VIEW_DAYS; //last interval used to retrieve stats of type view
	private byte[] lastStatsData = new byte[0];
	
	public static final int TYPE_VIEW = 0;
	public static final int TYPE_TOP = 1;
	public static final int TYPE_REFERRERS = 2;
	public static final int TYPE_SEARCH = 3;
	public static final int TYPE_CLICKS = 4;
	public static final int TYPE_VIDEO = 5;
	
	//constants used to group stats when stats type != view
	public static final int INTERVAL_7DAYS = 7;
	public static final int INTERVAL_30DAYS = 30;
	public static final int INTERVAL_QUARTER = 90;
	public static final int INTERVAL_YEAR = 365;
	public static final int INTERVAL_ALL = -1;

	//constants used when the stats type = view
	public static final int INTERVAL_TYPE_VIEW_DAYS = 300;
	public static final int INTERVAL_TYPE_VIEW_WEEKS = 301;
	public static final int INTERVAL_TYPE_VIEW_MONTHS = 302;
	
	
	public StatsController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
		view = new StatsView(this);
	}

	public void showView() {
	    retriveStatsAuthData();
	}

	public void refreshView() {
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public void setIntervalForTypeView(int interval) {
		this.intervalForTypeView = interval;
	}
	
	public int getIntervalForTypeView() {
		return intervalForTypeView;
	}
	
	public int getInterval() {
		return interval;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type ;
	}
	
	private String decodeStatTable() {
		String retType = "view";
		switch (this.type) {
		case TYPE_VIEW:
			retType = "view";
			break;
		case TYPE_CLICKS:
			retType =  "clicks";
			break;
		case TYPE_REFERRERS:
			retType = "referrers";
			break;
		case TYPE_SEARCH:
			retType = "searchterms";
			break;
		case TYPE_TOP:
			retType = "postviews";
			break;
		case TYPE_VIDEO:
			retType = "videoplays";
			break;
		default:
			break;
		}
		return retType;
	}
	
	
	public void retrieveStats() {
	    try {
	    	if (blogStatID != null) {
	    		String url = WordPressInfo.STATS_ENDPOINT_URL +"?api_key="+apiKey+"&blog_id="+blogStatID
	    		+"&table="+decodeStatTable();
	    		
	    		if(this.type != TYPE_VIEW) {
	    			url+="&days="+interval+"&summarize";
	    		} else {
	    			//&period=week&days=12 for the last quarter in weeks
	    			switch (getIntervalForTypeView()) {
	    			case StatsController.INTERVAL_TYPE_VIEW_DAYS:
	    				url+="&days="+INTERVAL_30DAYS;
	    				break;
	    			case StatsController.INTERVAL_TYPE_VIEW_WEEKS:
	    				url+="&period=week&days=30";
	    				break;
	    			case StatsController.INTERVAL_TYPE_VIEW_MONTHS:
	    				url+="&period=month&days=31";
	    				break;
	    			default:
	    				break;
	    			}
	    		}
	    		
				final HTTPGetConn connection = new HTTPGetConn(url, currentBlog.getStatsUsername(), 
						currentBlog.getStatsPassword());
				
		        connection.addObserver(new GetStatsDataCallBack(connection));  
		        connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_INPROGRESS));	       
		        connection.startConnWork(); //starts connection
						
				int choice = connectionProgressView.doModal();
				if(choice==Dialog.CANCEL) {
					connection.stopConnWork(); //stop the connection if the user click on cancel button
				}	
			}
	    	
		} catch (Exception e) {
	    	displayError(e, "Error while retriving Stats");
		}
	}
	
	private class GetStatsDataCallBack implements Observer {

		private final HTTPGetConn connection;

		public GetStatsDataCallBack(final HTTPGetConn connection) {
			this.connection = connection;
		}

		public void update(Observable observable, final Object object) {

			dismissDialog(connectionProgressView);
			BlogConnResponse resp = (BlogConnResponse) object;

			if(resp.isStopped()){
				return;
			}

			if(!resp.isError()) {						
				try {
					byte[] response = (byte[]) resp.getResponseObject();
					if(response != null ) {
						if(Log.getDefaultLogLevel() >= Log.TRACE)
							Log.trace("RESPONSE - " + new String(response));
						lastStatsData = response;
					} else {
						Log.trace("STATS RESPONSE IS EMPTY");
						lastStatsData = new byte[0];
					}
					UiApplication.getUiApplication().invokeLater(new Runnable() {
						public void run() {
							view.setStatsData(lastStatsData);
						}
					});
				} catch (Exception e) {
					displayError("Error while retriving stats data: "+e.getMessage());
					return;
				}						

			} else {
				final String respMessage = resp.getResponse();
				displayError(respMessage);	
			}
		}
	} 

	
	private void  parseStatsAuthResponse(byte[] response) {
		//parse the html and get the attribute for xmlrpc endpoint
		if(response != null) {
			try {				
				KXmlParser parser = new KXmlParser();
				parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true); //relaxed parser
				ByteArrayInputStream bais = new ByteArrayInputStream(response);
				parser.setInput(bais, "ISO-8859-1");
				String blogUrl = currentBlog.getUrl();
				blogUrl = blogUrl.toLowerCase();
				if(!blogUrl.endsWith("/")) 
					blogUrl+="/";
				blogUrl = StringUtils.replaceAll(blogUrl, "https://", "");
				blogUrl = StringUtils.replaceAll(blogUrl, "http://", "");
				if( blogUrl.startsWith("www.")) {
					blogUrl = blogUrl.substring(4);
				}
				
				String currentBlogID = currentBlog.getBlogIDForStats(); 
				
				Log.trace("current blog url " + blogUrl);
				while (parser.next() != XmlPullParser.END_DOCUMENT) {
					if (parser.getEventType() == XmlPullParser.START_TAG) {
						
						if(parser.getName()!=null && parser.getName().trim().equalsIgnoreCase("apikey")){
							
							int nextEvent = parser.next(); 
							if( nextEvent == XmlPullParser.TEXT ) {
								String result = parser.getText();
								Log.trace("apikey parser - " + result) ;
								apiKey = result;
							}
							
							parser.nextTag();
							parser.require( XmlPullParser.END_TAG, "", "apikey" );
						} else
						
						if(parser.getName()!=null && parser.getName().trim().equalsIgnoreCase("blog")){
							String id = null;
							String url = null;
							
							parser.nextTag();
							parser.require(XmlPullParser.START_TAG, null, "id");
							int nextEvent = parser.next(); 
							if( nextEvent == XmlPullParser.TEXT ) {
								id = parser.getText();
								Log.trace("id parser - " + id) ;
							}
							parser.nextTag();
							parser.require( XmlPullParser.END_TAG, "", "id" );
							
							parser.nextTag();
							parser.require(XmlPullParser.START_TAG, null, "url");
							nextEvent = parser.next(); 
							if( nextEvent == XmlPullParser.TEXT ) {
								url = parser.getText();
								Log.trace("url parser - " + url) ;
							}
							parser.nextTag();
							parser.require( XmlPullParser.END_TAG, "", "url" );
							
							
							parser.nextTag();
							parser.require(XmlPullParser.END_TAG, null, "blog" );
														
							if(url != null) {
								//trying to match the url
								url = url.toLowerCase();
								if(!url.endsWith("/")) url+="/";
								url = StringUtils.replaceAll(url, "https://", "");
								url = StringUtils.replaceAll(url, "http://", "");
								if( url.startsWith("www.")) {
									url = url.substring(4);
								}
								if (blogUrl.equals(url)) {
									blogStatID = id;
									//return; DO NOT STOP THE LOOP HERE, We need to match the latest blog...Ref: http://blackberry.trac.wordpress.org/ticket/248
								}
							}
						}//end 
					}
				}				
			} catch (Exception ex) {
				Log.error("parseStatsAuthResponse - parsing response document error");
				Log.error(ex.getMessage());
			}
		}
	}

	private void retriveStatsAuthData() {
		try {
			if (currentBlog != null) {

				final HTTPGetConn connection;

				connection = new HTTPGetConn(WordPressInfo.STATS_AUTH_ENDPOINT_URL, 
						currentBlog.getUsername(), 
						currentBlog.getPassword());
				//the blog has an http401 pass 
				if(currentBlog.getStatsPassword() != null || currentBlog.getStatsUsername() != null) {
					connection.setHttp401Password(currentBlog.getStatsPassword());
					connection.setHttp401Username(currentBlog.getStatsUsername());
				}

				connection.setAuthMessage(_resources.getString(WordPressResource.MESSAGE_STATS_AUTH_REQUIRED));
				connection.addObserver(new GetStatsAuthDataCallBack(connection));  
				connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_INPROGRESS));	       
				connection.startConnWork(); //starts connection

				int choice = connectionProgressView.doModal();
				if(choice==Dialog.CANCEL) {
					connection.stopConnWork(); //stop the connection if the user click on cancel button
				}	
			}

		} catch (Exception e) {
			displayError(e, "Error while retriving Stats");
		}
	}
	
	private void storeStatsAuthPassword(String user, String pass) {
		//check if http 401 credential are changed
		if(!user.equals(currentBlog.getStatsUsername())
			|| !pass.equals(currentBlog.getStatsPassword()) ) {
			Log.trace("Http Auth data changed");
			currentBlog.setStatsPassword(pass);
			currentBlog.setStatsUsername(user);
			
			try {
				BlogDAO.updateBlog(currentBlog);
			} catch (Exception e) {
				Log.error(e, "Error while updating blog with Auth data");
			}
			
		}
	}
	
	private class GetStatsAuthDataCallBack implements Observer {
		
		private final HTTPGetConn connection;

		public GetStatsAuthDataCallBack(final HTTPGetConn connection) {
			this.connection = connection;
		}

		public void update(Observable observable, final Object object) {

			dismissDialog(connectionProgressView);
			BlogConnResponse resp = (BlogConnResponse) object;

			if(resp.isStopped()){
				return;
			}

			if(!resp.isError()) {

				try {
					byte[] response = (byte[]) resp.getResponseObject();
					if(response != null ) {
						Log.trace("RESPONSE - " + new String(response));
						storeStatsAuthPassword(connection.getHttp401Username(), connection.getHttp401Password());
						parseStatsAuthResponse(response);
						if(blogStatID != null && apiKey != null) {
							UiApplication.getUiApplication().invokeLater(new Runnable() {
								public void run() {
									UiApplication.getUiApplication().pushScreen(view);
									retrieveStats();
								}
							});
						} else {
							//No stats ID for your blog found in the server response
							displayMessage(_resources.getString(WordPressResource.MESSAGE_CANNOT_FIND_STATS));
						}
					} else {
						Log.trace("HTTP RESPONSE IS EMPTY FROM STATS ENDPOINT or user has cancelled the operation");
					}
				} catch (Exception e) {
					Log.error(e,"Error while parsing stats data");
					displayError("Error while parsing stats data");
					return;
				}						

			} else {
				Log.error("Error while retriving stats data");
				final String respMessage = resp.getResponse();
				displayError(respMessage);	
			}
		}
	} 
}