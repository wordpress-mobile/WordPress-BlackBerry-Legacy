package com.wordpress.view;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.FrontController;
import com.wordpress.controller.MainController;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.DataCollector;
import com.wordpress.utils.ImageUtils;
import com.wordpress.view.component.BlogsListField;

public class MainView extends BaseView {
	
    private MainController mainController=null;
    private ListField listaBlog;
    private BlogsListField blogListController;
    private int PADDING = 20;
    
    private BitmapField wpLogoBitmapField;
    private VerticalFieldManager   subManager;
    
	public MainView(MainController mainController) {
		super( Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR 
				| VerticalFieldManager.USE_ALL_WIDTH | VerticalFieldManager.USE_ALL_HEIGHT);
		
		this.mainController=mainController;
		
		//Set the preferred width to the image size or screen width if the image is larger than the screen width.
		EncodedImage _theImage= EncodedImage.getEncodedImageResource("wplogo.png");
		int _preferredWidth = -1;
        if (_theImage.getWidth() > Display.getWidth()) {
            _preferredWidth = Display.getWidth();
        }
        if( _preferredWidth != -1) {        	
        	EncodedImage resImg = ImageUtils.resizeEncodedImage(_theImage, _preferredWidth, _theImage.getHeight());
        	_theImage = resImg;
        }
		wpLogoBitmapField =  new BitmapField(_theImage.getBitmap(), Field.FIELD_HCENTER| Field.FIELD_VCENTER);
		
		//this manager is used for the static background image
		VerticalFieldManager  mainManager = new VerticalFieldManager(VerticalFieldManager.USE_ALL_WIDTH | VerticalFieldManager.USE_ALL_HEIGHT 
				| Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR )
        {            
            public void paint(Graphics graphics)
            {
            	//height and width could change if the user tilt the device.
            	//we use os4.2 in which we cannot access to tilt events
            	//and so we need to retrive height and width at every paint...
               int deviceWidth = Display.getWidth(); 
               int deviceHeight = Display.getHeight();
               graphics.clear();
               graphics.drawBitmap(0, 0, deviceWidth, deviceHeight, _backgroundBitmap, 0, 0);                       
               super.paint(graphics);
            } 
            
            protected void sublayout( int width, int height ) {
                    int x = 0;
                    int y = 0;

                    for (int i = 0;  i < getFieldCount();  i++) {
                        Field field = getField(i);
                        if (i != 0){
                        	layoutChild( field, width - PADDING, height-y );
                        	setPositionChild(field, x+(PADDING/2), y);
                        } else { 
                        	 //the image field
                        	x = width / 2;
                        	layoutChild( field, width , height-y );
                        	setPositionChild(field, x - field.getPreferredWidth()/2, y);
                        }                        	
                        x = 0 ;
                        y += field.getHeight();
                    }
                 setExtent(width, height);
            } 
        };
        
        //this manger is used for adding the componentes
        subManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR  );

        mainManager.add(wpLogoBitmapField);
        mainManager.add(subManager);
        //finally add the mainManager over the screen
        this.add(mainManager);    
		
        setupUpBlogsView();
	
		addMenuItem(_aboutItem);
		addMenuItem(_addBlogItem);
		addMenuItem(_setupItem);
		addMenuItem(_updateItem);
	}
	
	
	 public void setupUpBlogsView() {
    	
		removeMenuItem(_deleteBlogItem);
    	removeMenuItem(_showBlogItem);

    	BlogInfo[] blogCaricati = mainController.getBlogsList();
				
        if (blogCaricati.length == 0){
        	blogCaricati = new BlogInfo[0];
        } else {
        	addMenuItem(_showBlogItem);
        	addMenuItem(_deleteBlogItem);
        }
    	blogListController = new BlogsListField(blogCaricati);
		this.listaBlog = blogListController.getList();
		subManager.add(listaBlog);    
	 }
	 
	 
	 //update the view of blog list entry
	 public synchronized void setBlogItemViewState(BlogInfo blogInfo) {
		 blogListController.setBlogState(blogInfo);
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
    
    public void refreshBlogList() {
    	synchronized (this) {
	        if(listaBlog != null){
				subManager.delete(listaBlog);
				setupUpBlogsView(); //repaint entire list
			}
    	}
    }
    
    
    private MenuItem _setupItem = new MenuItem( _resources, WordPressResource.MENUITEM_SETUP, 1000, 10) {
        public void run() {
        	FrontController.getIstance().showSetupView();
        }
    };

    private MenuItem _updateItem = new MenuItem( _resources, WordPressResource.MENUITEM_CHECKUPDATE, 1010, 10) {
        public void run() {
        	
        	try {
    			DataCollector dtc = new DataCollector();
    			dtc.checkForUpdate(listaBlog.getSize()); //start data gathering here
    		} catch (Exception e) {
    			mainController.displayError(e, "Error while check for a new version");
    		}
        	
        }
    };
    
    private MenuItem _aboutItem = new MenuItem( _resources, WordPressResource.MENUITEM_ABOUT, 1020, 10) {
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