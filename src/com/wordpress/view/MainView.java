package com.wordpress.view;

import java.io.IOException;

import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.MainController;
import com.wordpress.io.BlogDAO;

public class MainView extends BaseView {
	
    private MainController mainController=null;
    private ObjectListField listaBlog; 


	public MainView(MainController mainController) {
		super(_resources.getString(WordPressResource.TITLE_APPLICATION));
		this.mainController=mainController;
	
        setupUpBlogsView();
	
		addMenuItem(_aboutItem);
		addMenuItem(_addBlogItem);
		addMenuItem(_setupItem);
	}
	
	
	 public void setupUpBlogsView() {
		String[] blogCaricati = new String[0];
		try {
			blogCaricati = BlogDAO.getBlogsName();
		} catch (Exception e) {
			mainController.displayError(e, "Cannot load your blogs!");
		}

    	removeMenuItem(_deleteBlogItem);
    	removeMenuItem(_showBlogItem);
		
        listaBlog = new ObjectListField();
        if (blogCaricati.length == 0){
        	blogCaricati= new String[1];
        	blogCaricati[0]="Setup your blog...";
        } else {
        	addMenuItem(_showBlogItem);
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
            int selectedBlog = listaBlog.getSelectedIndex();
            mainController.showBlog(selectedBlog);
        }
    };
    
 

    //add blog menu item 
    private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 150, 10) {
        public void run() {
        	FrontController.getIstance().showAddBlogsView();
        }
    };

        
    private MenuItem _deleteBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE, 200, 10) {
        public void run() {
            int selectedBlog = listaBlog.getSelectedIndex();
            mainController.deleteBlog(selectedBlog);
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