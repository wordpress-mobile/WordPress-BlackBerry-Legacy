//#preprocess
package com.wordpress.view.component;

import java.util.Vector;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchGesture;
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.ui.component.ListField;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressInfo;
import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.GravatarController;
import com.wordpress.model.Comment;
import com.wordpress.utils.log.Log;
import com.wordpress.utils.observer.Observable;
import com.wordpress.utils.observer.Observer;

public class CommentsListField {
    
	private Vector _listData = new Vector();
    private ListField _innerListField;
    private boolean checkBoxVisible = false;
    private ListCallBack listFieldCallBack = null;
	private GravatarController gvtController;
    private ListActionListener defautActionListener = null;
	
	public void setDefautActionListener(ListActionListener defautActionListener) {
		this.defautActionListener = defautActionListener;
	}
       
   public boolean isCheckBoxVisible() {
		return checkBoxVisible;
	}

	public void setCheckBoxVisible(boolean checkBoxVisible) {
		this.checkBoxVisible = checkBoxVisible;
		_innerListField.invalidate(); //invalidate all list
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
		int selectedIndex = _innerListField.getSelectedIndex();
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
   public CommentsListField(Comment[] _elements, boolean[] _elementsChecked, GravatarController gvtController) {
		_innerListField = new ListField()
        {
			
			protected void drawFocus(Graphics graphics, boolean on) { } //remove the standard focus highlight
			
			private void checkItemAction() {
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
            
			}
			
			private boolean defaultAction() {
    	   		if (!isCheckBoxVisible()) {
        			if (defautActionListener != null) {
        				defautActionListener.actionPerformed();
        				return true;
        			} else        			
        			return false;
        		} else {
        			checkItemAction();
        			return true;
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
        			return defaultAction();
        		}
        		
        		return false;
        	}
			
            // Allow the space bar to toggle the status of the selected row.
			protected boolean keyChar(char key, int status, int time) {
				boolean retVal = false;
				// If the spacebar or enter was pressed...
				if ((key == Characters.SPACE || key == Characters.ENTER)) {
					retVal = defaultAction();
				}// end if keychar
				return retVal;
			}
            
            
        	//#ifdef IS_OS47_OR_ABOVE
        	protected boolean touchEvent(TouchEvent message) {
    			
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
        					return defaultAction();
        				}
        			} 
        			return false;
        		} else {
        			if(eventCode == TouchEvent.CLICK) {
        				return defaultAction();
        			}
        			return false;	
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
                //invalidate();

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
                //invalidate();
            }
            
        };
        
        //Set the ListFieldCallback
        listFieldCallBack = new ListCallBack();
        _innerListField.setCallback(listFieldCallBack);
        ResourceBundle resourceBundle = WordPressCore.getInstance().getResourceBundle();
        String emptyListString = resourceBundle.getString(WordPressResource.MESSAGE_NOTHING_TO_SEE_HERE);
        _innerListField.setEmptyString(emptyListString, DrawStyle.LEFT);
        _innerListField.setRowHeight(42);
        
        int elementLength = _elements.length;
	    
        //Populate the ListField & Vector with data.
        for(int count = 0; count < elementLength; ++count)
        {
           ChecklistData checklistData = new ChecklistData(_elements[count], _elementsChecked[count]);
           if(count == 0) checklistData.setSelected(true); //select the first element
           _listData.addElement(checklistData);
           _innerListField.insert(count);
        }
        
    	this.gvtController = gvtController;
    	gvtController.addObserver(new GravatarCallBack());
   }   
   
    public ListField getCommentListField() {
		return _innerListField;
	}

	//The menu item added to the screen when the _checkList field has focus.
    //This menu item toggles the checked/unchecked status of the selected row.
    public MenuItem _toggleItem = new MenuItem("Change Option", 1000, 50)    {
        public void run()
        {
            //Get the index of the selected row.
            int index = _innerListField.getSelectedIndex();
            
            //Get the ChecklistData for this row.
            ChecklistData data = (ChecklistData)_listData.elementAt(index);
            
            //Toggle its status.
            data.toggleChecked();
            
            //Update the Vector with the new ChecklistData.
            _listData.setElementAt(data, index);
            
            //Invalidate the modified row of the ListField.
            _innerListField.invalidate(index);
        }
    }; 
    
    public EncodedImage getLatestGravatar(String authorEmail) {
    	return  gvtController.getLatestGravatar(authorEmail);
    }
    
    private class ListCallBack extends BasicListFieldCallBack {
    	  
        protected Bitmap checkedBitmap         = Bitmap.getBitmapResource("check.png");
        protected Bitmap uncheckedBitmap       = Bitmap.getBitmapResource("uncheck.png");
 	    
    	//Draws the list row.
        public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) 
        {
            //Get the ChecklistData for the current row.
            ChecklistData currentRow = (ChecklistData)this.get(list, index);
            Comment currentComment = currentRow.getComment();
            
            Font originalFont = graphics.getFont();
            int originalColor = graphics.getColor();
            int height = list.getRowHeight();
            
            //drawXXX(graphics, 0, y, width, listField.getRowHeight());
           	drawBackground(graphics, 0, y, w, height, currentRow.isSelected);
            drawBorder(graphics, 0, y, w, height);
            
            int leftImageWidth = 0;
            //If it is checked draw the String prefixed with a checked box,
            //prefix an unchecked box if it is not.
    		if (isCheckBoxVisible()) {
    			if (currentRow.isChecked()) {
    				leftImageWidth = drawLeftImage(graphics, 0, y, height, checkedBitmap);
    			} else {
    				leftImageWidth = drawLeftImage(graphics, 0, y, height, uncheckedBitmap);
    			}
    		} else {
    			String authorEmail = currentComment.getAuthorEmail();
    			EncodedImage gravatarImage = getLatestGravatar(authorEmail);
    			leftImageWidth = drawLeftImage(graphics, 0, y, height, gravatarImage);
    			leftImageWidth = 40;
    		}

    		int authorWidth = 2;
            if(currentComment.getStatus().equalsIgnoreCase("hold"))
            	authorWidth = drawFirstRowMainText(graphics, leftImageWidth, y, w  - leftImageWidth, height, currentComment.getAuthor(), Color.YELLOWGREEN);
            else if(currentComment.getStatus().equalsIgnoreCase("spam"))
            	authorWidth = drawFirstRowMainText(graphics, leftImageWidth, y, w  - leftImageWidth, height, currentComment.getAuthor(), Color.RED);
            else
            	authorWidth = drawFirstRowMainText(graphics, leftImageWidth, y, w  - leftImageWidth, height, currentComment.getAuthor(), currentRow.isSelected);
            
            drawEMailText(graphics, w, y, w -  leftImageWidth - authorWidth - 10 , height, currentComment.getAuthorEmail(), currentRow.isSelected);
            drawSecondRowText(graphics, leftImageWidth, y, w - leftImageWidth, height, currentComment.getContent(), currentRow.isSelected);

            graphics.setFont(originalFont);
            graphics.setColor(originalColor);
            
        }
        
        //width is the total row width, not the space free for email text
        // x is the device width
        private void drawEMailText(Graphics graphics, int x, int y, int width, int height, String email, boolean selected) {
            //int fontHeight = ((2* height) / 5) - (PADDING * 2);
        	int fontHeight = ((3* height) / 6) - (PADDING * 2);
            graphics.setFont(Font.getDefault().derive(Font.PLAIN, fontHeight));
            
            int textX = 0;
            int fullTextWidth =  Font.getDefault().derive(Font.PLAIN, fontHeight).getAdvance(email); //space for the entire mail field
            if (fullTextWidth +2*PADDING > width) {
            	//space aren't enough for the entire email field
            	
            	//find the x position
            	textX = x - (width - PADDING);
               	width = width - 2*PADDING;
            } else {
                textX = x - (fullTextWidth + PADDING);
                width = fullTextWidth;
            }
            
            if (selected) {
                graphics.setColor(Color.WHITE);
            } else {
                graphics.setColor(Color.LIGHTGREY);
            }
            graphics.drawText(email, textX , y + PADDING + 2, DrawStyle.LEFT
                    | DrawStyle.TOP | DrawStyle.ELLIPSIS, width);
        }
        
    	 
        //Returns the object at the specified index.
        public Object get(ListField list, int index) 
        {
            return _listData.elementAt(index);
        }
        
   /*     //Returns the first occurence of the given String, beginning the search at index, 
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
        
	*/
    }
    //A class to hold the comment in the CheckBox and it's checkbox state (checked or unchecked).
    private class ChecklistData  {

    	private Comment comment;
        private boolean _checked;
        private boolean isSelected;
       
        ChecklistData(Comment comment, boolean checked)
        {
        	this.comment = comment;
        	_checked = checked;
        }

        public Comment getComment() {
        	return comment;
        }
                   
        private void setSelected(boolean flag){
        	isSelected = flag;
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
    
    private class GravatarCallBack implements Observer {
    	
    	public void update(Observable observable, Object object) {
    		if(object instanceof String) {
    			String email = (String) object;			
    			int elementLength = _listData.size();
    			
    			for(int count = 0; count < elementLength; count++)
    			{
    				//Get the ChecklistData for this row.
    				ChecklistData data = (ChecklistData)_listData.elementAt(count);
    				if ( data.comment.getAuthorEmail().equalsIgnoreCase(email) )
    					_innerListField.invalidate(count); //request row repaint
    			}
    		}	
    	}
    }
} 