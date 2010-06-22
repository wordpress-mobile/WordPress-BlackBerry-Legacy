package com.wordpress.utils.log;

import net.rim.device.api.system.EventLogger;

public class BlackberryEventLogAppender implements Appender {

    private final static long guid = 0x7b7f78a7b708bd7aL;
    private String appName;
    private boolean isOpen = false;

    /**
     * The default log level is INFO
     */
    private int level = Log.INFO;

    public BlackberryEventLogAppender(String appName) {
        this.appName = appName;
    }

    public void close() {
    	isOpen = false;
    }

    public void delete() {
    }

    public void open() {
    	try {
    		EventLogger.register(guid, appName + " ", EventLogger.VIEWER_STRING);
    		isOpen = true;
    	} catch (final Throwable t) {
    		System.out.println(">>> Exception by registering the event logger:\n"
    				+ ">>> Error message: " + t.getMessage() + "\n" + ">>> Short description: "
    				+ t.toString() + "\n");
    		t.printStackTrace();
    		isOpen = false;
    	}
    }

    public void writeLogMessage(String level, String msg) throws Exception {
    	if( !isOpen ) return;
    	
        StringBuffer message = new StringBuffer();
        message.append("[").append(level).append("] ").append(msg);
        msg = message.toString();

        try {
            EventLogger.logEvent(guid, msg.getBytes());
        } catch (final Throwable tt) {
            System.out.println(">>> Exception by registering the event logger:\n"
                    + ">>> Error message: " + tt.getMessage() + "\n"
                    + ">>> Short description: " + tt.toString() + "\n");
            tt.printStackTrace();
        }
    }

	public boolean isLogOpen() {
		return isOpen;
	}

	public int getLogLevel() {
		return level;
	}
	
    public void setLogLevel(int level) {
    	this.level = level;
    }

}
