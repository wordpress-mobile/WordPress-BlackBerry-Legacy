package com.wordpress.view;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;

import net.rim.device.api.ui.component.EditField;

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
    
    /*
    final static Command mark= new Command(PostTextBox.LABEL_MARK, Command.SCREEN, 2);
    final static Command em=(new Command(PostTextBox.LABEL_EM, Command.SCREEN, 3));
    final static Command strong=(new Command(PostTextBox.LABEL_STRONG, Command.SCREEN, 4));
    final static Command label=(new Command(PostTextBox.LABEL_A, Command.SCREEN, 5));
    final static Command p=(new Command(PostTextBox.LABEL_P, Command.SCREEN, 6));
    final static Command li=(new Command(PostTextBox.LABEL_LI, Command.SCREEN, 7));
    final static Command ul=(new Command(PostTextBox.LABEL_UL, Command.SCREEN, 8));
    final static Command ol=(new Command(PostTextBox.LABEL_OL, Command.SCREEN, 9));
    
	final static Command newPhotoCommand = new Command("New Photo", Command.SCREEN, 1);
	final static Command newVideoCommand = new Command("New Video", Command.SCREEN, 2);
	final static Command newAudioCommand = new Command("New Audio", Command.SCREEN, 3);
    */

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