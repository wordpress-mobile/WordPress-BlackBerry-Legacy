package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.BlogInfo;

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
	   };
	   
        _listField.setEmptyString(_resources.getString(WordPressResource.LABEL_ADD_YOUR_BLOG), DrawStyle.LEFT);
        _listData= blogCaricati;
        _listField.setRowHeight(48);//the others lists have rows of 42pixels height. added 6 pixel of blank space for each row
        //Set the ListFieldCallback
        _listField.setCallback(listFieldCallBack);
        
        int elementLength = blogCaricati.length;
        
        //Populate the ListField
        for(int count = 0; count < elementLength; ++count)
        {       
           _listField.insert(count);
        }    
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
    
    public void setBlogState(BlogInfo blogInfo){
        //Populate the ListField
        for(int count = 0; count < _listData.length; ++count)
        {
        	BlogInfo blog = _listData[count];
    
        	if (blogInfo.equals(blog) )		
        	 {
        		blog.setState(blogInfo.getState());
        		_listData[count]= blog;
                //Invalidate the modified row of the ListField.
                _listField.invalidate(count);
        	}
        }  
    }
    
    private class ListCallBack extends BasicListFieldCallBack {
		private Bitmap imgCompleted = Bitmap.getBitmapResource("complete.png");
		private Bitmap imgImportant = Bitmap.getBitmapResource("important.png");
		private Bitmap imgRefresh = Bitmap.getBitmapResource("refresh.png");  
		
        // Draws the list row.
    	public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) {
    		// Get the blog info for the current row.
    		BlogInfo currentRow = (BlogInfo) this.get(list, index);
    		
    		Bitmap icon;
    		Font originalFont = graphics.getFont();
    		int originalColor = graphics.getColor();
    		int height = list.getRowHeight();
    		
    		
    		int stato = currentRow.getState();
    		// If it is loading draw the String prefixed with a star,
    		if (stato == BlogInfo.STATE_LOADING || stato == BlogInfo.STATE_ADDED_TO_QUEUE ) {
    			icon = imgRefresh; 
    		} else if (stato == BlogInfo.STATE_LOADED_WITH_ERROR ||  stato == BlogInfo.STATE_ERROR) {
    			icon = imgImportant;
    		} else 
    			icon = imgCompleted;
    		
			/*
			 * 42px of row
			 * 6px blank space
			 */
			height = height - 6;
			w = w - 10;
			//y = y+3;
    		
    		//drawXXX(graphics, 0, y, width, listField.getRowHeight());
    		drawBackground(graphics, 5, y, w, height, _listField.getSelectedIndex() ==  index);
    		drawBorder(graphics, 5, y, w, height);
    	    int leftImageWidth = drawLeftImage(graphics, 5, y, height, icon);
            drawText(graphics, leftImageWidth+5, y, w  - leftImageWidth, height, currentRow.getName(), _listField.getSelectedIndex() ==  index);
    		
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
    	    	
        //Returns the object at the specified index.
        public Object get(ListField list, int index) 
        {
            return _listData[index];
        }
    }
} 

