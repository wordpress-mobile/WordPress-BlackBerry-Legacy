package com.wordpress.view.component;

import java.util.Vector;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;

public class CheckBoxListField {
    private Vector _listData = new Vector();
    private ListField _checkList;
    private ListCallBack listFieldCallBack = null;
   
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
    
	//return the focused comment
	public String getFocusedElement() {
		int selectedIndex = _checkList.getSelectedIndex();
		if (selectedIndex != -1) {
			ChecklistData data = (ChecklistData) _listData.elementAt(selectedIndex);
			return data.getStringVal();
		} else {
			return null;
		}
	}

   
   public void addElement(String label){
	   int elementLength = _listData.size(); //the field start with 0 index!!
       _listData.addElement(new ChecklistData(label, true));  
       _checkList.insert(elementLength);
   }
   
   public CheckBoxListField(String[] _elements, boolean[] _elementsChecked) {  
	    
        _checkList = new ListField()
        {
        	
        	protected void drawFocus(Graphics graphics, boolean on) { } //remove the standard focus highlight
        	
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
            
            protected int moveFocus(int amount, int status, int time) {
            	ChecklistData data = null;
                int oldSelection = getSelectedIndex();
                if(oldSelection != -1) {
                	data = (ChecklistData)_listData.elementAt(oldSelection);
                	data.setSelected(false);
                }

                // Forward the call
                int ret = super.moveFocus(amount, status, time);

                int newSelection = getSelectedIndex();
                // Get the next enabled item;
                if(newSelection != -1) {
	                data = (ChecklistData)_listData.elementAt(newSelection);
	                data.setSelected(true);
                }
                invalidate();

                return ret;
            }
            
            
            protected void moveFocus(int x, int y, int status, int time) {
                int oldSelection = getSelectedIndex();
                super.moveFocus(x, y, status, time);
                int newSelection = getSelectedIndex();
                ChecklistData data = (ChecklistData)_listData.elementAt(oldSelection);
                data.setSelected(false);
                
                data = (ChecklistData)_listData.elementAt(newSelection);
                data.setSelected(true);
                invalidate();
            }
           
        
            
        };
        
        //Set the ListFieldCallback
        listFieldCallBack = new ListCallBack();
        _checkList.setCallback(listFieldCallBack);
        ResourceBundle resourceBundle = WordPressCore.getInstance().getResourceBundle();
        String emptyListString = resourceBundle.getString(WordPressResource.MESSAGE_NOTHING_TO_SEE_HERE);
        _checkList.setEmptyString(emptyListString, DrawStyle.LEFT);
        _checkList.setRowHeight(42);
        
        int elementLength = _elements.length;
        
        //Populate the ListField & Vector with data.
        for(int count = 0; count < elementLength; ++count)
        {
           ChecklistData checklistData = new ChecklistData(_elements[count], _elementsChecked[count]);
           if(count == 0) checklistData.setSelected(true); //select the first element
           _listData.addElement(checklistData); 
           _checkList.insert(count);
        }    
    }
        
        
    public ListField get_checkList() {
		return _checkList;
	}
    
    //change the text of the menuitem associated with this list
    public void changeToggleItemLabel(String select, String deselect) {
   	 //Get the index of the selected row.
       int index = _checkList.getSelectedIndex();
       
       if(index == -1) {
       	//inserire una label di default
       	_toggleItem.setText("Change Option");
       	return;
       }
       
       //Get the ChecklistData for this row.
       ChecklistData data = (ChecklistData)_listData.elementAt(index);
   	
   	//change the label of the menuItem
       if(data.isChecked()) {
       	_toggleItem.setText(deselect);
       } else {
       	_toggleItem.setText(select);
       }
   }
   
	//The menu item added to the screen when the _checkList field has focus.
    //This menu item toggles the checked/unchecked status of the selected row.
    public MenuItem _toggleItem = new MenuItem("Change Option", 200, 10)    {
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
    
    
    private class ListCallBack extends BasicListFieldCallBack {

        protected Bitmap checkedBitmap         = Bitmap.getBitmapResource("check.png");
        protected Bitmap uncheckedBitmap       = Bitmap.getBitmapResource("uncheck.png");
 	    
    	//Draws the list row.
        public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) 
        {
            //Get the ChecklistData for the current row.
            ChecklistData currentRow = (ChecklistData)this.get(list, index);
            
            Font originalFont = graphics.getFont();
            int originalColor = graphics.getColor();
            int height = list.getRowHeight();
            
            //drawXXX(graphics, 0, y, width, listField.getRowHeight());
            drawBackground(graphics, 0, y, w, height, currentRow.isSelected);
            drawBorder(graphics, 0, y, w, height);
            
            int leftImageWidth = 0;
            Bitmap icon = Bitmap.getBitmapResource("category_child.png");
            
            //If it is checked draw the String prefixed with a checked box,
            //prefix an unchecked box if it is not.
            if (currentRow.isChecked()) {
            	drawRightImage(graphics, y, w, height,  checkedBitmap);
            } else {
            	drawRightImage(graphics, y, w, height, uncheckedBitmap);
            }

    	    drawText(graphics, leftImageWidth, y, w  - leftImageWidth, height, currentRow.getStringVal(), currentRow.isSelected);
            graphics.setFont(originalFont);
            graphics.setColor(originalColor);
        }
        
    	 
    	protected void drawBackground(Graphics graphics, int x, int y, int width, int height, boolean selected) {
    		Bitmap toDraw = null;
    		if (selected) {
    			toDraw = bgSelected;
    			int imgWidth = toDraw.getWidth();
    			while (width > -2) {
    				graphics.drawBitmap(x - 1, y - 1, width + 2, height + 1, toDraw, 0, 0);
    				width -= imgWidth;
    				// Overlap a little bit to avoid border issues
    				x += imgWidth - 2;
    			}
    		} else {
    			
    			graphics.setColor(Color.WHITE);
                graphics.fillRect(x - 1, y - 1, width + 2, height + 1);
    			
    		}
    	}
        
        //Returns the object at the specified index.
        public Object get(ListField list, int index) 
        {
            return _listData.elementAt(index);
        }
        
    }
    
    
    //A class to hold the Strings in the CheckBox and it's checkbox state (checked or unchecked).
    private class ChecklistData  {
        private String _stringVal;
        private boolean _checked;
		private boolean isSelected;
        
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
        
        private void setSelected(boolean flag){
        	isSelected = flag;
        }
        
        
        //Toggle the checked status.
        private void toggleChecked()
        {
            _checked = !_checked;
        }
    }
} 