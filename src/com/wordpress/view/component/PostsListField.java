package com.wordpress.view.component;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import net.rim.device.api.i18n.Locale;
import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ObjectListField;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.CalendarUtils;
import com.wordpress.utils.StringUtils;

/**
 * This class is a list field that we used to
 * show posts and pages list.
 * 
 * @author dercoli
 *
 */
public class PostsListField extends ObjectListField  {

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
    	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
	
    private SimpleDateFormat sdFormat = new SimpleDateFormat(_resources.getString(WordPressResource.DEFAULT_DATE_FORMAT));
    
	private Vector _listData = new Vector();
    private ListCallBack listFieldCallBack = null;
    		
	public PostsListField() {
		listFieldCallBack = new ListCallBack();
		// Set the ListFieldCallback
		setCallback(listFieldCallBack);
		setEmptyString("Nothing to see here", DrawStyle.LEFT);
		setRowHeight(42);

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
            drawSecondRowText(graphics, leftImageWidth, y, w - leftImageWidth, height, currentRow.getDate(), currentRow.isSelected);

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
        
        public String getDate() {
			if (data.containsKey("date_created_gmt")) {
				Date dateCreated = (Date) data.get("date_created_gmt");
				long time = dateCreated.getTime();
				Date dateCreatedTZ = new Date(CalendarUtils.adjustTimeToDefaultTimezone(time));		
				String format = sdFormat.format(dateCreatedTZ);
				return format;	
			
			} else
				return "";
		}
                   
        private void setSelected(boolean flag){
        	isSelected = flag;
        }
        
    }
}
