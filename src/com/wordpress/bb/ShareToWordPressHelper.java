//#preprocess
package com.wordpress.bb;

import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.io.BlogDAO;
import com.wordpress.model.AudioEntry;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.PhotoEntry;
import com.wordpress.model.Post;
import com.wordpress.model.VideoEntry;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.BaseView;
import com.wordpress.view.BlogView;
import com.wordpress.view.MainView;
import com.wordpress.view.NotificationView;
import com.wordpress.view.PreferencesView;
import com.wordpress.view.component.BlogSelectorPopupScreen;

public class ShareToWordPressHelper {
	
	private static ShareToWordPressHelper instance;
	private String noBlogAvailable;
	
	public static ShareToWordPressHelper getInstance() {
		if (instance == null) {
			instance = new ShareToWordPressHelper();
		}
		return instance;
	}
	
	private ShareToWordPressHelper() {};
	
	/**
	 * Add Global menus for sharing items
	 * This method is called at the end of app startup process	
	 */
	public void addGlobalMenuItems(ResourceBundle _resources) {
		ApplicationMenuItemRepository amir = ApplicationMenuItemRepository.getInstance();

		ShareToWordPressMenuItem shareToWordPressMenuItem = new ShareToWordPressMenuItem(10000, 
				_resources.getString(WordPressResource.MENUITEM_SHARE_TO_WORDPRESS));
		
		noBlogAvailable = _resources.getString(WordPressResource.MESSAGE_SHARE_TO_WORDPRESS_NO_BLOGS);
		
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_BROWSER, shareToWordPressMenuItem, ApplicationDescriptor.currentApplicationDescriptor());
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_FILE_EXPLORER_BROWSE, shareToWordPressMenuItem, ApplicationDescriptor.currentApplicationDescriptor());
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_FILE_EXPLORER_ITEM, shareToWordPressMenuItem, ApplicationDescriptor.currentApplicationDescriptor());
		
		//#ifdef IS_OS47_OR_ABOVE
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_CAMERA_PREVIEW, shareToWordPressMenuItem, ApplicationDescriptor.currentApplicationDescriptor());
		//#endif
	}
	
	/**
	 * Called at startup to store the app references onto runtime store
	 * @param istance
	 */
	void registerIstance(UiApplication istance) {
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
			Log.trace("Application References added to the runtimestore");
		} else
		{
			//should never fall here bc the app deregister istance on exit
			Log.trace("runtimestore not empty, why??");
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
			Log.trace("App removed from RuntimeStore");

		} else
		{ //never falls here
			Log.trace("RuntimeStore is already empty!");
		}
	}
	
	
	private void newSharing(final Object context) {
		Screen scr = UiApplication.getUiApplication().getActiveScreen();
		System.out.println("newSharing: "+scr.getClass().getName());

		if (scr instanceof SplashScreen){ //app is in loading wait till completed
			UiApplication.getUiApplication().invokeLater(new Runnable()
	        {
	          public void run()
	          {
	        	newSharing(context);
	          }
	        }, 1000, false);
			
			return;
		}

		//There is a dialog displayed within the app. do nothing in this case
		if (scr instanceof Dialog ){
			System.out.println("dialog visualizzata!!");
			return;
		}
					
		
		/*
		if (! (scr instanceof MainView || scr instanceof BlogView  
				|| scr instanceof  PreferencesView
				|| scr instanceof  NotificationView ) ){
			Dialog.alert("Please, save your changes and go back on the home screen before sharing items.");
			return;
		}
*/
		Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();
		if(applicationBlogs.size() == 0) {
			Dialog.alert(noBlogAvailable);			
			return;
		}
		
		
		int selection = -1;
		
		if(applicationBlogs.size() == 1) {
			selection = 0;
		} else {
			//choose a blog from the blog list. Excluded blogs in loading phase.
			Vector tmpBlogs = new Vector();
			
			for (int i = 0; i < applicationBlogs.size(); i++) {
				BlogInfo currentBlog = (BlogInfo) applicationBlogs.elementAt(i);
				if (currentBlog.getState() != BlogInfo.STATE_LOADING)
					tmpBlogs.addElement(currentBlog.getName());
			}

			String[] blogNames = new String[tmpBlogs.size()];
			tmpBlogs.copyInto(blogNames);
			BlogSelectorPopupScreen selScr = new BlogSelectorPopupScreen(blogNames);
			selScr.pickBlog();
			selection = selScr.getSelectedBlog();
		}

		if(selection != -1) {
			
			//back to main screen - draft post will be auto-saved			
			while ((scr = UiApplication.getUiApplication().getActiveScreen()) != null){
				if (scr instanceof MainView) {		

					break;
				} else {
					UiApplication.getUiApplication().popScreen(scr);
				}
			}
			
			try {
				Blog selectedBlog =  BlogDAO.getBlog((BlogInfo)applicationBlogs.elementAt(selection));
				Post post = new Post(selectedBlog);
				MediaEntry mediaObj = null;
				String imageExtensions[] = MultimediaUtils.getSupportedWordPressImageFormat();
				String videoExtensions[] = MultimediaUtils.getSupportedWordPressVideoFormat();
				String audioExtensions[] = MultimediaUtils.getSupportedWordPressAudioFormat();
				
				Log.trace("in URL: "+context.toString());
				String decodedURL = context.toString();
				
				for (int i = 0; i < audioExtensions.length; i++) {
					if (decodedURL.endsWith(audioExtensions[i])) {
						mediaObj = new AudioEntry();
						break;
					}
				}
				
				if(mediaObj == null)
				for (int i = 0; i < videoExtensions.length; i++) {
					if (decodedURL.endsWith(videoExtensions[i])) {
						mediaObj = new VideoEntry();
						break;
					}
				}
				
				if(mediaObj == null)
				for (int i = 0; i < imageExtensions.length; i++) {
					if (decodedURL.endsWith(imageExtensions[i])) {
						mediaObj = new PhotoEntry();
						break;
					}
				}
				
				if(mediaObj != null && decodedURL.startsWith("file://")) {
					mediaObj.setFilePath(decodedURL);
					Vector mediaObjs = new Vector();
					mediaObjs.addElement(mediaObj);
					post.setMediaObjects(mediaObjs);
				} else {
					post.setBody(context.toString());
				}
				
				FrontController.getIstance().showPost(post, true);
				
			} catch (Exception e) {
				Log.error(e, "Error while loading selected blog");
				Dialog.alert("Error while loading selected Blog");
			}				
		} else {
			Log.trace("pressed escape");
		}
	}
	
	
	private class ShareToWordPressMenuItem extends ApplicationMenuItem {
		
		private final String text; //string we want to use as the label of the menuItem

		ShareToWordPressMenuItem(int order, String text){
			super(order);
			this.text = text;
		}
		
		//Run is called when the menuItem is invoked. KEEP IN MIND - YOU ARE IN THE CONTEXT OF CALLER
		public Object run(final Object context){
			Log.trace("ShareToWordPressMenuItem - RUN");
			if(context != null ) {
	
				//Open the RuntimeStore.
				RuntimeStore store = RuntimeStore.getRuntimeStore();

				//Obtain the reference of WordPress for BlackBerry.
				final Object obj = store.get(WordPressInfo.APPLICATION_ID);

				//If obj is null, there is no current reference
				//to WordPress for BlackBerry
				if (obj == null) {    
					//never falls here, bc we registered this menu with wp4bb app descriptor.
					Log.trace("RuntimeStore is already empty!");
				} else {
					//When invoked this menu item YOU ARE IN THE CONTEXT OF CALLER... you cannot change wp app UI here!!
					((WordPress)obj).requestForeground();
										
			        //Grab the lock for the running WordPress GUI.
					((WordPress)obj).invokeLater(new Runnable()
			        {
			          public void run()
			          {
			        	newSharing(context);
			          }
			        });
			     }
			}
			return context;
		}
		
		//toString should return the string we want to use as the label of the menuItem
		public String toString(){
			return text;
		}
	}
	
}
