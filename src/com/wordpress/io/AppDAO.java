package com.wordpress.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.wordpress.utils.Preferences;

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
			System.out.println(">>>load application preferences");

			if (!JSR75FileSystem.isFileExist(getAppPrefsFilePath())) {
				return false;
			}

			DataInputStream in = JSR75FileSystem.getDataInputStream(getAppPrefsFilePath());
			Serializer ser = new Serializer(in);

			int tzOffsetIndex = ((Integer) ser.deserialize()).intValue();
			int localeIndex = ((Integer) ser.deserialize()).intValue();
			String videoEncoding = (String) ser.deserialize();
			String audioEncoding = (String) ser.deserialize();
			String photoEncoding = (String) ser.deserialize();
			boolean deviceSideConnection = ((Boolean) ser.deserialize()).booleanValue();
			
			pref.setAudioEncoding(audioEncoding);
			pref.setDeviceSideConnection(deviceSideConnection);
			pref.setLocaleIndex(localeIndex);
			pref.setPhotoEncoding(photoEncoding);
			pref.setTimeZoneIndex(tzOffsetIndex);
			pref.setVideoEncoding(videoEncoding);
			System.out.println("Prefs loading succesfully!");
			
			in.close();
			
			return true;
		}
	    
	    public static void storeApplicationPreferecens(Preferences pref) throws IOException, RecordStoreException  {
			System.out.println(">>>store application preferences");

		  	JSR75FileSystem.createFile(getAppPrefsFilePath()); 
	    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(getAppPrefsFilePath());
	    	
	    	int tzOffsetIndex = pref.getTimeZoneIndex();
			int localeIndex = pref.getLocaleIndex();
			String videoEncoding = pref.getVideoEncoding();
			String audioEncoding = pref.getAudioEncoding();
			String photoEncoding = pref.getPhotoEncoding();
			boolean deviceSideConnection = pref.isDeviceSideConnection();
			
			Serializer ser= new Serializer(out);
	    	ser.serialize(new Integer(tzOffsetIndex));
	    	ser.serialize(new Integer(localeIndex));
	    	ser.serialize(videoEncoding);
	    	ser.serialize(audioEncoding);
	    	ser.serialize(photoEncoding);
	    	ser.serialize(new Boolean(deviceSideConnection));
	    	
	    	out.close();
			System.out.println("Prefs stored succesfully!");
		}
}
