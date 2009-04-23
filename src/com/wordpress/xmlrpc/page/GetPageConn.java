package com.wordpress.xmlrpc.page;

import java.util.Date;
import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Page;
import com.wordpress.xmlrpc.BlogConn;

public class GetPageConn extends BlogConn {

	private final int pageID;
	private final int blogID;

	public GetPageConn(String hint, int blogID, String userHint,
			String passwordHint, TimeZone tz, int pageID) {
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
		this.pageID = pageID;
	}

	public void run() {
		if (this.pageID < 0) {
			setErrorMessage("Page does not have an Id");
			notifyObservers(connResponse);
			return;
		}
		if (this.blogID < 0) {
			setErrorMessage("Page does not have a BlogId");
			notifyObservers(connResponse);
			return;
		}

		Vector args = new Vector(4);
		args.addElement(String.valueOf(this.pageID));
		args.addElement(String.valueOf(this.blogID));
		args.addElement(mUsername);
		args.addElement(mPassword);

		Object response = execute("wp.getPage", args);
		if (connResponse.isError()) {
			notifyObservers(connResponse);
			return;
		}

		try {
			Hashtable returnPageData = (Hashtable) response;

			String title = ((String) returnPageData.get("title"));
			String description = ((String) returnPageData.get("description"));
			Date dateCreated = ((Date) returnPageData.get("dateCreated"));
			// Vector custom=(Vector) (returnPageData.get("custom"));

			Page page = new Page(blogID, pageID, title, description,
					dateCreated);

			page.setLink((String) returnPageData.get("link"));
			page.setPermaLink((String) returnPageData.get("permaLink"));
			page.setPageStatus((String) returnPageData.get("pageStatus"));
		//	page.setCategories((String) returnPageData.get("categories"));
			page.setMt_excerpt((String) returnPageData.get("excerpt"));
			page.setMt_text_more((String) returnPageData.get("text_more"));
			page.setPermaLink((String) returnPageData.get("permaLink"));

			Integer comments = (Integer) returnPageData.get("mt_allow_comments");
			if (comments != null) {
				page.setCommentsEnabled(comments.intValue() != 0);
			}
			Integer trackback = (Integer) returnPageData.get("mt_allow_pings");
			if (trackback != null) {
				page.setPingsEnabled(trackback.intValue() != 0);
			}
			page.setWpSlug((String) returnPageData.get("wp_slug"));
			page.setWpPassword((String) returnPageData.get("wp_password"));
			page.setWpAuthorID(Integer.parseInt((String) returnPageData.get("wp_author_id")));
			page.setWpPageParentID(Integer.parseInt((String) returnPageData.get("wp_parent_id")));
			page.setWpPageParentTitle((String) returnPageData.get("wp_page_parent_title"));
			page.setWpPageOrder(Integer.parseInt((String) returnPageData.get("wp_page_order")));
			page.setWpAuthorID(Integer.parseInt((String) returnPageData.get("wp_author_id")));
			page.setWpAuthorDisplayName((String) returnPageData.get("wp_author_display_name"));
			page.setCustom_field((Vector) returnPageData.get("custom_fields"));
			// TODO custom???
			page.setWpPageTemplate((String) returnPageData.get("wpPageTemplate"));


			connResponse.setResponseObject(page);
			notifyObservers(connResponse);

		} catch (Exception cce) {
			setErrorMessage(cce, "Edit page error");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("notify error"); // TODO handle errors here...
		}
	}
}