package com.wordpress.view;

import net.rim.device.api.system.ApplicationDescriptor;
import net.rim.device.api.system.CodeModuleGroup;
import net.rim.device.api.system.CodeModuleGroupManager;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.AccountsController;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.MainController;
import com.wordpress.controller.SignUpBlogController;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Preferences;
import com.wordpress.utils.DataCollector;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BlogsListField;
import com.wordpress.view.component.SelectorPopupScreen;

public class MainView extends StandardBaseView {
	
    private MainController mainController = null;
    private ListField listaBlog;
    private BlogsListField blogListController;
    
	public MainView(MainController mainController) {
		super( _resources.getString(WordPressResource.LABEL_BLOGS), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL | USE_ALL_HEIGHT);
		this.mainController=mainController;
     
		setupUpBlogsView();

        addMenuItem(_feedbackItem);
        addMenuItem(_bugReportItem);
        addMenuItem(_aboutItem);
		addMenuItem(_addBlogItem);
		addMenuItem(_setupItem);
		addMenuItem(_accountItem);
		addMenuItem(_updateItem);
	}
	
	 public void setupUpBlogsView() {
    	listaBlog = null;
    	blogListController = null;
    	
		removeMenuItem(_deleteBlogItem);
    	removeMenuItem(_showBlogItem);
    	removeMenuItem(_notificationItem);
    	
    	BlogInfo[] blogCaricati = mainController.getApplicationBlogs();
			    	
    	//TODO: reactivate this function later: check if there are blogs with pending activation
    	/*	for (int i=0; i< blogCaricati.length; i++){
        		if(blogCaricati[i].getState() == BlogInfo.STATE_PENDING_ACTIVATION){
        			mainController.startPendingActivationSchedule();
        			break;
        		}
        	} */

    	blogListController = new BlogsListField(blogCaricati, this);        	
    	listaBlog = blogListController.getList();
    	listaBlog.setMargin(5, 0, 0, 0);
    	add(listaBlog);    

    	if(blogCaricati.length > 0) {
	    	addMenuItem(_showBlogItem);
	    	addMenuItem(_notificationItem);
	    	addMenuItem(_deleteBlogItem);
    	} 
	 }

	 //update the view of blog list entry
	 public synchronized void setBlogItemViewState(BlogInfo blogInfo) {
		 if (blogListController == null) return;
		 blogListController.setBlogState(blogInfo);
	 }
	 
	 public int getBlogsNumber () {
		 if (blogListController == null) return 0;
		 return blogListController.getBlogs().length;
	 }
	
	 public void refreshBlogList() {
		 synchronized (this) {
			 delete(listaBlog);
			 setupUpBlogsView(); //repaint entire list	        
		 }
	 }        
	    
	 public boolean defaultAction() {
			if( listaBlog  == null ) {
					return false;
			} 
			if( blogListController  != null ) {
				BlogInfo blogSelected = blogListController.getBlogSelected();
				if(blogSelected != null)
					mainController.showBlog(blogSelected);
			} 
			return true;
		}
	
    private MenuItem _showBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_SHOWBLOG, 1300, 900) {
        public void run() {
        BlogInfo blogSelected = blogListController.getBlogSelected();
        mainController.showBlog(blogSelected);
        }
    };
    
   
    //add blog menu item 
    private MenuItem _addBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_ADDBLOG, 1500, 1000) {
    	public void run() {
    		if(mainController.isLoadingBlogs()) {
    			mainController.displayMessage(_resources.getString(WordPressResource.MESSAGE_LOADING_BLOGS));
				return;
    		}
    		mainController.showWelcomeView();
    	}
    };

        
    private MenuItem _deleteBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE_BLOG, 2000, 1000) {
        public void run() {
	        BlogInfo blogSelected = blogListController.getBlogSelected();
	        mainController.deleteBlog(blogSelected);
	        refreshBlogList();
        }
    };
    
    private MenuItem _notificationItem = new MenuItem( _resources, WordPressResource.MENUITEM_NOTIFICATIONS, 9000, 1000) {
        public void run() {
        	BlogInfo[] blogs = blogListController.getBlogs();
        	FrontController.getIstance().showNotificationView(blogs);
        }
    };
   
    
    private MenuItem _setupItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETUP, 10000, 1000) {
        public void run() {
        	FrontController.getIstance().showSetupView();
        }
    };
    
    private MenuItem _accountItem = new MenuItem( _resources, WordPressResource.MENUITEM_ACCOUNTS, 11000, 1000) {
        public void run() {
        	AccountsController ctrl = new AccountsController();
    		ctrl.showView();	
        }
    };

    private MenuItem _updateItem = new MenuItem( _resources, WordPressResource.MENUITEM_CHECKUPDATE, 12000, 1000) {
    	public void run() {

    		try {
    			DataCollector dtc = new DataCollector();
    			int numBlogs = 0;
    			if(listaBlog != null){
    				numBlogs = listaBlog.getSize();
    			} 
    			dtc.checkForUpdate(numBlogs); //start data gathering here
    		} catch (Exception e) {
    			mainController.displayError(e, "Error while checking for new versions.");
    		}
    	}
    };
    
    private MenuItem _feedbackItem = new MenuItem( _resources, WordPressResource.MENUITEM_FEEDBACK, 80200, 1000) {
        public void run() {
        	try {
        		 // Pull out the App World data from the CodeModuleGroup
                String myAppName = ApplicationDescriptor.currentApplicationDescriptor().getName();
                CodeModuleGroup group = CodeModuleGroupManager.load( myAppName );
                final String myContentId = group == null ? "" : group.getProperty( "RIM_APP_WORLD_ID" );

                //if App World data is null or empty string, put the id manually
                if(myContentId != null && !myContentId.trim().equalsIgnoreCase("")){
                	Tools.openAppWorld(myContentId);
                } else {
                	Tools.openAppWorld("5802"); //id of the App given by RIM
                }
                
			} catch (Exception e) {
				Log.error(e, "Problem invoking BlackBerry App World");
				mainController.displayError("Problem invoking BlackBerry App World");
			}
        }
    };
    
    private MenuItem _bugReportItem = new MenuItem( _resources, WordPressResource.MENUITEM_BUG_REPORT, 80300, 1000) {
    	public void run() {
    		int selection = -1;
    		String[] messages = _resources.getStringArray(WordPressResource.MESSAGES_ADD_BLOG);
    		String[] blogNames = new String[]{messages[1], messages[2]};
    		String title = _resources.getString(WordPressResource.MESSAGE_WORDPRESS_VERSION);
    		SelectorPopupScreen selScr = new SelectorPopupScreen(title, blogNames);
    		selScr.pickItem();
    		selection = selScr.getSelectedItem();
    		if(selection == 0) {
    			ContactSupportView view = new ContactSupportView(mainController, false);
    			UiApplication.getUiApplication().pushScreen(view);
    		} else if(selection == 1) {
    			ContactSupportView view = new ContactSupportView(mainController, true);
    			UiApplication.getUiApplication().pushScreen(view);
    		}
    	}
    };

    private MenuItem _aboutItem = new MenuItem( _resources, WordPressResource.MENUITEM_ABOUT, 80400, 1000) {
        public void run() {
        	FrontController.getIstance().showAboutView();
        }
    };
        
    /*
     * used when background on close is activated
     */
    private MenuItem _exitItem = new MenuItem( _resources, WordPressResource.MENUITEM_EXIT, 200000, 2000) {
        public void run() {
        	WordPressCore.getInstance().exitWordPress();
        }
    };
       
    
	
    private MenuItem _activateBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_CHECK_BLOG_ACTIVATION, 1000, 900) {
        public void run() {
        	if(blogListController == null) return;
        	int blogSelected = blogListController.getList().getSelectedIndex();
        	mainController.checkForNewActivatedBlog(blogSelected);
        }
    };
    
    //Override the makeMenu method so we can add a custom menu item
    protected void makeMenu(Menu menu, int instance)
    {
    	
    	if(Preferences.getIstance().isBackgroundOnClose())
    		menu.add(_exitItem);
    	
    	//add the check for activation menu item if the blog is on pending state
    	/*if(blogListController != null) {
    		BlogInfo blogSelected = blogListController.getBlogSelected();
    		if(blogSelected.getState() == BlogInfo.STATE_PENDING_ACTIVATION)
    			menu.add(_activateBlogItem);
    	}*/
    	
        //Create the default menu.
        super.makeMenu(menu, instance);
    }
    
    //override onClose() to display a dialog box when the application 
    //menu close is selected or return btn is hitted    
	public boolean onClose() {
		Log.trace ("public boolean onClose()...");
    	return mainController.exitApp();
    }
 
	public BaseController getController() {
		return mainController;
	}
}