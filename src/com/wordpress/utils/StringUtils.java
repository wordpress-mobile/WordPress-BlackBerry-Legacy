package com.wordpress.utils;

import java.util.Vector;

public class StringUtils {

	
	
	/**
	 * Turn any HTML escape entities in the string into
	 * characters and return the resulting string.
	 
	public static String unescapeHTML(String s){
		StringBuffer result = new StringBuffer(s.length());
		int ampInd = s.indexOf("&");
		int lastEnd = 0;
		while (ampInd >= 0){
			int nextAmp = s.indexOf("&", ampInd+1);
			int nextSemi = s.indexOf(";", ampInd+1);
			if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)){ //check if html entity
				int value = -1;
				String escape = s.substring(ampInd+1,nextSemi);
				try {
					if (escape.startsWith("#")){
						value = Integer.parseInt(escape.substring(1), 10);
					} else {
						if (htmlEntities.containsKey(escape)){
							value = htmlEntities.get(escape).intValue();
						}
					}
				} catch (NumberFormatException x){
					// Could not parse the entity,
					// output it verbatim
				}
				result.append(s.substring(lastEnd, ampInd));
				lastEnd = nextSemi + 1;
				if (value >= 0 && value <= 0xffff){
					result.append((char)value);
				} else {
					result.append("&").append(escape).append(";");
				}
			}
			ampInd = nextAmp;
		}
		result.append(s.substring(lastEnd));
		return result.toString();
	}
*/
	
	//	FIX WP DOUBLE ENCODED AMPESAND
	//@see http://blackberry.trac.wordpress.org/ticket/17
	public static String  fixWordPressDoubleEncodedAmpersand(String xmlInput) {
		String output = StringUtils.replaceAll(xmlInput, "&amp;amp;", "&amp;"); //FIX WP DOUBLE ENCODED AMPESAND;
		output = StringUtils.replaceAll(output, "&amp;#038;", "&#038;");
		output = StringUtils.replaceAll(output, "&amp;#039;", "&#039;"); 
		
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
