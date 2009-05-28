package com.wordpress.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import com.wordpress.utils.Tools;


/**
 * This class includes implementation for reading files using JSR-75.
 * 
 */
public class JSR75FileSystem  {

/*
 * The BlackBerry Pearl 8100 smartphone was the first BlackBerry smartphone to support microSD cards... 
 */
  public boolean hasMicroSD(){
	  String root = null;
	  Enumeration e = FileSystemRegistry.listRoots();
	  while (e.hasMoreElements()) {
	       root = (String) e.nextElement();
	       if( root.equalsIgnoreCase("sdcard/") ) {
	         return true;
	       } else if( root.equalsIgnoreCase("store/") ) {
	          //internal memory identifier
	       }
	  }
	  return false;
  }
  
  
  public static synchronized boolean isFileExist(String filePath) throws IOException {
		FileConnection filecon = null;
		try {
			filecon = (FileConnection) Connector.open(filePath);
			if (!filecon.exists()) {
				return false;
			} else {
				return true;
			}
		} finally {
			FileUtils.closeConnection(filecon);
		}
	}

  public static DataOutputStream getDataOutputStream(String filePath) throws IOException{
		FileConnection filecon = (FileConnection) Connector.open(filePath);
		if (!filecon.exists()) {
			throw new IOException("File not exist!");
		}
		return filecon.openDataOutputStream();
	}
  
  public static DataInputStream getDataInputStream(String filePath) throws IOException{
		FileConnection filecon = (FileConnection) Connector.open(filePath);
		if (!filecon.exists()) {
			throw new IOException("File not exist!");
		}
		return filecon.openDataInputStream();
	}
  
  public static synchronized void createDir(String filePath) throws IOException{
	   System.out.println("createFile: " + filePath);
		FileConnection filecon = (FileConnection) Connector.open(filePath);
		if (!filecon.exists()) {
			filecon.mkdir();
			System.out.println("file creato con successo");
		}
		filecon.close();
	}

  public static synchronized void createFile(String filePath) throws IOException{
		FileConnection filecon = (FileConnection) Connector.open(filePath);
		if (!filecon.exists()) {
			filecon.create();
			System.out.println("file creato con successo: " + filePath);
		} else 
			System.out.println("file gia presente : " + filePath);
	
		filecon.close();
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
    System.out.println("Start Loading file:///" + filename);

    FileConnection fconn = null;
    InputStream is = null;
    try {
     if(!filename.startsWith("file:///")) {
       fconn = (FileConnection) Connector.open("file:///" + filename, Connector.READ);
     } else {
    	 fconn = (FileConnection) Connector.open(filename, Connector.READ);
     }
      // commented to speed up
     //  if (!fconn.exists() || !fconn.canRead())
     //    throw new IOException("File does not exist");

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
      System.out.println("End Loading file:///" + filename);
      FileUtils.closeStream(is);
      FileUtils.closeConnection(fconn);
    }
  }

  

  /**
   * List all roots in the filesystem
   * 
   * @return a vector containing all the roots
   * @see com.nutiteq.utils.fs.FileSystem#getRoots()
   */
  public static synchronized  String[] getRoots() {
    final Vector v = new Vector();

    // list roots
    final Enumeration en = FileSystemRegistry.listRoots();

    // enumerate
    while (en.hasMoreElements()) {
      String root = (String) en.nextElement();
      if (!root.endsWith("/")) {
        root += '/';
      }
      v.addElement(root);
    }

    return Tools.toStringArray(v);
  }

  /**
   * List all files in a directory.
   * 
   * @param path
   *          path to list, null to list root
   * @return a vector of file names
   */
  public static synchronized  String[] listFiles(final String path) throws IOException {
    if (path == null || path.length() == 0) {
      return getRoots();
    }

    // open directory
    final Vector v = new Vector();
    FileConnection fconn = null;
    try {
      fconn = (FileConnection) Connector.open(path, Connector.READ);
     // v.addElement("../");
      final Enumeration en = fconn.list();
      while (en.hasMoreElements()) {
        String filename = (String) en.nextElement();
        // convert absolute to relative path
        int pos = filename.length() - 2;
        while (pos >= 0 && filename.charAt(pos) != '/') {
          pos--;
        }
        if (pos >= 0) {
          filename = filename.substring(pos + 1);
        }

        v.addElement(filename);
      }
    } finally {
      if (fconn != null) {
        fconn.close();
      }
    }

    return  Tools.toStringArray(v);
  }

  /**
   * Check if a file is a directory
   * 
   * @param filename
   *          file to check
   * @return true if it is a directory
 * @throws IOException 
 * @throws IOException 
   */
  public static synchronized boolean isDirectory(String filename) throws IOException {
		FileConnection fc = null;
		try {
			fc = (FileConnection) Connector.open(filename, Connector.READ);
			if (fc.isDirectory()) {
				return true;
			} else {
				return false;
			}
		} finally {
			FileUtils.closeConnection(fc);
		}
	}

  
  public static synchronized void removeFile(String url) throws IOException {
		FileConnection fc = (FileConnection) Connector.open(url);
		try {
			if (!isFileExist(url))
				return;

			if (isDirectory(url)) {
				final Enumeration en = fc.list();
				while (en.hasMoreElements()) {
					String filename =url+ (String) en.nextElement();
					removeFile(filename);
				}
			}
			
			fc.delete();
		} finally {
			fc.close();
		}
	}
}