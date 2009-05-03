package com.wordpress.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

public class FileUtils {
	
	
	 public static synchronized String readTxtFile(String fileName) {
		 String content="";

	        try {
	        	StringBuffer charBuff=new StringBuffer();

	            //The class name is the fully qualified package name followed by the actual name of this class

	            Class classs = Class.forName("com.wordpress.utils.FileUtils");
	            //to actually retrieve the resource prefix the name of the file with a "/"
	            InputStream is = classs.getResourceAsStream("/"+fileName);

	            //we now have an input stream. Create a reader and read out each character in the stream.
	            InputStreamReader isr = new InputStreamReader(is,"UTF-8"); //@see http://java.sun.com/docs/books/tutorial/i18n/text/stream.html
	            int ch;

	            while ((ch = isr.read()) > -1) {  
	                charBuff.append((char)ch);
	            }
	            content=charBuff.toString();
	        } catch(Exception ex) {
	            System.out.println("Error: " + ex.toString());
	        }
	        return content;
	    }
	 
	  public static void closeStream(final InputStream is) {
	    if (is != null) {
	      try {
	        is.close();
	      } catch (final IOException ignore) {
	      }
	    }
	  }

	  public static void closeStream(final OutputStream os) {
	    if (os != null) {
	      try {
	        os.close();
	      } catch (final IOException ignore) {
	      }
	    }
	  }

	  public static void closeReader(final Reader reader) {
	    if (reader != null) {
	      try {
	        reader.close();
	      } catch (final IOException ignore) {
	      }
	    }
	  }

	  public static void closeConnection(final Connection conn) {
	    if (conn != null) {
	      try {
	        conn.close();
	      } catch (final IOException ignore) {
	      }
	    }
	  }


	 
}
