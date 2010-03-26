package com.wordpress.model;

import java.util.Vector;

/**
 * This is the base class for main blog objs - pages, posts, galleries
 * @author dercoli
 *
 */
public class BlogEntry {
	
    private Vector mediaObjects = new Vector();
	private Vector customFields = new Vector();
    
	
	public void setCustomFields(Vector custom_field) {
		this.customFields = custom_field;
	}

	public Vector getCustomFields() {
		return customFields;
	}

    
	public Vector getMediaObjects() {
		return mediaObjects;
	}

	public void setMediaObjects(Vector mediaObjects) {
		this.mediaObjects = mediaObjects;
	}

}
