package com.wordpress.bb;

import java.util.Vector;

import javax.microedition.content.ActionNameMap;
import javax.microedition.content.ContentHandler;
import javax.microedition.content.ContentHandlerException;
import javax.microedition.content.ContentHandlerServer;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.content.RequestListener;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.ResourceBundleFamily;
import net.rim.device.api.system.ControlledAccessException;
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

public class SharingHelper implements RequestListener{
	
	/* We are using a singleton to simplify the check for new incoming request when main view appears (not before).
	 * This is due to the startup sequence that uses different threads... 
	 * Keep in mind that you should remove manually the listener from the CHAPI server at application exist. 
	 * Otherwise, the CHAPI server retain a reference to this class, and at next startup you have already 
	 * a listener in the place. 
	 */
	
	private static SharingHelper instance;
	private ResourceBundleFamily _resources;
	
	//The Content Handler ID
	private final static String CHAPI_ID = "com.wordpress.bb";
	
	//The content handler class name
	public final static String CHAPI_CLASS_NAME = CHAPI_ID + ".WordPress"; 
	
	public final static long CHAPI_MENUITEM_ID = 0x4a4f670272a059c3L; //com.wordpress.bb.WordPress.chapiMenuItems
    
    public final static String[] CHAPI_MIME_TYPES = new String[] {
    	"video/x-msvideo", "video/quicktime", "video/mp4", "video/mpeg", "video/3gpp", "video/3gpp2",
    	"audio/mpeg", "audio/mp4", "audio/wav", "application/ogg",
    	"image/jpg", "image/jpeg", "image/bmp", "image/png", "image/gif"
    };
    
    public final static String[] CHAPI_SUFFIXES = new String[] {
    	"avi", "mov", "mp4", "m4v", "mpg", "3gp", "3g2",
    	"mp3", "m4a","wav", "ogg",
    	"jpg", "jpeg","bmp", "png", "gif"
    };
    
    private Invocation pending;
    private ContentHandlerServer server;
    
	public static SharingHelper getInstance() {
		if (instance == null) {
			instance = new SharingHelper();
		}
		return instance;
	}
    
	private SharingHelper() {
		Log.trace("SharingHelper constructor");
		 //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
	};
	
	public void removeCHAPIListener() {
		try {
			server = Registry.getServer(CHAPI_CLASS_NAME);
			server.setListener(null);           //set the listener 
		} catch (ContentHandlerException e) {
			Log.error(e,"Error removing  CHAPI listener" );
		}
	}
	
	//this is called only once during devices startup to refresh strings registered with chapi. 
	//otherwise the string remains the same also after upgrading the app
	protected void unregisterCHAPI() {
		Log.trace("CHAPI unregister");
		try {
			Registry registry = Registry.getRegistry(CHAPI_CLASS_NAME);
			registry.unregister(CHAPI_CLASS_NAME);
		} catch (Throwable t) {
			Log.error(t, "Could not Unregister for " + CHAPI_ID);
		}
	}

	public void verifyRegistration() {
		Log.trace(">>> verifyRegistration");
		
		try
		{
			Registry registry = Registry.getRegistry(CHAPI_CLASS_NAME);
			ContentHandler registered = registry.forID(CHAPI_ID, true);
			if (registered != null)
			{
				return;
			}
			Log.trace("not found registered CHAPI, registering it now!");
			// Wasn't registered before, so do it now.
			String[] actions = new String[] { ContentHandler.ACTION_SEND };
			String[] actionNames = new String[] { _resources.getString(WordPressResource.MENUITEM_SHARE_TO_WORDPRESS) };
			ActionNameMap[] maps = new ActionNameMap[]{ new ActionNameMap(actions, actionNames, "en") };
			registry.register(CHAPI_CLASS_NAME, CHAPI_MIME_TYPES, CHAPI_SUFFIXES, actions, maps,
					CHAPI_ID, null);
		}
		catch (Throwable t)
		{
			Log.error(t, "Could not register for " + CHAPI_ID);
		}
		
		Log.trace("<<< verifyRegistration");
	}
	
	public void addCHAPIListener() {
		try {
			server = Registry.getServer(CHAPI_CLASS_NAME);
			pending = server.getRequest(false); //check if a request is already there
			server.setListener(this);           //set the listener 
		} catch (ContentHandlerException e) {
			Log.error(e,"Error during SharingHelp init" );
		}
	}
	
	public void invocationRequestNotify(ContentHandlerServer handler) {
		Log.trace(">>> invocationRequestNotify");
		pending = handler.getRequest(false);
		if(pending != null) {
			processRequest();
		}
		Log.trace("<<< invocationRequestNotify");
	}
	
	public void checkPendingRequest() {
		if(pending != null) {
			processRequest();
		}
	}
	
	private void processRequest() {
		Log.trace(">>> processRequest");
		String filename = null;
		String type = null;
		
		synchronized (this) {
			filename = pending.getURL();
			type = pending.getType();				
		}
		server.finish(pending, Invocation.OK);
			
		Screen scr = UiApplication.getUiApplication().getActiveScreen();
		//There is a dialog displayed within the app. do nothing in this case
		if (scr instanceof Dialog ){
			return;
		}				
		Vector applicationBlogs = WordPressCore.getInstance().getApplicationBlogs();
		if(applicationBlogs.size() == 0) {
			Dialog.alert( _resources.getString(WordPressResource.MESSAGE_SHARE_TO_WORDPRESS_NO_BLOGS));			
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
			selScr.pickItem();
			selection = selScr.getSelectedItem();
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
				
				if(filename != null && filename.toLowerCase().startsWith("file://")) {
					String filenameLowerCase = filename.toLowerCase();
					for (int i = 0; i < audioExtensions.length; i++) {
						if (filenameLowerCase.endsWith(audioExtensions[i].toLowerCase())) {
							mediaObj = new AudioEntry();
							break;
						}
					}

					if(mediaObj == null)
						for (int i = 0; i < videoExtensions.length; i++) {
							if (filenameLowerCase.endsWith(videoExtensions[i].toLowerCase())) {
								mediaObj = new VideoEntry();
								break;
							}
						}

					if(mediaObj == null)
						for (int i = 0; i < imageExtensions.length; i++) {
							if (filenameLowerCase.endsWith(imageExtensions[i].toLowerCase())) {
								mediaObj = new PhotoEntry();
								break;
							}
						}
					
					if(mediaObj == null) {
						Dialog.alert( _resources.getString(WordPressResource.ERROR_FILETYPE_NOT_SUPPORTED));
						return;
					}
									
					mediaObj.setFilePath(filename);
					Vector mediaObjs = new Vector();
					mediaObjs.addElement(mediaObj);
					post.setMediaObjects(mediaObjs);
					
				} else if(filename != null && type != null && type.startsWith("text/")) {
					post.setBody(filename);
				} else {
					Dialog.alert( _resources.getString(WordPressResource.ERROR_FILETYPE_NOT_SUPPORTED));
					return;
				}
				
				FrontController.getIstance().showPost(post, true);
				
			} catch (Exception e) {
				Log.error(e, "Error while Sharing to WordPress");
				Dialog.alert("Error while Sharing to WordPress");
			}				
		} 
	}
	
	/**
	 * Called during shutdown to delete the app references from runtime store
	 * @param istance
	 */
	static synchronized void deleteAppIstance() {
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
	
	/**
	 * Called at startup to store the app references onto runtime store
	 * @param istance
	 */
	static synchronized void storeAppIstance(UiApplication istance) {
		try{
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
		} catch (ControlledAccessException  e) {
			Log.trace(e, "Error while accessing the runtime store");
		}
	}
}