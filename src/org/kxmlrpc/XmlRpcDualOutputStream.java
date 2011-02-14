//#preprocess
package org.kxmlrpc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordStoreException;

import com.wordpress.io.AppDAO;
import com.wordpress.io.FileUtils;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.utils.log.Log;

public class XmlRpcDualOutputStream {
	
	private OutputStream os = null;
	private FileConnection filecon = null;
	private byte[] memoryStorage = null; //used when no disk storage
	private String tmpFilePath = null; //real tmp file path.
	
	public XmlRpcDualOutputStream() {
		
		//#ifndef IS_TORCH
		try {
			tmpFilePath = AppDAO.getXmlRpcTempFilesPath()+String.valueOf(System.currentTimeMillis());
			//check the folder first
			if(!JSR75FileSystem.isFileExist(AppDAO.getXmlRpcTempFilesPath())) {
				JSR75FileSystem.createDir(AppDAO.getXmlRpcTempFilesPath());
			}
			//create the file
			JSR75FileSystem.createFile(tmpFilePath);
			filecon = (FileConnection) Connector.open(tmpFilePath);
			if (!filecon.exists()) {
				throw new IOException("File does not exist: " + tmpFilePath);
			}
			os = filecon.openOutputStream();
		} catch (RecordStoreException e) {
	    	Log.error(e, "Error while allocating xmlrpc tmp file");
	    	os = null;
	    	tmpFilePath = null;
		} catch (IOException e) {
			Log.error(e, "Error while allocating xmlrpc file");
	    	os = null;
	    	tmpFilePath = null;
		}
		//#endif
		
    	if(os == null) {
    		setUpMemoryOutputStream();
    	}
	}
	
	public void close() {		
		try {
			Log.trace("Closing the xml rpc output stream");
			//os.flush();
			
		 	if(os instanceof ByteArrayOutputStream) {
	    		memoryStorage = ((ByteArrayOutputStream)os).toByteArray(); //set the global byte array before.
	    	} else {
	    		
	    	}		
			FileUtils.closeStream(os);
			FileUtils.closeConnection(filecon);
		} catch (Exception e) {
			Log.error("Error while closing the xmlrpc tmp file");
		}
	}
	
	public void clean() {		
		try {
			Log.trace("Clean the resources used by XmlRpc dual output stream");
			try {
				FileUtils.closeStream(os);
				FileUtils.closeConnection(filecon);
				if(tmpFilePath != null && JSR75FileSystem.isFileExist(tmpFilePath)) {
					JSR75FileSystem.removeFile(tmpFilePath);
				}
				memoryStorage = null;
			} catch (IOException e) {
				Log.error(e, "Error while clean xmlrpc file");
			}
		} catch (Exception e) {
			Log.error("Error while cleaning the xmlrpc tmp file");
		}
	}
	
	public void sendRequest(OutputStream out) throws Exception{
		// Push the request to the server
		if(os instanceof ByteArrayOutputStream) {
			if( memoryStorage == null )
				memoryStorage = ((ByteArrayOutputStream)os).toByteArray();
			out.write(memoryStorage);
		} else {
			FileConnection filecon = (FileConnection) Connector.open(tmpFilePath, Connector.READ);
			if (!filecon.exists()) {
				Log.error("The xml-rpc tmp file doesn't exist anymore!!");
				throw new IOException(null);
			}
			InputStream inStream = filecon.openDataInputStream();
			
			//InputStream inStream = JSR75FileSystem.getDataInputStream(tmpFilePath);
			byte[] buffer = new byte[3600]; 
			int length = -1;
			while ((length = inStream.read(buffer)) >0 ) {
				out.write(buffer, 0 , length);
			}
			FileUtils.closeStream(inStream);
			FileUtils.closeConnection(filecon);
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
}
