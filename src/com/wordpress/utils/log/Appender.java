package com.wordpress.utils.log;

import java.io.IOException;

public interface Appender {
      	
    /**
     *Open the log. The consequence is that the logging is enabled.
     */
    void open();
    
    
	/**
	 * Check if the log is open.
	 * 
	 * @return true if the log is open, false otherwise.
	 */
	boolean isLogOpen();
    
	/**
	 * Close the log. The consequence is that the logging is disabled until the
	 * log is opened. The logging could be enabled by calling
	 * <code>open()</code>.
	 * 
	 * @throws IOException
	 *             if the close failed.
	 */
    void close() throws IOException;
    
    /**
     * Delete Log file
     */
    void delete();
    
    /**
     * Perform additional actions needed when setting a new level.
     */
    public void setLogLevel(int level);

    
    /**
     * get the current log level.
     */
    public int getLogLevel();
    
    
    /**
     * Append a message to the Log file
     */
    void writeLogMessage(String level, String msg) throws Exception;
}