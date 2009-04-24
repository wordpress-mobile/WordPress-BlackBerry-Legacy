package com.wordpress.utils;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.Bitmap;

public class PropertyUtils {

	
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
