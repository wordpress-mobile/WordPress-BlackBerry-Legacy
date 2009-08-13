package com.wordpress.utils.log;

import java.util.Vector;


public class Log {
    
    public static final int DISABLED = -1;
    public static final int ERROR = 0;
    public static final int INFO = 1;
    public static final int DEBUG = 2;
    public static final int TRACE = 3;
    private static 	Vector appenders = new Vector();
    private static int level = INFO;
    
    private Log(){
    }

    public static void initLog(int level){
        setDefaultLogLevel(level);
        if (level > Log.DISABLED) {
            writeLogMessage(level, "Logging Started","---------");
        }
    }
    
    public static void initLog(Appender appender, int level){
    	appenders.addElement(appender);
    	if(appender.isLogOpen() == false) //auto open log
    		appender.open();
        setDefaultLogLevel(level);
        if (level > Log.DISABLED) {
            writeLogMessage(level, "Logging Started","---------");
        }
    }
    
    public static void initLog(Appender appender){
    	appenders.addElement(appender);
    	if(appender.isLogOpen() == false) //auto open log
    		appender.open();
    }
    
    
    public static void setDefaultLogLevel(int newlevel) {
    	level = newlevel;
    }
    
    public static int getDefaultLogLevel() {
        return level;
    }
    
    public static void error(String msg) {
        writeLogMessage(ERROR, "ERROR", msg);
    }
    

    public static void error(Object obj, String msg) {
        String message = "["+ obj.getClass().getName() + "] " + msg;
        writeLogMessage(ERROR, "ERROR", message);
    }
    

    public static void info(String msg) {
        writeLogMessage(INFO, "INFO", msg);
    }
    

    public static void info(Object obj, String msg) {
    	String message = "[" + obj.getClass().getName() + "] " + msg;
        writeLogMessage(INFO, "INFO", message);
    }

    public static void debug(String msg) {
        writeLogMessage(DEBUG, "DEBUG", msg);
    }

    public static void debug(Object obj, String msg) {
        String message = "["+ obj.getClass().getName() + "] " +msg;
        writeLogMessage(DEBUG, "DEBUG", message);
    }
    
    public static void trace(String msg) {
        writeLogMessage(TRACE, "TRACE", msg);
    }

    public static void trace(Object obj, String msg) {
        String message = "["+ obj.getClass().getName() + "] " +msg;
        writeLogMessage(TRACE, "TRACE", message);
    }

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


	public static void addAppender(Appender appender) {
		appenders.addElement(appender);
	}


	public static boolean removeAppender(Appender appender) {
		return appenders.removeElement(appender);
	}

	public static void removeAllAppenders() {
		appenders.removeAllElements();
	}
}