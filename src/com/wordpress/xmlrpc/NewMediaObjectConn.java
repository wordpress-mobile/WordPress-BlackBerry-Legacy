package com.wordpress.xmlrpc;

import java.util.Hashtable;
import java.util.TimeZone;
import java.util.Vector;

public class NewMediaObjectConn extends BlogConn  {

	
	private String fileName =null;
	private byte[] fileContent=null; //base64 Encoding
	private String blogID;
	
	public NewMediaObjectConn(String hint,	String userHint, String passwordHint, TimeZone tz, String blogID,
			String fileName, byte[] fileContent) {
		super(hint, userHint, passwordHint, tz);
		this.fileContent=fileContent;
		this.fileName=fileName;
		this.blogID=blogID;
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
			System.out.println("notify error"); //TODO handle error here
		}
	}
}

/*
<methodCall>
<methodName>metaWeblog.newMediaObject</methodName>
<params>
  <param>
    <value>
	<int>178663</int>
    </value>
  </param>
  <param>
    <value>
	<string>your username</string>
    </value>
  </param>
  <param>
    <value>
	<string>your password</string>
    </value>
  </param>
  <param>
    <value>
	<struct>
	  <member>
	    <name>name</name>
	    <value>
	      <string>foo.txt</string>
	    </value>
	  </member>
	  <member>
	    <name>bits</name>
	    <value>
	      <string>QUJDRDEyMzRlcwoK</string>
	    </value>
	  </member>
	</struct>
    </value>
  </param>
</params>
</methodCall>

<methodResponse>
  <params>
    <param>
      <value>
	<struct>
	  <member>
	    <name>url</name>
	    <value>
	      <string>
	      http://typekeytest111.typepad.com/my_weblog/foo.txt</string>
	    </value>
	  </member>
	</struct>
      </value>
    </param>
  </params>
</methodResponse>

*/