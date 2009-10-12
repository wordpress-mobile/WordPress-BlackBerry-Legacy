package com.wordpress.view;

import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.container.MainScreen;
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
    private VerticalFieldManager _container;
    VerticalFieldManager internalManager;
    private ListField listaBlog;
    private BlogsListField blogListController;
    
    
	public MainView(MainController mainController) {
		super( MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
		
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
        final int listWidth = wpLogoBitmapField.getBitmapWidth() - 10;
        
    	internalManager = new VerticalFieldManager( Manager.NO_VERTICAL_SCROLL | Manager.NO_VERTICAL_SCROLLBAR) {
    		public void paintBackground( Graphics g ) {
    			g.clear();
    			int color = g.getColor();
    			g.setColor( Color.LIGHTGREY );
    			g.drawBitmap(0, 0, Display.getWidth(), Display.getHeight(), _backgroundBitmap, 0, 0);
    			//g.fillRect( 0, 0, Display.getWidth(), Display.getHeight() );
    			g.setColor( color );
    		}
    		
    		protected void sublayout( int maxWidth, int maxHeight ) {
    			
    			int titleFieldHeight = 0;
    			if ( titleField != null ) {
    				titleFieldHeight = titleField.getHeight();
    			}
    			    			
    			int displayWidth = Display.getWidth(); 
    			int displayHeight = Display.getHeight();
    			
    			super.sublayout( displayWidth, displayHeight - (titleFieldHeight +5) );
    			setExtent( displayWidth, displayHeight - titleFieldHeight );
    		}
    	};
    	
    	_container = new VerticalFieldManager( Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR ){
    		protected void sublayout( int maxWidth, int maxHeight ) {
    			//super.sublayout( displayWidth, displayHeight - titleFieldHeight );
    			
    			for (int i = 0;  i < getFieldCount();  i++) {
    				Field field = getField(i);
    				if (i == 0){
    					layoutChild( field, listWidth, maxHeight );
    					
    					int x = maxWidth / 2;
    					x = x - listWidth/2;
    					
    					setPositionChild(field, x, 0);
    				} else { 
    					
    				}                        	
    			}
    			setExtent( maxWidth, maxHeight );
    		}
    	};
    	
    	internalManager.add( wpLogoBitmapField );
    	internalManager.add( _container );
    	super.add( internalManager );  
		
        setupUpBlogsView();
	
		addMenuItem(_aboutItem);
		addMenuItem(_addBlogItem);
		addMenuItem(_setupItem);
		addMenuItem(_updateItem);
	}
	
	public void add( Field field ) {
		_container.add( field );
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
		add(listaBlog);    
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
				_container.delete(listaBlog);
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
    			mainController.displayError(e, "Error while checking for new versions.");
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