package com.wordpress.controller;

import java.util.Hashtable;
import java.util.Vector;

import com.wordpress.model.Post;
import com.wordpress.utils.log.Log;

public class LocationHelper {
	
	 public static void removeAllLocationCustomFields(Vector customFields) {
			Log.debug(">>> removeLocationCustomFields ");
			int size = customFields.size();
	    	Log.debug("Found "+size +" custom fields");
	    	
			for (int i = 0; i <size; i++) {
				Log.debug("Elaborating custom field # "+ i);
				try {
					Hashtable customField = (Hashtable)customFields.elementAt(i);
					
					String ID = (String)customField.get("id");
					String key = (String)customField.get("key");
					String value = (String)customField.get("value");
					
					if(key == null) continue;
					
					Log.debug("id - "+ID);
					Log.debug("key - "+key);
					Log.debug("value - "+value);	
					
					//find location fields
					if(key.equalsIgnoreCase("geo_address") || key.equalsIgnoreCase("geo_public")
							|| key.equalsIgnoreCase("geo_accuracy")
							|| key.equalsIgnoreCase("geo_longitude") || key.equalsIgnoreCase("geo_latitude")) {
					
						 customField.remove("key");
						 customField.remove("value");
						 Log.debug("Removed custom field : "+ key);
					} 
				} catch(Exception ex) {
					Log.error("Error while Elaborating custom field # "+ i);
				}
			}
			Log.debug("<<< removeLocationCustomFields ");
		}
	 
		public static void setLocationPublicCustomField(Vector customFields ) {
			Log.debug(">>> setLocationPublic ");
			int size = customFields.size();
	    	Log.debug("Found "+size +" custom fields");
	    	boolean locationPublicFound = false;
	    	
			for (int i = 0; i <size; i++) {
				Log.debug("Elaborating custom field # "+ i);
				try {
					Hashtable customField = (Hashtable)customFields.elementAt(i);
					
					String ID = (String)customField.get("id");
					String key = (String)customField.get("key");
					String value = (String)customField.get("value");
					
					if(key == null) continue;
					
					Log.debug("id - "+ID);
					Log.debug("key - "+key);
					Log.debug("value - "+value);	
					
					//geo_public
					if( key.equalsIgnoreCase("geo_public")){
						Log.debug("Updated custom field : "+ key);
						customField.put("value", String.valueOf(1));
						locationPublicFound = true;
					}
					
				} catch(Exception ex) {
					Log.error("Error while Elaborating custom field # "+ i);
				}
			}
			
			if(locationPublicFound == false)
			{
				Hashtable customField3 = new Hashtable();
				customField3.put("key", "geo_public"); 
				customField3.put("value", String.valueOf(1));
				customFields.addElement(customField3);				
				Log.debug("Added custom field geo_public");
			}
			
			Log.debug("<<< setLocationPublic ");
		}
	 
	 public static void removeLocationPublicCustomField(Vector customFields) {
			Log.debug(">>> removeisLocationPublicCustomField ");
			int size = customFields.size();
	    	Log.debug("Found "+size +" custom fields");
	    	
			for (int i = 0; i <size; i++) {
				Log.debug("Elaborating custom field # "+ i);
				try {
					Hashtable customField = (Hashtable)customFields.elementAt(i);
					
					String ID = (String)customField.get("id");
					String key = (String)customField.get("key");
					String value = (String)customField.get("value");
					
					if(key == null) continue;
					
					Log.debug("id - "+ID);
					Log.debug("key - "+key);
					Log.debug("value - "+value);	
					
					//find location fields
					if( key.equalsIgnoreCase("geo_public")){
						 customField.remove("key");
						 customField.remove("value");
						 Log.debug("Removed custom field : "+ key);
					} 
				} catch(Exception ex) {
					Log.error("Error while Elaborating custom field # "+ i);
				}
			}
			Log.debug("<<< removeisLocationPublicCustomField ");
		}
	 
	public static boolean isLocationCustomFieldsAvailable(Post post) {
		Vector customFields = post.getCustomFields();
    	int size = customFields.size();
    	Log.debug("Found "+size +" custom fields");
    	
		for (int i = 0; i <size; i++) {
			Log.debug("Elaborating custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)customFields.elementAt(i);
				
				String ID = (String)customField.get("id");
				String key = (String)customField.get("key");
				String value = (String)customField.get("value");
				
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				if(key == null || value == null) continue;
				
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				if(key.equalsIgnoreCase("geo_longitude") ||
				   key.equalsIgnoreCase("geo_latitude") ||
				   key.equalsIgnoreCase("geo_address")
				) {
					Log.debug("Location Custom Field  found!");
					return true;
				} 
				
			} catch(Exception ex) {
				Log.error("Error while Elaborating custom field # "+ i);
			}
		}
		return false;
	}
	
    public static boolean isLocationPublicCustomField(Vector customFields){
    	int size = customFields.size();
    	Log.debug("Found "+size +" custom fields");
    	
		for (int i = 0; i <size; i++) {
			Log.debug("Elaborating custom field # "+ i);
			try {
				Hashtable customField = (Hashtable)customFields.elementAt(i);
				
				String ID = (String)customField.get("id");
				String key = (String)customField.get("key");
				String value = (String)customField.get("value");
				
				if(key == null || value == null) continue;
				
				Log.debug("id - "+ID);
				Log.debug("key - "+key);
				Log.debug("value - "+value);	
				
				if( key.equalsIgnoreCase("geo_public")){
					Log.debug("Updated custom field : "+ key);
					if(Integer.parseInt(value) == 0) return false;
					else return true;
				}
				
			} catch(Exception ex) {
				Log.error("Error while Elaborating custom field # "+ i);
			}
		}
		
		return false;
    }
    
    private static String getCustomFieldValue(Vector customFields, String cfKey) {
        int size = customFields.size();
        String cfValue = null;
    	for (int i = 0; i <size; i++) {

    		Hashtable customField = (Hashtable)customFields.elementAt(i);
    		String key = (String)customField.get("key");
    		String value = (String)customField.get("value");
    		
    		if(key == null || value == null) continue;
    		
    		if(key.equalsIgnoreCase(cfKey)) {
    			cfValue = value;
    			break;
    		} 
    	}
    	
    	return cfValue;
    }
    
    public static String getLatitute(Vector customFields) {
    	return getCustomFieldValue(customFields, "geo_latitude");

    }
    
    public static String getLongitude(Vector customFields) {
    	return getCustomFieldValue(customFields, "geo_longitude");
    }
    
}
