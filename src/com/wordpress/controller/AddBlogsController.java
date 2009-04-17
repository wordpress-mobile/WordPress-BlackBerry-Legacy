package com.wordpress.controller;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BasicEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.PasswordEditField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.Blog;
import com.wordpress.utils.Preferences;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;
import com.wordpress.view.AddBlogsView;
import com.wordpress.view.MainView;
import com.wordpress.view.dialog.ConnectionInProgressView;
import com.wordpress.xmlrpc.BlogAuthConn;
import com.wordpress.xmlrpc.BlogConnResponse;


public class AddBlogsController extends BaseController implements Observer{
	
	private AddBlogsView view = null;
	private String url="http://localhost/wp_mopress/xmlrpc.php;deviceside=true";
	private String pass="mopress"; // FIXME ricordati di togliere
	private String user="mopress";
	ConnectionInProgressView infoView=null;
	
	public AddBlogsController() {
		super();
		this.view= new AddBlogsView(this);
		UiApplication.getUiApplication().pushScreen(view);
	}
	
	private FieldChangeListener listener = new FieldChangeListener() {
        public void fieldChanged(Field field, int context) {
        	System.out.println("field class name: " + field.getOriginal().getClass().getName());

        	if (field instanceof PasswordEditField) {
        		pass=((PasswordEditField)field).getText();
        		System.out.println("pass: " + pass);
        	} else if(field instanceof BasicEditField) {
        		BasicEditField bf=(BasicEditField)field;
        		String bfLabel=bf.getLabel();
        		if( bfLabel.equals(view.getAssociatedResourceBundle().getString(WordPressResource.LABEL_BLOGUSER))){
        			user=bf.getText();
        		} else {
        			url=bf.getText();
        		}
        		System.out.println("label: "+bfLabel);
        		System.out.println("url: " + url+ " user: "+user);
        	} else {
	            ButtonField buttonField = (ButtonField) field;
	        	System.out.println("Button pressed: " + buttonField.getLabel());
	            if( buttonField.getLabel().equals(view.getAssociatedResourceBundle().getString(WordPressResource.BUTTON_OK))){
	            	addBlogs();
	            } else {
	            	backCmd();
	            }
        	}
       }
    };

	public FieldChangeListener getButtonListener() {
		return listener;
	}
	
	private void addBlogs(){

		if (pass != null && pass.length() == 0) {
        	pass = null;
            // FIXME lapassword opzionale esiste ancora??
            displayError("Please enter a password");
            return;
        }

        if (url != null && user != null && url.length() > 0 && user.length() > 0) {
            Preferences prefs = Preferences.getIstance();
            BlogAuthConn connection = new BlogAuthConn (url,user,pass,prefs.getTimeZone());
            connection.addObserver(this); 
             infoView= new ConnectionInProgressView(
            		view.getAssociatedResourceBundle().getString(WordPressResource.CONNECTION_INPROGRESS));
           
            connection.startConnWork(); //esegue il lavoro della connessione
            int choice = infoView.doModal();
    		if(choice==Dialog.CANCEL) {
    			System.out.println("Chiusura della conn dialog tramite cancel");
    			connection.stopConnWork(); //stop the connection if the user click on cancel button
    		}
        } else {
        	displayError("Please enter an address and username");
        }
	}
	
		
	public void update(Observable observable, Object object) {
		try{
			
		dismissDialog(infoView);
		BlogConnResponse resp=(BlogConnResponse)object;
		
		if(!resp.isError()) {
			System.out.println("Trovati blogs: "+((Blog[])resp.getResponseObject()).length);					 	
			if(resp.isStopped()){
				return;
			}
		 	Blog[]blogs=(Blog[])resp.getResponseObject();

		 	BlogController blogController= BlogController.getIstance();
		 	for (int i = 0; i < blogs.length; i++) {
					blogController.addBlog(blogs[i], true);
		        }
		 	backCmd();		 			 	
		} else {
			final String respMessage=resp.getResponse();
			
		 	UiApplication.getUiApplication().invokeLater(
		 			new Runnable(){
		 		public void run(){
		 			displayError(respMessage);	
		 		}
		 	});
		}		
	
		} catch (Exception e) {

			e.printStackTrace();
		} 
	}	
}