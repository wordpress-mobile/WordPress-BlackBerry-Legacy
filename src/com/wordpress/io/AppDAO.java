package com.wordpress.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.io.file.FileConnection;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;

public class AppDAO implements BaseDAO {

	private final static String basePathRecordStoreKey="basepath";
	
	public static final String SD_STORE_PATH;
	
	static {
		String path = System.getProperty("fileconn.dir.memorycard");
		if(path != null) {
			SD_STORE_PATH = System.getProperty("fileconn.dir.memorycard")+"BlackBerry/wordpress/";
		} else {
			SD_STORE_PATH = "file:///SDCard/BlackBerry/wordpress/";
		}
	}
	
	public static String getInstFilePath() throws RecordStoreException, IOException {
		return getBaseDirPath() + INST_FILE;
	}
	
	public static String getAppPrefsFilePath() throws RecordStoreException, IOException {	
		return getBaseDirPath() + APP_PREFS_FILE;
	}
	
	public static String getAccountsFilePath() throws RecordStoreException, IOException {	
		return getBaseDirPath() + ACCOUNTS_FILE;
	}
	
	public static String getXmlRpcTempFilePath() throws RecordStoreException, IOException {	
		return getBaseDirPath() + APP_TMP_XMLRPC_FILE;
	}
	
	public static String getImageTempFilePath() throws RecordStoreException, IOException {	
		return getBaseDirPath() + APP_TMP_IMG_FILE;
	}
	
	// return the application base path
	public static String getBaseDirPath() throws RecordStoreException, IOException {
		
		RecordStore records = null;
		byte[] record = null;
		DataInputStream data = null;
		String basePath = null;

		records = RecordStore.openRecordStore(basePathRecordStoreKey, true);
		if (records.getNumRecords() > 0) {
			record = records.getRecord(1);
			data = new DataInputStream(new ByteArrayInputStream(record));
			basePath = data.readUTF();
			data.close();
		}
		records.closeRecordStore();
		return basePath;
	}

	//remove the entire application folder structure
	public static void cleanUpFolderStructure() throws RecordStoreException, IOException {
		if(JSR75FileSystem.isFileExist(getBaseDirPath())){
			JSR75FileSystem.removeFile(getBaseDirPath());
		}
	}
	
	public static void setUpFolderStructure() throws RecordStoreException, IOException {
	
		if(JSR75FileSystem.isFileExist(getBaseDirPath())){
			//JSR75FileSystem.removeFile(getBaseDirPath());
		} else {
			JSR75FileSystem.createDir(AppDAO.getBaseDirPath());
		}
	}

	
	// set the application base path
	public static synchronized void setBaseDirPath(String basePath)
			throws 	RecordStoreException, IOException {
		
		RecordStore records = null;
		ByteArrayOutputStream bytes;
		DataOutputStream data;
		byte[] record;

		records = RecordStore.openRecordStore(basePathRecordStoreKey, true);
		bytes = new ByteArrayOutputStream();
		data = new DataOutputStream(bytes);
		data.writeUTF(basePath);
		data.close();
		record = bytes.toByteArray();
		if (records.getNumRecords() > 0) {
			records.setRecord(1, record, 0, record.length); //overwrite prev setting
		} else {
			records.addRecord(record, 0, record.length);			
		}
		records.closeRecordStore();

	}
	
	
	  public static boolean readApplicationPreferecens(Preferences pref) throws IOException, RecordStoreException {
			Log.debug(">>>load application preferences");

			if (!JSR75FileSystem.isFileExist(getAppPrefsFilePath())) {
				return false;
			}

			FileConnection fc = JSR75FileSystem.openFile(getAppPrefsFilePath());
			DataInputStream in = fc.openDataInputStream();
			Serializer ser = new Serializer(in);

			String videoEncoding = (String) ser.deserialize();
			String audioEncoding = (String) ser.deserialize();
			String photoEncoding = (String) ser.deserialize();
			String apn = (String) ser.deserialize();
		    String gateway = (String) ser.deserialize();
		    String gatewayPort = (String) ser.deserialize();
		    String sourceIP = (String) ser.deserialize();
		    String sourcePort = (String) ser.deserialize();
		    Boolean isUserConnectionOptionsEnabled = (Boolean) ser.deserialize();
		    Boolean isUserConnectionWap = (Boolean) ser.deserialize();
		    Boolean isWiFiConnectionPermitted = (Boolean) ser.deserialize();
		    Boolean isTcpConnectionPermitted = (Boolean) ser.deserialize();
		    Boolean isBESConnectionPermitted = (Boolean) ser.deserialize();
		    Boolean isServiceBookConnectionPermitted = (Boolean) ser.deserialize();
		    Boolean isWapConnectionPermitted = (Boolean) ser.deserialize();
		    String userName = (String) ser.deserialize();
		    String userPass = (String) ser.deserialize();

		    Hashtable opt = new Hashtable(); 
		    // try to read the added hashtable in the rev108
		    //in this hashtable we should store all next needed variable
		    if(in.available() != 0) { 
		    	opt = (Hashtable) ser.deserialize();
		    }
			
			pref.setAudioEncoding(audioEncoding);
			pref.setPhotoEncoding(photoEncoding);
			pref.setVideoEncoding(videoEncoding);
			pref.setGateway(gateway);
			pref.setGatewayPort(gatewayPort);
			pref.setApn(apn);
			pref.setSourceIP(sourceIP);
			pref.setSourcePort(sourcePort);
			pref.setUserConnectionOptionsEnabled(isUserConnectionOptionsEnabled.booleanValue());
			pref.setUserConnectionWap(isUserConnectionWap.booleanValue());
			pref.setWiFiConnectionPermitted(isWiFiConnectionPermitted.booleanValue());
			pref.setTcpConnectionPermitted(isTcpConnectionPermitted.booleanValue());
			pref.setBlackBerryInternetServicePermitted(isWapConnectionPermitted.booleanValue());
			pref.setBESConnectionPermitted(isBESConnectionPermitted.booleanValue());
			pref.setServiceBookConnectionPermitted(isServiceBookConnectionPermitted.booleanValue());
			pref.setUsername(userName);
			pref.setPassword(userPass);
			pref.setOpt(opt);
			
			Log.debug("Prefs loading succesfully!");
			in.close();
			fc.close();
			return true;
		}
	    
	    public static void storeApplicationPreferecens(Preferences pref) throws IOException, RecordStoreException  {
			Log.debug(">>>store application preferences");

		  	JSR75FileSystem.createFile(getAppPrefsFilePath()); 
		  	FileConnection fc = JSR75FileSystem.openFile(getAppPrefsFilePath());
	    	DataOutputStream out = fc.openDataOutputStream();
	    	
			String videoEncoding = pref.getVideoEncoding();
			String audioEncoding = pref.getAudioEncoding();
			String photoEncoding = pref.getPhotoEncoding();
			String apn = pref.getApn();
		    String gateway = pref.getGateway();
		    String gatewayPort = pref.getGatewayPort();
		    String sourceIP = pref.getSourceIP();
		    String sourcePort = pref.getSourcePort();
		    Boolean isUserConnectionOptionsEnabled = new Boolean(pref.isUserConnectionOptionsEnabled());
		    Boolean isUserConnectionWap = new Boolean(pref.isUserConnectionWap());
		    Boolean isWiFiConnectionPermitted =new Boolean(pref.isWiFiConnectionPermitted());
		    Boolean isTcpConnectionPermitted = new Boolean(pref.isTcpConnectionPermitted());
		    Boolean isBESConnectionPermitted = new Boolean(pref.isBESConnectionPermitted());
		    Boolean isServiceBookConnectionPermitted = new Boolean(pref.isServiceBookConnectionPermitted());
		    Boolean isWapConnectionPermitted = new Boolean(pref.isBlackBerryInternetServicePermitted());
		    String userPass = pref.getPassword();
		    String userName = pref.getUsername();
		    
		    
			Serializer ser= new Serializer(out);
	    	ser.serialize(videoEncoding);
	    	ser.serialize(audioEncoding);
	    	ser.serialize(photoEncoding);
	    	ser.serialize(apn);
	    	ser.serialize(gateway);
	    	ser.serialize(gatewayPort);
	    	ser.serialize(sourceIP);
	    	ser.serialize(sourcePort);
	    	ser.serialize(isUserConnectionOptionsEnabled);
	    	ser.serialize(isUserConnectionWap);
	    	ser.serialize(isWiFiConnectionPermitted);
	    	ser.serialize(isTcpConnectionPermitted);
	    	ser.serialize(isBESConnectionPermitted);
	    	ser.serialize(isServiceBookConnectionPermitted);
	    	ser.serialize(isWapConnectionPermitted);
	    	ser.serialize(userName);
	    	ser.serialize(userPass);
	    	ser.serialize(pref.getOpt());
	    	
	    	out.close();
	    	fc.close();
			Log.debug("Prefs stored succesfully!");
		}
}
