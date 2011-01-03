package com.wordpress.io;

import java.util.Hashtable;
import net.rim.device.api.util.Persistable;

public class CustomHashtable extends Hashtable implements Persistable {
	
	public void wrapAppData(Hashtable data) {
		this.put("addTrick", data);
	}
	
	public Hashtable unwrapAppData() {
		return (Hashtable)this.get("addTrick");
	}
}