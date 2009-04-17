package com.wordpress.model;

import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemStateListener;


public class PostState implements ItemStateListener {

    private boolean published = true;
    private boolean modified = false;

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean aPublished) {
        published = aPublished;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean aModified) {
        modified = aModified;	
    }

    public void itemStateChanged(Item aItem) {
        modified = true;
    }
    
}
