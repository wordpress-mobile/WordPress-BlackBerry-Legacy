package com.wordpress.utils;

import java.util.Random;
import java.util.Vector;

import com.wordpress.utils.log.Log;

public class Tools {
	
	
	public static synchronized String getAppVersion() {
    	String version = PropertyUtils.getAppVersion(); //read from the alx files
        if(version == null || version.trim().equals("")) { //read value from jad file
        	//MIDlet-Version
        	version = PropertyUtils.getIstance().get("MIDlet-Version");
        	if(version == null)
        		version = "";
        }
    	Log.debug("App version: "+version);
        return version;
	}
	
	
	/**
	 * check the path of the file xmlrpc.php into the url string
	 */
	public static String checkURL(String url){
		Log.trace(">>> checkURL");
		Log.trace("in URL: "+url);
		if(url == null || url.trim().length() == 0 ) {
			return null;
		}
			
		if (url.endsWith("xmlrpc.php")){
			
		} else {
			if (!url.endsWith("/")){
				url+="/";
			}
			url+="xmlrpc.php";
		}
		Log.trace("out URL: "+url);	
		return url;
	}
	
  public static double round(final double num) {
	    final double floor = Math.floor(num);
	    if (num - floor >= 0.5) {
	      return Math.ceil(num);
	    } else {
	      return floor;
	    }
	  }
	
	public static long generateDeviceUUID() {
		return (new Random()).nextLong();
	}
	
	 public static int decodeInt(Object input){	 
		 if (input instanceof String){
			 return Integer.parseInt((String)input);
		 } else if (input instanceof Integer){
			 return ((Integer)input).intValue();			 
		 } else if (input instanceof Boolean) {
			 if (((Boolean)input).booleanValue())
				 return 1;
			 else 
				 return 0;
		 }
		 return -1;		 
	 }
	 
	 public static String decodeString(Object input){
		 if(input == null ) return null;
		 if (input instanceof String)
			 return (String) input;
		  else 
			 return String.valueOf(input);			 			 
	 }
	
	
	
	  /**
	   * Convert a vector to a string array.
	   * 
	   * @param v
	   *          vector to convert
	   * @return the string array
	   */
	  public static String[] toStringArray(final Vector v) {
	    final String[] res = new String[v.size()];
	    v.copyInto(res);
	    return res;
	  }
	  

	  public static void bubbleSort(final int array[]) {
		    final int elements = array.length;
		    int i;
		    int j;
		    int t = 0;
		    for (i = 0; i < elements; i++) {
		      for (j = 1; j < (elements - i); j++) {
		        if (array[j - 1] > array[j]) {
		          t = array[j - 1];
		          array[j - 1] = array[j];
		          array[j] = t;
		        }
		      }
		    }
		  }
	  
}
