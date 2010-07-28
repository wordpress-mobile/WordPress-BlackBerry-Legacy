package com.wordpress.view.component;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.ContextMenu;
import net.rim.device.api.ui.DrawStyle;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.FocusChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.Screen;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.AutoTextEditField;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.wordpress.bb.WordPressCore;
import com.wordpress.bb.WordPressResource;
import com.wordpress.utils.log.Log;
import com.wordpress.view.GUIFactory;
import com.wordpress.view.component.MarkupToolBarTextFieldMediator.ButtonState;
import com.wordpress.view.container.JustifiedEvenlySpacedHorizontalFieldManager;

public class HtmlTextField extends AutoTextEditField {

	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
	private boolean ignore = false;
	
	private MarkupToolBarTextFieldMediator mediator;

	//wc variable
	private RE tagRexExp = null;
	private RE htmlWhiteSpaceRegExp = null;
	private RE keepOnlyWordsRegExp = null;
	private RE countWordsRegExp = null;
	private Timer timer;
	private TimerTask updateTask = null;
	    
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
        this.mediator = mediator;
        if(this.mediator != null)
        	this.setChangeListener(newlistener);
        
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
		
		timer = WordPressCore.getInstance().getTimer();
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
    	ignore = true; //skip the http link dialog in this case
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

    	//cancel the queue and reset the state
		if(updateTask != null) 
			updateTask.cancel();
		
		updateTask = new TimerTask() {
			public void run() {
				try {
					updateWordCountField();
				} catch (Throwable t) {
					cancel();
					Log.error(t, "Serious Error in scheduleWordCountUpdate: " + t.getMessage());
					//When updateTask throws an exception, it calls cancel on itself 
					//to remove itself from the Timer. 
					//It then logs the exception.
					//Because the exception never propagates back into the Timer thread, others Tasks continue to function even after 
					//updateTask fails.
				}
			}
		};
		timer.schedule(updateTask, 3000);
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
    	
    		//Log.trace("FieldChangeListener - Context == "+context );
    		if(context == 1){
    			return; //not a user changes
    		}
    		
    		synchronized (field) {
    			if(ignore == true){
    				ignore = false; //storm doesn't fire keychar in same cases
    				return; 
    			}
    			ignore = true;
			}
    		
    		AutoTextEditField campoIntelligente = ((AutoTextEditField) field);
			int pos = campoIntelligente.getCursorPosition();
			if(pos >= 1) {
				if(campoIntelligente.charAt(pos-1) == '<' ) {
					Screen currentScr = UiApplication.getUiApplication().getActiveScreen();
					if(currentScr == null ) return;
					if(!(currentScr instanceof TagPopupScreen)) {
						TagPopupScreen inqView= new TagPopupScreen();
						UiApplication.getUiApplication().pushScreen(inqView);
					}
				}
			}
    	}
    };
    
    private class TagPopupScreen extends PopupScreen {
    	private LabelField tooltipField;
    	
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

    			final String longLabel = tmpState.getLongLabel();
    			
    			BaseButtonField tmpButton= GUIFactory.createButton(tmpLabel, ButtonField.CONSUME_CLICK | ButtonField.USE_ALL_WIDTH | DrawStyle.ELLIPSIS);
    			final int tempIndex = i;
    			tmpButton.setChangeListener(
    					new FieldChangeListener() {
    						public void fieldChanged(Field field, int context) {
    							insertTag(tempIndex);
    						}
    					}
    			);

    			tmpButton.setFocusListener(
    					new FocusChangeListener() {
    						public void focusChanged(Field field, int eventType) {
    							if(eventType == FOCUS_GAINED) {
    								setToolTip(longLabel);
    							}
    						}
    					}
    			);
    			internalBtnContainer.add(tmpButton);
    		}
    		add(internalBtnContainer);
    		SeparatorField separator = GUIFactory.createSepatorField();
    		separator.setMargin(2,0,2,0);
    		add(separator);
    		tooltipField = new LabelField("", Field.USE_ALL_WIDTH);
    		tooltipField.setMargin(0,5,0,5);
    		add(tooltipField);
    	}

    	private void setToolTip(String tooltip){
    		tooltipField.setText(tooltip);
    	}

    	private void insertTag(int buttonPressedIndex) {
    		backspace(1, 1); //delete chars
    		mediator.actionPerformed(buttonPressedIndex);
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

    protected void makeContextMenu(ContextMenu contextMenu) {
    	if(mediator != null) {
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
    	super.makeContextMenu(contextMenu);
    }
}