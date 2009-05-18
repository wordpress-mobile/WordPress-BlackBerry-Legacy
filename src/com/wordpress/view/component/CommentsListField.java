package com.wordpress.view.component;

import java.util.Date;
import java.util.Vector;

import net.rim.device.api.i18n.SimpleDateFormat;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

import com.wordpress.model.Comment;

public class CommentsListField implements ListFieldCallback {
    private Vector _listData = new Vector();
    private ListField _checkList;
    private boolean checkBoxVisible = false;
    
       
   public boolean isCheckBoxVisible() {
		return checkBoxVisible;
	}

	public void setCheckBoxVisible(boolean checkBoxVisible) {
		this.checkBoxVisible = checkBoxVisible;
		_checkList.invalidate(); //invalidate all list
	}

	public boolean[] getSelected(){
       int elementLength = _listData.size();
       boolean[] selected= new boolean[elementLength];
       //Populate the ListField & Vector with data.
       for(int count = 0; count < elementLength; ++count)
       {
    	   //Get the ChecklistData for this row.
           ChecklistData data = (ChecklistData)_listData.elementAt(count);
           selected[count]= data.isChecked();
       }
       return selected;
   }

	
	public Comment[] getSelectedComments() {
		Vector comments = new Vector();
		int elementLength = _listData.size();
		for (int count = 0; count < elementLength; ++count) {
			// Get the ChecklistData for this row.
			ChecklistData data = (ChecklistData) _listData.elementAt(count);
			if (data.isChecked())
				comments.addElement(data.getComment());

		}
		Comment[] commentsArray = new Comment[comments.size()];
		comments.copyInto(commentsArray);
		return commentsArray;
	}
	
	//return the focused comment
	public Comment getFocusedComment() {
		int selectedIndex = _checkList.getSelectedIndex();
		if (selectedIndex != -1) {
			ChecklistData data = (ChecklistData) _listData.elementAt(selectedIndex);
			return data.getComment();
		} else {
			return null;
		}
	}

	
/*
   public void addElement(String label){
	   int elementLength = _listData.size(); //the field start with 0 index!!
       _listData.addElement(new ChecklistData(label, true));  
       _checkList.insert(elementLength);
   }
   */
   public CommentsListField(Comment[] _elements, boolean[] _elementsChecked) {  
	    
        _checkList = new ListField()
        {
            //Allow the space bar to toggle the status of the selected row.
            protected boolean keyChar(char key, int status, int time)
            {
                boolean retVal = false;
                
                //If the spacebar was pressed...
                if (key == Characters.SPACE && isCheckBoxVisible()) {
                    //Get the index of the selected row.
                    int index = getSelectedIndex();
                    
                    //Get the ChecklistData for this row.
                    ChecklistData data = (ChecklistData)_listData.elementAt(index);
                    
                    //Toggle its status.
                    data.toggleChecked();
                    
                    //Update the Vector with the new ChecklistData.
                    _listData.setElementAt(data, index);
                    
                    //Invalidate the modified row of the ListField.
                    invalidate(index);
                    
                    //Consume this keyChar (key pressed).
                    retVal = true;
                }
                return retVal;
            }
        };
        
        //Set the ListFieldCallback
        _checkList.setCallback(this);
        _checkList.setEmptyString("Nothing to see here", DrawStyle.LEFT);
        
        int elementLength = _elements.length;
        
        //Populate the ListField & Vector with data.
        for(int count = 0; count < elementLength; ++count)
        {
           _listData.addElement(new ChecklistData(_elements[count], _elementsChecked[count]));  
           _checkList.insert(count);
        }    
    }
        
    //Draws the list row.
    public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) 
    {
        //Get the ChecklistData for the current row.
        ChecklistData currentRow = (ChecklistData)this.get(list, index);
        
        StringBuffer rowString = new StringBuffer();
        
        //If it is checked draw the String prefixed with a checked box,
        //prefix an unchecked box if it is not.
		if (isCheckBoxVisible()) {
			if (currentRow.isChecked()) {
				rowString.append(Characters.BALLOT_BOX_WITH_CHECK);
			} else {
				rowString.append(Characters.BALLOT_BOX);
			}
		} else {
			
		}
        
        //Append a couple spaces and the row's text.
        rowString.append(Characters.SPACE);
        rowString.append(Characters.SPACE);
        rowString.append(currentRow.getStringVal());
        
        if (currentRow.getCommentStatus().equalsIgnoreCase("approve")) {
			// approved comment
			graphics.drawText("A "+rowString.toString(), 0, y, 0, w);
		} else if (currentRow.getCommentStatus().equalsIgnoreCase("hold")) {
			// holded comment
			graphics.drawText("H "+rowString.toString(), 0, y, 0, w);

		} else if (currentRow.getCommentStatus().equalsIgnoreCase("spam")) {
			// spam comment
			graphics.drawText("S "+rowString.toString(), 0, y, 0, w);
		} else {
			graphics.drawText(rowString.toString(), 0, y, 0, w);
		}
    }
    
    
    public ListField get_checkList() {
		return _checkList;
	}

	//The menu item added to the screen when the _checkList field has focus.
    //This menu item toggles the checked/unchecked status of the selected row.
    private MenuItem _toggleItem = new MenuItem("Change Option", 200, 10)    {
        public void run()
        {
            //Get the index of the selected row.
            int index = _checkList.getSelectedIndex();
            
            //Get the ChecklistData for this row.
            ChecklistData data = (ChecklistData)_listData.elementAt(index);
            
            //Toggle its status.
            data.toggleChecked();
            
            //Update the Vector with the new ChecklistData.
            _listData.setElementAt(data, index);
            
            //Invalidate the modified row of the ListField.
            _checkList.invalidate(index);
        }
    }; 
    
    //Returns the object at the specified index.
    public Object get(ListField list, int index) 
    {
        return _listData.elementAt(index);
    }
    
    //Returns the first occurence of the given String, beginning the search at index, 
    //and testing for equality using the equals method.
    public int indexOfList(ListField list, String p, int s) 
    {
        //return listElements.getSelectedIndex();
        return _listData.indexOf(p, s);
    }
    
    //Returns the screen width so the list uses the entire screen width.
    public int getPreferredWidth(ListField list) 
    {
        return Graphics.getScreenWidth();
    }
    
    
    //A class to hold the comment in the CheckBox and it's checkbox state (checked or unchecked).
    private class ChecklistData  {
        private Comment comment;
        public Comment getComment() {
			return comment;
		}

		private boolean _checked;
                
        ChecklistData(Comment comment, boolean checked)
        {
        	this.comment = comment;
            _checked = checked;
        }
        
        private String getCommentStatus(){
        	return comment.getStatus();
        }
        
        //Get a rapresentation of the comment...
        private String getStringVal()
        {
            Date dateCreated = comment.getDate_created_gmt();
            SimpleDateFormat sdFormat3 = new SimpleDateFormat("yyyy/MM/dd hh:mm");
            String format = sdFormat3.format(dateCreated);
            return comment.getContent().substring(0, 10)+"...  "+format;
        }
        
        private boolean isChecked()
        {
            return _checked;
        }
                
        private void setChecked(boolean checked)
        {
            _checked = checked;
        }
        
        //Toggle the checked status.
        private void toggleChecked()
        {
            _checked = !_checked;
        }
    }
} 