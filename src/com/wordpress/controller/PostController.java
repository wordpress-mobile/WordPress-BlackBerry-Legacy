package com.wordpress.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.Queue;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.PhotosView;
import com.wordpress.view.PostView;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.view.mm.MultimediaPopupScreen;
import com.wordpress.view.mm.PhotoPreview;
import com.wordpress.view.mm.PhotoSnapShotView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.NewMediaObjectConn;
import com.wordpress.xmlrpc.post.EditPostConn;
import com.wordpress.xmlrpc.post.NewPostConn;


public class PostController extends BaseController {
	
	private PostView view = null;
	private PhotosView photoView= null;
	ConnectionInProgressView connectionProgressView=null;
	private Post post=null;
	private BlogIOController blogController= BlogIOController.getIstance();
	private int draftPostId=-1; //identify a draft post id
	
	public static final int PHOTO=1;
	public static final int BROWSER=4;
	
	Preferences prefs = Preferences.getIstance();
	private Hashtable dummyFS = new Hashtable(); //dummy fs
	private Hashtable remoteFileInfo; //used when send MM files to blog
	private Queue files;  //used when send MM files to blog 
	private BlogConn connection; //used when send MM files to blog
	
	//used when loading new post/recent post
	public PostController(Post post) {
		super();	
		this.post=post;
	}
	
	//used when loading draft post
	public PostController(Post post,int draftPostId) {
		this(post);
		this.draftPostId=draftPostId;
	}
	
	public void showView() {
		this.view= new PostView(this, post);
		UiApplication.getUiApplication().pushScreen(view);
		
		
		
	}
		
	public String[] getAvailableCategories(){
		Category[] availableCategories = post.getBlog().getCategories();
		String[] categoryLabels;
		if (availableCategories != null) {
            categoryLabels = new String[availableCategories.length];
            for (int i = 0; i < availableCategories.length; i++) {
                categoryLabels[i] = availableCategories[i].getLabel();
            }
            
		} else {
			categoryLabels= new String[0];
		}
		return categoryLabels;
	}
	
	//return the post category n.b:change it
	public int getPostCategoryIndex(){
		return 0; //FIXME: categories managements
	/*	int primaryIndex = -1; 
		Category primaryCategory = post.getPrimaryCategory();
		if(primaryCategory == null) return primaryIndex;
		
		Category[] availableCategories = post.getBlog().getCategories();  
		if (availableCategories != null) {
            for (int i = 0; i < availableCategories.length; i++) {
                if (availableCategories[i].equals(primaryCategory)) {
                    primaryIndex = i;
                }
            }
		}
		return primaryIndex;*/
	}
	  
	public void sendPostToBlog() {
		
		if (!view.getPostState().isModified()) { //post without change
			return;
		}
		
		/*
		 * steps: 
		 * - show the dialog (no modal)
		 * - add the multimedia to blog and retrive response with id and url of files 
		 * - added the file to the end of the post
		 * - send the post to blog
		 *  
		 */
		connectionProgressView= new ConnectionInProgressView(_resources.getString(WordPressResource.CONNECTION_SENDING_PHOTOS));
		connectionProgressView.show();
	    
		remoteFileInfo = new Hashtable(); 
		
		if(dummyFS.size() > 0 ) {
			files= new Queue(dummyFS.size());
			Enumeration keys = dummyFS.keys();
			String key="";
			for ( ; keys.hasMoreElements(); ) {
				key = (String)keys.nextElement();
				files.push(key);				
			}
			sendMultimediaContent();
		} else {
			sendPostContent();
			}
	}
	
	public void saveDraftPost() {
		try {
		 blogController.saveDraftPost(post, draftPostId);
		 view.getPostState().setModified(false); //set the post as saved
		} catch (Exception e) {
			displayError(e,"Error while saving draft post!");
		}
	}
			
	public boolean dismissView() {
		if(view.getPostState().isModified()){
	    	int result=this.askQuestion("Changes Made, are sure to close this screen?");   
	    	if(Dialog.YES==result) {
	    		FrontController.getIstance().backAndRefreshView(false);
	    		return true;
	    	} else {
	    		return false;
	    	}
		} else {
			FrontController.getIstance().backAndRefreshView(false);
			return true;
		}
	}
	
	
	public void showPhotosView(){
		photoView= new PhotosView(this,dummyFS);
		UiApplication.getUiApplication().pushScreen(photoView);
	}
	
	/*
	 * show selected photo
	 */
	public void showEnlargedPhoto(String key){
		System.out.println("showed photos: "+key);
		EncodedImage mmObj = (EncodedImage) dummyFS.get(key);
		UiApplication.getUiApplication().pushScreen(new PhotoPreview(this, key ,mmObj)); //modal screen...
	}
	
	
	/*
	 * delete selected photo
	 */
	public boolean deletePhoto(String key){
		System.out.println("deleting photo: "+key);
		view.setPostState(true); //mark post has changed
		Object remove = dummyFS.remove(key);
		if(remove != null){
			photoView.deletePhotoBitmapField(key); //delete the thumbnail
			return true;
		} else {
			return false;
		}
	}
	
	//* called by photoview */
	public void showAddPhotoPopUp() {
		int response= BROWSER;
		
    	MultimediaPopupScreen multimediaPopupScreen = new MultimediaPopupScreen();
    	UiApplication.getUiApplication().pushModalScreen(multimediaPopupScreen); //modal screen...
		response = multimediaPopupScreen.getResponse();
			
		switch (response) {
		case BROWSER:
           	 String imageExtensions[] = {"jpg", "jpeg","bmp", "png", "gif"};
             FileSelectorPopupScreen fps = new FileSelectorPopupScreen(null, imageExtensions);
             fps.pickFile();
             String theFile = fps.getFile();
             if (theFile == null){
                 Dialog.alert("Screen was dismissed. No file was selected.");
             } else {
            	 String[] fileNameSplitted = StringUtils.split(theFile, "/");
            	 String ext= fileNameSplitted[fileNameSplitted.length-1];
				try {
					byte[] readFile = JSR75FileSystem.readFile(theFile);
					addPhoto(readFile,ext);	
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
             }					
			break;
			
		case PHOTO:
			PhotoSnapShotView snapView = new PhotoSnapShotView(this);
			UiApplication.getUiApplication().pushScreen(snapView); //modal screen...
			break;
			
		default:
			break;
		}		
	}
	
	public void addPhoto(byte[] data, String fileName){
		if(fileName == null) 
			fileName= String.valueOf(System.currentTimeMillis());
		
		EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
				
		//check if blog has "photo resize option" selected
		if (post.getBlog().isResizePhotos()){
			EncodedImage rescaled= MultimediaUtils.bestFit2(img, 640, 480);
			img=rescaled;
		} 

		dummyFS.put(fileName, img); //add to the dummy fs											
		photoView.addPhoto(fileName, img);
		view.setPostState(true); //mark post has changed
	}
	
	public void refreshView() {
		//resfresh the post view. not used.
	}

	//send the post alphanumeric data to blog
	private void sendPostContent(){
		
		//adding multimedia info to post
		String body = post.getBody();
		if(remoteFileInfo.size() > 0 ) {
			Enumeration keys = remoteFileInfo.keys();
			
			for (; keys.hasMoreElements(); ) {
				String key = (String) keys.nextElement();
				String url = (String) remoteFileInfo.get(key);
				body+="<br/><a href=\""+url+"\"  alt=\""+key+"\">"+key+"</a>";				
			}
		}
		post.setBody(body);
		
		if(post.getId() == null || post.getId().equalsIgnoreCase("-1")) { //new post
	           connection = new NewPostConn (post.getBlog().getBlogXmlRpcUrl(), 
	        		post.getBlog().getUsername(),post.getBlog().getPassword(),prefs.getTimeZone(), post, view.getPostState().isPublished());
		} else { //edit post
			
			 connection = new EditPostConn (post.getBlog().getBlogXmlRpcUrl(), 
					 post.getBlog().getUsername(),post.getBlog().getPassword(),prefs.getTimeZone(), post, view.getPostState().isPublished());
		}
		connectionProgressView.setDialogClosedListener(new ConnectionInProgressListener(connection));
		connection.addObserver(new sendPostCallBack()); 
		connection.startConnWork(); //starts connection		
	}
	
	//send the multimedia obj to blog
	private void sendMultimediaContent(){
		
		//check for previous errors during MM sending
		if(remoteFileInfo.containsKey("err")) {
		 	displayError((String)remoteFileInfo.get("err"));
			return;
 		}
		//check if there are others file to be sent
		if(files.isEmpty()) { 
			sendPostContent();
		} else {
		
			String poppedKey = (String)files.pop();
			EncodedImage img=(EncodedImage)dummyFS.get(poppedKey);
						
		    connection = new NewMediaObjectConn (post.getBlog().getBlogXmlRpcUrl(), 
	       		   post.getBlog().getUsername(),post.getBlog().getPassword(),prefs.getTimeZone(), post.getBlog().getBlogId(), 
	       		poppedKey,img.getData());
		    
		    connectionProgressView.setDialogClosedListener(new ConnectionInProgressListener(connection));
	
			connection.addObserver(new sendImagesCallBack());
			connection.startConnWork(); //starts connection
		}
	}
	
	
	//callback for send post to the blog
	private class sendPostCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					dismissDialog(connectionProgressView);
					BlogConnResponse resp= (BlogConnResponse) object;
					if(!resp.isError()) {
						if(resp.isStopped()){
							return;
						}
						FrontController.getIstance().backAndRefreshView(true);
					} else {
						final String respMessage=resp.getResponse();
					 	displayError(respMessage);	
					}			
				}
			});
		}
	}
	
	//callback for send Images to the blog
	private class sendImagesCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					
					BlogConnResponse resp= (BlogConnResponse) object;
					if(!resp.isError()) {
						if(resp.isStopped()){
							remoteFileInfo.put("err", "stopped by user");
						} else {
							Hashtable content =(Hashtable)resp.getResponseObject();
							System.out.println("url del file remoto: "+content.get("url") );
							System.out.println("nome file remoto: "+content.get("file") );	
							final String url=(String)content.get("url");
							remoteFileInfo.put(content.get("file"), url);
						}
					} else {
						dismissDialog(connectionProgressView);
						final String respMessage=resp.getResponse();
					 	remoteFileInfo.put("err", respMessage);
					}
					sendMultimediaContent(); //recursive...
				}
			});
		}
	}
	
	//callback for send post to the blog
	private class getPostStatusListCallBack implements Observer{
		public void update(Observable observable, final Object object) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
						
				}
			});
		}
	}
	
}