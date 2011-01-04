package com.wordpress.view.component;

import java.util.Timer;
import java.util.TimerTask;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.GIFEncodedImage;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.BlogInfo;
import com.wordpress.utils.log.Log;

/**
 * This class is a wrapper around a list field that we have used to
 * show Blogs on the Main Screen.
 * The Blog status is showed as Image.
 * 
 * @author dercoli
 *
 */

public class BlogsListField {
    
	private BlogInfo[] _listData;
    private ListField _listField;
    private ListCallBack listFieldCallBack = null;
	
    //throbber variables 
    private Timer timer;
	private TimerTask timerTask;
	private ThrobberRenderer throbberRenderer = null;
	private boolean[] tbsIndex = null;
    
  //create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
       
    public BlogsListField(BlogInfo[] blogCaricati) {  
    	listFieldCallBack = new ListCallBack();
    	
    	_listField = new ListField() {
    		protected void drawFocus(Graphics graphics, boolean on) { } //remove the standard focus highlight
    		
    		protected int moveFocus(int amount, int status, int time) {
    			// Forward the call
    			int ret = super.moveFocus(amount, status, time);
    			invalidate(); //we can invalidate only the 2 involved rows
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
    		
    		/* (non-Javadoc)
    		 * @see net.rim.device.api.ui.Field#onDisplay()
    		 */
    		protected void onDisplay() {
    			super.onDisplay();
    			timerTask = new AnimationTimerTask();
    			timer.schedule(timerTask, 200, 100);
    		}
    		
    		/* (non-Javadoc)
    		 * @see net.rim.device.api.ui.Field#onUndisplay()
    		 */
    		protected void onUndisplay() {
    			timerTask.cancel();
    			super.onUndisplay();
    		}
    	};
    	
    	_listField.setEmptyString(_resources.getString(WordPressResource.LABEL_ADD_YOUR_BLOG), DrawStyle.LEFT);
    	_listData= blogCaricati;
    	_listField.setRowHeight(48);//the others lists have rows of 42pixels height. added 6 pixel of blank space for each row
    	//Set the ListFieldCallback
    	_listField.setCallback(listFieldCallBack);
    	
    	int elementLength = blogCaricati.length;
    	tbsIndex = new boolean[elementLength];
    	//Populate the ListField and the throbbers vectors
    	for(int count = 0; count < elementLength; ++count)
    	{       
    		_listField.insert(count);
    		
    		int stato = blogCaricati[count].getState();
    		if (stato == BlogInfo.STATE_LOADING || stato == BlogInfo.STATE_ADDED_TO_QUEUE) {
    			tbsIndex[count] = true;
    		}
    		else
    			tbsIndex[count] = false;
    	}
    	//init the timer for throbbers
    	this.timer = new Timer();
    	EncodedImage _theImage= EncodedImage.getEncodedImageResource("loading-gif.bin");
    	throbberRenderer = new ThrobberRenderer((GIFEncodedImage) _theImage);
    }
        
    
    public ListField getList() {
		return _listField;
	}
    
    public BlogInfo getBlogSelected(){
        //Get the index of the selected row.
        int index = _listField.getSelectedIndex();
        
        //Get the ChecklistData for this row.
        BlogInfo data = (BlogInfo)_listData[index];
        
        return data;
    }
    
    
    public BlogInfo[] getBlogs(){
        return _listData;
    }
    
    public void setBlogState(BlogInfo blogInfo){
    	//Populate the ListField
    	Log.trace(">>> setBlogState");
    	for(int count = 0; count < _listData.length; ++count)
    	{
    		BlogInfo blog = _listData[count];
    		
    		if (blogInfo.equals(blog) )		
    		{
    			//blog.setState(blogInfo.getState());
    			_listData[count]= blogInfo;
    			//Invalidate the modified row of the ListField.
    			_listField.invalidate(count);
    			
    	  		int stato = blogInfo.getState();
        		if (stato == BlogInfo.STATE_LOADING || stato == BlogInfo.STATE_ADDED_TO_QUEUE) {
        			tbsIndex[count] = true;
        		}
        		else
        			tbsIndex[count] = false;
    			
    		}
    	}
    }
    
    private class ListCallBack extends BasicListFieldCallBack {
		private Bitmap imgImportant = Bitmap.getBitmapResource("important.png");
		private Bitmap imgQueue = Bitmap.getBitmapResource("enqueued.png");
		private Bitmap wp_blue = Bitmap.getBitmapResource("wp_blue-s.png");
		private Bitmap wp_grey = Bitmap.getBitmapResource("wp_grey-s.png");
		private Bitmap pendingActivation = Bitmap.getBitmapResource("pending_activation.png");
		
        // Draws the list row.
    	public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) {
    		// Get the blog info for the current row.
    		BlogInfo currentRow = (BlogInfo) this.get(list, index);
    		
    		Bitmap icon = null;
    		
    		Font originalFont = graphics.getFont();
    		int originalColor = graphics.getColor();
    		int height = list.getRowHeight();
    		
    		int stato = currentRow.getState();
    		if(stato == BlogInfo.STATE_PENDING_ACTIVATION) {
    			icon = pendingActivation;
    		} else if (stato == BlogInfo.STATE_LOADING) { 
    			icon = null;
    		} else if (stato == BlogInfo.STATE_ADDED_TO_QUEUE) {
    			icon = imgQueue; 
    		} else if (stato == BlogInfo.STATE_LOADED_WITH_ERROR ||  stato == BlogInfo.STATE_ERROR) {
    			icon = imgImportant;
    		} else if( stato == BlogInfo.STATE_LOADED ) {

    			if(currentRow.getBlogIcon() != null) {
    				try {
						icon = Bitmap.createBitmapFromBytes(currentRow.getBlogIcon(), 0, -1, 1);
					} catch (Exception e) {
						Log.error("no valid shortcut ico found in the blog obj");
					}
    			}
    			//still null there was an error during img generation process
    			if(icon == null) {
    				if(currentRow.isWPCOMBlog()) {
    					icon = wp_blue;
    				} else {
    					icon = wp_grey;
    				}
    			}
    		} 
    		
			/*
			 * 42px of row
			 * 6px blank space
			 */
			height = height - 6;
			w = w - 10;
    		
    		drawBackground(graphics, 5, y, w, height, _listField.getSelectedIndex() ==  index);
    		drawBorder(graphics, 5, y, w, height, _listField.getSelectedIndex() ==  index);
    		int leftImageWidth = 0;
    		
    		if (icon != null) {
    			leftImageWidth = drawLeftImage(graphics, 5, y, height, icon);
    		} else {
    			if (stato == BlogInfo.STATE_LOADING) {
	    			graphics.pushRegion(10, y+5, 32, 32, 0, 0);
	    			throbberRenderer.paint(graphics);
	    			graphics.popContext();
	    			leftImageWidth = height;
    			} 
    		}
    		
    		String blogName = currentRow.getName();
    		if(currentRow.isAwaitingModeration())
    			blogName = "(" + currentRow.getAwaitingModeration() + ") " + blogName;
    		
            drawText(graphics, leftImageWidth+5, y, w  - 5, height, blogName, _listField.getSelectedIndex() ==  index);

            graphics.setFont(originalFont);
            graphics.setColor(originalColor);
    	}
        
    	
		protected void drawBorder(Graphics graphics, int x, int y, int width,	int height, boolean selected) {
			if(selected) 
				graphics.setColor(Color.BLACK);
			else 
				graphics.setColor(Color.BLACK);	
			
			graphics.drawLine(x-1, y , x + width-1, y);
			graphics.drawLine(x-1, y, x-1 , y + height-1); //linea verticale sx
			graphics.drawLine(x + width, y-1, x + width , y + height-1); //linea verticale dx
			graphics.drawLine(x-1, y + height - 1, x + width-1, y + height - 1);
		}
    	    	
        //Returns the object at the specified index.
        public Object get(ListField list, int index) 
        {
            return _listData[index];
        }
    }
    
    /**
     * Internal timer task class to support animation.
     */
    private class AnimationTimerTask extends TimerTask {
    	
    	/* (non-Javadoc)
    	 * @see java.util.TimerTask#run()
    	 */
    	public void run() {
    		throbberRenderer.nextPosition();
    		boolean presence = false;
    		for (int i = 0; i < tbsIndex.length; i++) {
    			if(tbsIndex[i] == true) { 
    				_listField.invalidate(i);
    				presence = true;
    			}
    		}
    		//if zero thread in loading state, cancel the thread
    		if(!presence)
    			timerTask.cancel();
    	}
    }
} 

