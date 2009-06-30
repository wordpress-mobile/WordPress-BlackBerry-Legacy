package com.wordpress.utils;

import java.util.Vector;

public class StringUtils {

	//	FIX WP DOUBLE ENCODED AMPESAND
	//@see http://blackberry.trac.wordpress.org/ticket/17
	public static String  fixWordPressDoubleEncodedAmpersand(String xmlInput) {
		String output = StringUtils.replaceAll(xmlInput, "&amp;amp;", "&amp;"); //FIX WP DOUBLE ENCODED AMPESAND;
		output = StringUtils.replaceAll(output, "&amp;#038;", "&#038;"); 
		
		return output; 
	}
	
	  // Splits string 
	  public static String[] split(final String string, final String splitBy) {
		    final Vector tokens = new Vector();
		    final int tokenLength = splitBy.length();

		    int tokenStart = 0;
		    int splitIndex;
		    while ((splitIndex = string.indexOf(splitBy, tokenStart)) != -1) {
		      tokens.addElement(string.substring(tokenStart, splitIndex));
		      tokenStart = splitIndex + tokenLength;
		    }

		    tokens.addElement(string.substring(tokenStart));

		    final String[] result = new String[tokens.size()];
		    tokens.copyInto(result);
		    return result;
		  }
	
    /**
     * This method is missing in CLDC 1.0 String implementation
     */
    public static boolean equalsIgnoreCase(String string1, String string2) {
        // Strings are both null, return true
        if (string1 == null && string2 == null) {
            return true;
        }
        // One of the two is null, return false
        if (string1 == null || string2 == null) {
            return false;
        }
        // Both are not null, compare the lowercase strings
        if ((string1.toLowerCase()).equals(string2.toLowerCase())) {
            return true;
        } else {
            return false;
        }
    }

	  // Split string in 2 strings 
	 public synchronized static String[] split2Strings( String in, char ch ){
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
		 
	 
	  public static String replaceAll(final String original, final String tokenToBeReplaced, final String value) {
		    //TODO : optimize
		    final StringBuffer result = new StringBuffer();
		    final String[] originalSplit = split(original, tokenToBeReplaced);
		    for (int i = 0; i < originalSplit.length; i++) {
		      result.append(originalSplit[i]);
		      if (i != originalSplit.length - 1) {
		        result.append(value);
		      }
		    }
		    return result.toString();
		  }
	  
}
