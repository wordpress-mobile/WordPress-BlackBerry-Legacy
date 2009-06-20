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
import com.wordpress.utils.log.Log;

public class HtmlTextField extends AutoTextEditField {

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
    	super("",content, EditField.DEFAULT_MAXCHARS, EDITABLE | USE_ALL_HEIGHT | USE_ALL_WIDTH | FILTER_DEFAULT);
		this.controller = controller;
        setText(content);
        //this.setChangeListener(listener);
    }
    
    
/*
	private FieldChangeListener listener = new FieldChangeListener() {
	    public void fieldChanged(Field field, int context) {
	    	
	    	AutoTextEditField campoIntelligente = ((AutoTextEditField) field);
	    	
	   	Log.debug("field change listener: "+ ((AutoTextEditField) field).getText());
	    	
	    		int pos = campoIntelligente.getCursorPosition();
	    		Log.info("current pos : "+pos);
	    		//check the current pos
	    		if(pos >= 3) {
	    			//possibly match, compare the 3 prev chars
	    			Log.info("prev 1 char : "+ campoIntelligente.charAt(pos-1)); //ht-t-p
	    			Log.info("prev 2 char : "+ campoIntelligente.charAt(pos-2)); //h-t-tp
	    			Log.info("prev 3 char : "+ campoIntelligente.charAt(pos-3));//h-ttp

	    			if (campoIntelligente.charAt(pos-1) == 'p' && campoIntelligente.charAt(pos-2) == 't' && campoIntelligente.charAt(pos-3) == 't' 
	    				&& ( campoIntelligente.charAt(pos-4) == 'h' || campoIntelligente.charAt(pos-4) == 'H'))
	    				if( pos > 4 ){ //not at the begin of the field, we can check the -4 char
	    					  if(campoIntelligente.charAt(pos-5) == Characters.SPACE || campoIntelligente.charAt(pos-5) == Characters.ENTER){
	    						  Log.debug("match riconosciuto");
	    						    //showRequestPopUp();
	    					  }
	    				} else {
	    					Log.debug("match riconosciuto");
	    					//showRequestPopUp();
	    				}	
	    		}
	    	
	  
	   }
	};
 */   
	/*
    protected boolean keyDown(int keycode, int time) {
    	
    	super.keyDown(keycode, time); //call super for char insertion
    
    	//infer the modifier key state.
    	int status =  Keypad.status(keycode);
    
    	Log.info("keyDown - status : "+status );
    	Log.info("keyDown - keycode : "+keycode );
    	
    	Log.info("keyDown - keycode : "+keycode );
    	Log.info("keyDown - Keypad.key(keycode) : "+ Keypad.key(keycode) );
    	Log.info("keyDown - Keypad.getKeyCode('p',0) : "+Keypad.getKeyCode('p',0) );
    	//Log.info("keyDown - Keypad.keyCode('p',0) : "+Keypad.keycode('p',0) );
    	
    	if(status == 0 && Keypad.key(keycode) == Keypad.getKeyCode('p',0)) {

    	AutoTextEditField campoIntelligente = ((AutoTextEditField) this);
    	
	    		int pos = getCursorPosition();
	    		Log.info("current pos : "+pos);
	    		//check the current pos
	    		if(pos >= 2) {
	    			//possibly match, compare the 3 prev chars
	    			Log.info("prev 1 char : "+ campoIntelligente.charAt(pos-1)); //ht-t-p
	    			Log.info("prev 2 char : "+ campoIntelligente.charAt(pos-2)); //h-t-tp

	    			if (campoIntelligente.charAt(pos-1) == 't' && campoIntelligente.charAt(pos-2) == 't' 
	    				&& (campoIntelligente.charAt(pos-3) == 'h' || campoIntelligente.charAt(pos-3) == 'H')
	    				)
	    				if( pos > 3 ){ //not at the begin of the field, we can check the -4 char
	    					  if(campoIntelligente.charAt(pos-4) == Characters.SPACE || campoIntelligente.charAt(pos-4) == Characters.ENTER){
	    						  showRequestPopUp();
	    					  }
	    				} else {
	    					showRequestPopUp();
	    				}	
	    		}
    	
    	}
    	
    	return false;
    }
    
    private void showRequestPopUp() {
    	
    	int choice =  controller.askQuestion("Http link?");
    	if (choice == Dialog.YES) {
    		
    		TestDialog pw = new TestDialog();
    		if(pw.doModal() == Dialog.YES){
    			//apply change on textField
    			backspace(3, 1); //delete 4 chars
    			insert("<a href=\""+pw.getUrlFromField()+"\"  alt=\""+pw.getDescriptionFromField()+"\">"+pw.getDescriptionFromField()+"</a>",1);
    	      }
    	} else {
    		
    	}
    }
    */
   
	
	
    protected boolean keyChar(char key, int status, int time) {
    	Log.debug("keyChar - char.key : "+key + " | status : "+status);
    	
    	boolean isInserted = super.keyChar(key, status, time); //call super for char insertion
    	
    	if(isInserted && key == 'p') {

    		int pos = getCursorPosition();
    		//Log.debug("current pos : "+pos);
    		//check the current pos
    		if(pos >= 3) {
    			//possibly match, compare the 3 prev chars
    		//	Log.info("prev 1 char : "+ this.charAt(pos-1)); //ht-t-p
    		//	Log.info("prev 2 char : "+ this.charAt(pos-2)); //h-t-tp
    		//	Log.info("prev 3 char : "+ this.charAt(pos-3));//h-ttp

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
    
    
    
  /*  
   * this method works well on 8700 and 8900
   
    protected boolean insert(char key, int status) {
    	//Log.info("insert");
    	Log.info("insert.key : "+key + " | status : "+status);
    	
    	boolean isInserted = super.insert(key,status); //call super for char insertion
    	
    	if(isInserted && key == 'p') {

    		int pos = getCursorPosition();
    		Log.info("current pos : "+pos);
    		//check the current pos
    		if(pos >= 3) {
    			//possibly match, compare the 3 prev chars
    			Log.info("prev 1 char : "+ this.charAt(pos-1)); //ht-t-p
    			Log.info("prev 2 char : "+ this.charAt(pos-2)); //h-t-tp
    			Log.info("prev 3 char : "+ this.charAt(pos-3));//h-ttp

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
*/
    private void showRequestPopUp() {
    	
    	int choice =  controller.askQuestion(_resources.getString(WordPressResource.MESSAGE_HTTP_LINK));
    	if (choice == Dialog.YES) {
    		AddLinkDialog pw = new AddLinkDialog();
    		if(pw.doModal() == Dialog.YES){
    			//apply change on textField
    			backspace(4, 1); //delete 4 chars
    			insert("<a href=\""+pw.getUrlFromField()+"\"  alt=\""+pw.getDescriptionFromField()+"\">"+pw.getDescriptionFromField()+"</a>",1);
    	      }
    	} else {
    		
    	}
    }
    
    public final class AddLinkDialog extends Dialog {

        private EditField urlField;
        private EditField descriptionField;

        public AddLinkDialog(){
            super(Dialog.D_YES_NO, _resources.getString(WordPressResource.LABEL_ADDLINK_TITLE), Dialog.NO, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
            urlField = new EditField(_resources.getString(WordPressResource.LABEL_URL)+ " ", "http://", 100, EditField.EDITABLE);
            urlField.setFilter(new URLTextFilter());
            descriptionField = new EditField(_resources.getString(WordPressResource.LABEL_ADDLINK_DESC)+ " ", "", 50, EditField.EDITABLE);
            
            net.rim.device.api.ui.Manager delegate = getDelegate();
            if( delegate instanceof DialogFieldManager){
                DialogFieldManager dfm = (DialogFieldManager)delegate;
                net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
                if( manager != null ){
                    manager.insert(urlField, 0);
                    manager.insert(descriptionField, 1);
                    urlField.setCursorPosition(7);
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