package com.wordpress.xmlrpc.option;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.model.Option;
import com.wordpress.xmlrpc.BlogConn;

public class SetOptionsConn extends BlogConn  {


	private final int blogID;
	private final Option[] options;

	public SetOptionsConn(String hint, int blogID, String userHint, String passwordHint,  Option[] options){
		super(hint, userHint, passwordHint);
		this.blogID = blogID;
		this.options = options;
	}
	public void run() {
		
		Vector optionsVect= new Vector(options.length);

		for(int i=0; i< options.length; i++){
			Option currentOption=options[i];
			if (currentOption.getValue() == null || currentOption.getName()== null
					|| currentOption.getName().trim().equalsIgnoreCase("")) {
				
				Hashtable option = new Hashtable(2);
				option.put("value", currentOption.getValue());
				option.put("name", currentOption.getName());
				optionsVect.addElement(option);
			}
		}
			Vector args = new Vector(4);
			args.addElement(String.valueOf(this.blogID));
			args.addElement(mUsername);
			args.addElement(mPassword);
			args.addElement(optionsVect);

			Object response = execute("wp.setOptions", args);
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
				setErrorMessage(e, "SetOptions error: Invalid server response");
			} try {
				notifyObservers(connResponse);
			} catch (Exception e) {
				System.out.println("SetOptions error: Notify error"); 
			}
		
		}
	}

