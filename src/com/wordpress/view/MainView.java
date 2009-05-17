package com.wordpress.view;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.MainController;
import com.wordpress.model.BlogInfo;
import com.wordpress.view.component.BlogListField;

public class MainView extends BaseView {
	
    private MainController mainController=null;
    private ListField listaBlog;
    private BlogListField blogListController; 
    private Timer timer = new Timer(); 

	public MainView(MainController mainController) {
		super(_resources.getString(WordPressResource.TITLE_APPLICATION));
		this.mainController=mainController;
	
        setupUpBlogsView();
	
		addMenuItem(_aboutItem);
		addMenuItem(_addBlogItem);
		addMenuItem(_setupItem);
	}
	
	
	 public void setupUpBlogsView() {
	
		 //resetta qua il timer!!!
		timer.cancel();
    	
		removeMenuItem(_deleteBlogItem);
    	removeMenuItem(_showBlogItem);

    	BlogInfo[] blogCaricati = mainController.getBlogsList();
				
        if (blogCaricati.length == 0){
        	blogCaricati = new BlogInfo[0];
        } else {
        	addMenuItem(_showBlogItem);
        	addMenuItem(_deleteBlogItem);
            timer = new Timer();
            timer.schedule(new CountDown(), 1000, 3000); //3 second splash
        }
    	blogListController = new BlogListField(blogCaricati);
		this.listaBlog = blogListController.getCheckList();
        add(listaBlog);    
	 }
	 
	 
   private class CountDown extends TimerTask {
	      public void run() {
	    	  
	    	  UiApplication.getUiApplication().invokeLater(new Runnable() {
	  			public void run() {
	  				
	  				System.out.println("CountDown");
	  				BlogInfo[] blogCaricati = mainController.getBlogsList();
	  				boolean inLoading = false;
	  		  		for (int i = 0; i < blogCaricati.length; i++) {
	  		  			if (blogCaricati[i].getState() ==  BlogInfo.STATE_LOADING 
	  		  					|| blogCaricati[i].getState() == BlogInfo.STATE_ADDED_TO_QUEUE ){
	  		  				blogListController.setBlogState(blogCaricati[i]);
	  		  				inLoading = true;
	  		  			} else if(blogCaricati[i].getState() == BlogInfo.STATE_LOADED) {
	  		  				blogListController.setBlogState(blogCaricati[i]);
	  		  			}
	  				}
	  		  		
	  		  		if ( !inLoading )
	  		  			timer.cancel(); //cancel timer
	  				
	  			}
	  		});
	    	  
	    	  
	     
	      }
	   }

	 
	public void refreshBlogList(){	 
		if(listaBlog != null){
			this.delete(listaBlog);
			setupUpBlogsView();
		}
	 }

/*	
	// Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		return true;
	}

	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			return true;
		} else if (c == Characters.ENTER) {
			return true;
		}

		return super.keyChar(c, status, time);
	}
	
*/
	
    private MenuItem _showBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_SHOWBLOG, 130, 10) {
        public void run() {
        BlogInfo blogSelected = blogListController.getBlogSelected();
        mainController.showBlog(blogSelected);
        }
    };
    
 

    //add blog menu item 
    private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 150, 10) {
        public void run() {
        	mainController.addBlogs();
        }
    };

        
    private MenuItem _deleteBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 200, 10) {
        public void run() {
        BlogInfo blogSelected = blogListController.getBlogSelected();
        mainController.deleteBlog(blogSelected);
        refreshBlogList();
        }
    };
    
    private MenuItem _setupItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETUP, 1000, 10) {
        public void run() {
        	FrontController.getIstance().showSetupView();
        }
    };

    private MenuItem _aboutItem = new MenuItem( _resources, WordPressResource.MENUITEM_ABOUT, 1010, 10) {
        public void run() {
        	FrontController.getIstance().showAboutView();
        }
    };
   
    //override onClose() to display a dialog box when the application is closed    
	public boolean onClose()   {
    	return mainController.exitApp();
    }

	public BaseController getController() {
		return mainController;
	}
}