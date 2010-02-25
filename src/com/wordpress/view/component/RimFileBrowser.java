package com.wordpress.view.component;


import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.KeyListener;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.system.TrackwheelListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;

public class RimFileBrowser extends MainScreen {


    private static final String  ROOT           = "/";
    
    protected FileBrowserList    listField      = null;
    private HeaderField titleField = null; 
    private String               currDirName    = ROOT;
    private String[] extensions; // File extensions to filter by.
    private FileBrowserMenu      menu;
    private boolean              quit           = false;
    private Stack                dirStack       = new Stack();
    private Bitmap               folderIcon     = null;
	
    private RimFileBrowserListener listener;
    private ResourceBundle resourceBundle;


    public RimFileBrowser(String[] extensions) {
    	resourceBundle = WordPressCore.getInstance().getResourceBundle();

    	if(extensions == null) {
    		this.extensions = new String[0];
    	} else {
    		this.extensions = extensions;
    	}
    	
        initialize();
    }


    protected void makeMenu(Menu theMenu, int instance) {
        String entry = null;
        Log.trace("RIMFileBrowser.makeMenu");
        if (listField != null) {
            entry = listField.getSelectedEntry();
        }
        menu.makeMenu(theMenu, entry);
    }


    public void setListener(RimFileBrowserListener listener) {
        this.listener = listener;
        Log.trace("file browser listener setup");
    }


    public void setPath(String path) {

    	try {
            FileConnection fileConn = (FileConnection)Connector.open("file://" + path, Connector.READ);
            fileConn.close();
        } catch (Exception e) {
            path = ROOT;
        }
        currDirName = path;
        listField.changeDirectory(0);

        dirStack = new Stack();
        int idx = path.indexOf("/");
        String partialPath = ROOT;
        if (idx != -1) {
            DirInfo dirInfo = new DirInfo(partialPath);
            dirStack.push(dirInfo);
        }

        while (idx != -1) {
            int nextIdx = path.indexOf("/", idx + 1);

            if (nextIdx != -1 && nextIdx != (path.length() - 1)) {
                String entry;
                entry = path.substring(idx + 1, nextIdx + 1);
                partialPath = partialPath + entry;
                DirInfo dirInfo = new DirInfo(partialPath);
                dirStack.push(dirInfo);
            }
            idx = nextIdx;
        }
    }

    
    
    
    
    private void performDefaultActionOnItem() {
        String entry = null;
        Log.trace("performDefaultActionOnItem");
        if (listField != null) {
            entry = listField.getSelectedEntry();
        }        
       if (entry != null ) {
	    		    	
	        if (isDirectory(entry)) {
	        	chooseDirectory();
	        } else {
	        	//open the file into the browser
	        	Tools.getBrowserSession("file://"+currDirName + entry);
	        }
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
			performDefaultActionOnItem();
			 return true;
		}
		return super.navigationClick(status, time);
	}
	
    /**
     * Overrides default.  Enter key will take default action on selected item.
     *  
     * @see net.rim.device.api.ui.Screen#keyChar(char,int,int)
     * 
     */
	protected boolean keyChar(char c, int status, int time) {
		Log.trace(">>> keyChar");
		// Close this screen if escape is selected.
		if (c == Characters.ENTER) {
			performDefaultActionOnItem();
			return true;
		}
		return super.keyChar(c, status, time);
	}
    
 
    public boolean onClose() {
        if (!quit && !ROOT.equals(currDirName)) {
            // Change to the previous directory
            up();
            return false; 
        }

        try {
        	this.close();
            return true;
        } catch (Exception e) {
            Log.error("Cannot close FileBrowser " + e.toString());
            return false;
        }
    }

    protected void onDisplay() {

        Log.trace("RimFileBrowser - OnDisplay");
        int rowHeight = getFont().getHeight() + 4;
        if(rowHeight < folderIcon.getHeight())
        	rowHeight = folderIcon.getHeight() + 4;
        
        listField.setRowHeight(rowHeight);
        super.onDisplay();
    }

   
/*
    protected boolean navigationClick(int status, int time) {
        Log.trace("[RimFileBrowser.navigationClick]");
        if (status == 0) {
        	Log.trace("[RimFileBrowser.navigationClick] --> status == 0");
            buttonPressed(listField.getSelectedIndex());
            return true;
        }
        return false;
    }
*/

    // Update the stack of directories
    protected void chooseDirectory() {
        int selectedIndex = listField.getSelectedIndex();
        String entryName = listField.getSelectedEntry();
        if (entryName != null && isDirectory(entryName)) {
            DirInfo dirInfo = new DirInfo(currDirName);
            dirInfo.setSelected(selectedIndex);
            dirStack.push(dirInfo);
            // If this a directory then we change into it
            currDirName = currDirName + entryName;
            listField.changeDirectory(0);
        }
    }
    
    protected void chooseFile() {
   	  	Log.trace("[RimFileBrowser.chooseFile]");
        String entryName = listField.getSelectedEntry();
        Log.trace("entryName: "+entryName);
        Log.trace("currDirName: "+currDirName);
        
        if (entryName == null) return;
        if(isDirectory(currDirName + entryName)) {
        	return;
        }
        
        boolean accepted = true;
        if (listener != null) {
            accepted = listener.chosen(currDirName + entryName);
        }

        if (accepted) {
            quit = true;
            onClose();
        }
    }

    protected void quit() {
            quit = true;
            onClose();
    }

    protected void up() {
        DirInfo dirInfo = (DirInfo)dirStack.pop();
        if (dirInfo == null) {
            return;
        }
        currDirName = dirInfo.getPath();
        int previousSelection = dirInfo.getSelected();
        if (previousSelection == -1) {
            listField.changeDirectory(0);
        } else {
            listField.changeDirectory(dirInfo.getSelected());
        }
    }

    private void resetTitle() {
        String title = resourceBundle.getString(WordPressResource.TITLE_FILE_SELECTION_DIALOG);
        if (!ROOT.equals(currDirName)) {
            title = currDirName;
        }
        titleField.setTitle(title);
    }

    private void initialize() {

        Log.trace("[RimFileBrowser.initialize]");

        menu = new FileBrowserMenu();
        // Pre load the icons
        folderIcon = Bitmap.getBitmapResource("folder_yellow_open.png");

        titleField = new HeaderField("");
		//if you want change color of the title bar, make sure to set 
		//color for background and foreground (to avoid theme colors injection...)
        titleField.setFontColor(Color.WHITE); 
        titleField.setBackgroundColor(Color.BLACK); 
  
        resetTitle();        
        setTitle(titleField);
        
        listField = new FileBrowserList();
        add(listField);
    }

    
    private class FileBrowserListener implements KeyListener, TrackwheelListener {

        private final static int KEYCODE_ENTER = Keypad.KEY_ENTER;

        // KeypadListener
        public boolean keyChar(char key, int status, int time) {        	
            return false;
        }

        public boolean keyDown(int keycode, int time) {
            int mapped = Keypad.map(keycode);
            if (mapped == KEYCODE_ENTER || keycode == KEYCODE_ENTER) {
            	String entry = listField.getSelectedEntry();
            	
            	if (entry == null)
            		return false;
            	
                if (isDirectory(entry)) {
                	chooseDirectory();
                } else {
                	Tools.getBrowserSession("file://"+currDirName + entry);
                	//chooseFile();
                }
                return true;
            }
            return false;
        }

        public boolean keyRepeat(int keycode, int time) {
            return false;
        }

        public boolean keyStatus(int keycode, int time) {
            return false;
        }

        public boolean keyUp(int keycode, int time) {
            return false;
        }

        public boolean trackwheelClick(int status, int time) {
        	return false;
        }

        public boolean trackwheelRoll(int amount, int status, int time) {
            return false;
        }

        public boolean trackwheelUnclick(int status, int time) {
            return false;
        }
    }

    private boolean isDirectory(String entry) {
        return entry.endsWith("/");
    }

    private Enumeration getFileEntries() {
    	
    	Vector files = new Vector();
    	Enumeration entries = null;
    	
    	try {
    		if (ROOT.equals(currDirName)) {
    			entries = FileSystemRegistry.listRoots();
    		} else {
    			FileConnection fileConn = null;
    			fileConn = (FileConnection)Connector.open("file://" + currDirName, Connector.READ);
    			entries = fileConn.list();
    			fileConn.close();
    		}
    	} catch (Exception e) {
    		Log.error(e, "Cannot get file list");
    	}
    	
    	// Filter out directories
    	while(entries != null && entries.hasMoreElements()) {
    		String entry = (String)entries.nextElement();
    		if (!isDirectory(entry)) {
    			if (extensions.length > 0) {
    				for(int i=0;i<extensions.length;++i) {
    					String ext = extensions[i];
    					
    					if (entry.toLowerCase().endsWith(ext)) {
    						files.addElement(entry);
    						break;
    					}
    				}
    			} else {
    				files.addElement(entry);
    			}
    		}
    	}
    	entries = files.elements();
    	return entries;
    }

    private Enumeration getDirectoryEntries() {

        Enumeration entries = null;
        Vector dirs = new Vector();

            try {
                if (ROOT.equals(currDirName)) {
                    entries = FileSystemRegistry.listRoots();
                } else {
                    FileConnection fileConn = null;
                    fileConn = (FileConnection)Connector.open("file://" + currDirName, Connector.READ);
                    entries = fileConn.list();
                    fileConn.close();
                }
            } catch (Exception e) {
                Log.error(e, "Cannot get file list ");
            }

            // Filter out directories
            while(entries != null && entries.hasMoreElements()) {
                String entry = (String)entries.nextElement();
                if (isDirectory(entry)) {
                    dirs.addElement(entry);
                }
            }
        entries = dirs.elements();

        return entries;
    }

    private class FileBrowserList extends ListField implements ListFieldCallback {

        private final FileBrowserListener listener;

        private Vector  listItems = new Vector();

        public FileBrowserList() {

            // The number of rows is dynamic and it depends on the number of
            // files in the current directory, but the list is populated later
            super(0, Field.FIELD_TOP);

            changeDirectory(0);

            setEditable(false);
            setEmptyString("", DrawStyle.LEFT);

            setCallback(this);
            listener = new FileBrowserListener();
            addListeners();
        }

        public void changeDirectory(int index) {
            int count = 0;
            // Show directories first
            Enumeration dirs = getDirectoryEntries();
            while(dirs.hasMoreElements()) {
                String dir = (String)dirs.nextElement();
                if (count == listItems.size()) {
                    // We need to add a new entry in the list
                    insert(0);
                    FileBrowserListItem item = new FileBrowserListItem(dir, folderIcon);
                    listItems.addElement(item);
                } else {
                    // An item existed already, we just need to refresh it
                    FileBrowserListItem oldItem = (FileBrowserListItem)listItems.elementAt(count);
                    oldItem.setValue(dir);
                    oldItem.setIcon(folderIcon);
                    oldItem.setSeparator(false);
                    oldItem.setSelected(false);
                }
                count++;
            }
            // Now add the separator
            if (count == listItems.size()) {
                insert(0);
                FileBrowserListItem separator = new FileBrowserListItem(null, null);
                separator.setSeparator(true);
                listItems.addElement(separator);
            } else {
                FileBrowserListItem oldItem = (FileBrowserListItem)listItems.elementAt(count);
                oldItem.setValue(null);
                oldItem.setIcon(null);
                oldItem.setSeparator(true);
            }
            count++;
            // Add the files
            Enumeration files = getFileEntries();
            while(files.hasMoreElements()) {
                String file = (String)files.nextElement();
                if (count == listItems.size()) {
                    // We need to add a new entry in the list
                    insert(0);
                    FileBrowserListItem item = new FileBrowserListItem(file, null);
                    listItems.addElement(item);
                } else {
                    // An item existed already, we just need to refresh it
                    FileBrowserListItem oldItem = (FileBrowserListItem)listItems.elementAt(count);
                    oldItem.setValue(file);
                    oldItem.setIcon(null);
                    oldItem.setSeparator(false);
                    oldItem.setSelected(count == index);
                }
                count++;
            }
            // Now remove any left items
            int itemsToDelete = listItems.size() - count;
            for(int i=0;i<itemsToDelete;++i) {
                // We must always remove the last one
                int remIdx = listItems.size() - 1;
                delete(remIdx);
                listItems.removeElementAt(remIdx);
            }
            // We select the index if there is at least one (real) item
            if (count > 1) {
                setSelectedIndex(index);
            }
            // Set the title
            resetTitle();
            // Forces a repaint
            invalidate();
        }

        private int numRows() {
            return listItems.size();
        }

        // ListFieldCallback functions
        public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
            FileBrowserListItem item = ((FileBrowserListItem) listItems.elementAt(index));
            item.draw(graphics, 0, y, width, listField.getRowHeight());
        }

        public Object get(ListField listField, int index) {
            return null;
        }

        public String getSelectedEntry() {
            if (listItems.size() == 0) {
                return "";
            }

            int current = getSelectedIndex();
            if (current >= listItems.size()) {
                // In this case there is something wrong, but we better avoid
                // an ArrayIndexOutOfBoundException
                current = listItems.size() - 1;
            }
            FileBrowserListItem item = ((FileBrowserListItem) listItems.elementAt(current));
            return item.getValue(); 
        }

        public int indexOfList(ListField listField, String prefix, int start) {
            return listField.getSelectedIndex();
        }

        public int getPreferredWidth(ListField listField) {
            return getContentWidth();
        }

        protected int moveFocus(int amount, int status, int time) {
            FileBrowserListItem item;

            int oldSelection = getSelectedIndex();
            item = ((FileBrowserListItem) listItems.elementAt(oldSelection));
            item.setSelected(false);
            invalidate(oldSelection);

            // Forward the call
            int ret = super.moveFocus(amount, status, time);
            int newSelection = getSelectedIndex();

            // Select the new item
            item = ((FileBrowserListItem) listItems.elementAt(newSelection));
            if (item.isSeparator()) {
                // We cannot move on a separator and shall skip it
                if (newSelection > oldSelection) {
                    // Moving downward
                    if (newSelection + 1 < listField.numRows()) {
                        newSelection++;
                    } else {
                        newSelection = oldSelection;
                    }
                } else {
                    // Moving upward
                    if (newSelection - 1 >= 0) {
                        newSelection--;
                    } else {
                        newSelection = oldSelection;
                    }
                }
            }
            item = ((FileBrowserListItem) listItems.elementAt(newSelection));
            item.setSelected(true);
            setSelectedIndex(newSelection);
            invalidate(newSelection);

            //invalidate();

            return ret;
        }
        
        protected void moveFocus(int x, int y, int status, int time) {
        	 FileBrowserListItem item;

            int oldSelection = getSelectedIndex();
            super.moveFocus(x, y, status, time);
            int newSelection = getSelectedIndex();
            
            if(oldSelection != -1) {
	            item = ((FileBrowserListItem) listItems.elementAt(oldSelection));
	            item.setSelected(false);
	            invalidate(oldSelection);
            }
            
            if(newSelection != -1) {
	            // Select the new item
	            item = ((FileBrowserListItem) listItems.elementAt(newSelection));
	            if (item.isSeparator()) {
	                // We cannot move on a separator and shall skip it
	                if (newSelection > oldSelection) {
	                    // Moving downward
	                    if (newSelection + 1 < listField.numRows()) {
	                        newSelection++;
	                    } else {
	                        newSelection = oldSelection;
	                    }
	                } else {
	                    // Moving upward
	                    if (newSelection - 1 >= 0) {
	                        newSelection--;
	                    } else {
	                        newSelection = oldSelection;
	                    }
	                }
	            }
	            item = ((FileBrowserListItem) listItems.elementAt(newSelection));
	            item.setSelected(true);
	            setSelectedIndex(newSelection);
	            invalidate(newSelection);
            }
        }
        

        public void addListeners() {
            addKeyListener(listener);
            addTrackwheelListener(listener);
        }

        public void removeListeners() {
            removeKeyListener(listener);
            removeTrackwheelListener(listener);
        }

        public void setSelectedIndex(int index) {
            int current = getSelectedIndex();
            super.setSelectedIndex(index);
            ((FileBrowserListItem) listItems.elementAt(current)).setSelected(false);
            ((FileBrowserListItem) listItems.elementAt(index)).setSelected(true);
            invalidate();
        }
    }

    private class FileBrowserListItem {

        public static final int MARGIN_SIDE = 5;
        public static final int PADDING     = 2;
        private String          value;
        private Bitmap          icon;
        private boolean         selected    = false;
        private boolean         separator   = false;

        public FileBrowserListItem(String value, Bitmap icon) {
            this.value = value;
            this.icon = icon;
        }

        public void draw(Graphics graphics, int x, int y, int width, int height) {
            if (separator) {
                drawSeparator(graphics, x, y, width, height);
            } else {
                drawBackground(graphics, x, y, width, height);
                int leftImageWidth = drawIcon(graphics, x, y, height);
                drawName(graphics, leftImageWidth, y, width, height);
            }
        }

        private void drawName(Graphics graphics, int x, int y, int width, int height) {

            if (selected) {
                graphics.setColor(Color.WHITE);
            } else {
                graphics.setColor(Color.BLACK);
            }

            graphics.drawText(value, x + PADDING + 3, y + PADDING + 2, DrawStyle.LEFT
                                    | DrawStyle.TOP, width - x - (PADDING * 2));

        }

        private void drawSeparator(Graphics graphics, int x, int y, int width, int height) {

            graphics.setColor(Color.WHITE);
            graphics.fillRect(x - 1, y - 1, width + 2, height + 1);
            graphics.setColor(Color.BLACK);
            graphics.drawLine(x, y + (height / 2), x + width, y + (height / 2));
        }

        public void setSeparator(boolean separator) {
            this.separator = separator;
        }

        public boolean isSeparator() {
            return separator;
        }

        private int drawIcon(Graphics graphics, int x, int y, int height) {

            if (icon == null) {
                return 0;
            }

            int imageWidth  = icon.getWidth();
            int imageHeight = icon.getHeight();
            int imageTop = y + ((height - imageHeight) / 2);
            int imageLeft = x + ((height - imageWidth) / 2);
            graphics.drawBitmap(imageLeft, imageTop, imageWidth, imageHeight, icon, 0, 0);

            return height;
        }

        private void drawBackground(Graphics graphics, int x, int y, int width, int height) {
            if (selected) {
                graphics.setColor(Color.BLUE);
                graphics.fillRect(x - 1, y - 1, width + 2, height + 1);
            } else {
                graphics.setColor(Color.WHITE);
                graphics.fillRect(x - 1, y - 1, width + 2, height + 1);
            }
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setIcon(Bitmap icon) {
            this.icon = icon;
        }
    }

    private class FileBrowserMenu {

		public FileBrowserMenu() {
        }

        private final MenuItem openMenu =
            new MenuItem( resourceBundle, WordPressResource.MENUITEM_OPEN, 20000, 100) {
                public void run() {
                	
                	String entry = listField.getSelectedEntry();
                	
                	if (entry == null)
                		return ;
                	
                    if (isDirectory(entry)) {
                    	chooseDirectory();
                    } else {
                    	//open the file into the browser
                    	Tools.getBrowserSession("file://"+currDirName + entry);
                    }
                }
            };
                   
        private final MenuItem selectMenu =
            new MenuItem(resourceBundle, WordPressResource.MENUITEM_SELECT, 20000, 1000) {
                public void run() {
                    chooseFile();
                }
            };

        private final MenuItem   cancelMenu  =
            new MenuItem(resourceBundle, WordPressResource.MENUITEM_CLOSE, 20000, 1000) {
                public void run() {
                    quit();
                }
            };

        private final MenuItem   upMenu  =
            new MenuItem(resourceBundle, WordPressResource.MENUITEM_UP, 20000, 1000) {
                public void run() {
                    up();
                }
            };

		
	  
        public void makeMenu(Menu menu, String entry)
        { 
        	Log.trace("FileBrowserMenu -- > makeMenu");
        	
            if (!ROOT.equals(currDirName)) {
                menu.add(upMenu);
                if (entry != null && !isDirectory(entry)) {
                    menu.add(selectMenu);
                }
            }
            
            if (entry != null) {
            	menu.add(openMenu);
            }
            
           menu.add(cancelMenu);
        }
    }

    private class DirInfo {
        private String path     = null;
        private int    selected = -1;

        public DirInfo(String path) {
            this.path = path;
        }

        public void setSelected(int selected) {
            this.selected = selected;
        }

        public int getSelected() {
            return selected;
        }

        public String getPath() {
            return path;
        }
    }
}


