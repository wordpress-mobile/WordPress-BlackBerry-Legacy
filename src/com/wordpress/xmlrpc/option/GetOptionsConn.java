package com.wordpress.xmlrpc.option;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

import com.wordpress.model.Option;
import com.wordpress.xmlrpc.BlogConn;

public class GetOptionsConn extends BlogConn  {


	private final int blogID;
	private Vector options; //vector of strings

	public GetOptionsConn(String hint, int blogID, String userHint, String passwordHint,  TimeZone tz, Vector options){
		super(hint, userHint, passwordHint, tz);
		this.blogID = blogID;
		this.options = options;
	}
	public void run() {
			
			Vector args = new Vector(4);
			args.addElement(String.valueOf(this.blogID));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(options);

			Object response = execute("wp.getOptions", args);
			if(connResponse.isError()) {
				notifyObservers(connResponse);
				return;		
			}
			
			try{
				Vector optionList = (Vector) response;
				Option[] myOption= new Option[optionList.size()];

				for(int i=0; i<optionList.size(); i++){
					Hashtable	returnedOptionData = (Hashtable) optionList.elementAt(i);
					String option = ( (String) returnedOptionData.get("option") );
					String value = ((String) returnedOptionData.get("value"));
					Option optionVal = new Option(option, value);
					myOption[i]=optionVal;
				}
			connResponse.setResponseObject(myOption);

			} catch (Exception e) {
				setErrorMessage(e, "GetOptions error: Invalid server response");
			} try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("GetOptions error: Notify error"); 
			}
		
		}
	}

