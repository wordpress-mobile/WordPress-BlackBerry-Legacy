package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

import com.wordpress.bb.WordPressResource;
import com.wordpress.model.BlogInfo;

public class BlogListField implements ListFieldCallback {
    private BlogInfo[] _listData;
    private ListField _checkList;
    
  //create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
    
   public BlogListField(BlogInfo[] blogCaricati) {  
	    
        _checkList = new ListField();
        _checkList.setEmptyString(_resources.getString(WordPressResource.LABEL_ADD_YOUR_BLOG), DrawStyle.LEFT);
        _listData= blogCaricati;
        //Set the ListFieldCallback
        _checkList.setCallback(this);
        
        int elementLength = blogCaricati.length;
        
        //Populate the ListField
        for(int count = 0; count < elementLength; ++count)
        {       
           _checkList.insert(count);
        }    
    }
        
    // Draws the list row.
	public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) {
		// Get the ChecklistData for the current row.
		BlogInfo currentRow = (BlogInfo) this.get(list, index);

		StringBuffer rowString = new StringBuffer();

		int stato = currentRow.getState();

		// If it is loading draw the String prefixed with a star,
		if (stato != BlogInfo.STATE_LOADED) {
			rowString.append(Characters.ASTERISK);
		} else {
			rowString.append(Characters.SPACE);
		}

		// Append a couple spaces and the row's text.
		rowString.append(Characters.SPACE);
		rowString.append(Characters.SPACE);
		rowString.append((String) currentRow.getName());

		// Draw the text.
		graphics.drawText(rowString.toString(), 0, y, 0, w);
	}
    
    
    public ListField getCheckList() {
		return _checkList;
	}

    
    public BlogInfo getBlogSelected(){
        //Get the index of the selected row.
        int index = _checkList.getSelectedIndex();
        
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
                _checkList.invalidate(count);
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

