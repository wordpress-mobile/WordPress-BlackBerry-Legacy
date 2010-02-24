package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogController;
import com.wordpress.controller.FrontController;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BasicListFieldCallBack;


public class BlogView extends BaseView {
	
    private BlogController controller=null;
    private VerticalFieldManager _container;
    VerticalFieldManager internalManager;
	private static final int mnuPosts = 100;
	private static final int mnuPages = 110;
	private static final int mnuComments = 120;
	private static final int mnuOptions = 130;
	private static final int mnuRefresh= 140;
	
	
	private int awaitingModeration = 0;
	private BlogListField list;
    private static final int MIN_HEIGHT = 34; //based on the image
    //the others lists have rows of 42pixels height. added 6 pixel of blank space for each row
    private static final int MAX_HEIGHT = 48; 
	
	//main menu entries
	private int[] mainMenuItems = {mnuPosts, mnuPages, mnuComments, mnuOptions, mnuRefresh};
	private String[] mainMenuItemsLabel = {
			_resources.getString(WordPressResource.BUTTON_POSTS),
			_resources.getString(WordPressResource.BUTTON_PAGES),
			_resources.getString(WordPressResource.BUTTON_COMMENTS),
			_resources.getString(WordPressResource.BUTTON_OPTIONS),
			_resources.getString(WordPressResource.BUTTON_REFRESH_BLOG)
			};
	      	
	public BlogView(BlogController _controller) {
		super( MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
		this.controller=_controller;
		
		//Set the preferred width to the image size or screen width if the image is larger than the screen width.
		EncodedImage _theImage= EncodedImage.getEncodedImageResource("wplogo_header.png");
		int _preferredWidth = -1;
        if (_theImage.getWidth() > Display.getWidth()) {
            _preferredWidth = Display.getWidth();
        }
        
        final boolean  isResizedLogo;
        final int  resizedLogoHeight;
        final int  resizedLogoWidth;
        if( _preferredWidth != -1) {
        	isResizedLogo = true;
        	EncodedImage resImg = ImageUtils.resizeEncodedImage(_theImage, _preferredWidth, _theImage.getHeight());
        	_theImage = resImg;
        	resizedLogoHeight = _theImage.getHeight();
        	resizedLogoWidth = _theImage.getWidth();
        } else {
        	isResizedLogo = false;
        	resizedLogoHeight = 0; //not used
        	resizedLogoWidth = 0; // not used
        }
        
        final BitmapField wpLogoBitmapField =  new BitmapField(_theImage.getBitmap(), Field.FIELD_HCENTER | Field.FIELD_VCENTER)
        {
        	protected void paint(Graphics graphics) {
        		super.paint(graphics);
        		if(!isResizedLogo) {
        			int fontHeight = 19;
        			graphics.setFont(Font.getDefault().derive(Font.BOLD, fontHeight));
        			graphics.setColor(Color.GRAY);
        			graphics.drawText(controller.getBlogName(), 96, 59, DrawStyle.LEFT | DrawStyle.ELLIPSIS, 207);        			
        		} else {
        		/*	//logo height : 83 pixel -> text height : 19 px
        			int fontHeight = (resizedLogoHeight * 19) / 83;
        			graphics.setFont(Font.getDefault().derive(Font.BOLD, fontHeight));
        			graphics.setColor(Color.GRAY);
        			//X point at 96px when width = 320 pixel
        			int newX = (resizedLogoWidth * 96) / 320;
        			//Y point at 59px when height = 83 pixel
        			int newY = (resizedLogoHeight * 59) / 83;
        			int newWidth = (resizedLogoWidth * 207) / 320;
        			graphics.drawText(controller.getBlogName(), newX, newY, DrawStyle.LEFT | DrawStyle.ELLIPSIS, newWidth);*/
        		}
        	}
        };
        
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
    	
    	final int listWidth = wpLogoBitmapField.getBitmapWidth() - 10;
    	
    	_container = new VerticalFieldManager( Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR ) {
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
    //	LabelField blogLabel = getLabel(controller.getBlogName(), Field.FIELD_HCENTER | LabelField.ELLIPSIS);
    	//internalManager.add(blogLabel);
    	internalManager.add( _container );
    	super.add( internalManager );
  
    	
    	BlogInfo currentBlogInfo = controller.getCurrentBlogInfo();
		awaitingModeration = currentBlogInfo.getAwaitingModeration();
		list = new BlogListField();
		
		  //Populate the ListField
      for(int count = 0; count < mainMenuItems.length; ++count) {
      	list.insert(count);
      }
	/*       
			 // Leave some space at the bottom to avoid scrolling issue
	     // on some devices. This is just a workaround
	     int allowSpace = Display.getHeight() - ( titleField.getHeight()); 
	     int screenHeight = (98 * allowSpace) / 100;
	     int rowHeight = screenHeight /  mainMenuItems.length;
	
	     if (rowHeight < MIN_HEIGHT) {
	         rowHeight = MIN_HEIGHT;
	     }
	     if (rowHeight > MAX_HEIGHT) {
	         rowHeight = MAX_HEIGHT;
	     }
	     */
  	   
	   list.setRowHeight(48);  //the others lists have rows of 42pixels height. added 6 pixel of blank space for each row
	   list.setCallback(new BlogListFieldCallBack());	
	   add(list); 
    
	   addMenuItem(_goItem);
	}
	
	public void add( Field field ) {
		_container.add( field );
	}
    
    private MenuItem _goItem = new MenuItem( _resources, WordPressResource.BUTTON_OK, 220, 10) {
        public void run() {
        	doSelection();
        }
    };


    
    protected void onExposed() {
    	super.onExposed();
        Log.debug(">>> onExposed BlogView");
        awaitingModeration = controller.getCurrentBlogInfo().getAwaitingModeration();
        if(list != null)
        	list.invalidate();
    }
        
    
    protected void onDisplay() {
    	
        super.onDisplay();
        Log.debug(">>> onDisplay BlogView");
        awaitingModeration = controller.getCurrentBlogInfo().getAwaitingModeration();
        if(list != null)
        	list.invalidate();
    }
    
    private void doSelection() {

		int i = mainMenuItems[list.getSelectedIndex()];

		switch (i) {

		case (mnuPosts):
			controller.showPosts();
			break;
		case (mnuPages):
			controller.showPages();
			break;
		case (mnuComments):
			controller.showComments();
			break;

		case (mnuOptions):
			controller.showBlogOptions();
			break;

		case (mnuRefresh):
			controller.refreshBlog();
			break;

		default:
			controller.displayError("There was an error with the request.");
			break;

		}
	}
   
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		doSelection();
		return true;
	}

	
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose() {
		//controller.backCmd();
		FrontController.getIstance().backAndRefreshView(true);	
		return true;
	}

	public BaseController getController() {
		return controller;
	}   
	
	private class BlogListFieldCallBack extends BasicListFieldCallBack {
		
		private Bitmap imgFolder = Bitmap.getBitmapResource("drafts-folder.png");
		private Bitmap imgSettings = Bitmap.getBitmapResource("settings.png");
		private Bitmap imgRefresh = Bitmap.getBitmapResource("refresh.png");  
		private Bitmap icon;
		
		// We are going to take care of drawing the item.
		public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
			
			String label = mainMenuItemsLabel[index];
			
			if ( mainMenuItems[index] == mnuPosts) {
				icon = imgFolder;
			}   
			if ( mainMenuItems[index] == mnuPages) {
				icon = imgFolder;
			}  
			if ( mainMenuItems[index] == mnuComments) {
				icon = imgFolder;
				
				if(awaitingModeration > 0)
					label += " ("+awaitingModeration+")";
			}
			if ( mainMenuItems[index] == mnuOptions) {
				icon = imgSettings;
			}
			if ( mainMenuItems[index] == mnuRefresh) {
				icon = imgRefresh;
			}
						
			Font originalFont = graphics.getFont();
			int originalColor = graphics.getColor();
			int height = list.getRowHeight();
			
			/*
			 * 42px of row
			 * 6px blank space
			 */
			height = height - 6;
			width = width - 10;
			//y = y+3;
			
			//drawXXX(graphics, 0, y, width, listField.getRowHeight());
			drawBackground(graphics, 5, y, width, height, listField.getSelectedIndex() ==  index);
			drawBorder(graphics, 5, y, width, height);
			int leftImageWidth = drawLeftImage(graphics, 5, y, height, icon);
			drawText(graphics, leftImageWidth+5, y, width  - leftImageWidth, height, label, listField.getSelectedIndex() ==  index);
			
			graphics.setFont(originalFont);
			graphics.setColor(originalColor);
		}
		
		protected void drawBorder(Graphics graphics, int x, int y, int width,	int height) {
			graphics.setColor(Color.GRAY);
			graphics.drawLine(x-1, y , x + width-1, y);
			
			graphics.drawLine(x-1, y, x-1 , y + height-1); //linea verticale sx
			graphics.drawLine(x + width, y-1, x + width , y + height-1); //linea verticale dx
			
			graphics.drawLine(x-1, y + height - 1, x + width-1, y + height - 1);
		}
		
		public Object get(ListField listField, int index) {
			return null;
		} 
	}
	
	private class BlogListField extends ObjectListField {
			
	   protected void drawFocus(Graphics graphics, boolean on) { }
	   

        protected int moveFocus(int amount, int status, int time) {
    		
    		int oldSelection = getSelectedIndex();
    		if(oldSelection != -1) {
    			invalidate(oldSelection);
    		}
    		
    		// Forward the call
    		int ret = super.moveFocus(amount, status, time);
    		
    		int newSelection = getSelectedIndex();
    		// Get the next enabled item;
    		if(newSelection != -1) {
    			invalidate(newSelection);
    		}
            
            //invalidate(); //we can invalidate only the 2 involved rows
            return ret;
        }
        
		protected void moveFocus(int x, int y, int status, int time) {
            int oldSelection = getSelectedIndex();
            super.moveFocus(x, y, status, time);
            int newSelection = getSelectedIndex();
            
            if(oldSelection != -1) {
            	invalidate(oldSelection);
            }
            
            if(newSelection != -1) {
            	invalidate(newSelection);
            } else {
            	setSelectedIndex(oldSelection);
            	invalidate(oldSelection);
            }
        }
        
	}
}