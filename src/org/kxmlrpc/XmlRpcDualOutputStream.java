package org.kxmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.rms.RecordStoreException;

import com.wordpress.io.AppDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.utils.log.Log;

public class XmlRpcDualOutputStream {
	
	private OutputStream os = null;
	private byte[] memoryStorage = null; //used when no disk storage
	public String tmpFilePath = null; //used when disk storage
	
	public XmlRpcDualOutputStream() {
		os = getTmpFileOutputStream(); //first try the file output stream
    	if(os == null) {
    		setUpMemoryOutputStream();
    	} else {
    		
    		try {
				tmpFilePath = AppDAO.getXmlRpcTempFilePath();
			} catch (RecordStoreException e) {
				setUpMemoryOutputStream();
			} catch (IOException e) {
				setUpMemoryOutputStream();
			}
    	}
	}
	
	public void close() {		
		try {
			Log.trace("Closing the xml rpc output stream");
			os.flush();
			
		 	if(os instanceof ByteArrayOutputStream) {
	    		memoryStorage = ((ByteArrayOutputStream)os).toByteArray(); //set the global byte array before.
	    	} else {
	    		
	    	}		
			os.close();		
		} catch (Exception e) {
			Log.error("Error while closing the xmlrpc tmp file");
		}
	}
	
	public void clean() {		
		try {
			Log.trace("Clean the resources used by XmlRpc dual output stream");
			try {
				os.close();
				String tmpFilePath = AppDAO.getXmlRpcTempFilePath();
				if(JSR75FileSystem.isFileExist(tmpFilePath)) {
					JSR75FileSystem.removeFile(tmpFilePath);
				}
				memoryStorage = null;
			} catch (RecordStoreException e) {
		    	Log.error(e, "Error while clean xmlrpc tmp file");
			} catch (IOException e) {
				Log.error(e, "Error while clean xmlrpc file");
			}
		} catch (Exception e) {
			Log.error("Error while closing the xmlrpc tmp file");
		}
	}
	
	public void sendRequest(OutputStream out) throws Exception{
		// Push the request to the server
		if(os instanceof ByteArrayOutputStream) {
			if( memoryStorage == null )
				memoryStorage = ((ByteArrayOutputStream)os).toByteArray();
			out.write(memoryStorage);
		} else {
			InputStream inStream = JSR75FileSystem.getDataInputStream(tmpFilePath);
			byte[] buffer = new byte[1024]; 
			int length = -1;
			int count = 1;
			while ((length = inStream.read(buffer)) >0 ) {
				out.write(buffer, 0 , length);
				Log.trace("1024byte X: "+ (count++));
				//Log.trace("1048576byte per: "+ (count++));
			}
			inStream.close();
		}
	}
	
	public long getMessageLength() throws IOException {
    	    	
    	if(os instanceof ByteArrayOutputStream) {
    		//Log.trace("request sended to the wordpress server: "+os.toString());
    		//memoryStorage = ((ByteArrayOutputStream)os).toByteArray(); //set the global byte array before.
    		if(memoryStorage != null)
    			return  memoryStorage.length;
    		else return 0; //non è mai nullo se lo stream viene chiuso prima di chiamare le funzioni di output
    	} else {
    		return JSR75FileSystem.getFileSize(tmpFilePath);
    	}
	}
	

	public OutputStream getOutputStream() {
		return os;
	}


	private void setUpMemoryOutputStream() {
		Log.trace("Xmlrpc call works with byte array in memory! this could be a bottle neck when video/image");
		os = new ByteArrayOutputStream();
	}
	

    private DataOutputStream getTmpFileOutputStream() {
    	String tmpFilePath;
		try {
			tmpFilePath = AppDAO.getXmlRpcTempFilePath();
			if(JSR75FileSystem.isFileExist(tmpFilePath)) {
				JSR75FileSystem.removeFile(tmpFilePath);
			}
			JSR75FileSystem.createFile(tmpFilePath);
			return JSR75FileSystem.getDataOutputStream(tmpFilePath);
		
		} catch (RecordStoreException e) {
	    	Log.error(e, "Error while allocating xmlrpc tmp file");
		} catch (IOException e) {
			Log.error(e, "Error while allocating xmlrpc file");
		}
		
		return null;
    }
	
    
    
	
}
