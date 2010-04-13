package com.wordpress.view.mm;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.LabelField;

import com.wordpress.model.MediaEntry;
import com.wordpress.view.container.BorderedFieldManager;


public class MediaViewMediator {
		private MediaEntry mediaEntry;
		private BorderedFieldManager manager;
		private Field bitmapField;
		private LabelField fileNameField;
		private LabelField titleField;

		public void mediaEntryChanged() {
			//update the fields
			fileNameField.setText(mediaEntry.getFileName());
			titleField.setText(mediaEntry.getTitle());
			if(mediaEntry.getTitle() == null || mediaEntry.getTitle().trim().equals("")) {
				//define the italic font
				Font fnt = Font.getDefault().derive(Font.ITALIC);
				titleField.setText("None");
				titleField.setFont(fnt);
			} else
				titleField.setFont( Font.getDefault());
			//invalidate the container
			manager.invalidate();
		}
		
		public Field getField() {
			return bitmapField;
		}

		public MediaEntry getMediaEntry() {
			return mediaEntry;
		}

		public BorderedFieldManager getManager() {
			return manager;
		}

		public MediaViewMediator(MediaEntry mediaEntry, BorderedFieldManager manager, Field bitmap, LabelField fileNameField, LabelField titleField) {
			super();
			this.mediaEntry = mediaEntry;
			this.manager = manager;
			this.bitmapField = bitmap;
			this.fileNameField = fileNameField;
			this.titleField = titleField;
		}	
	}