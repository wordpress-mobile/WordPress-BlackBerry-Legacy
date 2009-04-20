package com.wordpress.view;

import java.io.IOException;

import javax.microedition.rms.RecordStoreException;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BlogController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.MainController;
import com.wordpress.model.Blog;
import com.wordpress.utils.Preferences;

public class MainView extends MainScreen {
	

    private Preferences blogPrefs = null;
    private BlogController blogController = null;
    private MainController mainController=null;

    private ObjectListField listaBlog; 
    		
    //create a variable to store the ResourceBundle for localization support
    private static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
	

	public MainView(MainController mainController) {
		super();
		this.mainController=mainController;
		blogController= BlogController.getIstance();
		blogPrefs = Preferences.getIstance();
        //add a screen title
        LabelField title = new LabelField(_resources.getString(WordPressResource.APPLICATION_TITLE),
                        LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        setTitle(title);
		
        
    	try {
			blogPrefs.load(); //TODO il caricamento delle preferenze deve avvenire altrove
		} catch (Exception e) {
			e.printStackTrace();
			//displayError(e, "Non riesco a caricare le preferenze");
		}
		
		setupUpBlogsView();
		
		addMenuItem(_aboutItem);
		addMenuItem(_addBlogItem);
		addMenuItem(_setupItem);
	}
	
	
	 public void setupUpBlogsView() {

		 String[] blogCaricati= blogController.getBlogNames();
	        
        listaBlog = new ObjectListField();
        if (blogCaricati.length == 0){
        	blogCaricati= new String[1];
        	blogCaricati[0]="No blog";
        	
        	removeMenuItem(_deleteBlogItem); 
        	removeMenuItem(_refreshBlogItem);
        } else {
        	//aggiungi i comandi dei blog	        	
        	/*
        	UiAccess.addSubCommand( newPostCommand, postCmd, blogs ); 
        	UiAccess.addSubCommand( draftPostCommand, postCmd, blogs );
        	UiAccess.addSubCommand( recentPostCommand, postCmd, blogs );
            */        	
        	addMenuItem(_refreshBlogItem);
        	addMenuItem(_deleteBlogItem);
        }
        		        	
    	listaBlog.set(blogCaricati);
        add(listaBlog);
	}
	 
	public void refreshBlogList(){	 
			if(listaBlog != null){
				this.delete(listaBlog);
				setupUpBlogsView();
			}
	 }
	 
	
    //create a menu item for users to click to add blog to the app
    private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 100, 10) {
        public void run() {
        	FrontController.getIstance().showAddBlogsView();
        }
    };
    
    //create a menu item for users to click to refresh blog
    private MenuItem _refreshBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 110, 10) {
        public void run() {
           
        }
    };

    //create a menu item for users to click to show setup
    private MenuItem _deleteBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETEBLOG, 120, 10) {
        public void run() {
            int selectedBlog = listaBlog.getSelectedIndex();
            mainController.deleteBlog(selectedBlog);
            refreshBlogList();
        }
    };
    
    
    //create a menu item for users to click to show setup
    private MenuItem _setupItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETUP, 1000, 10) {
        public void run() {
        	FrontController.getIstance().showSetupView();
        }
    };

    //create a menu item for users to click to see more information about the app
    private MenuItem _aboutItem = new MenuItem( _resources, WordPressResource.MENUITEM_ABOUT, 1010, 10) {
        public void run() {
        	FrontController.getIstance().showAboutView();
        }
    };
   
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
    	return mainController.exitApp();
    }

	
}