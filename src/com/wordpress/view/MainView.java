//#preprocess
package com.wordpress.view;

import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.MainController;
import com.wordpress.model.BlogInfo;
import com.wordpress.model.Preferences;
import com.wordpress.utils.DataCollector;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BaseButtonField;
import com.wordpress.view.component.BlogsListField;

public class MainView extends BaseView {
	
    private MainController mainController=null;
    private VerticalFieldManager _scrollerManager;
    VerticalFieldManager internalManager;
    private ListField listaBlog;
    private BlogsListField blogListController;
    
	public MainView(MainController mainController) {
		super( MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL | USE_ALL_HEIGHT);
		
		this.mainController=mainController;
		
		//Set the preferred width to the image size or screen width if the image is larger than the screen width.
		EncodedImage _theImage= EncodedImage.getEncodedImageResource("wplogo_header.png");
		int _preferredWidth = -1;
        if (_theImage.getWidth() > Display.getWidth()) {
            _preferredWidth = Display.getWidth();
        }
        if( _preferredWidth != -1) {        	
        	EncodedImage resImg = ImageUtils.resizeEncodedImage(_theImage, _preferredWidth, _theImage.getHeight());
        	_theImage = resImg;
        }
        
        final BitmapField wpLogoBitmapField =  new BitmapField(_theImage.getBitmap(), Field.FIELD_HCENTER | Field.FIELD_VCENTER);
        //final int listWidth = wpLogoBitmapField.getBitmapWidth() - 10;

    	internalManager = new VerticalFieldManager( Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR) {
    		public void paintBackground( Graphics g ) {
    			g.clear();
    			//int color = g.getColor();
    			//g.setColor(0xefebef);
    			//g.fillRect( 0, 0, Display.getWidth(), Display.getHeight() );
    			g.drawBitmap(0, 0, Display.getWidth(), Display.getHeight(), _backgroundBitmap, 0, 0);
    			//g.setColor( color );
    		}
    	};
        
    	_scrollerManager = new VerticalFieldManager( Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR | USE_ALL_HEIGHT | USE_ALL_WIDTH );
    	
    	internalManager.add( wpLogoBitmapField );
    	internalManager.add( _scrollerManager );
    	super.add( internalManager );
		
        setupUpBlogsView();
	
		addMenuItem(_aboutItem);
		addMenuItem(_addBlogItem);
		addMenuItem(_setupItem);
		addMenuItem(_updateItem);
	}
		
	public void add( Field field ) {
		_scrollerManager.add( field );
	}
	
	 public void setupUpBlogsView() {
    	listaBlog = null;
    	blogListController = null;
    	
		removeMenuItem(_deleteBlogItem);
    	removeMenuItem(_showBlogItem);
    	removeMenuItem(_notificationItem);
    	
    	BlogInfo[] blogCaricati = mainController.getApplicationBlogs();
    	
    	/*try {
    		Hashtable blogsInfo = BlogDAO.getBlogsInfo();
    		 blogCaricati =  (BlogInfo[]) blogsInfo.get("list");
    			if(blogsInfo.get("error") != null )
    				mainController.displayError((String)blogsInfo.get("error"));
		} catch (Exception e) {
			Log.error(e, "Error while reading stored blog");
			mainController.displayError("Error while reading stored blogs");
		}*/
		
        if (blogCaricati.length == 0) {
        	setUpPromoView();
        } else {
        	blogListController = new BlogsListField(blogCaricati);        	
        	listaBlog = blogListController.getList();
        	addMenuItem(_showBlogItem);
        	addMenuItem(_notificationItem);
        	addMenuItem(_deleteBlogItem);
        	add(listaBlog);    
        }
	 }
	 
	 private void setUpPromoView() {
		 
		 HorizontalFieldManager buttonsManagerOne = new HorizontalFieldManager(Field.FIELD_HCENTER);
		 HorizontalFieldManager buttonsManagerTwo = new HorizontalFieldManager(Field.FIELD_HCENTER);
		 int width = Display.getWidth();
		
		 
		 BaseButtonField buttonHaveBlog = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_HAVE_A_WP_BLOG), 
				 ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
		 buttonHaveBlog.setChangeListener(new FieldChangeListener() {
			 public void fieldChanged(Field field, int context) {
				 mainController.addBlogs();
			 }
		 });

		 if (width > 320)
			 buttonHaveBlog.setMargin( 15, 30, 15, 30 );
		 else
			 buttonHaveBlog.setMargin( 4, 4, 4, 4 );
		 
		 BaseButtonField buttonGetFreeBlog = GUIFactory.createButton(_resources.getString(WordPressResource.BUTTON_NEW_TO_WP_BLOG), 
				 ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
		 buttonGetFreeBlog.setChangeListener(new FieldChangeListener() {
			 public void fieldChanged(Field field, int context) {
				 Tools.openWordPressSignUpURL("MainScreen"); 
			 }
		 });
		 
		 if (width > 320)
			 buttonGetFreeBlog.setMargin( 5, 30, 15 , 30 );
		 else
			 buttonGetFreeBlog.setMargin( 4, 4, 4, 4 );
		
		 buttonsManagerOne.add(buttonHaveBlog);
		 buttonsManagerTwo.add(buttonGetFreeBlog);
		 _scrollerManager.add(buttonsManagerOne);
		 _scrollerManager.add(buttonsManagerTwo);
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
	
	 
    /**
     * Overrides default implementation.  Performs the show blog action if the 
     * 4ways trackpad was clicked; otherwise, the default action occurs.
     * 
     * @see net.rim.device.api.ui.Screen#navigationClick(int,int)
     */
	protected boolean navigationClick(int status, int time) {
		Log.trace(">>> navigationClick");
		
		if ((status & KeypadListener.STATUS_TRACKWHEEL) == KeypadListener.STATUS_TRACKWHEEL) {
			Log.trace("Input came from the trackwheel");
			// Input came from the trackwheel
			return super.navigationClick(status, time);
			
		} else if ((status & KeypadListener.STATUS_FOUR_WAY) == KeypadListener.STATUS_FOUR_WAY) {
			Log.trace("Input came from a four way navigation input device");
			return defaultAction();
		}
		return super.navigationClick(status, time);
	}
	
    /**
     * Overrides default.  Enter key will take show blog action on selected blog.
     *  
     * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
     * 
     */
	protected boolean keyChar(char c, int status, int time) {
		Log.trace(">>> keyChar");
		// Close this screen if escape is selected.
		if (c == Characters.ENTER) {
			return defaultAction();
		}
		
		return super.keyChar(c, status, time);
	}
	 
	private boolean defaultAction() {
		
		if( listaBlog  == null ) {
				return false;
		} 
		
		if( blogListController  != null ) {
			BlogInfo blogSelected = blogListController.getBlogSelected();
	        mainController.showBlog(blogSelected);
		} else {
			mainController.addBlogs();
		}
		return true;
	}
	
	//#ifdef IS_OS47_OR_ABOVE
	protected boolean touchEvent(TouchEvent message) {
		Log.trace(">>> touchEvent");
		int eventCode = message.getEvent();
		// Get the screen coordinates of the touch event
		int touchX = message.getX(1);
		int touchY = message.getY(1);
		
		if(eventCode == TouchEvent.CLICK) {
			return defaultAction();
		}
	return super.touchEvent(message);	
	}
	//#endif
	
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

        
    private MenuItem _deleteBlogItem = new MenuItem( _resources, WordPressResource.MENUITEM_DELETE_BLOG, 200, 10) {
        public void run() {
	        BlogInfo blogSelected = blogListController.getBlogSelected();
	        mainController.deleteBlog(blogSelected);
	        refreshBlogList();
        }
    };
    
    public void refreshBlogList() {
    	synchronized (this) {
			_scrollerManager.deleteAll();
	        setupUpBlogsView(); //repaint entire list
    	}
    }        
    
    private MenuItem _notificationItem = new MenuItem( _resources, WordPressResource.MENUITEM_NOTIFICATIONS, 900, 10) {
        public void run() {
        	BlogInfo[] blogs = blogListController.getBlogs();
        	FrontController.getIstance().showNotificationView(blogs);
        }
    };
   
    
    private MenuItem _setupItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETUP, 1000, 10) {
        public void run() {
        	FrontController.getIstance().showSetupView();
        }
    };

    private MenuItem _updateItem = new MenuItem( _resources, WordPressResource.MENUITEM_CHECKUPDATE, 1010, 10) {
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
    
    private MenuItem _aboutItem = new MenuItem( _resources, WordPressResource.MENUITEM_ABOUT, 1020, 10) {
        public void run() {
        	FrontController.getIstance().showAboutView();
        }
    };
   
    
    /*
     * used when background on close is activated
     */
    private MenuItem _exitItem = new MenuItem( _resources, WordPressResource.MENUITEM_EXIT, 102000, 2000) {
        public void run() {
        	WordPressCore.getInstance().exitWordPress();
        }
    };
   
    
    //Override the makeMenu method so we can add a custom menu item
    protected void makeMenu(Menu menu, int instance)
    {
    	
    	if(Preferences.getIstance().isBackgroundOnClose())
    		menu.add(_exitItem);
   
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