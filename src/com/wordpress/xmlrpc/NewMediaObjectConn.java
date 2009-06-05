package com.wordpress.xmlrpc;

import java.util.Hashtable;
import java.util.Vector;

public class NewMediaObjectConn extends BlogConn  {

	private String fileName =null;	
	private byte[] fileContent=null; //base64 Encoding.
	private String blogID;
	

	public NewMediaObjectConn(String hint,	String userHint, String passwordHint, String blogID,
			String fileName) {
		super(hint, userHint, passwordHint);
		this.fileName=fileName;
		this.blogID=blogID;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	
	public void setFileContent(byte[] fileContent) {
		this.fileContent = fileContent;
	}
	
	
	/**
	 * 
	 * @param provider
	 */
	public void run() {
		try{
			//#debug
			System.out.println("grandezza del file = " + fileContent.length );
	
		   Hashtable content = new Hashtable(2);

	       content.put("name", this.fileName);
	       content.put("bits", fileContent);

	       Vector args = new Vector(4);
	       args.addElement(blogID);
	       args.addElement(mUsername);
	       args.addElement(mPassword);
	       args.addElement(content);

	        Object response = execute("metaWeblog.newMediaObject", args);
			if(connResponse.isError()) {
				//se il server xml-rpc è andato in err
				notifyObservers(connResponse);
				return;		
			}
			
			connResponse.setResponseObject(response);
		} catch (Exception cce) {
			setErrorMessage(cce, "uploadMedia error");
		}
		try {
			notifyObservers(connResponse);
		} catch (Exception e) {
			System.out.println("New Media Object Notify Error");
		}
	}
}