package com.wordpress.view.component;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.Clipboard;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.text.URLTextFilter;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;
import com.wordpress.view.dialog.InquiryView;

public class HtmlTextField extends AutoTextEditField {

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	private boolean ignore = false;
	    
    static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

    public HtmlTextField(String content) {
        //super(EDITABLE | USE_ALL_HEIGHT | USE_ALL_WIDTH );
    	super("",content, EditField.DEFAULT_MAXCHARS, EDITABLE | USE_ALL_HEIGHT | USE_ALL_WIDTH | FILTER_DEFAULT);
        setText(content);
        this.setChangeListener(listener);
    }
    
    
    protected boolean keyChar(char key, int status, int time) {
    	Log.trace("keyChar - char.key : "+key + " | status : "+status);
    	
    	if(key == Characters.BACKSPACE) {
    		ignore = true;
    	} else {
    		ignore = false;
    	}
    	
    	boolean isInserted = super.keyChar(key, status, time); //call super for char ....calling field change listener here.
    	return isInserted;
    }
    
    
    private FieldChangeListener listener = new FieldChangeListener() {
    	public void fieldChanged(Field field, int context) {
    		
    		AutoTextEditField campoIntelligente = ((AutoTextEditField) field);
    		
    		Log.trace("field change listener: "+ ((AutoTextEditField) field).getText());
    		
    		synchronized (campoIntelligente) {
    			
    			if(ignore == true) {
    				//ignore = false;
    				return;
    			}
    			
    			int pos = campoIntelligente.getCursorPosition();
    			//Log.trace("current pos : "+pos);
    			//check the current pos
    			if(pos >= 3) {
    				//possibly match, compare the 3 prev chars
    				//Log.trace("prev 1 char : "+ campoIntelligente.charAt(pos-1)); //ht-t-p
    				//Log.trace("prev 2 char : "+ campoIntelligente.charAt(pos-2)); //h-t-tp
    				//Log.trace("prev 3 char : "+ campoIntelligente.charAt(pos-3));//h-ttp
    				
    				if (campoIntelligente.charAt(pos-1) == Characters.SPACE && campoIntelligente.charAt(pos-2) == 'a'
    					&& campoIntelligente.charAt(pos-3) == '<' ) {
    					
    					Log.debug("match riconosciuto");
    					signalMatch();
    					InquiryView inqView= new InquiryView(_resources.getString(WordPressResource.MESSAGE_HTTP_LINK));
    					inqView.setDialogClosedListener(new MyDialogClosedListener(3));
    					inqView.show();
    					
    				} else if (campoIntelligente.charAt(pos-1) == 'p' && campoIntelligente.charAt(pos-2) == 't' && campoIntelligente.charAt(pos-3) == 't' 
    					&& ( campoIntelligente.charAt(pos-4) == 'h' || campoIntelligente.charAt(pos-4) == 'H')){
    					Log.debug("match riconosciuto");
    					signalMatch();
    					InquiryView inqView= new InquiryView(_resources.getString(WordPressResource.MESSAGE_HTTP_LINK));
    					inqView.setDialogClosedListener(new MyDialogClosedListener(4));
    					inqView.show();
    				}		
    			}
    		}
    	}
    };
    


    private void signalMatch() {
    	ignore = true;
    }

	private class MyDialogClosedListener implements DialogClosedListener {
		
		private int charsNumber = 0;
		
		public MyDialogClosedListener(int charsNumber) {
			super();
			this.charsNumber = charsNumber;
		}

		
		public void dialogClosed(Dialog dialog, int choice) {
			if(dialog instanceof InquiryView) {
				if (choice == Dialog.YES) {
					AddLinkDialog pw = new AddLinkDialog();
					pw.setDialogClosedListener(new MyDialogClosedListener(this.charsNumber));
					pw.show();
				}
			} else {
				if (choice == Dialog.YES) {
					AddLinkDialog pw = (AddLinkDialog) dialog;
	    			//apply change on textField
	    			backspace(this.charsNumber, 1); //delete chars
	    			insert("<a href=\""+pw.getUrlFromField()+"\"  alt=\""+pw.getDescriptionFromField()+"\">"+pw.getDescriptionFromField()+"</a>",1);
	    	      }
			}
			ignore = false;
		}
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
*/    
    
    public final class AddLinkDialog extends Dialog {

        private EditField urlField;
        private EditField descriptionField;

        public AddLinkDialog(){
            super(Dialog.D_YES_NO, _resources.getString(WordPressResource.LABEL_ADDLINK_TITLE), Dialog.NO, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
            urlField = new EditField(_resources.getString(WordPressResource.LABEL_URL)+ " ", "http://", 255, EditField.EDITABLE);
            urlField.setFilter(new URLTextFilter());
            descriptionField = new EditField(_resources.getString(WordPressResource.LABEL_DESC)+ " ", "", 200, EditField.EDITABLE);
            
            ButtonField buttonPaste= new ButtonField(_resources.getString(WordPressResource.LABEL_ADDLINK_PASTE), ButtonField.CONSUME_CLICK);
            ButtonField buttonClear= new ButtonField(_resources.getString(WordPressResource.LABEL_ADDLINK_CLEAR), ButtonField.CONSUME_CLICK);
            HorizontalFieldManager clearAndPasteButtonsManager = new HorizontalFieldManager(Field.FIELD_HCENTER);
            clearAndPasteButtonsManager.add(buttonPaste);
            clearAndPasteButtonsManager.add(buttonClear);
            buttonPaste.setChangeListener(listenerPasteButton);
            buttonClear.setChangeListener(listenerClearButton);
            
            net.rim.device.api.ui.Manager delegate = getDelegate();
            if( delegate instanceof DialogFieldManager){
                DialogFieldManager dfm = (DialogFieldManager)delegate;
                net.rim.device.api.ui.Manager manager = dfm.getCustomManager();
                if( manager != null ){
                    manager.insert(urlField, 0);
                    manager.insert(descriptionField, 1);
                    urlField.setCursorPosition(7);
                    manager.insert(clearAndPasteButtonsManager, 2);
                }
            }
        }    

    	private FieldChangeListener listenerPasteButton = new FieldChangeListener() {
    	    public void fieldChanged(Field field, int context) {
    	    	Clipboard clipboard = Clipboard.getClipboard();
    	    	String content = clipboard.toString();
    	    	if( content.startsWith("http")) 
    	    		urlField.setText(content);
    	    	else 
    	    		urlField.insert(content);
    	   }
    	};
    	
    	private FieldChangeListener listenerClearButton = new FieldChangeListener() {
    	    public void fieldChanged(Field field, int context) {
    	    	urlField.setText("http://");
    	    	urlField.setCursorPosition(7);    	   
    	   }
    	};
        
        public String getUrlFromField(){
          return urlField.getText();
        }
        
        public String getDescriptionFromField(){
            return descriptionField.getText();
          }
    }
}