package com.wordpress.utils.log;

import java.io.OutputStream;
import java.util.Date;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.wordpress.io.JSR75FileSystem;
import com.wordpress.utils.CalendarUtils;


/**
 * A class that logs to a file. The class uses the FileConnection API from
 * JSR-75.
 * The default directory for storing files is set in the constructor.
 * 
 * The appender tracks the file size and if it exceeds a given maximum size
 * (customizable by clients) then the current log file is renamed appending a
 * .old to the log name and a new one is created. Therefore the maximum size
 * on this is about 2 times the maxFileSize (this is not accurate as there is
 * no limit on the size of the single message printed).
 * 
 */

public class FileAppender implements Appender {

    private String fileUrl   = "file:///root1/wplog.txt";
    private String oldSuffix = ".sav.txt";
    private String lineSeparator = "\r\n";
    private OutputStream os;
    private FileConnection file;
    private long maxFileSize = 512 * 1024;
    
    /**
     * The default log level is INFO
     */
    private int level = Log.INFO;

    /**
     * Default constructor
     */
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

    /**
     * Sets the maximum file size. Once this is size is reached, the current log
     * file is renamed and a new one is created. This way we have at most 2 log
     * files whose size is (roughly) bound to maxFileSize.
     * The minimum file size is 1024 as smaller size does not really make sense.
     * If a client needs smaller files it should probably the usage of other
     * Appenders or modify the behavior of this one by deriving it.
     *
     * @param maxFileSize the max size in bytes
     */
    public void setMaxFileSize(long maxFileSize) {
        if (maxFileSize > 1024) {
            this.maxFileSize = maxFileSize;
        }
    }

    //----------------------------------------------------------- Public Methods
    /**
     * FileAppender writes one message to the output file
     */
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

                // If the file grows beyond the limit, we rename it and create a new one
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
            // We try to close and reopen the log file. The message being logged
            // is lost. We don't try to reopen it and get into an infinite
            // recursion.
            try {
                file.close();
            } catch (Exception e1) {
                // We cannot even close the file, too bad. Logging maybe disabled
                // at this point. Nevertheless we try to reopen the file
            } finally {
                open();
            }
        }
    }

    /**
     * Init the logger
     */
    public void open() {
        try {
        	
        	if (!JSR75FileSystem.isFileExist(fileUrl))
        		JSR75FileSystem.createFile(fileUrl);
        	
        	file = (FileConnection) Connector.open(fileUrl);
            os = file.openOutputStream();
        } catch (Exception e) {
            System.out.println("Cannot open or create file at: " + fileUrl);
            e.printStackTrace();
        }
    }

    /**
     * Close connection and streams
     */
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

    /**
     * Delete the log file
     */
    public void delete() {
        try {
        	JSR75FileSystem.removeFile(fileUrl);
        } catch (Exception e) {
            System.out.println("Cannot open or create file at: " + fileUrl);
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
