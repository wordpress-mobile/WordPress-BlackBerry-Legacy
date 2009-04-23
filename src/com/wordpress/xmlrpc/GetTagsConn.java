package com.wordpress.xmlrpc;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Tag;

public class GetTagsConn extends BlogConn  {
	
	
	private final int blogID;

	public GetTagsConn(String hint, int blogID, String userHint, String passwordHint,  TimeZone tz){
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
		
		}
	
	public void run() {
				if (blogID < 0) {
					setErrorMessage("Error BlogId");
					notifyObservers(connResponse);
					return;
			}
			
			Vector args = new Vector(3);
	        args.addElement(String.valueOf(blogID));
	        args.addElement(mUsername);
	        args.addElement(mPassword);
		
	        Object response = execute("wp.getTags", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
			
			try {
				Vector tags = (Vector) response;
				
				Tag[] mytags= new Tag[tags.size()];
				
				Hashtable tagData = null;
				for (int i=0; i<tags.size(); i++){
					tagData = (Hashtable) tags.elementAt(i);

					System.out.println("tag_id: "+ (Integer.parseInt((String)tagData.get("tag_id"))));
					System.out.println("name: "+(String) tagData.get("name"));
					System.out.println("count: "+(Integer.parseInt((String) tagData.get("count"))));
					System.out.println("slug: "+(String) tagData.get("slug"));
					System.out.println("html_url: "+(String) tagData.get("html_url"));
					System.out.println("rss_url: "+(String) tagData.get("rss_url"));
					
					int tagId=Integer.parseInt((String)tagData.get("tag_id"));
					String tagName=(String) tagData.get("name");
					int count=Integer.parseInt((String) tagData.get("count"));
					String slug=(String) tagData.get("slug");
					String htmlUrl=(String) tagData.get("html_url");
					String rssUrl= (String) tagData.get("rss_url");
					
					Tag myTag= new Tag(blogID,tagId,tagName,count,slug, htmlUrl, rssUrl);
					mytags[i]=myTag;
				}
			
				connResponse.setResponseObject(mytags);
			}	catch (Exception e) {
				setErrorMessage(e, "Invalid server response");
			}
			
			try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("notify error"); //TODO handle errors here...
			}
			
	}
}