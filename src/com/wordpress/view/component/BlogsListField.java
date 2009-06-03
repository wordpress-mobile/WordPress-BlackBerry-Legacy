package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.BlogInfo;

/**
 * This class is a wrapper around a list field that we used to
 * show Blogs with their Status. 
 * The Blog status are showed as a prefix character:
 * Char STAR = blog is in loading
 * Char Upper triangle = blog loading error 
 * Char SPACE = blog loaded
 * 
 * @author dercoli
 *
 */

public class BlogsListField implements ListFieldCallback {
    
	private BlogInfo[] _listData;
    private ListField _listField;
    
    protected Bitmap bg                    = null;
    protected Bitmap bgSelected            = null;
    final int PADDING     = 2;
    
  //create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
    
   public BlogsListField(BlogInfo[] blogCaricati) {  
	    bg = Bitmap.getBitmapResource("bg_light.png");
	    bgSelected = Bitmap.getBitmapResource("bg_blue.png");

	    _listField = new ListField() {
        	
            protected int moveFocus(int amount, int status, int time) {
                // Forward the call
                int ret = super.moveFocus(amount, status, time);
                invalidate(); //we can invalidate only the 2 involved rows
                return ret;
            }
        };
        
        _listField.setEmptyString(_resources.getString(WordPressResource.LABEL_ADD_YOUR_BLOG), DrawStyle.LEFT);
        _listData= blogCaricati;
        _listField.setRowHeight(42);
        //Set the ListFieldCallback
        _listField.setCallback(this);
        
        int elementLength = blogCaricati.length;
        
        //Populate the ListField
        for(int count = 0; count < elementLength; ++count)
        {       
           _listField.insert(count);
        }    
    }
        
    // Draws the list row.
	public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) {
		// Get the blog info for the current row.
		BlogInfo currentRow = (BlogInfo) this.get(list, index);
		
		Bitmap icon;
		Font originalFont = graphics.getFont();
		int originalColor = graphics.getColor();
		int height = list.getRowHeight();
		
		//drawXXX(graphics, 0, y, width, listField.getRowHeight());
		drawBackground(graphics, 0, y, w, height, _listField.getSelectedIndex() ==  index);
		drawBorder(graphics, 0, y, w, height);
	    
		
		int stato = currentRow.getState();
		// If it is loading draw the String prefixed with a star,
		if (stato == BlogInfo.STATE_LOADING || stato == BlogInfo.STATE_ADDED_TO_QUEUE ) {
			icon = Bitmap.getBitmapResource("refresh.png"); 
		} else if (stato == BlogInfo.STATE_LOADED_WITH_ERROR ||  stato == BlogInfo.STATE_ERROR) {
			icon = Bitmap.getBitmapResource("cancel.png");
		} else 
			icon = Bitmap.getBitmapResource("complete.png");

	    int leftImageWidth = drawLeftImage(graphics, 0, y, height, icon);
        drawText(graphics, leftImageWidth, y, w  - leftImageWidth, height, currentRow.getName(), _listField.getSelectedIndex() ==  index);
		
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
			graphics.drawBitmap(x - 1, y - 1, width + 2, height + 1, toDraw, 0, 0);
			width -= imgWidth;
			// Overlap a little bit to avoid border issues
			x += imgWidth - 2;
		}
	}
	
	private void drawBorder(Graphics graphics, int x, int y, int width, int height) {
		
		graphics.setColor(Color.GRAY);
		graphics.drawLine(x, y - 1, x + width, y - 1);
		graphics.drawLine(x, y + height - 1, x + width, y + height - 1);
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
    
    
    
    //Returns the object at the specified index.
    public Object get(ListField list, int index) 
    {
        return _listData[index];
    }
    
    //Returns the first occurence of the given String, beginning the search at index, 
    //and testing for equality using the equals method.
    public int indexOfList(ListField list, String p, int s) 
    {
        return -1;
       // return _listData.indexOf(p, s);
    }
    
    //Returns the screen width so the list uses the entire screen width.
    public int getPreferredWidth(ListField list) 
    {
        return Graphics.getScreenWidth();
    }
} 

