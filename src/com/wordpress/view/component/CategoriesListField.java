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

import com.wordpress.model.Category;

public class CategoriesListField {

    
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

	
	public Category[] getSelectedCategories() {
		Vector comments = new Vector();
		int elementLength = _listData.size();
		for (int count = 0; count < elementLength; ++count) {
			// Get the ChecklistData for this row.
			ChecklistData data = (ChecklistData) _listData.elementAt(count);
			if (data.isChecked())
				comments.addElement(data.getComment());

		}
		Category[] commentsArray = new Category[comments.size()];
		comments.copyInto(commentsArray);
		return commentsArray;
	}
	
	   	
	
	private void addChild(Category rootCat, Category[] blogCategories,
			Vector categories, Vector categoriesLevel, int level) {

		int rootCatID = Integer.parseInt(rootCat.getId());

		for (int i = 0; i < blogCategories.length; i++) {
			// category has no parent cat
			if (blogCategories[i].getParentCategory() < 1)
				continue;

			int currentCatID = Integer.parseInt(blogCategories[i].getId());
			if (currentCatID == rootCatID)
				continue; // same category

			int parentCatID = blogCategories[i].getParentCategory();
			if (rootCatID == parentCatID) {
				// found child
				categories.addElement(blogCategories[i]);
				categoriesLevel.addElement(new Integer(level));

				addChild(blogCategories[i], blogCategories, categories,
						categoriesLevel, level+1);
			}

		}

	}
	
   public CategoriesListField( Category[] blogCategories, int[] postCategoriesID ) {

	   //ordering process
	   Vector rootCategories = new Vector();
	   for (int i = 0; i < blogCategories.length; i++) {
		   if( blogCategories[i].getParentCategory() < 1  ) 
			   rootCategories.addElement(blogCategories[i]);
	   }
	   //we have the root category node, start the process of ordering
	   Vector tmpCategoryList = new Vector();
	   Vector tmpCategoryLevelList = new Vector();
	   
	   for (int i = 0; i < rootCategories.size(); i++) {
		   Category rootCategory =(Category) rootCategories.elementAt(i);
		   
		   tmpCategoryList.addElement(rootCategory);
		   tmpCategoryLevelList.addElement(new Integer(0)); //level 0 for the root category
		   
		   addChild(rootCategory, blogCategories, tmpCategoryList, tmpCategoryLevelList, 1);	   
	   }
	   
	   Category[] orderedBlogCategories = new Category[blogCategories.length];
	   tmpCategoryList.copyInto(orderedBlogCategories);
	   int[]orderedBlogCategoriesLevel = new int[blogCategories.length];
	   
	   for (int i = 0; i < tmpCategoryLevelList.size(); i++) {
		   Integer elementAt =(Integer) tmpCategoryLevelList.elementAt(i);
		   orderedBlogCategoriesLevel[i] = elementAt.intValue();
	   }
	   
	   //start the checking process
		String[] catTitles = new String [orderedBlogCategories.length];
		boolean[] catCheck = new boolean [orderedBlogCategories.length];
		for (int i = 0; i < catCheck.length; i++) {
			Category category = orderedBlogCategories[i];
			catTitles[i]=category.getLabel();
			catCheck[i]=false;
			
			if(postCategoriesID != null) {
				for (int j = 0; j < postCategoriesID.length; j++) {
					if(postCategoriesID[j] == Integer.parseInt(category.getId()) ){
						catCheck[i]=true;
						break;
					}
				}
			}
		}
	   
	   
	   
        _checkList = new ListField()
        {
            //Allow the space bar to toggle the status of the selected row.
            protected boolean keyChar(char key, int status, int time)
            {
                boolean retVal = false;
                
                //If the spacebar was pressed...
                if (key == Characters.SPACE ) {
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
        _checkList.setEmptyString("Nothing to see here", DrawStyle.LEFT);
        _checkList.setRowHeight(42);
        
        int elementLength = orderedBlogCategories.length;
        
        //Populate the ListField & Vector with data.
        for(int count = 0; count < elementLength; ++count)
        {
           ChecklistData checklistData = new ChecklistData(orderedBlogCategories[count], orderedBlogCategoriesLevel[count], catCheck[count]);
           if(count == 0) checklistData.setSelected(true); //select the first element
           _listData.addElement(checklistData);
           _checkList.insert(count);
        }  
    }
        
    
  
   
     
    public ListField get_checkList() {
		return _checkList;
	}

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
    

    
    private class ListCallBack extends BasicListFieldCallBack {

    	  
        protected Bitmap checkedBitmap         = Bitmap.getBitmapResource("check.png");
        protected Bitmap uncheckedBitmap       = Bitmap.getBitmapResource("uncheck.png");
 	    
    	//Draws the list row.
        public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) 
        {
            //Get the ChecklistData for the current row.
            ChecklistData currentRow = (ChecklistData)this.get(list, index);
            Category currentComment = currentRow.getComment();
            
            Font originalFont = graphics.getFont();
            int originalColor = graphics.getColor();
            int height = list.getRowHeight();
            
            //drawXXX(graphics, 0, y, width, listField.getRowHeight());
            drawBackground(graphics, 0, y, w, height, currentRow.isSelected);
            drawBorder(graphics, 0, y, w, height);
            
            int leftImageWidth = 0;
            Bitmap icon = Bitmap.getBitmapResource("category_child.png");
            int level = currentRow.getLevel() ;

            if(level > 0) {
            	//print child selector
            	int spacer = 0;
            	if(level == 1) {
            		leftImageWidth = drawLeftImage(graphics, 0, y, height, icon);            		
            	} else {
            		spacer = icon.getWidth() * (level - 1);
            		leftImageWidth = drawLeftImage(graphics, spacer, y, height, icon);
            		leftImageWidth += spacer ; //adding padding to the left
            	}
            }
            
            //If it is checked draw the String prefixed with a checked box,
            //prefix an unchecked box if it is not.
            if (currentRow.isChecked()) {
            	drawRightImage(graphics, y, w, height,  checkedBitmap);
            } else {
            	drawRightImage(graphics, y, w, height, uncheckedBitmap);
            }

    	    drawText(graphics, leftImageWidth, y, w  - leftImageWidth, height, currentComment.getLabel(), currentRow.isSelected);
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
    
    
    //A class to hold the comment in the CheckBox and it's checkbox state (checked or unchecked).
    private class ChecklistData  {

    	private Category category;
        private boolean _checked;
        private boolean isSelected;
        private int level = 0; //depth of the category in the tree

        public int getLevel() {
			return level;
		}

		ChecklistData(Category comment, int level,  boolean checked)
        {
        	this.category = comment;
        	this.level = level;
        	_checked = checked;
        }

        public Category getComment() {
        	return category;
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