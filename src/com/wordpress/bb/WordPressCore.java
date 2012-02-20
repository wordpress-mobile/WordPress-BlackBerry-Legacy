package com.wordpress.bb;
import java.io.IOException;
import java.util.Timer;
import java.util.Vector;

import javax.microedition.io.file.FileSystemListener;
import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.UiApplication;

import com.webtrends.mobile.analytics.IllegalWebtrendsParameterValueException;
import com.webtrends.mobile.analytics.rim.WebtrendsDataCollector;
import com.wordpress.io.AppDAO;
import com.wordpress.task.AsyncRunner;
import com.wordpress.task.Task;
import com.wordpress.task.TasksRunner;
import com.wordpress.utils.Queue;
import com.wordpress.utils.conn.ConnectionManager;
import com.wordpress.utils.log.FileAppender;
import com.wordpress.utils.log.Log;

public class WordPressCore {
	
	private TasksRunner tasksRunner;
	private FileAppender fileAppender = null;
	private MySDListener sdCardListener = null;
	private Timer timer = null;
	private Vector applicationBlogs = new Vector();
	
	private String lastFileBrowserPath = null; //store the last path opened in the file browser
		
	//create a variable to store the ResourceBundle for localization support
    private final ResourceBundle _resources;

	private static WordPressCore instance;
	
	private WordPressCore() {
		Log.debug("WordPressCore initializated");
		sdCardListener = new MySDListener();
		UiApplication.getUiApplication().addFileSystemListener(sdCardListener);
		timer = new Timer();
		_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	}
	
	public static WordPressCore getInstance() {
		if (instance == null) {
			instance = new WordPressCore();
		}
		return instance;
	}
	
	public ResourceBundle getResourceBundle() {
		return _resources;
	}

	private class MySDListener implements FileSystemListener {
		public void rootChanged(int state, String rootName) {
			if( state == ROOT_ADDED ) {
				if( rootName.equalsIgnoreCase("sdcard/") ) {
					Log.trace("microSD card inserted");
				}
			} else if( state == ROOT_REMOVED ) {
				Log.trace("microSD card removed");
				boolean needClose = true;
				try {
					//if storage is not set on SD card
					if(!AppDAO.SD_STORE_PATH.equalsIgnoreCase(AppDAO.getBaseDirPath())) {
						needClose = false;
					 }
				} catch (RecordStoreException e) {
					Log.error(e, "Error reading RMS");
				} catch (IOException e) {
					Log.error(e, "Error reading RMS");
				} finally {
					//close the app only if needed
					if(needClose)
						exitWordPress();
				}
			}
		}
	}
	
	public void exitWordPress() {	
		Log.debug("closing app...");
		try {
			WebtrendsDataCollector.getInstance().onApplicationTerminate("", null);
		} catch (IllegalWebtrendsParameterValueException e) {
			e.printStackTrace();
		}
		UiApplication.getUiApplication().removeFileSystemListener(sdCardListener);
		getTasksRunner().quit(); //stop the runner thread
		timer.cancel(); //cancel the timer
		NotificationHandler.getInstance().shutdown(); //stop the notification handler
		SharingHelperOldDevices.deleteAppIstance();
		UiApplication.getUiApplication().removeGlobalEventListener( ConnectionManager.getInstance() ); //see WordPress.java : addGlobalEventListener( _manager );  
		System.exit(0);
	}
	
	public FileAppender getFileAppender() {
		return fileAppender;
	}
	
	public void setFileAppender(FileAppender fileAppender) {
		this.fileAppender = fileAppender;
	}
	
	public TasksRunner getTasksRunner() {
		if (tasksRunner == null) {
			tasksRunner = new TasksRunner(new Queue());
			Log.debug("tasksRunner ready");
			tasksRunner.startWorker(); //start the task runner thread
		}
		
		return tasksRunner;
	}
	
	public void runAsync(final Task task) {
		Log.debug("new AsyncRunner started");
		final AsyncRunner runner = new AsyncRunner(task);
		runner.start();
	}
/*
	public static void clean() {
		Log.debug("Runner Thread stopped");
		getInstance().getTasksRunner().quit();
		instance = null;
	}
	
	public void setTasksRunner(final TasksRunner nextRunner) {
		tasksRunner = nextRunner;
	}	
	*/

	public Timer getTimer() {
		return timer;
	}

	public Vector getApplicationBlogs() {
		return applicationBlogs;
	}

	public String getLastFileBrowserPath() {
		return lastFileBrowserPath;
	}

	public void setLastFileBrowserPath(String lastFileBrowserPath) {
		this.lastFileBrowserPath = lastFileBrowserPath;
	}	

}
