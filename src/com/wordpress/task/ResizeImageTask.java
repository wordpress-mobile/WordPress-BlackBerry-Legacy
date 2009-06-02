package com.wordpress.task;

import java.io.IOException;
import java.util.Hashtable;

import com.wordpress.utils.mm.MultimediaUtils;

public class ResizeImageTask implements LocalTask {
	
	private boolean isError = false;
	private String errorMsg = "";
	private byte[] data;
	private String fileName;
	private TaskProgressListener progressListener;
	
	public ResizeImageTask(byte[] data, String fileName) {
		this.data = data;
		this.fileName = fileName;
	}

	public void execute() {
		Hashtable content = null;
		try {
			content = MultimediaUtils.resizePhotoAndOutputJpeg(data,
					fileName);
		} catch (IOException e) {
			isError = true;
			errorMsg = "Resizing Error: " + e.getMessage();
		}
	
		if (progressListener != null)
			progressListener.taskComplete(content); 
		
	}
	

	public String getErrorMsg() {
		return errorMsg;
	}

	public boolean isError() {
		return isError;
	}

	public void setProgressListener(TaskProgressListener progressListener) {
		this.progressListener = progressListener;
	}
}