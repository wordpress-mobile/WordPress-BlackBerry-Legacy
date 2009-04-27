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
	 
	 
	 
	 /**
	   * Read a file using JSR-75 API.
	   * 
	   * @param filename
	   *          fully-qualified file path following "file:///" qualifier
	   * @return file data
	   * @throws IOException
	   *           if an exception occurs
	   */
	  public static synchronized byte[] readFile(String filename) throws IOException {
	   System.out.println("Loading file:///" + filename);

	    FileConnection fconn = null;
	    InputStream is = null;
	    try {
	      fconn = (FileConnection) Connector.open("file:///" + filename, Connector.READ);
	      // commented to speed up
	      // if (!fconn.exists() || !fconn.canRead())
	      //   throw new Exception("File does not exist");

	      int sz = (int) fconn.fileSize();
	      byte[] result = new byte[sz];

	      is = fconn.openInputStream();

	      // multiple bytes
	      int ch = 0;
	      int rd = 0;
	      while ((rd != sz) && (ch != -1)) {
	        ch = is.read(result, rd, sz - rd);
	        if (ch > 0) {
	          rd += ch;
	        }
	      }

	      return result;
	    } finally {
	      closeStream(is);
	      closeConnection(fconn);
	    }
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
