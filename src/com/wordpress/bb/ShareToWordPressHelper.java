package com.wordpress.bb;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.UiApplication;

import com.wordpress.utils.log.Log;

public class ShareToWordPressHelper {
	
	private static ShareToWordPressHelper instance;
	private ShareToWordPressMenuItem shareToWordPressMenuItem = new ShareToWordPressMenuItem(10);
	
	
	public static ShareToWordPressHelper getInstance() {
		if (instance == null) {
			instance = new ShareToWordPressHelper();
		}
		return instance;
	}

	
	void addGlobalMenuItems() {
		ApplicationMenuItemRepository amir = ApplicationMenuItemRepository.getInstance();
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_BROWSER, shareToWordPressMenuItem);
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_CALENDAR_EVENT, shareToWordPressMenuItem);
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_ADDRESSCARD_VIEW, shareToWordPressMenuItem);
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_MEMO_VIEW, shareToWordPressMenuItem);	
	}

	void registerWordPressIstance(UiApplication istance) {
		//Open the RuntimeStore.
		RuntimeStore store = RuntimeStore.getRuntimeStore();
		//Obtain the reference of WordPress for BlackBerry.
		Object obj = store.get(WordPressInfo.APPLICATION_ID);

		//If obj is null, there is no current reference
		//to WordPress for BlackBerry.
		if (obj == null)
		{    
			//Store a reference to this instance in the RuntimeStore.
			store.put(WordPressInfo.APPLICATION_ID, istance);
			Log.trace("inserito nel runtimestore");
			System.out.println("inserito nel runtimestore");

		} else
		{
			Log.trace(" runtimestore non e' vuoto");
			System.out.println(" runtimestore non e' vuoto");
			store.replace(WordPressInfo.APPLICATION_ID, UiApplication.getUiApplication());
		}
	}


	void deregisterIstance() {
		//Open the RuntimeStore.
		RuntimeStore store = RuntimeStore.getRuntimeStore();
		//Obtain the reference of WordPress for BlackBerry.
		Object obj = store.get(WordPressInfo.APPLICATION_ID);

		if (obj != null)
		{    
			store.remove(WordPressInfo.APPLICATION_ID);
			Log.trace("rimosso dal runtimestore");
			System.out.println("rimosso dal runtimestore");

		} else
		{
			Log.trace(" runtimestore e' vuoto");
			System.out.println(" runtimestore e' vuoto");
		}
	}
	
	private class ShareToWordPressMenuItem extends ApplicationMenuItem {
		
		
		//using the default constructors here.
		ShareToWordPressMenuItem(int order){
			super(order);
			System.out.println("COSTRUTTORE DEL MENU GLOBALE");
		}
		
		//Run is called when the menuItem is invoked
		public Object run(Object context){
			if(context != null ) {
				
				System.out.println("TEST " +ApplicationDescriptor.currentApplicationDescriptor().getName());
				//Open the RuntimeStore.
				RuntimeStore store = RuntimeStore.getRuntimeStore();
				//Obtain the reference of WordPress for BlackBerry.
				Object obj = store.get(WordPressInfo.APPLICATION_ID);
				
				//If obj is null, there is no current reference
				//to WordPress for BlackBerry.
				if (obj == null)
				{    
					Log.trace("non trovato nel runtimestore");
					System.out.println("non trovato nel runtimestore");
					//new WordPress(new String[]{""});
					
				} else
				{
					((WordPress)obj).newSharing();
					((WordPress)obj).requestForeground();
				}
				
				//ApplicationManager.getApplicationManager().requestForeground(ApplicationDescriptor.currentApplicationDescriptor().getModuleHandle());
				
				
				/*		
    		int moduleHandle = CodeModuleManager.getModuleHandle(appname); 
    		if (moduleHandle > 0) 
    		{ 
    		ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(moduleHandle); 
    		try {
				ApplicationManager.getApplicationManager().runApplication(apDes[0], true);
			} catch (ApplicationManagerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  
    		}*/    		
			}
			return context;
		}
		
		//toString should return the string we want to
		//use as the label of the menuItem
		public String toString(){
			return "Share to WP";
		}
	}
	
}
