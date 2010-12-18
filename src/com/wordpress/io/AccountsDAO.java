package com.wordpress.io;

import java.util.Hashtable;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeSigningKey;
import net.rim.device.api.system.ControlledAccess;
import net.rim.device.api.system.ControlledAccessException;
import net.rim.device.api.system.PersistentObject;
import net.rim.device.api.system.PersistentStore;

import com.wordpress.utils.log.Log;

public class AccountsDAO implements BaseDAO {
	
	public final static String USERNAME_KEY = "username";
	public final static String PASSWORD_KEY = "passwd";
	public final static String BLOGNUMBER_KEY = "blogs_number";
	private final static long ACCOUNTS_DATA_ID = 0x6dab7eef7d39609cL; //com.wordpress.io.AccountsDAOAccountsDAO
	
	public static synchronized Hashtable loadAccounts() throws ControlledAccessException {
		Log.debug(">>> loadAccounts");
		Hashtable accounts = null;
		
		try {
		     PersistentObject rec = PersistentStore.getPersistentObject(ACCOUNTS_DATA_ID);
		     int moduleHandle = ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle();
		     CodeSigningKey codeSigningKey = CodeSigningKey.get( moduleHandle, "WP" );
		     Object contents = rec.getContents(codeSigningKey);
		     if(contents == null) {
				Log.debug("No accounts found.");
				Log.debug("<<< loadAccounts");
				return new Hashtable();
		     }
		     
		     accounts = (Hashtable) contents;
		     
		}    catch (ControlledAccessException e) {
		     Log.error(e, "ControlledAccessException - not authorised to read accounts data");
		     throw e;
		}
				
		Log.debug("<<< loadAccounts");
		return accounts;
	}
	
    public static synchronized void storeAccounts(Hashtable accounts) throws ControlledAccessException  {
		Log.debug(">>> store accounts");
		
		PersistentObject persistentObject;
		persistentObject = PersistentStore.getPersistentObject( ACCOUNTS_DATA_ID );		
		int moduleHandle = ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle();
		// Get the code signing key associated with "WP"
	    CodeSigningKey codeSigningKey = CodeSigningKey.get( moduleHandle, "WP" );
		persistentObject.setContents( new ControlledAccess( accounts, codeSigningKey ) );
		persistentObject.commit();
		
		Log.debug("Account stored succesfully!");
		Log.debug("<<< store accounts");
	}
}