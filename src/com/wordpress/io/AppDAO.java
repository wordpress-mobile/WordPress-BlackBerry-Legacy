package com.wordpress.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeSigningKey;
import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

import com.wordpress.model.Preferences;
import com.wordpress.utils.log.Log;

public class AppDAO implements BaseDAO {

	private final static String basePathRecordStoreKey="basepath";
	public static final String SD_STORE_PATH;
	
	//PersistentStore constants
	public final static long BLOGS_DATA_ID = 0xc526d2b6855be856L; //com.wordpress.io.AppDAO.blogs
//	public final static long WPORG_BLOGS_PASSWORD_ID = 0x457d90a77c83dee3L; //com.wordpress.io.AppDAO.WPORG.blogs
	public final static long ACCOUNTS_DATA_ID = 0x197f31296e9ef33fL; //com.wordpress.io.AppDAO.accounts
	public final static long PREFERENCES_DATA_ID = 0x8d22e492b38c69efL; //com.wordpress.io.AppDAO.preferences
	public final static long[] persistentStoreUsedKeys = {BLOGS_DATA_ID, ACCOUNTS_DATA_ID, PREFERENCES_DATA_ID };	
	
    //keys used to store accounts
    public final static String USERNAME_KEY = "username";
	public final static String PASSWORD_KEY = "passwd";
	public final static String BLOGNUMBER_KEY = "blogs_number";
	
	static {
		String path = System.getProperty("fileconn.dir.memorycard");
		if(path != null) {
			SD_STORE_PATH = System.getProperty("fileconn.dir.memorycard")+"BlackBerry/wordpress/";
		} else {
			SD_STORE_PATH = "file:///SDCard/BlackBerry/wordpress/";
		}
	}
	
	private static boolean isKeysExists(long key) {
		for (int i = 0; i < persistentStoreUsedKeys.length; i++) {
			if (persistentStoreUsedKeys[i] == key) return true;
		}
		return false;
	}
	
	static synchronized Hashtable loadAppData(long key) throws ControlledAccessException, IOException {
		Log.debug(">>> loadAppData for key :"+ key);
		Hashtable appData = null;

		if(!isKeysExists(key)){
			throw new IOException("PersistentStore key doesn't exists!");
		}

		PersistentObject rec = PersistentStore.getPersistentObject(key);
		Object contents = rec.getContents();
		if(contents == null) {
			Log.debug("No app data found for key: " + String.valueOf(key) +" returning an empty hashtable");
			Log.debug("<<< loadAppData");
			return new Hashtable();
		}
		appData = ((CustomHashtable) contents).unwrapAppData();

		Log.debug("<<< loadAppData for key "+ key);
		return appData;
	}
	
	static synchronized Hashtable loadSecuredAppData(long key) throws ControlledAccessException, IOException {
		Log.debug(">>> loadSecuredAppData for key :"+ key);
		Hashtable appData = null;

		if(!isKeysExists(key)){
			throw new IOException("PersistentStore key doesn't exists!");
		}

		PersistentObject rec = PersistentStore.getPersistentObject(key);
		synchronized (rec) {
			try {				
				int moduleHandle = ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle();
				CodeSigningKey codeSigningKey = CodeSigningKey.get( moduleHandle, "WP" );
				Object contents = rec.getContents(codeSigningKey);
				if(contents == null) {
					Log.debug("No app data found for key: " + String.valueOf(key) +" returning an empty hashtable");
					Log.debug("<<< loadSecuredAppData");
					return new Hashtable();
				}
				appData = ((CustomHashtable) contents).unwrapAppData();
			}    catch (ControlledAccessException e) {
				Log.error(e, "ControlledAccessException - not authorised to read app data");
				throw e;
			}
		}
		Log.debug("<<< loadSecuredAppData for key "+ key);
		return appData;
	}
	
	static synchronized void storeAppData(long key, Hashtable appData) throws ControlledAccessException, IOException  {
		Log.debug(">>> storeAppData");
		if(!isKeysExists(key)){
			throw new IOException("key doesn't exists!");
		}
		PersistentObject persistentObject;
		persistentObject = PersistentStore.getPersistentObject( key );
		synchronized (persistentObject) {
			CustomHashtable myAppData = new CustomHashtable();
			myAppData.wrapAppData(appData); //trick to remove the content store obj when deleting the app
			persistentObject.setContents( myAppData );
			persistentObject.commit();
		}
		Log.debug("app data stored succesfully!");
		Log.debug("<<< storeAppData");
	}
    
	
	static synchronized void storeSecuredAppData(long key, Hashtable appData) throws ControlledAccessException, IOException  {
		Log.debug(">>> storeSecuredAppData");
		if(!isKeysExists(key)){
			throw new IOException("key doesn't exists!");
		}
		PersistentObject persistentObject;
		persistentObject = PersistentStore.getPersistentObject( key );
		synchronized (persistentObject) {
			int moduleHandle = ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle();
			CustomHashtable myAppData = new CustomHashtable();
			myAppData.wrapAppData(appData); //trick to remove the content store obj when deleting the app
			// Get the code signing key associated with "WP"
			CodeSigningKey codeSigningKey = CodeSigningKey.get( moduleHandle, "WP" );
			persistentObject.setContents( new ControlledAccess( myAppData, codeSigningKey ) );
			persistentObject.commit();
		}
		Log.debug("app data stored succesfully!");
		Log.debug("<<< storeSecuredAppData");
	}
    	
	public static synchronized Hashtable loadAccounts() throws ControlledAccessException, IOException {
		Log.debug(">>> loadAccounts");
		Hashtable accounts = loadSecuredAppData(ACCOUNTS_DATA_ID);
		Log.debug("<<< loadAccounts");
		return accounts;
	}

    public static synchronized void storeAccounts(Hashtable accounts) throws ControlledAccessException, IOException  {
		Log.debug(">>> store accounts");
		storeSecuredAppData(ACCOUNTS_DATA_ID, accounts);		
		Log.debug("Account stored succesfully!");
		Log.debug("<<< store accounts");
	}
        		
	public static String getXmlRpcTempFilesPath() throws RecordStoreException, IOException {	
		return getBaseDirPath() + APP_TMP_FOLDER_XMLRPC_FILES;
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

	//remove the entire application folder structure and other app data
	public synchronized static void resetAppData() throws RecordStoreException, IOException {

		if(JSR75FileSystem.isFileExist(getBaseDirPath())){
			JSR75FileSystem.removeFile(getBaseDirPath());
		}

		for (int i = 0; i < persistentStoreUsedKeys.length; i++) {
			if (persistentStoreUsedKeys[i] != PREFERENCES_DATA_ID){ //do not delete preferences on app reset

				try {
					PersistentObject rec = PersistentStore.getPersistentObject(persistentStoreUsedKeys[i] );

					if (persistentStoreUsedKeys[i] == ACCOUNTS_DATA_ID || persistentStoreUsedKeys[i] == BLOGS_DATA_ID){
						//delete the secure storage location
						int moduleHandle = ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle();
						CodeSigningKey codeSigningKey = CodeSigningKey.get( moduleHandle, "WP" );
						ControlledAccess controlledAccess = new ControlledAccess(rec, codeSigningKey);
					}

					PersistentStore.destroyPersistentObject(persistentStoreUsedKeys[i]);

				}    catch (ControlledAccessException e) {
					Log.error(e, "You are not authorized to access this delete App data");
					throw e;
				}

			}
		}//end for
	}

	public static void setUpFolderStructure() throws RecordStoreException, IOException {
		if(JSR75FileSystem.isFileExist(getBaseDirPath())){
			//JSR75FileSystem.removeFile(getBaseDirPath());
		} else {
			JSR75FileSystem.createDir(AppDAO.getBaseDirPath());
		}
	}

	
	  public static boolean readApplicationPreferecens(Preferences pref) throws IOException, RecordStoreException {
			Log.debug(">>>load application preferences");
			Hashtable preferences = loadAppData(PREFERENCES_DATA_ID);
			
			if(preferences.get("videoEncoding") != null) {
				pref.setVideoEncoding((String)preferences.get("videoEncoding"));
			}
			if(preferences.get("audioEncoding") != null) {
				pref.setAudioEncoding((String)preferences.get("audioEncoding"));
			}
			if(preferences.get("photoEncoding") != null) {
				pref.setPhotoEncoding((String)preferences.get("audioEncoding"));
			}	
			if(preferences.get("apn") != null) {
				pref.setApn((String)preferences.get("apn"));
			}
			if(preferences.get("gateway") != null) {
				pref.setGateway((String)preferences.get("gateway"));
			}
			if(preferences.get("gatewayPort") != null) {
				pref.setGatewayPort((String)preferences.get("gatewayPort"));
			}
			if(preferences.get("sourceIP") != null) {
				pref.setSourceIP((String)preferences.get("sourceIP"));
			}
			if(preferences.get("sourcePort") != null) {
				pref.setSourcePort((String)preferences.get("sourcePort"));
			}
			if(preferences.get("isUserConnectionOptionsEnabled") != null) {
				pref.setUserConnectionOptionsEnabled(((Boolean)preferences.get("isUserConnectionOptionsEnabled")).booleanValue());
			}
			if(preferences.get("isUserConnectionWap") != null) {
				pref.setUserConnectionWap(((Boolean)preferences.get("isUserConnectionWap")).booleanValue());
			}
			if(preferences.get("isWiFiConnectionPermitted") != null) {
				pref.setWiFiConnectionPermitted(((Boolean)preferences.get("isWiFiConnectionPermitted")).booleanValue());
			}
			if(preferences.get("isTcpConnectionPermitted") != null) {
				pref.setTcpConnectionPermitted(((Boolean)preferences.get("isTcpConnectionPermitted")).booleanValue());
			}
			if(preferences.get("isBESConnectionPermitted") != null) {
				pref.setBESConnectionPermitted(((Boolean)preferences.get("isBESConnectionPermitted")).booleanValue());
			}
			if(preferences.get("isServiceBookConnectionPermitted") != null) {
				pref.setServiceBookConnectionPermitted(((Boolean)preferences.get("isServiceBookConnectionPermitted")).booleanValue());
			}
			if(preferences.get("isWapConnectionPermitted") != null) {
				pref.setBlackBerryInternetServicePermitted(((Boolean)preferences.get("isWapConnectionPermitted")).booleanValue());
			}
	
			if(preferences.get("userName") != null) {
				pref.setUsername((String)preferences.get("userName"));
			}
			if(preferences.get("userPass") != null) {
				pref.setPassword((String)preferences.get("userPass"));
			}

			if(preferences.get("opt") != null) {
				pref.setOpt((Hashtable)preferences.get("opt"));
			}
			
			Log.debug("Prefs loading succesfully!");
			return true;
		}
	    
	    public static void storeApplicationPreferecens(Preferences pref) throws IOException, RecordStoreException  {
			Log.debug(">>>store application preferences");
	    	
			Hashtable preferences = new Hashtable();
			
			String videoEncoding = pref.getVideoEncoding();
			preferences.put("videoEncoding", videoEncoding);
			
			String audioEncoding = pref.getAudioEncoding();
			preferences.put("audioEncoding", audioEncoding);
			
			String photoEncoding = pref.getPhotoEncoding();
			preferences.put("photoEncoding", photoEncoding);
			
			String apn = pref.getApn();
			preferences.put("apn", apn);
			
		    String gateway = pref.getGateway();
		    preferences.put("videoEncoding", gateway);
		    
		    String gatewayPort = pref.getGatewayPort();
		    preferences.put("gatewayPort", gatewayPort);
		    
		    String sourceIP = pref.getSourceIP();
		    preferences.put("sourceIP", sourceIP);
		    
		    String sourcePort = pref.getSourcePort();
		    preferences.put("sourcePort", sourcePort);
		    
		    Boolean isUserConnectionOptionsEnabled = new Boolean(pref.isUserConnectionOptionsEnabled());
		    preferences.put("isUserConnectionOptionsEnabled", isUserConnectionOptionsEnabled);
		    
		    Boolean isUserConnectionWap = new Boolean(pref.isUserConnectionWap());
		    preferences.put("isUserConnectionWap", isUserConnectionWap);
		    
		    Boolean isWiFiConnectionPermitted =new Boolean(pref.isWiFiConnectionPermitted());
		    preferences.put("isWiFiConnectionPermitted", isWiFiConnectionPermitted);
		    
		    Boolean isTcpConnectionPermitted = new Boolean(pref.isTcpConnectionPermitted());
		    preferences.put("isTcpConnectionPermitted", isTcpConnectionPermitted);
		    
		    Boolean isBESConnectionPermitted = new Boolean(pref.isBESConnectionPermitted());
		    preferences.put("isBESConnectionPermitted", isBESConnectionPermitted);
		    
		    Boolean isServiceBookConnectionPermitted = new Boolean(pref.isServiceBookConnectionPermitted());
		    preferences.put("isServiceBookConnectionPermitted", isServiceBookConnectionPermitted);
		    
		    Boolean isWapConnectionPermitted = new Boolean(pref.isBlackBerryInternetServicePermitted());
		    preferences.put("isWapConnectionPermitted", isWapConnectionPermitted);
		    
		    String userPass = pref.getPassword();
		    preferences.put("userPass", userPass);
		    
		    String userName = pref.getUsername();
		    preferences.put("userName", userName);
	    	
		    preferences.put("opt", pref.getOpt());
		    
		    storeAppData(PREFERENCES_DATA_ID, preferences);

			Log.debug("Prefs stored succesfully!");
		}
}