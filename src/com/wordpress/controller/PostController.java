package com.wordpress.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.io.DraftDAO;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.model.Category;
import com.wordpress.model.Post;
import com.wordpress.utils.MultimediaUtils;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.Queue;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.CategoriesView;
import com.wordpress.view.NewCategoryView;
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
	private CategoriesView catView= null;
	ConnectionInProgressView connectionProgressView=null;
	private Post post=null;
	private int draftPostFolder=-1; //identify draft post folder
	private boolean isDraft= false; // identify if post is loaded from draft folder
		
	public static final int PHOTO=1;
	public static final int BROWSER=4;
	
	Preferences prefs = Preferences.getIstance();
	private Hashtable remoteFileInfo; ////contain files info on the WP server (used after sending MM files to the blog)
	private Queue files;  //used while sending MM files to the 	blog 
	private BlogConn connection; //used while sending MM files to the blog
	
	//used when new post/recent post
	// 0 = new post
	// 1 = edit recent post
	public PostController(Post post) {
		super();	
		this.post=post;
		//assign new space on draft folder, used for photo IO
		try {
			draftPostFolder = DraftDAO.storePost(post, draftPostFolder);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//used when loading draft post from disk
	public PostController(Post post,int _draftPostFolder) {
		super();	
		this.post=post;
		this.draftPostFolder=_draftPostFolder;
		this.isDraft = true;
	}
	
	public void showView() {
		this.view= new PostView(this, post);
		UiApplication.getUiApplication().pushScreen(view);
			
	}
		
	public String[] getBlogsCategories(){
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
	

	public String[] getPostCategories(){
		Category[] availableCategories = post.getBlog().getCategories();
		Vector categoryLabels = new Vector();
		int[] postCategories = post.getCategories();
		
		if (postCategories != null && availableCategories != null) {
            for (int i = 0; i < postCategories.length; i++) {
            	int idCatPost = postCategories[i];
            	for (int j = 0; j < availableCategories.length; j++) {
            		Category category = availableCategories[j];
            		String idString = category.getId();
            		int idCat = Integer.parseInt(idString);
            		if( idCatPost == idCat ) categoryLabels.addElement(category.getLabel());
				}
            }
		} else {
			return new String[0];
		}
		return Tools.toStringArray(categoryLabels);
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
		String[] draftPostPhotoList;
		try {
			draftPostPhotoList = DraftDAO.getPostPhotoList(post.getBlog(), draftPostFolder);
	
		if(draftPostPhotoList.length > 0 ) {
			files= new Queue(draftPostPhotoList.length);
			String key="";
			for (int i =0; i < draftPostPhotoList.length; i++ ) {
				key = draftPostPhotoList[i];
				files.push(key);				
			}
			sendMultimediaContent();
		} else {
			sendPostContent();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void saveDraftPost() {
		try {
		 draftPostFolder = DraftDAO.storePost(post, draftPostFolder);
		 view.getPostState().setModified(false); //set the post as saved
		} catch (Exception e) {
			displayError(e,"Error while saving draft post!");
		}
	}
			
	public boolean dismissView() {
		if(view.getPostState().isModified()){
	    	int result=this.askQuestion("Changes Made, are sure to close this screen?");   
	    	if(Dialog.YES==result) {
	    		try {
	    			if( !isDraft ){ //not previous draft saved post
	    				DraftDAO.removePost(post.getBlog(), draftPostFolder);
	    			}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	    		FrontController.getIstance().backAndRefreshView(false);
	    		return true;
	    	} else {
	    		return false;
	    	}
		} else {
			FrontController.getIstance().backAndRefreshView(true); //refresh prev view
			return true;
		}
	}
	
	
	public void showCategoriesView(){			
		catView= new CategoriesView(this, post.getBlog().getCategories(), post.getCategories());		
		UiApplication.getUiApplication().pushScreen(catView);
	}
	
	
	public void showNewCategoriesView(){			
		NewCategoryView newCatView= new NewCategoryView(this, post.getBlog().getCategories());		
		UiApplication.getUiApplication().pushScreen(newCatView);
	}

	
	public void showPhotosView(){
		
		String[] draftPostPhotoList;
		try {
			draftPostPhotoList = DraftDAO.getPostPhotoList(post.getBlog(), draftPostFolder);
		
			photoView= new PhotosView(this);
			for (int i = 0; i < draftPostPhotoList.length; i++) {
				String currPhotoPath = draftPostPhotoList[i];
				byte[] data=DraftDAO.loadPostPhoto(post, draftPostFolder, currPhotoPath);
				EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
				photoView.addPhoto(currPhotoPath, img);
			}
			
			UiApplication.getUiApplication().pushScreen(photoView);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * show selected photo
	 */
	public void showEnlargedPhoto(String key){
		System.out.println("showed photos: "+key);
		byte[] data;
		try {
			data = DraftDAO.loadPostPhoto(post, draftPostFolder, key);
			EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);
			UiApplication.getUiApplication().pushScreen(new PhotoPreview(this, key ,img)); //modal screen...
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/*
	 * delete selected photo
	 */
	public boolean deletePhoto(String key){
		System.out.println("deleting photo: "+key);
		view.setPostState(true); //mark post has changed
		
		try {
			DraftDAO.removePostPhoto(post.getBlog(), draftPostFolder, key);
			photoView.deletePhotoBitmapField(key); //delete the thumbnail
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return true;
		/*Object remove = dummyFS.remove(key);
		if(remove != null){
			photoView.deletePhotoBitmapField(key); //delete the thumbnail
			return true;
		} else {
			return false;
		}*/
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

		try {
			DraftDAO.storePostPhoto(post, draftPostFolder, data, fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//dummyFS.put(fileName, img); //add to the dummy fs
		
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
	           connection = new NewPostConn (post.getBlog().getXmlRpcUrl(), 
	        		post.getBlog().getUsername(),post.getBlog().getPassword(),prefs.getTimeZone(), post, view.getPostState().isPublished());
		} else { //edit post
			
			 connection = new EditPostConn (post.getBlog().getXmlRpcUrl(), 
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
			
			byte[] data;
			try {
				data = DraftDAO.loadPostPhoto(post, draftPostFolder, poppedKey);
			
			EncodedImage img= EncodedImage.createEncodedImage(data,0, -1);			

			connection = new NewMediaObjectConn (post.getBlog().getXmlRpcUrl(), 
	       		   post.getBlog().getUsername(),post.getBlog().getPassword(),prefs.getTimeZone(), post.getBlog().getId(), 
	       		poppedKey,img.getData());
		    
		    connectionProgressView.setDialogClosedListener(new ConnectionInProgressListener(connection));
	
			connection.addObserver(new sendImagesCallBack());
			connection.startConnWork(); //starts connection
			} catch (IOException e) {
				// TODO controllare bene questa fase
				e.printStackTrace();
			}
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