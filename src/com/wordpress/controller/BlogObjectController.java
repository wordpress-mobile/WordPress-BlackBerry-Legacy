//#preprocess
package com.wordpress.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.blackberry.api.invoke.CameraArguments;
import net.rim.blackberry.api.invoke.Invoke;
import net.rim.device.api.io.file.FileSystemJournalListener;
import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.ApplicationManager;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.CodeModuleManager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;


import com.wordpress.bb.WordPress;
import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.FileUtils;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.AudioEntry;
import com.wordpress.model.Blog;
import com.wordpress.model.BlogEntry;
import com.wordpress.model.MediaEntry;
import com.wordpress.model.PhotoEntry;
import com.wordpress.model.VideoEntry;
import com.wordpress.task.SendToBlogTask;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.CustomFieldsView;
import com.wordpress.view.MediaEntryPropView;
import com.wordpress.view.MediaView;
import com.wordpress.view.PostSettingsView;
import com.wordpress.view.PreviewView;
import com.wordpress.view.component.FileBrowser.RimFileBrowserListener;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.dialog.VideoPressInfoPopupScreen;
import com.wordpress.view.mm.MediaObjFileJournalListener;
import com.wordpress.view.mm.MediaViewMediator;
import com.wordpress.view.mm.MultimediaPopupScreen;
import com.wordpress.view.mm.VideoFileJournalListener;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.PreviewHTTPConn;

//#ifdef IS_OS50_OR_ABOVE
import net.rim.device.api.ui.picker.FilePicker;
//#else
import com.wordpress.view.component.FileBrowser.RimFileBrowser;
//#endif


/**
 * @author dercoli
 *
 */
public abstract class BlogObjectController extends BaseController {
	
	protected Blog blog;
	protected BlogEntry blogEntry;
 
	protected boolean isModified = false; //the state of post/page. track changes on post/page..
	protected int draftFolder=-1; //identify draft post folder
	protected boolean isDraft= false; // identify if post/page is loaded from draft folder

	public static final int NONE=-1;
	public static final int PHOTO=1;
	public static final int VIDEO=2;
	public static final int AUDIO=3;
	public static final int BROWSER_VIDEO=4;
	public static final int BROWSER_PHOTO=5;
	public static final int BROWSER_AUDIO=6;
	
	//related view
	protected SendToBlogTask sendTask;
	protected PostSettingsView settingsView = null;
	protected MediaView photoView = null;
	protected ConnectionInProgressView connectionProgressView=null;

		
	protected static final String LOCAL_DRAFT_KEY = "localdraft";
	protected static final String LOCAL_DRAFT_LABEL = _resources.getString(WordPress.LABEL_LOCAL_DRAFT);
	public abstract void setPhotosNumber(int count);
	public abstract void setAuthDate(long authoredOn);
	public abstract void setPassword(String password);
	public abstract void setSignature(boolean isSignatureEnabled, String signature);

	//journal listener
	FileSystemJournalListener mediaFileFSListener = null;
	
	public BlogObjectController(Blog _blog, BlogEntry entry) {
		this.blog = _blog;
		this.blogEntry = entry;
	}
	
	public abstract void showSettingsView();
	
	public void showCustomFieldsView(String title){
		CustomFieldsView view;
		view = new CustomFieldsView(this, blogEntry.getCustomFields(), title);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	public void setObjectAsChanged(boolean value) {
		isModified = value;
	}
	
	public boolean isObjectChanged() {
		return isModified;
	}
	
	public boolean isDraftItem() {
		return isDraft;
	}
	
	public void setPhotoResizing(boolean isPhotoRes, Integer imageResizeWidth, Integer imageResizeHeight) {

		Log.trace("Entering setPhotoResizing. imageResizeWidth is " + imageResizeWidth.toString());
		if( blogEntry.isPhotoResizing() != null && !blogEntry.isPhotoResizing().booleanValue()== isPhotoRes ){
			blogEntry.setPhotoResizing(new Boolean(isPhotoRes));
			setObjectAsChanged(true);
		} else {
			if(blogEntry.isPhotoResizing() == null ){
				blogEntry.setPhotoResizing(new Boolean(isPhotoRes));
				setObjectAsChanged(true);
			}
		}

		if(blogEntry.getImageResizeWidth() != null && !blogEntry.getImageResizeWidth().equals(imageResizeWidth)) {
			blogEntry.setImageResizeWidth(imageResizeWidth);
			setObjectAsChanged(true);
		} else {
			if(blogEntry.getImageResizeWidth() == null) {
				blogEntry.setImageResizeWidth(imageResizeWidth);
				setObjectAsChanged(true);
			}
		}
		
		if(blogEntry.getImageResizeHeight() != null && !blogEntry.getImageResizeHeight().equals(imageResizeHeight)) {
			blogEntry.setImageResizeHeight(imageResizeHeight);
			setObjectAsChanged(true);
		} else {
			if(blogEntry.getImageResizeHeight() == null) {
				blogEntry.setImageResizeHeight(imageResizeHeight);
				setObjectAsChanged(true);
			}
		}	
	}
	
	public void setVideoResizing(boolean isVideoRes, Integer videoResizeWidth, Integer videoResizeHeight) {

		Log.trace("Entering setVideoResizing. videoResizeWidth is " + videoResizeWidth.toString());
		
		if( blogEntry.isVideoResizing() != null && !blogEntry.isVideoResizing().booleanValue()== isVideoRes ){
			blogEntry.setVideoResizing(new Boolean(isVideoRes));
			setObjectAsChanged(true);
		} else {
			if(blogEntry.isVideoResizing() == null ){
				blogEntry.setVideoResizing(new Boolean(isVideoRes));
				setObjectAsChanged(true);
			}
		}

		if(blogEntry.getVideoResizeWidth() != null && !blogEntry.getVideoResizeWidth().equals(videoResizeWidth)) {
			blogEntry.setVideoResizeWidth(videoResizeWidth);
			setObjectAsChanged(true);
		} else {
			if(blogEntry.getVideoResizeWidth() == null) {
				blogEntry.setVideoResizeWidth(videoResizeWidth);
				setObjectAsChanged(true);
			}
		}
		
		if(blogEntry.getVideoResizeHeight() != null && !blogEntry.getVideoResizeHeight().equals(videoResizeHeight)) {
			blogEntry.setVideoResizeHeight(videoResizeHeight);
			setObjectAsChanged(true);
		} else {
			if(blogEntry.getVideoResizeHeight() == null) {
				blogEntry.setVideoResizeHeight(videoResizeHeight);
				setObjectAsChanged(true);
			}
		}	
	}
	
	protected String[] getPhotoList() {
		String[] draftPostPhotoList = new String [0];
		
		Vector mediaObjects;
		mediaObjects = blogEntry.getMediaObjects();
		draftPostPhotoList = new String[mediaObjects.size()];
		
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			draftPostPhotoList[i] = tmp.getFilePath();
		}
		
		return draftPostPhotoList;
	}


	protected synchronized void checkMediaObjectLinks() {
		try {
			Log.trace(">>> checkMediaLink ");
			
			Vector mediaObjects;
			mediaObjects = blogEntry.getMediaObjects();
			
			Vector notFoundMediaObjects = new Vector();
			//checking the existence of photo already linked with the page/post obj
			for (int i = 0; i < mediaObjects.size(); i++) {
				MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
				
				if(!JSR75FileSystem.isFileExist(tmp.getFilePath())) {
					notFoundMediaObjects.addElement(tmp);
					Log.trace("media file not found on disk  : "+tmp.getFilePath());
				}
				
				if(!JSR75FileSystem.isReadable(tmp.getFilePath())) {
					notFoundMediaObjects.addElement(tmp);
					Log.trace("media file is not readeable  : "+tmp.getFilePath());
				}

			}
			for (int i = 0; i < notFoundMediaObjects.size(); i++) {
				MediaEntry tmp = (MediaEntry) notFoundMediaObjects.elementAt(i);
				mediaObjects.removeElement(tmp);
				Log.trace("media file removed from post : "+tmp.getFilePath());
			}

			Log.trace("<<< checkMediaLink ");
		} catch (Exception e) {
			Log.error(e, "checkMediaLink error");
		}
	}
	
	
	public synchronized void addLinkToMediaObject(String completePath, int type) {
		try {
			Log.trace("linking media obj: "+completePath);
			isModified = true; //set the post/page as modified
			
       	 	MediaEntry mediaObj = null;
    		if(type == VIDEO) {
    			mediaObj = new VideoEntry();
    			//dont remove the file listener, because we cannot close the recording app programmatically, and the user could change the filename
    		}else if(type == PHOTO) {
    			mediaObj = new PhotoEntry();
    			removeMediaFileJournalListener(); //remove the fs listener.
    		} else {
    			//audio
    			mediaObj = new AudioEntry();
    			removeMediaFileJournalListener(); //remove the fs listener.
    		}
			mediaObj.setFilePath(completePath);
       	 	
       	 	Vector mediaObjects;
       	 	mediaObjects = blogEntry.getMediaObjects();
			//checking the existence of photo already linked with the page/post obj
			for (int i = 0; i < mediaObjects.size(); i++) {
				MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
				if(tmp.getFilePath().equalsIgnoreCase(mediaObj.getFilePath()))
					return;	
			}
			
			//check if the file is readable (0n some real phone you cannot access predefined imgs)
			if (!JSR75FileSystem.isReadable(completePath))
				throw new IOException("The file "+completePath+" isn't readable");
			
			photoView.addMedia(mediaObj);
			//photoView.setLastAddedMediaObj(mediaObj);
			
			mediaObjects.addElement(mediaObj);
		} catch (Exception e) {
			displayError(e, "Cannot link the media file!");
		} finally {
			
		}
	}
	
	
		
	/*
	 * delete selected photo
	 */
	public synchronized boolean deleteLinkToMediaObject(String key) {
		Log.trace("deleting link to photo: "+key);
		isModified = true; //set the post/page as modified
		Vector mediaObjects;
		mediaObjects = blogEntry.getMediaObjects();
		//checking the existence of photo linked with the page/post obj
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			if(tmp.getFilePath().equalsIgnoreCase(key)) {
				mediaObjects.removeElementAt(i);
				photoView.deleteMedia(key); //delete the thumb
				break;
			}
		}
		return true;
	}
		
	public void showPhotosView(){
		
		photoView= new MediaView(this);

		Vector mediaObjects;
		mediaObjects = blogEntry.getMediaObjects();
						
		for (int i = 0; i < mediaObjects.size(); i++) {
			MediaEntry tmp = (MediaEntry) mediaObjects.elementAt(i);
			photoView.addMedia(tmp);
		}			
		UiApplication.getUiApplication().pushScreen(photoView);
	}
	
	/*
	 * show selected photo properties
	 */
	public void showMediaObjectProperties(MediaViewMediator mediaViewMediator){
		Log.trace("showed properties for media: "+mediaViewMediator.getMediaEntry().getFilePath());
		UiApplication.getUiApplication().pushScreen(new MediaEntryPropView(this, mediaViewMediator));
	}
	
	/** show up multimedia type selection */
	public void showAddMediaPopUp(int mediaType) {
		int response = BROWSER_PHOTO;
		
		if(mediaType == VIDEO && blog.isWPCOMBlog() && !blog.isVideoPressUpgradeAvailable()) {
			VideoPressInfoPopupScreen popup = new VideoPressInfoPopupScreen();
			UiApplication.getUiApplication().pushModalScreen(popup);
			return;
		} 
		
		//when user want to add an audio the app doesn't show the rec-or-library popup
		if(mediaType != AUDIO) {		
	    	MultimediaPopupScreen multimediaPopupScreen = new MultimediaPopupScreen(mediaType);
	    	UiApplication.getUiApplication().pushModalScreen(multimediaPopupScreen); //modal screen...
			response = multimediaPopupScreen.getResponse();
		} else {
			response = BROWSER_AUDIO; 
		}
			
		switch (response) {
		case BROWSER_PHOTO:
           	String imageExtensions[] = MultimediaUtils.getSupportedWordPressImageFormat();
           	displayFileBrowser(PHOTO, imageExtensions, false);
           	//photoFileBrowser.setListener(new MultimediaFileBrowserListener(PHOTO));
			break;
			
		case BROWSER_VIDEO:
          	String videoExtensions[] = MultimediaUtils.getSupportedWordPressVideoFormat();// "mp4", "m4a","3gp", "3gp2", "avi", "wmv", "asf", "avi"};
          	displayFileBrowser(VIDEO, videoExtensions, false);
         	//videoFileBrowser.setListener(new MultimediaFileBrowserListener(VIDEO));
           	break;
           	
		case BROWSER_AUDIO:
           	String audioExtensions[] = MultimediaUtils.getSupportedWordPressAudioFormat();
           	displayFileBrowser(AUDIO, audioExtensions, false);
           	//audioFileBrowser.setListener(new MultimediaFileBrowserListener(AUDIO));
			break;
			
		case PHOTO:
			try {
				 	addMediaFileJournalListener();
					Invoke.invokeApplication(Invoke.APP_TYPE_CAMERA, new CameraArguments());
				} catch (Exception e) {
					removeMediaFileJournalListener();
					displayError(e, "Cannot invoke camera!");
				}
			break;
		
		case VIDEO:
			try {
				 	addVideoFileJournalListener();
				 	int moduleHandle = CodeModuleManager.getModuleHandle("net_rim_bb_videorecorder"); 
				 	ApplicationDescriptor[] apDes = CodeModuleManager.getApplicationDescriptors(moduleHandle); 
				 	ApplicationManager.getApplicationManager().runApplication(apDes[0]);  
				} catch (Exception e) {
					removeMediaFileJournalListener();
					displayError(e, "Cannot invoke camera!");
				}
			break;

		default:
			break;
		}		
	}
	
	private void addVideoFileJournalListener() {
		//create a new listener only if it is null
		if(mediaFileFSListener == null ) {
			mediaFileFSListener = new VideoFileJournalListener(this);
		}
		UiApplication.getUiApplication().addFileSystemJournalListener(mediaFileFSListener);
	}
	
	private void addMediaFileJournalListener() {
		//create a new listener only if it is null
		if(mediaFileFSListener == null ) {
			mediaFileFSListener = new MediaObjFileJournalListener(this);
		}
		UiApplication.getUiApplication().addFileSystemJournalListener(mediaFileFSListener);
	}
	
	//called when photoview is closed
	public void removeMediaFileJournalListener() {
		if(mediaFileFSListener != null) {
			Log.trace("Media FS listener rimosso");
			UiApplication.getUiApplication().removeFileSystemJournalListener(mediaFileFSListener);
			mediaFileFSListener = null;
		} 
	}
	
	public void startRemotePreview(String objectLink, String title, String content, String tags, String categories){
		String connMessage = null;
		connMessage = _resources.getString(WordPressResource.CONN_LOADING_PREVIEW_TEMPLATE);
		
		final PreviewHTTPConn connection = new PreviewHTTPConn(objectLink);
		
        connection.addObserver(new RemotePreviewCallBack(title, content, tags, categories));  
        connectionProgressView= new ConnectionInProgressView(connMessage);
		if(blog.isHTTPBasicAuthRequired()) {
			connection.setHttp401Password(blog.getHTTPAuthPassword());
			connection.setHttp401Username(blog.getHTTPAuthUsername());
		}
        connection.startConnWork(); //starts connection
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}		
	}
		
	public void startLocalPreview(String title, String content, String tags, String categories) {

		
		//build the body html for preview
		String bodyContentForPreview = buildBodyHtmlFragment(content);

		//build the full html 
		StringBuffer topMediaFragment = new StringBuffer();
		StringBuffer bottomMediaFragment = new StringBuffer();
		Vector mediaObjects;
		mediaObjects = blogEntry.getMediaObjects();
		
		for (int i = 0; i < mediaObjects.size(); i++) {

			MediaEntry remoteFileInfo = (MediaEntry)mediaObjects.elementAt(i);
			StringBuffer tmpBuff = null;
			if(remoteFileInfo.isVerticalAlignmentOnTop())
				tmpBuff  = 	topMediaFragment;
			else
				tmpBuff  = 	bottomMediaFragment;
			
			tmpBuff.append(remoteFileInfo.getMediaObjectAsSmallHtml());
		}
		
		String htmlPage = topMediaFragment.toString() + "<p>&nbsp;</p>" + bodyContentForPreview 
			+ "<p>&nbsp;</p>"+ bottomMediaFragment.toString()+ getTheSignaturePreview();
		
		String html = FileUtils.readTxtFile("defaultPostTemplate.html");

		if(tags !=null && tags.trim().length() > 0 ) 
			tags= "Tags: "+tags;		

		if(title == null || title.length() == 0) title = _resources.getString(WordPressResource.LABEL_EMPTYTITLE);
		html = StringUtils.replaceAll(html, "!$title$!", title);
		html = StringUtils.replaceAll(html, "<p>!$text$!</p>", htmlPage);
		if( tags !=null && tags.trim().length() > 0  ) {
			html = StringUtils.replaceAll(html, "!$mt_keywords$!", tags);
			html = StringUtils.replaceAll(html, "!$categories$!", "Categories: "+ categories);
		} else {
			html = StringUtils.replaceAll(html, "!$mt_keywords$!", "");//The pages have no tags
			html = StringUtils.replaceAll(html, "!$categories$!", ""); //The pages have no categories
		}
		
		//#ifdef IS_OS50_OR_ABOVE
		try {
			UiApplication.getUiApplication().pushScreen(new PreviewView(html.getBytes("UTF-8"), "text/html; charset=UTF-8"));
		} catch (UnsupportedEncodingException e) {
			//never fall here
		}
		//#else
		UiApplication.getUiApplication().pushScreen(new PreviewView(html));	
		//#endif
	
	}
	
	protected String getTheSignaturePreview() {
		return "";
	}
	
	//callback for post loading
	private class RemotePreviewCallBack implements Observer {
		
		private final String title;
		private final String content;
		private final String tags;
		private final String categories;

		public RemotePreviewCallBack(String title, String content, String tags, String categories) {
			this.title = title;
			this.content = content;
			this.tags = tags;
			this.categories = categories;
		}

		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {

					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					
					if(resp.isStopped()){
						return;
					}
					
					if(!resp.isError()) {
						String contentType = null;
						try {
							Hashtable responseHash =  (Hashtable) resp.getResponseObject();
							byte[] responseContent  = (byte[])responseHash.get("data");
							Hashtable responseHeaders =  (Hashtable)responseHash.get("headers");
							Log.trace("Finding respose content type from http header");		
				    		contentType = (String) responseHeaders.get("Content-Type");
				    		UiApplication.getUiApplication().pushScreen(new PreviewView(responseContent, contentType));
						} catch (Exception e) {
							startLocalPreview(title,content,tags, categories);
							return;
						}
					} else {
						startLocalPreview(title,content,tags, categories);
					}
				}
			});
		}
	} 
		
	/**
	 * Build the html fragment for the body preview
	 * @param body  original body text field content
	 * @return
	 */
	protected static synchronized String buildBodyHtmlFragment(String originalContent) {
		String[] split = StringUtils.split(originalContent, "\n\n");
		StringBuffer newContentBuff = new StringBuffer();
		for (int i = 0; i < split.length; i++) {
			newContentBuff.append("<p>");
			newContentBuff.append(split[i]);
			newContentBuff.append("</p>");
		}
		
		split = StringUtils.split(newContentBuff.toString(), "\n");
		newContentBuff = new StringBuffer();
		for (int i = 0; i < split.length; i++) {
			newContentBuff.append("<br/>");
			newContentBuff.append(split[i]);
		}
		return newContentBuff.toString();
	}
	
	protected void displayFileBrowser(int type, String[] extensions, boolean isThumbEnabled) {
		//#ifdef IS_OS50_OR_ABOVE
		FilePicker fp = FilePicker.getInstance();
		//note: the filepick doesn't support multiple filters (.jpg|.png)
		//then, all files type are showed in the filepick and we check the file type in the listener
		FilePickListener fileListener = new FilePickListener(type);
		fp.setListener(fileListener);
		WordPressCore wpCore = WordPressCore.getInstance(); 
		if(wpCore.getLastFileBrowserPath() != null)
			fp.setPath(wpCore.getLastFileBrowserPath());
		fp.show(); 
		//#else
		RimFileBrowser oldFileBrowser = new RimFileBrowser(extensions, false);
		if(type == VIDEO)
			oldFileBrowser.setPredefinedThumb(Bitmap.getBitmapResource("video_thumb_48.png"));
		oldFileBrowser.setListener(new OldFileBrowserListener(type));
		UiApplication.getUiApplication().pushScreen(oldFileBrowser);					
		//#endif
	}

	//#ifdef IS_OS50_OR_ABOVE
	private class FilePickListener implements FilePicker.Listener 
	{   

		int type = -1;

		public FilePickListener(int multimediaFileType) {
			type = multimediaFileType;			
		}
		
		public void selectionDone(String theFile)
		{
			Log.trace("[FilePickListener]");
			if (theFile == null){
				Log.trace("selected none"); 
			} else {
				Log.trace("filename: " + theFile);
				if(!theFile.startsWith("file://")) {
					theFile = "file://"+ theFile;
				}
				
				//retrieve the correct file type
				String[] split = StringUtils.split(theFile, ".");
				String ext = split[split.length - 1];
				if(!MultimediaUtils.getImageMIMEType(ext).equalsIgnoreCase("")) {
					type = PHOTO;
				} else if(!MultimediaUtils.getAudioMIMEType(ext).equalsIgnoreCase("")) {
					type = AUDIO;
				} else if(!MultimediaUtils.getVideoMIMEType(ext).equalsIgnoreCase("")) {
					type = VIDEO;
				} else {
					Dialog.alert( _resources.getString(WordPressResource.ERROR_FILETYPE_NOT_SUPPORTED));
					return;
				}
				//store the last used dir
				int lastPos = theFile.lastIndexOf('/');
				if(lastPos != -1) {
					String lastDir = theFile.substring(0, lastPos+1);
					if (!lastDir.endsWith("/")){
						lastDir+="/";
					}
					WordPressCore wpCore = WordPressCore.getInstance(); 
					wpCore.setLastFileBrowserPath(lastDir);
				}
				//link the file
				addLinkToMediaObject(theFile, type);	
			}
		}
	}
	//#else
	private class OldFileBrowserListener implements RimFileBrowserListener {
	    
		int type = -1;
	    
		public OldFileBrowserListener(int multimediaFileType) {
			type = multimediaFileType;			
		}

		public void selectionDone(String theFile) {
			Log.trace("[OldFileBrowserListener.selectionDone]");
			Log.trace("filename: " + theFile);
             if (theFile == null){
            	 
             } else {
    		     if(!theFile.startsWith("file://")) {
    		    	 theFile = "file://"+ theFile;
    		       } 
				addLinkToMediaObject(theFile, type);	
             }	
	    }
	}
	//#endif
}