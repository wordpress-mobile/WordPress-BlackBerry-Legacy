package com.wordpress.utils;

public class StringUtils {

	  // Divide una string in due parti con un dato carattere di separazione
	 public synchronized static String[] split( String in, char ch ){
	      String[] result = new String[2];
	      int      pos = in.indexOf( ch );

	      if( pos != -1 ){
	          result[0] = in.substring( 0, pos ).trim();
	          result[1] = in.substring( pos+1 ).trim();
	      } else {
	          result[0] = in.trim();
	      }

	      return result;
	  }

	
  public synchronized static int ensureInt(final String string, final int defaultValue) {
	    try {
	      return Integer.parseInt(string);
	    } catch (final NumberFormatException e) {
	      return defaultValue;
	    }
	  }

	  public synchronized static double ensureDouble(final String string, final double defaultValue) {
	    try {
	      return Double.parseDouble(string);
	    } catch (final NumberFormatException e) {
	      return defaultValue;
	    }
	  }
		 
	 
}
