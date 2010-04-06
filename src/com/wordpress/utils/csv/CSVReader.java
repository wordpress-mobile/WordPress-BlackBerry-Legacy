package com.wordpress.utils.csv;

/**
This CVS reader is based on code from opencsv-2.1 -


Copyright 2005 Bytecode Pty Ltd.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import com.wordpress.utils.log.Log;

import net.rim.device.api.io.LineReader;



/**
 * A very simple CSV reader released under a commercial-friendly license.
 * 
 * @author Glen Smith
 * 
 */
public class CSVReader {

    private LineReader br;

    private boolean hasNext = true;

    private CSVParser parser;
    
    private int skipLines;

    private boolean linesSkiped;

    /**
     * The default line to start reading.
     */
    public static final int DEFAULT_SKIP_LINES = 0;

    /**
     * Constructs CSVReader using a comma for the separator.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     */
    public CSVReader(InputStream reader) {
        this(reader, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * Constructs CSVReader with supplied separator.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries.
     */
    public CSVReader(InputStream reader, char separator) {
        this(reader, separator, CSVParser.DEFAULT_QUOTE_CHARACTER, CSVParser.DEFAULT_ESCAPE_CHARACTER);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     */
    public CSVReader(InputStream reader, char separator, char quotechar) {
        this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES);
    }

    /**
     * Constructs CSVReader with supplied separator, quote char and quote handling
     * behavior.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param strictQuotes
     *            sets if characters outside the quotes are ignored
     */
    public CSVReader(InputStream reader, char separator, char quotechar, boolean strictQuotes) {
        this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, DEFAULT_SKIP_LINES, strictQuotes);
    }

   /**
     * Constructs CSVReader with supplied separator and quote char.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     */

    public CSVReader(InputStream reader, char separator,
			char quotechar, char escape) {
        this(reader, separator, quotechar, escape, DEFAULT_SKIP_LINES, CSVParser.DEFAULT_STRICT_QUOTES);
	}
    
    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param line
     *            the line number to skip for start reading 
     */
    public CSVReader(InputStream reader, char separator, char quotechar, int line) {
        this(reader, separator, quotechar, CSVParser.DEFAULT_ESCAPE_CHARACTER, line, CSVParser.DEFAULT_STRICT_QUOTES);
    }

    /**
     * Constructs CSVReader with supplied separator and quote char.
     *
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     * @param line
     *            the line number to skip for start reading
     */
    public CSVReader(InputStream reader, char separator, char quotechar, char escape, int line) {
        this(reader, separator, quotechar, escape, line, CSVParser.DEFAULT_STRICT_QUOTES);
    }
    
    /**
     * Constructs CSVReader with supplied separator and quote char.
     * 
     * @param reader
     *            the reader to an underlying CSV source.
     * @param separator
     *            the delimiter to use for separating entries
     * @param quotechar
     *            the character to use for quoted elements
     * @param escape
     *            the character to use for escaping a separator or quote
     * @param line
     *            the line number to skip for start reading
     * @param strictQuotes
     *            sets if characters outside the quotes are ignored
     */
    public CSVReader(InputStream reader, char separator, char quotechar, char escape, int line, boolean strictQuotes) {
        this.br = new LineReader(reader);
        this.parser = new CSVParser(separator, quotechar, escape, strictQuotes);
        this.skipLines = line;
    }


    /**
     * Reads the next line from the buffer and converts to a string array.
     * 
     * @return a string array with each comma-separated element as a separate
     *         entry.
     * 
     * @throws IOException
     *             if bad things happen during the read
     */
    public String[] readNext() throws IOException {    	
    	String[] result = null;
    	do {
    		String nextLine = getNextLine();
    		if (!hasNext) {
    			return result; // should throw if still pending?
    		}
    		String[] r = parser.parseLineMulti(nextLine);
    		if (r.length > 0) {
    			if (result == null) {
    				result = r;
    			} else {
    				String[] t = new String[result.length+r.length];
    				System.arraycopy(result, 0, t, 0, result.length);
    				System.arraycopy(r, 0, t, result.length, r.length);
    				result = t;
    			}
    		}
    	} while (parser.isPending());
    	return result;
    }

    /**
     * Reads the next line from the file.
     * 
     * @return the next line from the file without trailing newline
     * @throws IOException
     *             if bad things happen during the read
     */
    private String getNextLine() throws IOException {

    	try {
    		if (!this.linesSkiped) {
    			for (int i = 0; i < skipLines; i++) {
    				br.readLine();
    			}
    			this.linesSkiped = true;
    		}

    		String nextLine = new String(br.readLine());
    		if (nextLine == null) {
    			hasNext = false;
    		} 

    		return hasNext ? nextLine : null;
    	} catch(EOFException eof) {
    		Log.trace("Reached end of CVS file");
    		hasNext = false;
    		return null;
    	} 
    }    
}


class CSVParser {

   private final char separator;

   private final char quotechar;
   
   private final char escape;

   private final boolean strictQuotes;

   private String pending;
   
   /** The default separator to use if none is supplied to the constructor. */
   public static final char DEFAULT_SEPARATOR = ',';

   public static final int INITIAL_READ_SIZE = 128;

   /**
    * The default quote character to use if none is supplied to the
    * constructor.
    */
   public static final char DEFAULT_QUOTE_CHARACTER = '"';
   

   /**
    * The default escape character to use if none is supplied to the
    * constructor.
    */
   public static final char DEFAULT_ESCAPE_CHARACTER = '\\';

  /**
   * The default strict quote behavior to use if none is supplied to the
   * constructor
   */
   public static final boolean DEFAULT_STRICT_QUOTES = false;
   
   /**
    * Constructs CSVParser using a comma for the separator.
    */
   public CSVParser() {
       this(DEFAULT_SEPARATOR, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
   }

   /**
    * Constructs CSVParser with supplied separator.
    * @param separator
    *            the delimiter to use for separating entries.
    */
   public CSVParser(char separator) {
       this(separator, DEFAULT_QUOTE_CHARACTER, DEFAULT_ESCAPE_CHARACTER);
   }
   
   

   /**
    * Constructs CSVParser with supplied separator and quote char.
    * @param separator
    *            the delimiter to use for separating entries
    * @param quotechar
    *            the character to use for quoted elements
    */
   public CSVParser(char separator, char quotechar) {
       this(separator, quotechar, DEFAULT_ESCAPE_CHARACTER);
   }

   /**
    * Constructs CSVReader with supplied separator and quote char.
    * @param separator
    *            the delimiter to use for separating entries
    * @param quotechar
    *            the character to use for quoted elements
    * @param escape
    *            the character to use for escaping a separator or quote
    */
   public CSVParser(char separator, char quotechar, char escape) {
       this(separator, quotechar, escape, DEFAULT_STRICT_QUOTES);
   }

   /**
    * Constructs CSVReader with supplied separator and quote char.
    * Allows setting the "strict quotes" flag
    * @param separator
    *            the delimiter to use for separating entries
    * @param quotechar
    *            the character to use for quoted elements
    * @param escape
    *            the character to use for escaping a separator or quote
    * @param strictQuotes
    *            if true, characters outside the quotes are ignored
    */
   public CSVParser(char separator, char quotechar, char escape, boolean strictQuotes) {
       this.separator = separator;
       this.quotechar = quotechar;
       this.escape = escape;
       this.strictQuotes = strictQuotes;
   }
   
   /**
    * 
    * @return true if something was left over from last call(s)
    */
   public boolean isPending() {
   	return pending != null;
   }

   public String[] parseLineMulti(String nextLine) throws IOException {
   	return parseLine(nextLine, true);
   }
   
   public String[]  parseLine(String nextLine) throws IOException {
   	return parseLine(nextLine, false);
   }
   /**
    * Parses an incoming String and returns an array of elements.
    * 
    * @param nextLine
    *            the string to parse
    * @param multi
    * @return the comma-tokenized list of elements, or null if nextLine is null
    * @throws IOException if bad things happen during the read
    */
   private String[] parseLine(String nextLine, boolean multi) throws IOException {

   	if (!multi && pending != null) {
   		pending = null;
   	}
   	
       if (nextLine == null) {
       	if (pending != null) {
       		String s = pending;
       		pending = null;
       		return new String[] {s};
       	} else {
       		return null;
       	}
       }

       Vector tokensOnThisLine = new Vector();
       StringBuffer sb = new StringBuffer(INITIAL_READ_SIZE);
       boolean inQuotes = false;
       if (pending != null) {
       	sb.append(pending);
       	pending = null;
       	inQuotes = true;
       }
       for (int i = 0; i < nextLine.length(); i++) {
       	
       	char c = nextLine.charAt(i);
       	if (c == this.escape) {
       		if( isNextCharacterEscapable(nextLine, inQuotes, i) ){
       			sb.append(nextLine.charAt(i+1));
       			i++;
       		} 
       	} else if (c == quotechar) {
       		if( isNextCharacterEscapedQuote(nextLine, inQuotes, i) ){
       			sb.append(nextLine.charAt(i+1));
       			i++;
       		}else{
       			inQuotes = !inQuotes;
       			// the tricky case of an embedded quote in the middle: a,bc"d"ef,g
                   if (!strictQuotes) {
                       if(i>2 //not on the beginning of the line
                               && nextLine.charAt(i-1) != this.separator //not at the beginning of an escape sequence
                               && nextLine.length()>(i+1) &&
                               nextLine.charAt(i+1) != this.separator //not at the	end of an escape sequence
                       ){
                           sb.append(c);
                       }
                   }
       		}
       	} else if (c == separator && !inQuotes) {
       		tokensOnThisLine.addElement(sb.toString());
       		sb = new StringBuffer(INITIAL_READ_SIZE); // start work on next token
       	} else {
               if (!strictQuotes || inQuotes)
                   sb.append(c);
       	}
       }
       // line is done - check status
       if (inQuotes) {
       	if (multi) {
       		// continuing a quoted section, re-append newline
       		sb.append("\n");
       		pending = sb.toString();
       		sb = null; // this partial content is not to be added to field list yet
       	} else {
       		throw new IOException("Un-terminated quoted field at end of CSV line");
       	}
       }
       if (sb != null) {
       	tokensOnThisLine.addElement(sb.toString());
       }
       String[] arr = new String[tokensOnThisLine.size()];
       tokensOnThisLine.copyInto(arr);
       return arr;
   }
   
   /**  
    * precondition: the current character is a quote or an escape
	 * @param nextLine the current line
	 * @param inQuotes true if the current context is quoted
	 * @param i current index in line
	 * @return true if the following character is a quote
	 */
	private boolean isNextCharacterEscapedQuote(String nextLine, boolean inQuotes, int i) {
		return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
		    && nextLine.length() > (i+1)  // there is indeed another character to check.
		    && nextLine.charAt(i+1) == quotechar;
	}

	/**  
	 * precondition: the current character is an escape
	 * @param nextLine the current line
	 * @param inQuotes true if the current context is quoted
	 * @param i current index in line
	 * @return true if the following character is a quote
	 */
	protected boolean isNextCharacterEscapable(String nextLine, boolean inQuotes, int i) {
		return inQuotes  // we are in quotes, therefore there can be escaped quotes in here.
		    && nextLine.length() > (i+1)  // there is indeed another character to check.
		    && ( nextLine.charAt(i+1) == quotechar || nextLine.charAt(i+1) == this.escape);
	}
}
