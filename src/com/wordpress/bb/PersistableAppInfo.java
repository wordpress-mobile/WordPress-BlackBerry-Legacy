package com.wordpress.bb;

import net.rim.device.api.util.IntHashtable;
import net.rim.device.api.util.Persistable;

/**
 * Container for persistable application-wide properties that need to be
 * kept outside the normal configuration system.
 */
public class PersistableAppInfo implements Persistable {
	private IntHashtable contents;
	public static final int FIELD_LAST_APP_VERSION = 0;
    public static final int FIELD_PERMISSION_ASKED = 1;
        
    /**
     * Instantiates a new persistable container.
     *
     * @param initialCapacity the initial capacity for the storage container
     */
    protected PersistableAppInfo(int initialCapacity) {
        contents = new IntHashtable(initialCapacity);
    }
    
    /**
     * Sets the specified element within the container.
     *
     * @param id the ID for the element, represented by a class constant
     * @param value the value to set for the element
     */
    public void setElement(int id, Object value) {
        contents.put(id, value);
    }
    
    /**
     * Gets the specified element from the container.
     *
     * @param id the ID for the element, represented by a class constant
     * @return the element, if it exists
     */
    public Object getElement(int id) {
        return contents.get(id);
    }
    
}