package com.wordpress.utils;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;

public class StringUtils {
	
	
	//check if RIM devices supports the encoding
	public static boolean isDeviceSupportEncoding(String encoding){
		try{
			new String("Testing string".getBytes(), encoding);
			return true;
		} catch (UnsupportedEncodingException e) {
			return false;
		}
	}
	
	private static Hashtable htmlEntities = new Hashtable();
	static {
		htmlEntities.put("nbsp", new String("160"));
		htmlEntities.put("iexcl", new String("161"));
		htmlEntities.put("cent", new String("162"));
		htmlEntities.put("pound", new String("163"));
		htmlEntities.put("curren", new String("164"));
		htmlEntities.put("yen", new String("165"));
		htmlEntities.put("brvbar", new String("166"));
		htmlEntities.put("sect", new String("167"));
		htmlEntities.put("uml", new String("168"));
		htmlEntities.put("copy", new String("169"));
		htmlEntities.put("ordf", new String("170"));
		htmlEntities.put("laquo", new String("171"));
		htmlEntities.put("not", new String("172"));
		htmlEntities.put("shy", new String("173"));
		htmlEntities.put("reg", new String("174"));
		htmlEntities.put("macr", new String("175"));
		htmlEntities.put("deg", new String("176"));
		htmlEntities.put("plusmn", new String("177"));
		htmlEntities.put("sup2", new String("178"));
		htmlEntities.put("sup3", new String("179"));
		htmlEntities.put("acute", new String("180"));
		htmlEntities.put("micro", new String("181"));
		htmlEntities.put("para", new String("182"));
		htmlEntities.put("middot", new String("183"));
		htmlEntities.put("cedil", new String("184"));
		htmlEntities.put("sup1", new String("185"));
		htmlEntities.put("ordm", new String("186"));
		htmlEntities.put("raquo", new String("187"));
		htmlEntities.put("frac14", new String("188"));
		htmlEntities.put("frac12", new String("189"));
		htmlEntities.put("frac34", new String("190"));
		htmlEntities.put("iquest", new String("191"));
		htmlEntities.put("Agrave", new String("192"));
		htmlEntities.put("Aacute", new String("193"));
		htmlEntities.put("Acirc", new String("194"));
		htmlEntities.put("Atilde", new String("195"));
		htmlEntities.put("Auml", new String("196"));
		htmlEntities.put("Aring", new String("197"));
		htmlEntities.put("AElig", new String("198"));
		htmlEntities.put("Ccedil", new String("199"));
		htmlEntities.put("Egrave", new String("200"));
		htmlEntities.put("Eacute", new String("201"));
		htmlEntities.put("Ecirc", new String("202"));
		htmlEntities.put("Euml", new String("203"));
		htmlEntities.put("Igrave", new String("204"));
		htmlEntities.put("Iacute", new String("205"));
		htmlEntities.put("Icirc", new String("206"));
		htmlEntities.put("Iuml", new String("207"));
		htmlEntities.put("ETH", new String("208"));
		htmlEntities.put("Ntilde", new String("209"));
		htmlEntities.put("Ograve", new String("210"));
		htmlEntities.put("Oacute", new String("211"));
		htmlEntities.put("Ocirc", new String("212"));
		htmlEntities.put("Otilde", new String("213"));
		htmlEntities.put("Ouml", new String("214"));
		htmlEntities.put("times", new String("215"));
		htmlEntities.put("Oslash", new String("216"));
		htmlEntities.put("Ugrave", new String("217"));
		htmlEntities.put("Uacute", new String("218"));
		htmlEntities.put("Ucirc", new String("219"));
		htmlEntities.put("Uuml", new String("220"));
		htmlEntities.put("Yacute", new String("221"));
		htmlEntities.put("THORN", new String("222"));
		htmlEntities.put("szlig", new String("223"));
		htmlEntities.put("agrave", new String("224"));
		htmlEntities.put("aacute", new String("225"));
		htmlEntities.put("acirc", new String("226"));
		htmlEntities.put("atilde", new String("227"));
		htmlEntities.put("auml", new String("228"));
		htmlEntities.put("aring", new String("229"));
		htmlEntities.put("aelig", new String("230"));
		htmlEntities.put("ccedil", new String("231"));
		htmlEntities.put("egrave", new String("232"));
		htmlEntities.put("eacute", new String("233"));
		htmlEntities.put("ecirc", new String("234"));
		htmlEntities.put("euml", new String("235"));
		htmlEntities.put("igrave", new String("236"));
		htmlEntities.put("iacute", new String("237"));
		htmlEntities.put("icirc", new String("238"));
		htmlEntities.put("iuml", new String("239"));
		htmlEntities.put("eth", new String("240"));
		htmlEntities.put("ntilde", new String("241"));
		htmlEntities.put("ograve", new String("242"));
		htmlEntities.put("oacute", new String("243"));
		htmlEntities.put("ocirc", new String("244"));
		htmlEntities.put("otilde", new String("245"));
		htmlEntities.put("ouml", new String("246"));
		htmlEntities.put("divide", new String("247"));
		htmlEntities.put("oslash", new String("248"));
		htmlEntities.put("ugrave", new String("249"));
		htmlEntities.put("uacute", new String("250"));
		htmlEntities.put("ucirc", new String("251"));
		htmlEntities.put("uuml", new String("252"));
		htmlEntities.put("yacute", new String("253"));
		htmlEntities.put("thorn", new String("254"));
		htmlEntities.put("yuml", new String("255"));
		htmlEntities.put("fnof", new String("402"));
		htmlEntities.put("Alpha", new String("913"));
		htmlEntities.put("Beta", new String("914"));
		htmlEntities.put("Gamma", new String("915"));
		htmlEntities.put("Delta", new String("916"));
		htmlEntities.put("Epsilon", new String("917"));
		htmlEntities.put("Zeta", new String("918"));
		htmlEntities.put("Eta", new String("919"));
		htmlEntities.put("Theta", new String("920"));
		htmlEntities.put("Iota", new String("921"));
		htmlEntities.put("Kappa", new String("922"));
		htmlEntities.put("Lambda", new String("923"));
		htmlEntities.put("Mu", new String("924"));
		htmlEntities.put("Nu", new String("925"));
		htmlEntities.put("Xi", new String("926"));
		htmlEntities.put("Omicron", new String("927"));
		htmlEntities.put("Pi", new String("928"));
		htmlEntities.put("Rho", new String("929"));
		htmlEntities.put("Sigma", new String("931"));
		htmlEntities.put("Tau", new String("932"));
		htmlEntities.put("Upsilon", new String("933"));
		htmlEntities.put("Phi", new String("934"));
		htmlEntities.put("Chi", new String("935"));
		htmlEntities.put("Psi", new String("936"));
		htmlEntities.put("Omega", new String("937"));
		htmlEntities.put("alpha", new String("945"));
		htmlEntities.put("beta", new String("946"));
		htmlEntities.put("gamma", new String("947"));
		htmlEntities.put("delta", new String("948"));
		htmlEntities.put("epsilon", new String("949"));
		htmlEntities.put("zeta", new String("950"));
		htmlEntities.put("eta", new String("951"));
		htmlEntities.put("theta", new String("952"));
		htmlEntities.put("iota", new String("953"));
		htmlEntities.put("kappa", new String("954"));
		htmlEntities.put("lambda", new String("955"));
		htmlEntities.put("mu", new String("956"));
		htmlEntities.put("nu", new String("957"));
		htmlEntities.put("xi", new String("958"));
		htmlEntities.put("omicron", new String("959"));
		htmlEntities.put("pi", new String("960"));
		htmlEntities.put("rho", new String("961"));
		htmlEntities.put("sigmaf", new String("962"));
		htmlEntities.put("sigma", new String("963"));
		htmlEntities.put("tau", new String("964"));
		htmlEntities.put("upsilon", new String("965"));
		htmlEntities.put("phi", new String("966"));
		htmlEntities.put("chi", new String("967"));
		htmlEntities.put("psi", new String("968"));
		htmlEntities.put("omega", new String("969"));
		htmlEntities.put("thetasym", new String("977"));
		htmlEntities.put("upsih", new String("978"));
		htmlEntities.put("piv", new String("982"));
		htmlEntities.put("bull", new String("8226"));
		htmlEntities.put("hellip", new String("8230"));
		htmlEntities.put("prime", new String("8242"));
		htmlEntities.put("Prime", new String("8243"));
		htmlEntities.put("oline", new String("8254"));
		htmlEntities.put("frasl", new String("8260"));
		htmlEntities.put("weierp", new String("8472"));
		htmlEntities.put("image", new String("8465"));
		htmlEntities.put("real", new String("8476"));
		htmlEntities.put("trade", new String("8482"));
		htmlEntities.put("alefsym", new String("8501"));
		htmlEntities.put("larr", new String("8592"));
		htmlEntities.put("uarr", new String("8593"));
		htmlEntities.put("rarr", new String("8594"));
		htmlEntities.put("darr", new String("8595"));
		htmlEntities.put("harr", new String("8596"));
		htmlEntities.put("crarr", new String("8629"));
		htmlEntities.put("lArr", new String("8656"));
		htmlEntities.put("uArr", new String("8657"));
		htmlEntities.put("rArr", new String("8658"));
		htmlEntities.put("dArr", new String("8659"));
		htmlEntities.put("hArr", new String("8660"));
		htmlEntities.put("forall", new String("8704"));
		htmlEntities.put("part", new String("8706"));
		htmlEntities.put("exist", new String("8707"));
		htmlEntities.put("empty", new String("8709"));
		htmlEntities.put("nabla", new String("8711"));
		htmlEntities.put("isin", new String("8712"));
		htmlEntities.put("notin", new String("8713"));
		htmlEntities.put("ni", new String("8715"));
		htmlEntities.put("prod", new String("8719"));
		htmlEntities.put("sum", new String("8721"));
		htmlEntities.put("minus", new String("8722"));
		htmlEntities.put("lowast", new String("8727"));
		htmlEntities.put("radic", new String("8730"));
		htmlEntities.put("prop", new String("8733"));
		htmlEntities.put("infin", new String("8734"));
		htmlEntities.put("ang", new String("8736"));
		htmlEntities.put("and", new String("8743"));
		htmlEntities.put("or", new String("8744"));
		htmlEntities.put("cap", new String("8745"));
		htmlEntities.put("cup", new String("8746"));
		htmlEntities.put("int", new String("8747"));
		htmlEntities.put("there4", new String("8756"));
		htmlEntities.put("sim", new String("8764"));
		htmlEntities.put("cong", new String("8773"));
		htmlEntities.put("asymp", new String("8776"));
		htmlEntities.put("ne", new String("8800"));
		htmlEntities.put("equiv", new String("8801"));
		htmlEntities.put("le", new String("8804"));
		htmlEntities.put("ge", new String("8805"));
		htmlEntities.put("sub", new String("8834"));
		htmlEntities.put("sup", new String("8835"));
		htmlEntities.put("nsub", new String("8836"));
		htmlEntities.put("sube", new String("8838"));
		htmlEntities.put("supe", new String("8839"));
		htmlEntities.put("oplus", new String("8853"));
		htmlEntities.put("otimes", new String("8855"));
		htmlEntities.put("perp", new String("8869"));
		htmlEntities.put("sdot", new String("8901"));
		htmlEntities.put("lceil", new String("8968"));
		htmlEntities.put("rceil", new String("8969"));
		htmlEntities.put("lfloor", new String("8970"));
		htmlEntities.put("rfloor", new String("8971"));
		htmlEntities.put("lang", new String("9001"));
		htmlEntities.put("rang", new String("9002"));
		htmlEntities.put("loz", new String("9674"));
		htmlEntities.put("spades", new String("9824"));
		htmlEntities.put("clubs", new String("9827"));
		htmlEntities.put("hearts", new String("9829"));
		htmlEntities.put("diams", new String("9830"));
		htmlEntities.put("quot", new String("34"));
		htmlEntities.put("amp", new String("38"));
		htmlEntities.put("lt", new String("60"));
		htmlEntities.put("gt", new String("62"));
		htmlEntities.put("OElig", new String("338"));
		htmlEntities.put("oelig", new String("339"));
		htmlEntities.put("Scaron", new String("352"));
		htmlEntities.put("scaron", new String("353"));
		htmlEntities.put("Yuml", new String("376"));
		htmlEntities.put("circ", new String("710"));
		htmlEntities.put("tilde", new String("732"));
		htmlEntities.put("ensp", new String("8194"));
		htmlEntities.put("emsp", new String("8195"));
		htmlEntities.put("thinsp", new String("8201"));
		htmlEntities.put("zwnj", new String("8204"));
		htmlEntities.put("zwj", new String("8205"));
		htmlEntities.put("lrm", new String("8206"));
		htmlEntities.put("rlm", new String("8207"));
		htmlEntities.put("ndash", new String("8211"));
		htmlEntities.put("mdash", new String("8212"));
		htmlEntities.put("lsquo", new String("8216"));
		htmlEntities.put("rsquo", new String("8217"));
		htmlEntities.put("sbquo", new String("8218"));
		htmlEntities.put("ldquo", new String("8220"));
		htmlEntities.put("rdquo", new String("8221"));
		htmlEntities.put("bdquo", new String("8222"));
		htmlEntities.put("dagger", new String("8224"));
		htmlEntities.put("Dagger", new String("8225"));
		htmlEntities.put("permil", new String("8240"));
		htmlEntities.put("lsaquo", new String("8249"));
		htmlEntities.put("rsaquo", new String("8250"));
		htmlEntities.put("euro", new String("8364"));
	}

	
	 /**
     * <p>Unescapes special characters in a <code>String</code>.</p>
     *
     * @param str The <code>String</code> to escape.
     * @return A un-escaped <code>String</code>.
     */
    public static String unescape(String str) {

        StringBuffer  buf          = null ;
        String        entityName   = null ;

        char          ch           = ' '  ;
        char          charAt1      = ' '  ;

        int           entityValue  = 0    ;

        buf = new StringBuffer(str.length());

        for (int i = 0, l = str.length(); i < l; ++i) {

            ch = str.charAt(i);

            if (ch == '&') {

                int semi = str.indexOf(';', i + 1);

                if (semi == -1) {
                    buf.append(ch);
                    continue;
                }

                entityName = str.substring(i + 1, semi);

                if (entityName.charAt(0) == '#') {
                    charAt1 = entityName.charAt(1);
                    if (charAt1 == 'x' || charAt1=='X') {
                        entityValue = Integer.valueOf(entityName.substring(2), 16).intValue();
                    } else {
                        entityValue = Integer.parseInt(entityName.substring(1));
                    }
                } else {
                	String entFromTable = (String)htmlEntities.get(entityName);
                	if(entFromTable != null) {
                		 entityValue = Integer.parseInt(entFromTable);
                	} else 
                    entityValue = -1;
                    
                } if (entityValue == -1) {
                    buf.append('&');
                    buf.append(entityName);
                    buf.append(';');
                } else {
                	//Log.trace("convertito nel char :"+(char) (entityValue));
                    buf.append((char) (entityValue));
                }

                i = semi;

            } else {
                buf.append(ch);
            }
        }
        return buf.toString();
    }
		
	
	/**
	 * 	FIX WP DOUBLE ENCODED AMPESAND
	 * @see http://blackberry.trac.wordpress.org/ticket/17
	 * 
	 * @param output
	 * @return
	 */
	public static String  fixWordPressDoubleEncodedAmpersand(String output) {
	 output = StringUtils.replaceAll(output, "&amp;amp;", "&amp;"); //FIX WP DOUBLE ENCODED AMPESAND;
	 output = StringUtils.replaceAll(output, "&amp;", "&"); 
	 return unescape(output);
	}
    
	
	/*	FIX WP DOUBLE ENCODED AMPESAND */
	//@see http://blackberry.trac.wordpress.org/ticket/17
	public static String  fixWordPressDoubleEncodedAmpersandOld(String xmlInput) {
		String output = StringUtils.replaceAll(xmlInput, "&amp;amp;", "&amp;"); //FIX WP DOUBLE ENCODED AMPESAND;
		output = StringUtils.replaceAll(output, "&amp;#038;", "&#038;");
		output = StringUtils.replaceAll(output, "&amp;#039;", "&#039;"); 
		return output;
		 /*
		 StringBuffer tempResult = new StringBuffer(output.length());
			
			int ampInd = output.indexOf("&amp;");
			int lastEnd = 0;
			int entityValue = 0;
			
			while (ampInd >= 0){
				int nextAmp = output.indexOf("&amp;", ampInd+5);
				int nextSemi = output.indexOf(";", ampInd+5);
				
				if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)){ //check if html entity
					String escape = output.substring(ampInd+5,nextSemi);
					
					char charAt1 = ' ';
					
					 if (escape.charAt(0) == '#') { //check numeric entity
		                 charAt1 = escape.charAt(1);
		                 if (charAt1 == 'x' || charAt1=='X') {
		                	 try {
		                		entityValue = Integer.valueOf(escape.substring(2), 16).intValue();
							} catch (NumberFormatException e) {
								entityValue = -1;
							}
		                 } else {
		                	 try {
		                		 entityValue = Integer.parseInt(escape.substring(1));
							} catch (NumberFormatException e) {
								entityValue = -1;
							}
		                 }
		             } else  { //check mnemonic entity table
		            	 
		            	 String entFromTable = (String)htmlEntities.get(escape);
		                	if(entFromTable != null) {
		                		 entityValue = Integer.parseInt(entFromTable);
		                	} else 
		                		entityValue = -1;
		             }
					 
					 tempResult.append(output.substring(lastEnd, ampInd)); //append all char before &amp;
									 
					if (entityValue == -1) { //se non hai trovato una entity definita 
						tempResult.append("&");
						tempResult.append(escape);
						tempResult.append(";");
					} else {
						Log.trace("convertito in char: "+(char) (entityValue));
						tempResult.append((char) (entityValue));
					}
					
					lastEnd = nextSemi + 1;
				}
				ampInd = nextAmp;
			}
			tempResult.append(output.substring(lastEnd));
			return unescape(tempResult.toString());*/
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
