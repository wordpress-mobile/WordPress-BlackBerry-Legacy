package com.wordpress.task;

import java.io.IOException;
import java.util.Hashtable;

import com.wordpress.utils.MultimediaUtils;

public class ResizeImageTask extends TaskImpl {
	
	private byte[] data;
	private String fileName;
	
	public ResizeImageTask(byte[] data, String fileName) {
		this.data = data;
		this.fileName = fileName;
	}

	public void execute() {
		Hashtable content = null;
		try {
			content = MultimediaUtils.resizePhotoAndOutputJpeg(data, fileName);
		} catch (IOException e) {
			isError = true;
			appendErrorMsg("Resizing Error: " + e.getMessage());
		}
	
		if (progressListener != null)
			progressListener.taskComplete(content); 
		
	}

}