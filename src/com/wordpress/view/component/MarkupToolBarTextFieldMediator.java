package com.wordpress.view.component;

import com.wordpress.bb.WordPressResource;
import com.wordpress.view.dialog.AddLinkDialog;

import net.rim.device.api.i18n.ResourceBundle;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.LabelField;

public class MarkupToolBarTextFieldMediator {
	
	//create a variable to store the ResourceBundle for localization support
    protected static ResourceBundle _resources;
    static {
    	//retrieve a reference to the ResourceBundle for localization support
    	_resources = ResourceBundle.getBundle(WordPressResource.BUNDLE_ID, WordPressResource.BUNDLE_NAME);
    }
    
    private MarkupToolBar tb; 
    private HtmlTextField textField;
    private LabelField wcField;

    private ButtonState[] buttonStateList = null; //the buttons/menuItem elements

	public MarkupToolBarTextFieldMediator() {
		buttonStateList= new ButtonState[6];
		buttonStateList[0] = new ButtonState("B", _resources.getString(WordPressResource.MENUITEM_HTML_BOLD), "b");
		buttonStateList[1] = new ButtonState("I", _resources.getString(WordPressResource.MENUITEM_HTML_EMPH) ,"em");
		buttonStateList[2] = new ButtonState("L",_resources.getString(WordPressResource.MENUITEM_HTML_LINK), "a");
		buttonStateList[3] = new ButtonState("UL",_resources.getString(WordPressResource.MENUITEM_HTML_UL), "ul");
		buttonStateList[4] = new ButtonState("OL", _resources.getString(WordPressResource.MENUITEM_HTML_OL), "ol");
		buttonStateList[5] = new ButtonState("LI",_resources.getString(WordPressResource.MENUITEM_HTML_LI), "li");	
	}
		
	public ButtonState[] getButtonStateList() {
		return buttonStateList;
	}

	public void setTb(MarkupToolBar tb) {
		this.tb = tb;
	}
	
	public void setTextField(HtmlTextField textField) {
		this.textField = textField;
	}
	
	public void setWcField(LabelField wcField) {
		this.wcField = wcField;
	}
	
	private void setFocusOnTextField() {
		if(textField != null)
			textField.setFocus();
	}
	
	public void updateWordCounter(final int count) {
		if(wcField != null) {
			UiApplication.getUiApplication().invokeLater(new Runnable() {
				public void run() {
					wcField.setText(_resources.getString(WordPressResource.LABEL_WORD_COUNT)+": "+count);
				}
			});			
		}
	}
	
	/* btn or menuItem was clicked */
	public void actionPerformed(int i){
		ButtonState cookie = buttonStateList[i];
		
		if(cookie.getTag().equalsIgnoreCase("a")) {
			AddLinkDialog pw = new AddLinkDialog();
			pw.setDialogClosedListener(new MyDialogClosedListener());
			pw.show();
			return;
		} 
				
		//tag is already opened, should be closed
		if(cookie.isOpen) { 
			insertTag(cookie.getTag(), false);
			cookie.setOpen(false);
			tb.changeButtonLabel(i,cookie.getLabel());
		} else {
			insertTag(cookie.getTag(), true);
			cookie.setOpen(true);
			tb.changeButtonLabel(i,'/'+cookie.getLabel());
		}
		
		this.setFocusOnTextField(); 
	}
	
	
    private void insertTag(String aTag, boolean open) {
    	if (!open)
    		aTag = '/'+aTag;
    	 this.insertText('<' + aTag + ">");
    }
    
	public void insertText(String text) {
		if(textField != null)
			textField.insertTextFromExt(text, true);
	}
	
    public class ButtonState {
    	private String label;
    	private String tag;
    	private boolean isOpen;
		private final String longLabel;

		public ButtonState(String label, String longLabel, String tag) {
			super();
			this.label = label;
			this.longLabel = longLabel;
			this.tag = tag;
		}
    	public boolean isOpen() {
			return isOpen;
		}
		public void setOpen(boolean isOpen) {
			this.isOpen = isOpen;
		}
		public String getLabel() {
			return label;
		}
    	public String getTag() {
			return tag;
		}
		public String getLongLabel() {
			return longLabel;
		}	
    }

    
	private class MyDialogClosedListener implements DialogClosedListener {

		public MyDialogClosedListener() {
			super();
		}

		public void dialogClosed(Dialog dialog, int choice) {
			if (choice == Dialog.YES) {
				AddLinkDialog pw = (AddLinkDialog) dialog;
				insertText("<a href=\""+pw.getUrlFromField()+"\"  alt=\""+pw.getDescriptionFromField()+"\">"+pw.getDescriptionFromField()+"</a>");
			}
			setFocusOnTextField();
		}
	}
}
