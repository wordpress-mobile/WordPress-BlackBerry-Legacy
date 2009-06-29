package com.wordpress.utils.log;

import java.util.Date;


import com.wordpress.utils.CalendarUtils;

public class ConsoleAppender implements Appender {

	public ConsoleAppender() {

	}

	public void writeLogMessage(String level, String msg) {
		System.out.print(CalendarUtils.dateToUTC(new Date()));
		System.out.print(" [" + level + "] ");
		System.out.println(msg);
	}

	public void open() {
	}

	public void close() {
	}

	public void delete() {
	}

	public void setLogLevel(int level) {
	
	}

	public boolean isLogOpen() {
		return true;
	}

	public int getLogLevel() {
		return Log.TRACE;
	}
}
