//#preprocess
package com.wordpress.view.component.FileBrowser;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.KeypadListener;
import net.rim.device.api.ui.Color;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
//#ifdef IS_OS47_OR_ABOVE
import net.rim.device.api.ui.TouchEvent;
//#endif
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;


import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.io.JSR75FileSystem;
import com.wordpress.utils.ImageUtils;
import com.wordpress.utils.Tools;
import com.wordpress.utils.log.Log;

public class RimFileBrowser extends PopupScreen {


    private static final String  ROOT           = "/";
    protected FileBrowserList    listField      = null;
    private String               currDirName    = ROOT;
    private String[] extensions; // File extensions to filter by.
    private FileBrowserMenu      menu;
    private boolean              quit           = false;
    private Stack                dirStack       = new Stack();

    
	private RimFileBrowserListener listener;
    private ResourceBundle resourceBundle;
    private LabelField currentPathLabelField = new LabelField("/", DrawStyle.ELLIPSIS);
    
    private boolean isThumbEnabled = false;
	private String thumbEnabledExtensions[] = { "jpg", "jpeg","bmp", "png", "gif"}; //file ext to read for thumb generation
	private Bitmap predefinedThumb = null; 
    private Bitmap loadingThumb = null;
    private int predefinedThumbWidth = 48;
    private int predefinedThumbHeight = 48;
    private Stack thumbStack = new Stack(); //stack of file to thumb 
    private Bitmap   folderIcon     = null;
    private Bitmap   folderIconBig     = null;
    

    private Thread t = null;
    private boolean isThumbRunning = false;
    
    private WordPressCore wpCore = null;

    
	public RimFileBrowser(String[] extensions, boolean isThumbEnabled) {
    	
    	super(new VerticalFieldManager(), DEFAULT_MENU|DEFAULT_CLOSE);
    	
    	resourceBundle = WordPressCore.getInstance().getResourceBundle();

    	if(extensions == null) {
    		this.extensions = new String[0];
    	} else {
    		this.extensions = extensions;
    	}
    	
    	this.isThumbEnabled = isThumbEnabled;
    	
        initialize();
        wpCore = WordPressCore.getInstance(); 
        if(wpCore.getLastFileBrowserPath() != null)
        	this.setPath(wpCore.getLastFileBrowserPath());
    }
    
    
    protected void sublayout(int width, int height) {    
    	layoutDelegate(width - 20, height - 20);
    	setPositionDelegate(5, 5);
    	setExtent(width - 10, height - 10);
    	setPosition(5, 5);
    }
    
    
	public boolean onMenu(int instance) {
		boolean result;
		// Prevent the context menu from being shown if focus
		// is on the list
		if (instance == Menu.INSTANCE_CONTEXT) {
			result = false;
		} else {
			result = super.onMenu(instance);
		}
		return result;
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
        if (listField != null ) {
            entry = listField.getSelectedEntry();
        }        
       if (entry != null && !entry.equalsIgnoreCase("")) {
	    		    	
	        if (isDirectory(entry)) {
	        	chooseDirectory();
	        } else {
	        	Tools.openFileWithExternalApp("file://"+currDirName + entry);
	        }
        }
    }
    
    
    private Bitmap getPredefinedFolderIcon() {
    	if(!isThumbEnabled) return folderIcon;
    	else return folderIconBig;
    }
    
    /**
	 * get the thumb - 
	 * @return
	 */
	private Bitmap getThumb(String path) {
		boolean isThumbAllowed = false;
		for(int i=0;i<thumbEnabledExtensions.length;++i) {
			String ext = thumbEnabledExtensions[i];
			
			if (path.toLowerCase().endsWith(ext)) {
				isThumbAllowed = true;				
			}
		}
		
		if(!isThumbAllowed)
			return predefinedThumb;
		
		byte[] readFile;
		try {
			readFile = JSR75FileSystem.readFile(path);
			EncodedImage img = EncodedImage.createEncodedImage(readFile, 0, -1);
			//find the photo size
			int scale = ImageUtils.findBestImgScale(img.getWidth(), img.getHeight(), predefinedThumbWidth, predefinedThumbHeight);
			if(scale > 1)
				img.setScale(scale); //set the scale
			img.setDecodeMode(EncodedImage.DECODE_ALPHA | EncodedImage.DECODE_READONLY);
			Bitmap bitmap = img.getBitmap();
			img = null;
			return bitmap;
		} catch (IOException e) {
			Log.error(e, "Error while creating the img thumb");
			return predefinedThumb;
		}
	}
	
    
    public boolean onClose() {
        if (!quit && !ROOT.equals(currDirName)) {
            // Change to the previous directory
            up();
            return false; 
        }

        try {
        	stopThumbLoader();
        	this.close();
            return true;
        } catch (Exception e) {
            Log.error("Cannot close FileBrowser " + e.toString());
            return false;
        }
    }

    protected void onDisplay() {

        Log.trace("RimFileBrowser - OnDisplay");
        int rowHeight = getFont().getHeight() + 10;

        if(isThumbEnabled) {
        	if(rowHeight < predefinedThumbHeight)
        		rowHeight = predefinedThumbHeight + 8;
        } else {
            if(rowHeight < folderIcon.getHeight())
            	rowHeight = folderIcon.getHeight() + 10;
        }

        listField.setRowHeight(rowHeight);
        super.onDisplay();
    }

   
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
            
            wpCore.setLastFileBrowserPath(currDirName);
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
        
        if (listener != null) {
            listener.selectionDone(currDirName + entryName);
        }

        quit = true;
        onClose();
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
        
        wpCore.setLastFileBrowserPath(currDirName);
        
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
        currentPathLabelField.setText(title);
//        titleField.setTitle(title);
    }

    
    private void initialize() {

        Log.trace("[RimFileBrowser.initialize]");

        menu = new FileBrowserMenu();
        // Pre load the icons
        folderIcon = Bitmap.getBitmapResource("folder_yellow_open.png");
        folderIconBig = Bitmap.getBitmapResource("folder_yellow_open_48.png");
        predefinedThumb = Bitmap.getBitmapResource("mime_unknown_48.png");
        loadingThumb = Bitmap.getBitmapResource("file_temporary_48.png");
        resetTitle();                
        listField = new FileBrowserList();
        add(currentPathLabelField);
        add( new SeparatorField());
        VerticalFieldManager internalListManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR);
        internalListManager.add(listField);        
        add(internalListManager);
        
        startThumbLoader();
    }

    
  public void setPredefinedThumb(Bitmap predefinedThumb) {
		this.predefinedThumb = predefinedThumb;
	}


	/*  private class FileBrowserListener implements KeyListener {

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
    }
*/
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

    
    private void startThumbLoader() {
    	Log.trace("starting Thumb Thread");
    	if(t != null) {
    		Log.trace("there is an istance of Thumb Thread already running");
    		stopThumbLoader();
    	}
    	
    	t = new Thread(new FileBrowserListThumbLoader());
		t.setPriority(Thread.MIN_PRIORITY); //thread by default is set to priority normal
		isThumbRunning = true;
		t.start();
    }
    
    private void stopThumbLoader() {
    	Log.trace(">>> stopThumbLoader");
    	isThumbRunning = false;
    	try {
    		if (t != null && t.isAlive() ) {
				Log.trace("interrupting Thumb Thread");
				t.interrupt();
    		}
		} catch (Exception e) {
			Log.error(e, "Error while interrupting Thumb Thread");
		} finally {
			t = null;
			Log.trace("Thumb Thread was set to null");
		}
    }
    
    private class FileBrowserListThumbLoader implements Runnable {

    	//Object[] item = {item, String.valueOf(index)};
    	Object[] currentItem = null;
		
    	public void run() {
    		next();
    	}
    	private void next() {
    		Log.trace("FileBrowserListThumbLoader.next");
    		synchronized (thumbStack) {
    			while(!isThumbEnabled || thumbStack.size() == 0) {
    				try {
    					thumbStack.wait();
    				} catch (InterruptedException e) {
    					//Log.error(e, "Error while synch over thumbStack");
    				}
    			}
    			currentItem =  (Object[]) thumbStack.pop();
    		}
    		doThumb();
    	}
    	
    	private void doThumb() {
    		Log.trace("FileBrowserListThumbLoader.doThumb");    		
    		FileBrowserListItem item = (FileBrowserListItem) currentItem[0];
    		if (item.isFile() && item.getThumb() == null) {
    			String path = "file://"+currDirName+item.getValue();
    			Bitmap thumb = getThumb(path);
    			item.setThumb(thumb);
    			item = null;
    			Integer indexOfCurrentItem = (Integer)currentItem[1];
    			listField.invalidate(indexOfCurrentItem.intValue());
    		}
    		next();
    	}
    }
    
    private class FileBrowserList extends ListField implements ListFieldCallback {

        private Vector  listItems = new Vector();

        public Vector getListItems() {
			return listItems;
		}

		public FileBrowserList() {

            // The number of rows is dynamic and it depends on the number of
            // files in the current directory, but the list is populated later
            super(0, Field.FIELD_TOP);

            changeDirectory(0);

            setEditable(false);
            setEmptyString("", DrawStyle.LEFT);

            setCallback(this);
           // listener = new FileBrowserListener();
         //   addListeners();
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
        
    	
        
    	//#ifdef IS_OS47_OR_ABOVE
    	protected boolean touchEvent(TouchEvent message) {
    		Log.trace(">>> touchEvent");
    		int eventCode = message.getEvent();
    		
    		/*if(!this.getContentRect().contains(message.getX(1), message.getY(1)))
    		{       			
    			return false;
    		} */
    		
    		// Get the screen coordinates of the touch event
    		if(eventCode == TouchEvent.CLICK) {
    			Log.trace("TouchEvent.CLICK");
    			performDefaultActionOnItem();
    			return true;
			} 
			return false; 
    	}
    	//#endif
    	
        
        public void changeDirectory(int index) {

        	synchronized (thumbStack) {
        		//this cleans the stack from the previous files queue that needing thumb
        		thumbStack.removeAllElements();
        	}
        	
            int count = 0;
            // Show directories first
            Enumeration dirs = getDirectoryEntries();
            listItems.removeAllElements();
            
            int test = this.getSize();
            while (test > 0 ) {
            	this.delete(0);
            	test--;
            }
            
            while(dirs.hasMoreElements()) {
                String dir = (String)dirs.nextElement();
                // We need to add a new entry in the list
                insert(0);
                FileBrowserListItem item = new FileBrowserListItem(dir, getPredefinedFolderIcon());
                listItems.addElement(item);
                count++;
            }
            
            // Add the files
            Enumeration files = getFileEntries();
          
            // Now add the separator only if there are directory and files 
            if(files.hasMoreElements())
                if(count > 0) {
	                insert(0);
	                FileBrowserListItem separator = new FileBrowserListItem(null, null);
	                separator.setSeparator(true);
	                listItems.addElement(separator);
    	            count++;
                }
            
            while(files.hasMoreElements()) {
                String file = (String)files.nextElement();
                // We need to add a new entry in the list
                insert(0);
                FileBrowserListItem item = new FileBrowserListItem(file, null);
                listItems.addElement(item);
                count++;
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
         
            if (item.isFile() && item.getThumb() == null) { //thumb not yet created
	            String path = item.getValue();
	            boolean isValidFileForThumb = false;
	            for(int i=0;i<thumbEnabledExtensions.length;++i) {
	    			String ext = thumbEnabledExtensions[i];
	    			if (path.toLowerCase().endsWith(ext)) {
	    				isValidFileForThumb = true;
	    				Object[] tmpValues = {item, new Integer(index)};
	    				synchronized (thumbStack) {
	    					thumbStack.push(tmpValues);
	    					thumbStack.notifyAll();
						}
	    				break;
	    			}
	    		}
	            
	            if(isValidFileForThumb == false) {
	            	item.setThumb(predefinedThumb);
	            }
	            
            }
            
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
            
            if(listItems.size() == 0) return super.moveFocus(amount, status, time);
            
            int oldSelection = getSelectedIndex();
            item = ((FileBrowserListItem) listItems.elementAt(oldSelection));
            item.setSelected(false);
            invalidate(oldSelection);
            
            // Forward the call
            int ret = super.moveFocus(amount, status, time);
            
            if(listItems.size() == 0) return ret;
            
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
            
            if(listItems.size() == 0) return;
            
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
        

     /*   public void addListeners() {
            addKeyListener(listener);
        }

        public void removeListeners() {
            removeKeyListener(listener);
        }
*/
        public void setSelectedIndex(int index) {
            int current = getSelectedIndex();
            super.setSelectedIndex(index);
            ((FileBrowserListItem) listItems.elementAt(current)).setSelected(false);
            ((FileBrowserListItem) listItems.elementAt(index)).setSelected(true);
            invalidate(current);
            invalidate(index);
        }
    }

    private class FileBrowserListItem {

        public static final int MARGIN_SIDE = 5;
        public static final int PADDING     = 2;
        private String          value;
        private Bitmap          icon;
        private boolean         selected    = false;
        private boolean         separator   = false;

        private Bitmap    thumb;
		private int thumbHeight;
		private int thumbWidth;

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
        	int fontHeight = graphics.getFont().getHeight();
        	int imageTop = y + ((height - fontHeight) / 2);
            graphics.drawText(value, x + PADDING + 3, imageTop, DrawStyle.LEFT
                                    | DrawStyle.ELLIPSIS , width - x - (PADDING * 2));

        }

        private void drawSeparator(Graphics graphics, int x, int y, int width, int height) {
            graphics.drawLine(x, y + (height / 2), x + width, y + (height / 2));
        }

        public void setSeparator(boolean separator) {
            this.separator = separator;
        }

        public boolean isSeparator() {
            return separator;
        }

        public boolean isFile() {
        	   if (icon == null &&  value != null) 
        		   return true;
        	   else return false; 
        }
       
        private int drawIcon(Graphics graphics, int x, int y, int height) {

        	//this is a file, draw the thumb if enabled 
            if (isFile()) {
            	
            	if (!isThumbEnabled) return 0; //thumb not enabled 
            	
            	Bitmap currentBitmap = null;
            	//put the default thumb size first.
            	int currentBitmapWidth  = 0;
            	int currentBitmapHeight = 0;
            	//thumb is not already loaded, shows the default one
            	if(thumb == null) {
            		currentBitmap = loadingThumb;
            		currentBitmapWidth = predefinedThumbWidth;
                	currentBitmapHeight = predefinedThumbHeight;
                } else {
                	currentBitmap = this.thumb;
                	currentBitmapWidth = thumbWidth;
                	currentBitmapHeight = thumbHeight;
                }

	            int imageTop = y + ((height - currentBitmapHeight) / 2);
	            int imageLeft = x + ((height - currentBitmapWidth) / 2);
    			graphics.drawBitmap(imageLeft, imageTop, currentBitmapWidth, currentBitmapHeight, currentBitmap, 0, 0);            	
            } else {
            	//this is a folder
	            int imageWidth  = icon.getWidth();
	            int imageHeight = icon.getHeight();
	            int imageTop = y + ((height - imageHeight) / 2);
	            int imageLeft = x + ((height - imageWidth) / 2);
	            graphics.drawBitmap(imageLeft, imageTop, imageWidth, imageHeight, getPredefinedFolderIcon(), 0, 0);
            }
            
            return height;
        }

        private void drawBackground(Graphics graphics, int x, int y, int width, int height) {
            int color = graphics.getColor();
        	if (selected) {
                graphics.setColor(Color.BLUE);
                graphics.fillRect(x - 1, y - 1, width + 2, height + 1);
            } 
            graphics.setColor(color);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getValue() {
            return value;
        }

        public Bitmap getThumb() {
        	return thumb;
        }
         
        public void setThumb(Bitmap thumb) {
            this.thumb = thumb;
            this.thumbWidth = thumb.getWidth();
        	this.thumbHeight = thumb.getHeight();
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
                    	Tools.openFileWithExternalApp("file://"+currDirName + entry);
                    }
                }
            };
                   
        private final MenuItem selectMenu =
            new MenuItem(resourceBundle, WordPressResource.MENUITEM_SELECT, 20000, 1000) {
                public void run() {
                    chooseFile();
                }
            };
            //0x00010000
        private final MenuItem   cancelMenu  =
            new MenuItem(resourceBundle, WordPressResource.MENUITEM_CLOSE, 2000000, 1000) {
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
            
        private final MenuItem   viewTitlesMenu  =
            	new MenuItem(resourceBundle, WordPressResource.MENUITEM_VIEW_TITLES, 200000, 1000) {
            	public void run() {
            		Log.trace(">>> viewTitlesMenu");
            		synchronized (thumbStack) {
            			isThumbEnabled = false;
            			thumbStack.removeAllElements();
					}
            		onDisplay();
            		listField.invalidate();
            	}
            };
            
            private final MenuItem   viewThumbnailsMenu  =
            	new MenuItem(resourceBundle, WordPressResource.MENUITEM_VIEW_THUMBNAILS, 200000, 1000) {
            	public void run() {
            		isThumbEnabled = true;
            		onDisplay();
            		listField.invalidate();
            	}
            };
	  
        public void makeMenu(Menu menu, String entry)
        { 
        	Log.trace("FileBrowserMenu -- > makeMenu");
        	
            if (!ROOT.equals(currDirName)) {
                menu.add(upMenu);
                if (entry != null && !entry.equalsIgnoreCase("") && !isDirectory(entry)) {
                    menu.add(selectMenu);
                }
            }
            
            if (entry != null && !entry.equalsIgnoreCase("")) {
            	menu.add(openMenu);
            }

            if(isThumbEnabled)
            	menu.add(viewTitlesMenu);
            else
            	menu.add(viewThumbnailsMenu);
            
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
