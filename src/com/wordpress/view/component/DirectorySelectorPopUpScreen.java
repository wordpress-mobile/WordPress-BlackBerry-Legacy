package com.wordpress.view.component;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.PopupScreen;

/**
 * A PopupScreen with a directory browser allowing for file selection.
 */

public class DirectorySelectorPopUpScreen extends PopupScreen {

	String _currentPath; // The current path;
	String[] _extensions; // File extensions to filter by.
	//ObjectListField _olf; // Lists fields and directories.
	DirCheckBoxListField chkListController;

		
	/**
	 * Open the screen to the root folder and show all files and directories.
	 */
	public DirectorySelectorPopUpScreen() {
		this(null, null);
	}

	/**
	 * Open the screen starting at the specified path and filter results based
	 * on a list of extensions.
	 * 
	 * @param startPath
	 *            The initial path to open. Use null to start at the root file
	 *            systems.
	 * @param extensions
	 *            Allowable file extensions to display. Use null to display all
	 *            file types.
	 */
	public DirectorySelectorPopUpScreen(String startPath, String[] extensions) {
		super(new DialogFieldManager());
		_extensions = extensions;
		prepScreen(startPath);
	}

	/**
	 * Display the screen, prompting the user to pick a file.
	 */
	public void pickFile() {
		UiApplication.getUiApplication().pushModalScreen(this);
	}

	/**
	 * Retrieves the current directory if the user is still browsing for a file,
	 * the selected file if the user has chosen one or null if the user
	 * dismissed the screen.
	 * 
	 * @return the current directory if the user is still browsing for a file,
	 *         the selected file if the user has chosen one or null if the user
	 *         dismissed the screen.
	 */
	public String getFile() {
		return _currentPath;
	}

	// Prepare the DialogFieldManager.
	private void prepScreen(String path) {
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.setIcon(new BitmapField(Bitmap.getPredefinedBitmap(Bitmap.QUESTION)));
		dfm.setMessage(new RichTextField("Select a folder"));
		
		chkListController = new DirCheckBoxListField(new String[0],new boolean[0]);
		chkListController.get_checkList();
		dfm.addCustomField(chkListController.get_checkList());

		updateList(path);		
		ButtonField buttonSelect = new ButtonField("Select");
		buttonSelect.setChangeListener(listenerButton);
		dfm.addCustomField(buttonSelect);	
	}

	private FieldChangeListener listenerButton = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	
	    	boolean[] sel = chkListController.getSelected();
	    	int index=-1;
	    	for (int i = 0; i < sel.length; i++) {
				if(sel[i]) index=i;
			}
	    	if(index == -1) return;
		    	
	    	String newPath = chkListController.getFirstSelected();

			if (newPath.equals("..")) {
				// Go up a directory.
				// Remove the trailing '/';
				newPath = _currentPath.substring(0, _currentPath.length() - 2);

				// Remove everything after the last '/' (the current directory).
				// If a '/' is not found, the user is opening the
				// file system roots.
				// Return null to cause the screen to display the
				// file system roots.
				int lastSlash = newPath.lastIndexOf('/');

				if (lastSlash == -1) {
					newPath = null;
				} else {
					newPath = newPath.substring(0, lastSlash + 1);
				}
			} else if (newPath.lastIndexOf('/') == (newPath.length() - 1)) {
				// If the path ends with /, a directory was selected.
				// Prefix the _currentPath if it is not null (not in the
				// root directory).
				if (_currentPath != null) {
					newPath = _currentPath + newPath;
				}
			} else {
				// A file was selected.
				_currentPath += newPath;

				// Return *?* to stop the screen update process.
				newPath = "*?*";
			}
			
			_currentPath = newPath;

			//return newPath;
	    	
	    	close();
	    }
	};
	
	// Reads all of the files and directories in a given path.
	private Vector readFiles(String path) {
		Enumeration fileEnum;
		Vector filesVector = new Vector();

		_currentPath = path;

		if (path == null) {
			// Read the file system roots.
			fileEnum = FileSystemRegistry.listRoots();

			while (fileEnum.hasMoreElements()) {
				filesVector.addElement((Object) fileEnum.nextElement());
			}
		} else {
			// Read the files and directories for the current path.
			try {
				FileConnection fc = (FileConnection) Connector.open("file:///"+ path);
				fileEnum = fc.list();
				String currentFile;

				while (fileEnum.hasMoreElements()) {
					// Use the file extension filter, if there is one.
					if (_extensions == null) {

						filesVector.addElement((Object) fileEnum.nextElement());
					} else {
						currentFile = ((String) fileEnum.nextElement());

						if (currentFile.lastIndexOf('/') == (currentFile.length() - 1)) {
							// Add all directories.
							filesVector.addElement((Object) currentFile);
						} else {
							// This is a file. Check if its
							// extension matches the filter.
						/*	for (int count = _extensions.length - 1; count >= 0; --count) {
								if (currentFile.indexOf(_extensions[count]) != -1) {
									// There was a match, add the file and
									// stop looping.

									filesVector.addElement((Object) currentFile);
									break;
								}
							} */
						}
					}
				}
			} catch (Exception ex) {
				Dialog.alert("Unable to open folder. " + ex.toString());
			}
		}
		return filesVector;
	}

	// Handles a user picking an entry in the ObjectListField.
	private void doSelection() {
		// Determine the current path.
		String thePath = buildPath();

		if (thePath == null) {
			// Only update the screen if a directory was selected.
			updateList(thePath);
		} else if (!thePath.equals("*?*")) {
			// Only update the screen if a directory was selected.
			// A second check is required here to avoid
			// a NullPointerException.
			updateList(thePath);
		} else {
			// The user has selected a file.
			// Close the screen.
			this.close();
		}
	}

	// Updates the entries in the ObjectListField.
	private void updateList(String path) {
		// Read all files and directories in the path.
		Vector fileList = readFiles(path);

		// Create an array from the Vector.
		Object fileArray[] = vectorToArray(fileList);
		
		String[] arrayFolder = new String[fileArray.length];
		boolean[] arrayFolderSelected = new boolean[fileArray.length];
		for (int i = 0; i < fileArray.length; i++) {
			arrayFolder[i]=(String) fileArray[i];
		}
		
		DialogFieldManager dfm = (DialogFieldManager) getDelegate();
		dfm.deleteCustomField(chkListController.get_checkList());
		
		chkListController = new DirCheckBoxListField(arrayFolder,arrayFolderSelected);
		chkListController.get_checkList();
		dfm.addCustomField(chkListController.get_checkList());
		
		// Update the field with the new files.
		//_olf.set(fileArray);
	}

	// Build a String that contains the full path of the user's selection.
	// If a file has been selected, close this screen.
	// Returns *?* if the user has selected a file.
	private String buildPath() {

		String newPath = chkListController.getFocusedElement();

		if (newPath.equals("..")) {
			// Go up a directory.
			// Remove the trailing '/';
			newPath = _currentPath.substring(0, _currentPath.length() - 2);

			// Remove everything after the last '/' (the current directory).
			// If a '/' is not found, the user is opening the
			// file system roots.
			// Return null to cause the screen to display the
			// file system roots.
			int lastSlash = newPath.lastIndexOf('/');

			if (lastSlash == -1) {
				newPath = null;
			} else {
				newPath = newPath.substring(0, lastSlash + 1);
			}
		} else if (newPath.lastIndexOf('/') == (newPath.length() - 1)) {
			// If the path ends with /, a directory was selected.
			// Prefix the _currentPath if it is not null (not in the
			// root directory).
			if (_currentPath != null) {
				newPath = _currentPath + newPath;
			}
		} else {
			// A file was selected.
			_currentPath += newPath;

			// Return *?* to stop the screen update process.
			newPath = "*?*";
		}

		return newPath;
	}

	// Saves the files and directories listed in vector format
	// into an object array.
	private Object[] vectorToArray(Vector filesVector) {
		int filesCount = filesVector.size();
		int dotIncrementor;
		Object[] files;

		// If not in the root, add ".." to the top of the array.
		if (_currentPath == null) {
			dotIncrementor = 0;
			files = new Object[(filesCount)];
		} else {
			dotIncrementor = 1;
			files = new Object[(filesCount + dotIncrementor)];

			// Add .. at the top to go back a directory.
			files[0] = (Object) ("..");
		}

		for (int count = 0; count < filesCount; ++count) {
			files[count + dotIncrementor] = (Object) filesVector.elementAt(count);
		}

		return files;
	}

	// Handle trackball clicks.
	protected boolean navigationClick(int status, int time) {
		doSelection();
		return true;
	}

	protected boolean keyChar(char c, int status, int time) {
		// Close this screen if escape is selected.
		if (c == Characters.ESCAPE) {
		//	_currentPath = null;
		//	this.close();
			return true;
		} else if (c == Characters.ENTER) {
			doSelection();
			return true;
		}

		return super.keyChar(c, status, time);
	}
	
	
	
	private class DirCheckBoxListField implements ListFieldCallback {
	    private Vector _listData = new Vector();
	    private ListField _checkList;
	   
	   private boolean[] getSelected(){
	       int elementLength = _listData.size();
	       boolean[] selected= new boolean[elementLength];
	       //Populate the ListField & Vector with data.
	       for(int count = 0; count < elementLength; ++count)
	       {
	    	   //Get the ChecklistData for this row.
	    	   DirChecklistData data = (DirChecklistData)_listData.elementAt(count);
	           selected[count]= data.isChecked();
	       }
	       return selected;
	   }
	   
	   private String getFirstSelected(){
	       int elementLength = _listData.size();
	       
	       //Populate the ListField & Vector with data.
	       for(int count = 0; count < elementLength; ++count)
	       {
	    	   //Get the ChecklistData for this row.
	    	   DirChecklistData data = (DirChecklistData)_listData.elementAt(count);
	         if (data.isChecked())
	        	 return data.getStringVal();
	       }
	       
	       return null;
	   }
	    
		//return the focused comment
		private String getFocusedElement() {
			int selectedIndex = _checkList.getSelectedIndex();
			if (selectedIndex != -1) {
				DirChecklistData data = (DirChecklistData) _listData.elementAt(selectedIndex);
				return data.getStringVal();
			} else {
				return null;
			}
		}

	   
	   public void addElement(String label){
		   int elementLength = _listData.size(); //the field start with 0 index!!
	       _listData.addElement(new DirChecklistData(label, true));  
	       _checkList.insert(elementLength);
	   }
	   
	   public DirCheckBoxListField(String[] _elements, boolean[] _elementsChecked) {  
		    
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
	                    DirChecklistData data = (DirChecklistData)_listData.elementAt(index);
	                    
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
	           _listData.addElement(new DirChecklistData(_elements[count], _elementsChecked[count]));  
	           _checkList.insert(count);
	        }    
	    }
	        
	    //Draws the list row.
	    public void drawListRow(ListField list, Graphics graphics, int index, int y, int w) 
	    {
	        //Get the ChecklistData for the current row.
	    	DirChecklistData currentRow = (DirChecklistData)this.get(list, index);
	        
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
	            DirChecklistData data = (DirChecklistData)_listData.elementAt(index);
	            
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
	    private class DirChecklistData  {
	        private String _stringVal;
	        private boolean _checked;
	        
	        DirChecklistData()
	        {
	            _stringVal = "";
	            _checked = false;
	        }
	        
	        DirChecklistData(String stringVal, boolean checked)
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

}

