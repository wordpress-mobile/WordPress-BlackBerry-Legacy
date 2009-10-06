package com.wordpress.bb;
import javax.microedition.io.file.FileSystemListener;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.task.AsyncRunner;
import com.wordpress.task.Task;
import com.wordpress.task.TasksRunner;
import com.wordpress.utils.Queue;
import com.wordpress.utils.log.FileAppender;
import com.wordpress.utils.log.Log;

public class WordPressCore {
	
	private TasksRunner tasksRunner;
	private FileAppender fileAppender = null;
	private MySDListener sdCardListener = null;

	private static WordPressCore instance;
	
	private WordPressCore() {
		Log.debug("WordPressCore initializated");
		sdCardListener = new MySDListener();
		UiApplication.getUiApplication().addFileSystemListener(sdCardListener);
	}
	
	public static WordPressCore getInstance() {
		if (instance == null) {
			instance = new WordPressCore();
		}
		return instance;
	}
	
	private class MySDListener implements FileSystemListener {
		public void rootChanged(int state, String rootName) {
			if( state == ROOT_ADDED ) {
				if( rootName.equalsIgnoreCase("sdcard/") ) {
					Log.debug("microSD card inserted");
				}
			} else if( state == ROOT_REMOVED ) {
				Log.debug("microSD card removed");
				//perform the same check as above			
			}
		}
	}
	
	public void exitWordPress() {
		Log.debug("closing app...");
		UiApplication.getUiApplication().removeFileSystemListener(sdCardListener);
		getTasksRunner().quit(); //stop the runner thread
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

	public static void clean() {
		Log.debug("Runner Thread stopped");
		getInstance().getTasksRunner().quit();
		instance = null;
	}
	
	public void setTasksRunner(final TasksRunner nextRunner) {
		tasksRunner = nextRunner;
	}	
	
	/**
	 * Screen dimensions
	 * 
	 * Pearl 8220 - 240 x 320 pixels
	 * Curve 8300 Series, 8800 Series, 8700 Series - 320 x 240 pixels
	 * Curve 8350i - 320 x 240 pixels
	 * Curve 8900 - 480 x 360 pixels
	 * Bold  9000 Series - 480 x 320 pixels
	 * Tour  9600 Series - 480 x 360 pixels
	 * Storm 9500 Series - portrait view: 360 x 480 pixels,  landscape view: 480 x 360 pixels
	 * 
	 */
	public Bitmap getBackgroundBitmap() {
		return Bitmap.getBitmapResource("MainBackground.png");
/*		 int width = Display.getWidth(); 
		 int height = Display.getHeight();
		 
		 if(width == 240 && height == 320 ) {
			 
		 } else if(width == 320 && height == 240) {
			 
		 } else if(width == 480 && height == 320) { 
			 
		 } else if(width == 480 && height == 360) {
			 
		 } else if(width == 360 && height == 480) {
			 
		 } else {
			 
		 }

		return null;*/
	}
	
	
}
