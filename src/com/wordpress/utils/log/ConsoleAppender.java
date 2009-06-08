package com.wordpress.utils.log;

import java.util.Date;

import javax.microedition.global.Formatter;

import com.wordpress.utils.CalendarUtils;

/**
 * Default debugger to be used instead of System.out.println
 */
public class ConsoleAppender implements Appender {

	/**
	 * Default constructor
	 */
	public ConsoleAppender() {
	}

	// ----------------------------------------------------------- Public
	// Methods
	/**
	 * ConsoleAppender writes one message on the standard output
	 */
	public void writeLogMessage(String level, String msg) {
		System.out.print(CalendarUtils.dateToUTC(new Date()));
		System.out.print(" [" + level + "] ");
		System.out.println(msg);
	}

	/**
	 * ConsoleAppender doesn't implement this method
	 */
	public void open() {
	}

	/**
	 * ConsoleAppender doesn't implement this method
	 */
	public void close() {
	}

	/**
	 * ConsoleAppender doesn't implement this method
	 */
	public void delete() {
	}

	/**
	 * Perform additional actions needed when setting a new level.
	 * ConsoleAppender doesn't implement this method
	 */
	public void setLogLevel(int level) {
	
	}

	public boolean isLogOpen() {
		return true;
	}

	public int getLogLevel() {
		return Log.TRACE;
	}
}
