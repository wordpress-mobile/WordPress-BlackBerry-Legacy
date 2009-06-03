package com.wordpress.view.component;

import java.util.Vector;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;

import com.wordpress.model.Comment;

public class CommentsListField implements ListFieldCallback {
    
    protected Bitmap bg                    = null;
    protected Bitmap bgSelected            = null;
    protected Bitmap checkedBitmap         = null;
    protected Bitmap uncheckedBitmap       = null;
    
    protected static final int PADDING     = 2;
    
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
	   bg = Bitmap.getBitmapResource("bg_light.png");
	   bgSelected = Bitmap.getBitmapResource("bg_blue.png");
	   checkedBitmap = Bitmap.getBitmapResource("check.png");
	   uncheckedBitmap = Bitmap.getBitmapResource("uncheck.png");
	    
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
            
            protected int moveFocus(int amount, int status, int time) {

                int oldSelection = getSelectedIndex();
                ChecklistData data = (ChecklistData)_listData.elementAt(oldSelection);
                data.setSelected(false);

                // Forward the call
                int ret = super.moveFocus(amount, status, time);

                int newSelection = getSelectedIndex();

                // Get the next enabled item;
                data = (ChecklistData)_listData.elementAt(newSelection);
                data.setSelected(true);

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
        _checkList.setCallback(this);
        _checkList.setEmptyString("Nothing to see here", DrawStyle.LEFT);
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
				leftImageWidth = drawLeftImage(graphics, 0, y, height, true);
			} else {
				leftImageWidth = drawLeftImage(graphics, 0, y, height, false);
			}
		} else {
			
		}

        
        int authorWidth = drawAuthorText(graphics, leftImageWidth, y, w  - leftImageWidth, height, currentComment.getAuthor(), currentRow.isSelected);
        drawEMailText(graphics, w -  leftImageWidth - authorWidth, y, w - leftImageWidth - authorWidth, height, currentComment.getAuthorEmail(), currentRow.isSelected);
        drawContentText(graphics, leftImageWidth, y, w - leftImageWidth, height, currentComment.getContent(), currentRow.isSelected);

        graphics.setFont(originalFont);
        graphics.setColor(originalColor);
        
    }
    
    private int drawAuthorText(Graphics graphics, int x, int y, int width, int height, String title, boolean selected) {
        //int fontHeight = ((int) (1.7 * (height / 3))) - (PADDING * 2);
        int fontHeight = ((int) ((3* height) / 5)) - (PADDING * 2);
        graphics.setFont(Font.getDefault().derive(Font.BOLD, fontHeight));

        if (selected) {
            graphics.setColor(Color.BLACK);
        } else {
            graphics.setColor(Color.GRAY);
        }

        if (title != null) {
            // Title takes top 2/3 of list item
        return   graphics.drawText(title, x + PADDING + 3, y + PADDING + 2, DrawStyle.LEFT
                    | DrawStyle.TOP | DrawStyle.ELLIPSIS, width - x - (PADDING * 2));
        }

        return 0;
    }
    
    private void drawEMailText(Graphics graphics, int x, int y, int width, int height, String email, boolean selected) {
        int fontHeight = ((2* height) / 5) - (PADDING * 2);
        graphics.setFont(Font.getDefault().derive(Font.PLAIN, fontHeight));

        if (selected) {
            graphics.setColor(Color.BLACK);
        } else {
            graphics.setColor(Color.LIGHTGREY);
        }
        graphics.drawText(email, x + PADDING + 3, y + PADDING + 2, DrawStyle.LEFT
                | DrawStyle.TOP | DrawStyle.ELLIPSIS, width - (PADDING * 2));
        
    }
    //item.draw(graphics, 0, y, width, listField.getRowHeight());

    private void drawContentText(Graphics graphics, int x, int y, int width, int height, String status, boolean selected) {
        int fontHeight = ((2* height) / 5) - (PADDING * 2);
        graphics.setFont(Font.getDefault().derive(Font.PLAIN, fontHeight));

        if (selected) {
            graphics.setColor(Color.BLACK);
        } else {
            graphics.setColor(Color.LIGHTGREY);
        }

        graphics.drawText(status, x + PADDING + 5, y - 4 + (height - fontHeight),
                DrawStyle.LEFT | DrawStyle.TOP, width - PADDING);
    }
    
    private int drawLeftImage(Graphics graphics, int x, int y, int height, boolean checked) {
        Bitmap leftImage;

        if (checked) {
            graphics.setColor(Color.BLACK);
            leftImage = checkedBitmap;
        } else {
            graphics.setColor(Color.LIGHTGREY);
            leftImage = uncheckedBitmap;
        }

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
     
    public ListField get_checkList() {
		return _checkList;
	}

	//The menu item added to the screen when the _checkList field has focus.
    //This menu item toggles the checked/unchecked status of the selected row.
    public MenuItem _toggleItem = new MenuItem("Change Option", 100200, 5)    {
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
} 