package com.wordpress.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;

public class AppDAO implements BaseDAO{

	private final static String basePathRecordStoreKey="basepath";
	
	public static String getInstFilePath() throws RecordStoreException, IOException {
		return getBaseDirPath() + INST_FILE;
	}

	public static String getAppPrefsFilePath() throws RecordStoreException, IOException {	
		return getBaseDirPath() + APP_PREFS_FILE;
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
	
	
	public static void setUpFolderStructure() throws RecordStoreException, IOException {
		setBaseDirPath("file:///store/home/user/wordpress/");
		
		if(JSR75FileSystem.isFileExist(getBaseDirPath())){
			JSR75FileSystem.removeFile(getBaseDirPath());
		}
		
		JSR75FileSystem.createDir(AppDAO.getBaseDirPath());
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
		
		records.addRecord(record, 0, record.length);
		records.closeRecordStore();

	}
	
	
	  public static boolean readApplicationPreferecens(Preferences pref) throws IOException, RecordStoreException {
			Log.debug(">>>load application preferences");

			if (!JSR75FileSystem.isFileExist(getAppPrefsFilePath())) {
				return false;
			}

			DataInputStream in = JSR75FileSystem.getDataInputStream(getAppPrefsFilePath());
			Serializer ser = new Serializer(in);

			String videoEncoding = (String) ser.deserialize();
			String audioEncoding = (String) ser.deserialize();
			String photoEncoding = (String) ser.deserialize();
			String apn = (String) ser.deserialize();
		    String gateway = (String) ser.deserialize();
		    String gatewayPort = (String) ser.deserialize();
		    String sourceIP = (String) ser.deserialize();
		    String sourcePort = (String) ser.deserialize();
		    Boolean isUserOptionsEnabled =  (Boolean) ser.deserialize();
		    String userName = (String) ser.deserialize();
		    String userPass = (String) ser.deserialize();
			
			pref.setAudioEncoding(audioEncoding);
			pref.setPhotoEncoding(photoEncoding);
			pref.setVideoEncoding(videoEncoding);
			pref.setGateway(gateway);
			pref.setGatewayPort(gatewayPort);
			pref.setApn(apn);
			pref.setSourceIP(sourceIP);
			pref.setSourcePort(sourcePort);
			pref.setUserConnectionOptionsEnabled(isUserOptionsEnabled.booleanValue());
			pref.setUsername(userName);
			pref.setPassword(userPass);
			
			Log.debug("Prefs loading succesfully!");
			in.close();
			return true;
		}
	    
	    public static void storeApplicationPreferecens(Preferences pref) throws IOException, RecordStoreException  {
			Log.debug(">>>store application preferences");

		  	JSR75FileSystem.createFile(getAppPrefsFilePath()); 
	    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(getAppPrefsFilePath());
	    	
			String videoEncoding = pref.getVideoEncoding();
			String audioEncoding = pref.getAudioEncoding();
			String photoEncoding = pref.getPhotoEncoding();
			String apn = pref.getApn();
		    String gateway = pref.getGateway();
		    String gatewayPort = pref.getGatewayPort();
		    String sourceIP = pref.getSourceIP();
		    String sourcePort = pref.getSourcePort();
		    Boolean isUserOptionsEnabled = new Boolean(pref.isUserConnectionOptionsEnabled());
		    String userName = pref.getUsername();
		    String userPass = pref.getPassword();
		    
			Serializer ser= new Serializer(out);
	    	ser.serialize(videoEncoding);
	    	ser.serialize(audioEncoding);
	    	ser.serialize(photoEncoding);
	    	ser.serialize(apn);
	    	ser.serialize(gateway);
	    	ser.serialize(gatewayPort);
	    	ser.serialize(sourceIP);
	    	ser.serialize(sourcePort);
	    	ser.serialize(isUserOptionsEnabled);
	    	ser.serialize(userName);
	    	ser.serialize(userPass);
	    	
	    	out.close();
			Log.debug("Prefs stored succesfully!");
		}
}
