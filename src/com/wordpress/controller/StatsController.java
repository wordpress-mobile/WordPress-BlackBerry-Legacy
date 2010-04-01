package com.wordpress.controller;

import java.io.ByteArrayInputStream;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
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

https://public-api.wordpress.com/getuserblogs.php
http://stats.wordpress.com/csv.php

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
	private String wpDotComUsername = null;
	private String wpDotComPassword = null;
	private String apiKey = null;
	private String blogStatID = null;
	
	private String lastStatsData ="";
	private int interval = INTERVAL_7DAYS; 
	private int type = TYPE_VIEW;
	
	public static final int TYPE_VIEW = 0;
	public static final int TYPE_TOP = 1;
	public static final int TYPE_REFERRERS = 2;
	public static final int TYPE_SEARCH = 3;
	public static final int TYPE_CLICKS = 4;
	
	public static final int INTERVAL_7DAYS = 7;
	public static final int INTERVAL_30DAYS = 30;
	public static final int INTERVAL_QUARTER = 90;
	public static final int INTERVAL_YEAR = 365;
	public static final int INTERVAL_ALL = -1;


	public StatsController(Blog currentBlog) {
		super();	
		this.currentBlog=currentBlog;
		view = new StatsView(this);
	}

	public void showView() {
	    getStatsAuthData();
	}

	public void refreshView() {
	}


	public String getLastStatsData() {
		return lastStatsData;
	}
	
	public void setInterval(int interval) {
		this.interval = interval;
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
		default:
			break;
		}
		return retType;
	}
	
	public void retriveStats() {
	    try {
	    	if (currentBlog != null) {
	    		String url = WordPressInfo.STATS_ENDPOINT_URL +"?api_key="+apiKey+"&blog_id="+blogStatID
	    		+"&table="+decodeStatTable()+"&days="+interval;//+"&summarize";
	    		
				final HTTPGetConn connection = new HTTPGetConn(url, wpDotComUsername, wpDotComPassword);
				
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
							String html = null;
							html = (String)resp.getResponseObject();
							if(html != null ) {
								Log.trace("RESPONSE - " + html);
								lastStatsData = html;
							} else {
								Log.trace("EMPTY RESPONSE");
								lastStatsData = "EMPTY RESPONSE";
							}
							UiApplication.getUiApplication().invokeLater(new Runnable() {
								public void run() {
									view.setStatsData();
								}
							});
						} catch (Exception e) {
							Log.error(e,"Errore durante il reperimento stats");
							return;
						}						
											
					} else {
						Log.error("Errore durante il reperimento stats");
						final String respMessage = resp.getResponse();
					 	displayError(respMessage);	
					}
		}
	} 
			
	
	private void  parseStatsAuthResponse(String xmlResponse) {
		//parse the html and get the attribute for xmlrpc endpoint
		if(xmlResponse != null) {
			try {				
				KXmlParser parser = new KXmlParser();
				parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true); //relaxed parser
				ByteArrayInputStream bais = new ByteArrayInputStream(xmlResponse.getBytes());
				parser.setInput(bais, "ISO-8859-1");
				
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
								String blogUrl = currentBlog.getUrl();
								if(!blogUrl.endsWith("/")) blogUrl+="/";
								Log.trace("current blog url " + blogUrl);
								if(!url.endsWith("/")) url+="/";
								if (currentBlog.getUrl().equalsIgnoreCase(url)) {
									blogStatID = id;
									return;
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
	
	


	private void getStatsAuthData() {
		try {
	    	if (currentBlog != null) {
				final HTTPGetConn connection = new HTTPGetConn(WordPressInfo.STATS_AUTH_ENDPOINT_URL, currentBlog.getUsername(), currentBlog.getPassword());
				
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
							String html = null;
							html = (String)resp.getResponseObject();
							if(html != null ) {
								Log.trace("RESPONSE - " + html);
								wpDotComPassword = connection.getWpDotComPassword();
								wpDotComUsername = connection.getWpDotComUsername();	
								parseStatsAuthResponse(html);
								if(blogStatID != null && apiKey != null) {
									UiApplication.getUiApplication().invokeLater(new Runnable() {
										public void run() {
											UiApplication.getUiApplication().pushScreen(view);
											retriveStats();
										}
									});
								} else {
									//No stats for you blog found
									displayMessage(_resources.getString(WordPressResource.MESSAGE_CANNOT_FIND_STATS));
								}
							} else {
								Log.trace("HTTP RESPONSE IS EMPTY FROM STATS ENDPOINT");
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