package com.wordpress.utils.log;

import java.util.Vector;

/**
 * Generic Log class
 */
public class Log {
    
    //---------------------------------------------------------------- Constants
    
    /**
     * Log level DISABLED: used to speed up applications using logging features
     */
    public static final int DISABLED = -1;
    
    /**
     * Log level ERROR: used to log error messages.
     */
    public static final int ERROR = 0;
    
    /**
     * Log level INFO: used to log information messages.
     */
    public static final int INFO = 1;
    
    /**
     * Log level DEBUG: used to log debug messages.
     */
    public static final int DEBUG = 2;
    
    /**
     * Log level TRACE: used to trace the program execution.
     */
    public static final int TRACE = 3;
    
    //---------------------------------------------------------------- Variables

    private static 	Vector appenders = new Vector();
    
    /**
     * The default log level is INFO
     */
    private static int level = INFO;
    
    
    //------------------------------------------------------------- Constructors
    /**
     * This class is static and cannot be instantiated
     */
    private Log(){
    }
    
    //----------------------------------------------------------- Public methods
    /**
     * Initialize log file with a specific log level.
     * With this implementation of initLog the initialization is skipped.
     *
     * @param object the appender object that write log file
     * @param level the log level
     */
    public static void initLog(int level){
        setDefaultLogLevel(level);
        if (level > Log.DISABLED) {
            writeLogMessage(level, "INITLOG","---------");
        }
    }
    
    /**
     * Initialize log file with a specific log level.
     * With this implementation of initLog the initialization is skipped.
     *
     * @param object the appender object that write log file
     * @param level the log level
     */
    public static void initLog(Appender appender, int level){
    	appenders.addElement(appender);
    	if(appender.isLogOpen() == false) //auto open log
    		appender.open();
        setDefaultLogLevel(level);
        if (level > Log.DISABLED) {
            writeLogMessage(level, "INITLOG","---------");
        }
    }
    
    /**
     * Ititialize log file
     * @param object the appender object that write log file
     */
    public static void initLog(Appender appender){
    	appenders.addElement(appender);
    	if(appender.isLogOpen() == false) //auto open log
    		appender.open();
    }
    
    
    /**
     * Accessor method to define log level:
     * @param newlevel log level to be set
     */
    public static void setDefaultLogLevel(int newlevel) {
    	level = newlevel;
    }
    
    /**
     * Accessor method to retrieve log level:
     * @return actual log level
     */
    public static int getDefaultLogLevel() {
        return level;
    }
    
    /**
     * ERROR: Error message
     * @param msg the message to be logged
     */
    public static void error(String msg) {
        writeLogMessage(ERROR, "ERROR", msg);
    }
    
    /**
     * ERROR: Error message
     * @param msg the message to be logged
     * @param obj the object that send error message
     */
    public static void error(Object obj, String msg) {
        String message = "["+ obj.getClass().getName() + "] " + msg;
        writeLogMessage(ERROR, "ERROR", message);
    }
    
    /**
     * INFO: Information message
     * @param msg the message to be logged
     */
    public static void info(String msg) {
        writeLogMessage(INFO, "INFO", msg);
    }
    
    /**
     * INFO: Information message
     * @param msg the message to be logged
     * @param obj the object that send log message
     */
    public static void info(Object obj, String msg) {
        writeLogMessage(INFO, "INFO", msg);
    }
    
    
    
    /**
     * DEBUG: Debug message
     * @param msg the message to be logged
     */
    public static void debug(String msg) {
        writeLogMessage(DEBUG, "DEBUG", msg);
    }
    
    /**
     * DEBUG: Information message
     * @param msg the message to be logged
     * @param obj the object that send log message
     */
    public static void debug(Object obj, String msg) {
        String message = "["+ obj.getClass().getName() + "] " +msg;
        writeLogMessage(DEBUG, "DEBUG", message);
    }
    
    /**
     * TRACE: Debugger mode
     */
    public static void trace(String msg) {
        writeLogMessage(TRACE, "TRACE", msg);
    }
    
    /**
     * TRACE: Information message
     * @param msg the message to be logged
     * @param obj the object that send log message
     */
    public static void trace(Object obj, String msg) {
        String message = "["+ obj.getClass().getName() + "] " +msg;
        writeLogMessage(TRACE, "TRACE", message);
    }
    
    
    /*
	public static void writeLogMessage(String level, String msg) throws Exception {
		for (int i = 0; i < appenders.size(); i++) {
			((Appender) appenders.elementAt(i)).writeLogMessage(level, msg);
		}
	}
    */

    private static void writeLogMessage(int msgLevel, String levelMsg, String msg) {
        
        if (level >= msgLevel) {
            try {
            	for (int i = 0; i < appenders.size(); i++) {
            		Appender currentAppender = ((Appender) appenders.elementAt(i));
        			if(currentAppender.getLogLevel() >= msgLevel )
        				currentAppender.writeLogMessage(levelMsg, msg);
        		}
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }


	/**
	 * ad an appender to the list of appenders
	 * 
	 * @param appender
	 */
	public static void addAppender(Appender appender) {
		appenders.addElement(appender);
	}

	/**
	 * remove given appender if present
	 * 
	 * @param appender
	 * @return true if appender has been found and removed
	 */
	public static boolean removeAppender(Appender appender) {
		return appenders.removeElement(appender);
	}

	/**
	 * remove all the appenders
	 */
	public static void removeAllAppenders() {
		appenders.removeAllElements();
	}
	
	
}