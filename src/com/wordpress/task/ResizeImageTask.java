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


/*	
	private class ResizeImgListener implements TaskProgressListener {

		private final WaitScreen waitScreen;
		private final ResizeImageTask resTask;

		public ResizeImgListener(WaitScreen waitScreen,	ResizeImageTask resTask) {
			this.waitScreen = waitScreen;
			this.resTask = resTask;

		}

		public void taskComplete(Object obj) {
			
			final Hashtable content = (Hashtable) obj;
			
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					waitScreen.close();

					if (resTask.isError()) {
						displayError(resTask.getErrorMsg());
						return;
					}
					
					String fileName = (String) content.get("name");
					byte[] data = (byte[]) content.get("bits");
					
					storePhoto(data, fileName);
					
				}
			});
			
		}

		public void taskUpdate(Object obj) {

		}
	}
	*/