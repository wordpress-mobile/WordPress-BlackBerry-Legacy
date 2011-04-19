//#preprocess
package com.wordpress.view.component;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.CalendarUtils;
import com.wordpress.utils.log.Log;

/**
 * This class is a list field that we used to
 * show posts and pages list.
 * 
 * @author dercoli
 *
 */
public class PostsListField extends ObjectListField  {

	
    private final SimpleDateFormat sdFormat;
	private Vector _listData = new Vector();
    private ListCallBack listFieldCallBack = null;
    private ListActionListener defautActionListener = null;
    		
	public void setDefautActionListener(ListActionListener defautActionListener) {
		this.defautActionListener = defautActionListener;
	}

	public PostsListField() {
		listFieldCallBack = new ListCallBack();
		// Set the ListFieldCallback
		setCallback(listFieldCallBack);
        ResourceBundle resourceBundle = WordPressCore.getInstance().getResourceBundle();
        String emptyListString = resourceBundle.getString(WordPressResource.MESSAGE_NOTHING_TO_SEE_HERE);
        setEmptyString(emptyListString, DrawStyle.LEFT);
		setRowHeight(42);
		sdFormat = new SimpleDateFormat(resourceBundle.getString(WordPressResource.DEFAULT_DATE_FORMAT));
	}
	
	//override methods of object list 
	
	public void set(Object[] objs) {		
		_listData = new Vector();
		
		//Populate the ListField & Vector with data.
        for(int count = 0; count < objs.length; ++count)
        {   
           ListData checklistData = new ListData((Hashtable)objs[count]);
           if(count == 0) checklistData.setSelected(true); //select the first element
           _listData.addElement(checklistData);
           insert(count);
        } 
        
        invalidate(); //invalidate this list
	}
	
	protected void onUnfocus(){
		super.onUnfocus();
		ListData data = null;
		int oldSelection = getSelectedIndex();
		if(oldSelection != -1) {
			data = (ListData)_listData.elementAt(oldSelection);
			data.setSelected(false);
			invalidate(oldSelection);
		}
	}
	
	/*
	 * direction - 
	 * If 1, the focus came from the previous field; 
	 * if -1, the focus came from the subsequent field; 
	 * if 0, the focus was set directly (not as a result of trackwheel movement). 
	 * The focus on a particular list item is set only if the selected index has not already been set.
	 *  
	 */
	protected void onFocus(int direction){
		super.onFocus(direction);
		ListData data = null;
		int oldSelection = getSelectedIndex();
		if(oldSelection != -1) {
			data = (ListData)_listData.elementAt(oldSelection);
			data.setSelected(true);
			invalidate(oldSelection);
		}
	}
	
	
	protected int moveFocus(int amount, int status, int time) {
		ListData data = null;
		
		int oldSelection = getSelectedIndex();
		if(oldSelection != -1) {
			data = (ListData)_listData.elementAt(oldSelection);
			data.setSelected(false);
			invalidate(oldSelection);
		}

		// Forward the call
		int ret = super.moveFocus(amount, status, time);
		int newSelection = getSelectedIndex();
		
		// Get the next enabled item;
		if(newSelection != -1) {
			data = (ListData)_listData.elementAt(newSelection);
			data.setSelected(true);
			invalidate(newSelection);
		}
		//invalidate();
		
		return ret;
	}
	
	protected void moveFocus(int x, int y, int status, int time) {
		ListData data = null;
		int oldSelection = getSelectedIndex();
		super.moveFocus(x, y, status, time);
		int newSelection = getSelectedIndex();
		
		if(oldSelection != -1) {
			data = (ListData)_listData.elementAt(oldSelection);
			data.setSelected(false);
			invalidate(oldSelection);
		}
		
		if(newSelection != -1) {
			data = (ListData)_listData.elementAt(newSelection);
			data.setSelected(true);
			invalidate(newSelection);
		}
		
		//invalidate();
	}
	
    private class ListCallBack extends BasicListFieldCallBack {

    	// override method of the callback
    	public void drawListRow(ListField listField, Graphics graphics, int index, int y, int w) {
            Font originalFont = graphics.getFont();
            int originalColor = graphics.getColor();
            int height = getRowHeight();
            
            //Get the ChecklistData for the current row.
            ListData currentRow = (ListData)this.get(listField, index);
             
            //drawXXX(graphics, 0, y, width, listField.getRowHeight());
            drawBackground(graphics, 0, y, w, height, currentRow.isSelected);
            drawBorder(graphics, 0, y, w, height);
            
            int leftImageWidth = 0;
                  
            drawFirstRowMainText(graphics, leftImageWidth, y, w  - leftImageWidth, height, currentRow.getTitle(), currentRow.isSelected);
            drawSecondRowText(graphics, leftImageWidth, y, w - leftImageWidth, height, currentRow.getSubTitle(), currentRow.isSelected);

            graphics.setFont(originalFont);
            graphics.setColor(originalColor);
    	}

        //Returns the object at the specified index.
        public Object get(ListField list, int index) 
        {
            return _listData.elementAt(index);
        }
    }
	
    
    //A class to hold the post and page Info 
    private class ListData  {

    	private Hashtable data;
        private boolean isSelected;

        ListData(Hashtable data)
        {
			this.data = data;
        }

        public String getTitle() {
        	return (String)data.get("title");
        }
        
        public String getSubTitle() {
			if (data.containsKey("date_created_gmt")) {
				Date dateCreated = (Date) data.get("date_created_gmt");
				long time = dateCreated.getTime();
				Date dateCreatedTZ = new Date(CalendarUtils.adjustTimeToDefaultTimezone(time));		
				String format = sdFormat.format(dateCreatedTZ);
				
				if (data.containsKey("post_status")) {
					format+= "  -  " + (String)data.get("post_status");
				}
				
				return format;	
			} else if (data.containsKey("post_status")) {
				String format= (String)data.get("post_status");
				return format; 
			} else if (data.containsKey("images_number")) {
				String subTitle = (String) data.get("images_number");
				return subTitle;
			} else
				return "";
		}
                   
        private void setSelected(boolean flag){
        	isSelected = flag;
        }
        
    }
	 /**
     * Overrides default implementation.  Performs default action if the 
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
			 if(this.defautActionListener != null)
	            {
	            defautActionListener.actionPerformed();
	            return true;
	            }
		}
		return super.navigationClick(status, time);
	}
	
    //Allow the space bar to toggle the status of the selected row.
    protected boolean keyChar(char key, int status, int time)
    {
        boolean retVal = false;
        
        //If the spacebar was pressed...
        if (key == Characters.SPACE || key == Characters.ENTER) {
            if(this.defautActionListener != null)
            {
            defautActionListener.actionPerformed();
            //Consume this keyChar (key pressed).
            retVal = true;
            }
        }
        return retVal;
    }
    
	//#ifdef IS_OS47_OR_ABOVE
	protected boolean touchEvent(TouchEvent message) {
		Log.trace(">>> touchEvent");
		int eventCode = message.getEvent();
		if (eventCode == TouchEvent.CLICK) {
			 boolean isOutOfBounds = false;
	        int x = message.getX(1);
	        int y = message.getY(1);
			// Check to ensure point is within this field
	        if(x < 0 || y < 0 || x > getExtent().width || y > getExtent().height) {
	            isOutOfBounds = true;
	        }
			if(isOutOfBounds)
    		{       			
    			return true; //consume
    		}  
			
			if (this.defautActionListener != null) {
				defautActionListener.actionPerformed();
				return true;
			}
		}
		return super.touchEvent(message);
	}
	//#endif
}