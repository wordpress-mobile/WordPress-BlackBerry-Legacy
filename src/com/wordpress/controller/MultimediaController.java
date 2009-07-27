package com.wordpress.controller;

import com.wordpress.model.Post;

public class MultimediaController {

	

	
	public MultimediaController(Post post) {
		super();
	}
	

	
	/*
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
			connection.stopConnWork(); //stop the connection if the user click on cancel button
		}		

	}
*/
}
