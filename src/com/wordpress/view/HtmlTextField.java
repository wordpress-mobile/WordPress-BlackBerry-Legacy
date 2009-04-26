package com.wordpress.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.EditField;

import com.wordpress.bb.WordPressResource;

public class HtmlTextField extends EditField{

	private int mMark = -1;
	//constant that identify type of marker tag
    public final static String LABEL_MARK = "Mark";
    public final static String LABEL_EM = "Emphasis";
    public final static String LABEL_STRONG = "Strong";
    public final static String LABEL_A = "Anchor";
    public final static String LABEL_P = "Paragraph";
    public final static String LABEL_LI = "List Item";
    public final static String LABEL_UL = "Unordered List";
    public final static String LABEL_OL = "Ordered List";
    
	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

    public HtmlTextField(String label, String content, int maxSize) {
        super("", content, maxSize, (EDITABLE | USE_ALL_HEIGHT | USE_ALL_WIDTH ) );
    }
    
    public void commandAction(Command aCommand, Displayable aDisplayable) {
        
    }

    protected void insertTag(String aTag) {
        insertTag(aTag, aTag);
    }

    public void insertImage(String url, String descr){
    	int caret = getCursorPosition();
        insert("<a href=\""+url+"\"  alt=\""+descr+"\">"+descr+"</a>", caret);
    }
    
    protected void insertTag(String aStart, String aEnd) {
        int caret = getCursorPosition();

        if (mMark == -1 || mMark == caret) {
            insert('<' + aStart + "></" + aEnd + '>', caret);
        } else {
            String start = '<' + aStart + '>';
            String end = "</" + aEnd + '>';
            int open;
            int close;
            
            if (mMark < caret) {
                open = mMark;
                close = caret;
            } else {
                open = caret;
                close = mMark;
            }

            insert(start, open);
            insert(end, close + start.length());
        }

        mMark = -1;
    }
    
/*    //Adding context menu
    protected void makeContextMenu(ContextMenu contextMenu) {
        contextMenu.addItem(_emItem);
        contextMenu.addItem(_strongItem);       
    }
*/
    
    private MenuItem _emItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 110, 10) {
        public void run() {
        	
        }
    };

    private MenuItem _strongItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 110, 10) {
        public void run() {
        	
        }
    };
    
    private MenuItem _anchorItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 110, 10) {
        public void run() {
        	
        }
    };
    
    private MenuItem _paragraphItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 110, 10) {
        public void run() {
        	
        }
    };
    
    private MenuItem _ulItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 110, 10) {
        public void run() {
        	
        }
    };
    
    private MenuItem _olItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 110, 10) {
        public void run() {
        	
        }
    };
    
    private MenuItem _liItem = new MenuItem( _resources, WordPressResource.MENUITEM_REFRESHBLOG, 110, 10) {
        public void run() {
        	
        }
    };

 protected void insertHtmlMarkup(String aCommand){
     if (aCommand == HtmlTextField.LABEL_MARK) {
     	mMark=getCursorPosition();	    		
     } else if (aCommand == HtmlTextField.LABEL_EM) {
     	insertTag("em");	
     } else if (aCommand == HtmlTextField.LABEL_STRONG) {
     	insertTag("strong");	
     } else if (aCommand == HtmlTextField.LABEL_A) {
     	insertTag("a href=\"http://\"", "a");
     } else if (aCommand == HtmlTextField.LABEL_P) {
     	insertTag("p");
     } else if (aCommand == HtmlTextField.LABEL_LI) {
     	insertTag("li");	
     } else if (aCommand == HtmlTextField.LABEL_UL) {
     	insertTag("ul");	
     } else if (aCommand == HtmlTextField.LABEL_OL) {
     	insertTag("ol");
     }
 }

}