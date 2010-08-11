package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;

import javax.microedition.rms.RecordStoreException;

import com.wordpress.utils.log.Log;

public class AccountsDAO implements BaseDAO {
	
	public final static String USERNAME_KEY = "username";
	public final static String PASSWORD_KEY = "passwd";
	public final static String BLOGNUMBER_KEY = "blogs_number";
	
	public static synchronized Hashtable loadAccounts() throws IOException, RecordStoreException {
		Log.debug(">>> loadAccounts");

		String filePath = AppDAO.getAccountsFilePath();
		
		if (!JSR75FileSystem.isFileExist(filePath)) {
			Log.debug("No accounts found.");
			Log.debug("<<< loadAccounts");
			return new Hashtable();
		}
		DataInputStream in = JSR75FileSystem.getDataInputStream(filePath);
		Serializer ser = new Serializer(in);
		Hashtable accounts = (Hashtable) ser.deserialize();
		in.close();
		Log.debug("<<< loadAccounts");
		return accounts;
	}
	
    public static void storeAccounts(Hashtable accounts) throws IOException, RecordStoreException  {
		Log.debug(">>> store accounts");
		
		String filePath = AppDAO.getAccountsFilePath();
	  	JSR75FileSystem.createFile(filePath); 
    	DataOutputStream out = JSR75FileSystem.getDataOutputStream(filePath);
    		    
		Serializer ser = new Serializer(out);
    	ser.serialize(accounts);
    	out.close();
		Log.debug("Account stored succesfully!");
		Log.debug("<<< store accounts");
	}
}