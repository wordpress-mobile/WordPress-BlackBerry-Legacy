//#preprocess
package com.wordpress.view.component;

import java.util.Vector;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.TouchEvent;
//#endif
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
        	        	
        	private void defaultItemAction() {
                //Get the index of the selected row.
                int index = getSelectedIndex();
                
                if(index == -1) return;
                
                //Get the ChecklistData for this row.
                ChecklistData data = (ChecklistData)_listData.elementAt(index);
                
                //Toggle its status.
                data.toggleChecked();
                
                //Update the Vector with the new ChecklistData.
                _listData.setElementAt(data, index);
                
                //Invalidate the modified row of the ListField.
                invalidate(index);
                
                //set the list state as dirty
                this.setDirty(true);
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
        			defaultItemAction();
        			return true;
        		}
        		return super.navigationClick(status, time);
        	}
        	        	
            //Allow the space bar to toggle the status of the selected row.
            protected boolean keyChar(char key, int status, int time)
            {
                //If the spacebar was pressed...
                if (key == Characters.SPACE || key == Characters.ENTER)
                {
                	defaultItemAction();
                	return true;
                }
                return false;
            }
            
        	//#ifdef IS_OS47_OR_ABOVE
        	protected boolean touchEvent(TouchEvent message) {
        		Log.trace("touchEvent");
        		
        		if(!this.getContentRect().contains(message.getX(1), message.getY(1)))
        		{       			
        			return false;
        		} 
        		
        		int eventCode = message.getEvent();

        		if(WordPressInfo.isTorch) {
    				if (eventCode == TouchEvent.GESTURE) {
    					TouchGesture gesture = message.getGesture();
    					int gestureCode = gesture.getEvent();
    					if (gestureCode == TouchGesture.TAP) {
    						defaultItemAction();
    						return true;
    					}
    				} 
    				return false;
    			} else {
    				if(eventCode == TouchEvent.CLICK) {
    					defaultItemAction();
    					return true;
    				}else if(eventCode == TouchEvent.DOWN) {
    				} else if(eventCode == TouchEvent.UP) {
    				} else if(eventCode == TouchEvent.UNCLICK) {
    					//return true; //consume the event: avoid context menu!!
    				} else if(eventCode == TouchEvent.CANCEL) {
    				}
    				return false; 
    				//return super.touchEvent(message);
    			}
        	}
        	//#endif
        	
            protected int moveFocus(int amount, int status, int time) {
            	ChecklistData data = null;
                int oldSelection = getSelectedIndex();
                if(oldSelection != -1) {
                	data = (ChecklistData)_listData.elementAt(oldSelection);
                	data.setSelected(false);
                	invalidate(oldSelection);
                }

                // Forward the call
                int ret = super.moveFocus(amount, status, time);

                int newSelection = getSelectedIndex();
                // Get the next enabled item;
                if(newSelection != -1) {
	                data = (ChecklistData)_listData.elementAt(newSelection);
	                data.setSelected(true);
	                invalidate(newSelection);
                }

                return ret;
            }
            
            
            protected void moveFocus(int x, int y, int status, int time) {
            	ChecklistData data = null;
            	int oldSelection = getSelectedIndex();
                super.moveFocus(x, y, status, time);
                int newSelection = getSelectedIndex();
                
                if(oldSelection != -1) {
                	data = (ChecklistData)_listData.elementAt(oldSelection);
                	data.setSelected(false);
                	invalidate(oldSelection);
                }
                
                if(newSelection != -1) {
                	data = (ChecklistData)_listData.elementAt(newSelection);
                	data.setSelected(true);
                	invalidate(newSelection);
                }
                
            }
            
        	protected void onUnfocus(){
        		super.onUnfocus();
        		ChecklistData data = null;
        		int oldSelection = getSelectedIndex();
        		if(oldSelection != -1) {
        			data = (ChecklistData)_listData.elementAt(oldSelection);
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
        		ChecklistData data = null;
        		int oldSelection = getSelectedIndex();
        		if(oldSelection != -1) {
        			data = (ChecklistData)_listData.elementAt(oldSelection);
        			data.setSelected(true);
        			invalidate(oldSelection);
        		}
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
           //if(count == 0) checklistData.setSelected(true); //select the first element
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
            //set the list state as dirty
            _checkList.setDirty(true);
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
            //check if it is the last row
            if( (index+1) < list.getSize() )
            	drawBorder(graphics, 0, y, w, height);
            
            int leftImageWidth = 40; //the image width
            
            //If it is checked draw the String prefixed with a checked box,
            //prefix an unchecked box if it is not.
            if (currentRow.isChecked()) {
            	drawRightImage(graphics, y, w, height,  checkedBitmap);
            } else {
            	drawRightImage(graphics, y, w, height, uncheckedBitmap);
            }

    	    drawText(graphics, 0, y, w  - leftImageWidth, height, currentRow.getStringVal(), currentRow.isSelected);
            graphics.setFont(originalFont);
            graphics.setColor(originalColor);
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
        
   /*     private void setStringVal(String stringVal)
        {
            _stringVal = stringVal;
        }
        
        private void setChecked(boolean checked)
        {
            _checked = checked;
        }
       */ 
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