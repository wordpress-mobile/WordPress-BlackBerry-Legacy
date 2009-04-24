package com.wordpress.xmlrpc.page;

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
				int pageID= Integer.parseInt( (String) returnPageData.get("page_id") );
				Page page = GetPageConn.getPage(returnPageData, blogID, pageID);
				myPages[i]=page;
			}

			connResponse.setResponseObject(myPages);

		} catch (Exception cce) {
			setErrorMessage(cce, "GetPages error: Invalid server response");
		}

		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("GetPages error: Notify error"); 
		}
	}
}