package com.wordpress.utils.log;

import java.io.OutputStream;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.wordpress.io.JSR75FileSystem;
import com.wordpress.utils.CalendarUtils;


/**
 * The class uses the FileConnection API from JSR-75 for logging into file
 * 
 */

public class FileAppender implements Appender {

    private String fileUrl   = "";
    private String oldSuffix = ".bak.txt";
    private String lineSeparator = "\r\n";
    private OutputStream os;
    private FileConnection file;
    private long maxFileSize = 512 * 1024;
    
    private int level = Log.INFO;

    public FileAppender(String path, String fileName) {
        if (path != null && fileName != null) {
            if (path.endsWith("/")) {
                this.fileUrl = path + fileName;
            } else {
                this.fileUrl = path + "/" + fileName;
            }
        }
        os = null;
    }

    synchronized public void writeLogMessage(String level, String msg) {
        String levelMsg = " [" + level + "] ";
        try {
            if (os != null) {
                StringBuffer logMsg = new StringBuffer(CalendarUtils.dateToUTC(new Date()));
                logMsg.append(levelMsg);
                logMsg.append(msg);
                logMsg.append(lineSeparator);
                os.write(logMsg.toString().getBytes());
                os.flush();
                
                if (file.fileSize()> maxFileSize) {
                    try {
                        String oldFileName = fileUrl + oldSuffix;
                        JSR75FileSystem.removeFile(oldFileName); //remove old stored log file
                        file.rename(oldFileName); //rename the current log file to the stored
                        file.close();
                        // Reopen the file
                        open();
                    } catch (Exception ioe) {
                        System.out.println("Exception while renaming " + ioe);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception while logging. " + e);
            e.printStackTrace();
            
            try {
                file.close();
            } catch (Exception e1) {

            } finally {
                open();
            }
        }
    }

    public void open() {
        try {
        	
        	if (!JSR75FileSystem.isFileExist(fileUrl))
        		JSR75FileSystem.createFile(fileUrl);
        	
        	file = (FileConnection) Connector.open(fileUrl);
            os = file.openOutputStream();
        } catch (Exception e) {
            System.out.println("Cannot open or create file: " + fileUrl);
            e.printStackTrace();
        }
    }


    public void close() {

        try {
            if (os != null) {
                os.close();
            }
            if (file != null) {
                file.close();
            
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void delete() {
        try {
        	JSR75FileSystem.removeFile(fileUrl);
        } catch (Exception e) {
            System.out.println("Cannot open or create file: " + fileUrl);
            e.printStackTrace();
        }
    }

    public void setLogLevel(int level) {
    	this.level = level;    	
    }
	public int getLogLevel() {
		return level;
	}

	public boolean isLogOpen() {
		return true;
	}

}
