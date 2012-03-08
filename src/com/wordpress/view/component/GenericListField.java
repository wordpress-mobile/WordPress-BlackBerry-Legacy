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
import net.rim.device.api.ui.Manager;
//#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
import net.rim.device.api.ui.TouchEvent;
import net.rim.device.api.ui.TouchGesture;
//#endif
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
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
public class GenericListField extends ObjectListField  {

    private final SimpleDateFormat sdFormat;
	private Vector _listData = new Vector();
    private ListCallBack listFieldCallBack = null;
    private ListActionListener defautActionListener = null;
    private ListLoadMoreListener loadMoreListener = null;
    		
	public void setDefautActionListener(ListActionListener defautActionListener) {
		this.defautActionListener = defautActionListener;
	}
	public void setDefautLoadMoreListener(ListLoadMoreListener defautLoadMoreListener) {
		this.loadMoreListener = defautLoadMoreListener;
	}

	public GenericListField() {
		listFieldCallBack = new ListCallBack();
		// Set the ListFieldCallback
		setCallback(listFieldCallBack);
        ResourceBundle resourceBundle = WordPressCore.getInstance().getResourceBundle();
        String emptyListString = resourceBundle.getString(WordPressResource.MESSAGE_NOTHING_TO_SEE_HERE);
        setEmptyString(emptyListString, DrawStyle.LEFT);
		setRowHeight(BasicListFieldCallBack.getRowHeightForDoubleLineRow());
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
	
	public void setSelectedIndex(int index){
		int oldSelection = getSelectedIndex();
		if(oldSelection != -1) {
			ListData data = (ListData)_listData.elementAt(oldSelection);
			data.setSelected(false);
		}
		((ListData)_listData.elementAt(index)).setSelected(true);
		super.setSelectedIndex(index);
	}
	
	public void resetListItems(Object[] objs) {
		//clear the list
		while ( this.isEmpty() == false ) {
			this.delete(0);	
		}
		_listData = new Vector();
		
		//Populate the ListField & Vector with data.
        for(int count = 0; count < objs.length; ++count)
        {   
           ListData checklistData = new ListData((Hashtable)objs[count]);
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
        	drawBorder(graphics, 0, y, w, height, currentRow.isSelected);
                  
            drawTextOnFirstRow(graphics, 0, y, w, height, currentRow.getTitle(), currentRow.isSelected);
            drawSecondRowText(graphics, 0, y, w, height, currentRow.getSubTitle(), currentRow.isSelected);

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
    
    protected boolean navigationMovement(int dx, int dy, int status, int time) {
    	Log.debug("dx: "+dx+" dy: "+dy+" status: "+ status+" time: "+time);
    	if ( dy == 1 ) //moving down in the list 
    		checkLoadMore();
    	return super.navigationMovement(dx, dy, status, time);
    }
        
    private void checkLoadMore() {
    	if( this.loadMoreListener == null )
    		return;
    	
    	Manager manager = this.getManager();
    	int managerHeight = manager.getHeight();
    	int managerContentHeight = manager.getContentHeight();
    	int managerVerticalScroll = manager.getVerticalScroll();
    	int managerVirtualHeight = manager.getVirtualHeight();

		Log.debug("getVirtualHeight: "+managerVirtualHeight+" getVerticalScroll: "+managerVerticalScroll );
		Log.debug("getContentHeight: "+managerContentHeight+" getHeight: "+managerHeight);
    	    	
		if( managerVerticalScroll == 0 ||  managerVerticalScroll ==  managerHeight )
    		return;
    	
    	int calculatedVirtualHeight = managerVerticalScroll + managerHeight;
    	int calculatedVirtuaContentHeight = managerVerticalScroll + managerContentHeight;
    	
    	boolean shouldLoadMore =  calculatedVirtualHeight >= managerVirtualHeight;
    	shouldLoadMore = shouldLoadMore || true == calculatedVirtuaContentHeight >= managerVirtualHeight; //just another check
    	
    	if (shouldLoadMore) this.loadMoreListener.loadMore();
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
    
    //#ifdef VER_4.7.0 | BlackBerrySDK5.0.0 | BlackBerrySDK6.0.0 | BlackBerrySDK7.0.0
    protected boolean touchEvent(TouchEvent message) {
    	Log.trace(">>> touchEvent");
    	int eventCode = message.getEvent();
    	
		if(!this.getContentRect().contains(message.getX(1), message.getY(1)))
		{       			
			return false;
		} 
    	
    /*	boolean isOutOfBounds = false;
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
    	*/
    	if(WordPressInfo.isForcelessTouchClickSupported) {
    		if (eventCode == TouchEvent.GESTURE) {
    			TouchGesture gesture = message.getGesture();
    			int gestureCode = gesture.getEvent();
    			if (gestureCode == TouchGesture.TAP) {
    				if (this.defautActionListener != null) {
    					defautActionListener.actionPerformed();
    					return true;
    				}
    			} else if (gestureCode == TouchGesture.HOVER) {
    				return false; //Shows the contextual menu
    			} else {
        			//is not a click!
        			checkLoadMore();
        		}
    		} 
    	} else {
    		if (eventCode == TouchEvent.CLICK) {
    			if (this.defautActionListener != null) {
    				defautActionListener.actionPerformed();
    				return true;
    			}
    		} else {
    			//is not a click!
    			checkLoadMore();
    		}
    	}
    	return super.touchEvent(message);
    }
    //#endif
}