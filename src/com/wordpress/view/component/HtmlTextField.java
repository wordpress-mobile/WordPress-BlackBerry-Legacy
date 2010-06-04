package com.wordpress.view.component;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;
import com.wordpress.view.GUIFactory;
import com.wordpress.view.component.MarkupToolBarTextFieldMediator.ButtonState;
import com.wordpress.view.container.JustifiedEvenlySpacedHorizontalFieldManager;
import com.wordpress.view.dialog.AddLinkDialog;
import com.wordpress.view.dialog.InquiryView;

public class HtmlTextField extends AutoTextEditField {

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	private boolean ignore = false;
	
	//wc variable
	private RE tagRexExp = null;
	private RE htmlWhiteSpaceRegExp = null;
	private RE keepOnlyWordsRegExp = null;
	private RE countWordsRegExp = null;
	private int wordCountID = -1;
	private MarkupToolBarTextFieldMediator mediator;
	    
	static {
        //retrieve a reference to the ResourceBundle for localization support
        _resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }

	public HtmlTextField(String content) {
		this(content, null);
	}
	
    public HtmlTextField(String content, MarkupToolBarTextFieldMediator mediator) {
    	super("",content, EditField.DEFAULT_MAXCHARS, EDITABLE | USE_ALL_HEIGHT | USE_ALL_WIDTH | FILTER_DEFAULT);
        setText(content);
        this.setChangeListener(newlistener);
        this.mediator = mediator;
        
        try {
			tagRexExp = new RE("<.[^<>]*?>");
		} catch (RESyntaxException e) {
			Log.error(e, "errore while compiling regexp: <.[^<>]*?>");
		}
		
		try {
			htmlWhiteSpaceRegExp = new RE("&nbsp;|&#160;");
			htmlWhiteSpaceRegExp.setMatchFlags(RE.MATCH_CASEINDEPENDENT);
		} catch (RESyntaxException e) {
			Log.error(e, "errore while compiling regexp: &nbsp;|&#160;");
		}
		
		try {
			keepOnlyWordsRegExp = new RE("[0-9.(),;:!?%#$Â¿'\"_+=\\/\\-]");
		} catch (RESyntaxException e) {
			Log.error(e, "errore while compiling regexp: [0-9.(),;:!?%#$Â¿'\"_+=\\/\\-]");
		}
		
		try {
			countWordsRegExp = new RE("\\S\\s+");
		} catch (RESyntaxException e) {
			Log.error(e, "errore while compiling regexp: \\S\\s+");
		}
		updateWordCountField();
    }
    
	public int countWordPressWord(String text) {
		if( tagRexExp == null || htmlWhiteSpaceRegExp == null || keepOnlyWordsRegExp == null || countWordsRegExp == null)
			return 0;
		text = text.trim()+" ";
		String tmpString = tagRexExp.subst(text, " ");
		tmpString = htmlWhiteSpaceRegExp.subst(tmpString, " ");
		tmpString = keepOnlyWordsRegExp.subst(tmpString, "");
		int count = 0;
		tmpString = countWordsRegExp.subst(tmpString, "1");

		for (int i = 0; i < tmpString.length(); i++) {
			if(tmpString.charAt(i) == '1')
				count++;
		}

		return count;
	}
    
	private void updateWordCountField() {
		if(mediator == null) return;
		int countWord = countWordPressWord(this.getText());
		mediator.updateWordCounter(countWord);
	}
		
    public void insertTextFromExt(String text, boolean shouldIgnore) {
    	ignore = true; //skip the http link dialog in this casw
    	insert(text,1); //inser the text at the current carret pos
    }
    
    //add the word counts
    protected void onUnfocus(){
    	super.onUnfocus();
    	scheduleWordCountUpdate();
    };

    //add the word counts
    protected void onFocus(int direction){
    	super.onFocus(direction);
    	scheduleWordCountUpdate();
    };
    
    private void scheduleWordCountUpdate() {
    	
    	if(mediator == null) return;
    	
    	if(wordCountID != -1 ) {
    		try {
    			UiApplication.getUiApplication().cancelInvokeLater(wordCountID);
    			//	Log.trace("wordCount runnable obj removed from the queue");
    		} catch (Exception e) {
    			Log.error(e, "no wordCount runnable obj in the queue");
    		}
    	}

    	//Log.trace("wordCount runnable inserted in the queue");
    	wordCountID = UiApplication.getUiApplication().invokeLater(new Runnable() {
    		public void run() {
    			updateWordCountField();
    		}
    	} , 3000, false);
    	//end of count word section
    }
    
    protected boolean keyChar(char key, int status, int time) {
    	//Log.trace("keyChar - char.key : "+key + " | status : "+status);
    	//count word section
    	scheduleWordCountUpdate();
    	if(key == Characters.BACKSPACE) {
    		ignore = true;
    	} else {
    		ignore = false;
    	}
    	
    	boolean isInserted = super.keyChar(key, status, time); //call super for char ....calling field change listener here.
    	return isInserted;
    }
    
    private FieldChangeListener newlistener = new FieldChangeListener() {
    	public void fieldChanged(Field field, int context) {

    		if(context == 1){
    			//Log.trace("Context == 1" );
    			return; //not user changed
    		}
    		
    		synchronized (field) {
    			if(ignore == true){
    				return; 
    			} else {
    				ignore = true;
    			}
			}
    		
    		AutoTextEditField campoIntelligente = ((AutoTextEditField) field);
			int pos = campoIntelligente.getCursorPosition();
			if(pos >= 1)
				if(campoIntelligente.charAt(pos-1) == '<' ) {
					TagPopupScreen inqView= new TagPopupScreen();
					UiApplication.getUiApplication().pushScreen(inqView);
				}
				
    	}
    };
    
    
    private FieldChangeListener listener = new FieldChangeListener() {
    	public void fieldChanged(Field field, int context) {

    		if(context == 1){
    			//Log.trace("Context == 1" );
    			return; //not user changed
    		}

    		AutoTextEditField campoIntelligente = ((AutoTextEditField) field);

    		//	Log.trace("field change listener: "+ ((AutoTextEditField) field).getText());

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
    
    
    private class TagPopupScreen extends PopupScreen {
    	  public TagPopupScreen()
    	    {
    	        super(new VerticalFieldManager(Field.FIELD_HCENTER | Manager.NO_VERTICAL_SCROLL),Field.FOCUSABLE);
    	        ButtonState[] buttonStateList = mediator.getButtonStateList();
    	        JustifiedEvenlySpacedHorizontalFieldManager internalBtnContainer = new JustifiedEvenlySpacedHorizontalFieldManager();
    	    	for (int i = 0; i < buttonStateList.length; i++) {
    	    		ButtonState tmpState = buttonStateList[i];
    	    		String tmpLabel = null;
    	    		if(tmpState.isOpen())
    	    			tmpLabel = '/' + tmpState.getLabel();
    	    		else 
    	    			tmpLabel = tmpState.getLabel();
    				
    	    		BaseButtonField tmpButton= GUIFactory.createButton(tmpLabel, ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
    				final int tempIndex = i;
    				tmpButton.setChangeListener(
    					new FieldChangeListener() {
    						public void fieldChanged(Field field, int context) {
    							hh(tempIndex);
    						}
    					}
    				);
    				internalBtnContainer.add(tmpButton);
    			}
    	    	add(internalBtnContainer);
    	    }
    	  
    		private void hh(int selection) {
    			backspace(1, 1); //delete chars
    			mediator.actionPerformed(selection);
    		    close();
    		};
    		
    		protected boolean keyChar(char c, int status, int time) {
    			// Close this screen if escape is selected.
    			if (c == Characters.ESCAPE) {
    				this.close();
    				return true;
    			} 
    			else 	
    			return super.keyChar(c, status, time);
    		}
    }
    
    
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

    protected void makeContextMenu(ContextMenu contextMenu) {
    	ButtonState[] buttonStateList = mediator.getButtonStateList();
    	for (int i = 0; i < buttonStateList.length; i++) {
    		ButtonState tmpState = buttonStateList[i];
    		final int currentIndex = i;
    		String tmpLabel = null;
    		if(tmpState.isOpen())
    			tmpLabel = '/' + tmpState.getLongLabel();
    		else 
    			tmpLabel = tmpState.getLongLabel();
    		MenuItem tmpMenuItem = new MenuItem(tmpLabel, 10, 10) {
    	        public void run() {
    	        	mediator.actionPerformed(currentIndex);
    	        }
    	    };
    	    contextMenu.addItem(tmpMenuItem);
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
}