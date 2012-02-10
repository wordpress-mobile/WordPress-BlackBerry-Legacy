//#preprocess
package com.wordpress.view;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;

import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BaseController;
import com.wordpress.controller.BlogController;
import com.wordpress.controller.FrontController;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.log.Log;
import com.wordpress.view.component.BasicListFieldCallBack;


//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
//#endif

public class BlogView extends StandardBaseView {
	
    private BlogController controller=null;
    
	private static final int mnuPosts = 100;
	private static final int mnuPages = 110;
	private static final int mnuComments = 120;
	private static final int mnuMedia = 130;
	private static final int mnuStats = 140;
	private static final int mnuOptions = 150;
	private static final int mnuRefresh= 160;
	private static final int mnuDashboard= 170;
	
	private int awaitingModeration = 0;
	private BlogListField list;
	
	//main menu entries
	private int[] mainMenuItems = {mnuPosts, mnuPages, mnuComments, mnuMedia, mnuStats, mnuOptions, mnuRefresh, mnuDashboard};
	private String[] mainMenuItemsLabel = {
			_resources.getString(WordPressResource.BUTTON_POSTS),
			_resources.getString(WordPressResource.BUTTON_PAGES),
			_resources.getString(WordPressResource.BUTTON_COMMENTS),
			_resources.getString(WordPressResource.BUTTON_MEDIA),
			_resources.getString(WordPressResource.BUTTON_STATS),
			_resources.getString(WordPressResource.BUTTON_OPTIONS),
			_resources.getString(WordPressResource.BUTTON_REFRESH_BLOG),
			_resources.getString(WordPressResource.MENUITEM_DASHBOARD)
			};
	      	
	public BlogView(BlogController _controller) {
		super(_controller.getBlogName(), MainScreen.NO_VERTICAL_SCROLL | Manager.NO_HORIZONTAL_SCROLL);
		this.controller=_controller;  
    	BlogInfo currentBlogInfo = controller.getCurrentBlogInfo();
		awaitingModeration = currentBlogInfo.getAwaitingModeration();
		list = new BlogListField();
		list.setMargin(5, 0, 0, 0);
		
		//Populate the ListField
		for(int count = 0; count < mainMenuItems.length; ++count) {
			list.insert(count);
		}

	   list.setRowHeight(BasicListFieldCallBack.getRowHeightForSingleLineRow() 
			   + BasicListFieldCallBack.SPACE_BETWEEN_ROW   //space between rows
			   + BasicListFieldCallBack.SPACE_BETWEEN_ROW); //a little bit bigger
	   list.setCallback(new BlogListFieldCallBack());	
	   add(list); 
    
	   
	   for (int i=0; i<mainMenuItems.length; i++){
		   final int currentIndex = i;
		   addMenuItem(
				   new MenuItem(mainMenuItemsLabel[i], (220+i), 10) {
				        public void run() {
				        	doSelection(mainMenuItems[currentIndex]);
				        }
				    }
		   );  
	   }
	}
	        
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
    
    private void doSelection(int i) {

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
		case (mnuMedia):
			controller.showMediaLibrary();
			break;
		case (mnuStats):
			controller.showStats();
			break;
		case (mnuOptions):
			controller.showBlogOptions();
			break;
		case (mnuRefresh):
			controller.refreshBlog();
			break;
		case (mnuDashboard):
			controller.openDashboard();
			break;
		default:
			controller.displayError("There was an error with the request.");
			break;

		}
	}
   
	public boolean onClose() {
		//controller.backCmd();
		FrontController.getIstance().backAndRefreshView(true);	
		return true;
	}

	public BaseController getController() {
		return controller;
	}   
	
	private class BlogListFieldCallBack extends BasicListFieldCallBack {
		
		private Bitmap imgWritePost = Bitmap.getBitmapResource("write_post.png");
		private Bitmap imgWritePage = Bitmap.getBitmapResource("write_page.png");
		private Bitmap imgComments = Bitmap.getBitmapResource("comments.png");
		private Bitmap imgMedia = Bitmap.getBitmapResource("media_library.png");
		private Bitmap imgSettings = Bitmap.getBitmapResource("settings.png");
		private Bitmap imgRefresh = Bitmap.getBitmapResource("refresh.png");
		private Bitmap imgStats = Bitmap.getBitmapResource("stats.png");  
		private Bitmap imgDashboard = Bitmap.getBitmapResource("dashboard.png");
		
		private Bitmap icon;
		
		// We are going to take care of drawing the item.
		public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
			
			String label = mainMenuItemsLabel[index];
			
			if ( mainMenuItems[index] == mnuPosts) {
				icon = imgWritePost;
			}   
			if ( mainMenuItems[index] == mnuPages) {
				icon = imgWritePage;
			}
			if ( mainMenuItems[index] == mnuMedia) {
				icon = imgMedia;
			}
			if ( mainMenuItems[index] == mnuStats) {
				icon = imgStats;
			}
			if ( mainMenuItems[index] == mnuComments) {
				icon = imgComments;
				
				if(awaitingModeration > 0)
					label += " ("+awaitingModeration+")";
			}
			if ( mainMenuItems[index] == mnuOptions) {
				icon = imgSettings;
			}
			if ( mainMenuItems[index] == mnuRefresh) {
				icon = imgRefresh;
			}
			if ( mainMenuItems[index] == mnuDashboard) {
				icon = imgDashboard;
			}
			
			Font originalFont = graphics.getFont();
			int originalColor = graphics.getColor();
			int height = list.getRowHeight();
			
			/*
			 * Example
			 * 42px of row
			 * 6px blank space
			 */
			height = height - SPACE_BETWEEN_ROW;
			width = width - 10; //add some space to left and right
					
			drawBackground(graphics, 5, y, width, height, listField.getSelectedIndex() ==  index);
			drawBorder(graphics, 5, y, width, height);

			drawLeftImage(graphics, 10, y, height, icon);			
			int leftImageWidth = 10 + 32;
			drawSingleLineText(graphics, leftImageWidth, y, width - leftImageWidth, height, label, listField.getSelectedIndex() ==  index, Font.PLAIN);
			
			graphics.setFont(originalFont);
			graphics.setColor(originalColor);
		}
		
		protected void drawBorder(Graphics graphics, int x, int y, int width, int height) {
			graphics.setColor(Color.DARKGRAY);
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
			
		 // Handle trackball clicks.
		protected boolean navigationClick(int status, int time) {
			doSelection(mainMenuItems[list.getSelectedIndex()]);
			return true;
		}
		
	    protected boolean keyChar(char key, int status, int time)
	    {
	        
	        //If the spacebar was pressed...
	        if (key == Characters.SPACE || key == Characters.ENTER)
	        {
	        	doSelection(mainMenuItems[list.getSelectedIndex()]);
	        	return true;
	        }
	        return false;
	    }
	    
		//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
		protected boolean touchEvent(TouchEvent message) {
			Log.trace("touchEvent");
			
			if(!this.getContentRect().contains(message.getX(1), message.getY(1)))
			{       			
				return true; //we are return true bc we are eating the event even if it outside the list
			} 
			
			int eventCode = message.getEvent();

			if(WordPressInfo.isForcelessTouchClickSupported) {
				if (eventCode == TouchEvent.GESTURE) {
					TouchGesture gesture = message.getGesture();
					int gestureCode = gesture.getEvent();
					if (gestureCode == TouchGesture.TAP) {
						doSelection(mainMenuItems[list.getSelectedIndex()]);;
						return true;
					}
				} 
				return false;
			} else {
				if(eventCode == TouchEvent.CLICK) {
					doSelection(mainMenuItems[list.getSelectedIndex()]);
					return true;
				}else if(eventCode == TouchEvent.DOWN) {
				} else if(eventCode == TouchEvent.UP) {
				} else if(eventCode == TouchEvent.UNCLICK) {
					//return true; //consume the event: avoid context menu!!
				} else if(eventCode == TouchEvent.CANCEL) {
				}
				return false; 
				//return super.touchEvent(message);
			}
		}
		//#endif
		
		
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