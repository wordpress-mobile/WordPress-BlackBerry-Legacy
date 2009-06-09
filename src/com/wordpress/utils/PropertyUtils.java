package com.wordpress.utils;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;

/**
 * This class provides a mechanism to read attributes stored in the jad file and 
 * in the RIM Application Descriptor.
 * The class provides a list of basic properties, but it can be used to get any
 * custom property, not just the ones listed as constants.
 */
public class PropertyUtils {

	private static PropertyUtils singletonObject;
	
	private CodeModuleGroup myGroup = null;
	private boolean first    = true;
   
	//singleton
	private PropertyUtils() {

		CodeModuleGroup[] allGroups = CodeModuleGroupManager.loadAll();
		String moduleName = ApplicationDescriptor.currentApplicationDescriptor().getModuleName();

		// We shall check for null because when the app is copied into the
		// emulator (or installed via JavaLoader) it does not allow jad
		// properties to be read
		if (moduleName == null || allGroups == null) {
			return;
		}

		for (int i = 0; i < allGroups.length; i++) {
			if (allGroups[i] != null && allGroups[i].containsModule(moduleName)) {
				myGroup = allGroups[i];
				break;
			}
		}

	}
	
	public static PropertyUtils getIstance() {
		if (singletonObject == null) {
			singletonObject = new PropertyUtils();
		}
		return singletonObject;
	}
	
	
    public String get(String property) {
        if (first) {
            // As recommended by RIM documentation we shall wait one second for
            // the jad property writer to update the jad on the very first run
            // of the application
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {}
            first = false;
        }
        if (myGroup != null) {
            return myGroup.getProperty(property);
        } else {
            return null;
        }
    }
	

	
	public static synchronized String getAppName(){
	 ApplicationDescriptor descriptor = ApplicationDescriptor.currentApplicationDescriptor();
	 return descriptor.getName();
	}
  
	
	public static synchronized String getAppVersion(){
		 ApplicationDescriptor descriptor = ApplicationDescriptor.currentApplicationDescriptor();
		 return descriptor.getVersion();
	}


	public static synchronized Bitmap getAppIcon(){
		 ApplicationDescriptor descriptor = ApplicationDescriptor.currentApplicationDescriptor();
		 return descriptor.getIcon();
	}
	
/* Research In Motion tracks the use of sensitive APIs in the BlackBerry® Java® Development Environment for security and export
control reasons. This method require code sign otherwise the application won't start on real devices.
*/
	/**
	 * Chiama la midlet per accedere alle risorse definite nel file jad
	 * @param key
	 * @return
	 
	public static synchronized String getAppProperty(String key){
		//return midlet.getAppProperty(key);
		CodeModuleGroup myGroup = null;
		CodeModuleGroup[] allGroups=null;
			
		allGroups = CodeModuleGroupManager.loadAll();
		String moduleName = ApplicationDescriptor.currentApplicationDescriptor().getModuleName();

		for (int i = 0; i < allGroups.length; i++) {
		   if (allGroups[i].containsModule(moduleName)) {
		      myGroup = allGroups[i];
		      break;
		    }
		}

		if(myGroup != null){
			String description = myGroup.getProperty(key);
			return description;
		} else {
			return "";
		} 
	}
	*/
}
