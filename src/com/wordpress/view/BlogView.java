package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogController;
import com.wordpress.controller.FrontController;


public class BlogView extends BaseView {
	
    private BlogController controller=null;

	private static final int mnuPosts = 100;
	private static final int mnuPages = 110;
	private static final int mnuComments = 120;
	private static final int mnuOptions = 130;
	private static final int mnuRefresh= 140;

	private BlogListField list;
    private static final int MIN_HEIGHT = 36;
    private static final int MAX_HEIGHT = 42;
	
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
		super(_controller.getBlogName(), Field.FIELD_HCENTER);
		this.controller=_controller;
		list = new BlogListField();
		
		  //Populate the ListField
        for(int count = 0; count < mainMenuItems.length; ++count) {
        	list.insert(count);
        }
        
		 // Leave some space at the bottom to avoid scrolling issue
       // on some devices. This is just a workaround
       int allowSpace = Display.getHeight() - titleField.getHeight(); 
       int screenHeight = (98 * allowSpace) / 100;
       int rowHeight = screenHeight /  mainMenuItems.length;

       if (rowHeight < MIN_HEIGHT) {
           rowHeight = MIN_HEIGHT;
       }
       if (rowHeight > MAX_HEIGHT) {
           rowHeight = MAX_HEIGHT;
       }
		list.setRowHeight(rowHeight);
               
        add(list);   
        addMenuItem(_goItem);
	}
	
    
    private MenuItem _goItem = new MenuItem( _resources, WordPressResource.BUTTON_OK, 220, 10) {
        public void run() {
        	doSelection();
        }
    };

    

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

        
  /*
	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
			return  this.onClose();
		} else if (c == Characters.ENTER) {
			doSelection();
			return true;
		}

		return super.keyChar(c, status, time);
	}
*/
    
	 // Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		doSelection();
		return true;
	}

	
	//override onClose() to by-pass the standard dialog box when the screen is closed    
	public boolean onClose()   {
		//controller.backCmd();
		FrontController.getIstance().backAndRefreshView(true);	
		return true;
	}

	public BaseController getController() {
		return controller;
	}   
	
	private class BlogListField extends ObjectListField {
		
		Bitmap bg = Bitmap.getBitmapResource("bg_light.png");
		Bitmap bgSelected = Bitmap.getBitmapResource("bg_blue.png");
        Bitmap imgFolder = Bitmap.getBitmapResource("drafts-folder.png");
        Bitmap imgSettings = Bitmap.getBitmapResource("settings.png");
        Bitmap imgRefresh = Bitmap.getBitmapResource("refresh.png");  
	    private Bitmap icon;
	    final int PADDING     = 2;
	    
	    // We are going to take care of drawing the item.
	    public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
	                
	        if ( mainMenuItems[index] == mnuPosts) {
	            icon = imgFolder;
	        }   
	        if ( mainMenuItems[index] == mnuPages) {
	            icon = imgFolder;
	        }  
	        if ( mainMenuItems[index] == mnuComments) {
	            icon = imgFolder;
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
	        
	        //drawXXX(graphics, 0, y, width, listField.getRowHeight());
	        drawBackground(graphics, 0, y, width, height, listField.getSelectedIndex() ==  index);
	        drawBorder(graphics, 0, y, width, height);
	        int leftImageWidth = drawLeftImage(graphics, 0, y, height, icon);
	        drawText(graphics, leftImageWidth, y, width  - leftImageWidth, height, mainMenuItemsLabel[index], listField.getSelectedIndex() ==  index);
	        
	        graphics.setFont(originalFont);
	        graphics.setColor(originalColor);
	    }
	    
	    private int drawText(Graphics graphics, int x, int y, int width, int height, String title, boolean selected) {
	        int fontHeight = ((int) ((3* height) / 5)) - (PADDING * 2);
	        graphics.setFont(Font.getDefault().derive(Font.BOLD, fontHeight));

	        if (selected) {
	            graphics.setColor(Color.BLACK);
	        } else {
	            graphics.setColor(Color.GRAY);
	        }

	        if (title != null) {
	        	// Title is vertically centered
	        return   graphics.drawText(title, x + PADDING + 3, y + PADDING + 2 + (fontHeight / 2), DrawStyle.LEFT
	                    | DrawStyle.TOP | DrawStyle.ELLIPSIS, width - x - (PADDING * 2));
	        }

	        return 0;
	    }
	    
	    
	    private int drawLeftImage(Graphics graphics, int x, int y, int height, Bitmap leftImage) {
	       
	        int imageWidth = leftImage.getWidth();
	        int imageHeight = leftImage.getHeight();
	        int imageTop = y + ((height - imageHeight) / 2);
	        int imageLeft = x + ((height - imageWidth) / 2);

	        // Image on left side
	        graphics.drawBitmap(imageLeft, imageTop, imageWidth, imageHeight, leftImage, 0, 0);

	        return height;
	    }
	    
	    private void drawBackground(Graphics graphics, int x, int y, int width, int height, boolean selected) {
	    	Bitmap toDraw = null;
	    	if (selected) {
	    		toDraw = bgSelected;
	    	} else {
	    		toDraw = bg;
	    	}
	    	int imgWidth = toDraw.getWidth();
	    	while (width > -2) {
	    		graphics.drawBitmap(x - 1, y - 1, width + 2, height + 1,
	    				toDraw, 0, 0);
	    		width -= imgWidth;
	    		// Overlap a little bit to avoid border issues
	    		x += imgWidth - 2;
	    	}
	    }
	    
	    private void drawBorder(Graphics graphics, int x, int y, int width,	int height) {
	    	graphics.setColor(Color.GRAY);
	    	graphics.drawLine(x, y - 1, x + width, y - 1);
	    	graphics.drawLine(x, y + height - 1, x + width, y + height - 1);
	    }
	    
        protected int moveFocus(int amount, int status, int time) {
            // Forward the call
            int ret = super.moveFocus(amount, status, time);
            invalidate(); //we can invalidate only the 2 involved rows
            return ret;
        }
	}
}