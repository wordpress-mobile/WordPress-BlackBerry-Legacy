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

import net.rim.device.api.system.DeviceInfo;

import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;


/**
 * This class includes implementation for reading files using JSR-75.
 * 
 */
public class JSR75FileSystem  {

/*
 * The BlackBerry Pearl 8100 smartphone was the first BlackBerry smartphone to support microSD cards... 
 */
  public static boolean hasMicroSD(){
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
  
/*
 * An API did not exist to determine if the BlackBerry smartphone could support microSD regardless 
 * of whether or not a card was inserted. However, the BlackBerry smartphone model number can be 
 * used to determine microSD card support,  as in the following code, since the list of BlackBerry 
 * smartphones that support this feature is finite.
 * 
 */
  public static boolean supportMicroSD(){
	  String modelNum = DeviceInfo.getDeviceName();
	  if ((modelNum.startsWith("8") && !modelNum.startsWith("87")) || modelNum.startsWith("9")) {
		  //microSD card supported
		  return true;
	  } else {
		  return false;
	  }
  }
  
  /** 
   * Renames this File to the name represented by the File dest. This works
   * for both normal files and directories.
   *
   * @param newName - the File containing the new name. 
   * @return true if the File was renamed, false otherwise.
   */
  public static synchronized void rename(String oldName, String newName) throws IOException {
	  FileConnection filecon = null;
		try {
			filecon = (FileConnection) Connector.open(oldName);
			filecon.rename(newName);
		} finally {
			FileUtils.closeConnection(filecon);
		}
  }
  
  public static synchronized long getFileSize(String filePath) throws IOException {
		FileConnection filecon = null;
		try {
			filecon = (FileConnection) Connector.open(filePath);
			if (!filecon.exists()) {
				return 0;
			} else {
				return filecon.fileSize();
			}
		} finally {
			FileUtils.closeConnection(filecon);
		}
	}
  
  public static synchronized boolean isFileExist(String filePath) throws IOException {
		FileConnection filecon = null;
		try {
		     if(!filePath.startsWith("file:///")) {
		         filecon = (FileConnection) Connector.open("file:///"+ filePath);
		       } else {
		    	   filecon = (FileConnection) Connector.open(filePath);
		       }
			if (!filecon.exists()) {
				return false;
			} else {
				return true;
			}
		} finally {
			FileUtils.closeConnection(filecon);
		}
	}
  
  public static synchronized boolean isReadable(String filePath) throws IOException {
		FileConnection filecon = null;
		try {
		     if(!filePath.startsWith("file:///")) {
		         filecon = (FileConnection) Connector.open("file:///"+ filePath, Connector.READ);
		       } else {
		    	   filecon = (FileConnection) Connector.open(filePath, Connector.READ);
		       }
			if (!filecon.exists() || !filecon.canRead()) {
				return false;
			} else {
				return true;
			}
		} finally {
			FileUtils.closeConnection(filecon);
		}
	}

  /*
   *  This method opens an output stream positioned at the start of the file.
   *   Data written to the returned output stream overwrites any existing data until EOF is reached, 
   *   and then additional data is appended.  
   */
  public static synchronized void write(String filePath, byte[] content) throws IOException{
		FileConnection filecon = (FileConnection) Connector.open(filePath);
		if (!filecon.exists()) {
			filecon.create();
			Log.debug("File successfully created: " + filePath);
		} else {
			Log.trace("File already available: " + filePath);			
		}
		
		if (!filecon.exists()) {
			throw new IOException("File does not exist: " + filePath);
		}
		DataOutputStream dataOutputStream = filecon.openDataOutputStream();
		dataOutputStream.write(content);
		dataOutputStream.flush();
		dataOutputStream.close();
		filecon.close();
	}
  
  public static synchronized FileConnection openFile(String filePath) throws IOException{
		FileConnection filecon = (FileConnection) Connector.open(filePath);
		if (!filecon.exists()) {
			throw new IOException("File does not exist: " + filePath);
		}
		return filecon;
	}
  
  public static synchronized void createDir(String filePath) throws IOException{
		FileConnection filecon = (FileConnection) Connector.open(filePath);
		if (!filecon.exists()) {
			filecon.mkdir();
			Log.debug("Dir successfully created: " + filePath);
		}
		filecon.close();
	}

  public static synchronized void createFile(String filePath) throws IOException{
		FileConnection filecon = (FileConnection) Connector.open(filePath);
		if (!filecon.exists()) {
			filecon.create();
			Log.debug("File successfully created: " + filePath);
		} else 
			Log.trace("File already created: " + filePath);
	
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
   Log.debug("Start Loading file: " + filename);

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
      Log.debug("End Loading file: " + filename);
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