package com.wordpress.view.component;


import java.util.Vector;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

public class CheckBoxListField implements ListFieldCallback {
    private Vector _listData = new Vector();
    private ListField _checkList;
   
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
    
   public void addElement(String label){
	   int elementLength = _listData.size(); //the field start with 0 index!!
       _listData.addElement(new ChecklistData(label, true));  
       _checkList.insert(elementLength);
   }
   
   public CheckBoxListField(String[] _elements, boolean[] _elementsChecked) {  
	    
        _checkList = new ListField()
        {
            //Allow the space bar to toggle the status of the selected row.
            protected boolean keyChar(char key, int status, int time)
            {
                boolean retVal = false;
                
                //If the spacebar was pressed...
                if (key == Characters.SPACE)
                {
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
        if (currentRow.isChecked())
        {
            rowString.append(Characters.BALLOT_BOX_WITH_CHECK);
        }
        else
        {
            rowString.append(Characters.BALLOT_BOX);
        }
        
        //Append a couple spaces and the row's text.
        rowString.append(Characters.SPACE);
        rowString.append(Characters.SPACE);
        rowString.append(currentRow.getStringVal());
        
        //Draw the text.
        graphics.drawText(rowString.toString(), 0, y, 0, w);
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
    
    
    //A class to hold the Strings in the CheckBox and it's checkbox state (checked or unchecked).
    private class ChecklistData  {
        private String _stringVal;
        private boolean _checked;
        
        ChecklistData()
        {
            _stringVal = "";
            _checked = false;
        }
        
        ChecklistData(String stringVal, boolean checked)
        {
            _stringVal = stringVal;
            _checked = checked;
        }
        
        //Get/set methods.
        private String getStringVal()
        {
            return _stringVal;
        }
        
        private boolean isChecked()
        {
            return _checked;
        }
        
        private void setStringVal(String stringVal)
        {
            _stringVal = stringVal;
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