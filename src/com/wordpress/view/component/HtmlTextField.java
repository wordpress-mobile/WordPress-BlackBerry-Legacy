package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.controller.BlogObjectController;

public class HtmlTextField extends AutoTextEditField{

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
	private final BlogObjectController controller;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

    public HtmlTextField(BlogObjectController controller, String content) {
        //super(EDITABLE | USE_ALL_HEIGHT | USE_ALL_WIDTH );
    	super("",content, EditField.DEFAULT_MAXCHARS, EDITABLE | USE_ALL_HEIGHT | USE_ALL_WIDTH );
		this.controller = controller;
        setText(content);
    }
    
    
    protected boolean insert(char key, int status) {
    	//System.out.println("char.key : "+key + " | status : "+status);
    	
    	boolean isInserted = super.insert(key,status); //call super for char insertion
    	
    	if(isInserted && key == 'p') {

    		int pos = getCursorPosition();
    	//	System.out.println("current pos : "+pos);
    		//check the current pos
    		if(pos >= 3) {
    			//possibly match, compare the 3 prev chars
    		/*	System.out.println("prev 1 char : "+ this.charAt(pos-1)); //ht-t-p
    			System.out.println("prev 2 char : "+ this.charAt(pos-2)); //h-t-tp
    			System.out.println("prev 3 char : "+ this.charAt(pos-3));//h-ttp
    			*/

    			if (this.charAt(pos-1) == 'p' && this.charAt(pos-2) == 't' && this.charAt(pos-3) == 't' 
    				&& ( this.charAt(pos-4) == 'h' || this.charAt(pos-4) == 'H'))
    				if( pos > 4 ){ //not at the begin of the field, we can check the -4 char
    					  if(this.charAt(pos-5) == Characters.SPACE || this.charAt(pos-5) == Characters.ENTER)
    						  showRequestPopUp();
    				} else {
    					showRequestPopUp();
    				}	
    		}
    	}
    	return isInserted;
    }

    private void showRequestPopUp() {
    	System.out.println("trovato match");
    	
    	int choice =  controller.askQuestion("Http link?");
    	if (choice == Dialog.YES) {
    		//System.out.println("ha detto si!!");
    		
    		TestDialog pw = new TestDialog();
    		if(pw.doModal() == Dialog.YES){
    			System.out.println("ha inserito bene i dati");
    			//apply change on textField
    			backspace(4, 1); //delete 4 chars
    			insert("<a href=\""+pw.getUrlFromField()+"\"  alt=\""+pw.getDescriptionFromField()+"\">"+pw.getDescriptionFromField()+"</a>",1);
    	      }
    	} else {
    		//System.out.println("ha detto no!!");
    	}
    }
    
    public final class TestDialog extends Dialog {

        private EditField urlField;
        private EditField descriptionField;

        public TestDialog(){
            super(Dialog.D_YES_NO, "Insert Link Informations", Dialog.NO, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
            urlField = new EditField("Url: ", "http://", 100, EditField.EDITABLE);
            urlField.setFilter(new URLTextFilter());
            descriptionField = new EditField("Description: ", "", 50, EditField.EDITABLE);
            
            net.rim.device.api.ui.Manager delegate = getDelegate();
            if( delegate instanceof DialogFieldManager){
                DialogFieldManager dfm = (DialogFieldManager)delegate;
                net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
                if( manager != null ){
                    manager.insert(urlField, 0);
                    manager.insert(descriptionField, 1);
                }
            }
        }    

        public String getUrlFromField(){
          return urlField.getText();
        }
        
        public String getDescriptionFromField(){
            return descriptionField.getText();
          }
    }

    /*       
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
    
    protected void insertTag(String aTag) {
    	insertTag(aTag, aTag);
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
    
    //Adding context menu
    protected void makeContextMenu(ContextMenu contextMenu) {
        contextMenu.addItem(_emItem);
        contextMenu.addItem(_strongItem);       
    }
*/
    /*
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
*/

}