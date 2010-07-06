//#preprocess

package com.wordpress.bb;

import java.util.Vector;

import net.rim.blackberry.api.menuitem.ApplicationMenuItem;
import net.rim.blackberry.api.menuitem.ApplicationMenuItemRepository;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.RuntimeStore;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

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
import com.wordpress.view.MainView;
import com.wordpress.view.component.SelectorPopupScreen;

public class SharingHelperOldDevices {
	
	private ResourceBundleFamily _resources;
	private static SharingHelperOldDevices instance;
	private String noBlogAvailable;
	
	public static SharingHelperOldDevices getInstance() {
		if (instance == null) {
			instance = new SharingHelperOldDevices();
		}
		return instance;
	}
	
	private SharingHelperOldDevices() {
		 //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
         noBlogAvailable = _resources.getString(WordPressResource.MESSAGE_SHARE_TO_WORDPRESS_NO_BLOGS);
	};
	
	/**
	 * Add Global menus for sharing items
	 * This method is called at the end of app startup process	
	 */
	public void addGlobalMenuItems(ResourceBundle _resources) {
		ApplicationMenuItemRepository amir = ApplicationMenuItemRepository.getInstance();

		String[] chapiMimeTypes = new String[] {
				"video/x-msvideo", "video/quicktime", "video/mp4", "video/mpeg", "video/3gpp", "video/3gpp2",
				"audio/mpeg", "audio/mp4", "audio/wav", "application/ogg",
				"image/jpg", "image/bmp", "image/png", "image/gif"
		};

		for (int i = 0; i < chapiMimeTypes.length; i++) {

			ShareToWordPressMenuItem shareToWordPressMenuItem = new ShareToWordPressMenuItem(10000, 
					_resources.getString(WordPressResource.MENUITEM_SHARE_TO_WORDPRESS));

			amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_FILE_EXPLORER_BROWSE, shareToWordPressMenuItem, ApplicationDescriptor.currentApplicationDescriptor(), chapiMimeTypes[i]);
			amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_FILE_EXPLORER_ITEM, shareToWordPressMenuItem, ApplicationDescriptor.currentApplicationDescriptor(), chapiMimeTypes[i]);
		}

		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_BROWSER, 
				new ShareToWordPressMenuItem(10000, _resources.getString(WordPressResource.MENUITEM_SHARE_TO_WORDPRESS)),
				ApplicationDescriptor.currentApplicationDescriptor());
		
		//#ifdef IS_OS47_OR_ABOVE
		amir.addMenuItem(ApplicationMenuItemRepository.MENUITEM_CAMERA_PREVIEW,
				new ShareToWordPressMenuItem(10000, _resources.getString(WordPressResource.MENUITEM_SHARE_TO_WORDPRESS)), 
				ApplicationDescriptor.currentApplicationDescriptor());
		//#endif
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
			
			return;
		}
					
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
			String title = _resources.getString(WordPressResource.TITLE_BLOG_SELECTOR_POPUP);
			SelectorPopupScreen selScr = new SelectorPopupScreen(title, blogNames);
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
				
				String decodedURLLowerCase = decodedURL.toLowerCase();
				
				for (int i = 0; i < audioExtensions.length; i++) {
					if (decodedURLLowerCase.endsWith(audioExtensions[i].toLowerCase())) {
						mediaObj = new AudioEntry();
						break;
					}
				}
				
				if(mediaObj == null)
				for (int i = 0; i < videoExtensions.length; i++) {
					if (decodedURLLowerCase.endsWith(videoExtensions[i].toLowerCase())) {
						mediaObj = new VideoEntry();
						break;
					}
				}
				
				if(mediaObj == null)
				for (int i = 0; i < imageExtensions.length; i++) {
					if (decodedURLLowerCase.endsWith(imageExtensions[i].toLowerCase())) {
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
					String sharingText = "<a href=\""+context.toString()+"\">"+context.toString()+"</a>";
					post.setBody(sharingText);
				}
				
				FrontController.getIstance().showPost(post, true);
				
			} catch (Exception e) {
				Log.error(e, "Error while Sharing to WordPress");
				Dialog.alert("Error while Sharing to WordPress");
			}				
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
