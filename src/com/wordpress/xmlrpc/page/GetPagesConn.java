package com.wordpress.xmlrpc.page;

import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Page;
import com.wordpress.xmlrpc.BlogConn;

public class GetPagesConn extends BlogConn  {
	
	private final int blogID;
	
	public GetPagesConn(String hint, int blogID, String userHint, String passwordHint, TimeZone tz) {
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
	}

	public void run() {
		 
		 if (this.blogID < 0) {
			 setErrorMessage("Page does not have a BlogId");
			 notifyObservers(connResponse);
	         return;
	        }
	        
	        Vector args = new Vector(3);
	        args.addElement(String.valueOf(this.blogID));
	        args.addElement(mUsername);
	        args.addElement(mPassword);

	        Object response = execute("wp.getPages", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
			try{
			Vector returnedPages = (Vector) response;
			Page[] myPages= new Page[returnedPages.size()];
			for (int i=0; i < returnedPages.size(); i++){
				Hashtable	returnPageData = (Hashtable) returnedPages.elementAt(i);
				 String title=((String)returnPageData.get("title"));
		         String description=((String)returnPageData.get("description"));
		         Date dateCreated=((Date)returnPageData.get("dateCreated"));
		         int pageID= Integer.parseInt( (String) returnPageData.get("page_id") );
		         
				 Page page = new Page(blogID, pageID, title, description, dateCreated);
				page.setLink((String)returnPageData.get("link"));
				page.setPermaLink((String)returnPageData.get("permaLink"));
				page.setUserID(Integer.parseInt((String)returnPageData.get("userid")));
				page.setPageStatus((String)returnPageData.get("page_status"));
				
				page.setCategories((Vector)returnPageData.get("categories"));
				
				page.setMt_excerpt((String)returnPageData.get("excerpt"));
				page.setMt_text_more((String)returnPageData.get("text_more"));
				Integer comments = (Integer) returnPageData.get("mt_allow_comments");
	            if (comments != null) {
	                page.setCommentsEnabled(comments.intValue() != 0);
	            }
	                Integer trackback = (Integer) returnPageData.get("mt_allow_pings");
	                if (trackback != null) {
	                    page.setPingsEnabled(trackback.intValue() != 0);
	                } 
	            page.setWpSlug((String)returnPageData.get("wp_slug"));
	            page.setWpPassword((String)returnPageData.get("wp_password"));
				page.setWpAuthor((String)returnPageData.get("wp_author"));
				page.setWpAuthorID(Integer.parseInt((String)returnPageData.get("wp_author_id")));

				page.setWpAuthorDisplayName((String)returnPageData.get("wp_author_display_name"));
				
				//page.setWp_page_parent_id(  ((Integer)returnPageData.get("wp_page_parent_id")).intValue());

				page.setWpPageParentTitle((String)returnPageData.get("wp_page_parent_title"));
				page.setWpPageOrder(Integer.parseInt((String)returnPageData.get("wp_page_order")));
				page.setWpPageTemplate((String)returnPageData.get("wp_page_template"));		
				page.setCustom_field((Vector)returnPageData.get("custom_fields")); //TODO handle CF 

				
				Vector cf=(Vector)returnPageData.get("custom_fields");
				System.out.println("stampa dei field custom");
				for (int j=0; j<cf.size(); j++){
					Hashtable tempData2 = (Hashtable) cf.elementAt(j);
						System.out.println("id: "+(String) tempData2.get("pageId"));
						System.out.println("key: "+(String) tempData2.get("apiKey"));
						System.out.println("value: "+(String) tempData2.get("value"));
					}
			}
							
			connResponse.setResponseObject(myPages);

		} catch (Exception cce) {
			setErrorMessage(cce, "Edit page error");
		}
		
		try {
			   notifyObservers(connResponse);
		  } catch (Exception e) {
		   System.out.println("notify error");
		  }
	}
}