package com.wordpress.controller;

import java.io.IOException;
import java.util.Hashtable;

import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.MediaObject;
import com.wordpress.model.Post;
import com.wordpress.utils.FileUtils;
import com.wordpress.utils.JSR75FileSystem;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.StringUtils;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.component.FileSelectorPopupScreen;
import com.wordpress.view.component.HtmlTextField;
import com.wordpress.view.component.MultimediaPopupScreen;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogConn;
import com.wordpress.xmlrpc.BlogConnResponse;
import com.wordpress.xmlrpc.NewMediaObjectConn;

public class MultimediaController extends BaseController implements Observer{

	public static final int PHOTO=1;
	public static final int VIDEO=2;
	public static final int AUDIO=3;
	public static final int BROWSER=4;
	ConnectionInProgressView connectionProgressView=null;
	private Post post=null;
	
	private HtmlTextField targetTextField;
	private MediaObject mmObject;
	
	public void showView() {}

	
	public MultimediaController(Post post) {
		super();
		this.post=post;
	}
	
	private void resetFile(){
		this.targetTextField=null;//update reference to target text field
		this.mmObject=null;
	}
	
	public void update(Observable observable, Object object) {
		dismissDialog(connectionProgressView);
		BlogConnResponse resp= (BlogConnResponse) object;
		if(!resp.isError()) {
			if(resp.isStopped()){
				return;
			}
			
			Hashtable content =(Hashtable)resp.getResponseObject();
			System.out.println("url del file remoto: "+content.get("url") );
			System.out.println("nome file remoto: "+content.get("file") );	
			final String url=(String)content.get("url");
			final String desc;
			
			if(mmObject!=null && !mmObject.getDescription().equals("")){
				desc=mmObject.getDescription();
			} else {
				desc=(String)content.get("file");
			}
							
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					targetTextField.insertImage(url,desc);		
				}
			});
			
		} else {
			final String respMessage=resp.getResponse();
		 	displayError(respMessage);	
		 	
		}
		resetFile();
	}
	
	
	public void showMultimediaSelectionBox(HtmlTextField textField){
		resetFile();
    	MultimediaPopupScreen multimediaPopupScreen = new MultimediaPopupScreen();
    	
    	UiApplication.getUiApplication().pushModalScreen(multimediaPopupScreen); //modal screen...
    	int response = multimediaPopupScreen.getResponse();

		switch (response) {
		case BROWSER:
           	 String imageExtensions[] = {"jpg", "jpeg","bmp", "png", "gif"};
             FileSelectorPopupScreen fps = new FileSelectorPopupScreen(null, imageExtensions);
             fps.pickFile();
             String theFile = fps.getFile();
             if (theFile == null){
                 Dialog.alert("Screen was dismissed. No file was selected.");
             } else {
            	 String[] fileNameSplitted = StringUtils.split(theFile, ".");
            	 String ext= fileNameSplitted[fileNameSplitted.length-1];
         
            	 try {
					byte[] readFile = JSR75FileSystem.readFile(theFile);
					MediaObject mmObj= new MediaObject();
					mmObj.setContentType(ext); //setting the content type as the file extension
					mmObj.setMediaData(readFile);
					sendMultimediaContent(mmObj, textField);
										
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.out.println("Error while sending file to blog");
				}

             }					
			break;
		case AUDIO:
			
			break;
			
		case PHOTO:
			
			break;
			
		case VIDEO:
			
			break;
			
		default:
			break;
		}
	}
	
	//send the multimedia obj to blog
	private void sendMultimediaContent(MediaObject mmObject, HtmlTextField textField){
		this.targetTextField=textField;//update reference to target text field
		this.mmObject=mmObject;
		
		final BlogConn connection;
		Preferences prefs = Preferences.getIstance();
		 String filename=System.currentTimeMillis()+"."+mmObject.guessFileExtension();
		 
	     connection = new NewMediaObjectConn (post.getBlog().getBlogXmlRpcUrl(), 
       		   post.getBlog().getUsername(),post.getBlog().getPassword(),prefs.getTimeZone(), post.getBlog().getBlogId(), 
       		   filename,mmObject.getMediaData() );

		
		connection.addObserver(this); 
        connectionProgressView= new ConnectionInProgressView(
       		_resources.getString(WordPressResource.CONNECTION_INPROGRESS));
      
       connection.startConnWork(); //starts connection
				
		int choice = connectionProgressView.doModal();
		if(choice==Dialog.CANCEL) {
			System.out.println("Chiusura della conn dialog tramite cancel");
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}		

	}


	public void refreshView() {
		
	}
}
